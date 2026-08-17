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
