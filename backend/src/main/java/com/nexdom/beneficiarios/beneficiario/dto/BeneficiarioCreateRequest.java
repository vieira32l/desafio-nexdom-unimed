package com.nexdom.beneficiarios.beneficiario.dto;

import com.nexdom.beneficiarios.beneficiario.StatusBeneficiario;
import com.nexdom.beneficiarios.beneficiario.TipoBeneficiario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

// Sem campo tenantId de propósito: o tenant nunca vem do corpo da requisição,
// é sempre resolvido a partir do TenantContext do usuário autenticado.
public record BeneficiarioCreateRequest(
        @NotNull UUID pessoaId,
        @NotBlank String matricula,
        @NotNull TipoBeneficiario tipo,
        @NotNull StatusBeneficiario status,
        LocalDate dataAdesao
) {
}
