<script setup lang="ts">
defineProps<{
  open: boolean
  title: string
  message: string
  busy?: boolean
  errorMessage?: string | null
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <div v-if="open" class="modal-backdrop" @click.self="emit('cancel')">
    <div class="modal-card modal-card--small">
      <h2>{{ title }}</h2>
      <p>{{ message }}</p>

      <p v-if="errorMessage" class="banner-erro" role="alert">{{ errorMessage }}</p>

      <div class="acoes">
        <button type="button" class="secundario" :disabled="busy" @click="emit('cancel')">Cancelar</button>
        <button type="button" class="perigo" :disabled="busy" @click="emit('confirm')">
          {{ busy ? 'Excluindo...' : 'Excluir' }}
        </button>
      </div>
    </div>
  </div>
</template>
