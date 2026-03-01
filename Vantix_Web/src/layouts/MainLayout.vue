<template>
  <!-- ================= ROOT LAYOUT ================= -->
  <div class="layout">

    <!-- ================= SIDEBAR ================= -->
    <!-- Nhận prop collapsed để thu gọn sidebar -->
    <Sidebar :collapsed="collapsed" />

    <!-- ================= RIGHT SIDE ================= -->
    <div class="main">

      <!-- ================= HEADER ================= -->
      <!-- Toggle sidebar từ header -->
      <AppHeader @toggle-sidebar="toggleSidebar" />

      <!-- ================= PAGE CONTENT ================= -->
      <main class="content">
      <router-view />
    </main>

    </div>
  </div>
</template>

<script setup>
/* ================= IMPORT ================= */
import { ref } from "vue"
import Sidebar from "@/components/Sidebar.vue"
import AppHeader from "@/components/AppHeader.vue"

/* ================= SIDEBAR STATE ================= */
/* Điều khiển trạng thái collapse */
const collapsed = ref(false)

/* Toggle function gọn hơn */
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}
</script>

<style scoped>

/* ================= LAYOUT CONTAINER ================= */
/* Chia layout thành sidebar + main */
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ================= RIGHT SIDE ================= */
/* Chiếm toàn bộ phần còn lại */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;      /* Tránh overflow khi sidebar collapse */
  overflow: hidden;
}

/* ================= HEADER ================= */
/* Cố định chiều cao header */
.main :deep(nav) {
  height: 60px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

/* ================= CONTENT AREA ================= */
/* Khu vực hiển thị router-view */
.content {
  flex: 1;
  overflow-y: auto;
  background: #f8fafc;
  padding: 20px;
}

</style>