import apiClient from './client'
import type { PagedResponse } from '@/types/pagination'
import type {
  Beneficiario,
  BeneficiarioCreatePayload,
  BeneficiarioUpdatePayload,
  StatusBeneficiario,
} from '@/types/beneficiario'

export interface ListBeneficiariosParams {
  nomePessoa?: string
  status?: StatusBeneficiario
  page?: number
  size?: number
}

export function listBeneficiarios(params: ListBeneficiariosParams) {
  return apiClient.get<PagedResponse<Beneficiario>>('/beneficiarios', { params })
}

export function getBeneficiario(id: string) {
  return apiClient.get<Beneficiario>(`/beneficiarios/${id}`)
}

export function createBeneficiario(payload: BeneficiarioCreatePayload) {
  return apiClient.post<Beneficiario>('/beneficiarios', payload)
}

export function updateBeneficiario(id: string, payload: BeneficiarioUpdatePayload) {
  return apiClient.put<Beneficiario>(`/beneficiarios/${id}`, payload)
}

export function deleteBeneficiario(id: string) {
  return apiClient.delete<void>(`/beneficiarios/${id}`)
}
