export const menuSections = [
    {
        section: 'TỔNG QUAN',
        items: [
            {label: 'Bảng điều khiển', icon: 'bi-grid-1x2', to: '/'},
            {label: 'Thông báo của tôi', icon: 'bi-bell', to: '/my-notifications'},
        ]
    },
    {
        section: 'CÔNG VIỆC',
        items: [
            {
                label: 'Công việc',
                icon: 'bi-clipboard-data',
                children: [
                    {label: 'Quản lý công việc', icon: 'bi-kanban', to: '/task-management'},
                    {label: 'Công việc của tôi', icon: 'bi-person-workspace', to: '/my-task'},
                    {label: 'Xếp hạng KPI', icon: 'bi-trophy', to: '/kpi-ranking'},
                ]
            },
            {label: 'Lịch làm việc', icon: 'bi-calendar3', to: '/schedules', permission: 'SCHEDULE_VIEW'},
        ]
    },
    {
        section: 'NHÂN SỰ',
        items: [
            {
                label: 'Tổ chức',
                icon: 'bi-diagram-3',
                children: [
                    {label: 'Phòng ban', icon: 'bi-building', to: '/departments', permission: 'DEPARTMENT_VIEW'},
                    {label: 'Chức vụ', icon: 'bi-person-badge', to: '/positions', permission: 'POSITION_VIEW'},
                ]
            },
            {
                label: 'Nhân viên',
                icon: 'bi-people',
                children: [
                    {label: 'Danh sách nhân viên', icon: 'bi-people', to: '/employees', permission: 'EMPLOYEE_VIEW'},
                    {label: 'Hợp đồng', icon: 'bi-file-text', to: '/contracts', permission: 'CONTRACT_VIEW'},
                ]
            },
        ]
    },
    {
        section: 'THỜI GIAN & NGHỈ PHÉP',
        items: [
            {
                label: 'Chấm công',
                icon: 'bi-calendar-check',
                children: [
                    {label: 'Chấm công của tôi', icon: 'bi-calendar-check', to: '/attendances', permission: 'ATTENDANCE_VIEW'},
                    {label: 'Duyệt chấm công', icon: 'bi-check2-square', to: '/attendances-management', permission: 'ATTENDANCE_MANAGEMENT_VIEW'},
                ]
            },
            {
                label: 'Nghỉ phép',
                icon: 'bi-send',
                children: [
                    {label: 'Đơn nghỉ phép', icon: 'bi-send', to: '/leaves', permission: 'LEAVE_VIEW'},
                    {label: 'Duyệt nghỉ phép', icon: 'bi-inbox', to: '/leaves-manager', permission: 'LEAVE_MANAGE'},
                    {label: 'Loại nghỉ phép', icon: 'bi-list-check', to: '/leave-types', permission: 'LEAVE_TYPE_VIEW'},
                ]
            },
        ]
    },
    {
        section: 'LƯƠNG',
        items: [
            {
                label: 'Tiền lương',
                icon: 'bi-cash-coin',
                children: [
                    {label: 'Bảng lương', icon: 'bi-cash-stack', to: '/salaries', permission: 'SALARY_VIEW'},
                    {label: 'Đợt trả lương', icon: 'bi-receipt', to: '/payrollbatch', permission: 'SALARY_VIEW'},
                ]
            },
        ]
    },
    {
        section: 'QUẢN TRỊ',
        items: [
            {
                label: 'Phân quyền',
                icon: 'bi-shield-lock',
                children: [
                    {label: 'Người dùng', icon: 'bi-person', to: '/users', permission: 'USER_VIEW'},
                    {label: 'Vai trò', icon: 'bi-shield-check', to: '/roles', permission: 'ROLE_VIEW'},
                ]
            },
            {label: 'Thông báo', icon: 'bi-megaphone', to: '/notifications'},
            {label: 'Cài đặt', icon: 'bi-gear', to: '/settings', permission: 'SYSTEM_CONFIG'},
        ]
    },
]
