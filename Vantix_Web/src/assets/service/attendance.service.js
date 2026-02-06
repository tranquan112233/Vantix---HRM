import api from "./api.js";

const ENDPOINT = '/attendance';

export default {
    // Đã đổi tên tham số thành employeeId
    getMonthlyAttendance(employeeId, month, year) {
        return api.get(`${ENDPOINT}/getMonthlyAttendance`, {
            params: {
                // Bên trái: Tên tham số Backend yêu cầu (@RequestParam)
                // Bên phải: Biến employeeId vừa khai báo ở trên
                employeeId: employeeId,
                Month: month,
                Year: year
            }
        });
    },

    // Đã đổi tên tham số thành employeeId
    checkIn(employeeId) {
        return api.post(`${ENDPOINT}/checkIn`, employeeId);
    },

    // Bổ sung luôn hàm checkOut cho đủ bộ (dựa theo file Vue bạn gửi)
    // checkOut(employeeId) {
    //     // Giả sử backend có endpoint này, nếu chưa có trong Controller thì bạn cần thêm vào Java nhé
    //     return api.post(`${ENDPOINT}/checkOut`, employeeId);
    // }
}