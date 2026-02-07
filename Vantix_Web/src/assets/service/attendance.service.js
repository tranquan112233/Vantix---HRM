import api from "./api.js";

const ENDPOINT = '/attendance';

export default {
    // Đã đổi tên tham số thành employeeId
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

    checkOutManual(employeeId) {
        return api.put(`${ENDPOINT}/checkOutManual`, employeeId);
    }
}