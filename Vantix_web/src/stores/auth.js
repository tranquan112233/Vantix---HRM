import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import router from '@/router'
import { canAccessPermissions } from '@/config/menu.config'

export const useAuthStore = defineStore('auth', () => {
  function getStoredUser() {
    try {
      return JSON.parse(localStorage.getItem('vx_user') || sessionStorage.getItem('vx_user') || 'null')
    } catch {
      return null
    }
  }

  function normalizeUser(data = {}) {
    return {
      id: data.id,
      username: data.username,
      email: data.email,
      role: data.role ?? data.roleName ?? '',
      permissions: Array.isArray(data.permissions) ? data.permissions : [],
    }
  }

  function persistUser(nextUser, remember = true) {
    user.value = normalizeUser(nextUser)
    const storage = remember ? localStorage : sessionStorage
    storage.setItem('vx_user', JSON.stringify(user.value))
  }

  function clearSession() {
    token.value = ''
    user.value = null
    localStorage.removeItem('vx_token')
    localStorage.removeItem('vx_user')
    sessionStorage.removeItem('vx_token')
    sessionStorage.removeItem('vx_user')
  }

  const token = ref(localStorage.getItem('vx_token') || sessionStorage.getItem('vx_token') || '')
  const user = ref(getStoredUser())

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')
  const email = computed(() => user.value?.email || '')
  const role = computed(() => user.value?.role || '')
  const permissions = computed(() => user.value?.permissions || [])

  function hasPermission(perm) {
    return permissions.value.includes(perm)
  }

  function canAccess(requiredPermissions = [], mode = 'all') {
    return canAccessPermissions(requiredPermissions, permissions.value, mode)
  }

  async function login(credentials, remember = true) {
    const { data } = await authApi.login(credentials)
    localStorage.removeItem('vx_token')
    localStorage.removeItem('vx_user')
    sessionStorage.removeItem('vx_token')
    sessionStorage.removeItem('vx_user')
    const storage = remember ? localStorage : sessionStorage
    token.value = data.accessToken
    storage.setItem('vx_token', data.accessToken)
    persistUser(data.user || {}, remember)
    router.push('/')
  }

  async function refreshCurrentUser() {
    if (!token.value) {
      return null
    }

    try {
      const { data } = await authApi.me()
      persistUser(data)
      return user.value
    } catch (error) {
      if (error.response?.status === 401) {
        clearSession()
      }
      throw error
    }
  }

  function logout() {
    clearSession()
    router.push('/login')
  }

  return {
    token,
    user,
    isLoggedIn,
    username,
    email,
    role,
    permissions,
    hasPermission,
    canAccess,
    login,
    refreshCurrentUser,
    logout,
  }
})
