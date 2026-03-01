<template>
  <div class="w-75 m-auto">
    <div class="card border-0">
      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">Sign in</h4>
        <p class="text-muted small mb-4">
          Enter your email and password to sign in
        </p>

        <!-- GENERAL ERROR -->
        <div v-if="errors.general" class="alert alert-danger py-2">
          {{ errors.general }}
        </div>

        <form @submit.prevent="login">

          <!-- EMAIL -->
          <div class="mb-3">
            <label class="form-label fw-semibold">
              Email <span class="text-danger">*</span>
            </label>

            <input
                v-model="email"
                type="email"
                class="form-control"
                :class="{ 'is-invalid': errors.email }"
                placeholder="info@gmail.com"
            />

            <div class="invalid-feedback">
              {{ errors.email }}
            </div>
          </div>

          <!-- PASSWORD -->
          <div class="mb-3">
            <label class="form-label fw-semibold">
              Password <span class="text-danger">*</span>
            </label>

            <input
                v-model="password"
                type="password"
                class="form-control"
                :class="{ 'is-invalid': errors.password }"
                placeholder="Enter your password"
            />

            <div class="invalid-feedback">
              {{ errors.password }}
            </div>
          </div>

          <div class="d-flex justify-content-between mb-3">
            <div class="form-check">
              <input
                  v-model="rememberMe"
                  type="checkbox"
                  class="form-check-input"
              />
              <label class="form-check-label small">
                Remember me
              </label>
            </div>

            <router-link
                to="/auth/forgot-password"
                class="small text-decoration-none"
            >
              Forgot password?
            </router-link>
          </div>

          <button class="btn btn-primary w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? "Signing in..." : "Sign in" }}
          </button>

        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue"
import { useRouter } from "vue-router"
import AuthService from "@/services/auth.service"
import { useErrorHandler } from "@/composables/useErrorHandler"

const router = useRouter()
const { errors, handleError } = useErrorHandler()

const email = ref("")
const password = ref("")
const rememberMe = ref(false)
const loading = ref(false)

/* Load remember email */
onMounted(() => {
  email.value = localStorage.getItem("remember_email") || ""
  rememberMe.value = !!email.value
})

/* Clear field error */
watch([email, password], () => errors.value = {})

/* Login */
const login = async () => {
  loading.value = true
  errors.value = {}

  try {
    const { data } = await AuthService.login(email.value, password.value)
    AuthService.saveToken(data.token)

    rememberMe.value
        ? localStorage.setItem("remember_email", email.value)
        : localStorage.removeItem("remember_email")

    router.push("/home")
  } catch (err) {
    handleError(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-control,
.btn {
  height: 45px;
}
.btn {
  font-weight: 600;
}
</style>