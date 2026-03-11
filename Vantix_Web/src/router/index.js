import { createRouter, createWebHistory } from "vue-router"
import { getUser } from "@/utils/jwtDecode"

/* ================= LAYOUT ================= */
import MainLayout from "@/layouts/MainLayout.vue"
import AuthLayout from "@/layouts/AuthLayout.vue"

/* ================= AUTH VIEWS ================= */
import Login from "@/views/auth/Login.vue"
import ForgotPassword from "@/views/auth/ForgotPassword.vue"
import VerifyOtp from "@/views/auth/VerifyOtp.vue"
import ResetPassword from "@/views/auth/ResetPassword.vue"

/* ================= MAIN VIEWS ================= */
import Dashboard from "@/views/main/Dashboard.vue"
import UserManagement from "@/views/main/UserManagement.vue"
import RoleManagement from "@/views/main/RoleManagement.vue"
import Forbidden from "@/views/errors/Forbidden.vue"
import DepartmentManagement from "@/views/main/DepartmentManagement.vue";
import PositionManagement from "@/views/main/PositionManagement.vue";
import EmployeeManagement from "@/views/main/EmployeeManagement.vue";
import Attendance from "../views/main/Attendance.vue";
import Contract from "@/views/main/Contract.vue";
import LeaveManagement from "@/views/main/LeaveManagement.vue";
import LeaveRequest from "@/views/user/LeaveRequest.vue";

/* ================= ROUTES ================= */
const routes = [
    /* ========= AUTH ========= */
    {
        path: "/auth",
        component: AuthLayout,
        children: [
            { path: "login", component: Login, meta: { guestOnly: true } },
            { path: "forgot-password", component: ForgotPassword },
            { path: "verify-otp", component: VerifyOtp },
            { path: "reset-password", component: ResetPassword }
        ]
    },

    /* ========= MAIN ========= */
    {
        path: "/",
        component: MainLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: "dashboard",
                component: Dashboard
                // Dashboard thường ai cũng xem được nên không cần lock
            },
            {
                path: "users",
                component: UserManagement,
                meta: { permission: "users" } // Yêu cầu có permission 'users'
            },
            {
                path: "roles",
                component: RoleManagement,
                meta: { permission: "roles" }
            },
            {
                path: "departments",
                component: DepartmentManagement,
                meta: { permission: "departments" }
            },
            {
                path: "positions",
                component: PositionManagement,
                meta: { permission: "positions" }
            },
            {
                path: "employees",
                component: EmployeeManagement,
                meta: { permission: "employees" }
            },
            {
                path: "attendances",
                component: Attendance,
                meta: { permission: "attendances" }
            },
            {
                path: "contracts",
                component: Contract,
                meta: { permission: "contracts" }
            },
            {
                path: "leave",
                component: LeaveManagement,
                meta: { permission: "leave" }
            },
            {
                path: "leaves",
                component: LeaveRequest,
                meta: { permission: "leaves" }
            }
        ]
    },

    /* ========= ERROR ========= */
    { path: "/403", component: Forbidden },

    /* ========= DEFAULT ========= */
    { path: "/", redirect: "/dashboard" },
    { path: "/:pathMatch(.*)*", redirect: "/dashboard" }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/* ================= ROUTE GUARD ================= */
router.beforeEach((to) => {
    const user = getUser()

    const requiresAuth = to.matched.some(r => r.meta.requiresAuth)
    const guestOnly = to.matched.some(r => r.meta.guestOnly)

    // 1️⃣ Cần login nhưng chưa login
    if (requiresAuth && !user) {
        return { path: "/auth/login" }
    }

    // 2️⃣ Đã login mà vào trang login/auth
    if (guestOnly && user) {
        return { path: "/dashboard" }
    }

    // 3️⃣ KIỂM TRA QUYỀN ĐỘNG (DYNAMIC PERMISSIONS)
    if (user && to.meta.permission) {
        const userRoles = user.roles || []
        const userPermissions = user.permissions || []

        // ADMIN là chúa tể, cho qua hết
        const isAdmin = userRoles.includes("ADMIN") || userRoles.includes("ROLE_ADMIN")

        if (!isAdmin) {
            // Nếu không phải ADMIN, kiểm tra xem key permission có nằm trong mảng của User không
            const hasPermission = userPermissions.includes(to.meta.permission)

            if (!hasPermission) {
                console.warn(`🔒 Truy cập bị từ chối. Cần quyền: ${to.meta.permission}`)
                return { path: "/403" } // Đá ra chuồng gà
            }
        }
    }

    // (Giữ lại logic cũ phòng trường hợp bạn cấu hình meta: { roles: [...] })
    if (user && to.meta.roles) {
        const userRoles = user.roles || []
        const isAdmin = userRoles.includes("ADMIN") || userRoles.includes("ROLE_ADMIN")

        if (!isAdmin) {
            const hasRole = to.meta.roles.some(requiredRole =>
                userRoles.includes(requiredRole) || userRoles.includes(`ROLE_${requiredRole}`)
            )
            if (!hasRole) return { path: "/403" }
        }
    }

    return true
})

export default router