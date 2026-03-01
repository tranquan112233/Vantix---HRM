<template>
  <div class="w-75 m-auto">
    <div class="card border-0">
      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">Reset Password</h4>

        <div v-if="errors.general" class="alert alert-danger py-2">
          {{ errors.general }}
        </div>

        <div v-if="success" class="alert alert-success py-2">
          {{ success }}
        </div>

        <form @submit.prevent="submit">

          <!-- NEW PASSWORD -->
          <div class="mb-3">
            <label class="form-label fw-semibold">
              New Password
            </label>

            <input
                type="password"
                v-model="password"
                class="form-control"
                :class="{ 'is-invalid': errors.newPassword }"
                placeholder="Enter new password"
            />

            <div class="invalid-feedback">
              {{ errors.newPassword }}
            </div>
          </div>

          <!-- CONFIRM PASSWORD -->
          <div class="mb-3">
            <label class="form-label fw-semibold">
              Confirm Password
            </label>

            <input
                type="password"
                v-model="confirmPassword"
                class="form-control"
                :class="{ 'is-invalid': errors.confirmPassword }"
                placeholder="Confirm new password"
            />

            <div class="invalid-feedback">
              {{ errors.confirmPassword }}
            </div>
          </div>

          <button class="btn btn-primary w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            {{ loading ? "Resetting..." : "Reset Password" }}
          </button>

        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from "vue"
import { useRoute, useRouter } from "vue-router"
import AuthService from "@/services/auth.service"
import { useErrorHandler } from "@/composables/useErrorHandler"

const route = useRoute()
const router = useRouter()
const { errors, handleError } = useErrorHandler()

const token = route.query.token
const password = ref("")
const confirmPassword = ref("")
const success = ref("")
const loading = ref(false)

/* Nếu không có token -> về login */
if (!token) router.replace("/auth/login")

/* Clear error khi nhập */
watchEffect(() => {
  if (password.value || confirmPassword.value) errors.value = {}
})

const submit = async () => {
  loading.value = true
  success.value = ""
  errors.value = {}

  try {
    await AuthService.resetPassword(
        token,
        password.value,
        confirmPassword.value
    )

    success.value = "Password reset successfully"

    setTimeout(() => router.push("/auth/login"), 1000)

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