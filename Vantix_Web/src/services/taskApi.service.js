import api from "@/services/axios";

class TaskService {

    /* ================= TASK ================= */

    // 🔥 GET ALL TASKS
    async getAll() {
        try {
            // Đã bỏ /api vì baseURL đã có rồi
            return await api.get("/tasks");
        } catch (error) {
            console.error("Error getAll:", error);
            throw error;
        }
    }

    // 🔥 GET TASK BY ID
    async getById(id) {
        try {
            return await api.get(`/tasks/${id}`);
        } catch (error) {
            console.error("Error getById:", error);
            throw error;
        }
    }

    // 🔥 CREATE TASK
    async create(data) {
        try {
            return await api.post("/tasks", data);
        } catch (error) {
            console.error("Error create task:", error);
            throw error;
        }
    }

    // 🔥 UPDATE TASK
    async update(id, data) {
        try {
            // Sửa lại đoạn này, cắm thẳng 8080 vào:
            return await api.put(`http://localhost:8080/api/tasks/${id}`, data);
        } catch (error) {
            console.error("Error update task:", error);
            throw error;
        }
    }

    // 🔥 APPROVE TASK
    async approve(id) {
        try {
            return await api.put(`http://localhost:8080/api/tasks/${id}/approve`);
        } catch (error) {
            console.error("Error approve task:", error);
            throw error;
        }
    }

    // 🔥 DELETE TASK
    async delete(id) {
        try {
            return await api.delete(`/tasks/${id}`);
        } catch (error) {
            console.error("Error delete task:", error);
            throw error;
        }
    }

    /* ================= EMPLOYEE ================= */

    async getEmployees() {
        // Bỏ /api/ ở đầu
        return await api.get("/employees")
    }

    /* ================= ASSIGN ================= */

    async assign(data) {
        // Gọi thẳng sang cổng Backend
        return await api.post("http://localhost:8080/api/tasks/assign", data);
    }

    /* ================= REPORT ================= */

    async report(reportData) {
        console.log("Dữ liệu gửi đi nè:", reportData);
        // 🔥 Thêm http://localhost:8080 vào trước link
        return api.post('http://localhost:8080/api/tasks/report', reportData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    /* ================= MY TASK ================= */

    async myTasks(employeeId) {
        try {
            // 🔥 SỬA TẠI ĐÂY: Bỏ /api ở đầu để tránh lỗi /api/api
            return await api.get(`/tasks/my?employeeId=${employeeId}`);
        } catch (error) {
            console.error("Error myTasks:", error);
            throw error;
        }
    }

    /* ================= KPI ================= */

    async getRanking(month = "") {
        try {
            const url = month ? `http://localhost:8080/api/tasks/ranking?month=${month}` : `http://localhost:8080/api/tasks/ranking`;
            return await api.get(url);
        } catch (error) {
            console.error("Error ranking:", error);
            throw error;
        }
    }
}

export default new TaskService();