<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useSettingsStore } from '@/stores/settings'

const router = useRouter()
const settings = useSettingsStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  resetToken: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = computed(() => ({
  newPassword: [
    { required: true, message: settings.t('login.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 100, message: settings.t('profile.passwordLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: settings.t('login.passwordRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) callback(new Error(settings.t('profile.passwordMismatch')))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}))

onMounted(() => {
  form.resetToken = sessionStorage.getItem('vx_reset_token') || ''
  if (!form.resetToken) router.replace('/forgot-password')
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.resetPassword({ resetToken: form.resetToken, newPassword: form.newPassword })
    sessionStorage.removeItem('vx_reset_request_id')
    sessionStorage.removeItem('vx_reset_dev_otp')
    sessionStorage.removeItem('vx_reset_token')
    ElMessage.success(settings.t('auth.passwordResetDone'))
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <div class="auth-card-header">
      <h2>{{ settings.t('auth.resetPassword') }}</h2>
      <p>{{ settings.t('auth.resetPasswordSubtitle') }}</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
      <el-form-item prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password :placeholder="settings.t('auth.newPassword')" :prefix-icon="Lock" />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password :placeholder="settings.t('profile.confirmPassword')" :prefix-icon="Lock" />
      </el-form-item>
      <el-button type="primary" class="auth-submit" :loading="loading" @click="submit">
        {{ settings.t('auth.resetPassword') }}
      </el-button>
    </el-form>
  </div>
</template>

<style scoped>
.auth-card { width: 100%; max-width: 380px; }
.auth-card-header { margin-bottom: 28px; }
.auth-card-header h2 { font-size: var(--vx-font-size-4xl); color: var(--vx-text); margin-bottom: 8px; }
.auth-card-header p { color: var(--vx-text-secondary); line-height: 1.5; }
.auth-submit { width: 100%; height: 44px; border-radius: 8px; font-weight: 700; }
</style>
