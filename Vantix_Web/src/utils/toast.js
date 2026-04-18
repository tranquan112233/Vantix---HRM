import { useToast as useToastification } from 'vue-toastification'

const fallbackErrorMessage = 'Đã có lỗi xảy ra. Vui lòng thử lại.'

const normalizeMessage = (value, fallback = fallbackErrorMessage) => {
  if (!value) return fallback
  if (typeof value === 'string') return value
  if (Array.isArray(value)) {
    return value.map(item => normalizeMessage(item, '')).filter(Boolean).join(', ') || fallback
  }
  if (typeof value === 'object') {
    if (typeof value.message === 'string' && value.message.trim()) return value.message
    if (typeof value.error === 'string' && value.error.trim()) return value.error
    if (typeof value.detail === 'string' && value.detail.trim()) return value.detail

    if (value.errors && typeof value.errors === 'object') {
      const firstFieldError = Object.values(value.errors)
        .flat()
        .find(item => typeof item === 'string' && item.trim())

      if (firstFieldError) return firstFieldError
    }
  }

  return fallback
}

export const getErrorMessage = (error, fallback = fallbackErrorMessage) => {
  if (!error) return fallback
  if (typeof error === 'string') return error

  return normalizeMessage(
    error.response?.data ??
      error.data ??
      error.message ??
      error,
    fallback
  )
}

export const useToast = () => {
  const toast = useToastification()

  return {
    success(message, options) {
      return toast.success(normalizeMessage(message, 'Thao tác thành công.'), options)
    },
    error(messageOrError, fallbackOrOptions, options) {
      if (
        fallbackOrOptions &&
        typeof fallbackOrOptions === 'object' &&
        !Array.isArray(fallbackOrOptions)
      ) {
        return toast.error(getErrorMessage(messageOrError), fallbackOrOptions)
      }

      return toast.error(
        getErrorMessage(messageOrError, fallbackOrOptions),
        options
      )
    },
    warning(message, options) {
      return toast.warning(normalizeMessage(message, 'Vui lòng kiểm tra lại thông tin.'), options)
    },
    info(message, options) {
      return toast.info(normalizeMessage(message, 'Thông tin mới đã được cập nhật.'), options)
    },
    dismiss(id) {
      if (typeof id === 'undefined') {
        toast.clear()
        return
      }

      toast.dismiss(id)
    }
  }
}

export default useToast
