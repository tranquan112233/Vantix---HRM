import api from "@/services/axios";

class EmployeeService {

    getAll() {
        return api.get("/employees");
    }

    getById(id) {
        return api.get(`/employees/${id}`);
    }

    create(data) {
        return api.post("/employees", data);
    }

    update(id, data) {
        return api.put(`/employees/${id}`, data);
    }

    delete(id) {
        return api.delete(`/employees/${id}`);
    }
}

export default new EmployeeService();