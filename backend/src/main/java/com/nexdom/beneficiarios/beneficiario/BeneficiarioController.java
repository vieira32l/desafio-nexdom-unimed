package com.nexdom.beneficiarios.beneficiario;

import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioCreateRequest;
import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioResponse;
import com.nexdom.beneficiarios.beneficiario.dto.BeneficiarioUpdateRequest;
import com.nexdom.beneficiarios.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/beneficiarios")
public class BeneficiarioController {

    private final BeneficiarioService beneficiarioService;

    public BeneficiarioController(BeneficiarioService beneficiarioService) {
        this.beneficiarioService = beneficiarioService;
    }

    @GetMapping
    public PagedModel<BeneficiarioResponse> listar(@RequestParam(required = false) String nomePessoa,
                                                     @RequestParam(required = false) StatusBeneficiario status,
                                                     @PageableDefault(size = 20, sort = "matricula") Pageable pageable) {
        return new PagedModel<>(beneficiarioService.listar(tenantAtual(), nomePessoa, status, pageable));
    }

    @GetMapping("/{id}")
    public BeneficiarioResponse buscarPorId(@PathVariable UUID id) {
        return beneficiarioService.buscarPorId(id, tenantAtual());
    }

    @PostMapping
    public ResponseEntity<BeneficiarioResponse> criar(@Valid @RequestBody BeneficiarioCreateRequest request) {
        BeneficiarioResponse beneficiario = beneficiarioService.criar(request, tenantAtual());
        return ResponseEntity.created(URI.create("/beneficiarios/" + beneficiario.id())).body(beneficiario);
    }

    @PutMapping("/{id}")
    public BeneficiarioResponse atualizar(@PathVariable UUID id, @Valid @RequestBody BeneficiarioUpdateRequest request) {
        return beneficiarioService.atualizar(id, tenantAtual(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        beneficiarioService.deletar(id, tenantAtual());
    }

    // O JwtAuthenticationFilter garante que, para qualquer requisição autenticada
    // que chegue até aqui, o TenantContext já está populado; o 401 abaixo é apenas
    // uma rede de segurança caso essa invariante seja quebrada no futuro.
    private UUID tenantAtual() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant não identificado no token.");
        }
        return tenantId;
    }
}
