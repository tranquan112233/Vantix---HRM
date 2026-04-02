import api from './axios.js';

const API_URL = '/leave-types';

export default {
    getAll() {
        return api.get(API_URL)
    },
    create(data) {
        return api.post(API_URL, data)
    },
    update(id, data) {
        return api.put(`${API_URL}/${id}`, data)
    },
    delete(id) {
        return api.delete(`${API_URL}/${id}`)
    }
}
