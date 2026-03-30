import api from './axios.js'

class LeaveService {
    // 1. Gửi đơn xin nghỉ
    createLeaveRequest(data) {
        return api.post('/leaves', data)
    }

    // 2. Lấy danh sách đơn của nhân viên (Dành cho User)
    getMyLeaveRequests() { // Xóa biến employeeId ở đây đi
        return api.get('/leaves/my-requests') // Trỏ vào API mới
    }

    // 3. Lấy danh sách đơn đang chờ duyệt (Dành cho Admin/HR)
    getPendingRequests() {
        return api.get('/leaves/pending')
    }

    // 4. Cập nhật trạng thái duyệt đơn (Dành cho Admin/HR)
    updateLeaveStatus(leaveId, status) {
        return api.put(`/leaves/${leaveId}/status`, null, {
            params: { status }
        })
    }

    // 🔥 5. Lấy danh sách Loại nghỉ phép (SỬA LẠI ĐÚNG 1 DÒNG NÀY THÔI)
    getLeaveTypes() {
        return api.get('/leave-types');
    }
}

export default new LeaveService()