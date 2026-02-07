<template>
  <div class="login">
    <h3>Đăng nhập</h3>

    <input v-model="form.username" placeholder="Username" />
    <input v-model="form.password" type="password" placeholder="Password" />

    <button @click="login">Login</button>

    <p v-if="error" style="color:red">{{ error }}</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthService from '@/assets/services/auth.service'

const router = useRouter()   // ← CỰC KỲ QUAN TRỌNG
const error = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const login = async () => {
  try {
    const res = await AuthService.login(form)

    localStorage.setItem('token', res.data.token)
    localStorage.setItem(
        'user',
        JSON.stringify({
          username: res.data.username,
          role: res.data.role
        })
    )

    // redirect theo role
    if (res.data.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/home')
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Login thất bại'
  }
}
</script>
