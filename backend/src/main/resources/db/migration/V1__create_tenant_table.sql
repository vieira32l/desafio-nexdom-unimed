CREATE TABLE tenant (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    CONSTRAINT uk_tenant_slug UNIQUE (slug)
);
