package com.nexdom.beneficiarios.beneficiario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, UUID> {

    @Query("SELECT b FROM Beneficiario b JOIN FETCH b.pessoa WHERE b.id = :id AND b.tenantId = :tenantId")
    Optional<Beneficiario> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    Optional<Beneficiario> findByTenantIdAndMatricula(UUID tenantId, String matricula);

    @Query("""
            SELECT b FROM Beneficiario b JOIN FETCH b.pessoa p
            WHERE b.tenantId = :tenantId
              AND (:nomePessoa IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:nomePessoa AS string), '%')))
              AND (:status IS NULL OR b.status = :status)
            """)
    Page<Beneficiario> buscar(@Param("tenantId") UUID tenantId,
                               @Param("nomePessoa") String nomePessoa,
                               @Param("status") StatusBeneficiario status,
                               Pageable pageable);
}
