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
            {
                path: "login",
                component: Login,
                meta: { guestOnly: true }
            },
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
            },
            {
                path: "users",
                component: UserManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "roles",
                component: RoleManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "departments",
                component: DepartmentManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "positions",
                component: PositionManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "employees",
                component: EmployeeManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "attendances",
                component: Attendance,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "contracts",
                component: Contract,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "leave",
                component: LeaveManagement,
                meta: { roles: ["ADMIN"] }
            },
            {
                path: "leaves",
                component: LeaveRequest,
                meta: { roles: ["EMPLOYEE"] }
            }

        ]
    },

    /* ========= ERROR ========= */
    {
        path: "/403",
        component: Forbidden
    },

    /* ========= DEFAULT ========= */
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

/* ================= ROUTE GUARD ================= */
router.beforeEach((to) => {

    const user = getUser()

    const requiresAuth = to.matched.some(r => r.meta.requiresAuth)
    const guestOnly = to.matched.some(r => r.meta.guestOnly)
    const roles = to.meta.roles

    // 1️⃣ Cần login nhưng chưa login
    if (requiresAuth && !user) {
        return { path: "/auth/login" }
    }

    // 2️⃣ Đã login mà vào login
    if (guestOnly && user) {
        return { path: "/dashboard" }
    }

    // 3️⃣ Kiểm tra role (ĐÃ NÂNG CẤP ĐA QUYỀN)
    if (roles && user) {
        // Lấy mảng quyền từ payload của Token (Backend trả về biến "roles")
        const userRoles = user.roles || []

        // Kiểm tra xem User có ít nhất 1 quyền nằm trong danh sách yêu cầu của Route không
        // Xử lý luôn trường hợp Backend có tiền tố "ROLE_" mà Frontend thì không ghi
        const hasPermission = roles.some(requiredRole =>
            userRoles.includes(requiredRole) || userRoles.includes(`ROLE_${requiredRole}`)
        )

        if (!hasPermission) {
            return { path: "/403" } // Không có quyền thì đá sang trang Forbidden
        }
    }

    return true
})

export default router