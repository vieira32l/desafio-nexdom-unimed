package com.nexdom.beneficiarios.pessoa;

import com.nexdom.beneficiarios.beneficiario.BeneficiarioRepository;
import com.nexdom.beneficiarios.pessoa.dto.PessoaCreateRequest;
import com.nexdom.beneficiarios.pessoa.dto.PessoaResponse;
import com.nexdom.beneficiarios.pessoa.dto.PessoaUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final BeneficiarioRepository beneficiarioRepository;

    public PessoaService(PessoaRepository pessoaRepository, BeneficiarioRepository beneficiarioRepository) {
        this.pessoaRepository = pessoaRepository;
        this.beneficiarioRepository = beneficiarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<PessoaResponse> listar(String nome, Pageable pageable) {
        Page<Pessoa> pagina = StringUtils.hasText(nome)
                ? pessoaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                : pessoaRepository.findAll(pageable);
        return pagina.map(PessoaResponse::from);
    }

    @Transactional(readOnly = true)
    public PessoaResponse buscarPorId(UUID id) {
        return PessoaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public PessoaResponse criar(PessoaCreateRequest request) {
        validarCpfDisponivel(request.cpf(), null);
        Pessoa pessoa = new Pessoa(request.nome(), request.cpf(), request.dataNascimento(), request.email());
        // flush imediato para que criadoEm/atualizadoEm (gerados pelo Hibernate só no flush)
        // já venham preenchidos na resposta deste mesmo request.
        return PessoaResponse.from(pessoaRepository.saveAndFlush(pessoa));
    }

    @Transactional
    public PessoaResponse atualizar(UUID id, PessoaUpdateRequest request) {
        Pessoa pessoa = buscarEntidade(id);
        validarCpfDisponivel(request.cpf(), id);
        pessoa.setNome(request.nome());
        pessoa.setCpf(request.cpf());
        pessoa.setDataNascimento(request.dataNascimento());
        pessoa.setEmail(request.email());
        // flush imediato para que atualizadoEm (gerado pelo Hibernate só no flush)
        // já venha atualizado na resposta deste mesmo request.
        return PessoaResponse.from(pessoaRepository.saveAndFlush(pessoa));
    }

    @Transactional
    public void deletar(UUID id) {
        Pessoa pessoa = buscarEntidade(id);

        // Pessoa é global (não tem tenant_id), então um vínculo em QUALQUER operadora
        // deve bloquear a exclusão — não só um vínculo no tenant do usuário logado.
        // Por isso essa checagem é feita sem filtro de tenant, de propósito.
        //
        // Isso funciona porque PessoaService nunca é interceptado pelo
        // BeneficiarioTenantAspect (o pointcut dele só cobre métodos de
        // BeneficiarioService) — então, aqui, a sessão do Postgres continua com o
        // usuário de conexão da aplicação (superuser), sem "SET LOCAL ROLE app_runtime"
        // nem app.current_tenant setados. Superuser sempre ignora RLS, então esta query
        // vê beneficiarios de todos os tenants mesmo com a policy de V6 habilitada.
        // Se um dia isso for refatorado para passar por um contexto com tenant setado,
        // NÃO adicione um filtro de tenant aqui — isso quebraria esta checagem, que
        // precisa ser global por design.
        if (beneficiarioRepository.existsByPessoaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir esta pessoa porque ela possui vínculo de beneficiário em uma ou mais "
                            + "operadoras. Remova os vínculos antes de excluir.");
        }

        pessoaRepository.delete(pessoa);
    }

    private Pessoa buscarEntidade(UUID id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada: " + id));
    }

    private void validarCpfDisponivel(String cpf, UUID idAtual) {
        pessoaRepository.findByCpf(cpf)
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado para outra pessoa.");
                });
    }
}
