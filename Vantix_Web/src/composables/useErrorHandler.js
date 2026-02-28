import { ref } from "vue"

export function useErrorHandler() {

    const errors = ref({})

    const handleError = (err) => {

        errors.value = {}

        const data = err.response?.data

        if (!data) {
            errors.value.general = "Server error"
            return
        }

        if (data.validationErrors) {
            errors.value = { ...data.validationErrors }
        } else {
            errors.value.general = data.message
        }
    }

    return {
        errors,
        handleError
    }
}