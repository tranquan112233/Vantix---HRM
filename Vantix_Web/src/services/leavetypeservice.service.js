import api from './axios.js';

// 🔥 BÁC QUÊN DÒNG NÀY NÈ:
// Nếu axios.js có baseURL rồi thì chỉ cần '/leave-types', nếu chưa thì điền full link
const API_URL = 'http://localhost:8080/api/leave-types';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token') || ''
    return {
        headers: { Authorization: `Bearer ${token}` }
    }
}

export default {
    getAll() {
        return api.get(API_URL, getAuthHeaders())
    },
    create(data) {
        return api.post(API_URL, data, getAuthHeaders())
    },
    update(id, data) {
        return api.put(`${API_URL}/${id}`, data, getAuthHeaders())
    },
    delete(id) {
        return api.delete(`${API_URL}/${id}`, getAuthHeaders())
    }
}