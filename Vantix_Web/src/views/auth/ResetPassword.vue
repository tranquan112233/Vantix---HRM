<template>
  <div class="auth-card">
    <div class="card-head">
      <div class="icon-box"><i class="bi bi-key"></i></div>
      <h1 class="card-title">Đặt mật khẩu mới</h1>
      <p class="card-sub">Chọn mật khẩu mạnh cho tài khoản của bạn.</p>
    </div>

    <form class="auth-form" @submit.prevent="handleReset" novalidate>

      <!-- New password -->
      <div class="field-group" :class="{ 'field-error': errors.newPassword }">
        <label class="field-label">Mật khẩu mới</label>
        <div class="input-wrap">
          <i class="bi bi-lock input-icon"></i>
          <input
              v-model="form.newPassword"
              :type="showPw1 ? 'text' : 'password'"
              class="field-input"
              placeholder="Ít nhất 8 ký tự"
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
        <label class="field-label">Xác nhận mật khẩu</label>
        <div class="input-wrap">
          <i class="bi bi-lock-fill input-icon"></i>
          <input
              v-model="form.confirmPassword"
              :type="showPw2 ? 'text' : 'password'"
              class="field-input"
              placeholder="Nhập lại mật khẩu"
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
        <span>{{ loading ? 'Đang cập nhật...' : 'Đặt lại mật khẩu' }}</span>
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
  { label: 'Ít nhất 8 ký tự',            passed: form.newPassword.length >= 8 },
  { label: 'Có chữ cái viết hoa',        passed: /[A-Z]/.test(form.newPassword) },
  { label: 'Có chữ số',                  passed: /\d/.test(form.newPassword) },
  { label: 'Có ký tự đặc biệt',          passed: /[^A-Za-z0-9]/.test(form.newPassword) },
])

const strengthScore = computed(() => rules.value.filter(r => r.passed).length)
const strengthPct   = computed(() => (strengthScore.value / 4) * 100)
const strengthClass = computed(() => ['', 'weak', 'fair', 'good', 'strong'][strengthScore.value])
const strengthLabel = computed(() => ['', 'Yếu', 'Trung bình', 'Tốt', 'Mạnh'][strengthScore.value])

function onPasswordInput() { errors.newPassword = '' }

function validate() {
  let ok = true
  if (!form.newPassword)         { errors.newPassword = 'Vui lòng nhập mật khẩu!'; ok = false }
  else if (form.newPassword.length < 8) { errors.newPassword = 'Mật khẩu cần ít nhất 8 ký tự!'; ok = false }
  if (!form.confirmPassword)     { errors.confirmPassword = 'Vui lòng xác nhận mật khẩu!'; ok = false }
  else if (form.newPassword !== form.confirmPassword) { errors.confirmPassword = 'Mật khẩu xác nhận không khớp!'; ok = false }
  return ok
}

async function handleReset() {
  if (!validate()) return
  loading.value = true
  globalError.value = ''
  try {
    await authService.resetPassword({ token, newPassword: form.newPassword, confirmPassword: form.confirmPassword })
    successMsg.value = 'Cập nhật mật khẩu thành công. Đang chuyển về trang đăng nhập...'
    setTimeout(() => router.push('/auth/login'), 1800)
  } catch (err) {
    if (err?.errors) Object.assign(errors, err.errors)
    else globalError.value = err?.message || 'Không thể đặt lại mật khẩu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>

<!-- Auth page styles are defined in assets/css/management.css (shared across all auth views) -->
