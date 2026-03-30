<template>
  <div class="auth-card">
    <RouterLink to="/auth/login" class="back-link">
      <i class="bi bi-arrow-left"></i> Back to login
    </RouterLink>

    <div class="card-head">
      <div class="icon-box"><i class="bi bi-envelope-paper"></i></div>
      <h1 class="card-title">Forgot password?</h1>
      <p class="card-sub">Enter your email address and we'll send you a 6-digit OTP code.</p>
    </div>

    <form class="auth-form" @submit.prevent="handleSubmit" novalidate>

      <div class="field-group" :class="{ 'field-error': error }">
        <label class="field-label">Email address</label>
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
        <span>{{ loading ? 'Sending...' : 'Send OTP' }}</span>
      </button>

    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import authService from '@/services/auth.service'

const router = useRouter()
const toast = useToast()

const email      = ref('')
const error      = ref('')
const loading    = ref(false)
const successMsg = ref('')

async function handleSubmit() {
  if (!email.value.trim()) { error.value = 'Email is required'; return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) { error.value = 'Invalid email address'; return }

  loading.value = true
  error.value = ''
  successMsg.value = ''
  try {
    await authService.forgotPassword(email.value)
    toast.success('OTP code already sent! Check your inbox.')
    // Navigate after a short delay so the user sees a success message
    setTimeout(() => {
      router.push({ name: 'verify-otp', query: { email: email.value } })
    }, 1200)
  } catch (err) {
    error.value = err?.errors?.email || err?.message || 'Failed to send OTP. Please try again.'
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
  padding: 36px 36px 40px;
  box-shadow: 0 4px 32px rgba(0,0,0,.08), 0 1px 4px rgba(0,0,0,.04);
}

.back-link {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12.5px; font-weight: 500; color: #6366f1;
  text-decoration: none; margin-bottom: 24px;
  transition: color .15s;
}
.back-link:hover { color: #4f46e5; }

.icon-box {
  width: 48px; height: 48px; border-radius: 13px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #6366f1; margin-bottom: 14px;
}

.card-head  { margin-bottom: 26px; }
.card-title { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0 0 6px; letter-spacing: -.4px; }
.card-sub   { font-size: 13px; color: #64748b; margin: 0; line-height: 1.6; }

.auth-form { display: flex; flex-direction: column; gap: 16px; }

.field-group  { display: flex; flex-direction: column; gap: 5px; }
.field-label  { font-size: 12.5px; font-weight: 600; color: #374151; }

.input-wrap { position: relative; display: flex; align-items: center; }
.input-icon { position: absolute; left: 11px; color: #94a3b8; font-size: 14px; pointer-events: none; }
.field-input {
  width: 100%; height: 40px;
  padding: 0 12px 0 34px;
  border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 13.5px; color: #0f172a; background: #f8fafc;
  outline: none;
  transition: border-color .18s, box-shadow .18s, background .18s;
}
.field-input::placeholder { color: #94a3b8; }
.field-input:focus { border-color: #6366f1; background: #fff; box-shadow: 0 0 0 3px rgba(99,102,241,.12); }
.field-error .field-input { border-color: #ef4444; background: #fff5f5; }
.error-msg { font-size: 11.5px; color: #ef4444; font-weight: 500; }

.btn-primary {
  height: 42px; width: 100%; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  box-shadow: 0 3px 14px rgba(99,102,241,.35);
  transition: opacity .18s, transform .12s, box-shadow .18s;
}
.btn-primary:hover:not(:disabled) { opacity: .92; transform: translateY(-1px); }
.btn-primary:disabled { opacity: .65; cursor: not-allowed; }

@media (max-width: 480px) {
  .auth-card { padding: 24px 20px 28px; border-radius: 14px; }
}
</style>