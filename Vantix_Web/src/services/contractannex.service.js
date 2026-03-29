import api from "@/services/axios";

export default {
    // Lấy danh sách phụ lục theo ID hợp đồng
    getByContractId(contractId) {
        return api.get(`/contract-annexes/contract/${contractId}`);
    },

    // Tạo mới một phụ lục
    create(data) {
        return api.post(`/contract-annexes`, data);
    },

    // Xóa một phụ lục
    delete(annexId) {
        return api.delete(`/contract-annexes/${annexId}`);
    },

    getPositions() {
        return api.get(`/contract-annexes/positions`);
    },

    // Đổi trạng thái một phụ lục
    updateStatus(annexId) {
        return api.put(`/contract-annexes/${annexId}/status`);
    }
}