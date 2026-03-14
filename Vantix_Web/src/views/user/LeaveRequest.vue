<template>
  <div class="leave-management-wrapper">
    <div class="leave-card form-card">
      <div class="card-header">
        <h2>✈️ Nộp đơn xin nghỉ phép</h2>
        <p class="subtitle">Vui lòng điền đầy đủ thông tin để HR duyệt đơn nhanh nhất nhé!</p>
      </div>

      <form @submit.prevent="submitLeaveRequest" class="leave-form">
        <div class="form-row">
          <div class="form-group">
            <label>Loại nghỉ phép <span class="required">*</span></label>
            <div class="input-wrapper">
              <select v-model="formData.leaveTypeId" required class="form-control">
                <option value="" disabled>-- Chọn loại nghỉ --</option>
                <option v-for="type in leaveTypeOptions" :key="type.leaveTypeId" :value="type.leaveTypeId">
                  {{ type.typeName }} ({{ type.isPaid ? 'Có lương' : 'Không lương' }})
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>Tổng số ca nghỉ <span class="required">*</span></label>
            <input type="number" v-model="formData.totalShift" min="1" required class="form-control" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Từ ngày <span class="required">*</span></label>
            <input type="date" v-model="formData.startDate" required class="form-control" />
          </div>

          <div class="form-group">
            <label>Đến ngày <span class="required">*</span></label>
            <input type="date" v-model="formData.endDate" required class="form-control" />
          </div>
        </div>

        <div class="form-group">
          <label>Lý do nghỉ <span class="required">*</span></label>
          <textarea v-model="formData.reason" rows="3" placeholder="Sếp cho em nghỉ để đi..." required class="form-control"></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isSubmitting" class="btn-submit">
            <span v-if="isSubmitting" class="spinner"></span>
            {{ isSubmitting ? 'Đang gửi...' : 'Gửi đơn xin nghỉ' }}
          </button>
        </div>
      </form>
    </div>

    <div class="leave-card history-card">
      <div class="card-header">
        <h2>🕒 Lịch sử nghỉ phép của tôi</h2>
      </div>

      <div class="table-container">
        <table v-if="requests.length > 0" class="modern-table">
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
          <tr v-for="req in requests" :key="req.leaveId" class="table-row">
            <td class="font-medium">{{ req.leaveTypeName }}</td>
            <td>
              <div class="date-range">
                <span class="date">{{ req.startDate }}</span>
                <span class="arrow">→</span>
                <span class="date">{{ req.endDate }}</span>
              </div>
            </td>
            <td class="text-center"><strong>{{ req.totalShift }}</strong></td>
            <td class="reason-cell">{{ req.reason }}</td>
            <td>
                <span :class="['status-badge', req.status.toLowerCase()]">
                  {{ req.status === 'PENDING' ? 'Chờ duyệt' : req.status === 'APPROVED' ? 'Đã duyệt' : 'Từ chối' }}
                </span>
            </td>
            <td>
              <span class="approver" v-if="req.approvedByName">{{ req.approvedByName }}</span>
              <span class="no-approver" v-else>---</span>
            </td>
          </tr>
          </tbody>
        </table>

        <div v-else class="empty-state">
          <div class="empty-icon">📁</div>
          <p>Bạn chưa có đơn xin nghỉ nào.</p>
          <span class="empty-sub">Cứ làm việc chăm chỉ nhé, sếp đang nhìn đấy! 😉</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'
// import { useAuthStore } from '@/stores/auth' // Mở comment này khi có store auth
import { getUser } from "@/utils/jwtDecode" // Import hàm giải mã Token của bạn

// Lấy thông tin người đang đăng nhập
const leaveTypeOptions = ref([])
const user = getUser()
const currentEmployeeId = user?.employeeId || 1

const formData = reactive({
  leaveTypeId: '',
  startDate: '',
  endDate: '',
  totalShift: 1,
  reason: ''
})

const isSubmitting = ref(false)
const requests = ref([])

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

// 3. 🔥 HÀM GỬI ĐƠN (ĐÃ ĐƯỢC KHÔI PHỤC)
const submitLeaveRequest = async () => {
  try {
    isSubmitting.value = true
    await LeaveService.createLeaveRequest(formData)

    alert('Nộp đơn xin nghỉ thành công!')

    // Reset form
    formData.startDate = ''
    formData.endDate = ''
    formData.reason = ''
    formData.leaveTypeId = ''
    formData.totalShift = 1

    // QUAN TRỌNG: Gọi lại hàm fetch để cập nhật danh sách lịch sử ngay lập tức
    await fetchMyRequests()

  } catch (error) {
    console.error('Lỗi khi nộp đơn:', error)
    alert('Có lỗi xảy ra, vui lòng kiểm tra lại thông tin!')
  } finally {
    isSubmitting.value = false
  }
}

// 4. Hook chạy khi component load
onMounted(() => {
  fetchMyRequests()
  fetchLeaveTypes()
})
</script>

<style scoped>
/* Reset & Base Variables */
.leave-management-wrapper {
  --primary-color: #4f46e5;
  --primary-hover: #4338ca;
  --bg-color: #f3f4f6;
  --card-bg: #ffffff;
  --text-main: #111827;
  --text-muted: #6b7280;
  --border-color: #e5e7eb;

  max-width: 960px;
  margin: 0 auto;
  padding: 30px 15px;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: var(--text-main);
}

/* Cards */
.leave-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  margin-bottom: 30px;
  border: 1px solid var(--border-color);
}

.card-header {
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
}

.subtitle {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* Forms */
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

label {
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-control {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.2s;
  background-color: #f9fafb;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
  background-color: #fff;
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

/* Buttons */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-submit {
  background-color: var(--primary-color);
  color: white;
  font-weight: 600;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);
}

.btn-submit:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

/* Spinner for button */
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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
  padding: 12px 16px;
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

.table-row:last-child td {
  border-bottom: none;
}

.table-row:hover {
  background-color: #f9fafb;
}

.font-medium {
  font-weight: 500;
  color: var(--text-main);
}

.text-center {
  text-align: center;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow {
  color: var(--text-muted);
  font-size: 1.1rem;
}

.reason-cell {
  max-width: 200px;
  white-space: normal;
  color: #4b5563;
  line-height: 1.4;
}

/* Badges */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
  text-align: center;
}

.status-badge.approved { background-color: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; }
.status-badge.rejected { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.status-badge.pending { background-color: #fef3c7; color: #92400e; border: 1px solid #fde68a; }

.approver {
  font-weight: 500;
  color: #374151;
}

.no-approver {
  color: #9ca3af;
  font-style: italic;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 8px 0;
}

.empty-sub {
  font-size: 0.9rem;
  color: var(--text-muted);
}

/* Responsive */
@media (max-width: 768px) {
  .form-row { flex-direction: column; gap: 0; }
  .form-group { margin-bottom: 20px; }
  .leave-card { padding: 20px 16px; }
}
</style>