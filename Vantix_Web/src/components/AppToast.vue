<template>
  <div class="toast-wrapper">
    <transition-group name="toast-fade" tag="div">

      <div
          v-for="toast in toasts"
          :key="toast.id"
          class="mini-toast"
      >

        <!-- ICON -->
        <i
            :class="toast.type === 'success'
            ? 'bi bi-check-circle-fill text-success'
            : 'bi bi-x-circle-fill text-danger'"
            class="toast-icon"
        ></i>

        <!-- MESSAGE -->
        <span class="toast-message">
          {{ toast.message }}
        </span>

        <!-- CLOSE -->
        <button
            class="btn-close btn-close-sm"
            @click="removeToast(toast.id)"
        ></button>

        <!-- PROGRESS (CSS only) -->
        <div
            class="toast-progress"
            :class="toast.type === 'success'
            ? 'bg-success'
            : 'bg-danger'"
            :style="{ animationDuration: toast.duration + 'ms' }"
        ></div>

      </div>

    </transition-group>
  </div>
</template>

<script setup>
import { useToast } from "@/composables/useToast"
const { toasts, removeToast } = useToast()
</script>

<style scoped>

.toast-wrapper {
  position: fixed;
  top: 15px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mini-toast {
  position: relative;
  background: #fff;
  min-width: 260px;
  max-width: 320px;
  padding: 10px 14px;
  padding-bottom: 12px;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.12);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.toast-icon {
  font-size: 18px;
}

.toast-message {
  flex: 1;
  font-weight: 500;
  color: #333;
}

/* PROGRESS - smooth */
.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  width: 100%;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  animation-name: shrink;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}

@keyframes shrink {
  from { width: 100%; }
  to { width: 0%; }
}

/* ENTER */
.toast-fade-enter-active {
  animation: slideDown 0.25s ease;
}

/* LEAVE */
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

</style>