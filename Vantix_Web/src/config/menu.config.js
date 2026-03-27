export const menuItems = [

    // ================= DASHBOARD =================
    {
        section: "Dashboard",
        key: "dashboard",
        label: "Dashboard",
        icon: "bi bi-grid",
        to: "/dashboard",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    },

    // ================= USER & ACCESS =================
    {
        section: "User & Access",
        key: "user_management",
        label: "Users",
        icon: "bi bi-person",
        roles: ["ADMIN"],
        children: [
            {
                key: "user_list",
                label: "User List",
                to: "/users",
                roles: ["ADMIN"]
            },
            {
                key: "role_list",
                label: "Roles",
                to: "/roles",
                roles: ["ADMIN"]
            }
        ]
    },

    // ================= ORGANIZATION =================
    {
        section: "Organization",
        key: "departments",
        label: "Departments",
        icon: "bi bi-diagram-3",
        to: "/departments",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Organization",
        key: "positions",
        label: "Positions",
        icon: "bi bi-award",
        to: "/positions",
        roles: ["ADMIN", "HR"]
    },

    // ================= EMPLOYEE =================
    {
        section: "Employee Management",
        key: "employees",
        label: "Employees",
        icon: "bi bi-people",
        to: "/employees",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Employee Management",
        key: "contracts",
        label: "Contracts",
        icon: "bi bi-file-earmark-text",
        to: "/contracts",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Employee Management",
        key: "contract_annex",
        label: "Contract Annexes",
        icon: "bi bi-file-earmark-plus",
        to: "/contract-annexes",
        roles: ["ADMIN", "HR"]
    },

    // ================= ATTENDANCE =================
    {
        section: "Attendance & Leave",
        key: "attendances",
        label: "Attendances",
        icon: "bi bi-calendar-check",
        to: "/attendances",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    },
    {
        section: "Attendance & Leave",
        key: "shifts",
        label: "Shifts",
        icon: "bi bi-clock",
        to: "/shifts",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Attendance & Leave",
        key: "leave_requests",
        label: "Leave Requests",
        icon: "bi bi-calendar-x",
        to: "/leave-request",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    },
    {
        section: "Attendance & Leave",
        key: "leave_manager",
        label: "Leave Manager",
        icon: "bi bi-calendar2-check",
        to: "/leaves",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Attendance & Leave",
        key: "leave_types",
        label: "Leave Types",
        icon: "bi bi-list-check",
        to: "/leave-types",
        roles: ["ADMIN", "HR"]
    },

    // ================= PAYROLL =================
    {
        section: "Payroll",
        key: "salaries",
        label: "Salaries",
        icon: "bi bi-cash-stack",
        to: "/salaries",
        roles: ["ADMIN", "HR"]
    },

    // ================= TASK MANAGEMENT =================
    {
        section: "Task Management",
        key: "task_list",
        label: "Task List",
        icon: "bi bi-list-task",
        to: "/tasks",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Task Management",
        key: "task_assign",
        label: "Assign Task",
        icon: "bi bi-person-check",
        to: "/task-assign",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Task Management",
        key: "task_my",
        label: "My Tasks",
        icon: "bi bi-check2-square",
        to: "/my-tasks",
        roles: ["EMPLOYEE"]
    },
    {
        section: "Task Management",
        key: "task_kpi",
        label: "KPI Dashboard",
        icon: "bi bi-bar-chart",
        to: "/task-kpi",
        roles: ["ADMIN", "HR"]
    },

    // ================= SYSTEM =================
    {
        section: "System",
        key: "notifications",
        label: "Notifications",
        icon: "bi bi-bell",
        to: "/notifications",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    }

]