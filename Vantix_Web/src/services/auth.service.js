import api from './axios.js'

export default {
    login(data) {
        return api.post('/auth/login', data)
    },

    logout() {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    },

    getUser() {
        return JSON.parse(localStorage.getItem('user'))
    }
}
