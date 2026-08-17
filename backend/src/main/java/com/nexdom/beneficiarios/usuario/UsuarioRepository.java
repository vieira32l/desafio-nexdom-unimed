package com.nexdom.beneficiarios.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.tenant WHERE u.login = :login")
    Optional<Usuario> findByLogin(@Param("login") String login);
}
