<script setup lang="ts">
import { reactive, ref } from 'vue'
import type {
  Beneficiario,
  BeneficiarioCreatePayload,
  BeneficiarioUpdatePayload,
  StatusBeneficiario,
  TipoBeneficiario,
} from '@/types/beneficiario'
import PessoaAutocomplete from './PessoaAutocomplete.vue'

const props = defineProps<{
  beneficiario: Beneficiario | null
  submitting: boolean
  errorMessage: string | null
  fieldErrors: Record<string, string> | null
}>()

const emit = defineEmits<{
  submit: [payload: BeneficiarioCreatePayload | BeneficiarioUpdatePayload]
  cancel: []
}>()

const form = reactive({
  pessoaId: (props.beneficiario?.pessoaId ?? null) as string | null,
  matricula: props.beneficiario?.matricula ?? '',
  tipo: (props.beneficiario?.tipo ?? 'TITULAR') as TipoBeneficiario,
  status: (props.beneficiario?.status ?? 'ATIVO') as StatusBeneficiario,
  dataAdesao: props.beneficiario?.dataAdesao ?? '',
})

const localErrors = ref<Record<string, string>>({})

function validar(): boolean {
  const erros: Record<string, string> = {}

  if (!props.beneficiario && !form.pessoaId) {
    erros.pessoaId = 'Selecione uma pessoa.'
  }
  if (!form.matricula.trim()) {
    erros.matricula = 'Matrícula é obrigatória.'
  }

  localErrors.value = erros
  return Object.keys(erros).length === 0
}

function erroDoCampo(campo: string): string | null {
  return localErrors.value[campo] ?? props.fieldErrors?.[campo] ?? null
}

function handleSubmit() {
  if (!validar()) return

  if (props.beneficiario) {
    const payload: BeneficiarioUpdatePayload = {
      matricula: form.matricula.trim(),
      tipo: form.tipo,
      status: form.status,
      dataAdesao: form.dataAdesao || null,
    }
    emit('submit', payload)
  } else {
    const payload: BeneficiarioCreatePayload = {
      pessoaId: form.pessoaId as string,
      matricula: form.matricula.trim(),
      tipo: form.tipo,
      status: form.status,
      dataAdesao: form.dataAdesao || null,
    }
    emit('submit', payload)
  }
}
</script>

<template>
  <div class="modal-backdrop" @click.self="emit('cancel')">
    <div class="modal-card">
      <h2>{{ beneficiario ? 'Editar beneficiário' : 'Novo beneficiário' }}</h2>

      <p v-if="errorMessage" class="banner-erro" role="alert">{{ errorMessage }}</p>

      <form novalidate @submit.prevent="handleSubmit">
        <label v-if="!beneficiario">
          Pessoa
          <PessoaAutocomplete
            :invalid="!!erroDoCampo('pessoaId')"
            @update:model-value="(id) => (form.pessoaId = id)"
          />
          <span v-if="erroDoCampo('pessoaId')" class="erro-campo">{{ erroDoCampo('pessoaId') }}</span>
        </label>
        <label v-else>
          Pessoa
          <input type="text" :value="beneficiario.pessoaNome" disabled />
          <span class="ajuda-campo">O vínculo com a pessoa não pode ser alterado após a criação.</span>
        </label>

        <label>
          Matrícula
          <input v-model="form.matricula" type="text" :class="{ invalido: erroDoCampo('matricula') }" />
          <span v-if="erroDoCampo('matricula')" class="erro-campo">{{ erroDoCampo('matricula') }}</span>
        </label>

        <label>
          Tipo
          <select v-model="form.tipo">
            <option value="TITULAR">Titular</option>
            <option value="DEPENDENTE">Dependente</option>
          </select>
        </label>

        <label>
          Status
          <select v-model="form.status">
            <option value="ATIVO">Ativo</option>
            <option value="INATIVO">Inativo</option>
          </select>
        </label>

        <label>
          Data de adesão
          <input v-model="form.dataAdesao" type="date" />
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
