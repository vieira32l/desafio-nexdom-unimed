package com.nexdom.beneficiarios.security;

import java.util.UUID;

public record AuthenticatedUser(String login, UUID tenantId, String tenantNome) {
}
