package com.nexdom.beneficiarios.beneficiario.dto;

import com.nexdom.beneficiarios.beneficiario.StatusBeneficiario;
import com.nexdom.beneficiarios.beneficiario.TipoBeneficiario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// pessoaId e tenantId não são editáveis: o vínculo com a pessoa e o tenant
// são definidos apenas na criação do beneficiário.
public record BeneficiarioUpdateRequest(
        @NotBlank String matricula,
        @NotNull TipoBeneficiario tipo,
        @NotNull StatusBeneficiario status,
        LocalDate dataAdesao
) {
}
