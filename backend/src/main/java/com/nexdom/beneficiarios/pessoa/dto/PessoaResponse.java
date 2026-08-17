package com.nexdom.beneficiarios.pessoa.dto;

import com.nexdom.beneficiarios.pessoa.Pessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PessoaResponse(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static PessoaResponse from(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getEmail(),
                pessoa.getCriadoEm(),
                pessoa.getAtualizadoEm()
        );
    }
}
