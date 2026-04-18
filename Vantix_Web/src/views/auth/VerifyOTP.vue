<template>
  <div class="auth-card">
    <RouterLink to="/auth/forgot-password" class="back-link">
      <i class="bi bi-arrow-left"></i> Quay lại
    </RouterLink>

    <div class="card-head">
      <div class="icon-box"><i class="bi bi-shield-lock"></i></div>
      <h1 class="card-title">Nhập mã OTP</h1>
      <p class="card-sub">
        Chúng tôi đã gửi mã 6 chữ số đến
        <strong class="email-highlight">{{ email || 'email của bạn' }}</strong>.
        Mã có hiệu lực trong 5 phút.
      </p>
    </div>

    <form class="auth-form" @submit.prevent="handleVerify" novalidate>

      <!-- OTP 6 boxes -->
      <div class="otp-row">
        <input
            v-for="(_, i) in 6" :key="i"
            :ref="el => (otpRefs[i] = el)"
            :value="otpDigits[i]"
            type="text"
            inputmode="numeric"
            maxlength="1"
            class="otp-box"
            :class="{ 'otp-box--error': hasError }"
            @input="onInput(i, $event)"
            @keydown="onKeydown(i, $event)"
            @paste.prevent="onPaste($event)"
        />
      </div>

      <span v-if="error" class="error-center">{{ error }}</span>

      <button type="submit" class="btn-primary" :disabled="loading || otp.length < 6">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <span>{{ loading ? 'Đang xác thực...' : 'Xác thực mã' }}</span>
      </button>

      <!-- Resend -->
      <p class="resend-row">
        Chưa nhận được mã?
        <button type="button" class="resend-btn" :disabled="resendCooldown > 0" @click="resend">
          <span v-if="resendCooldown > 0">Gửi lại sau {{ resendCooldown }} giây</span>
          <span v-else>Gửi lại mã</span>
        </button>
      </p>

      <div v-if="successMsg" class="alert-success">
        <i class="bi bi-check-circle-fill"></i> {{ successMsg }}
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import authService from '@/services/auth.service'

const router = useRouter()
const route  = useRoute()

const email       = route.query.email || ''
const otpDigits   = ref(Array(6).fill(''))
const otpRefs     = ref([])
const loading     = ref(false)
const error       = ref('')
const hasError    = ref(false)
const successMsg  = ref('')
const resendCooldown = ref(0)

let timer = null

const otp = computed(() => otpDigits.value.join(''))

onMounted(() => otpRefs.value[0]?.focus())
onUnmounted(() => clearInterval(timer))

function onInput(i, e) {
  const val = e.target.value.replace(/\D/g, '')
  otpDigits.value[i] = val.slice(-1)
  e.target.value = otpDigits.value[i]  // sync DOM
  error.value = ''; hasError.value = false
  if (otpDigits.value[i] && i < 5) otpRefs.value[i + 1]?.focus()
}

function onKeydown(i, e) {
  if (e.key === 'Backspace' && !otpDigits.value[i] && i > 0) {
    otpRefs.value[i - 1]?.focus()
  }
}

function onPaste(e) {
  const text = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
  text.split('').forEach((ch, i) => { otpDigits.value[i] = ch })
  otpRefs.value[Math.min(text.length, 5)]?.focus()
}

async function handleVerify() {
  if (otp.value.length < 6) {
    error.value = 'Vui lòng nhập đủ 6 chữ số'
    hasError.value = true
    return
  }

  loading.value = true
  error.value = ''

  try {
    // authService.verifyOtp đã unwrap res.data → nhận resetToken (string UUID) trực tiếp
    const resetToken = await authService.verifyOtp({ email, otp: otp.value })

    successMsg.value = 'Xác thực OTP thành công!'

    setTimeout(() => {
      router.push({
        name: 'reset-password',
        query: { token: resetToken } // ❗ chỉ truyền token
      })
    }, 800)

  } catch (err) {
    error.value = err?.errors?.otp || err?.message || 'Mã OTP không hợp lệ.'
    hasError.value = true
    otpDigits.value = Array(6).fill('')
    otpRefs.value.forEach(el => { if (el) el.value = '' })
    otpRefs.value[0]?.focus()
  } finally {
    loading.value = false
  }
}

async function resend() {
  if (!email) return
  try {
    await authService.forgotPassword(email)
    resendCooldown.value = 60
    timer = setInterval(() => {
      if (resendCooldown.value <= 1) { clearInterval(timer); resendCooldown.value = 0 }
      else resendCooldown.value--
    }, 1000)
    successMsg.value = 'Mã OTP mới đã được gửi.'
    setTimeout(() => successMsg.value = '', 3000)
  } catch {
    error.value = 'Không thể gửi lại OTP.'
  }
}
</script>

<!-- Auth page styles are defined in assets/css/management.css (shared across all auth views) -->
