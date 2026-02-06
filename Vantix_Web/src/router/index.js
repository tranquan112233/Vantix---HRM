import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import UserLayout from '@/layouts/UserLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// User pages
import Home from '@/views/user/Home.vue'
import Profile from '@/views/user/Profile.vue'

// Admin pages
import Dashboard from '@/views/admin/Dashboard.vue'
import UserManagement from "../views/admin/UserManagement.vue"

// Auth
import Auth from "@/components/views/Auth.vue";

const routes = [

    // ✅ LUÔN MẶC ĐỊNH VỀ LOGIN
    {
        path: '/',
        redirect: '/login'
    },

    // LOGIN
    {
        path: '/login',
        component: Auth
    },

    // USER
    {
        path: '/app',
        component: UserLayout,
        children: [
            {
                path: 'home',
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
        children: [
            {
                path: '',
                component: Dashboard
            },
            {
                path: 'users',
                component: UserManagement
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {

    const user = localStorage.getItem("user")

    if (!user && to.path !== '/login') {
        return next('/login')
    }

    // ✅ đã login mà vào login → chuyển về home
    if (user && to.path === '/login') {
        return next('/app/home')
    }

    next()
})

export default router
