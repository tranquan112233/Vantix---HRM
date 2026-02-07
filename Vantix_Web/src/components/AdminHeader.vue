<template>
  <nav class="navbar navbar-expand-lg navbar-dark admin-header px-3 shadow sticky-top">

    <!-- LOGO -->
    <RouterLink
        to="/"
        class="navbar-brand fw-bold text-white text-decoration-none"
    >
      <i class="bi bi-building"></i> Vantix HRM
    </RouterLink>

    <!-- HOME ICON -->
    <RouterLink
        to="/home"
        class="btn btn-link text-light ms-auto me-3 p-0 fs-5 home-btn"
        title="Trang chủ"
    >
      <i class="bi bi-house-door-fill"></i>
    </RouterLink>

    <!-- USER DROPDOWN -->
    <div class="dropdown">
      <button
          class="btn dropdown-toggle p-0 border-0 bg-transparent
               d-flex align-items-center gap-2"
          data-bs-toggle="dropdown"
          data-bs-auto-close="true"
      >
        <div class="avatar-circle">
          {{ firstLetter }}
        </div>
        <span class="text-white fw-semibold">
          {{ username }}
        </span>
      </button>

      <ul class="dropdown-menu dropdown-menu-end shadow dropdown-dark">
        <li>
          <RouterLink class="dropdown-item" to="/profile">
            <i class="bi bi-person-circle me-2"></i>
            Hồ sơ cá nhân
          </RouterLink>
        </li>

        <li>
          <RouterLink class="dropdown-item" to="/change-password">
            <i class="bi bi-key-fill me-2"></i>
            Đổi mật khẩu
          </RouterLink>
        </li>

        <li><hr class="dropdown-divider" /></li>

        <li>
          <button
              class="dropdown-item text-danger"
              @click="logout"
          >
            <i class="bi bi-box-arrow-right me-2"></i>
            Đăng xuất
          </button>
        </li>
      </ul>
    </div>

  </nav>
</template>

<script setup>
import { computed } from 'vue'

const user = JSON.parse(localStorage.getItem('user')) || {}

const username = computed(() => user.username || 'Admin')
const firstLetter = computed(() =>
    username.value.charAt(0).toUpperCase()
)

const logout = () => {
  localStorage.removeItem('user')
  location.href = '/login'
}
</script>

<style scoped>
/* ===== HEADER ===== */
.admin-header {
  background: linear-gradient(90deg, #020617, #0F172A, #1E293B);
  border-bottom: 1px solid #1E293B;
}

/* Logo */
.navbar-brand {
  letter-spacing: 0.6px;
  color: #F8FAFC;
  font-weight: 600;
}

/* Home button */
.home-btn {
  color: #CBD5F5;
}
.home-btn:hover {
  color: #60A5FA !important;
}

/* ===== AVATAR ===== */
.avatar-circle {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563EB, #1D4ED8);
  color: #FFFFFF;
  border: 1px solid #3B82F6;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== DROPDOWN ===== */
.dropdown-dark {
  background-color: #020617;
  border: 1px solid #1E293B;
  box-shadow: 0 12px 28px rgba(0,0,0,0.45);
}

.dropdown-dark .dropdown-item {
  color: #E5E7EB;
}

.dropdown-dark .dropdown-item:hover {
  background-color: #1E293B;
  color: #60A5FA;
}

.dropdown-divider {
  border-color: #1E293B;
}
</style>
