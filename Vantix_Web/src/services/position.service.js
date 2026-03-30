import api from '@/services/axios'

const positionService = {
    getAll(params = {}) {
        return api.get('/positions', { params })
    },

    getByDepartment(departmentId) {
        return api.get(`/positions/department/${departmentId}`)
    },

    getById(id) {
        return api.get(`/positions/${id}`)
    },

    create(data) {
        return api.post('/positions', data)
    },

    update(id, data) {
        return api.put(`/positions/${id}`, data)
    },

    delete(id) {
        return api.delete(`/positions/${id}`)
    },
}

export default positionService