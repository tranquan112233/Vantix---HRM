export const menuItems = [
    {
        section: "Menu",
        key: "dashboard",
        label: "Dashboard",
        icon: "bi bi-grid",
        to: "/dashboard",
        roles: ["ADMIN", "HR", "EMPLOYEE"]
    },
    {
        section: "Menu",
        key: "user",
        label: "User",
        icon: "bi bi-person",
        roles: ["ADMIN"],
        children: [
            { label: "User List", to: "/users", roles: ["ADMIN"] },
            { label: "Role", to: "/roles", roles: ["ADMIN"] }
        ]
    },
    {
        section: "Menu",
        key: "employees",
        label: "Employees",
        icon: "bi bi-people",
        to: "/employees",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Menu",
        key: "department",
        label: "Department",
        icon: "bi bi-diagram-3",
        to: "/departments",
        roles: ["ADMIN", "HR"]
    },
    {
        section: "Other",
        key: "settings",
        label: "Settings",
        icon: "bi bi-gear",
        to: "/settings"
    }
]