<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<template>
  <AppHeader />
  <main class="app-main">
    <router-view />
  </main>
</template>

<style scoped>
.app-main {
  max-width: 960px;
  margin: 0 auto;
  padding: 1.5rem;
}
</style>
