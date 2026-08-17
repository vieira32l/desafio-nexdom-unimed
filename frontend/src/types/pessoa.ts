export interface Pessoa {
  id: string
  nome: string
  cpf: string
  dataNascimento: string | null
  email: string | null
  criadoEm: string
  atualizadoEm: string
}

export interface PessoaPayload {
  nome: string
  cpf: string
  dataNascimento: string | null
  email: string | null
}

export interface PageInfo {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

export interface PagedResponse<T> {
  content: T[]
  page: PageInfo
}
