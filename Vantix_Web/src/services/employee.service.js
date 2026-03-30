import api from '@/services/axios'

const employeeService = {
    getAll(params = {}) {
        return api.get('/employees', { params })
    },

    getById(id) {
        return api.get(`/employees/${id}`)
    },

    getByUserId(userId) {
        return api.get(`/employees/user/${userId}`)
    },

    create(data) {
        return api.post('/employees', data)
    },

    update(id, data) {
        return api.put(`/employees/${id}`, data)
    },

    delete(id) {
        return api.delete(`/employees/${id}`)
    },
}

export default employeeService