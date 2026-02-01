import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080/api', // đổi theo backend của bạn
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Nếu có JWT thì mở ra dùng
// api.interceptors.request.use(config => {
//   const token = localStorage.getItem('token')
//   if (token) {
//     config.headers.Authorization = `Bearer ${token}`
//   }
//   return config
// })

export default api
