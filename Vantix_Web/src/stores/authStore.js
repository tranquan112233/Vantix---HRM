import { reactive } from 'vue'
import profileService from '@/services/profileservice.service.js'
import authService from '@/services/auth.service.js'

const state = reactive({
    user: null,
    loading: false
})

export default {

    state,

    async loadUser() {
        try {
            state.loading = true

            const res = await profileService.getMyProfile()
            state.user = res.data

        } catch (e) {
            state.user = null
            authService.logout()
        } finally {
            state.loading = false
        }
    },

    logout() {
        authService.logout()
        state.user = null
        window.location.href = '/login'
    }
}