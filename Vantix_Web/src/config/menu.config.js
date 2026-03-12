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
        key: "users",
        label: "Users",
        icon: "bi bi-person",
        roles: ["ADMIN"],
        children: [
            {
                label: "User List",
                to: "/users",
                roles: ["ADMIN"]
            },
            {
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


    // ================= EMPLOYEE MANAGEMENT =================
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
        key: "schedules",
        label: "Schedules",
        icon: "bi bi-calendar-check",
        to: "/schedules",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    },
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
        key: "leave_requests",
        label: "Leave Requests Manager",
        icon: "bi bi-calendar-x",
        to: "/leaves",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
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