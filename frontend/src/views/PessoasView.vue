<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createPessoa, deletePessoa, listPessoas, updatePessoa } from '@/api/pessoas'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import PessoaForm from '@/components/PessoaForm.vue'
import type { Pessoa, PessoaPayload } from '@/types/pessoa'
import { extractErrorMessage, extractFieldErrors } from '@/utils/problemDetail'

const pessoas = ref<Pessoa[]>([])
const loading = ref(false)
const listError = ref<string | null>(null)

const nomeFiltro = ref('')
const nomeBusca = ref('')

const page = ref(0)
const pageSize = 10
const totalPages = ref(0)
const totalElements = ref(0)

const showForm = ref(false)
const editingPessoa = ref<Pessoa | null>(null)
const formSubmitting = ref(false)
const formError = ref<string | null>(null)
const formFieldErrors = ref<Record<string, string> | null>(null)

const pessoaParaExcluir = ref<Pessoa | null>(null)
const deleteBusy = ref(false)
const deleteError = ref<string | null>(null)

async function carregarPessoas() {
  loading.value = true
  listError.value = null
  try {
    const { data } = await listPessoas({
      nome: nomeBusca.value || undefined,
      page: page.value,
      size: pageSize,
    })
    pessoas.value = data.content
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
  carregarPessoas()
}

function limparBusca() {
  nomeFiltro.value = ''
  nomeBusca.value = ''
  page.value = 0
  carregarPessoas()
}

function irParaPagina(novaPagina: number) {
  if (novaPagina < 0 || novaPagina >= totalPages.value) return
  page.value = novaPagina
  carregarPessoas()
}

function abrirCriacao() {
  editingPessoa.value = null
  formError.value = null
  formFieldErrors.value = null
  showForm.value = true
}

function abrirEdicao(pessoa: Pessoa) {
  editingPessoa.value = pessoa
  formError.value = null
  formFieldErrors.value = null
  showForm.value = true
}

function fecharForm() {
  showForm.value = false
  editingPessoa.value = null
  formError.value = null
  formFieldErrors.value = null
}

async function salvarPessoa(payload: PessoaPayload) {
  formSubmitting.value = true
  formError.value = null
  formFieldErrors.value = null
  try {
    if (editingPessoa.value) {
      await updatePessoa(editingPessoa.value.id, payload)
    } else {
      await createPessoa(payload)
    }
    fecharForm()
    await carregarPessoas()
  } catch (error) {
    formError.value = extractErrorMessage(error)
    formFieldErrors.value = extractFieldErrors(error)
  } finally {
    formSubmitting.value = false
  }
}

function pedirConfirmacaoExclusao(pessoa: Pessoa) {
  pessoaParaExcluir.value = pessoa
  deleteError.value = null
}

function cancelarExclusao() {
  pessoaParaExcluir.value = null
  deleteError.value = null
}

async function confirmarExclusao() {
  if (!pessoaParaExcluir.value) return
  deleteBusy.value = true
  deleteError.value = null
  try {
    await deletePessoa(pessoaParaExcluir.value.id)
    pessoaParaExcluir.value = null
    if (pessoas.value.length === 1 && page.value > 0) {
      page.value -= 1
    }
    await carregarPessoas()
  } catch (error) {
    deleteError.value = extractErrorMessage(error)
  } finally {
    deleteBusy.value = false
  }
}

function formatarData(data: string | null): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function formatarCpf(cpf: string): string {
  if (cpf.length !== 11) return cpf
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`
}

onMounted(carregarPessoas)
</script>

<template>
  <section>
    <div class="cabecalho">
      <h1>Pessoas</h1>
      <button type="button" @click="abrirCriacao">Nova pessoa</button>
    </div>

    <form class="busca" @submit.prevent="buscar">
      <input v-model="nomeFiltro" type="text" placeholder="Buscar por nome..." />
      <button type="submit">Buscar</button>
      <button v-if="nomeBusca" type="button" class="secundario" @click="limparBusca">Limpar</button>
    </form>

    <p v-if="listError" class="banner-erro" role="alert">{{ listError }}</p>

    <p v-if="loading">Carregando...</p>

    <template v-else>
      <p v-if="pessoas.length === 0">
        {{ nomeBusca ? `Nenhuma pessoa encontrada para "${nomeBusca}".` : 'Nenhuma pessoa cadastrada.' }}
      </p>

      <div v-else class="tabela-wrapper">
        <table class="tabela">
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF</th>
              <th>Data de nascimento</th>
              <th>E-mail</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="pessoa in pessoas" :key="pessoa.id">
              <td>{{ pessoa.nome }}</td>
              <td>{{ formatarCpf(pessoa.cpf) }}</td>
              <td>{{ formatarData(pessoa.dataNascimento) }}</td>
              <td>{{ pessoa.email ?? '—' }}</td>
              <td>
                <div class="acoes-linha">
                  <button type="button" class="secundario" @click="abrirEdicao(pessoa)">Editar</button>
                  <button type="button" class="perigo" @click="pedirConfirmacaoExclusao(pessoa)">Excluir</button>
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
        <span>Página {{ page + 1 }} de {{ totalPages }} ({{ totalElements }} pessoas)</span>
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

    <PessoaForm
      v-if="showForm"
      :pessoa="editingPessoa"
      :submitting="formSubmitting"
      :error-message="formError"
      :field-errors="formFieldErrors"
      @submit="salvarPessoa"
      @cancel="fecharForm"
    />

    <ConfirmDialog
      :open="pessoaParaExcluir !== null"
      title="Excluir pessoa"
      :message="`Tem certeza que deseja excluir ${pessoaParaExcluir?.nome}? Essa ação não pode ser desfeita.`"
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
</style>
