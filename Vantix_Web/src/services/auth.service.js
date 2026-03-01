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
        localStorage.removeItem('token')
    },

    getUser() {
        return JSON.parse(localStorage.getItem('user'))
    }
}
