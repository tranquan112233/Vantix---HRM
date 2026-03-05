import api from './axios.js'

class ProfileService {
    getMyProfile() {
        return api.get('/profile/me')
    }
    // Đổi tên hàm và đường dẫn cho khớp với Backend
    updateContactInfo(employeeId, data) {
        return api.put(`/profile/${employeeId}`, data)
    }

    uploadAvatar(employeeId, file) {
        let formData = new FormData()
        formData.append('file', file)

        return api.post(`/profile/${employeeId}/avatar`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    }

}

export default new ProfileService()