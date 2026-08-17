import axios from 'axios'

interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: string[]
}

// Extrai uma mensagem legível do ProblemDetail (RFC 7807) devolvido pelo backend.
// Nunca deixa vazar "Request failed with status code 409" genérico do axios.
export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const problem = error.response?.data as ProblemDetail | undefined

    if (problem?.errors?.length) {
      return problem.errors.join(' ')
    }
    if (problem?.detail) {
      return problem.detail
    }
    if (!error.response) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.'
    }
    return `Ocorreu um erro inesperado (HTTP ${error.response.status}).`
  }
  return 'Ocorreu um erro inesperado.'
}

// As entradas de "errors" vêm como "campo: mensagem" (ver ApiExceptionHandler no backend).
// Aqui elas são quebradas para destacar o campo certo no formulário.
export function extractFieldErrors(error: unknown): Record<string, string> | null {
  if (!axios.isAxiosError(error)) return null

  const problem = error.response?.data as ProblemDetail | undefined
  if (!problem?.errors?.length) return null

  const campos: Record<string, string> = {}
  for (const linha of problem.errors) {
    const separador = linha.indexOf(':')
    if (separador === -1) continue
    const campo = linha.slice(0, separador).trim()
    const mensagem = linha.slice(separador + 1).trim()
    campos[campo] = mensagem
  }

  return Object.keys(campos).length > 0 ? campos : null
}
