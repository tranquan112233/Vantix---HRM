function extractDate(text = '') {
  return text.match(/\d{4}-\d{2}-\d{2}/)?.[0] || ''
}

function legacyAttendanceKey(notification) {
  const title = notification?.title || ''
  const message = notification?.message || ''
  const combined = `${title}\n${message}`.toLowerCase()

  if (combined.includes('missing checkout') || combined.includes('forgot to check out')) {
    return {
      titleKey: 'notification.attendance.missingCheckout.title',
      messageKey: 'notification.attendance.missingCheckout.message',
      params: { date: extractDate(combined) },
    }
  }

  if (combined.includes('new make-up checkout request') || combined.includes('đơn bù check-out mới')) {
    return {
      titleKey: 'notification.makeup.created.title',
      messageKey: 'notification.makeup.created.message',
      params: {
        employee: title.split('/')[0]?.trim() || '',
        date: extractDate(combined),
      },
    }
  }

  return null
}

function quotedText(text = '') {
  return text.match(/"([^"]+)"/)?.[1] || ''
}

function allDates(text = '') {
  return text.match(/\d{4}-\d{2}-\d{2}/g) || []
}

function leadingName(text = '') {
  return text.split(/\s+(đã|has|submitted|cancelled)/, 1)[0]?.trim() || ''
}

function legacyLeaveKey(notification) {
  const title = notification?.title || ''
  const message = notification?.message || ''
  const combined = `${title}\n${message}`.toLowerCase()
  const dates = allDates(combined)
  const params = {
    employee: leadingName(message),
    startDate: dates[0] || '',
    endDate: dates[1] || '',
    decision: combined.includes('approved') || combined.includes('được duyệt')
      ? 'notification.decision.approved'
      : 'notification.decision.rejected',
  }

  if (combined.includes('new leave request') || combined.includes('đơn xin nghỉ mới')) {
    return { titleKey: 'notification.leave.created.title', messageKey: 'notification.leave.created.message', params }
  }
  if (combined.includes('cancelled') || combined.includes('bị hủy')) {
    return { titleKey: 'notification.leave.cancelled.title', messageKey: 'notification.leave.cancelled.message', params }
  }
  if (combined.includes('approved') || combined.includes('được duyệt')) {
    return { titleKey: 'notification.leave.approved.title', messageKey: 'notification.leave.decision.message', params }
  }
  if (combined.includes('rejected') || combined.includes('bị từ chối')) {
    return { titleKey: 'notification.leave.rejected.title', messageKey: 'notification.leave.decision.message', params }
  }

  return null
}

function legacyTaskKey(notification) {
  const title = notification?.title || ''
  const message = notification?.message || ''
  const combined = `${title}\n${message}`.toLowerCase()
  const dates = allDates(combined)
  const params = {
    task: quotedText(message),
    dueDate: dates[0] || '',
    days: combined.match(/(\d+)\s+(ngày|day)/)?.[1] || '',
  }

  if (combined.includes('due today') || combined.includes('đến hạn hôm nay')) {
    return { titleKey: 'notification.task.dueToday.title', messageKey: 'notification.task.dueToday.message', params }
  }
  if (combined.includes('overdue') || combined.includes('quá hạn')) {
    return { titleKey: 'notification.task.overdue.title', messageKey: 'notification.task.overdue.message', params }
  }
  if (combined.includes('assigned') || combined.includes('mới được giao')) {
    return {
      titleKey: 'notification.task.assigned.title',
      messageKey: params.dueDate ? 'notification.task.assignedWithDue.message' : 'notification.task.assigned.message',
      params,
    }
  }
  if (combined.includes('updated') || combined.includes('cập nhật')) {
    return { titleKey: 'notification.task.updated.title', messageKey: 'notification.task.updated.message', params }
  }
  if (combined.includes('completed') || combined.includes('hoàn thành')) {
    return { titleKey: 'notification.task.done.title', messageKey: 'notification.task.done.message', params }
  }
  if (combined.includes('reopened') || combined.includes('mở lại')) {
    return { titleKey: 'notification.task.reopened.title', messageKey: 'notification.task.reopened.message', params }
  }

  return null
}

function legacyKey(notification) {
  if (notification?.type === 'ATTENDANCE') return legacyAttendanceKey(notification)
  if (notification?.type === 'LEAVE') return legacyLeaveKey(notification)
  if (notification?.type === 'TASK') return legacyTaskKey(notification)
  return null
}

export function notificationTitle(notification, settings) {
  if (notification?.titleKey) {
    return settings.t(notification.titleKey, notification.messageParams)
  }

  const legacy = legacyKey(notification)
  return legacy ? settings.t(legacy.titleKey, legacy.params) : notification?.title
}

export function notificationMessage(notification, settings) {
  if (notification?.messageKey) {
    return settings.t(notification.messageKey, notification.messageParams)
  }

  const legacy = legacyKey(notification)
  return legacy ? settings.t(legacy.messageKey, legacy.params) : notification?.message
}
