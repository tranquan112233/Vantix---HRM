<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const settings = useSettingsStore()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  remember: true,
})

const serverErrors = reactive({
  username: '',
  password: '',
})

const rules = computed(() => ({
  username: [{ required: true, message: settings.t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: settings.t('login.passwordRequired'), trigger: 'blur' }],
}))

onMounted(() => {
  form.username = localStorage.getItem('vx_remember_username') || ''
})

function clearServerError(field) {
  serverErrors[field] = ''
}

function resetServerErrors() {
  serverErrors.username = ''
  serverErrors.password = ''
}

function applyLoginErrors(errors = {}) {
  serverErrors.username = (errors.username || errors.usernameOrEmail)
    ? settings.t('login.accountNotFound')
    : ''
  serverErrors.password = errors.password
    ? settings.t('login.passwordIncorrect')
    : ''
}

async function handleLogin() {
  resetServerErrors()
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (form.remember) {
      localStorage.setItem('vx_remember_username', form.username)
    } else {
      localStorage.removeItem('vx_remember_username')
    }
    await auth.login({ username: form.username, password: form.password }, form.remember)
    ElMessage.success(settings.t('login.success'))
  } catch (e) {
    const errors = e.response?.data?.errors
    if (errors && (errors.username || errors.usernameOrEmail || errors.password)) {
      applyLoginErrors(errors)
      return
    }

    ElMessage.error(e.response?.data?.message || settings.t('login.invalid'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <div class="auth-card-header">
      <h2>{{ settings.t('login.signIn') }}</h2>
      <p>{{ settings.t('login.signInSubtitle') }}</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
      <el-form-item prop="username" :error="serverErrors.username">
        <el-input
          v-model="form.username"
          :placeholder="settings.t('login.username')"
          :prefix-icon="User"
          @input="clearServerError('username')"
        />
      </el-form-item>

      <el-form-item prop="password" :error="serverErrors.password">
        <el-input
          v-model="form.password"
          type="password"
          :placeholder="settings.t('login.password')"
          :prefix-icon="Lock"
          show-password
          @input="clearServerError('password')"
        />
      </el-form-item>

      <div class="auth-options">
        <el-checkbox v-model="form.remember">{{ settings.t('auth.rememberMe') }}</el-checkbox>
        <router-link to="/forgot-password">{{ settings.t('auth.forgotPassword') }}</router-link>
      </div>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleLogin" class="auth-submit">
          {{ settings.t('login.signIn') }}
        </el-button>
      </el-form-item>
    </el-form>

    <p class="auth-hint">{{ settings.t('login.defaultAccount') }}</p>
  </div>
</template>

<style scoped>
.auth-card {
  width: 100%;
  max-width: 380px;
}

.auth-card-header {
  margin-bottom: 28px;
}

.auth-card-header h2 {
  font-size: var(--vx-font-size-4xl);
  color: var(--vx-text);
  margin-bottom: 8px;
}

.auth-card-header p,
.auth-hint {
  color: var(--vx-text-secondary);
  line-height: 1.5;
}

.auth-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: -4px 0 18px;
}

.auth-options a {
  color: var(--vx-primary);
  text-decoration: none;
  font-weight: 600;
  font-size: var(--vx-font-size-sm);
}

.auth-submit {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-weight: 700;
}

.auth-hint {
  text-align: center;
  font-size: var(--vx-font-size-xs);
  padding: 12px;
  background: var(--vx-bg);
  border-radius: 8px;
}
</style>
