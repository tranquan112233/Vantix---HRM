<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthService from '@/services/auth.service'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref(null)
const success = ref(null)

const form = reactive({
  email: route.query.email || '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const submit = async () => {
  error.value = null
  success.value = null

  if (form.newPassword !== form.confirmPassword) {
    error.value = 'Xác nhận mật khẩu không khớp'
    return
  }

  loading.value = true
  try {
    await AuthService.resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    })
    success.value = 'Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.'
    setTimeout(() => router.push('/'), 700)
  } catch (e) {
    error.value = e.response?.data?.message || 'Đặt lại mật khẩu thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-5" style="max-width: 520px;">
    <h3 class="mb-3">Đặt lại mật khẩu</h3>

    <div class="alert alert-success" v-if="success">{{ success }}</div>
    <div class="alert alert-danger" v-if="error">{{ error }}</div>

    <form @submit.prevent="submit">
      <div class="mb-3">
        <label class="form-label">Email</label>
        <input v-model="form.email" type="email" class="form-control" required />
      </div>

      <div class="mb-3">
        <label class="form-label">Mã 6 chữ số</label>
        <input v-model="form.code" type="text" inputmode="numeric" maxlength="6" class="form-control" required />
      </div>

      <div class="mb-3">
        <label class="form-label">Mật khẩu mới</label>
        <input v-model="form.newPassword" type="password" class="form-control" required minlength="6" />
      </div>

      <div class="mb-3">
        <label class="form-label">Nhập lại mật khẩu mới</label>
        <input v-model="form.confirmPassword" type="password" class="form-control" required minlength="6" />
      </div>

      <button class="btn btn-primary w-100" :disabled="loading">
        {{ loading ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
      </button>

      <div class="mt-3 text-center">
        <router-link to="/">Quay lại đăng nhập</router-link>
      </div>
    </form>
  </div>
</template>