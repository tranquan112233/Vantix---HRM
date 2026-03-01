import { ref, computed } from 'vue'

const toastRef = ref(null)
const message = ref('')
const type = ref('success')
let toastInstance = null

export function useToast() {

    const iconMap = {
        success: 'bi bi-check-circle-fill',
        danger: 'bi bi-x-circle-fill',
        warning: 'bi bi-exclamation-triangle-fill',
        info: 'bi bi-info-circle-fill'
    }

    const icon = computed(() => iconMap[type.value] || iconMap.success)

    const showToast = (msg, t = 'success', delay = 3000) => {
        message.value = msg
        type.value = t

        const el = toastRef.value
        if (!el) return

        el.classList.remove(
            'text-bg-success',
            'text-bg-danger',
            'text-bg-warning',
            'text-bg-info'
        )
        el.classList.add(`text-bg-${t}`)

        if (!toastInstance) {
            toastInstance = new window.bootstrap.Toast(el, {
                delay,
                autohide: true
            })
        }

        toastInstance.show()
    }

    return {
        toastRef,
        message,
        icon,
        showToast
    }
}
