<template>
  <div class="auth-card">
    <div class="card-head">
      <h1 class="card-title">Welcome back</h1>
      <p class="card-sub">Sign in to your Vantix HRM account</p>
    </div>

    <form class="auth-form" @submit.prevent="handleLogin" novalidate>

      <!-- Username / Email -->
      <div class="field-group" :class="{ 'field-error': errors.usernameOrEmail }">
        <label class="field-label">Username or Email</label>
        <div class="input-wrap">
          <i class="bi bi-person input-icon"></i>
          <input
              v-model="form.usernameOrEmail"
              type="text"
              class="field-input"
              placeholder="Enter your username or email"
              autocomplete="username"
              @input="clearError('usernameOrEmail')"
          />
        </div>
        <span v-if="errors.usernameOrEmail" class="error-msg">{{ errors.usernameOrEmail }}</span>
      </div>

      <!-- Password -->
      <div class="field-group" :class="{ 'field-error': errors.password }">
        <label class="field-label">Password</label>
        <div class="input-wrap">
          <i class="bi bi-lock input-icon"></i>
          <input
              v-model="form.password"
              :type="showPw ? 'text' : 'password'"
              class="field-input"
              placeholder="Enter your password"
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
        <RouterLink to="/auth/forgot-password" class="forgot-link">Forgot password?</RouterLink>
      </div>

      <!-- Submit -->
      <button type="submit" class="btn-primary" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <span>{{ loading ? 'Signing in...' : 'Sign in' }}</span>
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from 'vue-toastification'

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
      toast.error(err?.message || 'Login failed. Please try again.')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 40px 36px;
  box-shadow: var(--shadow-lg);
}

.card-head  { margin-bottom: 28px; }
.card-title { font-size: 22px; font-weight: 800; color: var(--text-dark); margin: 0 0 5px; letter-spacing: -.4px; }
.card-sub   { font-size: 13.5px; color: var(--text-muted-dark); margin: 0; }

/* ── Form ─────────────────────────────────────────────────────────────────── */
.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field-group  { display: flex; flex-direction: column; gap: 5px; }
.field-label  { font-size: 12.5px; font-weight: 600; color: var(--text-darker); }

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute; left: 11px;
  color: var(--input-icon-color); font-size: 14px; pointer-events: none;
}
.field-input {
  width: 100%; height: 40px;
  padding: 0 38px 0 34px;
  border: 1.5px solid var(--border-light);
  border-radius: var(--radius-md);
  font-size: 13.5px; color: var(--text-dark);
  background: var(--bg);
  outline: none;
  transition: border-color .18s, box-shadow .18s, background .18s;
}
.field-input::placeholder { color: var(--input-icon-color); }
.field-input:focus {
  border-color: var(--primary-color);
  background: var(--bg-white);
  box-shadow: var(--primary-focus-shadow);
}
.field-error .field-input {
  border-color: var(--danger-color);
  background: var(--danger-bg-light);
}
.field-error .field-input:focus { box-shadow: var(--danger-focus-shadow); }

.pw-toggle {
  position: absolute; right: 10px;
  border: none; background: transparent;
  color: var(--input-icon-color); font-size: 15px; cursor: pointer; padding: 2px;
  transition: color .15s;
}
.pw-toggle:hover { color: var(--sidebar-text-dim); }

/* Ẩn icon mắt mặc định của trình duyệt (Edge/IE/Chrome) */
.field-input::-ms-reveal,
.field-input::-ms-clear { display: none; }

.error-msg { font-size: 11.5px; color: var(--danger-color); font-weight: 500; }

/* ── Forgot row ───────────────────────────────────────────────────────────── */
.forgot-row { display: flex; justify-content: flex-end; margin-top: -4px; }
.forgot-link {
  font-size: 12.5px; font-weight: 500;
  color: var(--primary-color); text-decoration: none;
  transition: color .15s;
}
.forgot-link:hover { color: var(--primary-color-dark); text-decoration: underline; }

/* ── Submit ───────────────────────────────────────────────────────────────── */
.btn-primary {
  height: 42px; width: 100%;
  border: none; border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-dark));
  color: var(--text-color-light); font-size: 14px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  gap: 6px; margin-top: 4px;
  transition: opacity .18s, transform .12s, box-shadow .18s;
  box-shadow: var(--primary-shadow);
}
.btn-primary:hover:not(:disabled) { opacity: .92; transform: translateY(-1px); box-shadow: var(--primary-shadow-hover); }
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { opacity: .65; cursor: not-allowed; }

@media (max-width: 480px) {
  .auth-card { padding: 28px 20px; border-radius: 14px; }
}
</style>
