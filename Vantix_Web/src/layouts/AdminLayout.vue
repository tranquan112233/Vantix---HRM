<script setup>

import AdminSidebar from "../components/AdminSidebar.vue";
</script>

<template>
  <div class="d-flex">
    <AdminSidebar />
    <main class="flex-fill p-4 bg-light">
      <router-view />
    </main>
  </div>

</template>