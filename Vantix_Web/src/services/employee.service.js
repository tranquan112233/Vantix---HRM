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
    // 🔥 THÊM HÀM NÀY VÀO ĐỂ VUE KHÔNG BÁO LỖI "IS NOT A FUNCTION"
    getMyProfile() {
        // Gọi API lấy hồ sơ cá nhân của người đang đăng nhập
        return api.get('/employees/my-profile');
    }
}

export default employeeService