<script setup>
const menus = [
  {
    label: 'Dashboard',
    icon: 'bi bi-speedometer2',
    to: '/admin'
  },
  {
    label: 'Quản lý nhân viên',
    icon: 'bi bi-people',
    to: '/admin/users'
  },
  {
    label: 'Phòng ban',
    icon: 'bi bi-diagram-3',
    to: '/admin/departments'
  },
  {
    label: 'Cài đặt hệ thống',
    icon: 'bi bi-gear',
    to: '/admin/settings'
  }
]
</script>

<template>
  <aside class="admin-sidebar bg-dark text-white">
    <!-- Logo -->
    <div class="sidebar-header text-center py-3">
      <router-link
          to="/home"
          class="text-decoration-none text-white fs-5 fw-semibold"
      >
        VANTIX
        <span class="fw-light">Co.</span>
      </router-link>
    </div>
    <!-- Menu -->
    <ul class="nav flex-column mt-3">
      <li class="nav-item" v-for="item in menus" :key="item.to">
        <router-link
            :to="item.to"
            class="nav-link text-white d-flex align-items-center"
        >
          <i :class="item.icon"></i>
          <span class="ms-2">{{ item.label }}</span>
        </router-link>
      </li>
    </ul>
  </aside>
</template>

<style scoped>
.admin-sidebar {
  width: 260px;
  min-height: 100vh;
}

.sidebar-header {
  height: 60px;
  padding: 0 1rem;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
}

.nav-link {
  padding: 12px 20px;
  border-radius: 6px;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.15);
}
</style>
