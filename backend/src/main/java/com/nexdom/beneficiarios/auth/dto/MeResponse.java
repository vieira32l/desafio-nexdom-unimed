package com.nexdom.beneficiarios.auth.dto;

import java.util.UUID;

public record MeResponse(String login, UUID tenantId, String tenantNome, UUID tenantIdFromTenantContext) {
}
