import api from "@/services/axios";

export default {
    getPendingAttendances(managerId) {
        return api.get(`/attendance-management/pending/${managerId}`);
    },

    approveAttendance(attendanceId) {
        return api.put(`/attendance-management/approve/${attendanceId}`);
    },

    rejectAttendance(attendanceId) {
        return api.put(`/attendance-management/reject/${attendanceId}`);
    }
}
