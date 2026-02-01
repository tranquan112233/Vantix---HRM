import { createRouter, createWebHistory } from 'vue-router'

import Auth from "../components/views/Auth.vue";
import Home from "../components/views/Home.vue"
import Attendance from "../components/views/Attendance.vue";

const routes = [
    {
        path: '/',
        redirect: '/home'
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
        meta: { requiresAuth: true }
    },
    {
        path: '/attendance',
        name: 'Attendance',
        component: Attendance,
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router


