<template>
  <div class="d-flex flex-column min-vh-100">
    <AdminHeader />

    <div class="d-flex flex-grow-1">
      <!-- SIDEBAR -->
      <AdminSidebar />

      <!-- CONTENT -->
      <div class="flex-fill d-flex flex-column">
        <main class="flex-fill container py-4">
          <router-view />
        </main>

        <!-- FOOTER (chỉ nằm dưới content) -->
        <AdminFooter />
      </div>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from "@/components/AdminHeader.vue"
import AdminSidebar from "@/components/AdminSidebar.vue"
import AdminFooter from "@/components/AdminFooter.vue";
</script>
