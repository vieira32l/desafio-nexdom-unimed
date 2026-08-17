package com.nexdom.beneficiarios.pessoa.dto;

import com.nexdom.beneficiarios.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PessoaUpdateRequest(
        @NotBlank String nome,
        @NotBlank @CPF String cpf,
        @PastOrPresent LocalDate dataNascimento,
        @Email String email
) {
}
