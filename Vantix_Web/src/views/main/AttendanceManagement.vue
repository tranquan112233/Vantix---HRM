<script setup>
import {ref, computed, onMounted} from 'vue';
import attendanceManagementService from "@/services/attendancemanagement.service.js";

// --- CẤU HÌNH ---
const managerId = ref(13);

// --- FORMATTER & HELPERS ---
const formatTime = (timeStr) => {
  if (!timeStr) return '--:--';
  if (Array.isArray(timeStr)) {
    return `${String(timeStr[0]).padStart(2, '0')}:${String(timeStr[1]).padStart(2, '0')}`;
  }
  return timeStr.slice(0, 5);
};

const getShiftLabel = (shiftObj) => {
  if (!shiftObj) return 'Khác';
  if (shiftObj.shiftId === 1) return 'Ca sáng';
  if (shiftObj.shiftId === 2) return 'Ca chiều';
  if (shiftObj.shiftId === 3) return 'Hành chính';
  return shiftObj.shiftName || 'Khác';
};

const getShiftBadgeClass = (shiftId) => {
  if (shiftId === 1) return 'shift-morning';
  if (shiftId === 2) return 'shift-afternoon';
  if (shiftId === 3) return 'shift-full';
  return 'badge-gray';
};

const getStatusClass = (status) => {
  if (!status) return 'badge-default';
  const s = status.toLowerCase();
  if (s === 'approved') return 'badge-success';
  if (s === 'pending') return 'badge-warning';
  if (s === 'rejected') return 'badge-danger';
  return 'badge-default';
};

// --- STATE ---
const message = ref('');
const messageType = ref('success');
const loading = ref(false);

const attendanceList = ref([]);
const departmentEmployees = ref([]);
const selectedEmployee = ref(null);
const showDetailModal = ref(false);
const selectedRecord = ref(null);

let messageTimeout = null;

const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;
  if (messageTimeout) clearTimeout(messageTimeout);
  messageTimeout = setTimeout(() => {
    message.value = '';
  }, 5000);
};

// --- API CALLS ---
const fetchData = async () => {
  loading.value = true;
  try {
    // Chỉ gọi 1 API duy nhất
    const response = await attendanceManagementService.getRejectedAttendances(managerId.value);
    const data = response.data;

    const uniqueEmployees = new Map();
    const validAttendances = [];

    // Tách dữ liệu trả về giống như bóc tách kết quả Left Join
    data.forEach(item => {
      // 1. Đưa vào danh sách nhân viên duy nhất (bất kể có lỗi hay không)
      if (!uniqueEmployees.has(item.employeeId)) {
        uniqueEmployees.set(item.employeeId, {
          id: item.employeeId,
          name: item.fullName,
          code: `NV${String(item.employeeId).padStart(3, '0')}`,
          avatar: '👤' // Có thể tự set icon ngoài frontend
        });
      }

      // 2. Nếu attendanceId tồn tại (khác null) -> Có vi phạm -> Push vào attendanceList
      if (item.attendanceId) {
        validAttendances.push({
          attendanceId: item.attendanceId,
          employee: {id: item.employeeId, name: item.fullName, code: `NV${String(item.employeeId).padStart(3, '0')}`},
          workDate: item.workDate,
          shift: {shiftId: item.shiftId, shiftName: item.shiftName},
          checkIn: item.checkIn,
          checkOut: item.checkOut,
          lateMinutes: item.lateMinutes,
          earlyLeaveMinutes: item.earlyLeaveMinutes,
          status: item.status
        });
      }
    });

    departmentEmployees.value = Array.from(uniqueEmployees.values());
    attendanceList.value = validAttendances;

  } catch (error) {
    console.error("Lỗi lấy dữ liệu:", error);
    showMessage("Không thể tải dữ liệu.", "error");
  } finally {
    loading.value = false;
  }
};

// --- COMPUTED: Xử lý logic đếm và lọc ---
const employeesWithStats = computed(() => {
  return departmentEmployees.value.map(emp => {
    const rejectedCount = attendanceList.value.filter(
        att => att.employee.id === emp.id && att.status === 'REJECTED'
    ).length;
    return {...emp, rejectedCount};
  });
});

const currentEmployeeAttendances = computed(() => {
  if (!selectedEmployee.value) return [];
  return attendanceList.value.filter(
      att => att.employee.id === selectedEmployee.value.id && att.status === 'REJECTED'
  );
});

// --- THAO TÁC ---
const selectEmployee = (emp) => {
  selectedEmployee.value = emp;
};

const openDetail = (record) => {
  selectedRecord.value = {...record};
  showDetailModal.value = true;
};

const handleApprove = async () => {
  try {
    await attendanceManagementService.approveAttendance(selectedRecord.value.attendanceId);
    showMessage(`✅ Đã phê duyệt công cho ${selectedRecord.value.employee.name} (Ngày ${selectedRecord.value.workDate}).`, 'success');

    // Xóa khỏi UI
    const index = attendanceList.value.findIndex(a => a.attendanceId === selectedRecord.value.attendanceId);
    if (index !== -1) attendanceList.value.splice(index, 1);

    showDetailModal.value = false;
  } catch (error) {
    console.error("Lỗi duyệt công:", error);
    showMessage("Duyệt công thất bại.", "error");
  }
};

const handleCancel = () => {
  showDetailModal.value = false;
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="page-wrapper">
    <transition name="fade">
      <div v-if="message" :class="['alert-toast', messageType]">
        {{ message }}
      </div>
    </transition>

    <div class="header-title mb-4">
      <span class="header-icon">🛡️</span>
      <div>
        <h2>Phê Duyệt Chấm Công</h2>
        <p>Chọn nhân viên trong phòng ban để xem và duyệt các ca làm vi phạm</p>
      </div>
    </div>

    <div class="layout-container">

      <div class="sidebar-card">
        <div class="sidebar-header">
          <h3>Nhân viên phòng ban</h3>
        </div>

        <div v-if="loading" class="empty-state" style="padding: 20px;">
          <p>Đang tải dữ liệu...</p>
        </div>

        <div v-else class="employee-list">
          <div
              v-for="emp in employeesWithStats"
              :key="emp.id"
              :class="['employee-item', { 'active': selectedEmployee?.id === emp.id }]"
              @click="selectEmployee(emp)"
          >
            <div class="emp-info">
              <span class="emp-avatar">{{ emp.avatar }}</span>
              <div>
                <div class="emp-name">{{ emp.name }}</div>
                <div class="emp-code">{{ emp.code }}</div>
              </div>
            </div>
            <span v-if="emp.rejectedCount > 0" class="badge-notification">
              {{ emp.rejectedCount }}
            </span>
            <span v-else class="status-ok">✔️</span>
          </div>

          <div v-if="employeesWithStats.length === 0" class="empty-state" style="padding: 20px;">
            <p>Phòng ban chưa có nhân viên.</p>
          </div>
        </div>
      </div>

      <div class="main-card">
        <div v-if="selectedEmployee" class="main-content-inner">
          <div class="main-header">
            <h3>Ca làm việc cần duyệt của: <span class="text-primary">{{ selectedEmployee.name }}</span></h3>
          </div>

          <div class="table-responsive">
            <table class="vantix-table">
              <thead>
              <tr>
                <th>Ngày làm việc</th>
                <th>Ca làm</th>
                <th>Trạng thái</th>
              </tr>
              </thead>
              <tbody>
              <tr
                  v-for="att in currentEmployeeAttendances"
                  :key="att.attendanceId"
                  class="clickable-row"
                  @click="openDetail(att)"
              >
                <td class="fw-600">{{ att.workDate }}</td>
                <td>
                    <span :class="['vt-badge', getShiftBadgeClass(att.shift?.shiftId)]">
                      {{ getShiftLabel(att.shift) }}
                    </span>
                </td>
                <td>
                  <span :class="['vt-badge', getStatusClass(att.status)]">{{ att.status }}</span>
                </td>
              </tr>
              <tr v-if="currentEmployeeAttendances.length === 0">
                <td colspan="3" class="empty-state">
                  <span class="empty-icon">🎉</span>
                  <p>Nhân viên này không có ca làm việc nào cần phê duyệt.</p>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else class="empty-state select-prompt">
          <span class="empty-icon">👈</span>
          <p>Vui lòng chọn một nhân viên từ danh sách bên trái để xem chi tiết.</p>
        </div>
      </div>

    </div>

    <div v-if="showDetailModal" class="modal-overlay" @click.self="handleCancel">
      <div class="modal-content">
        <div class="modal-header-custom">
          <h3>Chi tiết chấm công</h3>
          <button class="close-btn" @click="handleCancel">&times;</button>
        </div>

        <div class="modal-body">
          <div class="info-group">
            <label>Nhân viên:</label>
            <span><b>{{ selectedRecord.employee.name }}</b> ({{ selectedRecord.employee.code }})</span>
          </div>
          <div class="info-group">
            <label>Ngày / Ca làm:</label>
            <span>{{ selectedRecord.workDate }} -
              <span :class="['vt-badge', getShiftBadgeClass(selectedRecord.shift?.shiftId)]">
                {{ getShiftLabel(selectedRecord.shift) }}
              </span>
            </span>
          </div>

          <div class="time-breakdown mt-3">
            <div class="time-box">
              <span class="box-label">Giờ Vào</span>
              <span class="box-value time-text">{{ formatTime(selectedRecord.checkIn) }}</span>
              <span v-if="selectedRecord.lateMinutes > 0"
                    class="box-alert text-danger">Trễ {{ selectedRecord.lateMinutes }}p</span>
              <span v-else class="box-alert text-success">Đúng giờ</span>
            </div>

            <div class="time-box">
              <span class="box-label">Giờ Ra</span>
              <span class="box-value time-text">{{ formatTime(selectedRecord.checkOut) }}</span>
              <span v-if="selectedRecord.earlyLeaveMinutes > 0"
                    class="box-alert text-danger">Về sớm {{ selectedRecord.earlyLeaveMinutes }}p</span>
              <span v-else class="box-alert text-success">Đúng giờ</span>
            </div>
          </div>

          <p class="warning-text mt-3">
            * Hệ thống đã đánh dấu <b>REJECTED</b> do vi phạm thời gian làm việc. Bạn có muốn du di và phê duyệt cho
            nhân viên này không?
          </p>
        </div>

        <div class="modal-actions mt-3">
          <button class="btn btn-outline" @click="handleCancel">Cancel</button>
          <button class="btn btn-primary" @click="handleApprove">Đồng Ý Duyệt</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Biến toàn cục & Layout */
.page-wrapper {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  font-family: 'Inter', 'Segoe UI', sans-serif;
  box-sizing: border-box;
}

.layout-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* Header Chung */
.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  background: #fff2e8;
  padding: 8px 12px;
  border-radius: 8px;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.header-title p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.mb-4 {
  margin-bottom: 24px;
}

.text-primary {
  color: #409eff;
}

/* Cột Trái: Sidebar danh sách nhân viên */
.sidebar-card {
  width: 320px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.employee-list {
  max-height: 600px;
  overflow-y: auto;
}

.employee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
}

.employee-item:hover {
  background-color: #f5f7fa;
}

.employee-item.active {
  background-color: #ecf5ff;
  border-left: 4px solid #409eff;
  padding-left: 16px;
}

.emp-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.emp-avatar {
  font-size: 24px;
  background: #f0f2f5;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emp-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.emp-code {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* Chấm đỏ Badge */
.badge-notification {
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.2);
}

.status-ok {
  font-size: 14px;
  opacity: 0.5;
}

/* Cột Phải: Bảng chi tiết */
.main-card {
  flex-grow: 1;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  min-height: 400px;
}

.main-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.main-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.vantix-table {
  width: 100%;
  border-collapse: collapse;
}

.vantix-table th {
  padding: 12px 20px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.vantix-table td {
  padding: 16px 20px;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
  vertical-align: middle;
}

.clickable-row {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.clickable-row:hover {
  background-color: #f0f7ff !important;
}

/* Empty States */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.select-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
}

.empty-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}

/* Badges & Text */
.time-text {
  font-family: monospace;
  font-size: 14px;
}

.vt-badge {
  display: inline-block;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 6px;
}

.shift-morning {
  background: #eef5fe;
  color: #5097fa;
}

.shift-afternoon {
  background: #fff2e8;
  color: #f89e5a;
}

.shift-full {
  background: #eaf5e8;
  color: #62a353;
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

.text-success {
  color: #67c23a;
}

.fw-600 {
  font-weight: 600;
}

/* Modal & Component nhỏ */
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
  max-width: 450px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
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

.info-group {
  margin-bottom: 12px;
  font-size: 14px;
}

.info-group label {
  color: #909399;
  display: inline-block;
  width: 100px;
}

.time-breakdown {
  display: flex;
  gap: 16px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.time-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.box-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.box-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.box-alert {
  font-size: 12px;
  font-weight: 600;
}

.warning-text {
  font-size: 13px;
  color: #e6a23c;
  line-height: 1.5;
  background: #fdf6ec;
  padding: 10px;
  border-radius: 6px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.mt-3 {
  margin-top: 24px;
}

/* Buttons & Alerts */
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

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>