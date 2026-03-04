import api from './axios.js' // Đổi đường dẫn này cho khớp với file axios của bạn

class LeaveService {
    // 1. Gửi đơn xin nghỉ
    createLeaveRequest(data) {
        return api.post('/leaves', data)
    }

    // 2. Lấy danh sách đơn của nhân viên (Dành cho User)
    getMyLeaveRequests(employeeId) {
        return api.get(`/leaves/employee/${employeeId}`)
    }

    // 3. Lấy danh sách đơn đang chờ duyệt (Dành cho Admin/HR)
    getPendingRequests() {
        return api.get('/leaves/pending')
    }

    // 4. Cập nhật trạng thái duyệt đơn (Dành cho Admin/HR)
    updateLeaveStatus(leaveId, status, approverId) {
        return api.put(`/leaves/${leaveId}/status`, null, {
            params: { status, approverId }
        })
    }
}

export default new LeaveService()