-- Dados de exemplo para desenvolvimento/demonstração.
-- Senha de todos os usuários: "123456" (hash BCrypt abaixo).

-- Tenants
INSERT INTO tenant (id, nome, slug) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Unimed São Paulo', 'unimed-sp'),
    ('10000000-0000-0000-0000-000000000002', 'Unimed Rio', 'unimed-rio'),
    ('10000000-0000-0000-0000-000000000003', 'Unimed Minas', 'unimed-mg');

-- Usuários (1 por tenant)
INSERT INTO usuario (id, login, senha_hash, tenant_id) VALUES
    ('20000000-0000-0000-0000-000000000001', 'admin.sp', '$2a$10$ZzAC6XoU0W/GpL5deLvrReBYGTepWniTrOLyNRaL004ACVm/AwWCu', '10000000-0000-0000-0000-000000000001'),
    ('20000000-0000-0000-0000-000000000002', 'admin.rio', '$2a$10$KmJS5C76yeo5BcO3q06DLu67oei1QagNwImwUAozhd72rnAPrUIAK', '10000000-0000-0000-0000-000000000002'),
    ('20000000-0000-0000-0000-000000000003', 'admin.mg', '$2a$10$sRtITsOeei64xcCoOsuKpem6x0Bp/6A/RxIz9IZXpgEYhCDwBfrvK', '10000000-0000-0000-0000-000000000003');

-- Pessoas (globais, não pertencem a nenhum tenant)
INSERT INTO pessoa (id, nome, cpf, data_nascimento, email) VALUES
    ('30000000-0000-0000-0000-000000000001', 'Ana Beatriz Souza',       '12345678909', '1990-03-14', 'ana.souza@example.com'),
    ('30000000-0000-0000-0000-000000000002', 'Carlos Eduardo Lima',     '98765432100', '1985-07-22', 'carlos.lima@example.com'),
    ('30000000-0000-0000-0000-000000000003', 'Fernanda Oliveira Costa', '11122233396', '1992-11-05', 'fernanda.costa@example.com'),
    ('30000000-0000-0000-0000-000000000004', 'João Pedro Santos',       '22233344405', '1978-01-30', 'joao.santos@example.com'),
    ('30000000-0000-0000-0000-000000000005', 'Mariana Alves Pereira',   '33344455508', '2001-09-17', 'mariana.pereira@example.com'),
    ('30000000-0000-0000-0000-000000000006', 'Ricardo Gomes Ferreira',  '44455566619', '1995-05-08', NULL);

-- Beneficiários distribuídos entre os tenants.
-- Ana (P1), Carlos (P2) e Fernanda (P3) aparecem em mais de um tenant,
-- deixando explícito que Pessoa é uma entidade global compartilhada.

-- Unimed São Paulo
INSERT INTO beneficiario (id, pessoa_id, tenant_id, matricula, tipo, status, data_adesao) VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'SP0001', 'TITULAR',    'ATIVO',   '2020-01-10'),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'SP0002', 'DEPENDENTE', 'ATIVO',   '2020-01-10'),
    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'SP0003', 'TITULAR',    'INATIVO', '2019-06-01');

-- Unimed Rio
INSERT INTO beneficiario (id, pessoa_id, tenant_id, matricula, tipo, status, data_adesao) VALUES
    ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'RJ0001', 'TITULAR',    'ATIVO', '2021-03-15'),
    ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'RJ0002', 'TITULAR',    'ATIVO', '2018-08-20'),
    ('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002', 'RJ0003', 'DEPENDENTE', 'ATIVO', '2022-02-01');

-- Unimed Minas
INSERT INTO beneficiario (id, pessoa_id, tenant_id, matricula, tipo, status, data_adesao) VALUES
    ('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'MG0001', 'DEPENDENTE', 'ATIVO',   '2023-04-12'),
    ('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000003', 'MG0002', 'TITULAR',    'ATIVO',   '2017-10-09'),
    ('40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'MG0003', 'TITULAR',    'INATIVO', '2016-12-25');
