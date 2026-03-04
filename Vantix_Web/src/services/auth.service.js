import api from './axios.js'

export default {
    login(data) {
        return api.post('/auth/login', data)
    },

    forgotPassword(data) {
        return api.post('/auth/forgot-password', data)
    },

    resetPassword(data) {
        return api.post('/auth/reset-password', data)
    },

    logout() {
        // Xóa sạch mọi thứ liên quan đến phiên đăng nhập cũ
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('employeeId') // Thêm dòng này để dọn dẹp profile
    },

    getUser() {
        return JSON.parse(localStorage.getItem('user'))
    }

}
