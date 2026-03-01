<template>
  <div class="w-75 m-auto">
    <div class="card border-0">
      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">Verify OTP</h4>
        <p class="text-muted small mb-4">
          Enter OTP sent to email
        </p>

        <div v-if="errors.general" class="alert alert-danger py-2">
          {{ errors.general }}
        </div>

        <form @submit.prevent="submit">

          <div class="mb-3">
            <label class="form-label fw-semibold">OTP</label>

            <input
                v-model="otp"
                class="form-control"
                :class="{ 'is-invalid': errors.otp }"
            />

            <div class="invalid-feedback">
              {{ errors.otp }}
            </div>
          </div>

          <button class="btn btn-primary w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Verify
          </button>

        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from "vue"
import { useRouter, useRoute } from "vue-router"
import AuthService from "@/services/auth.service"
import { useErrorHandler } from "@/composables/useErrorHandler"

const router = useRouter()
const route = useRoute()
const { errors, handleError } = useErrorHandler()

const email = route.query.email
const otp = ref("")
const loading = ref(false)

/* Nếu không có email -> quay lại forgot */
if (!email) router.replace("/auth/forgot-password")

/* Clear error khi nhập */
watchEffect(() => otp.value && (errors.value = {}))

const submit = async () => {
  loading.value = true
  errors.value = {}

  try {
    const { data } = await AuthService.verifyOtp(email, otp.value)

    router.push({
      path: "/auth/reset-password",
      query: { token: data }
    })

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