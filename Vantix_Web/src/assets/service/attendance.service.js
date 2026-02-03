import api from "./api.js";

const ENDPOINT = '/attendance';

export default {
    // 1. Lấy dữ liệu lịch sử
    getMonthlyAttendance(userId, month, year) {
        return api.get(`${ENDPOINT}/getAttendance`, {
            params: {
                UserID: userId, // Giữ nguyên Case chữ hoa nếu Backend yêu cầu
                Month: month,
                Year: year
            }
        });
    },

    // 2. Chấm công
    checkIn(userId) {
        // Gửi thẳng userId (số nguyên) vì Backend nhận @RequestBody Integer
        return api.post(`${ENDPOINT}/create`, userId);
    }
}