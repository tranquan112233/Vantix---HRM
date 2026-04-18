<template>
  <div class="auth-card">
    <RouterLink to="/auth/login" class="back-link">
      <i class="bi bi-arrow-left"></i> Quay lại đăng nhập
    </RouterLink>

    <div class="card-head">
      <div class="icon-box"><i class="bi bi-envelope-paper"></i></div>
      <h1 class="card-title">Quên mật khẩu?</h1>
      <p class="card-sub">Nhập email để nhận mã OTP gồm 6 chữ số.</p>
    </div>

    <form class="auth-form" @submit.prevent="handleSubmit" novalidate>

      <div class="field-group" :class="{ 'field-error': error }">
        <label class="field-label">Địa chỉ email</label>
        <div class="input-wrap">
          <i class="bi bi-envelope input-icon"></i>
          <input
              v-model="email"
              type="email"
              class="field-input"
              placeholder="you@example.com"
              autocomplete="email"
              @input="error = ''"
          />
        </div>
        <span v-if="error" class="error-msg">{{ error }}</span>
      </div>

      <button type="submit" class="btn-primary" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <span>{{ loading ? 'Đang gửi...' : 'Gửi OTP' }}</span>
      </button>

    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useToast } from '@/utils/toast'
import authService from '@/services/auth.service'

const router = useRouter()
const toast = useToast()

const email      = ref('')
const error      = ref('')
const loading    = ref(false)
const successMsg = ref('')

async function handleSubmit() {
  if (!email.value.trim()) { error.value = 'Vui lòng nhập email'; return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) { error.value = 'Email không hợp lệ'; return }

  loading.value = true
  error.value = ''
  successMsg.value = ''
  try {
    await authService.forgotPassword(email.value)
    toast.success('Mã OTP đã được gửi. Vui lòng kiểm tra hộp thư.')
    // Navigate after a short delay so the user sees a success message
    setTimeout(() => {
      router.push({ name: 'verify-otp', query: { email: email.value } })
    }, 1200)
  } catch (err) {
    error.value = err?.errors?.email || err?.message || 'Không thể gửi OTP. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>

<!-- Auth page styles are defined in assets/css/management.css (shared across all auth views) -->
