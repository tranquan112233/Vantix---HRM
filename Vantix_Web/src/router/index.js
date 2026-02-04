import { createRouter, createWebHistory } from 'vue-router'

import Auth from "../components/views/Auth.vue";
import Home from "../components/views/Home.vue";
import Profile from "../components/views/Profile.vue";

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
    },
    {
        path: '/profile',
        name: 'Profile',
        component: Profile,

    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router


