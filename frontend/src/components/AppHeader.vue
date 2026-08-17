<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header v-if="authStore.isAuthenticated" class="app-header">
    <span class="tenant-banner">
      Você está em: <strong>{{ authStore.tenantNome }}</strong>
    </span>
    <nav class="app-nav">
      <router-link to="/pessoas">Pessoas</router-link>
      <router-link to="/beneficiarios">Beneficiários</router-link>
    </nav>
    <span class="spacer" />
    <span class="usuario">{{ authStore.login }}</span>
    <button type="button" class="logout" @click="handleLogout">Sair</button>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.6rem 1.25rem;
  background: #1f6f43;
  color: #fff;
  font-size: 0.9rem;
}

.tenant-banner strong {
  font-weight: 600;
}

.app-nav {
  display: flex;
  gap: 1rem;
}

.app-nav a {
  color: #fff;
  opacity: 0.8;
  text-decoration: none;
}

.app-nav a:hover,
.app-nav a.router-link-active {
  opacity: 1;
  text-decoration: underline;
}

.spacer {
  flex: 1;
}

.usuario {
  opacity: 0.85;
}

.logout {
  padding: 0.35rem 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 0.85rem;
}

.logout:hover {
  background: rgba(255, 255, 255, 0.15);
}
</style>
