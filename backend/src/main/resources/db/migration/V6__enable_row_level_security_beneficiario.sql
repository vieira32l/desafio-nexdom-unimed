-- Row-Level Security em beneficiario: segunda camada de isolamento multitenant,
-- independente do filtro explícito por tenant_id já existente na aplicação
-- (BeneficiarioRepository/BeneficiarioService). Ver README para a justificativa completa.

-- IMPORTANTE: a imagem oficial do Postgres cria o usuário definido em POSTGRES_USER
-- (aqui, "app_user") como SUPERUSER (é o "bootstrap superuser" do cluster). RLS nunca
-- se aplica a superusers — nem com FORCE ROW LEVEL SECURITY, que só afeta o dono
-- não-superuser de uma tabela. E o Postgres nem deixa o bootstrap superuser abrir mão
-- do próprio atributo SUPERUSER via ALTER ROLE.
--
-- Por isso a aplicação não passa a se conectar com um usuário diferente (isso mudaria
-- credenciais em vários lugares, incluindo o Flyway, que precisa continuar rodando
-- como dono/superuser pra aplicar migrations). Em vez disso, criamos um role sem
-- privilégio de login e sem SUPERUSER, e a sessão "veste" esse role só durante as
-- transações do BeneficiarioService, via SET LOCAL ROLE (reverte sozinho no fim da
-- transação — ver BeneficiarioTenantAspect).
--
-- GRANT via current_user (não "app_user" fixo) porque essa mesma migration roda tanto
-- no docker-compose (usuário "app_user") quanto no Testcontainers dos testes de
-- integração (usuário gerado pelo container, ex. "test") — hardcodear o nome quebraria
-- um dos dois ambientes.
CREATE ROLE app_runtime NOLOGIN NOSUPERUSER;

DO $$
BEGIN
    EXECUTE format('GRANT app_runtime TO %I', current_user);
END
$$;

GRANT USAGE ON SCHEMA public TO app_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON beneficiario TO app_runtime;
-- BeneficiarioService.criar() consulta pessoa (para validar o pessoaId) e o JOIN FETCH
-- do BeneficiarioRepository também lê pessoa — a sessão já está como app_runtime
-- nesse ponto, então precisa desse SELECT para não quebrar a criação/listagem.
GRANT SELECT ON pessoa TO app_runtime;

ALTER TABLE beneficiario ENABLE ROW LEVEL SECURITY;

-- FORCE: mesmo sem efeito sobre app_user (superuser), é o que faz a policy valer
-- para app_runtime mesmo que ele um dia venha a ser dono da tabela.
ALTER TABLE beneficiario FORCE ROW LEVEL SECURITY;

-- FOR ALL (não só SELECT/UPDATE/DELETE): sem cobrir INSERT também, toda inserção
-- ficaria bloqueada por padrão assim que RLS é habilitado (uma policy que não cobre
-- um comando bloqueia esse comando inteiramente). USING controla quais linhas
-- existentes são visíveis/afetáveis; WITH CHECK controla o valor de tenant_id
-- permitido em linhas novas ou atualizadas — ambos usam a mesma condição.
--
-- current_setting(..., true) com missing_ok=true: se app.current_tenant não tiver
-- sido definido na sessão, a comparação vira "tenant_id = NULL", que nunca é
-- verdadeira — sessão sem o tenant setado não enxerga nenhuma linha, em vez de
-- estourar um erro toda vez que alguém conectar via psql para debug.
CREATE POLICY beneficiario_tenant_isolation ON beneficiario
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);
