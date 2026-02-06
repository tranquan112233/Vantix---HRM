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
  position: sticky;
  top: 56px;
  height: calc(100vh - 56px);
  overflow-y: auto;
  flex: 0 0 240px;

  background: linear-gradient(180deg, #020617, #0F172A, #020617);
  border-right: 1px solid #1E293B;
}

/* ===== LINK ===== */
.nav-link {
  color: #CBD5F5;
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 4px;
  transition: all 0.2s ease;
  font-size: 14px;
}

.nav-link i {
  font-size: 16px;
  color: #94A3B8;
}

/* Hover */
.nav-link:hover {
  background-color: #1E293B;
  color: #60A5FA;
}

.nav-link:hover i {
  color: #60A5FA;
}

/* Active */
.router-link-active {
  background: linear-gradient(90deg, #2563EB, #1D4ED8);
  color: #FFFFFF !important;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.55);
}

.router-link-active i {
  color: #FFFFFF;
}

/* ===== SECTION TITLE ===== */
.sidebar-title {
  font-size: 11px;
  font-weight: 600;
  color: #64748B;
  padding: 14px 12px 6px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

/* ===== DIVIDER ===== */
.sidebar-divider {
  height: 1px;
  background-color: #1E293B;
  margin: 12px 0;
}
</style>

