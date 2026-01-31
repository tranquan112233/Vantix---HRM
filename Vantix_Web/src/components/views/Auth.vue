<template>
  <div class="auth-bg d-flex justify-content-center align-items-center">
    <div class="card auth-card border-0">
      <div class="card-body">
        <!-- Title -->
        <h3 class="text-center title mb-1">
          {{ isLogin ? 'Chào mừng trở lại 👋' : 'Tạo tài khoản mới ✨' }}
        </h3>

        <p class="text-center subtitle mb-4">
          {{ isLogin
            ? 'Rất vui khi được gặp lại bạn'
            : 'Chỉ mất một phút để bắt đầu' }}
        </p>

        <!-- Form -->
        <form @submit.prevent>
          <div class="form-floating mb-3">
            <input
                type="email"
                class="form-control"
                id="email"
                placeholder="Email"
                v-model="form.email"
                autofocus
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

          <button class="btn btn-primary w-100 py-2">
            {{ isLogin ? 'Đăng nhập' : 'Tạo tài khoản' }}
          </button>
        </form>

        <!-- Trust text -->
        <p class="text-center trust-text mt-3">
          🔒 Thông tin của bạn luôn được bảo mật
        </p>

        <!-- Switch -->
        <div class="text-center mt-3">
          <small class="text-muted">
            {{ isLogin ? 'Chưa có tài khoản?' : 'Đã có tài khoản?' }}
            <a href="#" @click.prevent="toggle" class="link-primary">
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
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* CARD */
.auth-card {
  width: 420px;
  border-radius: 14px;
  background: #ffffff;
  box-shadow:
      0 18px 40px rgba(13, 110, 253, 0.18),
      0 4px 10px rgba(0, 0, 0, 0.05);
}

/* CONTENT */
.card-body {
  padding: 36px;
}

/* TITLE */
.title {
  color: #0d6efd;
  font-weight: 600;
  font-size: 22px;
}

.subtitle {
  color: #6c757d;
  font-size: 14px;
}

/* INPUT */
.form-control {
  background: #f8f9fa;
  border: 1px solid #ced4da;
  color: #212529;
  border-radius: 10px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-control:focus {
  border-color: #0d6efd;
  background: #ffffff;
  box-shadow: 0 0 0 0.15rem rgba(13, 110, 253, 0.15);
}

/* LABEL */
.form-floating label {
  color: #6c757d;
}

/* BUTTON */
.btn-primary {
  background: #0d6efd;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  transition: background 0.2s ease, transform 0.1s ease;
}

.btn-primary:hover {
  background: #0b5ed7;
  transform: translateY(-1px);
}

/* TRUST TEXT */
.trust-text {
  font-size: 13px;
  color: #6c757d;
}

/* LINK */
.link-primary {
  color: #0d6efd;
  text-decoration: none;
  font-weight: 500;
}

.link-primary:hover {
  text-decoration: underline;
}

</style>
