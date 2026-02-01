<script setup>

import AdminSidebar from "../components/AdminSidebar.vue";
import AppNotify from "../components/AppNotify.vue";
import AppFooter from "../components/AppFooter.vue";
</script>

<template>
  <div class="d-flex">
    <AdminSidebar />
    <AppNotify />
    <div class="bg-light d-flex flex-column flex-fill">
      <main class="flex-fill p-4 bg-light">
        <router-view />
      </main>
      <AppFooter />
    </div>
  </div>

</template>