import http from './http'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),
  forgotPassword: (data) => http.post('/auth/forgot-password', data),
  verifyOtp: (data) => http.post('/auth/verify-otp', data),
  resetPassword: (data) => http.post('/auth/reset-password', data),
}

export const profileApi = {
  get: () => http.get('/profile'),
  updateEmail: (data) => http.patch('/profile/email', data),
  changePassword: (data) => http.patch('/profile/password', data),
}

export const employeeApi = {
  list: (params) => http.get('/employees', { params }),
  get: (id) => http.get(`/employees/${id}`),
  create: (data) => http.post('/employees', data),
  update: (id, data) => http.put(`/employees/${id}`, data),
  delete: (id) => http.delete(`/employees/${id}`),
}

export const taskApi = {
  list: (params) => http.get('/tasks', { params }),
  get: (id) => http.get(`/tasks/${id}`),
  create: (data) => http.post('/tasks', data),
  update: (id, data) => http.put(`/tasks/${id}`, data),
  updateStatus: (id, status) => http.patch(`/tasks/${id}/status`, { status }),
  delete: (id) => http.delete(`/tasks/${id}`),
  uploadAttachments: (id, files) => {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return http.post(`/tasks/${id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  deleteAttachment: (taskId, attachmentId) => http.delete(`/tasks/${taskId}/attachments/${attachmentId}`),
  downloadAttachment: (attachmentId) => http.get(`/tasks/attachments/${attachmentId}/download`, {
    responseType: 'blob',
  }),
}

export const leaveRequestApi = {
  list: (params) => http.get('/leave-requests', { params }),
  get: (id) => http.get(`/leave-requests/${id}`),
  create: (data) => http.post('/leave-requests', data),
  update: (id, data) => http.put(`/leave-requests/${id}`, data),
  cancel: (id) => http.patch(`/leave-requests/${id}/cancel`),
  approve: (id, data = {}) => http.patch(`/leave-requests/${id}/approve`, data),
  reject: (id, data = {}) => http.patch(`/leave-requests/${id}/reject`, data),
}

export const departmentApi = {
  list: (params) => http.get('/departments', { params }),
  get: (id) => http.get(`/departments/${id}`),
  create: (data) => http.post('/departments', data),
  update: (id, data) => http.put(`/departments/${id}`, data),
  delete: (id) => http.delete(`/departments/${id}`),
}

export const positionApi = {
  list: (params) => http.get('/positions', { params }),
  get: (id) => http.get(`/positions/${id}`),
  create: (data) => http.post('/positions', data),
  update: (id, data) => http.put(`/positions/${id}`, data),
  delete: (id) => http.delete(`/positions/${id}`),
}

export const userApi = {
  list: (params) => http.get('/users', { params }),
  get: (id) => http.get(`/users/${id}`),
  create: (data) => http.post('/users', data),
  update: (id, data) => http.put(`/users/${id}`, data),
  delete: (id) => http.delete(`/users/${id}`),
}

export const roleApi = {
  list: (params) => http.get('/roles', { params }),
  get: (id) => http.get(`/roles/${id}`),
  create: (data) => http.post('/roles', data),
  update: (id, data) => http.put(`/roles/${id}`, data),
  delete: (id) => http.delete(`/roles/${id}`),
  permissions: () => http.get('/permissions'),
}

export const notificationApi = {
  list: (params) => http.get('/notifications', { params }),
  unreadCount: () => http.get('/notifications/unread-count'),
  create: (data) => http.post('/notifications', data),
  markAsRead: (id) => http.patch(`/notifications/${id}/read`),
  markAllAsRead: () => http.patch('/notifications/read-all'),
  delete: (id) => http.delete(`/notifications/${id}`),
}

export const dashboardApi = {
  stats: () => http.get('/dashboard/stats'),
}

export const systemLogApi = {
  list: (params) => http.get('/system-logs', { params }),
  modules: () => http.get('/system-logs/modules'),
}

export const attendanceApi = {
  list: (params) => http.get('/attendance', { params }),
  my: (params) => http.get('/attendance/me', { params }),
  today: () => http.get('/attendance/me/today'),
  checkIn: (data) => http.post('/attendance/check-in', data),
  checkOut: (data) => http.post('/attendance/check-out', data),
}

export const workScheduleApi = {
  list: (params) => http.get('/work-schedules', { params }),
  my: (params) => http.get('/work-schedules/me', { params }),
  today: () => http.get('/work-schedules/me/today'),
  create: (data) => http.post('/work-schedules', data),
  bulkCreate: (data) => http.post('/work-schedules/bulk', data),
  update: (id, data) => http.put(`/work-schedules/${id}`, data),
  delete: (id) => http.delete(`/work-schedules/${id}`),
}

export const shiftApi = {
  list: (params) => http.get('/shifts', { params }),
  get: (id) => http.get(`/shifts/${id}`),
  create: (data) => http.post('/shifts', data),
  update: (id, data) => http.put(`/shifts/${id}`, data),
  delete: (id) => http.delete(`/shifts/${id}`),
}

export const workLocationApi = {
  list: (params) => http.get('/work-locations', { params }),
  get: (id) => http.get(`/work-locations/${id}`),
  create: (data) => http.post('/work-locations', data),
  update: (id, data) => http.put(`/work-locations/${id}`, data),
  delete: (id) => http.delete(`/work-locations/${id}`),
}

export const contractApi = {
  list: (params) => http.get('/contracts', { params }),
  get: (id) => http.get(`/contracts/${id}`),
  create: (data) => http.post('/contracts', data),
  update: (id, data) => http.put(`/contracts/${id}`, data),
  delete: (id) => http.delete(`/contracts/${id}`),
  expiring: (params) => http.get('/contracts/expiring', { params }),
  activate: (id) => http.patch(`/contracts/${id}/activate`),
  terminate: (id, data) => http.patch(`/contracts/${id}/terminate`, data),
}

export const payrollApi = {
  listPeriods: (params) => http.get('/payrolls/periods', { params }),
  getPeriod: (id) => http.get(`/payrolls/periods/${id}`),
  createPeriod: (data) => http.post('/payrolls/periods', data),
  updatePeriod: (id, data) => http.put(`/payrolls/periods/${id}`, data),
  deletePeriod: (id) => http.delete(`/payrolls/periods/${id}`),
  generate: (id) => http.post(`/payrolls/periods/${id}/generate`),
  recalculate: (id) => http.post(`/payrolls/periods/${id}/recalculate`),
  approve: (id) => http.post(`/payrolls/periods/${id}/approve`),
  markPaid: (id) => http.post(`/payrolls/periods/${id}/pay`),
  list: (params) => http.get('/payrolls', { params }),
  my: () => http.get('/payrolls/me'),
  get: (id) => http.get(`/payrolls/${id}`),
  adjust: (id, data) => http.put(`/payrolls/${id}`, data),
}

export const payrollSettingApi = {
  get: () => http.get('/payroll-settings'),
  update: (data) => http.patch('/payroll-settings', data),
  reset: () => http.post('/payroll-settings/reset'),
}
