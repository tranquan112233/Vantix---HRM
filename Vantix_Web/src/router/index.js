import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import UserLayout from '@/layouts/UserLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// User pages
import Home from '@/views/user/Home.vue'
import Profile from '@/views/user/Profile.vue'

// Admin pages
import Dashboard from '@/views/admin/Dashboard.vue'
import UserManagement from "../views/admin/UserManagement.vue";

// Auth
import Login from "@/views/auth/Login.vue";

const routes = [

    // LOGIN
    {
        path: '/login',
        component: Login
    },

    // USER LAYOUT
    {
        path: '/',
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

    // ADMIN LAYOUT
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
    history: createWebHistory(),
    routes
})

export default router
