import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth.store'

import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

// Auth pages
import Login from "@/views/auth/Login.vue"
import ForgotPassword from "@/views/auth/ForgotPassword.vue"
import VerifyOTP from "@/views/auth/VerifyOTP.vue"
import ResetPassword from "@/views/auth/ResetPassword.vue"
import Dashboard from "@/views/main/Dashboard.vue";
import UserManagement from "@/views/main/UserManagement.vue";
import RoleManagement from "@/views/main/RoleManagement.vue";
import DepartmentManagement from "@/views/main/DepartmentManagement.vue";
import PositionManagement from "@/views/main/PositionManagement.vue";
import EmployeeManagement from "@/views/main/EmployeeManagement.vue";
import Schedules from "@/views/main/Schedules.vue";
import LeaveTypes from "@/views/main/LeaveTypes.vue";
import LeaveRequest from "@/views/main/LeaveRequest.vue";
import LeaveManagement from "@/views/main/LeaveManagement.vue";
import ContractAnnex from "@/views/main/ContractAnnex.vue";
import Contract from "@/views/main/Contract.vue";
import AttendanceManagement from "@/views/main/AttendanceManagement.vue";
import Profile from "@/views/main/Profile.vue";
import Mytask from "@/views/main/MyTaskManagement.vue";
import TaskListManagement from "@/views/main/TaskListManagement.vue";
import KpiRanking from "@/views/main/KpiRanking.vue";
import Salaries from "@/views/main/Salaries.vue";
import PayrollBatch from "@/views/main/PayrollBatch.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [

        // ── Auth ─────────────────────────────────────────────
        {
            path: '/auth',
            component: AuthLayout,
            children: [
                {
                    path: 'login',
                    name: 'login',
                    component: Login,
                    meta: {title: 'Login'}
                },
                {
                    path: 'forgot-password',
                    name: 'forgot-password',
                    component: ForgotPassword,
                    meta: {title: 'Forgot Password'}
                },
                {
                    path: 'verify-otp',
                    name: 'verify-otp',
                    component: VerifyOTP,
                    meta: {title: 'Verify OTP'}
                },
                {
                    path: 'reset-password',
                    name: 'reset-password',
                    component: ResetPassword,
                    meta: {title: 'Reset Password'}
                }
            ]
        },

        // ── Main app ─────────────────────────────────────────
        {
            path: '/',
            component: MainLayout,
            meta: {requiresAuth: true},
            children: [
                {
                    path: '',
                    component: Dashboard,
                    meta: {title: 'Dashboard'}
                },
                {
                    path: 'users',
                    component: UserManagement,
                    meta: {title: 'Users Management'}
                },
                {
                    path: 'roles',
                    component: RoleManagement,
                    meta: {title: 'Roles Management'}
                },
                {
                    path: 'departments',
                    component: DepartmentManagement,
                    meta: {title: 'Departments Management'}
                },
                {
                    path: 'positions',
                    component: PositionManagement,
                    meta: {title: 'Positions Management'}
                },
                {
                    path: 'employees',
                    component: EmployeeManagement,
                    meta: {title: 'Employees Management'}
                },
                {
                    path: 'leaves',
                    component: LeaveRequest,
                    meta: {title: 'Leaves Request'}
                },
                {
                    path: 'leaves-manager',
                    component: LeaveManagement,
                    meta: {title: 'Leaves Management'}
                },
                {
                    path: 'leave-types',
                    component: LeaveTypes,
                    meta: {title: 'Leaves Types'}
                },
                {
                    path: 'schedules',
                    component: Schedules,
                    meta: {title: 'Schedules Management'}
                },
                {
                    path: 'contract-annexes/:id',
                    name: 'ContractAnnex',
                    component: ContractAnnex,
                    meta: {title: 'Contract Annex'}
                },
                {
                    path: 'contracts',
                    component: Contract,
                    meta: {title: 'Contract'}
                },
                {
                    path: 'attendances',
                    component: AttendanceManagement,
                    meta: {title: 'Attendance Management'}
                },
                {
                    path: 'profile',
                    component: Profile,
                    meta: {title: 'Profile'}
                },
                {
                    path: 'task-management',
                    component: TaskListManagement,
                    meta: {title: 'Task Management'}
                },
                {
                    path: 'my-task',
                    component: Mytask,
                    meta: {title: 'My Task'}
                },
                {
                    path: 'kpi-ranking',
                    component: KpiRanking,
                    meta: {title: 'KPI Ranking'}
                },
                {
                    path: 'salaries',
                    component: Salaries,
                    meta: {title: 'Salaries Management'}
                },
                {
                    path: 'payrollbatch',
                    component: PayrollBatch,
                    meta: {title: 'Payroll Batch'}
                }



                // Ví dụ thêm
                // {
                //     path: 'users',
                //     component: () => import('@/views/main/UserManagement.vue'),
                //     meta: { title: 'Users', permission: 'USER_VIEW' }
                // }
            ]
        },

        // ── Error ────────────────────────────────────────────
        {
            path: '/403',
            component: () => import('@/views/errors/Forbidden.vue'),
            meta: {title: 'Forbidden'}
        },

        {path: '/:pathMatch(.*)*', redirect: '/'}
    ]
})


// ── Navigation Guard ──────────────────────────────────────
router.beforeEach(async (to) => {
    const token = localStorage.getItem('token')
    const auth = useAuthStore()

    // Chưa login → login
    if (to.meta.requiresAuth && !token) return '/auth/login'

    // Đã login → không cho vào auth
    if (to.path.startsWith('/auth') && token) return '/'

    // Luôn đồng bộ user + permissions từ DB mỗi khi navigate
    // → đảm bảo sidebar/quyền luôn phản ánh đúng DB (kể cả khi admin vừa đổi quyền)
    if (token) await auth.fetchMe()

    // Check permission
    if (to.meta.permission && !auth.can(to.meta.permission)) return '/403'
})


// ── Set TITLE (KHÔNG đổi favicon) ─────────────────────────
router.afterEach((to) => {
    document.title = to.meta.title
        ? `${to.meta.title} | Vantix HRM`
        : 'Vantix HRM'
})

export default router