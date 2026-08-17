import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

// Estado guardado só em memória (nenhum localStorage/sessionStorage/cookie):
// um refresh de página derruba a sessão de propósito.
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const login = ref<string | null>(null)
  const tenantId = ref<string | null>(null)
  const tenantNome = ref<string | null>(null)

  const isAuthenticated = computed(() => token.value !== null)

  function setToken(newToken: string) {
    token.value = newToken
  }

  function setProfile(profile: { login: string; tenantId: string; tenantNome: string }) {
    login.value = profile.login
    tenantId.value = profile.tenantId
    tenantNome.value = profile.tenantNome
  }

  function logout() {
    token.value = null
    login.value = null
    tenantId.value = null
    tenantNome.value = null
  }

  return { token, login, tenantId, tenantNome, isAuthenticated, setToken, setProfile, logout }
})
