import api from "@/services/axios";

class TaskService {
    /* ================= TASK ================= */
    async getAll() {
        return await api.get("/tasks");
    }

    async getById(id) {
        return await api.get(`/tasks/${id}`);
    }

    async create(data) {
        return await api.post("/tasks", data);
    }

    async update(id, data) {
        return await api.put(`/tasks/${id}`, data);
    }

    async approve(id) {
        return await api.put(`/tasks/${id}/approve`);
    }

    async delete(id) {
        return await api.delete(`/tasks/${id}`);
    }

    /* ================= CÁC HÀM XỬ LÝ (TỪ ADMIN) ================= */
    async reopen(id) {
        return await api.put(`/tasks/${id}/reopen`);
    }

    async cancel(id) {
        return await api.put(`/tasks/${id}/cancel`);
    }

    /* ================= EMPLOYEE ================= */
    async getEmployees() {
        return await api.get("/employees");
    }

    /* ================= ASSIGN ================= */
    async assign(data) {
        return await api.post("/tasks/assign", data);
    }

    /* ================= REPORT ================= */
    async report(reportData) {
        return api.post('/tasks/report', reportData);
    }

    /* ================= MY TASK ================= */
    async myTasks(employeeId) {
        return await api.get(`/tasks/my?employeeId=${employeeId}`);
    }

    /* ================= KPI ================= */
    async getRanking(month = "") {
        const url = month ? `/tasks/ranking?month=${month}` : `/tasks/ranking`;
        return await api.get(url);
    }
}

export default new TaskService();