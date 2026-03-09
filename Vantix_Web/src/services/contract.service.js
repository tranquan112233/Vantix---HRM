import api from "@/services/axios";

export default {
    getAll() {
        return api.get(`/contracts`);
    },

    create(data) {
        return api.post("/contracts", data);
    },

    update(id, data) {
        return api.put(`/contracts/${id}`, data);
    },

    delete(id) {
        return api.delete(`/contracts/${id}`);
    }
}