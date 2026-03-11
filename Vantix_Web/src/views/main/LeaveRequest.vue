<template>
  <div class="leave-management-container">

    <div class="leave-form-section">
      <h2>Nộp đơn xin nghỉ phép</h2>
      <form @submit.prevent="submitLeaveRequest">

        <div class="form-row">
          <div class="form-group">
            <label>Loại nghỉ phép:</label>
            <select v-model="formData.leaveTypeId" required>
              <option value="" disabled>-- Chọn loại nghỉ --</option>
              <option value="1">Nghỉ ốm (Có lương)</option>
              <option value="2">Nghỉ phép năm (Có lương)</option>
              <option value="3">Nghỉ việc riêng (Không lương)</option>
            </select>
          </div>

          <div class="form-group">
            <label>Tổng số ca nghỉ:</label>
            <input type="number" v-model="formData.totalShift" min="1" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Từ ngày:</label>
            <input type="date" v-model="formData.startDate" required />
          </div>

          <div class="form-group">
            <label>Đến ngày:</label>
            <input type="date" v-model="formData.endDate" required />
          </div>
        </div>

        <div class="form-group">
          <label>Lý do nghỉ:</label>
          <textarea v-model="formData.reason" rows="3" placeholder="Nhập lý do chi tiết..." required></textarea>
        </div>

        <button type="submit" :disabled="isSubmitting" class="submit-btn">
          {{ isSubmitting ? 'Đang gửi...' : 'Gửi đơn xin nghỉ' }}
        </button>
      </form>
    </div>

    <hr class="divider" />

    <div class="leave-history-section">
      <h2>Lịch sử nghỉ phép của tôi</h2>

      <div class="table-responsive">
        <table v-if="requests.length > 0" class="leave-table">
          <thead>
          <tr>
            <th>Loại nghỉ</th>
            <th>Từ ngày</th>
            <th>Đến ngày</th>
            <th>Số ca</th>
            <th>Lý do</th>
            <th>Trạng thái</th>
            <th>Người duyệt</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="req in requests" :key="req.leaveId">
            <td>{{ req.leaveTypeName }}</td>
            <td>{{ req.startDate }}</td>
            <td>{{ req.endDate }}</td>
            <td>{{ req.totalShift }}</td>
            <td>{{ req.reason }}</td>
            <td>
                <span :class="['status-badge', req.status.toLowerCase()]">
                  {{ req.status }}
                </span>
            </td>
            <td>{{ req.approvedByName || 'Chưa có' }}</td>
          </tr>
          </tbody>
        </table>

        <p v-else class="empty-message">Bạn chưa có đơn xin nghỉ nào.</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'
// import { useAuthStore } from '@/stores/auth' // Mở comment này khi có store auth

// 1. Khai báo state
const employeeId = 1 // Tạm fix cứng. Sau này lấy từ AuthStore: const employeeId = authStore.user.employeeId

const formData = reactive({
  employeeId: employeeId,
  leaveTypeId: '',
  startDate: '',
  endDate: '',
  totalShift: 1,
  reason: ''
})

const isSubmitting = ref(false)
const requests = ref([])

// 2. Hàm lấy danh sách lịch sử
const fetchMyRequests = async () => {
  try {
    const response = await LeaveService.getMyLeaveRequests(employeeId)
    requests.value = response.data
  } catch (error) {
    console.error('Lỗi lấy danh sách đơn:', error)
  }
}

// 3. Hàm xử lý nộp đơn
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
})
</script>

<style scoped>
/* Bạn có thể xóa CSS này đi nếu dùng Bootstrap / Tailwind */
.leave-management-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.leave-form-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

label { font-weight: bold; margin-bottom: 5px; }
input, select, textarea { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }

.submit-btn {
  background-color: #007bff;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.submit-btn:disabled { background-color: #aaa; }

.divider { margin: 30px 0; border: 0; border-top: 1px solid #ddd; }

.leave-table {
  width: 100%;
  border-collapse: collapse;
}

.leave-table th, .leave-table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}

.leave-table th { background-color: #f1f1f1; }

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: bold;
}
.status-badge.approved { background-color: #d4edda; color: #155724; }
.status-badge.rejected { background-color: #f8d7da; color: #721c24; }
.status-badge.pending { background-color: #fff3cd; color: #856404; }
</style>