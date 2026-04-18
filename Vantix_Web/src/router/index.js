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
import MyTaskManagement from "@/views/main/MyTaskManagement.vue";
import Settings from "@/views/main/Settings.vue";
import Attendance from "@/views/main/Attendance.vue";
import Notification from "@/views/main/NotificationManagement.vue";
import MyNotifications from "@/views/main/MyNotifications.vue";

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
                    meta: {title: 'Đăng nhập'}
                },
                {
                    path: 'forgot-password',
                    name: 'forgot-password',
                    component: ForgotPassword,
                    meta: {title: 'Quên mật khẩu'}
                },
                {
                    path: 'verify-otp',
                    name: 'verify-otp',
                    component: VerifyOTP,
                    meta: {title: 'Xác thực OTP'}
                },
                {
                    path: 'reset-password',
                    name: 'reset-password',
                    component: ResetPassword,
                    meta: {title: 'Đặt lại mật khẩu'}
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
                    meta: {title: 'Bảng điều khiển'}
                },
                {
                    path: 'users',
                    component: UserManagement,
                    meta: {title: 'Quản lý người dùng'}
                },
                {
                    path: 'roles',
                    component: RoleManagement,
                    meta: {title: 'Quản lý vai trò'}
                },
                {
                    path: 'departments',
                    component: DepartmentManagement,
                    meta: {title: 'Quản lý phòng ban'}
                },
                {
                    path: 'positions',
                    component: PositionManagement,
                    meta: {title: 'Quản lý chức vụ'}
                },
                {
                    path: 'employees',
                    component: EmployeeManagement,
                    meta: {title: 'Quản lý nhân viên'}
                },
                {
                    path: 'leaves',
                    component: LeaveRequest,
                    meta: {title: 'Đơn nghỉ phép'}
                },
                {
                    path: 'leaves-manager',
                    component: LeaveManagement,
                    meta: {title: 'Duyệt nghỉ phép'}
                },
                {
                    path: 'leave-types',
                    component: LeaveTypes,
                    meta: {title: 'Loại nghỉ phép'}
                },
                {
                    path: 'schedules',
                    component: Schedules,
                    meta: {title: 'Quản lý lịch làm việc'}
                },
                {
                    path: 'contract-annexes/:id',
                    name: 'ContractAnnex',
                    component: ContractAnnex,
                    meta: {title: 'Phụ lục hợp đồng'}
                },
                {
                    path: 'contracts',
                    component: Contract,
                    meta: {title: 'Hợp đồng'}
                },
                {
                    path: 'attendances-management',
                    component: AttendanceManagement,
                    meta: {title: 'Duyệt chấm công'}
                },
                {
                    path: 'attendances',
                    component: Attendance,
                    meta: {title: 'Chấm công'}
                },
                {
                    path: 'profile',
                    component: Profile,
                    meta: {title: 'Hồ sơ'}
                },
                {
                    path: 'task-management',
                    component: TaskListManagement,
                    meta: {title: 'Quản lý công việc'}
                },
                {
                    path: 'my-task',
                    component: MyTaskManagement,
                    meta: {title: 'Công việc của tôi'}
                },
                {
                    path: 'kpi-ranking',
                    component: KpiRanking,
                    meta: {title: 'Xếp hạng KPI'}
                },
                {
                    path: 'salaries',
                    component: Salaries,
                    meta: {title: 'Quản lý lương'}
                },
                {
                    path: 'payrollbatch',
                    component: PayrollBatch,
                    meta: {title: 'Đợt trả lương'}
                },
                {
                    path: 'settings',
                    component: Settings,
                    meta: {title: 'Cài đặt', permission: 'SYSTEM_CONFIG'}
                },
                {
                    path: 'notifications',
                    component: Notification,
                    meta: {title: 'Thông báo'}
                },
                {
                    path: 'my-notifications',
                    component: MyNotifications,
                    meta: {title: 'Thông báo của tôi'}
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
            meta: {title: 'Không có quyền'}
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
