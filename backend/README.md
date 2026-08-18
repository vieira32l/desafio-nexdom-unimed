# beneficiarios-api

API do desafio Nexdom/Unimed: cadastro de Pessoas (global) e Beneficiários (por tenant), com autenticação JWT e isolamento multitenant em duas camadas independentes.

## Rodando

Veja o `docker-compose.yml` na raiz do monorepo — `docker compose up` sobe banco, migrations (com seed) e API automaticamente. Usuários de seed: `admin.sp`, `admin.rio`, `admin.mg` (senha `123456` para todos).

## Isolamento multitenant: duas camadas independentes

`Beneficiario` é a única entidade com `tenant_id` (Pessoa é intencionalmente global — a mesma pessoa pode ser beneficiária em tenants diferentes). O isolamento entre tenants é garantido por **duas camadas que não dependem uma da outra**:

### Camada 1 — filtro explícito na aplicação (primeira linha de defesa)

Todo método de `BeneficiarioRepository`/`BeneficiarioService` recebe `tenantId` como parâmetro explícito e o usa no `WHERE` da query (`findByIdAndTenantId`, `findByTenantIdAndMatricula`, etc. — ver `BeneficiarioRepository`). O `tenantId` vem do `TenantContext` (populado pelo `JwtAuthenticationFilter` a partir do claim do JWT) e **nunca** do corpo da requisição.

Essa é a camada "de negócio": rápida, expressiva nas mensagens de erro (404 em vez de um erro genérico de banco), e é onde a maior parte da lógica de autorização por tenant vive no dia a dia.

**Limitação:** ela depende inteiramente do código Java estar correto. Um novo método de repositório escrito sem o filtro de `tenantId`, um `@Query` mal escrito, uma migration futura que rode SQL bruto, ou até uma ferramenta de admin/BI conectando direto no banco — qualquer um desses cenários ignora essa camada por completo, porque ela só existe em código de aplicação.

### Camada 2 — Row-Level Security no Postgres (defesa em profundidade)

A migration `V6__enable_row_level_security_beneficiario.sql` habilita RLS na tabela `beneficiario` com uma policy que só permite ver/alterar linhas onde `tenant_id = current_setting('app.current_tenant', true)::uuid`. Diferente da camada 1, essa regra é **imposta pelo próprio banco**, em qualquer conexão, qualquer query, qualquer cliente — inclusive um `psql` direto, um `UPDATE` manual, uma ferramenta externa, ou um bug futuro que esqueça o filtro de tenant na camada 1.

Isso foi confirmado na prática durante o desenvolvimento: um `UPDATE beneficiario SET status = 'INATIVO' WHERE matricula = 'SP0001'` rodado via SQL puro, sem nenhum `WHERE tenant_id = ...`, com o contexto de outro tenant ativo na sessão, resultou em `UPDATE 0` — o Postgres simplesmente não enxerga a linha, então não há o que atualizar. Nenhum código Java estava envolvido nesse teste.

**Por que ela é só uma segunda camada, não a única:** RLS sozinho não teria as mensagens de erro específicas (404 vs 409 vs 422) nem cobre validações de negócio (matrícula duplicada, pessoa inexistente); ela é puramente sobre "quais linhas essa sessão pode tocar". As duas camadas são complementares, não substitutas uma da outra.

#### Detalhe de infraestrutura: o papel do `app_runtime`

A imagem oficial do Postgres cria o usuário definido em `POSTGRES_USER` (`app_user`) como **superuser** do cluster (é o "bootstrap superuser"). RLS nunca se aplica a superusers — nem com `FORCE ROW LEVEL SECURITY`, que só afeta o dono não-superuser de uma tabela — e o Postgres nem deixa o bootstrap superuser abrir mão do próprio atributo `SUPERUSER`. Ou seja: com a aplicação conectando como `app_user`, a policy de RLS simplesmente não teria efeito nenhum, dando uma falsa sensação de segurança.

A migration V6 cria um segundo role, `app_runtime` (`NOLOGIN`, `NOSUPERUSER`, com `SELECT/INSERT/UPDATE/DELETE` em `beneficiario` e `SELECT` em `pessoa`), e a sessão "veste" esse role apenas durante as transações do `BeneficiarioService`, via `SET LOCAL ROLE app_runtime` — que reverte sozinho no fim da transação (commit ou rollback), então a próxima transação na mesma conexão do pool começa limpa. Isso evita ter que trocar as credenciais de conexão da aplicação em vários lugares (o que quebraria o Flyway, que precisa continuar como dono/superuser para aplicar migrations).

### Como o backend fixa o tenant na sessão do Postgres

`BeneficiarioTenantAspect` (`@Aspect`, `@Before` em torno de todo método público de `BeneficiarioService`) executa, a cada transação:

1. `SET LOCAL ROLE app_runtime` — veste o role sem privilégio de bypass de RLS.
2. `SELECT set_config('app.current_tenant', ?, true)` — fixa o tenant atual (o `?` é um bind parameter de verdade, via `PreparedStatement`; `SET LOCAL app.current_tenant = '<uuid>'` não aceita parâmetro no valor, e concatenar string ali seria abrir espaço para injeção à toa).

Isso precisa rodar **depois** que a transação (e a conexão JDBC) já foi aberta pelo advisor de `@Transactional` do Spring — por isso `TransactionManagementConfig` fixa `@EnableTransactionManagement(order = HIGHEST_PRECEDENCE)`, garantindo que o advisor transacional envolva o aspecto por fora (e não o contrário, o que deixaria o `SET LOCAL` "solto" fora de qualquer transação, sem efeito nas queries seguintes).

## CPF e validação

Veja `com.nexdom.beneficiarios.validation.CPF` — validação por algoritmo de dígito verificador (não só formato), replicada no frontend (`frontend/src/utils/cpf.ts`) para feedback imediato, mas sempre revalidada no backend.
