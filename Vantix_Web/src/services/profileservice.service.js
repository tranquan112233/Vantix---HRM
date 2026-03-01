import api from './axios.js'

export default {

    getMyProfile() {
        return api.get('/profile/me')
    },

    updateProfile(data) {
        return api.put('/profile', data)
    },

    uploadAvatar(file) {

        const formData = new FormData()
        formData.append('file', file)

        return api.post('/profile/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    }
}