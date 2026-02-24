<script setup>
import { reactive, ref } from 'vue'
import UserService from '@/services/user.service.js'

const error = ref(null)
const success = ref(null)
const loading = ref(false)

const form = reactive({
  currentPassword: '',
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
    await UserService.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })

    success.value = 'Đổi mật khẩu thành công'
    form.currentPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (e) {
    error.value = e?.response?.data?.message || 'Đổi mật khẩu thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-4" style="max-width: 720px;">
    <h3 class="mb-3">Đổi mật khẩu</h3>

    <div class="card">
      <div class="card-body">
        <div v-if="success" class="alert alert-success">{{ success }}</div>
        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <form @submit.prevent="submit">
          <div class="mb-3">
            <label class="form-label">Mật khẩu hiện tại</label>
            <input v-model="form.currentPassword" type="password" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Mật khẩu mới</label>
            <input v-model="form.newPassword" type="password" class="form-control" minlength="6" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Nhập lại mật khẩu mới</label>
            <input v-model="form.confirmPassword" type="password" class="form-control" minlength="6" required />
          </div>

          <button class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Đang cập nhật...' : 'Cập nhật' }}
          </button>

        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* dùng bootstrap là đủ */
</style>