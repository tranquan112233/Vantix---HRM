<script setup>
import AppHeader from "../components/AppHeader.vue";
import AppFooter from "../components/AppFooter.vue";
</script>

<template>
  <div class="d-flex flex-column min-vh-100">
    <AppHeader />

    <main class="container py-4 flex-fill">
      <router-view />
    </main>

    <AppFooter />
  </div>
</template>