CREATE TABLE beneficiario (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pessoa_id      UUID NOT NULL,
    tenant_id      UUID NOT NULL,
    matricula      VARCHAR(50) NOT NULL,
    tipo           VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    data_adesao    DATE,
    criado_em      TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_beneficiario_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa (id),
    CONSTRAINT fk_beneficiario_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id),
    CONSTRAINT uk_beneficiario_tenant_matricula UNIQUE (tenant_id, matricula),
    CONSTRAINT ck_beneficiario_tipo CHECK (tipo IN ('TITULAR', 'DEPENDENTE')),
    CONSTRAINT ck_beneficiario_status CHECK (status IN ('ATIVO', 'INATIVO'))
);

CREATE INDEX idx_beneficiario_tenant_id ON beneficiario (tenant_id);
