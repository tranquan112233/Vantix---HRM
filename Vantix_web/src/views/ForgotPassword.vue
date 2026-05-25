<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useSettingsStore } from '@/stores/settings'
import { showApiError } from '@/utils/errors'

const router = useRouter()
const settings = useSettingsStore()
const formRef = ref(null)
const loading = ref(false)
const devOtp = ref('')

const form = reactive({
  email: '',
})

const rules = {
  email: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
    { type: 'email', message: settings.t('common.emailInvalid'), trigger: ['blur', 'change'] },
  ],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const { data } = await authApi.forgotPassword({ email: form.email.trim() })
    if (!data.requestId) {
      ElMessage.success(settings.t('auth.otpSent'))
      return
    }
    sessionStorage.setItem('vx_reset_request_id', data.requestId)
    if (data.devOtp) {
      devOtp.value = data.devOtp
      sessionStorage.setItem('vx_reset_dev_otp', data.devOtp)
    } else {
      devOtp.value = ''
      sessionStorage.removeItem('vx_reset_dev_otp')
    }
    ElMessage.success(settings.t('auth.otpSent'))
    router.push('/verify-otp')
  } catch (e) {
    showApiError(e, settings, e.response?.status === 404 ? 'auth.emailNotFound' : 'common.somethingWrong')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <div class="auth-card-header">
      <h2>{{ settings.t('auth.forgotPassword') }}</h2>
      <p>{{ settings.t('auth.forgotPasswordSubtitle') }}</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
      <el-form-item prop="email">
        <el-input v-model.trim="form.email" type="email" autocomplete="email" :placeholder="settings.t('user.email')" :prefix-icon="Message" />
      </el-form-item>
      <el-button type="primary" class="auth-submit" :loading="loading" @click="submit">
        {{ settings.t('auth.sendOtp') }}
      </el-button>
    </el-form>

    <router-link class="auth-link" to="/login">{{ settings.t('auth.backToLogin') }}</router-link>
  </div>
</template>

<style scoped>
.auth-card { width: 100%; max-width: 380px; }
.auth-card-header { margin-bottom: 28px; }
.auth-card-header h2 { font-size: var(--vx-font-size-4xl); color: var(--vx-text); margin-bottom: 8px; }
.auth-card-header p { color: var(--vx-text-secondary); line-height: 1.5; }
.auth-submit { width: 100%; height: 44px; border-radius: 8px; font-weight: 700; }
.auth-link { display: inline-flex; margin-top: 18px; color: var(--vx-primary); text-decoration: none; font-weight: 700; }
</style>
