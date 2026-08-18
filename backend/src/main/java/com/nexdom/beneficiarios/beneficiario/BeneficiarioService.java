package com.nexdom.beneficiarios.beneficiario;

import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioCreateRequest;
import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioResponse;
import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioUpdateRequest;
import com.nexdom.beneficiarios.pessoa.Pessoa;
import com.nexdom.beneficiarios.pessoa.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final PessoaRepository pessoaRepository;

    public BeneficiarioService(BeneficiarioRepository beneficiarioRepository, PessoaRepository pessoaRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public Page<BeneficiarioResponse> listar(UUID tenantId, String nomePessoa, StatusBeneficiario status, Pageable pageable) {
        String filtroNome = StringUtils.hasText(nomePessoa) ? nomePessoa : null;
        return beneficiarioRepository.buscar(tenantId, filtroNome, status, pageable).map(BeneficiarioResponse::from);
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponse buscarPorId(UUID id, UUID tenantId) {
        return BeneficiarioResponse.from(buscarEntidade(id, tenantId));
    }

    @Transactional
    public BeneficiarioResponse criar(BeneficiarioCreateRequest request, UUID tenantId) {
        Pessoa pessoa = buscarPessoa(request.pessoaId());
        validarMatriculaDisponivel(tenantId, request.matricula(), null);
        validarVinculoAtivoUnico(request.pessoaId(), tenantId, request.tipo(), request.status(), null);

        Beneficiario beneficiario = new Beneficiario(
                pessoa, tenantId, request.matricula(), request.tipo(), request.status(), request.dataAdesao());

        // flush imediato para que criadoEm/atualizadoEm (gerados pelo Hibernate só no flush)
        // já venham preenchidos na resposta deste mesmo request.
        return BeneficiarioResponse.from(beneficiarioRepository.saveAndFlush(beneficiario));
    }

    @Transactional
    public BeneficiarioResponse atualizar(UUID id, UUID tenantId, BeneficiarioUpdateRequest request) {
        Beneficiario beneficiario = buscarEntidade(id, tenantId);
        validarMatriculaDisponivel(tenantId, request.matricula(), id);
        validarVinculoAtivoUnico(beneficiario.getPessoa().getId(), tenantId, request.tipo(), request.status(), id);

        beneficiario.setMatricula(request.matricula());
        beneficiario.setTipo(request.tipo());
        beneficiario.setStatus(request.status());
        beneficiario.setDataAdesao(request.dataAdesao());

        return BeneficiarioResponse.from(beneficiarioRepository.saveAndFlush(beneficiario));
    }

    @Transactional
    public void deletar(UUID id, UUID tenantId) {
        beneficiarioRepository.delete(buscarEntidade(id, tenantId));
    }

    private Beneficiario buscarEntidade(UUID id, UUID tenantId) {
        return beneficiarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado: " + id));
    }

    private Pessoa buscarPessoa(UUID pessoaId) {
        return pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Pessoa não encontrada: " + pessoaId));
    }

    private void validarMatriculaDisponivel(UUID tenantId, String matricula, UUID idAtual) {
        beneficiarioRepository.findByTenantIdAndMatricula(tenantId, matricula)
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cadastrada para este tenant.");
                });
    }

    // A mesma pessoa pode ter um TITULAR ativo e um DEPENDENTE ativo ao mesmo tempo no
    // mesmo tenant — a restrição é só entre registros do mesmo tipo. Um cadastro
    // INATIVO nunca é bloqueado por essa regra, então só valida quando o resultado da
    // operação for ATIVO.
    private void validarVinculoAtivoUnico(UUID pessoaId, UUID tenantId, TipoBeneficiario tipo,
                                           StatusBeneficiario status, UUID idAtual) {
        if (status != StatusBeneficiario.ATIVO) {
            return;
        }

        beneficiarioRepository
                .findByPessoaIdAndTenantIdAndTipoAndStatus(pessoaId, tenantId, tipo, StatusBeneficiario.ATIVO)
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Já existe um vínculo ativo como " + tipo + " para esta pessoa neste tenant. "
                                    + "Inative o vínculo atual antes de criar ou ativar um novo.");
                });
    }
}
