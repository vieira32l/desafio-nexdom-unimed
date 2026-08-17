CREATE TABLE pessoa (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome             VARCHAR(255) NOT NULL,
    cpf              VARCHAR(11) NOT NULL,
    data_nascimento  DATE,
    email            VARCHAR(255),
    criado_em        TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_pessoa_cpf UNIQUE (cpf)
);
