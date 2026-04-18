<template>
  <div class="auth-card">
    <div class="card-head">
      <h1 class="card-title">Chào mừng trở lại</h1>
      <p class="card-sub">Đăng nhập vào tài khoản Vantix HRM</p>
    </div>

    <form class="auth-form" @submit.prevent="handleLogin" novalidate>

      <!-- Username / Email -->
      <div class="field-group" :class="{ 'field-error': errors.usernameOrEmail }">
        <label class="field-label">Tên đăng nhập hoặc Email</label>
        <div class="input-wrap">
          <i class="bi bi-person input-icon"></i>
          <input
              v-model="form.usernameOrEmail"
              type="text"
              class="field-input"
              placeholder="Nhập tên đăng nhập hoặc email"
              autocomplete="username"
              @input="clearError('usernameOrEmail')"
          />
        </div>
        <span v-if="errors.usernameOrEmail" class="error-msg">{{ errors.usernameOrEmail }}</span>
      </div>

      <!-- Password -->
      <div class="field-group" :class="{ 'field-error': errors.password }">
        <label class="field-label">Mật khẩu</label>
        <div class="input-wrap">
          <i class="bi bi-lock input-icon"></i>
          <input
              v-model="form.password"
              :type="showPw ? 'text' : 'password'"
              class="field-input"
              placeholder="Nhập mật khẩu"
              autocomplete="current-password"
              @input="clearError('password')"
          />
          <button type="button" class="pw-toggle" @click="showPw = !showPw" tabindex="-1">
            <i :class="showPw ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
        <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
      </div>

      <!-- Forgot password link -->
      <div class="forgot-row">
        <RouterLink to="/auth/forgot-password" class="forgot-link">Quên mật khẩu?</RouterLink>
      </div>

      <!-- Submit -->
      <button type="submit" class="btn-primary" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <span>{{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}</span>
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/utils/toast'

const router = useRouter()
const auth   = useAuthStore()
const toast  = useToast()

const form = reactive({ usernameOrEmail: '', password: '' })
const errors = reactive({ usernameOrEmail: '', password: '' })
const loading     = ref(false)
const showPw      = ref(false)

function clearError(field) {
  errors[field] = ''
}

async function handleLogin() {
  loading.value = true

  try {
    await auth.login({
      usernameOrEmail: form.usernameOrEmail,
      password: form.password
    })
    router.push('/')
  } catch (err) {
    // Xử lý lỗi giống y chang ForgotPassword
    errors.usernameOrEmail = err?.errors?.usernameOrEmail || ''
    errors.password = err?.errors?.password || ''

    // Nếu không có lỗi field cụ thể thì hiển thị toast với message chung
    if (!errors.usernameOrEmail && !errors.password) {
      toast.error(err?.message || 'Đăng nhập thất bại. Vui lòng thử lại.')
    }
  } finally {
    loading.value = false
  }
}
</script>

<!-- Auth page styles are defined in assets/css/management.css (shared across all auth views) -->
