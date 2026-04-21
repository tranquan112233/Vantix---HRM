export const menuConfig = [
  {
    type: 'group',
    name: 'main',
    label: 'Main',
    labelKey: 'menu.main',
    children: [
      {
        path: '/',
        name: 'Dashboard',
        label: 'Dashboard',
        labelKey: 'menu.dashboard',
        title: 'Dashboard',
        titleKey: 'menu.dashboard',
        icon: 'DataAnalysis',
        component: () => import('@/views/Dashboard.vue'),
      },
      {
        path: '/profile',
        name: 'Profile',
        label: 'Profile',
        labelKey: 'menu.profile',
        title: 'Profile',
        titleKey: 'menu.profile',
        icon: 'User',
        hidden: true,
        component: () => import('@/views/Profile.vue'),
      },
    ],
  },
  {
    type: 'group',
    name: 'workforce',
    label: 'Workforce',
    labelKey: 'menu.workforce',
    children: [
      {
        type: 'submenu',
        name: 'PeopleManagement',
        label: 'People',
        labelKey: 'menu.peopleManagement',
        icon: 'User',
        children: [
          {
            path: '/employees',
            name: 'Employees',
            label: 'Employees',
            labelKey: 'menu.employees',
            title: 'Employees',
            titleKey: 'menu.employees',
            icon: 'User',
            permissions: ['EMPLOYEE_VIEW'],
            component: () => import('@/views/Employees.vue'),
          },
          {
            path: '/departments',
            name: 'Departments',
            label: 'Departments',
            labelKey: 'menu.departments',
            title: 'Departments',
            titleKey: 'menu.departments',
            icon: 'OfficeBuilding',
            permissions: ['DEPARTMENT_VIEW'],
            component: () => import('@/views/Departments.vue'),
          },
          {
            path: '/positions',
            name: 'Positions',
            label: 'Positions',
            labelKey: 'menu.positions',
            title: 'Positions',
            titleKey: 'menu.positions',
            icon: 'Medal',
            permissions: ['POSITION_VIEW'],
            component: () => import('@/views/Positions.vue'),
          },
        ],
      },
      {
        type: 'submenu',
        name: 'Operations',
        label: 'Operations',
        labelKey: 'menu.operations',
        icon: 'Calendar',
        children: [
          {
            path: '/tasks',
            name: 'Tasks',
            label: 'Tasks',
            labelKey: 'menu.tasks',
            title: 'Tasks',
            titleKey: 'menu.tasks',
            icon: 'Tickets',
            permissions: ['TASK_VIEW'],
            component: () => import('@/views/Tasks.vue'),
          },
          {
            path: '/attendance',
            name: 'Attendance',
            label: 'Attendance',
            labelKey: 'menu.attendance',
            title: 'Attendance',
            titleKey: 'menu.attendance',
            icon: 'Clock',
            component: () => import('@/views/Attendance.vue'),
          },
          {
            path: '/work-schedules',
            name: 'WorkSchedules',
            label: 'Work Schedules',
            labelKey: 'menu.workSchedules',
            title: 'Work Schedules',
            titleKey: 'menu.workSchedules',
            icon: 'Calendar',
            permissions: [
              'SCHEDULE_VIEW_ALL',
              'SCHEDULE_CREATE',
              'SCHEDULE_UPDATE',
              'SCHEDULE_DELETE',
              'SHIFT_VIEW',
              'WORK_LOCATION_VIEW',
            ],
            permissionMode: 'any',
            component: () => import('@/views/WorkSchedules.vue'),
          },
          {
            path: '/leave-requests',
            name: 'LeaveRequests',
            label: 'Leave Requests',
            labelKey: 'menu.leaveRequests',
            title: 'Leave Requests',
            titleKey: 'menu.leaveRequests',
            icon: 'Calendar',
            permissions: [
              'LEAVE_REQUEST_VIEW',
              'LEAVE_REQUEST_CREATE',
              'LEAVE_REQUEST_UPDATE',
              'LEAVE_REQUEST_CANCEL',
              'LEAVE_REQUEST_VIEW_ALL',
              'LEAVE_REQUEST_APPROVE',
              'LEAVE_REQUEST_MANAGE',
            ],
            permissionMode: 'any',
            component: () => import('@/views/LeaveRequests.vue'),
          },
        ],
      },
      {
        type: 'submenu',
        name: 'Compensation',
        label: 'Compensation',
        labelKey: 'menu.compensation',
        icon: 'Money',
        children: [
          {
            path: '/contracts',
            name: 'Contracts',
            label: 'Contracts',
            labelKey: 'menu.contracts',
            title: 'Contracts',
            titleKey: 'menu.contracts',
            icon: 'Document',
            permissions: ['CONTRACT_VIEW'],
            component: () => import('@/views/Contracts.vue'),
          },
          {
            path: '/payrolls',
            name: 'Payrolls',
            label: 'Payrolls',
            labelKey: 'menu.payrolls',
            title: 'Payrolls',
            titleKey: 'menu.payrolls',
            icon: 'Money',
            permissions: ['PAYROLL_VIEW', 'PAYROLL_MANAGE'],
            permissionMode: 'any',
            component: () => import('@/views/Payrolls.vue'),
          },
        ],
      },
    ],
  },
  {
    type: 'group',
    name: 'administration',
    label: 'Administration',
    labelKey: 'menu.administration',
    children: [
      {
        type: 'submenu',
        name: 'AccessControl',
        label: 'Access Control',
        labelKey: 'menu.accessControl',
        icon: 'Lock',
        children: [
          {
            path: '/users',
            name: 'Users',
            label: 'Users',
            labelKey: 'menu.users',
            title: 'Users',
            titleKey: 'menu.users',
            icon: 'UserFilled',
            permissions: ['USER_VIEW'],
            component: () => import('@/views/Users.vue'),
          },
          {
            path: '/roles',
            name: 'Roles',
            label: 'Roles',
            labelKey: 'menu.roles',
            title: 'Roles',
            titleKey: 'menu.roles',
            icon: 'Lock',
            permissions: ['ROLE_VIEW'],
            component: () => import('@/views/Roles.vue'),
          },
        ],
      },
      {
        type: 'submenu',
        name: 'SystemTools',
        label: 'System Tools',
        labelKey: 'menu.systemTools',
        icon: 'Setting',
        children: [
          {
            path: '/system-logs',
            name: 'SystemLogs',
            label: 'System Logs',
            labelKey: 'menu.systemLogs',
            title: 'System Logs',
            titleKey: 'menu.systemLogs',
            icon: 'Document',
            permissions: ['SYSTEM_LOG_VIEW'],
            component: () => import('@/views/SystemLogs.vue'),
          },
          {
            path: '/settings',
            name: 'Settings',
            label: 'Settings',
            labelKey: 'menu.settings',
            title: 'Settings',
            titleKey: 'menu.settings',
            icon: 'Setting',
            component: () => import('@/views/Settings.vue'),
          },
          {
            path: '/payroll-settings',
            name: 'PayrollSettings',
            label: 'Payroll Settings',
            labelKey: 'menu.payrollSettings',
            title: 'Payroll Settings',
            titleKey: 'menu.payrollSettings',
            icon: 'Money',
            permissions: ['PAYROLL_MANAGE'],
            component: () => import('@/views/PayrollSettings.vue'),
          },
        ],
      },
    ],
  },
  {
    type: 'group',
    name: 'communication',
    label: 'Communication',
    labelKey: 'menu.communication',
    children: [
      {
        path: '/notifications',
        name: 'Notifications',
        label: 'Notifications',
        labelKey: 'menu.notifications',
        title: 'Notifications',
        titleKey: 'menu.notifications',
        icon: 'Bell',
        permissions: ['NOTIFICATION_VIEW'],
        component: () => import('@/views/Notifications.vue'),
      },
    ],
  },
]

export function canAccessPermissions(requiredPermissions = [], userPermissions = [], mode = 'all') {
  if (!requiredPermissions.length) {
    return true
  }

  const permissionSet = new Set(userPermissions || [])

  if (mode === 'any') {
    return requiredPermissions.some(permission => permissionSet.has(permission))
  }

  return requiredPermissions.every(permission => permissionSet.has(permission))
}

export function canAccessMenuItem(item, userPermissions = []) {
  if (item.hidden) {
    return false
  }

  if (!canAccessPermissions(item.permissions, userPermissions, item.permissionMode)) {
    return false
  }

  if (item.children?.length) {
    return item.children.some(child => canAccessMenuItem(child, userPermissions))
  }

  return canAccessPermissions(item.permissions, userPermissions, item.permissionMode)
}

export function filterMenuByPermissions(items = menuConfig, userPermissions = []) {
  return items
    .map(item => {
      if (!item.children?.length) {
        return canAccessMenuItem(item, userPermissions) ? item : null
      }

      const children = filterMenuByPermissions(item.children, userPermissions)
      if (!children.length) {
        return null
      }

      return { ...item, children }
    })
    .filter(Boolean)
}

export function flattenMenuItems(items = menuConfig) {
  return items.flatMap(item => [
    item,
    ...(item.children?.length ? flattenMenuItems(item.children) : []),
  ])
}

export function findFirstAccessiblePath(items = menuConfig, userPermissions = []) {
  for (const item of items) {
    if (item.children?.length) {
      const childPath = findFirstAccessiblePath(item.children, userPermissions)
      if (childPath) {
        return childPath
      }
    }

    if (item.path && canAccessMenuItem(item, userPermissions)) {
      return item.path
    }
  }

  return '/'
}

export function buildRouteRecords(items = menuConfig) {
  return flattenMenuItems(items)
    .filter(item => item.path && item.component)
    .map(item => ({
      path: item.path === '/' ? '' : item.path.replace(/^\//, ''),
      name: item.name,
      component: item.component,
      meta: {
        title: item.title || item.label,
        titleKey: item.titleKey || item.labelKey,
        permissions: item.permissions || [],
        permissionMode: item.permissionMode || 'all',
      },
    }))
}
