<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useSettingsStore } from '@/stores/settings'

const router = useRouter()
const settings = useSettingsStore()
const loading = ref(false)

const form = reactive({
  requestId: '',
  otp: '',
})

onMounted(() => {
  form.requestId = sessionStorage.getItem('vx_reset_request_id') || ''
  sessionStorage.removeItem('vx_reset_dev_otp')
  if (!form.requestId) router.replace('/forgot-password')
})

async function submit() {
  if (!form.otp) {
    ElMessage.error(settings.t('auth.otpRequired'))
    return
  }
  loading.value = true
  try {
    const { data } = await authApi.verifyOtp({ ...form })
    sessionStorage.setItem('vx_reset_token', data.resetToken)
    ElMessage.success(settings.t('auth.otpVerified'))
    router.push('/reset-password')
  } catch (e) {
    ElMessage.error(e.response?.data?.errors?.otp || e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <div class="auth-card-header">
      <h2>{{ settings.t('auth.verifyOtp') }}</h2>
      <p>{{ settings.t('auth.verifyOtpSubtitle') }}</p>
    </div>

    <el-input v-model="form.otp" class="otp-input" maxlength="6" :placeholder="settings.t('auth.otp')" :prefix-icon="Key" size="large" @keyup.enter="submit" />
    <el-button type="primary" class="auth-submit" :loading="loading" @click="submit">
      {{ settings.t('auth.verifyOtp') }}
    </el-button>

    <router-link class="auth-link" to="/forgot-password">{{ settings.t('auth.resendOtp') }}</router-link>
  </div>
</template>

<style scoped>
.auth-card { width: 100%; max-width: 380px; }
.auth-card-header { margin-bottom: 24px; }
.auth-card-header h2 { font-size: var(--vx-font-size-4xl); color: var(--vx-text); margin-bottom: 8px; }
.auth-card-header p { color: var(--vx-text-secondary); line-height: 1.5; }
.otp-input { margin-bottom: 18px; }
.auth-submit { width: 100%; height: 44px; border-radius: 8px; font-weight: 700; }
.auth-link { display: inline-flex; margin-top: 18px; color: var(--vx-primary); text-decoration: none; font-weight: 700; }
</style>
