<template>
  <nav class="navbar navbar-expand-lg navbar-dark user-header px-3 shadow">
    <!-- LOGO -->
    <RouterLink
        to="/"
        class="navbar-brand fw-bold text-white text-decoration-none"
    >
      <i class="bi bi-building"></i> Vantix HRM
    </RouterLink>

    <div class="ms-auto dropdown">
      <button
          class="btn p-0 border-0 bg-transparent d-flex align-items-center gap-2"
          data-bs-toggle="dropdown"
      >
        <div class="avatar-circle">
          {{ firstLetter }}
        </div>
        <span class="text-white fw-semibold">{{ username }}</span>
        <i class="bi bi-chevron-down text-white small"></i>
      </button>

      <ul class="dropdown-menu dropdown-menu-end shadow">
        <li>
          <RouterLink class="dropdown-item" to="/profile">
            <i class="bi bi-person-circle me-2"></i> Hồ sơ cá nhân
          </RouterLink>
        </li>

        <li>
          <a class="dropdown-item" href="#">
            <i class="bi bi-key-fill"></i> Đổi mật khẩu
          </a>
        </li>

        <li v-if="isAdmin"><hr class="dropdown-divider" /></li>

        <li v-if="isAdmin">
          <RouterLink
              to="/admin"
              class="dropdown-item text-primary fw-bold"
          >
            <i class="bi bi-hdd-stack-fill"></i> Admin Panel
          </RouterLink>
        </li>

        <li><hr class="dropdown-divider" /></li>

        <li>
          <a
              class="dropdown-item text-danger"
              href="#"
              @click.prevent="logout"
          >
            <i class="bi bi-box-arrow-right me-2"></i> Đăng xuất
          </a>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'

// TEST DATA
const user = {
  username: 'Admin',
  role: { roleName: 'ADMIN' }
}

const username = computed(() => user.username)
const firstLetter = computed(() =>
    username.value.charAt(0).toUpperCase()
)

const isAdmin = computed(() => user.role.roleName === 'ADMIN')

const logout = () => {
  localStorage.removeItem('user')
  location.href = '/login'
}
</script>

<style scoped>
.user-header {
  background: linear-gradient(90deg, #020617, #0F172A, #1E293B);
  border-bottom: 1px solid #1E293B;
}
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
  line-height: 1;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.45);
}
</style>
