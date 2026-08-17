package com.nexdom.beneficiarios.beneficiario.dto;

import com.nexdom.beneficiarios.beneficiario.Beneficiario;
import com.nexdom.beneficiarios.beneficiario.StatusBeneficiario;
import com.nexdom.beneficiarios.beneficiario.TipoBeneficiario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BeneficiarioResponse(
        UUID id,
        UUID pessoaId,
        String pessoaNome,
        UUID tenantId,
        String matricula,
        TipoBeneficiario tipo,
        StatusBeneficiario status,
        LocalDate dataAdesao,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static BeneficiarioResponse from(Beneficiario beneficiario) {
        return new BeneficiarioResponse(
                beneficiario.getId(),
                beneficiario.getPessoa().getId(),
                beneficiario.getPessoa().getNome(),
                beneficiario.getTenantId(),
                beneficiario.getMatricula(),
                beneficiario.getTipo(),
                beneficiario.getStatus(),
                beneficiario.getDataAdesao(),
                beneficiario.getCriadoEm(),
                beneficiario.getAtualizadoEm()
        );
    }
}
