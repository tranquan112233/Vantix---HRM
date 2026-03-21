import api from "@/services/axios";

class TaskService {

    /* ================= TASK ================= */

    // 🔥 GET ALL TASKS
    async getAll() {
        try {
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
            return await api.put(`/tasks/${id}`, data);
        } catch (error) {
            console.error("Error update task:", error);
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

    // 🔥 GET EMPLOYEES (FIX QUAN TRỌNG)
    async getEmployees() {
        return await api.get("/api/employees") // 🔥 đúng với backend
    }

    /* ================= ASSIGN ================= */

    // 🔥 ASSIGN TASK
    async assign(data) {
        try {
            return await api.post("/tasks/assign", data);
        } catch (error) {
            console.error("Error assign:", error);
            throw error;
        }
    }

    /* ================= REPORT ================= */

    // 🔥 REPORT TASK
    async report(data) {
        try {
            return await api.post("/tasks/report", data);
        } catch (error) {
            console.error("Error report:", error);
            throw error;
        }
    }

    /* ================= MY TASK ================= */

    // 🔥 GET MY TASKS
    async myTasks(employeeId) {
        try {
            return await api.get(`/tasks/my?employeeId=${employeeId}`);
        } catch (error) {
            console.error("Error myTasks:", error);
            throw error;
        }
    }

    /* ================= KPI ================= */

    // 🔥 KPI 1 EMPLOYEE
    async getKPI(employeeId) {
        try {
            return await api.get(`/tasks/kpi?employeeId=${employeeId}`);
        } catch (error) {
            console.error("Error KPI:", error);
            throw error;
        }
    }

    // 🔥 KPI RANKING
    async getRanking() {
        try {
            return await api.get("/tasks/ranking");
        } catch (error) {
            console.error("Error ranking:", error);
            throw error;
        }
    }

}

export default new TaskService();