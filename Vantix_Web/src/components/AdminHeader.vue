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
        to="/"
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
  background: linear-gradient(90deg, #0f0f0f, #676767);
  border-bottom: 1px solid #2a2a2a;
}

/* Logo */
.navbar-brand {
  letter-spacing: 0.5px;
  color: #f1f1f1;
}

/* Home button */
.home-btn {
  color: #cfcfcf;
}
.home-btn:hover {
  color: #ffffff !important;
}

/* ===== AVATAR ===== */
.avatar-circle {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background-color: #1c1c1c;
  color: #ffffff;
  border: 1px solid #3a3a3a;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== DROPDOWN ===== */
.dropdown-dark {
  background-color: #1a1a1a;
  border: 1px solid #2f2f2f;
}

.dropdown-dark .dropdown-item {
  color: #e0e0e0;
}

.dropdown-dark .dropdown-item:hover {
  background-color: #2a2a2a;
  color: #ffffff;
}

.dropdown-divider {
  border-color: #2f2f2f;
}
</style>
