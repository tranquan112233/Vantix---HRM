import api from './axios.js'

const leaveService = {

    // Lấy tất cả đơn (Admin)
    getAll() {
        return api.get("/leaves");
    },

    // Lấy đơn theo nhân viên
    getByEmployee(employeeId) {
        return api.get(`/leaves/employee/${employeeId}`);
    },

    // Tạo đơn nghỉ
    create(data) {
        return api.post("/leaves", data);
    },

    // Duyệt đơn
    approve(id, approverId) {
        return api.put(`/leaves/approve/${id}?approverId=${approverId}`);
    },

    // Từ chối đơn
    reject(id, approverId) {
        return api.put(`/leaves/reject/${id}?approverId=${approverId}`);
    }

};

export default leaveService;