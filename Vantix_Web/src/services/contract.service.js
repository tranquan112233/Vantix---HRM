import api from "@/services/axios";

export default {
    // Chỉ gọi /contracts/all vì baseURL đã có sẵn /api rồi
    getAll() {
        return api.get(`/contracts/all`);
    },

    create(data) {
        return api.post(`/contracts/create`, data);
    },

    update(id) {
        return api.put(`/contracts/${id}/status`);
    },

    delete(id) {
        return api.delete(`/contracts/${id}`);
    }
}