<script setup>
import {ref, onMounted, watch} from 'vue';
import attendanceService from "../assets/service/attendance.service.js";

// --- CẤU HÌNH ---
// Đã đổi tên biến từ currentUserID -> employeeId
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
const isError = ref(false);
const loading = ref(false);
const attendanceList = ref([]);

// Filter: Tháng/Năm hiện tại
const today = new Date();
const selectedMonth = ref(today.getMonth() + 1);
const selectedYear = ref(today.getFullYear());

// --- 1. HÀM LẤY DỮ LIỆU ---
const fetchAttendanceData = async () => {
  try {
    // Truyền employeeId vào service
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
    // Truyền employeeId vào service
    const response = await attendanceService.checkIn(employeeId.value);
    message.value = `✅ Chấm công thành công! Giờ vào: ${formatTime(response.data.checkIn)}`;
    isError.value = false;
    await fetchAttendanceData();
  } catch (error) {
    isError.value = true;
    if (error.response && error.response.data) {
      message.value = error.response.data; // Hiển thị message lỗi từ backend (vd: "Đã chấm công rồi")
    } else {
      message.value = "❌ Có lỗi kết nối đến máy chủ.";
    }
  } finally {
    loading.value = false;
  }
};

// --- 3. HÀM CHẤM OUT (CHECK-OUT) ---
const handleCheckOut = async () => {
  if (loading.value) return;
  loading.value = true;
  message.value = '';

  try {
    // Truyền employeeId vào service
    const response = await attendanceService.checkOut(employeeId.value);
    message.value = response.data;
    isError.value = false;
    await fetchAttendanceData();
  } catch (error) {
    isError.value = true;
    if (error.response && error.response.data) {
      message.value = error.response.data;
    } else {
      message.value = "❌ Có lỗi kết nối đến máy chủ.";
    }
  } finally {
    loading.value = false;
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
            @click="!loading && handleCheckOut()"
            :class="{ 'loading-state': loading }"
        >
          <div class="icon">🚪</div>
          <h3>Chấm Out</h3>
          <p>Nhấn để kết thúc ca làm</p>
        </div>
      </div>

      <transition name="fade">
        <div v-if="message" :class="['alert', isError ? 'error' : 'success']">
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
  </div>
</template>

<style scoped>
/* Code CSS giữ nguyên như cũ */
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  transition: border-color 0.2s;
}

.custom-select:hover {
  border-color: #2196f3;
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
  letter-spacing: 1px;
}

td {
  padding: 15px;
  border-bottom: 1px solid #f1f1f1;
  color: #37474f;
  font-size: 14px;
}

.empty-cell {
  text-align: center;
  padding: 30px;
  color: #90a4ae;
  font-style: italic;
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
  font-weight: 500;
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

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>