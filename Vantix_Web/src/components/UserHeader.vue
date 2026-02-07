<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-primary px-3 shadow">
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
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const jwtUser = ref(null)

/**
 * Decode JWT payload (base64url)
 */
function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

const loadUserFromJwt = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    jwtUser.value = null
    return
  }

  const payload = decodeJwt(token)
  if (!payload) {
    jwtUser.value = null
    return
  }

  // Optional: check expire
  if (payload.exp && Date.now() >= payload.exp * 1000) {
    logout()
    return
  }

  jwtUser.value = {
    username: payload.sub,
    role: payload.role
  }
}

onMounted(loadUserFromJwt)

/* ===== COMPUTED ===== */

const username = computed(() => jwtUser.value?.username || 'User')

const firstLetter = computed(() =>
    (username.value.charAt(0) || '?').toUpperCase()
)

const isAdmin = computed(() =>
    (jwtUser.value?.role || '').toUpperCase() === 'ADMIN'
)

/* ===== ACTIONS ===== */

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>


<style scoped>
.avatar-circle {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366F1, #4F46E5);
  color: #FFFFFF;
  border: 1px solid #818CF8;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.45);
}
</style>
