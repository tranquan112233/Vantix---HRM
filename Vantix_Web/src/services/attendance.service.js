import api from "@/services/axios";

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
        return api.post(`${ENDPOINT}/checkIn`, null, {
            params: {employeeId: employeeId}
        });
    },

    // Check-out thường (Manual)
    checkOutManual(employeeId) {
        return api.put(`${ENDPOINT}/checkOutManual`, null, {
            params: {employeeId: employeeId}
        });
    },

    // --- MỚI THÊM: API XÁC NHẬN CÔNG ---
    confirmCheckOut(employeeId) {
        return api.put(`${ENDPOINT}/confirm-checkout`, null, {
            params: {employeeId: employeeId}
        });
    }
}