import axios  from 'axios'
import router from '@/router'

const api = axios.create({ baseURL: '/api' })

// ── Request: gắn Bearer token ─────────────────────────────────────────────
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

// ── Response: xử lý lỗi tập trung ────────────────────────────────────────
api.interceptors.response.use(
    res => res,

    err => {
        // Mất kết nối server
        if (!err.response) {
            return Promise.reject({ message: 'Không thể kết nối đến server' })
        }

        const { status, data } = err.response

        // Token hết hạn → về login
        // Ngoại lệ: /auth/** không redirect (đang login/reset password thì 401 là lỗi nghiệp vụ bình thường)
        if (status === 401 && !err.config?.url?.includes('/auth/')) {
            localStorage.removeItem('token')
            router.push('/auth/login')
            return Promise.reject({ message: 'Phiên đăng nhập đã hết hạn' })
        }

        // Ném ra object lỗi chuẩn để component xử lý
        // data có dạng: { message, errors: { field: msg } }
        return Promise.reject(data ?? { message: 'Có lỗi xảy ra' })
    }
)

export default api