<template>
  <div class="w-75 m-auto">
    <div class="card border-0">
      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">Forgot Password</h4>
        <p class="text-muted small mb-4">
          Enter your email to receive OTP
        </p>

        <!-- ALERT -->
        <div v-if="errors.general" class="alert alert-danger py-2">
          {{ errors.general }}
        </div>

        <div v-if="success" class="alert alert-success py-2">
          {{ success }}
        </div>

        <form @submit.prevent="submit">

          <div class="mb-3">
            <label class="form-label fw-semibold">Email</label>

            <input
                v-model="email"
                type="email"
                class="form-control"
                :class="{ 'is-invalid': errors.email }"
            />

            <div class="invalid-feedback">
              {{ errors.email }}
            </div>
          </div>

          <button class="btn btn-primary w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Send OTP
          </button>

        </form>

        <div class="text-center mt-3">
          <router-link to="/auth/login">
            Back to login
          </router-link>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue"
import { useRouter } from "vue-router"
import AuthService from "@/services/auth.service"
import { useErrorHandler } from "@/composables/useErrorHandler"

const router = useRouter()
const { errors, handleError } = useErrorHandler()

const email = ref("")
const success = ref("")
const loading = ref(false)

/* Clear error when typing */
watch(email, () => errors.value = {})

const submit = async () => {
  loading.value = true
  success.value = ""
  errors.value = {}

  try {
    await AuthService.forgotPassword(email.value)

    success.value = "OTP sent to your email"

    setTimeout(() => {
      router.push({
        path: "/auth/verify-otp",
        query: { email: email.value }
      })
    }, 800)

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