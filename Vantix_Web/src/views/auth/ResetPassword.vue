<template>
  <div class="auth-card">
    <div class="card-head">
      <div class="icon-box"><i class="bi bi-key"></i></div>
      <h1 class="card-title">Set new password</h1>
      <p class="card-sub">Choose a strong password for your account.</p>
    </div>

    <form class="auth-form" @submit.prevent="handleReset" novalidate>

      <!-- New password -->
      <div class="field-group" :class="{ 'field-error': errors.newPassword }">
        <label class="field-label">New password</label>
        <div class="input-wrap">
          <i class="bi bi-lock input-icon"></i>
          <input
              v-model="form.newPassword"
              :type="showPw1 ? 'text' : 'password'"
              class="field-input"
              placeholder="At least 8 characters"
              autocomplete="new-password"
              @input="onPasswordInput"
          />
          <button type="button" class="pw-toggle" @click="showPw1 = !showPw1" tabindex="-1">
            <i :class="showPw1 ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
        <span v-if="errors.newPassword" class="error-msg">{{ errors.newPassword }}</span>

        <!-- Strength indicator -->
        <div v-if="form.newPassword" class="strength-wrap">
          <div class="strength-bar">
            <div class="strength-fill" :style="{ width: strengthPct + '%' }" :class="strengthClass"></div>
          </div>
          <span class="strength-label" :class="strengthClass">
            <i class="bi" :class="{
              'bi-x-circle': strengthScore <= 1,
              'bi-exclamation-circle': strengthScore == 2,
              'bi-check-circle': strengthScore >= 3
            }"></i>
            {{ strengthLabel }}
          </span>
        </div>
      </div>

      <!-- Confirm password -->
      <div class="field-group" :class="{ 'field-error': errors.confirmPassword }">
        <label class="field-label">Confirm password</label>
        <div class="input-wrap">
          <i class="bi bi-lock-fill input-icon"></i>
          <input
              v-model="form.confirmPassword"
              :type="showPw2 ? 'text' : 'password'"
              class="field-input"
              placeholder="Re-enter your password"
              autocomplete="new-password"
              @input="errors.confirmPassword = ''"
          />
          <button type="button" class="pw-toggle" @click="showPw2 = !showPw2" tabindex="-1">
            <i :class="showPw2 ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>
        <span v-if="errors.confirmPassword" class="error-msg">{{ errors.confirmPassword }}</span>
      </div>

      <!-- Password rules -->
      <ul class="rules-list">
        <li v-for="r in rules" :key="r.label" :class="{ 'rule-ok': r.passed }">
          <i :class="r.passed ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
          {{ r.label }}
        </li>
      </ul>

      <button type="submit" class="btn-primary" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <span>{{ loading ? 'Updating...' : 'Reset password' }}</span>
      </button>

      <div v-if="globalError" class="alert-error">
        <i class="bi bi-exclamation-circle-fill"></i> {{ globalError }}
      </div>
      <div v-if="successMsg" class="alert-success">
        <i class="bi bi-check-circle-fill"></i> {{ successMsg }}
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import authService from '@/services/auth.service'

const router = useRouter()
const route  = useRoute()

const token = route.query.token || ''

const form = reactive({ newPassword: '', confirmPassword: '' })
const errors = reactive({ newPassword: '', confirmPassword: '' })
const showPw1    = ref(false)
const showPw2    = ref(false)
const loading    = ref(false)
const globalError = ref('')
const successMsg  = ref('')

// ── Password strength ──────────────────────────────────────────────────────
const rules = computed(() => [
  { label: 'At least 8 characters',      passed: form.newPassword.length >= 8 },
  { label: 'Contains uppercase letter',  passed: /[A-Z]/.test(form.newPassword) },
  { label: 'Contains a number',          passed: /\d/.test(form.newPassword) },
  { label: 'Contains special character', passed: /[^A-Za-z0-9]/.test(form.newPassword) },
])

const strengthScore = computed(() => rules.value.filter(r => r.passed).length)
const strengthPct   = computed(() => (strengthScore.value / 4) * 100)
const strengthClass = computed(() => ['', 'weak', 'fair', 'good', 'strong'][strengthScore.value])
const strengthLabel = computed(() => ['', 'Weak', 'Fair', 'Good', 'Strong'][strengthScore.value])

function onPasswordInput() { errors.newPassword = '' }

function validate() {
  let ok = true
  if (!form.newPassword)         { errors.newPassword = 'Password is required!'; ok = false }
  else if (form.newPassword.length < 8) { errors.newPassword = 'At least 8 characters required!'; ok = false }
  if (!form.confirmPassword)     { errors.confirmPassword = 'Please confirm your password!'; ok = false }
  else if (form.newPassword !== form.confirmPassword) { errors.confirmPassword = 'Passwords do not match!'; ok = false }
  return ok
}

async function handleReset() {
  if (!validate()) return
  loading.value = true
  globalError.value = ''
  try {
    await authService.resetPassword({ token, newPassword: form.newPassword, confirmPassword: form.confirmPassword })
    successMsg.value = 'Password updated successfully! Redirecting to login...'
    setTimeout(() => router.push('/auth/login'), 1800)
  } catch (err) {
    if (err?.errors) Object.assign(errors, err.errors)
    else globalError.value = err?.message || 'Failed to reset password. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-card {
  width: 100%; max-width: 420px; background: #fff;
  border-radius: 18px; padding: 36px 36px 40px;
  box-shadow: 0 4px 32px rgba(0,0,0,.08), 0 1px 4px rgba(0,0,0,.04);
}

.icon-box {
  width: 48px; height: 48px; border-radius: 13px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #6366f1; margin-bottom: 14px;
}

.card-head  { margin-bottom: 26px; }
.card-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0 0 6px; letter-spacing: -.4px; }
.card-sub   { font-size: 13px; color: #64748b; margin: 0; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field-group { display: flex; flex-direction: column; gap: 5px; }
.field-label { font-size: 12.5px; font-weight: 600; color: #374151; }

.input-wrap { position: relative; display: flex; align-items: center; }
.input-icon { position: absolute; left: 11px; color: #94a3b8; font-size: 14px; pointer-events: none; }
.field-input {
  width: 100%; height: 40px; padding: 0 38px 0 34px;
  border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 13.5px; color: #0f172a; background: #f8fafc; outline: none;
  transition: border-color .18s, box-shadow .18s, background .18s;
}
.field-input::placeholder { color: #94a3b8; }
.field-input:focus { border-color: #6366f1; background: #fff; box-shadow: 0 0 0 3px rgba(99,102,241,.12); }
.field-error .field-input { border-color: #ef4444; background: #fff5f5; }
.pw-toggle {
  position: absolute; right: 10px; border: none; background: transparent;
  color: #94a3b8; font-size: 15px; cursor: pointer; padding: 2px; transition: color .15s;
}
.pw-toggle:hover { color: #475569; }

/* Ẩn icon mắt mặc định của trình duyệt (Edge/IE/Chrome) */
.field-input::-ms-reveal,
.field-input::-ms-clear { display: none; }
.error-msg { font-size: 11.5px; color: #ef4444; font-weight: 500; }

/* Strength bar */
.strength-wrap { display: flex; align-items: center; gap: 10px; margin-top: 2px; }
.strength-bar { flex: 1; height: 4px; background: #e2e8f0; border-radius: 99px; overflow: hidden; }
.strength-fill { height: 100%; border-radius: 99px; transition: width .3s, background .3s; }
/* chỉ dùng cho thanh bar */
.strength-fill.weak   { background: #ef4444; }
.strength-fill.fair   { background: #f59e0b; }
.strength-fill.good   { background: #3b82f6; }
.strength-fill.strong { background: #22c55e; }

.strength-label { font-size: 11.5px; font-weight: 500; }
/* chỉ dùng cho text */
.strength-label.weak   { color: #ef4444; }
.strength-label.fair   { color: #d97706; }
.strength-label.good   { color: #2563eb; }
.strength-label.strong { color: #16a34a; }

/* Rules list */
.rules-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 6px; }
.rules-list li {
  display: flex; align-items: center; gap: 7px;
  font-size: 12px; color: #94a3b8;
  transition: color .2s;
}
.rule-ok { color: #22c55e !important; }
.rules-list li i { font-size: 12px; }

.btn-primary {
  height: 42px; width: 100%; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  box-shadow: 0 3px 14px rgba(99,102,241,.35);
  transition: opacity .18s, transform .12s;
  margin-top: 4px;
}
.btn-primary:hover:not(:disabled) { opacity: .92; transform: translateY(-1px); }
.btn-primary:disabled { opacity: .65; cursor: not-allowed; }

.alert-error {
  display: flex; align-items: center; gap: 8px; padding: 10px 13px;
  border-radius: 9px; background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 12.5px; font-weight: 500;
}
.alert-success {
  display: flex; align-items: center; gap: 8px; padding: 10px 13px;
  border-radius: 9px; background: #f0fdf4; border: 1px solid #bbf7d0;
  color: #16a34a; font-size: 12.5px; font-weight: 500;
}

@media (max-width: 480px) {
  .auth-card { padding: 24px 20px 28px; border-radius: 14px; }
}
</style>