import api from './axios.js'


export default {
    getAll() {
        return api.get('/users')
    },


    getById(id) {
        return api.get(`/users/${id}`)
    },


    create(data) {
        return api.post('/users', data)
    },


    update(id, data) {
        return api.put(`/users/${id}`, data)
    },


    toggleLock(id) {
        return api.put(`/users/${id}/toggle-lock`)
    },


    delete(id) {
        return api.delete(`/users/${id}`)
    }
}