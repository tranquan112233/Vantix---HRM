import { createRouter, createWebHistory } from 'vue-router'
import {
  buildRouteRecords,
  canAccessPermissions,
  findFirstAccessiblePath,
  menuConfig,
} from '@/config/menu.config'

const routes = [
  {
    path: '/login',
    component: () => import('@/layouts/AuthLayout.vue'),
    meta: { guest: true },
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { guest: true, titleKey: 'login.signIn' },
      },
      {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/ForgotPassword.vue'),
        meta: { guest: true, titleKey: 'auth.forgotPassword' },
      },
      {
        path: '/verify-otp',
        name: 'VerifyOtp',
        component: () => import('@/views/VerifyOtp.vue'),
        meta: { guest: true, titleKey: 'auth.verifyOtp' },
      },
      {
        path: '/reset-password',
        name: 'ResetPassword',
        component: () => import('@/views/ResetPassword.vue'),
        meta: { guest: true, titleKey: 'auth.resetPassword' },
      },
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: buildRouteRecords(menuConfig),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem('vx_user') || sessionStorage.getItem('vx_user') || 'null')
  } catch {
    return null
  }
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('vx_token') || sessionStorage.getItem('vx_token')
  const user = getStoredUser()
  const permissions = user?.permissions || []

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next(findFirstAccessiblePath(menuConfig, permissions))
  } else if (
    to.meta.requiresAuth &&
    !canAccessPermissions(to.meta.permissions, permissions, to.meta.permissionMode)
  ) {
    next(findFirstAccessiblePath(menuConfig, permissions))
  } else {
    next()
  }
})

export default router
