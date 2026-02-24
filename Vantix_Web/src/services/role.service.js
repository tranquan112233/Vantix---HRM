import api from './axios.js'

export default {
    getAll() {
        return api.get('/roles')
    },

    getById(id) {
        return api.get(`/roles/${id}`)
    },

    create(data) {
        return api.post('/roles', data)
    },

    update(id, data) {
        return api.put(`/roles/${id}`, data)
    },

    delete(id) {
        return api.delete(`/roles/${id}`)
    }
}
