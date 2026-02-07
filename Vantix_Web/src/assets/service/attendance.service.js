import api from "./api.js";

const ENDPOINT = '/attendance';

export default {
    getMonthlyAttendance(employeeId, month, year) {
        return api.get(`${ENDPOINT}/getMonthlyAttendance`, {
            params: {
                employeeId: employeeId, Month: month, Year: year
            }
        });
    },

    checkIn(employeeId) {
        return api.post(`${ENDPOINT}/checkIn`, employeeId);
    },

    // Check-out thường (Manual)
    checkOutManual(employeeId) {
        return api.put(`${ENDPOINT}/checkOutManual`, employeeId);
    },

    // --- MỚI THÊM: API XÁC NHẬN CÔNG ---
    confirmCheckOut(employeeId) {
        // Gửi employeeId dạng số nguyên (Integer) để tránh lỗi JSON parse
        return api.put(`${ENDPOINT}/confirm-checkout`, employeeId);
    }
}