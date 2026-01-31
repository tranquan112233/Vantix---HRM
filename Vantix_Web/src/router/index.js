import { createRouter, createWebHistory } from 'vue-router'

import Auth from "../components/views/Auth.vue";
import Home from "../components/views/Home.vue"

const routes = [
    {
        path: '/',
        redirect: '/auth'
    },
    {
        path: '/auth',
        name: 'Auth',
        component: Auth,
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true } // cần đăng nhập
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router


