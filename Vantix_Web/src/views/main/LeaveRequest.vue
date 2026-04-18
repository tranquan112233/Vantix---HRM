<template>
  <div class="leave-request-page mgmt-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Đơn nghỉ phép</h1>
        <p class="page-desc">Gửi đơn nghỉ phép và theo dõi tiến trình duyệt của từng yêu cầu tại một nơi.</p>
      </div>
      <div class="header-actions">
        <span class="status-badge pending">Chờ duyệt {{ pendingCount }}</span>
        <span class="status-badge approved">Đã duyệt {{ approvedCount }}</span>
        <span class="status-badge rejected">Từ chối {{ rejectedCount }}</span>
      </div>
    </div>

    <transition name="fade">
      <div v-if="message" :class="['alert-toast', messageType]">
        {{ message }}
      </div>
    </transition>

    <div class="leave-layout">
      <div class="content-card form-panel">
        <div class="panel-heading">
          <i class="bi bi-send"></i>
          <span>Tạo đơn mới</span>
        </div>
        <p class="panel-copy">Điền đầy đủ thông tin để HR duyệt nhanh hơn và hạn chế phải chỉnh sửa lại yêu cầu.</p>

        <form @submit.prevent="submitLeaveRequest" class="leave-form">
          <div class="form-row">
            <div class="field">
              <label>Loại nghỉ phép <span class="req">*</span></label>
              <div class="select-wrap">
                <select v-model="formData.leaveTypeId" required>
                  <option value="" disabled>-- Chọn loại nghỉ --</option>
                  <option v-for="type in leaveTypeOptions" :key="type.leaveTypeId" :value="type.leaveTypeId">
                    {{ type.typeName }} ({{ type.isPaid ? 'Có lương' : 'Không lương' }})
                  </option>
                </select>
                <i class="bi bi-chevron-down"></i>
              </div>
            </div>

            <div class="field">
              <label>Tổng số ca nghỉ <span class="req">*</span></label>
              <input v-model="formData.totalShift" type="number" min="1" required class="field-input" />
            </div>
          </div>

          <div class="form-row">
            <div class="field">
              <label>Từ ngày <span class="req">*</span></label>
              <input v-model="formData.startDate" type="date" required class="field-input" />
            </div>

            <div class="field">
              <label>Đến ngày <span class="req">*</span></label>
              <input v-model="formData.endDate" type="date" required class="field-input" />
            </div>
          </div>

          <div class="field">
            <label>Lý do nghỉ <span class="req">*</span></label>
            <textarea
              v-model="formData.reason"
              rows="4"
              required
              class="textarea-field"
              placeholder="Mô tả ngắn gọn lý do nghỉ để người duyệt nắm được bối cảnh."
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="submit" :disabled="isSubmitting" class="btn-primary">
              <span v-if="isSubmitting" class="spin-sm"></span>
              <template v-else>
                <i class="bi bi-send-check"></i>
                Gửi đơn xin nghỉ
              </template>
            </button>
          </div>
        </form>
      </div>

      <div class="table-card history-panel">
        <div class="card-header">
          <div class="header-title">
            <span class="header-icon"><i class="bi bi-clock-history"></i></span>
            <div>
              <h2>Lịch sử nghỉ phép</h2>
              <p>Xem lại các đơn đã gửi, trạng thái duyệt và người phê duyệt tương ứng.</p>
            </div>
          </div>
        </div>

        <div v-if="requests.length > 0" class="history-table">
          <table class="vantix-table">
            <thead>
              <tr>
                <th>Loại nghỉ</th>
                <th>Thời gian</th>
                <th>Số ca</th>
                <th>Lý do</th>
                <th>Trạng thái</th>
                <th>Người duyệt</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in requests" :key="req.leaveId">
                <td class="fw-700">{{ req.leaveTypeName }}</td>
                <td>
                  <div class="date-range">
                    <span>{{ formatDate(req.startDate) }}</span>
                    <i class="bi bi-arrow-right"></i>
                    <span>{{ formatDate(req.endDate) }}</span>
                  </div>
                </td>
                <td>{{ req.totalShift }}</td>
                <td class="reason-cell">{{ req.reason }}</td>
                <td>
                  <span :class="['status-badge', (req.status || '').toLowerCase()]">
                    {{ formatStatus(req.status) }}
                  </span>
                </td>
                <td>
                  <span v-if="req.approvedByName" class="approver">{{ req.approvedByName }}</span>
                  <span v-else class="no-approver">---</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state history-empty">
          <i class="bi bi-folder2-open empty-icon"></i>
          <p>Bạn chưa có đơn xin nghỉ nào.</p>
          <span class="empty-sub">Tất cả yêu cầu mới sẽ xuất hiện tại đây ngay sau khi gửi.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'
import { useAuthStore } from '@/stores/auth.store' // Mở comment này khi có store auth

// Lấy thông tin người đang đăng nhập
const leaveTypeOptions = ref([])
const auth = useAuthStore()
const currentUser = computed(() => auth.user)

const formData = reactive({
  leaveTypeId: '',
  startDate: '',
  endDate: '',
  totalShift: 1,
  reason: ''
})

const isSubmitting = ref(false)
const requests = ref([])
const message = ref('')
const messageType = ref('success')
let messageTimeout = null

const pendingCount = computed(() => requests.value.filter(req => req.status === 'PENDING').length)
const approvedCount = computed(() => requests.value.filter(req => req.status === 'APPROVED').length)
const rejectedCount = computed(() => requests.value.filter(req => req.status === 'REJECTED').length)

const showMessage = (text, type = 'success') => {
  message.value = text
  messageType.value = type
  if (messageTimeout) clearTimeout(messageTimeout)
  messageTimeout = setTimeout(() => {
    message.value = ''
  }, 4000)
}

const resetForm = () => {
  formData.startDate = ''
  formData.endDate = ''
  formData.reason = ''
  formData.leaveTypeId = ''
  formData.totalShift = 1
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatStatus = (status) => {
  if (status === 'PENDING') return 'Chờ duyệt'
  if (status === 'APPROVED') return 'Đã duyệt'
  if (status === 'REJECTED') return 'Từ chối'
  return status || 'Không rõ'
}

// 1. Hàm lấy danh sách lịch sử nghỉ phép
const fetchMyRequests = async () => {
  try {
    const response = await LeaveService.getMyLeaveRequests()
    requests.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh sách đơn:', error)
  }
}

// 2. 🔥 HÀM LẤY LOẠI NGHỈ PHÉP (ĐÃ ĐƯỢC CHỮA BỆNH)
const fetchLeaveTypes = async () => {
  try {
    const response = await LeaveService.getLeaveTypes()
    leaveTypeOptions.value = response.data
  } catch (error) {
    console.error('Lỗi lấy loại nghỉ phép:', error)
  }
}

// 3. HÀM GỬI ĐƠN
const submitLeaveRequest = async () => {
  // Validate ngày ở frontend trước khi gọi API
  const today = new Date().toISOString().split('T')[0]
  if (formData.startDate < today) {
    showMessage('Ngày bắt đầu không được trong quá khứ.', 'warning')
    return
  }
  if (formData.endDate < formData.startDate) {
    showMessage('Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.', 'warning')
    return
  }

  try {
    isSubmitting.value = true

    if (!currentUser.value) {
      showMessage('Không tìm thấy thông tin nhân viên hiện tại.', 'error')
      return
    }

    const payload = {
      ...formData,
      employeeId: currentUser.value.employeeId || currentUser.value.id
    }

    // Gửi payload có chứa employeeId đi thay vì formData trống không
    await LeaveService.createLeaveRequest(payload)

    showMessage('Nộp đơn xin nghỉ thành công.', 'success')
    resetForm()

    // Gọi lại hàm fetch để cập nhật danh sách lịch sử ngay lập tức
    await fetchMyRequests()

  } catch (error) {
    console.error('Lỗi khi nộp đơn:', error)
    showMessage('Có lỗi xảy ra, vui lòng kiểm tra lại thông tin.', 'error')
  } finally {
    isSubmitting.value = false
  }
}

// 4. Hook chạy khi component load
onMounted(() => {
  fetchMyRequests()
  fetchLeaveTypes()
})

onBeforeUnmount(() => {
  if (messageTimeout) clearTimeout(messageTimeout)
})
</script>

<style scoped>
.leave-request-page {
  gap: 22px;
}

.leave-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.form-panel,
.history-panel {
  padding: 24px;
}

.form-panel {
  position: sticky;
  top: 32px;
}

.panel-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  color: #222;
  font-size: 14px;
  font-weight: 700;
}

.panel-heading i {
  color: var(--primary-color);
}

.panel-copy {
  margin: 0 0 18px;
  color: #888;
  font-size: 13px;
  line-height: 1.7;
}

.leave-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.history-table {
  overflow-x: auto;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted-dark);
  font-size: 13px;
}

.date-range i {
  color: var(--text-dim);
  font-size: 11px;
}

.reason-cell {
  max-width: 260px;
  white-space: normal;
  line-height: 1.6;
  color: var(--text-muted-dark);
}

.approver {
  color: var(--text-darker);
  font-size: 13px;
  font-weight: 600;
}

.no-approver {
  color: var(--text-dim);
  font-style: italic;
}

.history-empty {
  padding: 56px 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 992px) {
  .leave-layout {
    grid-template-columns: 1fr;
  }

  .form-panel {
    position: static;
  }
}
</style>
