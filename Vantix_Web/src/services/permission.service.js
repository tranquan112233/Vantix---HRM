import api from '@/services/axios'

// ─── Permission Service ───────────────────────────────────────────────────────
// Tương ứng với PermissionController: /api/permissions

const permissionService = {

    /**
     * Lấy danh sách permission có phân trang + lọc
     *
     * @param {Object} params
     * @param {string} [params.keyword]  - tìm theo name / description
     * @param {number} [params.page=0]
     * @param {number} [params.size=10]
     * @param {string} [params.sortBy=createdAt]
     * @param {string} [params.sortDir=desc]
     * @returns {Promise<PageResponseDTO<PermissionResponse>>}
     */
    getAll(params = {}) {
        return api.get('/permissions', { params })
    },

    /**
     * Lấy chi tiết 1 permission theo id
     *
     * @param {number} id
     * @returns {Promise<PermissionResponse>}
     */
    getById(id) {
        return api.get(`/permissions/${id}`)
    },

    /**
     * Tạo mới permission
     *
     * @param {Object} data
     * @param {string} data.name
     * @param {string} [data.description]
     * @returns {Promise<PermissionResponse>}
     */
    create(data) {
        return api.post('/permissions', data)
    },

    /**
     * Cập nhật permission
     *
     * @param {number} id
     * @param {Object} data - PermissionRequest
     * @returns {Promise<PermissionResponse>}
     */
    update(id, data) {
        return api.put(`/permissions/${id}`, data)
    },

    /**
     * Xóa mềm permission
     *
     * @param {number} id
     * @returns {Promise<void>}
     */
    delete(id) {
        return api.delete(`/permissions/${id}`)
    },
}

export default permissionService