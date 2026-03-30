import api from '@/services/axios'

// ─── Role Service ─────────────────────────────────────────────────────────────
// Tương ứng với RoleController: /api/roles

const roleService = {

    /**
     * Lấy danh sách role có phân trang + lọc
     *
     * @param {Object} params
     * @param {string} [params.keyword]  - tìm theo name / description
     * @param {number} [params.page=0]
     * @param {number} [params.size=10]
     * @param {string} [params.sortBy=createdAt]
     * @param {string} [params.sortDir=desc]
     * @returns {Promise<PageResponseDTO<RoleResponse>>}
     */
    getAll(params = {}) {
        return api.get('/roles', { params })
    },

    /**
     * Lấy chi tiết 1 role theo id
     *
     * @param {number} id
     * @returns {Promise<RoleResponse>}
     */
    getById(id) {
        return api.get(`/roles/${id}`)
    },

    /**
     * Tạo mới role
     *
     * @param {Object}   data
     * @param {string}   data.name
     * @param {string}   [data.description]
     * @param {number[]} [data.permissionIds]
     * @returns {Promise<RoleResponse>}
     */
    create(data) {
        return api.post('/roles', data)
    },

    /**
     * Cập nhật role
     *
     * @param {number} id
     * @param {Object} data - RoleRequest
     * @returns {Promise<RoleResponse>}
     */
    update(id, data) {
        return api.put(`/roles/${id}`, data)
    },

    /**
     * Xóa mềm role
     *
     * @param {number} id
     * @returns {Promise<void>}
     */
    delete(id) {
        return api.delete(`/roles/${id}`)
    },
}

export default roleService