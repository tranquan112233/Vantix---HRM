<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="dialog.open"
        class="modal-overlay"
        @click.self="handleCancel"
      >
        <div class="modal-container confirm-dialog-container">
          <div class="modal-custom confirm-dialog-card">
            <div class="modal-header-custom">
              <div class="confirm-header">
                <div class="confirm-icon" :class="`is-${dialog.variant}`">
                  <i :class="dialog.icon"></i>
                </div>
                <div>
                  <h3 class="modal-title">{{ dialog.title }}</h3>
                  <p class="modal-subtitle">{{ dialog.message }}</p>
                </div>
              </div>
              <button class="modal-close-btn" type="button" @click="handleCancel">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="modal-footer-custom">
              <button type="button" class="btn-ghost" @click="handleCancel">
                {{ dialog.cancelText }}
              </button>
              <button
                type="button"
                :class="confirmButtonClass"
                @click="handleConfirm"
              >
                {{ dialog.confirmText }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'
import {
  closeConfirmDialog,
  resolveConfirmDialog,
  useConfirmDialogState
} from '@/composables/useConfirmDialog'

const dialog = useConfirmDialogState()

const confirmButtonClass = computed(() => {
  if (dialog.variant === 'danger') return 'btn-danger'
  if (dialog.variant === 'success') return 'btn-success'
  return 'btn-primary'
})

const handleConfirm = () => resolveConfirmDialog(true)
const handleCancel = () => closeConfirmDialog()

const handleKeydown = event => {
  if (event.key === 'Escape' && dialog.open) {
    handleCancel()
  }
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))
</script>

<style scoped>
.confirm-dialog-container {
  width: min(95vw, 520px);
}

.confirm-dialog-card {
  overflow: hidden;
}

.confirm-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.confirm-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
  font-size: 18px;
}

.confirm-icon.is-primary {
  background: #eef4ff;
  color: #465fff;
}

.confirm-icon.is-success {
  background: #ecfdf3;
  color: #027a48;
}

.confirm-icon.is-danger {
  background: #fef3f2;
  color: #b42318;
}
</style>
