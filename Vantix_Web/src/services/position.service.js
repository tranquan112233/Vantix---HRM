import api from "@/services/axios";

class positionService {

    getAll() {
        return api.get("/positions");
    }

    getById(id) {
        return api.get(`/positions/${id}`);
    }

    create(data) {
        return api.post("/positions", data);
    }

    update(id, data) {
        return api.put(`/positions/${id}`, data);
    }

    delete(id) {
        return api.delete(`/positions/${id}`);
    }
}

export default new positionService();