package com.nexdom.beneficiarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// Fixa a precedência do advisor de @Transactional como a mais alta possível (envolve
// todo o resto por fora). Sem isso, a ordem entre o @Transactional e um @Aspect
// customizado (como o BeneficiarioTenantAspect) não tem garantia nenhuma — os dois
// caem no mesmo LOWEST_PRECEDENCE por padrão. Aqui isso é deliberado: precisamos que
// o aspecto que fixa app.current_tenant rode DEPOIS que a transação (e a conexão JDBC)
// já esteja aberta, nunca antes.
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class TransactionManagementConfig {
}
