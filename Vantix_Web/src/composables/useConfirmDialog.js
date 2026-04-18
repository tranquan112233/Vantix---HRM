import { reactive, readonly } from 'vue'

const defaultState = {
  open: false,
  title: 'Xác nhận thao tác',
  message: 'Bạn có chắc chắn muốn tiếp tục?',
  confirmText: 'Xác nhận',
  cancelText: 'Hủy',
  variant: 'primary',
  icon: 'bi bi-question-circle'
}

const state = reactive({ ...defaultState })

let resolver = null

const resetState = () => {
  Object.assign(state, defaultState)
}

export const useConfirmDialogState = () => readonly(state)

export const confirmDialog = (options = {}) => {
  if (resolver) {
    resolver(false)
    resolver = null
  }

  Object.assign(state, defaultState, options, { open: true })

  return new Promise(resolve => {
    resolver = resolve
  })
}

export const resolveConfirmDialog = (confirmed) => {
  const currentResolver = resolver
  resolver = null
  resetState()
  currentResolver?.(confirmed)
}

export const closeConfirmDialog = () => resolveConfirmDialog(false)
