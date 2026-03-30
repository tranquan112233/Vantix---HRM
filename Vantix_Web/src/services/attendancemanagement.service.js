import api from "@/services/axios";

export default {
    getRejectedAttendances(managerId) {
        return api.get(`/attendance-management/rejected/${managerId}`);
    },

    approveAttendance(attendanceId) {
        return api.put(`/attendance-management/approve/${attendanceId}`);
    }
}