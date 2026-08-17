CREATE TABLE usuario (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login      VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    tenant_id  UUID NOT NULL,
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT fk_usuario_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id)
);

CREATE INDEX idx_usuario_tenant_id ON usuario (tenant_id);
