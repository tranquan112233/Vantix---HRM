<template>
  <div class="auth-bg d-flex justify-content-center align-items-center">
    <div class="card auth-card border-0">
      <div class="card-body p-4">
        <h3 class="text-center text-primary fw-bold mb-3">
          {{ isLogin ? 'Đăng nhập' : 'Đăng ký' }}
        </h3>

        <p class="text-center text-muted mb-4">
          Chào mừng quay lại hệ thống
        </p>

        <form @submit.prevent>
          <div class="form-floating mb-3">
            <input
                type="email"
                class="form-control"
                id="email"
                placeholder="Email"
                v-model="form.email"
            />
            <label for="email">Email</label>
          </div>

          <div class="form-floating mb-3">
            <input
                type="password"
                class="form-control"
                id="password"
                placeholder="Password"
                v-model="form.password"
            />
            <label for="password">Mật khẩu</label>
          </div>

          <div class="form-floating mb-3" v-if="!isLogin">
            <input
                type="password"
                class="form-control"
                id="confirm"
                placeholder="Confirm"
                v-model="form.confirm"
            />
            <label for="confirm">Nhập lại mật khẩu</label>
          </div>

          <button class="btn btn-primary w-100 py-2 fw-semibold">
            {{ isLogin ? 'Login' : 'Register' }}
          </button>
        </form>

        <div class="text-center mt-4">
          <small class="text-muted">
            {{ isLogin ? 'Chưa có tài khoản?' : 'Đã có tài khoản?' }}
            <a href="#" @click.prevent="toggle" class="text-primary fw-semibold">
              {{ isLogin ? 'Đăng ký' : 'Đăng nhập' }}
            </a>
          </small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

const isLogin = ref(true)

const form = reactive({
  email: '',
  password: '',
  confirm: ''
})

const toggle = () => {
  isLogin.value = !isLogin.value
}
</script>

<style scoped>
.auth-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd, #ffffff);
}

.auth-card {
  width: 380px;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(13, 110, 253, 0.15);
}

.form-control {
  border-radius: 12px;
}

.btn-primary {
  border-radius: 12px;
}
</style>
