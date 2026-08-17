package com.nexdom.beneficiarios.beneficiario;

import com.nexdom.beneficiarios.pessoa.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "beneficiario")
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBeneficiario tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusBeneficiario status;

    @Column(name = "data_adesao")
    private LocalDate dataAdesao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Beneficiario() {
    }

    public Beneficiario(Pessoa pessoa, UUID tenantId, String matricula, TipoBeneficiario tipo,
                         StatusBeneficiario status, LocalDate dataAdesao) {
        this.pessoa = pessoa;
        this.tenantId = tenantId;
        this.matricula = matricula;
        this.tipo = tipo;
        this.status = status;
        this.dataAdesao = dataAdesao;
    }

    public UUID getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    // Definido apenas no construtor: sem setter, para que nenhuma camada de API possa
    // reatribuir o tenant de um beneficiário já persistido.
    public UUID getTenantId() {
        return tenantId;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public TipoBeneficiario getTipo() {
        return tipo;
    }

    public void setTipo(TipoBeneficiario tipo) {
        this.tipo = tipo;
    }

    public StatusBeneficiario getStatus() {
        return status;
    }

    public void setStatus(StatusBeneficiario status) {
        this.status = status;
    }

    public LocalDate getDataAdesao() {
        return dataAdesao;
    }

    public void setDataAdesao(LocalDate dataAdesao) {
        this.dataAdesao = dataAdesao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
