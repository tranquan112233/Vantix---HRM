<template>
  <aside class="admin-sidebar d-flex flex-column">
    <ul class="nav nav-pills flex-column px-2 pt-3">

      <!-- ===== QUẢN LÝ ===== -->
      <li class="sidebar-title">QUẢN LÝ</li>

      <li class="nav-item">
        <RouterLink to="/admin/dashboard" class="nav-link">
          <i class="bi bi-speedometer2 me-2"></i>
          Dashboard
        </RouterLink>
      </li>

      <li class="nav-item">
        <RouterLink to="/admin/users" class="nav-link">
          <i class="bi bi-people me-2"></i>
          Users
        </RouterLink>
      </li>

      <li class="nav-item">
        <RouterLink to="/admin/departments" class="nav-link">
          <i class="bi bi-diagram-3 me-2"></i>
          Departments
        </RouterLink>
      </li>

      <!-- ===== HỆ THỐNG ===== -->
      <li class="sidebar-divider"></li>
      <li class="sidebar-title">HỆ THỐNG</li>

      <li class="nav-item">
        <RouterLink to="/admin/settings" class="nav-link">
          <i class="bi bi-gear me-2"></i>
          Settings
        </RouterLink>
      </li>

    </ul>
  </aside>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>

<style scoped>
/* ===== SIDEBAR ===== */
.admin-sidebar {
  width: 240px;
  /* cố định như header */
  position: sticky;
  top: 56px; /* chiều cao header */
  height: calc(100vh - 56px);
  overflow-y: auto;
  flex: 0 0 240px;

  background: linear-gradient(180deg, #1f2937, #111827);
  border-right: 1px solid #1f2937;
}

/* ===== LINK ===== */
.nav-link {
  color: #d1d5db;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 4px;
  transition: all 0.2s ease;
  font-size: 14px;
}

.nav-link i {
  font-size: 16px;
}

/* Hover */
.nav-link:hover {
  background-color: #1f2937;
  color: #60a5fa;
}

/* Active */
.router-link-active {
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);
}

/* ===== SECTION TITLE ===== */
.sidebar-title {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  padding: 14px 12px 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* ===== DIVIDER ===== */
.sidebar-divider {
  height: 1px;
  background-color: #374151;
  margin: 12px 0;
}
</style>
