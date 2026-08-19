# Desafio Nexdom/Unimed: Beneficiários

Cadastro de Pessoas (globais) e Beneficiários (por operadora/tenant) com autenticação JWT, isolamento multitenant em duas camadas e frontend em Vue 3.

Monorepo:

```
backend/    API Spring Boot 3 (Java 21)
frontend/   SPA Vue 3 + TypeScript
```

## Como subir

Pré-requisito: Docker e Docker Compose.

```bash
docker compose up
```

Isso builda as imagens (se ainda não existirem), sobe Postgres, aplica todas as migrations Flyway (incluindo o seed de dados de exemplo) e sobe a API e o frontend. **Sem nenhum passo manual**: nada de rodar migration à parte, criar usuário, ou popular dado.

Para simular um clone novo do zero (limpando inclusive o volume do banco):

```bash
docker compose down -v && docker compose up
```

## Portas

| Serviço | URL | Descrição |
|---|---|---|
| Frontend | http://localhost:8080 | SPA Vue servida por Nginx |
| Backend | http://localhost:8081 | API REST (Spring Boot) |
| Backend (Swagger UI) | http://localhost:8081/swagger-ui.html | Documentação interativa da API |
| Postgres | localhost:5432 | Banco (acessível do host, ex. via `psql`/DBeaver) |

Se `8080`/`8081` já estiverem em uso, crie um `.env` na raiz:

```env
FRONTEND_PORT=9090
BACKEND_PORT=9091
```

O CORS do backend se ajusta sozinho a essas portas (é lido em runtime). Já a URL da API que o frontend usa é embutida no bundle **em build time**: depois de mudar as portas, rode `docker compose up --build` (não só `up`) para o frontend ser gerado de novo com a porta certa.

## Usuários de teste (seed)

A troca de tenant neste desafio acontece via login com usuários diferentes. Não existe seletor de tenant na UI. A senha é a mesma para todos: **`123456`**.

| Login | Senha | Tenant |
|---|---|---|
| `admin.sp` | `123456` | Unimed São Paulo |
| `admin.rj` | `123456` | Unimed Rio |
| `admin.mg` | `123456` | Unimed Minas |

O tenant ativo fica sempre visível no topo da aplicação ("Você está em: ...") depois do login.

O seed também cria 6 pessoas globais e 9 beneficiários distribuídos entre os três tenants, incluindo pessoas que são beneficiárias em mais de um tenant, para deixar explícito que `Pessoa` é uma entidade global, não presa a um tenant.

## Stack

- **Backend**: Spring Boot 3.5, Java 21, Spring Data JPA (Hibernate), Spring Security + JWT (`jjwt`), Flyway, PostgreSQL, springdoc-openapi, Spring AOP.
- **Frontend**: Vue 3 (Composition API), TypeScript, Vite, Vue Router, Pinia, Axios.
- **Infra**: Docker multi-stage builds (Maven+JDK → JRE enxuto; Node → Nginx), Docker Compose.

## Desenvolvimento local (sem Docker)

Ver [backend/README.md](backend/README.md) para detalhes de RLS e da arquitetura de isolamento. Resumidamente: com o Postgres do `docker compose` já no ar, dá para rodar `./mvnw spring-boot:run` (backend, porta 8080) e `npm run dev` (frontend, porta 5173) direto na máquina. O CORS já contempla `localhost:5173` por padrão.

---

## Decisões Arquiteturais

- **Isolamento**: banco único + `tenant_id`, reforçado por Row-Level Security.
- **Identificação do tenant**: claim assinado no JWT, nunca em header solto.
- **Autenticação**: simulada (login + JWT), sem gestão de identidade completa.
- **RLS**: camada extra de defesa, não substitui o filtro explícito da aplicação.

Raciocínio completo de cada decisão abaixo (clique para expandir).

<details>
<summary><strong>Banco único com <code>tenant_id</code>, em vez de schema-por-tenant ou banco-por-tenant</strong></summary>

Cada tenant é uma linha na tabela `tenant`; `Beneficiario` carrega `tenant_id` como coluna, sem schema ou banco separado por operadora. Motivos:

- **Pessoa é global por design.** O desafio pede explicitamente que uma mesma pessoa possa ser beneficiária em tenants diferentes. Com schema-por-tenant ou banco-por-tenant isso complica a modelagem: a pessoa é duplicada em cada schema (perdendo a garantia de ser "a mesma pessoa"), ou fica isolada num schema à parte com referências cross-schema, que o Postgres não trata como FK de verdade. Com banco único, `beneficiario.pessoa_id → pessoa.id` é uma FK normal, sem gambiarra.
- **Provisionar um tenant novo é um `INSERT`.** Com schema/banco por tenant, onboarding de uma operadora nova significa rodar a pipeline de migrations de novo (uma vez por schema/banco): infraestrutura, não dado de negócio.
- **Simplicidade operacional.** Uma única connection pool, uma única pipeline de migration, sem lógica de roteamento dinâmico de schema/banco por requisição.
- O trade-off real é isolamento mais fraco "de fábrica": um bug no filtro por `tenant_id` pode vazar dado entre tenants, algo que schema/banco separado tornaria estruturalmente impossível. A camada de Row-Level Security existe exatamente para fechar essa lacuna, sem abrir mão da simplicidade do banco único.

</details>

<details>
<summary><strong>JWT carregando o tenant, em vez de header customizado ou subdomínio</strong></summary>

O token emitido em `POST /auth/login` carrega `tenantId` e `tenantNome` como claims assinados, extraídos no `JwtAuthenticationFilter` para um `TenantContext` por requisição.

- **Um header customizado (`X-Tenant-Id`) exigiria confiar no cliente.** Nada impediria a requisição de mandar um tenant diferente do real, a menos que o servidor validasse o header contra outra fonte de verdade, o que o tornaria redundante. O JWT assinado elimina essa classe de problema: o valor vem de algo que o cliente não pode forjar sem invalidar a assinatura.
- **Subdomínio por tenant** (`sp.app.com`, `rio.app.com`) é legítimo em SaaS multitenant, mas custa infraestrutura (DNS wildcard, certificado TLS por subdomínio ou wildcard, roteamento adicional) desproporcional ao escopo do desafio, e ainda precisaria de autenticação por trás pra confirmar o tenant, o que o JWT já resolve sozinho.
- Na prática: a API vive numa única origem, sem roteamento por domínio, e o frontend nunca precisa "lembrar" de enviar o tenant certo. Ele simplesmente não tem essa informação para errar.

</details>

<details>
<summary><strong>Autenticação simulada, não um sistema de auth completo</strong></summary>

O login valida usuário/senha (BCrypt) e emite um JWT válido por 8h, mas não há cadastro de usuário via API, troca de senha, recuperação de senha, refresh token ou revogação. Isso foi deliberado:

- O desafio pede explicitamente troca de tenant "via login simulado com usuários diferentes por tenant", não gestão de identidade completa.
- Implementar só o suficiente (login, JWT assinado, claims de tenant, guard de rotas) já demonstra o mecanismo central pedido (isolamento multitenant a partir do usuário autenticado), sem o tempo desproporcional de uma auth completa, que é essencialmente um projeto à parte.
- "Simulada" não é sinônimo de insegura: hash BCrypt de verdade na senha, JWT assinado (HS384) e validado a cada requisição, CORS restrito às origens conhecidas. A limitação é de escopo, não de correção.

</details>

<details>
<summary><strong>Row-Level Security como camada extra (defesa em profundidade)</strong></summary>

A migration `V6` habilita RLS na tabela `beneficiario` como complemento, não substituto, do filtro explícito por `tenant_id` que já existe em `BeneficiarioRepository`/`BeneficiarioService`.

- O filtro explícito é a camada "de negócio": rápida, com mensagens de erro específicas (404 vs 409 vs 422), onde a autorização por tenant vive no dia a dia. Mas depende inteiramente do código Java estar correto: um método de repositório sem o filtro, uma query mal escrita, ou uma ferramenta de admin conectando direto no banco ignoram essa camada por completo, porque ela só existe em código de aplicação.
- RLS é imposta pelo próprio Postgres, em qualquer conexão, qualquer query, qualquer cliente. Confirmado na prática: um `UPDATE` via SQL puro, sem `WHERE tenant_id = ...` nenhum, tentando alterar a linha de outro tenant, retornou `UPDATE 0`. A linha é literalmente invisível para a sessão, com ou sem código Java no meio.
- O domínio é de operadoras de saúde: vazamento de dado entre tenants (operadoras concorrentes, no fim das contas) é o tipo de falha que justifica duas camadas independentes de proteção, não só uma.

Detalhes de implementação (por que existe um role `app_runtime`, como o tenant chega até a sessão do Postgres, etc.) estão em [backend/README.md](backend/README.md).

</details>

---

## O que eu faria diferente com mais tempo

- **Testes automatizados mais completos.** Hoje existem só dois testes de backend: um smoke test de contexto e um teste de integração (Testcontainers) especificamente para isolamento entre tenants em `Beneficiario`. Faltam testes unitários para as regras de negócio dos services (CPF/matrícula duplicados, pessoa inexistente, etc.) mockando os repositórios, testes cobrindo os demais cenários de `ProblemDetail`, e nenhum teste automatizado de frontend: toda a validação de UI neste desafio foi manual, via navegador.

<details>
<summary><strong>Row-Level Security</strong> (implementada, ver seção acima e <code>backend/README.md</code>)</summary>

Com mais tempo eu iria além do que está hoje: um teste JUnit dedicado que prove RLS via SQL bruto (a prova que existe hoje foi feita manualmente e documentada, não virou um teste reproduzível no CI), e avaliar se faz sentido dar a cada tenant um role de banco de fato distinto: hoje todos compartilham o mesmo role `app_runtime`, isolados só pela variável de sessão `app.current_tenant`.

</details>

- **Autenticação mais robusta.** Sem refresh token, o usuário é simplesmente deslogado quando o JWT expira (8h), sem renovação silenciosa. Não há recuperação de senha, nem revogação/blacklist de token: o "logout" hoje só limpa o token no frontend, e um JWT emitido continua criptograficamente válido no backend até expirar, mesmo depois do usuário clicar em "Sair". Adicionaria também rotação de secret e, dependendo do requisito, MFA.

<details>
<summary><strong>Observabilidade</strong></summary>

Logs hoje são o padrão do Spring Boot/Logback em texto, sem correlation ID por requisição nem formato estruturado (JSON) para agregação em ferramentas como ELK/Datadog. O Actuator expõe só `/health`; faltam métricas de negócio (ex. beneficiários criados por tenant) e técnicas (latência e taxa de erro por endpoint/tenant) via Micrometer, e, se a arquitetura crescer para múltiplos serviços, tracing distribuído.

</details>

- **Vínculo entre dependente e titular.** Hoje o sistema não modela a relação entre um `Beneficiario` do tipo `DEPENDENTE` e o `Beneficiario` `TITULAR` ao qual ele está vinculado: o campo `tipo` indica que é um dependente, mas não indica de quem. Com mais tempo, adicionaria um campo (ex.: `titularId`, referência a outro `Beneficiario` do mesmo tenant com `tipo = TITULAR`) para modelar esse vínculo corretamente.
