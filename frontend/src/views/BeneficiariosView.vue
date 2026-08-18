<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createBeneficiario, deleteBeneficiario, listBeneficiarios, updateBeneficiario } from '@/api/beneficiarios'
import BeneficiarioForm from '@/components/BeneficiarioForm.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useSuccessMessage } from '@/composables/useSuccessMessage'
import type {
  Beneficiario,
  BeneficiarioCreatePayload,
  BeneficiarioUpdatePayload,
  StatusBeneficiario,
} from '@/types/beneficiario'
import { formatDate } from '@/utils/date'
import { extractErrorMessage, extractFieldErrors } from '@/utils/problemDetail'

const { successMessage, mostrarSucesso } = useSuccessMessage()

const beneficiarios = ref<Beneficiario[]>([])
const loading = ref(false)
const listError = ref<string | null>(null)

const nomeFiltro = ref('')
const nomeBusca = ref('')
const statusFiltro = ref<StatusBeneficiario | ''>('')

const page = ref(0)
const pageSize = 10
const totalPages = ref(0)
const totalElements = ref(0)

const showForm = ref(false)
const editingBeneficiario = ref<Beneficiario | null>(null)
const formSubmitting = ref(false)
const formError = ref<string | null>(null)
const formFieldErrors = ref<Record<string, string> | null>(null)

const beneficiarioParaExcluir = ref<Beneficiario | null>(null)
const deleteBusy = ref(false)
const deleteError = ref<string | null>(null)

async function carregarBeneficiarios() {
  loading.value = true
  listError.value = null
  try {
    const { data } = await listBeneficiarios({
      nomePessoa: nomeBusca.value || undefined,
      status: statusFiltro.value || undefined,
      page: page.value,
      size: pageSize,
    })
    beneficiarios.value = data.content
    totalPages.value = data.page.totalPages
    totalElements.value = data.page.totalElements
  } catch (error) {
    listError.value = extractErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function buscar() {
  nomeBusca.value = nomeFiltro.value.trim()
  page.value = 0
  carregarBeneficiarios()
}

function aoMudarStatus() {
  page.value = 0
  carregarBeneficiarios()
}

function limparFiltros() {
  nomeFiltro.value = ''
  nomeBusca.value = ''
  statusFiltro.value = ''
  page.value = 0
  carregarBeneficiarios()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPages.value) return
  page.value = novaPagina
  carregarBeneficiarios()
}

function abrirCriacao() {
  editingBeneficiario.value = null
  formError.value = null
  formFieldErrors.value = null
  showForm.value = true
}

function abrirEdicao(beneficiario: Beneficiario) {
  editingBeneficiario.value = beneficiario
  formError.value = null
  formFieldErrors.value = null
  showForm.value = true
}

function fecharForm() {
  showForm.value = false
  editingBeneficiario.value = null
  formError.value = null
  formFieldErrors.value = null
}

async function salvarBeneficiario(payload: BeneficiarioCreatePayload | BeneficiarioUpdatePayload) {
  formSubmitting.value = true
  formError.value = null
  formFieldErrors.value = null
  const editando = editingBeneficiario.value !== null
  try {
    if (editingBeneficiario.value) {
      await updateBeneficiario(editingBeneficiario.value.id, payload as BeneficiarioUpdatePayload)
    } else {
      await createBeneficiario(payload as BeneficiarioCreatePayload)
    }
    fecharForm()
    await carregarBeneficiarios()
    mostrarSucesso(editando ? 'Beneficiário atualizado com sucesso.' : 'Beneficiário cadastrado com sucesso.')
  } catch (error) {
    formError.value = extractErrorMessage(error)
    formFieldErrors.value = extractFieldErrors(error)
  } finally {
    formSubmitting.value = false
  }
}

function pedirConfirmacaoExclusao(beneficiario: Beneficiario) {
  beneficiarioParaExcluir.value = beneficiario
  deleteError.value = null
}

function cancelarExclusao() {
  beneficiarioParaExcluir.value = null
  deleteError.value = null
}

async function confirmarExclusao() {
  if (!beneficiarioParaExcluir.value) return
  deleteBusy.value = true
  deleteError.value = null
  try {
    await deleteBeneficiario(beneficiarioParaExcluir.value.id)
    beneficiarioParaExcluir.value = null
    if (beneficiarios.value.length === 1 && page.value > 0) {
      page.value -= 1
    }
    await carregarBeneficiarios()
    mostrarSucesso('Beneficiário excluído com sucesso.')
  } catch (error) {
    deleteError.value = extractErrorMessage(error)
  } finally {
    deleteBusy.value = false
  }
}

function rotuloTipo(tipo: string): string {
  return tipo === 'TITULAR' ? 'Titular' : 'Dependente'
}

function rotuloStatus(status: string): string {
  return status === 'ATIVO' ? 'Ativo' : 'Inativo'
}

onMounted(carregarBeneficiarios)
</script>

<template>
  <section>
    <div class="cabecalho">
      <h1>Beneficiários</h1>
      <button type="button" @click="abrirCriacao">Novo beneficiário</button>
    </div>

    <form class="busca" @submit.prevent="buscar">
      <input v-model="nomeFiltro" type="text" placeholder="Buscar por nome da pessoa..." />
      <select v-model="statusFiltro" @change="aoMudarStatus">
        <option value="">Todos os status</option>
        <option value="ATIVO">Ativo</option>
        <option value="INATIVO">Inativo</option>
      </select>
      <button type="submit">Buscar</button>
      <button v-if="nomeBusca || statusFiltro" type="button" class="secundario" @click="limparFiltros">
        Limpar
      </button>
    </form>

    <p v-if="successMessage" class="banner-sucesso" role="status">{{ successMessage }}</p>
    <p v-if="listError" class="banner-erro" role="alert">{{ listError }}</p>

    <p v-if="loading">Carregando...</p>

    <template v-else>
      <p v-if="beneficiarios.length === 0">
        {{
          nomeBusca || statusFiltro
            ? 'Nenhum beneficiário encontrado para esse filtro.'
            : 'Nenhum beneficiário cadastrado.'
        }}
      </p>

      <div v-else class="tabela-wrapper">
        <table class="tabela">
          <thead>
            <tr>
              <th>Pessoa</th>
              <th>Matrícula</th>
              <th>Tipo</th>
              <th>Status</th>
              <th>Data de adesão</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="beneficiario in beneficiarios" :key="beneficiario.id">
              <td>{{ beneficiario.pessoaNome }}</td>
              <td>{{ beneficiario.matricula }}</td>
              <td>{{ rotuloTipo(beneficiario.tipo) }}</td>
              <td>
                <span
                  class="badge"
                  :class="beneficiario.status === 'ATIVO' ? 'badge--ativo' : 'badge--inativo'"
                >
                  {{ rotuloStatus(beneficiario.status) }}
                </span>
              </td>
              <td>{{ formatDate(beneficiario.dataAdesao) }}</td>
              <td>
                <div class="acoes-linha">
                  <button type="button" class="secundario" @click="abrirEdicao(beneficiario)">Editar</button>
                  <button type="button" class="perigo" @click="pedirConfirmacaoExclusao(beneficiario)">
                    Excluir
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalElements > 0" class="paginacao">
        <button type="button" class="secundario" :disabled="page === 0" @click="irParaPagina(page - 1)">
          Anterior
        </button>
        <span>Página {{ page + 1 }} de {{ totalPages }} ({{ totalElements }} beneficiários)</span>
        <button
          type="button"
          class="secundario"
          :disabled="page + 1 >= totalPages"
          @click="irParaPagina(page + 1)"
        >
          Próxima
        </button>
      </div>
    </template>

    <BeneficiarioForm
      v-if="showForm"
      :beneficiario="editingBeneficiario"
      :submitting="formSubmitting"
      :error-message="formError"
      :field-errors="formFieldErrors"
      @submit="salvarBeneficiario"
      @cancel="fecharForm"
    />

    <ConfirmDialog
      :open="beneficiarioParaExcluir !== null"
      title="Excluir beneficiário"
      :message="`Tem certeza que deseja excluir o vínculo de ${beneficiarioParaExcluir?.pessoaNome} (matrícula ${beneficiarioParaExcluir?.matricula})? Essa ação não pode ser desfeita.`"
      :busy="deleteBusy"
      :error-message="deleteError"
      @confirm="confirmarExclusao"
      @cancel="cancelarExclusao"
    />
  </section>
</template>

<style scoped>
.cabecalho {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.cabecalho h1 {
  margin: 0;
}

.busca {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.busca input {
  flex: 1;
  max-width: 320px;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.busca select {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  background: #fff;
}
</style>
