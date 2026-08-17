package com.nexdom.beneficiarios.pessoa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    Optional<Pessoa> findByCpf(String cpf);

    Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
