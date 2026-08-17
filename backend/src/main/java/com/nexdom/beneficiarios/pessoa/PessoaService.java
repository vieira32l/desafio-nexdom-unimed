package com.nexdom.beneficiarios.pessoa;

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

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
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
        pessoaRepository.delete(buscarEntidade(id));
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
