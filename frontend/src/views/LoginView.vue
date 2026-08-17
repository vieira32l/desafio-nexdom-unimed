<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiClient from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type { LoginResponse, MeResponse } from '@/types/auth'

const login = ref('')
const senha = ref('')
const erro = ref<string | null>(null)
const carregando = ref(false)

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

async function handleSubmit() {
  erro.value = null
  carregando.value = true

  try {
    const { data: loginResponse } = await apiClient.post<LoginResponse>('/auth/login', {
      login: login.value,
      senha: senha.value,
    })
    authStore.setToken(loginResponse.token)

    // Confirma o token junto do backend e busca o tenant ativo (nome vem do /auth/me).
    const { data: perfil } = await apiClient.get<MeResponse>('/auth/me')
    authStore.setProfile({
      login: perfil.login,
      tenantId: perfil.tenantId,
      tenantNome: perfil.tenantNome,
    })

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/pessoas'
    router.push(redirect)
  } catch {
    authStore.logout()
    erro.value = 'Login ou senha inválidos.'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <form class="login-card" @submit.prevent="handleSubmit">
      <h1>Beneficiários</h1>
      <p class="subtitulo">Entre com seu usuário do tenant</p>

      <label>
        Login
        <input v-model="login" type="text" autocomplete="username" required />
      </label>

      <label>
        Senha
        <input v-model="senha" type="password" autocomplete="current-password" required />
      </label>

      <p v-if="erro" class="erro" role="alert">{{ erro }}</p>

      <button type="submit" :disabled="carregando">
        {{ carregando ? 'Entrando...' : 'Entrar' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f5f7;
}

.login-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  max-width: 320px;
  padding: 2rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}

.login-card h1 {
  margin: 0;
  font-size: 1.4rem;
}

.subtitulo {
  margin: 0 0 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: #333;
}

input {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  margin-top: 0.5rem;
  padding: 0.6rem;
  border: none;
  border-radius: 4px;
  background: #1f6f43;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: default;
}

.erro {
  margin: 0;
  color: #b3261e;
  font-size: 0.85rem;
}
</style>
