export const menuItems = [
    {
        section: "Menu",
        key: "dashboard",
        label: "Dashboard",
        icon: "bi bi-grid",
        to: "/dashboard"
    },
    {
        section: "Menu",
        key: "user",
        label: "User",
        icon: "bi bi-person",
        children: [
            { label: "User List", to: "/users" },
            { label: "Role", to: "/roles" }
        ]
    },
    {
        section: "Menu",
        key: "employees",
        label: "Employees",
        icon: "bi bi-people",
        to: "/employees"
    },
    {
        section: "Menu",
        key: "department",
        label: "Department",
        icon: "bi bi-diagram-3",
        to: "/departments"
    },
    {
        section: "Other",
        key: "settings",
        label: "Settings",
        icon: "bi bi-gear",
        to: "/settings"
    }
]