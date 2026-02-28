import { createRouter, createWebHistory } from "vue-router"
import { getUser } from "@/utils/jwtDecode"

/*
|--------------------------------------------------------------------------
| IMPORT LAYOUT
|--------------------------------------------------------------------------
| AuthLayout: dùng cho các trang không cần đăng nhập
| MainLayout: chứa Header + Sidebar, chỉ dùng cho user đã đăng nhập
|--------------------------------------------------------------------------
*/
import MainLayout from "@/layouts/MainLayout.vue"
import AuthLayout from "@/layouts/AuthLayout.vue"

/*
|--------------------------------------------------------------------------
| IMPORT VIEW
|--------------------------------------------------------------------------
*/
import Login from "@/views/auth/Login.vue"
import ResetPassword from "@/views/auth/ResetPassword.vue"
import VerifyOtp from "@/views/auth/VerifyOtp.vue"
import ForgotPassword from "@/views/auth/ForgotPassword.vue"

import Dashboard from "@/views/main/Dashboard.vue"
import RoleManagement from "@/views/main/RoleManagement.vue";
import UserManagement from "@/views/main/UserManagement.vue";

/*
|--------------------------------------------------------------------------
| ROUTE CONFIGURATION
|--------------------------------------------------------------------------
*/
const routes = [

    /*
    |--------------------------------------------------------------------------
    | AUTH LAYOUT
    | Các route trong đây không yêu cầu đăng nhập
    |--------------------------------------------------------------------------
    */
    {
        path: "/auth",
        component: AuthLayout,
        children: [
            {
                path: "login",
                component: Login,
                meta: { guestOnly: true }
            },
            {
                path: "forgot-password",
                component: ForgotPassword
            },
            {
                path: "verify-otp",
                component: VerifyOtp
            },
            {
                path: "reset-password",
                component: ResetPassword
            }
        ]
    },

    /*
    |--------------------------------------------------------------------------
    | MAIN LAYOUT
    | Các route trong đây yêu cầu đăng nhập
    |--------------------------------------------------------------------------
    */
    {
        path: "/",
        component: MainLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: "dashboard",
                component: Dashboard
            },
            {
                path: "users",
                component: UserManagement
            },
            {
                path: "roles",
                component: RoleManagement
            }
        ]
    },

    /*
    |--------------------------------------------------------------------------
    | REDIRECT MẶC ĐỊNH VÀ 404
    |--------------------------------------------------------------------------
    */
    {
        path: "/",
        redirect: "/dashboard"
    },
    {
        path: "/:pathMatch(.*)*",
        redirect: "/dashboard"
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/*
|--------------------------------------------------------------------------
| ROUTE GUARD
|--------------------------------------------------------------------------
| Đây là guard kiểm tra JWT phía frontend.
|
| getUser():
| - Lấy token từ localStorage
| - Decode JWT
| - Trả về payload nếu tồn tại
| - Trả về null nếu không có token
|
| Lưu ý:
| Guard này chỉ có tác dụng ở frontend.
| Backend vẫn phải kiểm tra JWT bằng Spring Security.
|--------------------------------------------------------------------------
*/
router.beforeEach((to) => {

    const user = getUser()

    /*
    |--------------------------------------------------------------------------
    | Nếu route yêu cầu đăng nhập mà không có token
    |--------------------------------------------------------------------------
    */
    if (to.meta.requiresAuth && !user) {
        return { path: "/auth/login" }
    }

    /*
    |--------------------------------------------------------------------------
    | Nếu đã đăng nhập mà truy cập trang chỉ dành cho khách
    |--------------------------------------------------------------------------
    */
    if (to.meta.guestOnly && user) {
        return { path: "/" }
    }

    /*
    |--------------------------------------------------------------------------
    | Kiểm tra role nếu route có yêu cầu role
    |--------------------------------------------------------------------------
    | Ví dụ:
    | meta: { role: "ADMIN" }
    |--------------------------------------------------------------------------
    */
    if (to.meta.role && user?.role !== to.meta.role) {
        return { path: "/dashboard" }
    }

    /*
    |--------------------------------------------------------------------------
    | Cho phép điều hướng tiếp tục
    |--------------------------------------------------------------------------
    */
    return true
})

export default router