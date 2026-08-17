<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Pessoa, PessoaPayload } from '@/types/pessoa'
import { isValidCPF } from '@/utils/cpf'

const props = defineProps<{
  pessoa: Pessoa | null
  submitting: boolean
  errorMessage: string | null
  fieldErrors: Record<string, string> | null
}>()

const emit = defineEmits<{
  submit: [payload: PessoaPayload]
  cancel: []
}>()

const form = reactive({
  nome: props.pessoa?.nome ?? '',
  cpf: props.pessoa?.cpf ?? '',
  dataNascimento: props.pessoa?.dataNascimento ?? '',
  email: props.pessoa?.email ?? '',
})

const localErrors = ref<Record<string, string>>({})

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validar(): boolean {
  const erros: Record<string, string> = {}

  if (!form.nome.trim()) {
    erros.nome = 'Nome é obrigatório.'
  }

  const cpfDigits = form.cpf.replace(/\D/g, '')
  if (!cpfDigits) {
    erros.cpf = 'CPF é obrigatório.'
  } else if (!isValidCPF(cpfDigits)) {
    erros.cpf = 'CPF inválido.'
  }

  if (form.dataNascimento) {
    const hoje = new Date().toISOString().slice(0, 10)
    if (form.dataNascimento > hoje) {
      erros.dataNascimento = 'Data de nascimento não pode ser no futuro.'
    }
  }

  if (form.email.trim() && !EMAIL_PATTERN.test(form.email.trim())) {
    erros.email = 'E-mail inválido.'
  }

  localErrors.value = erros
  return Object.keys(erros).length === 0
}

function erroDoCampo(campo: string): string | null {
  return localErrors.value[campo] ?? props.fieldErrors?.[campo] ?? null
}

function handleSubmit() {
  if (!validar()) return

  const payload: PessoaPayload = {
    nome: form.nome.trim(),
    cpf: form.cpf.replace(/\D/g, ''),
    dataNascimento: form.dataNascimento || null,
    email: form.email.trim() || null,
  }
  emit('submit', payload)
}
</script>

<template>
  <div class="modal-backdrop" @click.self="emit('cancel')">
    <div class="modal-card">
      <h2>{{ pessoa ? 'Editar pessoa' : 'Nova pessoa' }}</h2>

      <p v-if="errorMessage" class="banner-erro" role="alert">{{ errorMessage }}</p>

      <form novalidate @submit.prevent="handleSubmit">
        <label>
          Nome
          <input v-model="form.nome" type="text" :class="{ invalido: erroDoCampo('nome') }" />
          <span v-if="erroDoCampo('nome')" class="erro-campo">{{ erroDoCampo('nome') }}</span>
        </label>

        <label>
          CPF
          <input
            v-model="form.cpf"
            type="text"
            placeholder="000.000.000-00"
            :class="{ invalido: erroDoCampo('cpf') }"
          />
          <span v-if="erroDoCampo('cpf')" class="erro-campo">{{ erroDoCampo('cpf') }}</span>
        </label>

        <label>
          Data de nascimento
          <input
            v-model="form.dataNascimento"
            type="date"
            :class="{ invalido: erroDoCampo('dataNascimento') }"
          />
          <span v-if="erroDoCampo('dataNascimento')" class="erro-campo">{{ erroDoCampo('dataNascimento') }}</span>
        </label>

        <label>
          E-mail
          <input v-model="form.email" type="email" :class="{ invalido: erroDoCampo('email') }" />
          <span v-if="erroDoCampo('email')" class="erro-campo">{{ erroDoCampo('email') }}</span>
        </label>

        <div class="acoes">
          <button type="button" class="secundario" :disabled="submitting" @click="emit('cancel')">
            Cancelar
          </button>
          <button type="submit" :disabled="submitting">
            {{ submitting ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
