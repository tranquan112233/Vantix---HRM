// stores/auth.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/auth.service.js'

export const useAuthStore = defineStore('auth', () => {

    // ─── State ───────────────────────────────────────────────────────────────
    const token = ref(localStorage.getItem('token') || null)
    const user  = ref(null)
    const loading = ref(false)

    // ─── Getters ──────────────────────────────────────────────────────────────
    const isLoggedIn = computed(() => !!token.value)
    const userRole = computed(() => user.value?.role || null)
    const userPermissions = computed(() => user.value?.permissions || [])

    // ─── Actions ──────────────────────────────────────────────────────────────

    async function login(credentials) {
        try {
            const res = await authService.login(credentials)
            token.value = res.token
            localStorage.setItem('token', res.token)
            await fetchMe()
            return { success: true, data: res }
        } catch (error) {
            throw error.response?.data || error
        }
    }

    async function fetchMe() {
        if (!token.value) return null

        loading.value = true
        try {
            const res = await authService.me()
            user.value = res
            return res
        } catch (error) {
            logout()
            return null
        } finally {
            loading.value = false
        }
    }

    async function refreshPermissions() {
        if (!token.value) return false

        try {
            const res = await authService.me()
            user.value = res
            return true
        } catch (error) {
            return false
        }
    }

    function can(permission) {
        if (!permission) return true
        if (!user.value) return false
        if (user.value.role === 'ADMIN') return true

        const userPermissions = user.value.permissions || []

        if (Array.isArray(permission)) {
            return permission.some(p => userPermissions.includes(p))
        } else {
            return userPermissions.includes(permission)
        }
    }

    function canAny(permissions) {
        if (!permissions || permissions.length === 0) return true
        if (user.value?.role === 'ADMIN') return true
        return permissions.some(p => user.value?.permissions?.includes(p))
    }

    function canAll(permissions) {
        if (!permissions || permissions.length === 0) return true
        if (user.value?.role === 'ADMIN') return true
        return permissions.every(p => user.value?.permissions?.includes(p))
    }

    function logout() {
        token.value = null
        user.value = null
        localStorage.removeItem('token')

        try {
            authService.logout()
        } catch (error) {
            // Silent fail
        }

        window.location.href = '/login'
    }

    return {
        token,
        user,
        loading,
        isLoggedIn,
        userRole,
        userPermissions,
        login,
        fetchMe,
        refreshPermissions,
        can,
        canAny,
        canAll,
        logout
    }
})