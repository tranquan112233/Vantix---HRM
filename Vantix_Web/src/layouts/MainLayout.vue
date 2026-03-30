<template>
  <div class="app-shell">
    <AppSidebar :collapsed="collapsed" />

    <div class="app-body">
      <AppHeader @toggle-sidebar="collapsed = !collapsed" />

      <main class="app-main">
        <RouterView />
      </main>

      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref }        from 'vue'
import { RouterView } from 'vue-router'
import AppSidebar     from '@/components/AppSidebar.vue'
import AppHeader      from '@/components/AppHeader.vue'
import AppFooter      from '@/components/AppFooter.vue'

const collapsed = ref(false)
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg);
}

.app-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0; /* quan trọng: ngăn overflow */
  overflow: hidden;
}

.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
