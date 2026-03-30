<template>
  <div class="auth-card">
    <RouterLink to="/auth/forgot-password" class="back-link">
      <i class="bi bi-arrow-left"></i> Back
    </RouterLink>

    <div class="card-head">
      <div class="icon-box"><i class="bi bi-shield-lock"></i></div>
      <h1 class="card-title">Enter OTP code</h1>
      <p class="card-sub">
        We've sent a 6-digit code to
        <strong class="email-highlight">{{ email || 'your email' }}</strong>.
        The code expires in 5 minutes.
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
        <span>{{ loading ? 'Verifying...' : 'Verify code' }}</span>
      </button>

      <!-- Resend -->
      <p class="resend-row">
        Didn't receive it?
        <button type="button" class="resend-btn" :disabled="resendCooldown > 0" @click="resend">
          <span v-if="resendCooldown > 0">Resend in {{ resendCooldown }}s</span>
          <span v-else>Resend code</span>
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
    error.value = 'Please enter all 6 digits'
    hasError.value = true
    return
  }

  loading.value = true
  error.value = ''

  try {
    // authService.verifyOtp đã unwrap res.data → nhận resetToken (string UUID) trực tiếp
    const resetToken = await authService.verifyOtp({ email, otp: otp.value })

    successMsg.value = 'OTP verified!'

    setTimeout(() => {
      router.push({
        name: 'reset-password',
        query: { token: resetToken } // ❗ chỉ truyền token
      })
    }, 800)

  } catch (err) {
    error.value = err?.errors?.otp || err?.message || 'Invalid OTP code.'
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
    successMsg.value = 'A new OTP has been sent.'
    setTimeout(() => successMsg.value = '', 3000)
  } catch {
    error.value = 'Failed to resend OTP.'
  }
}
</script>

<style scoped>
.auth-card {
  width: 100%; max-width: 420px; background: #fff;
  border-radius: 18px; padding: 36px 36px 40px;
  box-shadow: 0 4px 32px rgba(0,0,0,.08), 0 1px 4px rgba(0,0,0,.04);
}

.back-link {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12.5px; font-weight: 500; color: #6366f1;
  text-decoration: none; margin-bottom: 24px; transition: color .15s;
}
.back-link:hover { color: #4f46e5; }

.icon-box {
  width: 48px; height: 48px; border-radius: 13px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #6366f1; margin-bottom: 14px;
}

.card-head  { margin-bottom: 26px; }
.card-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0 0 7px; letter-spacing: -.4px; }
.card-sub   { font-size: 13px; color: #64748b; margin: 0; line-height: 1.65; }
.email-highlight { color: #4f46e5; font-weight: 600; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }

/* OTP boxes */
.otp-row {
  display: flex; gap: 8px; justify-content: center;
}
.otp-box {
  width: 46px; height: 52px;
  border: 2px solid #e2e8f0; border-radius: 11px;
  text-align: center; font-size: 20px; font-weight: 700; color: #0f172a;
  background: #f8fafc; outline: none;
  transition: border-color .18s, box-shadow .18s, background .18s;
  caret-color: transparent;
}
.otp-box:focus {
  border-color: #6366f1; background: #fff;
  box-shadow: 0 0 0 3px rgba(99,102,241,.15);
}
.otp-box--error { border-color: #ef4444; background: #fff5f5; animation: shake .35s; }

@keyframes shake {
  0%,100% { transform: translateX(0); }
  20%,60%  { transform: translateX(-5px); }
  40%,80%  { transform: translateX(5px); }
}

.error-center { font-size: 12px; color: #ef4444; font-weight: 500; text-align: center; }

.btn-primary {
  height: 42px; width: 100%; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  box-shadow: 0 3px 14px rgba(99,102,241,.35);
  transition: opacity .18s, transform .12s;
}
.btn-primary:hover:not(:disabled) { opacity: .92; transform: translateY(-1px); }
.btn-primary:disabled { opacity: .55; cursor: not-allowed; }

.resend-row { text-align: center; font-size: 12.5px; color: #64748b; margin: 0; }
.resend-btn {
  border: none; background: transparent; color: #6366f1;
  font-size: 12.5px; font-weight: 600; cursor: pointer; padding: 0;
  transition: color .15s;
}
.resend-btn:hover:not(:disabled) { color: #4f46e5; text-decoration: underline; }
.resend-btn:disabled { color: #94a3b8; cursor: default; }

.alert-success {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 13px; border-radius: 9px;
  background: #f0fdf4; border: 1px solid #bbf7d0;
  color: #16a34a; font-size: 12.5px; font-weight: 500;
}

@media (max-width: 480px) {
  .auth-card { padding: 24px 20px 28px; border-radius: 14px; }
  .otp-box { width: 40px; height: 46px; font-size: 18px; }
}
</style>