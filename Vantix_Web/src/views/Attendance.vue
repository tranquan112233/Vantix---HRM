<script setup>
import {ref, onMounted, watch} from 'vue';
import attendanceService from "../assets/service/attendance.service.js";

// --- CẤU HÌNH ---
const employeeId = ref(1); // Giả lập ID nhân viên

// --- FORMATTER ---
const formatTime = (timeStr) => timeStr ? timeStr.slice(0, 5) : '--:--';

const getShiftLabel = (shiftObj) => {
  if (!shiftObj) return 'Khác';
  if (shiftObj.shiftId === 1) return 'Sáng';
  if (shiftObj.shiftId === 2) return 'Chiều';
  return shiftObj.shiftName || 'Khác';
};

// --- STATE ---
const message = ref('');
const messageType = ref('success');
const loading = ref(false);
const attendanceList = ref([]);
const showConfirmModal = ref(false);

// Filter
const today = new Date();
const selectedMonth = ref(today.getMonth() + 1);
const selectedYear = ref(today.getFullYear());

// --- 1. HÀM LẤY DỮ LIỆU ---
const fetchAttendanceData = async () => {
  try {
    const response = await attendanceService.getMonthlyAttendance(
        employeeId.value,
        selectedMonth.value,
        selectedYear.value
    );
    attendanceList.value = response.data.sort((a, b) => {
      const dateA = new Date(a.workDate);
      const dateB = new Date(b.workDate);
      if (dateB - dateA !== 0) return dateB - dateA;
      return (b.shift?.shiftId || 0) - (a.shift?.shiftId || 0);
    });
  } catch (error) {
    console.error("Lỗi tải dữ liệu:", error);
    attendanceList.value = [];
  }
};

// --- 2. HÀM CHẤM CÔNG ---
const handleCheckIn = async () => {
  if (loading.value) return;
  loading.value = true;
  message.value = '';

  try {
    const response = await attendanceService.checkIn(employeeId.value);
    message.value = `✅ Chấm công thành công! Giờ vào: ${formatTime(response.data.checkIn)}`;
    messageType.value = 'success';
    await fetchAttendanceData();
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// --- 3. HÀM CHECK OUT ---
const requestCheckOut = () => {
  if (loading.value) return;
  showConfirmModal.value = true;
  message.value = '';
};

const confirmCheckOut = async () => {
  showConfirmModal.value = false;
  loading.value = true;
  try {
    const response = await attendanceService.checkOutManual(employeeId.value);
    message.value = "✅ Kết thúc ca làm việc thành công!";
    messageType.value = 'success';
    await fetchAttendanceData();
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// --- 4. HÀM XÁC NHẬN CÔNG (MỚI THÊM) ---
const handleConfirm = async () => {
  if (loading.value) return;
  loading.value = true;
  message.value = '';

  try {
    // Gọi API Confirm
    const response = await attendanceService.confirmCheckOut(employeeId.value);

    message.value = "✅ Xác nhận công thành công! Trạng thái đã chuyển sang APPROVED.";
    messageType.value = 'success';
    await fetchAttendanceData(); // Load lại bảng để thấy status thay đổi
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// --- XỬ LÝ LỖI ---
const handleError = (error) => {
  messageType.value = 'error';
  if (error.response && error.response.data) {
    const data = error.response.data;
    if (typeof data === 'object' && data.message) {
      message.value = data.message;
    } else {
      message.value = String(data);
    }
    const msgLower = String(message.value).toLowerCase();
    // Logic đổi màu vàng nếu là cảnh báo
    if (msgLower.includes("xác nhận") || msgLower.includes("approved") || msgLower.includes("đã có trạng thái")) {
      messageType.value = 'warning';
    }
  } else if (error.message) {
    message.value = error.message;
  } else {
    message.value = "❌ Không thể kết nối đến máy chủ.";
  }
};

watch([selectedMonth, selectedYear], fetchAttendanceData);
onMounted(() => fetchAttendanceData());
</script>

<template>
  <div class="attendance-page">
    <div class="container">
      <h1 class="page-title">Hệ Thống Chấm Công Vantix</h1>

      <div class="card-grid">
        <div class="card" @click="!loading && handleCheckIn()" :class="{ 'loading-state': loading }">
          <div class="icon">⏱️</div>
          <h3>Chấm Công</h3>
          <p>Bắt đầu ca làm việc</p>
        </div>

        <div class="card" @click="!loading && requestCheckOut()" :class="{ 'loading-state': loading }">
          <div class="icon">🚪</div>
          <h3>Chấm Out</h3>
          <p>Kết thúc ca làm</p>
        </div>

        <div class="card confirm-card" @click="!loading && handleConfirm()" :class="{ 'loading-state': loading }">
          <div class="icon">✅</div>
          <h3>Xác nhận</h3>
          <p>Duyệt công bị treo</p>
        </div>
      </div>

      <transition name="fade">
        <div v-if="message" :class="['alert', messageType]">
          {{ message }}
        </div>
      </transition>

      <div class="table-container">
        <div class="table-header">
          <h3>Lịch sử chấm công</h3>
          <div class="header-controls">
            <select v-model="selectedMonth" class="custom-select">
              <option v-for="m in 12" :key="m" :value="m">Tháng {{ m }}</option>
            </select>
            <select v-model="selectedYear" class="custom-select">
              <option v-for="y in 5" :key="y" :value="2024 + y">{{ 2024 + y }}</option>
            </select>
          </div>
        </div>

        <table>
          <thead>
          <tr>
            <th>Ngày</th>
            <th>Ca làm</th>
            <th>Vào</th>
            <th>Ra</th>
            <th>Trễ (p)</th>
            <th>Sớm (p)</th>
            <th>Trạng thái</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="attendanceList.length === 0">
            <td colspan="7" class="empty-cell">Không có dữ liệu trong tháng {{ selectedMonth }}/{{ selectedYear }}.</td>
          </tr>
          <tr v-for="att in attendanceList" :key="att.attendanceId">
            <td>{{ att.workDate }}</td>
            <td>
              <span :class="['shift-badge', att.shift?.shiftId === 1 ? 'morning' : 'afternoon']">
                 {{ getShiftLabel(att.shift) }}
              </span>
            </td>
            <td>{{ formatTime(att.checkIn) }}</td>
            <td>{{ formatTime(att.checkOut) }}</td>
            <td :class="{ 'warning-text': att.lateMinutes > 0 }">{{ att.lateMinutes > 0 ? att.lateMinutes : '-' }}</td>
            <td :class="{ 'warning-text': att.earlyLeaveMinutes > 0 }">
              {{ att.earlyLeaveMinutes > 0 ? att.earlyLeaveMinutes : '-' }}
            </td>
            <td><span :class="['status-badge', att.status]">{{ att.status || 'Draft' }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showConfirmModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Xác nhận Check-out?</h3>
        <p>Bạn có chắc chắn muốn kết thúc ca làm việc này không?</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showConfirmModal = false">Hủy bỏ</button>
          <button class="btn-confirm" @click="confirmCheckOut">Đồng ý</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* --- GIỮ NGUYÊN CSS CŨ --- */
.attendance-page {
  min-height: 100vh;
  padding: 60px 0;
  background: linear-gradient(135deg, #f0f7ff, #ffffff);
  display: flex;
  justify-content: center;
}

.container {
  width: 90%;
  max-width: 950px;
}

.page-title {
  text-align: center;
  color: #1a237e;
  margin-bottom: 40px;
  font-weight: 700;
}

/* --- CẬP NHẬT CSS CARD GRID (QUAN TRỌNG) --- */
.card-grid {
  display: grid;
  /* Sửa thành 3 cột để chứa 3 nút */
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

/* Nếu màn hình nhỏ thì tự xuống dòng */
@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}

.card {
  background: #ffffff;
  border-radius: 20px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  border: 1px solid #e3f2fd;
  box-shadow: 0 10px 25px rgba(33, 150, 243, 0.05);
  transition: all 0.3s;
}

.card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(33, 150, 243, 0.15);
  border-color: #2196f3;
}

/* Style riêng cho nút Confirm nếu muốn */
.confirm-card:hover {
  border-color: #4caf50;
}

.loading-state {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon {
  font-size: 50px;
  margin-bottom: 15px;
}

h3 {
  color: #0d47a1;
  margin-bottom: 10px;
}

p {
  color: #78909c;
  font-size: 15px;
}

/* --- CSS BẢNG VÀ ALERT GIỮ NGUYÊN --- */
.table-container {
  background: white;
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.03);
}

.table-header {
  margin-bottom: 20px;
  border-left: 4px solid #2196f3;
  padding-left: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  gap: 10px;
}

.custom-select {
  padding: 8px 12px;
  border: 1px solid #e3f2fd;
  border-radius: 8px;
  background-color: #f8fbff;
  color: #0d47a1;
  font-weight: 600;
  outline: none;
  cursor: pointer;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #f8fbff;
  color: #546e7a;
  text-align: left;
  padding: 15px;
  font-size: 13px;
  text-transform: uppercase;
}

td {
  padding: 15px;
  border-bottom: 1px solid #f1f1f1;
  color: #37474f;
  font-size: 14px;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  text-transform: capitalize;
}

.Draft {
  background: #eceff1;
  color: #607d8b;
}

.Approved {
  background: #e8f5e9;
  color: #2e7d32;
}

.Pending {
  background: #fff8e1;
  color: #ffa000;
}

.Rejected {
  background: #ffebee;
  color: #c62828;
}

.shift-badge {
  font-weight: 700;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 15px;
}

.shift-badge.morning {
  color: #0277bd;
  background-color: #e1f5fe;
}

.shift-badge.afternoon {
  color: #ef6c00;
  background-color: #fff3e0;
}

.warning-text {
  color: #d32f2f;
  font-weight: 700;
}

.alert {
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 30px;
  text-align: center;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.warning {
  background: #fff3e0;
  color: #ef6c00;
  border: 1px solid #ffe0b2;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.modal-content h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #1a237e;
}

.modal-actions {
  margin-top: 25px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.btn-confirm, .btn-cancel {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-confirm {
  background: #2196f3;
  color: white;
}

.btn-confirm:hover {
  background: #1976d2;
}

.btn-cancel {
  background: #f5f5f5;
  color: #616161;
}

.btn-cancel:hover {
  background: #eeeeee;
}

@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>