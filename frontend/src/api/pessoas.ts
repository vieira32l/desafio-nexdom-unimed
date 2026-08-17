import apiClient from './client'
import type { PagedResponse, Pessoa, PessoaPayload } from '@/types/pessoa'

export interface ListPessoasParams {
  nome?: string
  page?: number
  size?: number
}

export function listPessoas(params: ListPessoasParams) {
  return apiClient.get<PagedResponse<Pessoa>>('/pessoas', { params })
}

export function getPessoa(id: string) {
  return apiClient.get<Pessoa>(`/pessoas/${id}`)
}

export function createPessoa(payload: PessoaPayload) {
  return apiClient.post<Pessoa>('/pessoas', payload)
}

export function updatePessoa(id: string, payload: PessoaPayload) {
  return apiClient.put<Pessoa>(`/pessoas/${id}`, payload)
}

export function deletePessoa(id: string) {
  return apiClient.delete<void>(`/pessoas/${id}`)
}
