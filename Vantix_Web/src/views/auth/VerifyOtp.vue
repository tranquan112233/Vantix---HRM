<template>

  <div class="w-75 m-auto">

    <div class="card border-0">

      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">
          Verify OTP
        </h4>

        <p class="text-muted small mb-4">
          Enter OTP sent to email
        </p>


        <div class="alert alert-danger py-2"
             v-if="errors.general">
          {{ errors.general }}
        </div>


        <form @submit.prevent="submit">

          <div class="mb-3">

            <label class="form-label fw-semibold">
              OTP
            </label>

            <input
                v-model="otp"
                class="form-control"
            >

          </div>


          <button
              class="btn btn-primary w-100"
              :disabled="loading"
          >

            Verify

          </button>

        </form>

      </div>

    </div>

  </div>

</template>


<script setup>

import { ref } from "vue"
import { useRouter, useRoute } from "vue-router"
import AuthService from "@/services/auth.service.js"

const router = useRouter()
const route = useRoute()

const email =
    route.query.email

const otp = ref("")
const errors = ref({})
const loading = ref(false)


const submit = async () => {

  loading.value = true
  errors.value = {}

  try {

    const res =
        await AuthService.verifyOtp(
            email,
            otp.value
        )

    const resetToken =
        res.data

    router.push({
      path: "/auth/reset-password",
      query: { token: resetToken }
    })

  }
  catch (err) {

    const data = err.response?.data

    if (!data) {

      errors.value.general = "Server error"
      return

    }

    // lỗi validation
    if (data.validationErrors) {

      errors.value = { ...data.validationErrors }

    }
    else {

      errors.value.general = data.message

    }

  }
  finally {

    loading.value = false

  }

}

</script>

<style scoped>

.form-control {

  height: 45px;

}


.btn {

  height: 45px;

  font-weight: 600;

}

</style>