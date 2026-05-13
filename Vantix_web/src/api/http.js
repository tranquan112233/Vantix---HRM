import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('vx_token') || sessionStorage.getItem('vx_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status
    const requestUrl = error.config?.url || ''
    if (status === 401) {
      localStorage.removeItem('vx_token')
      localStorage.removeItem('vx_user')
      sessionStorage.removeItem('vx_token')
      sessionStorage.removeItem('vx_user')
      if (!requestUrl.includes('/auth/login') && router.currentRoute.value.path !== '/login') {
        router.push('/login')
        ElMessage.error('Your session has expired')
      }
    } else if (status === 403) {
      ElMessage.error('You do not have permission to perform this action')
    } else if (status >= 500) {
      ElMessage.error('System error. Please try again')
    }
    return Promise.reject(error)
  }
)

export default http
