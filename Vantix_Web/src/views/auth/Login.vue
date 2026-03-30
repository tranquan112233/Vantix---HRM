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
  background: #fff;
  border-radius: 18px;
  padding: 40px 36px;
  box-shadow: 0 4px 32px rgba(0,0,0,.08), 0 1px 4px rgba(0,0,0,.04);
}

.card-head  { margin-bottom: 28px; }
.card-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0 0 5px; letter-spacing: -.4px; }
.card-sub   { font-size: 13.5px; color: #64748b; margin: 0; }

/* ── Form ─────────────────────────────────────────────────────────────────── */
.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field-group  { display: flex; flex-direction: column; gap: 5px; }
.field-label  { font-size: 12.5px; font-weight: 600; color: #374151; }

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute; left: 11px;
  color: #94a3b8; font-size: 14px; pointer-events: none;
}
.field-input {
  width: 100%; height: 40px;
  padding: 0 38px 0 34px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13.5px; color: #0f172a;
  background: #f8fafc;
  outline: none;
  transition: border-color .18s, box-shadow .18s, background .18s;
}
.field-input::placeholder { color: #94a3b8; }
.field-input:focus {
  border-color: #6366f1;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(99,102,241,.12);
}
.field-error .field-input {
  border-color: #ef4444;
  background: #fff5f5;
}
.field-error .field-input:focus { box-shadow: 0 0 0 3px rgba(239,68,68,.12); }

.pw-toggle {
  position: absolute; right: 10px;
  border: none; background: transparent;
  color: #94a3b8; font-size: 15px; cursor: pointer; padding: 2px;
  transition: color .15s;
}
.pw-toggle:hover { color: #475569; }

/* Ẩn icon mắt mặc định của trình duyệt (Edge/IE/Chrome) */
.field-input::-ms-reveal,
.field-input::-ms-clear { display: none; }

.error-msg { font-size: 11.5px; color: #ef4444; font-weight: 500; }

/* ── Forgot row ───────────────────────────────────────────────────────────── */
.forgot-row { display: flex; justify-content: flex-end; margin-top: -4px; }
.forgot-link {
  font-size: 12.5px; font-weight: 500;
  color: #6366f1; text-decoration: none;
  transition: color .15s;
}
.forgot-link:hover { color: #4f46e5; text-decoration: underline; }

/* ── Submit ───────────────────────────────────────────────────────────────── */
.btn-primary {
  height: 42px; width: 100%;
  border: none; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: 14px; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  gap: 6px; margin-top: 4px;
  transition: opacity .18s, transform .12s, box-shadow .18s;
  box-shadow: 0 3px 14px rgba(99,102,241,.35);
}
.btn-primary:hover:not(:disabled) { opacity: .92; transform: translateY(-1px); box-shadow: 0 5px 18px rgba(99,102,241,.4); }
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { opacity: .65; cursor: not-allowed; }

@media (max-width: 480px) {
  .auth-card { padding: 28px 20px; border-radius: 14px; }
}
</style>