<script setup lang="ts">
import { ref } from 'vue'
import { listPessoas } from '@/api/pessoas'
import type { Pessoa } from '@/types/pessoa'
import { formatCPF } from '@/utils/cpf'

defineProps<{
  invalid?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const termo = ref('')
const resultados = ref<Pessoa[]>([])
const buscando = ref(false)
const aberto = ref(false)

let debounceTimer: ReturnType<typeof setTimeout> | undefined

// O backend só filtra /pessoas por nome, então quando o termo "parece" um CPF
// (só dígitos/pontuação típica) buscamos um lote maior e filtramos o CPF no cliente.
const CPF_LIKE = /^[\d.\-\s]+$/

function onInput() {
  emit('update:modelValue', null)
  aberto.value = true

  if (debounceTimer) clearTimeout(debounceTimer)
  const valor = termo.value.trim()
  if (!valor) {
    resultados.value = []
    return
  }
  debounceTimer = setTimeout(() => buscar(valor), 300)
}

async function buscar(valor: string) {
  buscando.value = true
  try {
    if (CPF_LIKE.test(valor)) {
      const digitos = valor.replace(/\D/g, '')
      const { data } = await listPessoas({ size: 50 })
      resultados.value = data.content.filter((pessoa) => pessoa.cpf.includes(digitos))
    } else {
      const { data } = await listPessoas({ nome: valor, size: 10 })
      resultados.value = data.content
    }
  } catch {
    resultados.value = []
  } finally {
    buscando.value = false
  }
}

function selecionar(pessoa: Pessoa) {
  termo.value = `${pessoa.nome} — ${formatCPF(pessoa.cpf)}`
  emit('update:modelValue', pessoa.id)
  aberto.value = false
}
</script>

<template>
  <div class="autocomplete">
    <input
      v-model="termo"
      type="text"
      placeholder="Buscar por nome ou CPF..."
      :class="{ invalido: invalid }"
      autocomplete="off"
      @input="onInput"
      @focus="aberto = true"
      @blur="aberto = false"
    />

    <ul v-if="aberto && termo.trim()" class="autocomplete-lista">
      <li v-if="buscando" class="autocomplete-item autocomplete-item--info">Buscando...</li>
      <template v-else-if="resultados.length > 0">
        <li
          v-for="pessoa in resultados"
          :key="pessoa.id"
          class="autocomplete-item"
          @mousedown.prevent="selecionar(pessoa)"
        >
          {{ pessoa.nome }} — {{ formatCPF(pessoa.cpf) }}
        </li>
      </template>
      <li v-else class="autocomplete-item autocomplete-item--info">Nenhuma pessoa encontrada.</li>
    </ul>
  </div>
</template>

<style scoped>
.autocomplete {
  position: relative;
}

.autocomplete-lista {
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  margin: 0;
  padding: 0.25rem 0;
  list-style: none;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.autocomplete-item {
  padding: 0.5rem 0.75rem;
  font-size: 0.9rem;
  cursor: pointer;
}

.autocomplete-item:hover {
  background: #f4f5f7;
}

.autocomplete-item--info {
  color: #6b6b6b;
  cursor: default;
}

.autocomplete-item--info:hover {
  background: transparent;
}
</style>
