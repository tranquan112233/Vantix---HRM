import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import UserLayout from '@/layouts/UserLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// User pages
import Home from '@/views/user/Home.vue'
import Profile from '@/views/user/Profile.vue'

// Admin pages
import Dashboard from '@/views/admin/Dashboard.vue'
import UserManagement from '@/views/admin/UserManagement.vue'

// Auth
import Login from '@/views/auth/Login.vue'

const routes = [
    // LOGIN
    {
        path: '/',
        name: 'login',
        component: Login,
        meta: { public: true }
    },
    {
        path: '/login',
        redirect: '/'
    },

    // USER
    {
        path: '/home',
        component: UserLayout,
        children: [
            {
                path: '',
                name: 'home',
                component: Home
            },
            {
                path: 'profile',
                name: 'profile',
                component: Profile
            }
        ]
    },

    // ADMIN
    {
        path: '/admin',
        component: AdminLayout,
        meta: { role: 'ADMIN' },
        children: [
            {
                path: '',
                name: 'admin-dashboard',
                component: Dashboard
            },
            {
                path: 'users',
                name: 'admin-users',
                component: UserManagement
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

/**
 * Đọc auth CHUẨN theo cách bạn lưu
 * token: localStorage.token
 * user : { username, role }
 */
function getAuth() {
    const token = localStorage.getItem('token')
    const rawUser = localStorage.getItem('user')

    if (!token || !rawUser) return null

    try {
        const user = JSON.parse(rawUser)
        return {
            token,
            username: user.username,
            role: user.role?.toUpperCase()
        }
    } catch {
        return null
    }
}

router.beforeEach((to, from, next) => {
    const auth = getAuth()
    const isAuthenticated = !!auth
    const requiredRole = to.meta.role

    // Chưa login → về login
    if (!isAuthenticated && !to.meta.public) {
        return next('/')
    }

    // Đã login mà vào login → redirect theo role
    if (isAuthenticated && to.meta.public) {
        return next(from.fullPath !== '/' ? from.fullPath : '/home')
    }

    // Chỉ chặn USER vào ADMIN
    if (requiredRole === 'ADMIN' && auth.role !== 'ADMIN') {
        return next('/home/profile')
    }

    next()
})


export default router
