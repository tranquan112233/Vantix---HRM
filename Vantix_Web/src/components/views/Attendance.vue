<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// --- Giả định: ID này sẽ được lấy từ Store (Pinia/Vuex) hoặc LocalStorage sau khi Login ---
const currentUserID = ref(1);

const message = ref('');
const isError = ref(false);
const loading = ref(false);
const attendanceList = ref([]);

// --- Logic Chấm Công (Check-In) ---
const handleCheckIn = async () => {
  loading.value = true;
  message.value = '';

  try {
    // Sử dụng currentUserID đã có sẵn từ hệ thống login
    const response = await axios.post(
        `http://localhost:8080/api/attendance/create`,
        currentUserID.value, // Gửi trực tiếp ID nhân viên
        { headers: { 'Content-Type': 'application/json' } }
    );

    message.value = `Chấm công thành công! Giờ vào: ${response.data.checkIn}`;
    isError.value = false;

    // Refresh lại bảng dữ liệu
    fetchAttendanceData();
  } catch (error) {
    isError.value = true;
    message.value = error.response?.data || "Có lỗi xảy ra khi chấm công.";
  } finally {
    loading.value = false;
  }
};

const handleCheckOut = () => {
  message.value = "Chức năng Chấm Out đang được kết nối Backend...";
  isError.value = true;
};

// --- Lấy dữ liệu danh sách ---
const fetchAttendanceData = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/attendance/list');
    attendanceList.value = response.data;
  } catch (error) {
    console.error("Lỗi tải dữ liệu:", error);
  }
};

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

        <div class="card" @click="handleCheckOut">
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
          <h3>Lịch sử chấm công của bạn</h3>
        </div>
        <table>
          <thead>
          <tr>
            <th>Ca làm</th>
            <th>Ngày</th>
            <th>Vào</th>
            <th>Ra</th>
            <th>Giờ làm</th>
            <th>Trễ (m)</th>
            <th>Sớm (m)</th>
            <th>Trạng thái</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="att in attendanceList" :key="att.attendanceID">
            <td>{{ att.shift?.shiftName || 'Ca 1' }}</td>
            <td>{{ att.workDate }}</td>
            <td>{{ att.checkIn }}</td>
            <td>{{ att.checkOut || '--:--' }}</td>
            <td>{{ att.workHours || 0 }}</td>
            <td :class="{ 'warning': att.lateMinutes > 0 }">{{ att.lateMinutes }}</td>
            <td :class="{ 'warning': att.earlyLeaveMinutes > 0 }">{{ att.earlyLeaveMinutes }}</td>
            <td>
                <span :class="['status-badge', att.status]">
                  {{ att.status || 'Draft' }}
                </span>
            </td>
          </tr>
          <tr v-if="attendanceList.length === 0">
            <td colspan="8" class="empty-cell">Bạn chưa có dữ liệu chấm công nào.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

/* Card Grid */
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

.loading-state { opacity: 0.6; cursor: not-allowed; }

.icon { font-size: 50px; margin-bottom: 15px; }
h3 { color: #0d47a1; margin-bottom: 10px; }
p { color: #78909c; font-size: 15px; }

/* Table Section */
.table-container {
  background: white;
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
}

.table-header { margin-bottom: 20px; border-left: 4px solid #2196f3; padding-left: 15px; }

table { width: 100%; border-collapse: collapse; }
th { background: #f8fbff; color: #546e7a; text-align: left; padding: 15px; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; }
td { padding: 15px; border-bottom: 1px solid #f1f1f1; color: #37474f; font-size: 14px; }

/* Badge & Status */
.status-badge {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.Draft { background: #eceff1; color: #607d8b; }
.Approved { background: #e8f5e9; color: #2e7d32; }

.warning { color: #d32f2f; font-weight: 600; }

.alert {
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 30px;
  text-align: center;
  font-weight: 500;
}
.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; }
.error { background: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }

/* Animation */
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>