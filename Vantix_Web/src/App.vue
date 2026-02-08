<template>
  <router-view />

  <!-- GLOBAL TOAST -->
  <div class="toast-container position-fixed top-0 start-50 translate-middle-x p-3">
    <div
        ref="toastRef"
        class="toast text-bg-success border-0 shadow"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
    >
      <div class="d-flex align-items-center">
        <!-- ICON -->
        <div class="me-0 ms-3">
          <i :class="icon" class="text-white fs-4"></i>
        </div>

        <!-- MESSAGE -->
        <div class="toast-body flex-grow-1">
          {{ message }}
        </div>

        <!-- CLOSE -->
        <button
            type="button"
            class="btn-close btn-close-white me-2"
            data-bs-dismiss="toast"
        ></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useToast } from '@/composables/useToast'

const { toastRef, message, icon } = useToast()
</script>

