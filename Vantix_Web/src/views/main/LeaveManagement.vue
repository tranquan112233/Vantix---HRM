<template>
  <div class="admin-leave-wrapper mgmt-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">Duyệt nghỉ phép</h2>
        <p class="page-desc">Danh sách đơn nghỉ phép đang chờ quản lý hoặc HR xử lý.</p>
      </div>
      <div class="status-badge done">
        <span class="dot"></span>
        {{ pendingRequests.length }} đơn chờ duyệt
      </div>
    </div>

    <div v-if="loading" class="state-center table-card">
      <div class="spin-lg mb-3"></div>
      <div class="empty-title">Đang tải danh sách đơn nghỉ</div>
      <div class="empty-sub">Hệ thống đang đồng bộ dữ liệu đơn nghỉ mới nhất.</div>
    </div>

    <div v-else class="table-card">
      <div v-if="pendingRequests.length > 0" class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Nhân viên</th>
              <th>Loại nghỉ</th>
              <th>Thời gian</th>
              <th class="text-center">Số ca</th>
              <th>Lý do</th>
              <th>Ngày gửi</th>
              <th class="text-right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="req in pendingRequests" :key="req.leaveId">
              <td>
                <div class="employee-cell">
                  <div class="avatar">{{ req.employeeName?.charAt(0)?.toUpperCase() || 'N' }}</div>
                  <div>
                    <div class="employee-name">{{ req.employeeName }}</div>
                    <div class="employee-username">Đơn #{{ req.leaveId }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="leave-type-cell">
                  <span class="employee-name">{{ req.leaveTypeName }}</span>
                  <span :class="['status-badge', req.isPaid ? 'completed' : 'open']">
                    {{ req.isPaid ? 'Có lương' : 'Không lương' }}
                  </span>
                </div>
              </td>
              <td>
                <div class="date-range">
                  <span>{{ formatShortDate(req.startDate) }}</span>
                  <i class="bi bi-arrow-right"></i>
                  <span>{{ formatShortDate(req.endDate) }}</span>
                </div>
              </td>
              <td class="text-center fw-600">{{ req.totalShift }}</td>
              <td class="reason-cell">{{ req.reason || 'Không có ghi chú' }}</td>
              <td class="submit-date">{{ formatDate(req.createdAt) }}</td>
              <td class="text-right">
                <div class="table-actions">
                  <button
                    class="btn-success btn-sm"
                    :disabled="isProcessing"
                    @click="approveRequest(req)"
                  >
                    <i class="bi bi-check-lg"></i> Duyệt
                  </button>
                  <button
                    class="btn-danger btn-sm"
                    :disabled="isProcessing"
                    @click="openRejectModal(req.leaveId)"
                  >
                    <i class="bi bi-x-lg"></i> Từ chối
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="state-center">
        <i class="bi bi-calendar-check empty-icon"></i>
        <div class="empty-title">Không có đơn nghỉ chờ duyệt</div>
        <div class="empty-sub">Các đơn mới sẽ xuất hiện tại đây khi nhân viên gửi yêu cầu.</div>
      </div>
    </div>

    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-container">
        <div class="modal-custom">
          <div class="modal-header-custom">
            <div>
              <h3 class="modal-title">Từ chối đơn nghỉ</h3>
              <p class="modal-subtitle">Bạn có thể thêm lý do để nhân viên dễ theo dõi.</p>
            </div>
            <button class="btn-close-custom" type="button" @click="closeRejectModal">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <div class="modal-body-custom">
            <div class="field">
              <label>Lý do từ chối</label>
              <textarea
                v-model="rejectReason"
                rows="4"
                class="textarea-field"
                placeholder="Ví dụ: thiếu minh chứng hoặc thời gian nghỉ chưa phù hợp..."
              ></textarea>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button class="btn-ghost" type="button" @click="closeRejectModal">Hủy</button>
            <button class="btn-danger" type="button" :disabled="isProcessing" @click="confirmReject">
              <i class="bi bi-x-circle"></i> Xác nhận từ chối
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'
import { confirmDialog } from '@/composables/useConfirmDialog'
import { useToast } from '@/utils/toast'

const pendingRequests = ref([])
const loading = ref(false)
const isProcessing = ref(false)

const showRejectModal = ref(false)
const rejectReason = ref('')
const rejectingLeaveId = ref(null)

const toast = useToast()

const fetchPendingRequests = async () => {
  try {
    loading.value = true
    const response = await LeaveService.getPendingRequests()
    pendingRequests.value = Array.isArray(response.data)
      ? response.data
      : response.data?.data || []
  } catch (error) {
    console.error('Lỗi lấy danh sách đơn chờ duyệt:', error)
    pendingRequests.value = []
    toast.error(error, 'Không thể tải danh sách đơn nghỉ.')
  } finally {
    loading.value = false
  }
}

const approveRequest = async (request) => {
  const confirmed = await confirmDialog({
    title: 'Duyệt đơn nghỉ',
    message: `Xác nhận duyệt đơn nghỉ của ${request.employeeName}?`,
    confirmText: 'Duyệt đơn',
    cancelText: 'Hủy',
    variant: 'success',
    icon: 'bi bi-check-circle'
  })

  if (!confirmed) return

  try {
    isProcessing.value = true
    await LeaveService.updateLeaveStatus(request.leaveId, 'APPROVED')
    pendingRequests.value = pendingRequests.value.filter(item => item.leaveId !== request.leaveId)
    toast.success(`Đã duyệt đơn nghỉ của ${request.employeeName}.`)
  } catch (error) {
    console.error('Lỗi khi duyệt đơn:', error)
    toast.error(error, 'Không thể duyệt đơn nghỉ.')
  } finally {
    isProcessing.value = false
  }
}

const openRejectModal = (leaveId) => {
  rejectingLeaveId.value = leaveId
  rejectReason.value = ''
  showRejectModal.value = true
}

const closeRejectModal = () => {
  showRejectModal.value = false
  rejectingLeaveId.value = null
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectingLeaveId.value) return

  try {
    isProcessing.value = true
    await LeaveService.updateLeaveStatus(
      rejectingLeaveId.value,
      'REJECTED',
      rejectReason.value.trim() || null
    )

    pendingRequests.value = pendingRequests.value.filter(
      req => req.leaveId !== rejectingLeaveId.value
    )
    closeRejectModal()
    toast.success('Đã từ chối đơn nghỉ.')
  } catch (error) {
    console.error('Lỗi khi từ chối đơn:', error)
    toast.error(error, 'Không thể từ chối đơn nghỉ.')
  } finally {
    isProcessing.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'

  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatShortDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

onMounted(fetchPendingRequests)
</script>

<style scoped>
.admin-leave-wrapper {
  gap: 24px;
}

.leave-type-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.date-range {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475467;
  font-size: 13px;
  font-weight: 600;
}

.reason-cell {
  max-width: 260px;
  color: #667085;
  line-height: 1.6;
  white-space: normal;
}

.submit-date {
  color: #667085;
  font-size: 13px;
}

.table-actions {
  display: inline-flex;
  justify-content: flex-end;
  gap: 8px;
}

.spin-lg {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
