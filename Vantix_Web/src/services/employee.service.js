import api from "@/services/axios";

class employeeService {

    // ================= GET ALL =================
    getAll() {
        return api.get("/employees");
    }

    // ================= GET BY ID =================
    getById(id) {
        return api.get(`/employees/${id}`);
    }

    // ================= CREATE =================
    create(data) {
        return api.post("/employees", data);
    }

    // ================= UPDATE =================
    update(id, data) {
        return api.put(`/employees/${id}`, data);
    }

    // ================= DELETE =================
    delete(id) {
        return api.delete(`/employees/${id}`);
    }
}

export default new employeeService();