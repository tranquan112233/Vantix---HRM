<template>
  <div class="login-bg">
    <img
        :src="logo"
        alt="Vantix HR"
        class="app-logo"
    />


    <div class="auth-card">

      <!-- LEFT -->
      <div class="auth-left">
        <h3 class="title">
          {{ isLogin ? 'Chào mừng trở lại 👋' : '' }}
        </h3>

        <p class="subtitle">
          {{ isLogin
            ? 'Rất vui khi được gặp lại bạn'
            : 'Chỉ mất một phút để bắt đầu' }}
        </p>

        <form @submit.prevent>
          <div class="form-floating mb-3">
            <input
                type="username"
                class="form-control"
                placeholder="Username"
                v-model="form.username"
                required
            />
            <label>Tên tài khoản</label>
          </div>

          <div class="form-floating mb-3">
            <input
                type="password"
                class="form-control"
                placeholder="Password"
                v-model="form.password"
                required
            />
            <label>Mật khẩu</label>
          </div>


          <button class="btn-login" @click="login">
            {{ isLogin ? 'Đăng nhập' : '' }}
          </button>
        </form>

        <p class="trust-text">
          🔒 Thông tin của bạn luôn được bảo mật
        </p>

      </div>

      <!-- RIGHT -->
      <div class="auth-right" v-if="isLogin">
        <img
            :src="logo"
            alt="QR"
        />
        <h4>Chào mừng bạn đến VANTIX</h4>
        <p>
          Nền tảng quản lý nhân sự hiện đại
        </p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import logo from '../../assets/img/removeBackgroundLogo.png'
import router from "../../router/index.js";


const isLogin = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const toggle = () => {
  isLogin.value = !isLogin.value
}

const login = async () => {
  try {
    const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(form)
        }
    )

    if (!response.ok) {
      alert("Sai tài khoản hoặc mật khẩu")
      return
    }

    const user = await response.json()

    localStorage.setItem("user", JSON.stringify(user))

    router.push({ name: 'home' })

  } catch (e) {
    alert("Không kết nối được server")
  }
}
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  background-image: url("https://i.pinimg.com/1200x/76/a1/d5/76a1d5df2961390d07e44e640108addc.jpg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.app-logo {
  position: absolute;
  top: 0;
  left: 0;

  width: 120px;
  height: auto;

  z-index: 999;
}

.login-bg::before {
  content: "";
  position: absolute;
  inset: 0;
  background: rgba(6, 12, 36, 0.65);
}

/* CARD (Discord style) */
.auth-card {
  position: relative;
  z-index: 1;
  width: 960px;
  max-width: 96%;
  display: flex;
  border-radius: 22px;
  overflow: hidden;
  background: rgba(16, 24, 52, 0.97);
  backdrop-filter: blur(14px);
  box-shadow:
      0 35px 80px rgba(0, 0, 0, 0.8),
      0 0 0 1px rgba(30, 136, 229, 0.18);
  color: #e3f2fd;
}

/* LEFT */
.auth-left {
  flex: 1;
  padding: 56px 64px;
}

.title {
  color: #1e88e5;
  font-size: 26px;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 32px;
  color: #b6ccff;
  font-size: 15px;
}

/* INPUT */
.form-floating .form-control {
  height: 56px;
  background: #0b132b;
  border: 1px solid #243b80;
  border-radius: 12px;
  color: #e3f2fd;
}

.form-floating .form-control:focus {
  background: #0b132b;
  border-color: #1e88e5;
  box-shadow: 0 0 0 0.18rem rgba(30, 136, 229, 0.35);
}

.form-floating label {
  color: #90caf9;
}

/* BUTTON */
.btn-login {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #1e88e5, #1565c0);
}

.btn-login:hover {
  background: linear-gradient(135deg, #1565c0, #0d47a1);
}

/* TEXT */
.trust-text {
  margin-top: 18px;
  font-size: 13px;
  color: #90caf9;
}

.switch {
  margin-top: 16px;
  font-size: 14px;
  color: #9bbcff;
}

.switch a {
  color: #1e88e5;
  font-weight: 500;
  text-decoration: none;
}

.switch a:hover {
  text-decoration: underline;
}

/* RIGHT (QR) */
.auth-right {
  width: 340px;
  padding: 56px 40px;
  text-align: center;
  background: linear-gradient(
      180deg,
      rgba(6, 12, 36, 0.95),
      rgba(2, 6, 23, 0.95)
  );
  border-left: 1px solid rgba(30, 136, 229, 0.25);
}

.auth-right img {
  width: 180px;
  border-radius: 14px;
  margin-bottom: 20px;
  background: #fff;
  padding: 10px;
}

.auth-right h4 {
  color: #e3f2fd;
  margin-bottom: 8px;
}

.auth-right p {
  font-size: 14px;
  color: #b6ccff;
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .auth-card {
    flex-direction: column;
  }

  .auth-right {
    display: none;
  }

  .auth-left {
    padding: 40px 28px;
  }
}
</style>
