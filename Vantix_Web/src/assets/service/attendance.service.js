import api from "./api.js";

export default {
    getMonthlyAttendance() {
        return api.get('/getAttendance')
    }
}