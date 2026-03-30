<template>
  <div class="admin-leave-wrapper">
    <div class="leave-card">
      <div class="card-header flex-between">
        <div class="header-title">
          <h2>👨‍💼 Quản lý Đơn xin nghỉ phép</h2>
          <p class="subtitle">Danh sách các đơn đang chờ bạn xem xét và phê duyệt.</p>
        </div>
        <div class="header-stats" v-if="pendingRequests.length > 0">
          <span class="pulse-dot"></span>
          <strong>{{ pendingRequests.length }}</strong> đơn chờ duyệt
        </div>
      </div>

      <div class="table-container">
        <table v-if="pendingRequests.length > 0" class="modern-table">
          <thead>
          <tr>
            <th>Người nộp đơn</th>
            <th>Thông tin nghỉ</th>
            <th>Thời gian</th>
            <th class="text-center">Số ca</th>
            <th>Lý do</th>
            <th>Ngày gửi</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="req in pendingRequests" :key="req.leaveId" class="table-row">
            <td>
              <div class="employee-info">
                <div class="avatar">{{ req.employeeName.charAt(0).toUpperCase() }}</div>
                <span class="fw-bold">{{ req.employeeName }}</span>
              </div>
            </td>
            <td>
              <div class="leave-type-info">
                <span class="type-name">{{ req.leaveTypeName }}</span>
                <span v-if="req.isPaid" class="badge-tag paid">Có lương</span>
                <span v-else class="badge-tag unpaid">Không lương</span>
              </div>
            </td>
            <td>
              <div class="date-range">
                <span class="date">{{ req.startDate }}</span>
                <span class="arrow">→</span>
                <span class="date">{{ req.endDate }}</span>
              </div>
            </td>
            <td class="text-center"><strong>{{ req.totalShift }}</strong></td>
            <td class="reason-cell">{{ req.reason }}</td>
            <td class="submit-date">{{ formatDate(req.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button
                    @click="updateStatus(req.leaveId, 'APPROVED')"
                    class="btn-action btn-approve"
                    :disabled="isProcessing"
                    title="Duyệt đơn này"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                  Duyệt
                </button>
                <button
                    @click="updateStatus(req.leaveId, 'REJECTED')"
                    class="btn-action btn-reject"
                    :disabled="isProcessing"
                    title="Từ chối đơn này"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                  Từ chối
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>

        <div v-else class="empty-state">
          <div class="empty-icon">🎉</div>
          <p>Tuyệt vời! Không có đơn xin nghỉ nào cần duyệt.</p>
          <span class="empty-sub">Bạn có thể thảnh thơi nhâm nhi một ly cà phê rồi! ☕</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'

const pendingRequests = ref([])
const isProcessing = ref(false)

// 1. Lấy danh sách đơn đang chờ duyệt
const fetchPendingRequests = async () => {
  try {
    const response = await LeaveService.getPendingRequests()
    pendingRequests.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh sách đơn chờ duyệt:', error)
  }
}

// 2. Xử lý Duyệt / Từ chối đơn
const updateStatus = async (leaveId, status) => {
  const actionText = status === 'APPROVED' ? 'duyệt' : 'từ chối'

  if (!confirm(`Bạn có chắc chắn muốn ${actionText} đơn này không?`)) {
    return
  }

  try {
    isProcessing.value = true

    // Backend tự biết ai duyệt thông qua Token, mình chỉ cần gửi leaveId và status
    await LeaveService.updateLeaveStatus(leaveId, status)

    alert(`Đã ${actionText} đơn xin nghỉ thành công!`)

    // Lọc bỏ đơn vừa duyệt khỏi danh sách chờ
    pendingRequests.value = pendingRequests.value.filter(req => req.leaveId !== leaveId)

  } catch (error) {
    console.error(`Lỗi khi ${actionText} đơn:`, error)
    alert('Có lỗi xảy ra, vui lòng thử lại!')
  } finally {
    isProcessing.value = false
  }
}

// Hàm phụ trợ format ngày tháng (Tùy chọn)
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute:'2-digit' })
}

onMounted(() => {
  fetchPendingRequests()
})
</script>

<style scoped>
/* Reset & Base Variables */
.admin-leave-wrapper {
  --primary-color: #4f46e5;
  --bg-color: #f3f4f6;
  --card-bg: #ffffff;
  --text-main: #111827;
  --text-muted: #6b7280;
  --border-color: #e5e7eb;
  --success-color: #10b981;
  --success-hover: #059669;
  --danger-color: #ef4444;
  --danger-hover: #dc2626;

  max-width: 1200px; /* Rộng hơn chút để chứa đủ cột bảng */
  margin: 0 auto;
  padding: 30px 15px;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: var(--text-main);
}

/* Card & Header */
.leave-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  border: 1px solid var(--border-color);
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.card-header {
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.card-header h2 { margin: 0 0 8px 0; font-size: 1.5rem; font-weight: 700; }
.subtitle { margin: 0; color: var(--text-muted); font-size: 0.9rem; }

.header-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #fef3c7;
  color: #92400e;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  border: 1px solid #fde68a;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background-color: #d97706;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(217, 119, 6, 0.7); }
  70% { box-shadow: 0 0 0 6px rgba(217, 119, 6, 0); }
  100% { box-shadow: 0 0 0 0 rgba(217, 119, 6, 0); }
}

/* Table */
.table-container {
  overflow-x: auto;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  white-space: nowrap;
}

.modern-table th {
  background-color: #f9fafb;
  padding: 14px 16px;
  font-size: 0.85rem;
  text-transform: uppercase;
  color: var(--text-muted);
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
}

.modern-table td {
  padding: 16px;
  font-size: 0.9rem;
  border-bottom: 1px solid var(--border-color);
  vertical-align: middle;
}

.table-row:hover { background-color: #f9fafb; }
.text-center { text-align: center; }

/* Employee Info & Badges */
.employee-info { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}
.fw-bold { font-weight: 600; color: var(--text-main); }

.leave-type-info { display: flex; flex-direction: column; gap: 4px; }
.type-name { font-weight: 500; }
.badge-tag {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
  font-weight: 600;
}
.badge-tag.paid { background-color: #d1fae5; color: #065f46; }
.badge-tag.unpaid { background-color: #f3f4f6; color: #4b5563; }

.date-range { display: flex; align-items: center; gap: 6px; }
.arrow { color: var(--text-muted); }
.submit-date { font-size: 0.85rem; color: var(--text-muted); }
.reason-cell {
  max-width: 180px;
  white-space: normal;
  color: #4b5563;
  line-height: 1.4;
  font-size: 0.85rem;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}

.btn-approve { background-color: var(--success-color); }
.btn-approve:hover:not(:disabled) { background-color: var(--success-hover); }

.btn-reject { background-color: var(--danger-color); }
.btn-reject:hover:not(:disabled) { background-color: var(--danger-hover); }

.btn-action:disabled { opacity: 0.5; cursor: not-allowed; }

/* Empty State */
.empty-state { text-align: center; padding: 50px 20px; }
.empty-icon { font-size: 3.5rem; margin-bottom: 16px; opacity: 0.8; }
.empty-state p { font-size: 1.1rem; font-weight: 600; margin: 0 0 8px 0; color: var(--text-main); }
.empty-sub { font-size: 0.9rem; color: var(--text-muted); }
</style>