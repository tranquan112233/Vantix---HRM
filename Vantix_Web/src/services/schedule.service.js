import api from "@/services/axios";

const ENDPOINT = '/schedules';

export default {
    // 1. Hàm GET: Lấy danh sách nhân viên và lịch làm việc
    getSchedules(viewerId, month, year) {
        return api.get(`${ENDPOINT}`, {
            params: {
                viewerId: viewerId, month: month, year: year
            }
        });
    },

    // 2. Hàm POST: Lưu lịch làm việc xuống DB (Để sẵn cho chức năng Lưu sắp tới)
    saveDailySchedules(employeeId, month, year, scheduleData) {
        return api.post(`${ENDPOINT}/${employeeId}/${month}/${year}/daily`, scheduleData);
    },

    // 3. Thêm thằng này để sửa trạng thái
    updateStatus(monthlyScheduleId, status) {
        return api.put(`${ENDPOINT}/${monthlyScheduleId}/status`, null, {params: {status}});
    }
}