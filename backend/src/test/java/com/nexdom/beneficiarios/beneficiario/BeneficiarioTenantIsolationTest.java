package com.nexdom.beneficiarios.beneficiario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, com uma aplicação real e um Postgres real (Testcontainers), que um usuário
 * autenticado do tenant A nunca consegue ler, editar ou excluir um beneficiário do
 * tenant B — mesmo sabendo o id exato do registro.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BeneficiarioTenantIsolationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private String tokenSp;
    private String tokenRio;

    @BeforeEach
    void login() {
        tokenSp = login("admin.sp", "123456");
        tokenRio = login("admin.rio", "123456");
    }

    @Test
    void usuarioDeUmTenantNuncaConsegueLerEditarOuExcluirBeneficiarioDeOutroTenant() {
        // Pessoa global do seed (V5), usada só como vínculo válido para o beneficiário de teste.
        UUID pessoaGlobalId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        // 1) Tenant RIO cria um beneficiário próprio.
        Map<String, Object> criarBody = Map.of(
                "pessoaId", pessoaGlobalId.toString(),
                "matricula", "ISO-TEST-001",
                "tipo", "TITULAR",
                "status", "ATIVO"
        );
        ResponseEntity<Map> criado = restTemplate.postForEntity(
                "/beneficiarios", authenticated(tokenRio, criarBody), Map.class);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String beneficiarioId = (String) criado.getBody().get("id");
        String path = "/beneficiarios/" + beneficiarioId;

        // 2) Sanity check: o próprio tenant RIO consegue ler o registro (ele existe de fato).
        ResponseEntity<Map> leituraPropria = restTemplate.exchange(path, HttpMethod.GET, authenticated(tokenRio), Map.class);
        assertThat(leituraPropria.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3) Tenant SP tenta LER o beneficiário do tenant RIO pelo id exato -> 404, nunca 403.
        ResponseEntity<Map> leituraCruzada = restTemplate.exchange(path, HttpMethod.GET, authenticated(tokenSp), Map.class);
        assertThat(leituraCruzada.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 4) Tenant SP tenta EDITAR o beneficiário do tenant RIO -> 404.
        Map<String, Object> tentativaEdicao = Map.of(
                "matricula", "HACKED",
                "tipo", "TITULAR",
                "status", "INATIVO"
        );
        ResponseEntity<Map> edicaoCruzada = restTemplate.exchange(
                path, HttpMethod.PUT, authenticated(tokenSp, tentativaEdicao), Map.class);
        assertThat(edicaoCruzada.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 5) Tenant SP tenta EXCLUIR o beneficiário do tenant RIO -> 404.
        ResponseEntity<Void> exclusaoCruzada = restTemplate.exchange(path, HttpMethod.DELETE, authenticated(tokenSp), Void.class);
        assertThat(exclusaoCruzada.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 6) As tentativas do SP não tiveram nenhum efeito: o registro do RIO continua intacto.
        ResponseEntity<Map> confirmacao = restTemplate.exchange(path, HttpMethod.GET, authenticated(tokenRio), Map.class);
        assertThat(confirmacao.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmacao.getBody().get("matricula")).isEqualTo("ISO-TEST-001");
        assertThat(confirmacao.getBody().get("status")).isEqualTo("ATIVO");

        // 7) O beneficiário do tenant RIO nunca aparece na listagem do tenant SP.
        ResponseEntity<Map> listagemSp = restTemplate.exchange(
                "/beneficiarios?size=100", HttpMethod.GET, authenticated(tokenSp), Map.class);
        assertThat(listagemSp.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conteudoSp = (List<Map<String, Object>>) listagemSp.getBody().get("content");
        assertThat(conteudoSp).noneMatch(b -> beneficiarioId.equals(b.get("id")));
    }

    private String login(String usuario, String senha) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/auth/login", Map.of("login", usuario, "senha", senha), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private HttpEntity<Object> authenticated(String token) {
        return authenticated(token, null);
    }

    private HttpEntity<Object> authenticated(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
