<template>

  <div class="w-75 m-auto">

    <div class="card border-0">

      <div class="card-body p-4">

        <h4 class="fw-bold mb-1">
          Reset Password
        </h4>


        <!-- GENERAL ERROR -->
        <div
            class="alert alert-danger py-2"
            v-if="errors.general"
        >
          {{ errors.general }}
        </div>


        <!-- SUCCESS -->
        <div
            class="alert alert-success py-2"
            v-if="success"
        >
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
            >

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
            >

            <div class="invalid-feedback">
              {{ errors.confirmPassword }}
            </div>

          </div>


          <!-- BUTTON -->
          <button
              class="btn btn-primary w-100"
              :disabled="loading"
          >

            <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
            ></span>

            {{ loading ? "Resetting..." : "Reset Password" }}

          </button>

        </form>

      </div>

    </div>

  </div>

</template>


<script setup>

import { ref, watch, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import AuthService from "@/services/auth.service.js"

const route = useRoute()
const router = useRouter()

const token = route.query.token

const password = ref("")
const confirmPassword = ref("")

const errors = ref({})
const success = ref("")
const loading = ref(false)


/* redirect nếu không có token */
onMounted(() => {

  if (!token) {

    router.push("/auth/login")

  }

})


/* clear error khi nhập */
watch(password, () => {

  delete errors.value.newPassword
  delete errors.value.general

})

watch(confirmPassword, () => {

  delete errors.value.confirmPassword
  delete errors.value.general

})


/* submit */
const submit = async () => {

  errors.value = {}
  success.value = ""
  loading.value = true

  try {

    await AuthService.resetPassword(

        token,
        password.value,
        confirmPassword.value

    )

    success.value =
        "Password reset successfully"


    setTimeout(() => {

      router.push("/auth/login")

    }, 1500)

  }
  catch (err) {

    const data = err.response?.data


    if (!data) {

      errors.value.general = "Server error"
      return

    }


    /* validation error từ backend */
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