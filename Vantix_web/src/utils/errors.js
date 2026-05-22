import { ElMessage } from 'element-plus'

function firstValidationError(errors) {
  if (!errors || typeof errors !== 'object') return ''

  const first = Object.values(errors)[0]
  if (Array.isArray(first)) return first[0] || ''
  return first || ''
}

export function apiErrorMessage(error, settings, fallbackKey = 'common.somethingWrong') {
  const data = error?.response?.data
  const validationMessage = firstValidationError(data?.errors)
  const message = validationMessage || data?.message || data?.error

  if (message) return message
  return settings?.t ? settings.t(fallbackKey) : 'Something went wrong'
}

export function showApiError(error, settings, fallbackKey = 'common.somethingWrong') {
  if (error?.vxHandled) return
  ElMessage.error(apiErrorMessage(error, settings, fallbackKey))
}

export function isCancelError(error) {
  return error === 'cancel' || error === 'close'
}
