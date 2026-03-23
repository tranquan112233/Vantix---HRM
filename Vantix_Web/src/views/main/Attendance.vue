<script setup>
import {ref, onMounted, watch, computed} from 'vue';
import attendanceService from "@/services/attendance.service.js";

// --- CẤU HÌNH ---
const employeeId = ref(8); // Giả lập ID nhân viên

// --- FORMATTER & HELPERS ---
const formatTime = (timeStr) => timeStr ? timeStr.slice(0, 5) : '--:--';

const getShiftLabel = (shiftObj) => {
  if (!shiftObj) return 'Khác';
  if (shiftObj.shiftId === 1) return 'Sáng';
  if (shiftObj.shiftId === 2) return 'Chiều';
  return shiftObj.shiftName || 'Khác';
};

const getStatusClass = (status) => {
  if (!status) return 'badge-default';
  const s = status.toLowerCase();
  if (s === 'approved') return 'badge-success';
  if (s === 'pending' || s === 'draft') return 'badge-warning';
  if (s === 'rejected') return 'badge-danger';
  return 'badge-default';
};

// --- STATE ---
const message = ref('');
const messageType = ref('success');
const loading = ref(false);
const attendanceList = ref([]);

// Modals
const showConfirmModal = ref(false);
const showDetailModal = ref(false);

// Chi tiết ngày được chọn
const selectedDayRecords = ref([]);
const selectedDateDisplay = ref('');

let messageTimeout = null;

// Filter
const today = new Date();
const selectedMonth = ref(today.getMonth() + 1);
const selectedYear = ref(today.getFullYear());

// --- HÀM XỬ LÝ THÔNG BÁO ---
const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;
  if (messageTimeout) clearTimeout(messageTimeout);
  messageTimeout = setTimeout(() => {
    message.value = '';
  }, 5000);
};

// --- 1. LẤY DỮ LIỆU VÀ XỬ LÝ LỊCH ---
const fetchAttendanceData = async () => {
  try {
    const response = await attendanceService.getMonthlyAttendance(
        employeeId.value,
        selectedMonth.value,
        selectedYear.value
    );
    attendanceList.value = response.data || [];
  } catch (error) {
    console.error("Lỗi tải dữ liệu:", error);
    attendanceList.value = [];
  }
};

const attendanceMap = computed(() => {
  const map = {};
  attendanceList.value.forEach(att => {
    const dateKey = att.workDate;
    if (!map[dateKey]) map[dateKey] = [];
    map[dateKey].push(att);
  });
  // Sắp xếp các ca trong cùng 1 ngày (Ca sáng trước, chiều sau)
  for (const date in map) {
    map[date].sort((a, b) => (a.shift?.shiftId || 0) - (b.shift?.shiftId || 0));
  }
  return map;
});

const calendarDays = computed(() => {
  const year = selectedYear.value;
  const month = selectedMonth.value - 1;
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);

  let startDayOfWeek = firstDay.getDay();
  startDayOfWeek = (startDayOfWeek + 6) % 7;

  const days = [];
  for (let i = 0; i < startDayOfWeek; i++) days.push(null);
  for (let d = 1; d <= lastDay.getDate(); d++) days.push(d);
  return days;
});

const getDayStatus = (day) => {
  if (!day) return 'empty';
  const dateStr = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  const currentDayDate = new Date(selectedYear.value, selectedMonth.value - 1, day);
  const todayDate = new Date();
  todayDate.setHours(0, 0, 0, 0);

  if (currentDayDate > todayDate) return 'status-future';
  if (currentDayDate.getDay() === 0) return 'status-sunday';

  const records = attendanceMap.value[dateStr] || [];
  if (records.length >= 2) return 'status-full';
  if (records.length === 1) return 'status-half';
  return 'status-absent';
};

// --- 2. HÀM XEM CHI TIẾT NGÀY ---
const openDayDetail = (day) => {
  if (!day) return;
  const dateStr = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

  // Format hiển thị: DD/MM/YYYY
  selectedDateDisplay.value = `${String(day).padStart(2, '0')}/${String(selectedMonth.value).padStart(2, '0')}/${selectedYear.value}`;

  // Lấy danh sách ca làm việc của ngày đó
  selectedDayRecords.value = attendanceMap.value[dateStr] || [];
  showDetailModal.value = true;
};

// --- 3. HÀM CHẤM CÔNG VÀ XÁC NHẬN ---
const handleCheckIn = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const response = await attendanceService.checkIn(employeeId.value);
    showMessage(`✅ Chấm công thành công! Giờ vào: ${formatTime(response.data.checkIn)}`, 'success');
    await fetchAttendanceData();
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

const requestCheckOut = () => {
  if (loading.value) return;
  showConfirmModal.value = true;
};

const confirmCheckOut = async () => {
  showConfirmModal.value = false;
  loading.value = true;
  try {
    await attendanceService.checkOutManual(employeeId.value);
    showMessage("✅ Kết thúc ca làm việc thành công!", 'success');
    await fetchAttendanceData();
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

const handleConfirm = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    await attendanceService.confirmCheckOut(employeeId.value);
    showMessage("✅ Xác nhận công thành công! Trạng thái đã chuyển sang APPROVED.", 'success');
    await fetchAttendanceData();
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// --- XỬ LÝ LỖI ---
const handleError = (error) => {
  let errorText = "❌ Không thể kết nối đến máy chủ.";
  let errorType = 'error';

  if (error.response && error.response.data) {
    const data = error.response.data;
    errorText = typeof data === 'object' && data.message ? data.message : String(data);
    const msgLower = String(errorText).toLowerCase();
    if (msgLower.includes("xác nhận") || msgLower.includes("approved") || msgLower.includes("đã có trạng thái")) {
      errorType = 'warning';
    }
  } else if (error.message) {
    errorText = error.message;
  }
  showMessage(errorText, errorType);
};

watch([selectedMonth, selectedYear], fetchAttendanceData);
onMounted(() => fetchAttendanceData());
</script>

<template>
  <div class="page-wrapper">
    <div class="dashboard-metric-cards">
      <div class="metric-card" @click="!loading && handleCheckIn()" :class="{ 'loading-state': loading }">
        <div class="metric-info">
          <span class="metric-title">Bắt đầu ca làm</span>
          <h3 class="metric-action">Chấm Công In</h3>
        </div>
        <div class="metric-icon bg-blue">⏱️</div>
      </div>

      <div class="metric-card" @click="!loading && requestCheckOut()" :class="{ 'loading-state': loading }">
        <div class="metric-info">
          <span class="metric-title">Kết thúc ca làm</span>
          <h3 class="metric-action">Chấm Out</h3>
        </div>
        <div class="metric-icon bg-orange">🚪</div>
      </div>

      <div class="metric-card" @click="!loading && handleConfirm()" :class="{ 'loading-state': loading }">
        <div class="metric-info">
          <span class="metric-title">Duyệt công treo</span>
          <h3 class="metric-action">Xác nhận</h3>
        </div>
        <div class="metric-icon bg-green">✅</div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="message" :class="['alert-toast', messageType]">
        {{ message }}
      </div>
    </transition>

    <div class="content-card">
      <div class="card-header">
        <div class="header-title">
          <span class="header-icon">📅</span>
          <div>
            <h2>Lịch Chấm Công</h2>
            <p>Thống kê số ca làm việc theo ngày</p>
          </div>
        </div>
        <div class="header-filters">
          <div class="search-box">
            <select v-model="selectedMonth" class="flat-select">
              <option v-for="m in 12" :key="m" :value="m">Tháng {{ m }}</option>
            </select>
          </div>
          <div class="search-box">
            <select v-model="selectedYear" class="flat-select">
              <option v-for="y in 5" :key="y" :value="2024 + y">{{ 2024 + y }}</option>
            </select>
          </div>
        </div>
      </div>

      <div class="calendar-legend">
        <span class="legend-item"><span class="dot bg-success"></span> Đủ 2 ca</span>
        <span class="legend-item"><span class="dot bg-warning"></span> 1 ca</span>
        <span class="legend-item"><span class="dot bg-danger"></span> Vắng</span>
        <span class="legend-item"><span class="dot bg-gray"></span> Chưa tới / Nghỉ CN</span>
      </div>

      <div class="calendar-wrapper">
        <div class="calendar-header">
          <div class="weekday">Thứ 2</div>
          <div class="weekday">Thứ 3</div>
          <div class="weekday">Thứ 4</div>
          <div class="weekday">Thứ 5</div>
          <div class="weekday">Thứ 6</div>
          <div class="weekday">Thứ 7</div>
          <div class="weekday sunday-col">CN</div>
        </div>

        <div class="calendar-grid">
          <div
              v-for="(day, index) in calendarDays"
              :key="index"
              :class="['calendar-cell', getDayStatus(day), { 'is-clickable': day }]"
              @click="openDayDetail(day)"
          >
            <template v-if="day">
              <div class="date-num">{{ day }}</div>
              <div class="date-status-text">
                <span v-if="getDayStatus(day) === 'status-full'">Đủ ca</span>
                <span v-else-if="getDayStatus(day) === 'status-half'">Thiếu 1 ca</span>
                <span v-else-if="getDayStatus(day) === 'status-absent'">Vắng mặt</span>
                <span v-else-if="getDayStatus(day) === 'status-sunday'">Nghỉ Lễ / CN</span>
                <span v-else-if="getDayStatus(day) === 'status-future'">-</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal-content modal-lg">
        <div class="modal-header-custom">
          <h3>Chi tiết ngày làm việc - {{ selectedDateDisplay }}</h3>
          <button class="close-btn" @click="showDetailModal = false">&times;</button>
        </div>

        <div class="modal-body">
          <div v-if="selectedDayRecords.length > 0" class="table-responsive">
            <table class="vantix-table">
              <thead>
              <tr>
                <th>Ca làm</th>
                <th>Giờ vào</th>
                <th>Giờ ra</th>
                <th>Trễ (p)</th>
                <th>Sớm (p)</th>
                <th>Trạng thái</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="att in selectedDayRecords" :key="att.attendanceId">
                <td>
                    <span :class="['vt-badge', att.shift?.shiftId === 1 ? 'badge-cyan' : 'badge-gray']">
                      {{ getShiftLabel(att.shift) }}
                    </span>
                </td>
                <td class="time-text">{{ formatTime(att.checkIn) }}</td>
                <td class="time-text">{{ formatTime(att.checkOut) }}</td>
                <td :class="{ 'text-danger fw-600': att.lateMinutes > 0 }">
                  {{ att.lateMinutes > 0 ? att.lateMinutes : '-' }}
                </td>
                <td :class="{ 'text-danger fw-600': att.earlyLeaveMinutes > 0 }">
                  {{ att.earlyLeaveMinutes > 0 ? att.earlyLeaveMinutes : '-' }}
                </td>
                <td>
                  <span :class="['vt-badge', getStatusClass(att.status)]">{{ att.status || 'Draft' }}</span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="empty-state">
            <span class="empty-icon">📂</span>
            <p>Không có dữ liệu chấm công cho ngày này.</p>
          </div>
        </div>

        <div class="modal-actions mt-3">
          <button class="btn btn-outline" @click="showDetailModal = false">Đóng</button>
        </div>
      </div>
    </div>

    <div v-if="showConfirmModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Xác nhận Check-out?</h3>
        <p>Bạn có chắc chắn muốn kết thúc ca làm việc này không?</p>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showConfirmModal = false">Hủy bỏ</button>
          <button class="btn btn-primary" @click="confirmCheckOut">Đồng ý</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* (Phần CSS chung từ các bước trước giữ nguyên) */
.page-wrapper {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  font-family: 'Inter', 'Segoe UI', sans-serif;
  box-sizing: border-box;
}

.dashboard-metric-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s ease;
}

.metric-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.metric-info {
  display: flex;
  flex-direction: column;
}

.metric-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.metric-action {
  font-size: 18px;
  color: #303133;
  margin: 0;
  font-weight: 600;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.bg-blue {
  background: #e6f1fc;
  color: #409eff;
}

.bg-orange {
  background: #fdf0e6;
  color: #e6a23c;
}

.bg-green {
  background: #e1f3d8;
  color: #67c23a;
}

.loading-state {
  opacity: 0.6;
  pointer-events: none;
}

.content-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  background: #f0f7ff;
  padding: 8px 12px;
  border-radius: 8px;
}

.header-title h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.header-title p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #909399;
}

.header-filters {
  display: flex;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 0 12px;
  transition: border-color 0.2s;
}

.search-box:hover {
  border-color: #c0c4cc;
}

.flat-select {
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  outline: none;
  cursor: pointer;
  min-width: 100px;
}

.alert-toast {
  padding: 12px 20px;
  border-radius: 6px;
  margin-bottom: 24px;
  font-size: 14px;
  font-weight: 500;
}

.alert-toast.success {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.alert-toast.error {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.alert-toast.warning {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #faecd8;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.modal-content h3 {
  margin-top: 0;
  font-size: 18px;
  color: #303133;
}

.modal-content p {
  color: #606266;
  font-size: 14px;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mt-3 {
  margin-top: 24px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-outline {
  background: white;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.btn-outline:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* CSS cho Table (Tái sử dụng cho Modal) */
.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.vantix-table {
  width: 100%;
  border-collapse: collapse;
}

.vantix-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.vantix-table td {
  padding: 16px;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
  vertical-align: middle;
}

.vantix-table tbody tr:hover {
  background-color: #f5f7fa;
}

.time-text {
  font-family: monospace;
  font-size: 14px;
}

.vt-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
}

.badge-cyan {
  background: #e0f7fa;
  color: #00838f;
}

.badge-gray {
  background: #f0f2f5;
  color: #606266;
}

.badge-success {
  background: #e1f3d8;
  color: #67c23a;
}

.badge-warning {
  background: #fdf0e6;
  color: #e6a23c;
}

.badge-danger {
  background: #fde2e2;
  color: #f56c6c;
}

.badge-default {
  background: #f4f4f5;
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.fw-600 {
  font-weight: 600;
}

/* CSS Lịch (Giữ nguyên, thêm class is-clickable) */
.calendar-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #606266;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  display: inline-block;
}

.dot.bg-success {
  background: #67c23a;
}

.dot.bg-warning {
  background: #e6a23c;
}

.dot.bg-danger {
  background: #f56c6c;
}

.dot.bg-gray {
  background: #dcdfe6;
}

.calendar-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.weekday {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  font-size: 13px;
  color: #909399;
}

.sunday-col {
  color: #f56c6c;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(80px, auto);
}

.calendar-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease;
}

.calendar-cell:nth-child(7n) {
  border-right: none;
}

.calendar-cell.empty {
  background-color: #fafafa;
}

.date-num {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.date-status-text {
  font-size: 12px;
  font-weight: 600;
  margin-top: auto;
}

.status-full {
  background-color: #f0f9eb;
}

.status-full .date-status-text {
  color: #67c23a;
}

.status-half {
  background-color: #fdf6ec;
}

.status-half .date-status-text {
  color: #e6a23c;
}

.status-absent {
  background-color: #fef0f0;
}

.status-absent .date-status-text {
  color: #f56c6c;
}

.status-future, .status-sunday {
  background-color: #fafafa;
}

.status-future .date-status-text, .status-sunday .date-status-text {
  color: #c0c4cc;
}

.status-sunday .date-num {
  color: #f56c6c;
}

/* -- CSS MỚI CHO HOVER CLICK VÀ MODAL DETAIL -- */
.calendar-cell.is-clickable {
  cursor: pointer;
}

.calendar-cell.is-clickable:hover {
  box-shadow: inset 0 0 0 2px #409eff;
  opacity: 0.9;
}

.modal-lg {
  max-width: 650px !important;
}

.modal-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
  margin-bottom: 16px;
}

.modal-header-custom h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
  line-height: 1;
}

.close-btn:hover {
  color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .dashboard-metric-cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .calendar-header .weekday {
    font-size: 11px;
    padding: 8px 4px;
  }

  .calendar-cell {
    padding: 4px;
  }

  .date-status-text {
    font-size: 10px;
  }

  .modal-lg {
    width: 95%;
    padding: 16px;
  }
}
</style>