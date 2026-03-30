import api from '@/services/axios'

// ─── User Service ─────────────────────────────────────────────────────────────
// Tương ứng với UserController: /api/users

const userService = {

    /**
     * Lấy danh sách user có phân trang + lọc
     *
     * @param {Object} params
     * @param {string}     [params.keyword]  - tìm theo username / email
     * @param {string}     [params.status]   - ACTIVE | LOCKED
     * @param {number}     [params.page=0]
     * @param {number}     [params.size=10]
     * @param {string}     [params.sortBy=createdAt]
     * @param {string}     [params.sortDir=desc]
     * @returns {Promise<PageResponseDTO<UserResponse>>}
     */
    getAll(params = {}) {
        return api.get('/users', { params })
    },

    /**
     * Lấy chi tiết 1 user theo id
     *
     * @param {number} id
     * @returns {Promise<UserResponse>}
     */
    getById(id) {
        return api.get(`/users/${id}`)
    },

    /**
     * Tạo mới user
     *
     * @param {Object} data - UserRequest
     * @param {string} data.username
     * @param {string} data.password
     * @param {string} data.email
     * @param {number} [data.roleId]
     * @param {string} [data.status]  - ACTIVE | LOCKED
     * @returns {Promise<UserResponse>}
     */
    create(data) {
        return api.post('/users', data)
    },

    /**
     * Cập nhật user
     *
     * @param {number} id
     * @param {Object} data - UserRequest (password có thể bỏ trống)
     * @returns {Promise<UserResponse>}
     */
    update(id, data) {
        return api.put(`/users/${id}`, data)
    },

    /**
     * Xóa mềm user
     *
     * @param {number} id
     * @returns {Promise<void>}
     */
    delete(id) {
        return api.delete(`/users/${id}`)
    },

    /**
     * Thay đổi trạng thái ACTIVE / LOCKED
     *
     * @param {number} id
     * @param {string} status - ACTIVE | LOCKED
     * @returns {Promise<UserResponse>}
     */
    changeStatus(id, status) {
        return api.patch(`/users/${id}/status`, null, { params: { status } })
    },
}

export default userService