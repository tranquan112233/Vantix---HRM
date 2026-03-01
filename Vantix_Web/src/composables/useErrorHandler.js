import { ref } from "vue"

export function useErrorHandler() {

    // reactive object lưu lỗi để bind lên UI
    const errors = ref({})

    const handleError = (err) => {

        // reset lỗi cũ trước khi xử lý lỗi mới
        errors.value = {}

        // optional chaining tránh crash nếu err hoặc response = undefined
        const data = err?.response?.data

        // xử lý lỗi network hoặc server không trả response
        if (!data) {
            // dùng ?. để tránh crash nếu err = null/undefined
            errors.value.general = err?.message || "Cannot connect to server"
            return
        }

        // chỉ spread khi validationErrors là object hợp lệ
        if (data.validationErrors && typeof data.validationErrors === "object") {
            errors.value = { ...data.validationErrors }
            return
        }

        // fallback message nếu backend không trả message chuẩn
        errors.value.general =
            data?.message ||
            data?.error ||
            "Unexpected error occurred"
    }

    return {
        errors,
        handleError
    }
}