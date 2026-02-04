<template>
  <div class="layout d-flex flex-column min-vh-100">

    <!-- HEADER -->
    <UserHeader />

    <!-- MAIN CONTENT -->
    <main class="container flex-fill py-4">
      <router-view />
    </main>

    <!-- FOOTER -->
    <UserFooter />

  </div>
</template>

<script setup>
import UserHeader from '@/components/UserHeader.vue'
import UserFooter from '@/components/UserFooter.vue'
</script>
