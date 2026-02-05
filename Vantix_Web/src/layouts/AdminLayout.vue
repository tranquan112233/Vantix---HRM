<template>
  <div class="d-flex flex-column vh-100">
    <AdminHeader />

    <div class="d-flex flex-grow-1">
      <!-- SIDEBAR -->
      <AdminSidebar />

      <!-- CONTENT -->
      <div class="flex-fill d-flex flex-column" style="min-height: 0; min-width: 0;">
        <main class="flex-grow-1 container overflow-auto">
          <router-view />
        </main>

        <AdminFooter />
      </div>
    </div>
  </div>
</template>

<script setup>
import AdminHeader from "@/components/AdminHeader.vue"
import AdminSidebar from "@/components/AdminSidebar.vue"
import AdminFooter from "@/components/AdminFooter.vue"
</script>
