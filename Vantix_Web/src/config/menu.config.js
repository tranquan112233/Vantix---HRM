/*
 * menu.config.js
 * ─────────────────────────────────────────────────────────────────────────
 * Cấu hình menu sidebar theo section
 *
 * Cấu trúc item:
 *   { label, icon, to, permission? }          ← item đơn
 *   { label, icon, children: [...items] }     ← group có submenu
 *
 * Lưu ý:
 *   - Không có "permission" → mọi user đều thấy
 *   - Tên permission phải KHỚP CHÍNH XÁC với DB (Permission.name)
 * ─────────────────────────────────────────────────────────────────────────
 */

export const menuSections = [
    {
        section: 'DASHBOARD',
        items: [
            { label: 'Dashboard', icon: 'bi-grid-1x2', to: '/' },
        ]
    },
    {
        section: 'USER & ACCESS',
        items: [
            {
                label: 'Users', icon: 'bi-person',
                children: [
                    { label: 'User List',   icon: 'bi-people',       to: '/users',       permission: 'USER_VIEW'       },
                    { label: 'Roles',       icon: 'bi-shield-check', to: '/roles',       permission: 'ROLE_VIEW'       },
                ]
            },
        ]
    },
    {
        section: 'ORGANIZATION',
        items: [
            { label: 'Departments', icon: 'bi-diagram-3',    to: '/departments', permission: 'DEPARTMENT_VIEW' },
            { label: 'Positions',   icon: 'bi-person-badge', to: '/positions',   permission: 'POSITION_VIEW'   },
        ]
    },
    {
        section: 'EMPLOYEE MANAGEMENT',
        items: [
            { label: 'Employees',        icon: 'bi-people',    to: '/employees',        permission: 'EMPLOYEE_VIEW' },
            { label: 'Contracts',        icon: 'bi-file-text', to: '/contracts',        permission: 'CONTRACT_VIEW' },
            { label: 'Contract Annexes', icon: 'bi-file-plus', to: '/contract-annexes', permission: 'CONTRACT_VIEW' },
        ]
    },
    {
        section: 'ATTENDANCE & LEAVE',
        items: [
            { label: 'Attendances', icon: 'bi-calendar-check', to: '/attendance-management', permission: 'ATTENDANCE_VIEW' },
            { label: 'Shifts',      icon: 'bi-clock',          to: '/shifts',     permission: 'SHIFT_VIEW'      },
            {
                label: 'Leave', icon: 'bi-box-arrow-right',
                children: [
                    { label: 'Leave Requests', icon: 'bi-send',       to: '/leaves', permission: 'LEAVE_VIEW'   },
                    { label: 'Leave Manager',  icon: 'bi-inbox',      to: '/leave-manager',  permission: 'LEAVE_MANAGE' },
                    { label: 'Leave Types',    icon: 'bi-list-check', to: '/leave-types',    permission: 'LEAVE_VIEW'   },
                ]
            },
        ]
    },
    {
        section: 'PAYROLL',
        items: [
            { label: 'Salaries', icon: 'bi-cash-coin', to: '/salaries', permission: 'SALARY_VIEW' },
        ]
    },
    {
        section: 'OTHERS',
        items: [
            { label: 'Settings', icon: 'bi-setting', to: '/settings'},
            { label: 'Schedules', icon: 'bi-setting', to: '/schedules'},
        ]
    },
]
