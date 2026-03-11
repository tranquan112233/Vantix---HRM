export const menuItems = [

    // ================= DASHBOARD =================
    {
        section: "Dashboard",
        key: "dashboard",
        label: "Dashboard",
        icon: "bi bi-grid",
        to: "/dashboard"
    },

    // ================= USER & ACCESS =================
    {
        section: "User & Access",
        key: "users_access", // Key của menu cha
        label: "Users",
        icon: "bi bi-person",
        children: [
            {
                key: "users", // PHẢI CÓ KEY ĐỂ LƯU VÀO DB
                label: "User List",
                to: "/users"
            },
            {
                key: "roles", // PHẢI CÓ KEY ĐỂ LƯU VÀO DB
                label: "Roles",
                to: "/roles"
            }
        ]
    },

    // ================= ORGANIZATION =================
    {
        section: "Organization",
        key: "departments",
        label: "Departments",
        icon: "bi bi-diagram-3",
        to: "/departments"
    },
    {
        section: "Organization",
        key: "positions",
        label: "Positions",
        icon: "bi bi-award",
        to: "/positions"
    },

    // ================= EMPLOYEE MANAGEMENT =================
    {
        section: "Employee Management",
        key: "employees",
        label: "Employees",
        icon: "bi bi-people",
        to: "/employees"
    },
    {
        section: "Employee Management",
        key: "contracts",
        label: "Contracts",
        icon: "bi bi-file-earmark-text",
        to: "/contracts"
    },
    {
        section: "Employee Management",
        key: "contract_annex",
        label: "Contract Annexes",
        icon: "bi bi-file-earmark-plus",
        to: "/contract-annexes"
    },

    // ================= ATTENDANCE & LEAVE =================
    {
        section: "Attendance & Leave",
        key: "attendances",
        label: "Attendances",
        icon: "bi bi-calendar-check",
        to: "/attendances"
    },
    {
        section: "Attendance & Leave",
        key: "shifts",
        label: "Shifts",
        icon: "bi bi-clock",
        to: "/shifts"
    },
    {
        section: "Attendance & Leave",
        key: "leave", // Tách riêng key cho trang duyệt phép của HR
        label: "Leave Approvals (HR)",
        icon: "bi bi-calendar-x",
        to: "/leave"
    },
    {
        section: "Attendance & Leave",
        key: "leaves", // Tách riêng key cho trang xin phép của cá nhân
        label: "My Leave Requests",
        icon: "bi bi-calendar-x",
        to: "/leaves"
    },
    {
        section: "Attendance & Leave",
        key: "leave_types",
        label: "Leave Types",
        icon: "bi bi-list-check",
        to: "/leave-types"
    },

    // ================= PAYROLL =================
    {
        section: "Payroll",
        key: "salaries",
        label: "Salaries",
        icon: "bi bi-cash-stack",
        to: "/salaries"
    },

    // ================= SYSTEM =================
    {
        section: "System",
        key: "notifications",
        label: "Notifications",
        icon: "bi bi-bell",
        to: "/notifications"
    }
]