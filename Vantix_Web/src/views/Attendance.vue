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
const messageType = ref('success'); // 'success' | 'error' | 'warning'
const loading = ref(false);
const attendanceList = ref([]);
const showConfirmModal = ref(false); // Trạng thái hiển thị Modal xác nhận

// Filter: Tháng/Năm hiện tại
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

// --- 2. HÀM CHẤM CÔNG (CHECK-IN) ---
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

// --- 3. LOGIC CHẤM OUT (CHECK-OUT) ---

// Bước 1: Kích hoạt Modal hỏi xác nhận
const requestCheckOut = () => {
  if (loading.value) return;
  showConfirmModal.value = true; // Mở Modal
  message.value = '';
};

// Bước 2: Thực hiện gọi API sau khi người dùng bấm "Đồng ý"
const confirmCheckOut = async () => {
  showConfirmModal.value = false; // Đóng Modal
  loading.value = true;

  try {
    // --- SỬA LỖI TẠI ĐÂY ---
    // Trước đó: checkOutManual({ employeeId: ... }) -> Gửi Object -> Lỗi Backend
    // Sửa thành: checkOutManual(employeeId.value) -> Gửi số nguyên -> OK
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

// --- 4. HÀM XỬ LÝ LỖI THÔNG MINH ---
const handleError = (error) => {
  messageType.value = 'error'; // Mặc định là lỗi đỏ

  if (error.response && error.response.data) {
    const data = error.response.data;

    // Lấy nội dung message từ JSON hoặc String
    if (typeof data === 'object' && data.message) {
      message.value = data.message;
    } else {
      message.value = String(data);
    }

    // --- LOGIC ĐỔI MÀU ---
    // Chuyển chữ thường để so sánh cho dễ
    const msgLower = String(message.value).toLowerCase();

    // Nếu có từ khóa "xác nhận" hoặc "approved" -> Màu Vàng
    if (msgLower.includes("xác nhận") || msgLower.includes("approved") || msgLower.includes("đã có trạng thái")) {
      messageType.value = 'warning';
    }

  } else if (error.message) {
    message.value = error.message;
  } else {
    message.value = "❌ Không thể kết nối đến máy chủ.";
  }
};

// --- LIFECYCLE ---
watch([selectedMonth, selectedYear], fetchAttendanceData);

onMounted(() => {
  fetchAttendanceData();
});
</script>

<template>
  <div class="attendance-page">
    <div class="container">
      <h1 class="page-title">Hệ Thống Chấm Công Vantix</h1>

      <div class="card-grid">
        <div
            class="card"
            @click="!loading && handleCheckIn()"
            :class="{ 'loading-state': loading }"
        >
          <div class="icon">⏱️</div>
          <h3>Chấm Công</h3>
          <p>Nhấn để bắt đầu ca làm việc</p>
        </div>

        <div
            class="card"
            @click="!loading && requestCheckOut()"
            :class="{ 'loading-state': loading }"
        >
          <div class="icon">🚪</div>
          <h3>Chấm Out</h3>
          <p>Nhấn để kết thúc ca làm</p>
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
            <td :class="{ 'warning-text': att.lateMinutes > 0 }">
              {{ att.lateMinutes > 0 ? att.lateMinutes : '-' }}
            </td>
            <td :class="{ 'warning-text': att.earlyLeaveMinutes > 0 }">
              {{ att.earlyLeaveMinutes > 0 ? att.earlyLeaveMinutes : '-' }}
            </td>
            <td>
                <span :class="['status-badge', att.status]">
                  {{ att.status || 'Draft' }}
                </span>
            </td>
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-bottom: 40px;
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

/* CSS ALERT */
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

/* CSS MODAL */
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