import api from "./api.js";

const ENDPOINT = '/contracts';

export default {
    // 1. Lấy danh sách hợp đồng
    getAllContracts() {
        return api.get(`${ENDPOINT}/getAllContracts`);
    },

    // 2. Thêm mới hợp đồng (Gọi vào @PostMapping("/postContract") của Spring Boot)
    createContract(data) {
        return api.post(`${ENDPOINT}/postContract`, data);
    },

    // 3. Xóa hợp đồng (Gọi vào @DeleteMapping("/{id}") của Spring Boot)
    deleteContract(id) {
        return api.delete(`${ENDPOINT}/${id}`);
    }
}