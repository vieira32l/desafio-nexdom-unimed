export type TipoBeneficiario = 'TITULAR' | 'DEPENDENTE'
export type StatusBeneficiario = 'ATIVO' | 'INATIVO'

export interface Beneficiario {
  id: string
  pessoaId: string
  pessoaNome: string
  tenantId: string
  matricula: string
  tipo: TipoBeneficiario
  status: StatusBeneficiario
  dataAdesao: string | null
  criadoEm: string
  atualizadoEm: string
}

export interface BeneficiarioCreatePayload {
  pessoaId: string
  matricula: string
  tipo: TipoBeneficiario
  status: StatusBeneficiario
  dataAdesao: string | null
}

// pessoaId não é editável: o backend não aceita esse campo no PUT
// (o vínculo com a pessoa é definido só na criação).
export interface BeneficiarioUpdatePayload {
  matricula: string
  tipo: TipoBeneficiario
  status: StatusBeneficiario
  dataAdesao: string | null
}
