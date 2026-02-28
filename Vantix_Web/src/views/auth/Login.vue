<template>

  <div class="w-75 m-auto">

    <div class="card border-0">

      <div class="card-body p-4">

        <!-- TITLE -->
        <h4 class="fw-bold mb-1">
          Sign in
        </h4>

        <p class="text-muted small mb-4">
          Enter your email and password to sign in
        </p>


        <!-- GENERAL ERROR -->
        <div
            class="alert alert-danger py-2"
            v-if="errors.general"
        >
          {{ errors.general }}
        </div>


        <form @submit.prevent="login">

          <!-- EMAIL -->
          <div class="mb-3">

            <label class="form-label fw-semibold">

              Email
              <span class="text-danger">*</span>

            </label>

            <input
                v-model="email"
                type="email"
                class="form-control"
                :class="{ 'is-invalid': errors.email }"
                placeholder="info@gmail.com"
            >

            <div class="invalid-feedback">
              {{ errors.email }}
            </div>

          </div>


          <!-- PASSWORD -->
          <div class="mb-3">

            <label class="form-label fw-semibold">

              Password
              <span class="text-danger">*</span>

            </label>

            <input
                v-model="password"
                type="password"
                class="form-control"
                :class="{ 'is-invalid': errors.password }"
                placeholder="Enter your password"
            >

            <div class="invalid-feedback">
              {{ errors.password }}
            </div>

          </div>


          <!-- REMEMBER + FORGOT -->
          <div class="d-flex justify-content-between mb-3">

            <div class="form-check">

              <input
                  v-model="rememberMe"
                  type="checkbox"
                  class="form-check-input"
                  id="rememberMe"
              >

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


          <!-- BUTTON -->
          <button
              class="btn btn-primary w-100"
              :disabled="loading"
          >

            <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
            ></span>

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
import AuthService from "@/services/auth.service.js"

const router = useRouter()


const email = ref("")
const password = ref("")
const rememberMe = ref(false)

const errors = ref({})
const loading = ref(false)


/* Load saved email */
onMounted(() => {

  const savedEmail =
      localStorage.getItem("remember_email")

  if (savedEmail) {

    email.value = savedEmail
    rememberMe.value = true

  }

})


watch(email, () => {

  delete errors.value.email
  delete errors.value.general

})

watch(password, () => {

  delete errors.value.password
  delete errors.value.general

})


const login = async () => {

  errors.value = {}
  loading.value = true

  try {

    const res =
        await AuthService.login(
            email.value,
            password.value
        )

    const token =
        res.data.token

    AuthService.saveToken(token)


    if (rememberMe.value) {

      localStorage.setItem(
          "remember_email",
          email.value
      )

    }
    else {

      localStorage.removeItem(
          "remember_email"
      )

    }

    router.push("/home")

  }
  catch (err) {

    const data =
        err.response?.data

    if (!data) {

      errors.value.general =
          "Server error"

      return

    }

    if (data.validationErrors) {

      errors.value =
          { ...data.validationErrors }

    }
    else {

      errors.value.general =
          data.message

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