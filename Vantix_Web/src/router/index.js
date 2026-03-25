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
import Profile from "@/components/Profile.vue";
import Settings from "@/components/Settings.vue";
import LeaveTypes from "@/views/main/LeaveTypes.vue";

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
        meta: { requiresAuth: true }, // Layout chính bắt buộc phải login
        children: [
            {
                path: "dashboard",
                component: Dashboard
                // Dashboard ai cũng xem được nên không cần lock
            },
            {
                path: "users",
                component: UserManagement,
                meta: { permission: "users" }
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
                path: "leave-approvals",
                component: LeaveManagement,
                meta: { permission: "leave-approvals" }
            },
            {
                path: "my-leaves",
                component: LeaveRequest,
                meta: { permission: "my-leaves" }
            },
            {
                path: "profile",
                component: Profile,
                meta: { permission: "profile" }
            },
            {
                path: "settings",
                component: Settings,
                meta: { permission: "settings" }
            },
            {
                path: "leavetypes",
                component: LeaveTypes,
                meta: { permission: "leavetypes" }
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

// =============================================================
router.beforeEach((to) => {
    const user = getUser()
    if (to.matched.some(r => r.meta.requiresAuth) && !user) return { path: "/auth/login" }
    if (to.matched.some(r => r.meta.guestOnly) && user) return { path: "/dashboard" }

    if (user && to.meta.permission) {
        const userRoles = user.roles || []
        const userPermissions = user.permissions || []
        const isAdmin = userRoles.includes("ADMIN") || userRoles.includes("ROLE_ADMIN")
        const isHR = userRoles.includes("HR") || userRoles.includes("ROLE_HR")

        if (!isAdmin) {
            const reqPerm = to.meta.permission
            const employeeZone = ['dashboard', 'attendances', 'my-leaves', 'shifts', 'profile']
            const hrZone = ['departments', 'positions', 'employees', 'contracts', 'contract-annexes', 'leave-approvals', 'leave-types', 'salaries', 'notifications']

            let hasRoleAccess = false;
            if (employeeZone.includes(reqPerm)) hasRoleAccess = true;
            else if (hrZone.includes(reqPerm) && isHR) hasRoleAccess = true;

            if (!hasRoleAccess && !userPermissions.includes(reqPerm)) {
                return { path: "/403" }
            }
        }
    }
    return true
})

// 👉 VÀ CHỐT SỔ FILE BẰNG DÒNG NÀY LÀ XONG:
export default router