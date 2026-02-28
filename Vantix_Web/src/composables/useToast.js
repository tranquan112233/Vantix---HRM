import { ref } from "vue"

let id = 0
const toasts = ref([])

export function useToast() {

    function showToast(message, type = "success", duration = 3000) {

        const toastId = id++

        const toast = {
            id: toastId,
            message,
            type,
            duration
        }

        toasts.value.push(toast)

        setTimeout(() => {
            removeToast(toastId)
        }, duration)
    }

    function removeToast(toastId) {
        toasts.value = toasts.value.filter(t => t.id !== toastId)
    }

    return {
        toasts,
        showToast,
        removeToast
    }
}