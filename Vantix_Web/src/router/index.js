import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import UserLayout from '@/layouts/UserLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// User pages
import Home from '@/views/user/Home.vue'
import Profile from '@/views/user/Profile.vue'
import ChangePassword from "@/views/user/ChangePassword.vue"
import Attendance from "@/views/Attendance.vue"

// Admin pages
import Dashboard from '@/views/admin/Dashboard.vue'
import UserManagement from '@/views/admin/UserManagement.vue'
import DepartmentManagement from "@/views/admin/DepartmentManagement.vue"
import EmployeeManagement from "@/views/admin/EmployeeManagement.vue"

// Auth
import Login from '@/views/auth/Login.vue'
import ForgotPassword from "@/views/auth/ForgotPassword.vue"
import ResetPassword from "@/views/auth/ResetPassword.vue"

const routes = [

    // ================= AUTH =================
    {
        path: '/login',
        name: 'login',
        component: Login,
        meta: { public: true }
    },
    {
        path: '/forgot-password',
        name: 'forgot-password',
        component: ForgotPassword,
        meta: { public: true }
    },
    {
        path: '/reset-password',
        name: 'reset-password',
        component: ResetPassword,
        meta: { public: true }
    },

    // ================= USER =================
    {
        path: '/',
        component: UserLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                redirect: '/home'
            },
            {
                path: 'home',
                name: 'home',
                component: Home
            },
            {
                path: 'profile',
                name: 'profile',
                component: Profile
            },
            {
                path: 'change-password',
                name: 'change-password',
                component: ChangePassword
            },
            {
                path: 'attendance',
                name: 'attendance',
                component: Attendance
            }
        ]
    },

    // ================= ADMIN =================
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true, role: 'ADMIN' },
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
            },
            {
                path: 'departments',
                name: 'admin-departments',
                component: DepartmentManagement
            },
            {
                path: 'employees',
                name: 'admin-employees',
                component: EmployeeManagement
            }
        ]
    },

    // ================= NOT FOUND =================
    {
        path: '/:pathMatch(.*)*',
        redirect: '/home'
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})


// ================= AUTH HELPER =================
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


// ================= ROUTE GUARD =================
router.beforeEach((to, from, next) => {
    const auth = getAuth()
    const isAuthenticated = !!auth

    // Không login mà vào trang cần auth
    if (to.meta.requiresAuth && !isAuthenticated) {
        return next('/login')
    }

    // Đã login mà vào login
    if (to.meta.public && isAuthenticated) {
        if (auth.role === 'ADMIN') {
            return next('/admin')
        }
        return next('/home')
    }

    // Kiểm tra role ADMIN
    if (to.meta.role === 'ADMIN' && auth?.role !== 'ADMIN') {
        return next('/home')
    }

    next()
})

export default router