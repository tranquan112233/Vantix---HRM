import api from "@/services/axios";

const ENDPOINT = '/payroll-batches';

export default {
    // 1. Lấy danh sách tất cả đợt lương
    getAllBatches() {
        return api.get(`${ENDPOINT}`);
    },

    // 2. Cập nhật trạng thái (Duyệt/Từ chối)
    updateStatus(batchId, status) {
        return api.put(`${ENDPOINT}/${batchId}/status`, null, {
            params: {status: status}
        });
    }

}