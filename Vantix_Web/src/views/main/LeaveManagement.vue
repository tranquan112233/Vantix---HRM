<template>
  <div class="admin-leave-container">
    <h2>Quản lý Đơn xin nghỉ phép</h2>

    <div class="table-responsive">
      <table v-if="pendingRequests.length > 0" class="leave-table">
        <thead>
        <tr>
          <th>Nhân viên</th>
          <th>Loại nghỉ</th>
          <th>Từ ngày</th>
          <th>Đến ngày</th>
          <th>Số ca</th>
          <th>Lý do</th>
          <th>Ngày nộp</th>
          <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="req in pendingRequests" :key="req.leaveId">
          <td class="fw-bold">{{ req.employeeName }}</td>
          <td>
            {{ req.leaveTypeName }}
            <span v-if="req.isPaid" class="badge-paid">(Có lương)</span>
            <span v-else class="badge-unpaid">(Không lương)</span>
          </td>
          <td>{{ req.startDate }}</td>
          <td>{{ req.endDate }}</td>
          <td class="text-center">{{ req.totalShift }}</td>
          <td>{{ req.reason }}</td>
          <td>{{ formatDate(req.createdAt) }}</td>
          <td class="action-buttons">
            <button
                @click="updateStatus(req.leaveId, 'APPROVED')"
                class="btn-approve"
                :disabled="isProcessing"
            >
              Duyệt
            </button>
            <button
                @click="updateStatus(req.leaveId, 'REJECTED')"
                class="btn-reject"
                :disabled="isProcessing"
            >
              Từ chối
            </button>
          </td>
        </tr>
        </tbody>
      </table>

      <div v-else class="empty-state">
        <p>🎉 Hiện tại không có đơn xin nghỉ nào cần duyệt!</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import LeaveService from '@/services/leaveservice.service.js'
// import { useAuthStore } from '@/stores/auth' // Mở comment khi có store auth

const pendingRequests = ref([])
const isProcessing = ref(false)

// Lấy ID của Admin/HR đang đăng nhập (Tạm fix cứng là 2)
// const authStore = useAuthStore()
// const approverId = authStore.user.employeeId
const approverId = 2

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

  // Hiển thị hộp thoại xác nhận trước khi thao tác
  if (!confirm(`Bạn có chắc chắn muốn ${actionText} đơn này không?`)) {
    return
  }

  try {
    isProcessing.value = true
    await LeaveService.updateLeaveStatus(leaveId, status, approverId)

    alert(`Đã ${actionText} đơn xin nghỉ thành công!`)

    // Cập nhật lại danh sách trên UI bằng cách loại bỏ đơn vừa xử lý
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
.admin-leave-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.leave-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.leave-table th, .leave-table td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: left;
  vertical-align: middle;
}

.leave-table th { background-color: #f4f6f8; font-weight: bold; }

.fw-bold { font-weight: bold; }
.text-center { text-align: center; }

.badge-paid { color: #155724; font-size: 0.8em; font-style: italic; }
.badge-unpaid { color: #721c24; font-size: 0.8em; font-style: italic; }

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-approve {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-reject {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled { opacity: 0.6; cursor: not-allowed; }

.empty-state {
  text-align: center;
  padding: 40px;
  background: #f9f9f9;
  border-radius: 8px;
  color: #666;
  margin-top: 20px;
}
</style>