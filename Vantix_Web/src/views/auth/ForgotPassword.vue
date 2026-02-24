<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthService from '@/services/auth.service'

const router = useRouter()
const loading = ref(false)
const error = ref(null)
const info = ref(null)

const form = reactive({
  email: ''
})

const submit = async () => {
  error.value = null
  info.value = null
  loading.value = true
  try {
    await AuthService.forgotPassword({ email: form.email })
    info.value = 'Nếu email tồn tại, hệ thống đã gửi mã xác nhận.'
    router.push({ name: 'reset-password', query: { email: form.email } })
  } catch (e) {
    error.value = e.response?.data?.message || 'Gửi mã thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-5" style="max-width: 520px;">
    <h3 class="mb-3">Quên mật khẩu</h3>

    <div class="alert alert-info" v-if="info">{{ info }}</div>
    <div class="alert alert-danger" v-if="error">{{ error }}</div>

    <form @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">Email</label>
        <input v-model="form.email" type="email" class="form-control" required />
      </div>

      <button class="btn btn-primary w-100" :disabled="loading">
        {{ loading ? 'Đang gửi...' : 'Gửi mã xác nhận' }}
      </button>

      <div class="mt-3 text-center">
        <router-link to="/">Quay lại đăng nhập</router-link>
      </div>
    </form>
  </div>
</template>