export interface LoginRequest {
  login: string
  senha: string
}

export interface LoginResponse {
  token: string
}

export interface MeResponse {
  login: string
  tenantId: string
  tenantNome: string
  tenantIdFromTenantContext: string
}
