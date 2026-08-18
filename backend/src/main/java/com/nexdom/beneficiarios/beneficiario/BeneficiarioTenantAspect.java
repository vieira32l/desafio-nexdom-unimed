package com.nexdom.beneficiarios.beneficiario;

import com.nexdom.beneficiarios.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

// Antes de qualquer método do BeneficiarioService: (1) veste o role app_runtime na
// sessão Postgres da transação atual — necessário porque o usuário de conexão da
// aplicação é o bootstrap superuser do Postgres, e RLS nunca se aplica a superusers,
// nem com FORCE; (2) define app.current_tenant, que é o que a policy de RLS de V6
// usa para filtrar beneficiario. Os dois juntos formam a segunda camada de isolamento,
// independente do filtro explícito do BeneficiarioRepository. Ver README.
//
// SET LOCAL ROLE e SELECT set_config(..., true) revertem sozinhos no fim da transação
// (commit ou rollback) — a próxima transação, na mesma conexão pooled, começa limpa.
//
// set_config em vez de "SET LOCAL app.current_tenant = '<uuid>'": set_config é uma
// função normal e aceita bind parameter via PreparedStatement — SET não aceita
// parâmetro no valor, então concatenar string ali seria abrir espaço pra injeção
// (mesmo o tenantId vindo de um UUID confiável, não há motivo pra arriscar). Já
// "SET LOCAL ROLE app_runtime" é um identificador fixo definido por nós, não input
// de usuário, então não tem o mesmo risco.
//
// Precisa rodar DEPOIS que a transação (e a conexão JDBC) já foi aberta pelo advisor de
// @Transactional — daí o TransactionManagementConfig ordenando isso explicitamente.
@Aspect
@Component
public class BeneficiarioTenantAspect {

    private static final String SET_ROLE_SQL = "SET LOCAL ROLE app_runtime";
    private static final String SET_TENANT_SQL = "SELECT set_config('app.current_tenant', ?, true)";

    private final EntityManager entityManager;

    public BeneficiarioTenantAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.nexdom.beneficiarios.beneficiario.BeneficiarioService.*(..))")
    public void definirContextoDeTenant() {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement setRole = connection.createStatement()) {
                setRole.execute(SET_ROLE_SQL);
            }
            try (PreparedStatement setTenant = connection.prepareStatement(SET_TENANT_SQL)) {
                setTenant.setString(1, tenantId.toString());
                setTenant.execute();
            }
        });
    }
}
