<script setup>
import {ref, computed, onMounted, watch} from 'vue';
import attendanceManagementService from "@/services/attendancemanagement.service.js";
import { useAuthStore } from '@/stores/auth.store';

// --- CẤU HÌNH ---
const auth = useAuthStore();
const managerId  = computed(() => auth.user?.employeeId ?? null);
const canApprove = computed(() => auth.can('ATTENDANCE_MANAGEMENT_APPROVE'));

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
  return shiftObj.shiftName || 'Khác';
};

const getShiftBadgeClass = (shiftObj) => {
  if (!shiftObj) return 'badge-gray';
  const name = (shiftObj.shiftName || '').toLowerCase();
  if (name.includes('sáng') || name.includes('sang')) return 'shift-morning';
  if (name.includes('chiều') || name.includes('chieu')) return 'shift-afternoon';
  if (name.includes('hành chính') || name.includes('hanh chinh') || name.includes('full')) return 'shift-full';
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
  if (!managerId.value) return;
  loading.value = true;
  try {
    // Chỉ gọi 1 API duy nhất
    const response = await attendanceManagementService.getPendingAttendances(managerId.value);
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
    const pendingCount = attendanceList.value.filter(
        att => att.employee.id === emp.id && att.status === 'PENDING'
    ).length;
    return {...emp, pendingCount};
  });
});

const currentEmployeeAttendances = computed(() => {
  if (!selectedEmployee.value) return [];
  return attendanceList.value.filter(
      att => att.employee.id === selectedEmployee.value.id && att.status === 'PENDING'
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

const handleReject = async () => {
  try {
    await attendanceManagementService.rejectAttendance(selectedRecord.value.attendanceId);
    showMessage(`Đã từ chối chấm công của ${selectedRecord.value.employee.name} (Ngày ${selectedRecord.value.workDate}).`, 'error');

    const index = attendanceList.value.findIndex(a => a.attendanceId === selectedRecord.value.attendanceId);
    if (index !== -1) attendanceList.value.splice(index, 1);

    showDetailModal.value = false;
  } catch (error) {
    console.error("Lỗi từ chối công:", error);
    showMessage("Từ chối thất bại.", "error");
  }
};

const handleCancel = () => {
  showDetailModal.value = false;
};

onMounted(() => {
  fetchData();
});

watch(managerId, (newVal) => {
  if (newVal) fetchData();
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
            <span v-if="emp.pendingCount > 0" class="badge-notification">
              {{ emp.pendingCount }}
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
                    <span :class="['vt-badge', getShiftBadgeClass(att.shift)]">
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
              <span :class="['vt-badge', getShiftBadgeClass(selectedRecord.shift)]">
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
            * Ca làm việc đang chờ phê duyệt (<b>PENDING</b>). Vui lòng xem xét và chọn Duyệt hoặc Từ chối.
          </p>
        </div>

        <div class="modal-actions mt-3">
          <button class="btn btn-outline" @click="handleCancel">Hủy</button>
          <button v-if="canApprove" class="btn btn-danger" @click="handleReject">Từ Chối</button>
          <button v-if="canApprove" class="btn btn-primary" @click="handleApprove">Đồng Ý Duyệt</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* { box-sizing: border-box; }

.page-wrapper {
  padding: 28px 32px;
  background: #f0f4ff;
  min-height: 100vh;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

.layout-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* Header */
.header-title {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  flex-shrink: 0;
}

.header-title h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.header-title p {
  margin: 2px 0 0 0;
  font-size: 13px;
  color: #64748b;
}

.mb-4 {
  margin-bottom: 24px;
}

.text-primary {
  color: #4f46e5;
}

/* Sidebar */
.sidebar-card {
  width: 300px;
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  border: 1.5px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1.5px solid #e8edff;
  background: #fafbff;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.employee-list { max-height: 600px; overflow-y: auto; }

.employee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 18px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
}

.employee-item:hover { background: #fafbff; }

.employee-item.active {
  background: #eef2ff;
  border-left: 3px solid #6366f1;
  padding-left: 15px;
}

.emp-info { display: flex; align-items: center; gap: 11px; }

.emp-avatar {
  width: 38px; height: 38px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.emp-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.emp-code { font-size: 11px; color: #94a3b8; margin-top: 1px; }

.badge-notification {
  background: linear-gradient(135deg, #f43f5e, #dc2626);
  color: white;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(220, 38, 38, 0.3);
}

.status-ok { font-size: 16px; opacity: 0.4; }

/* Main Card */
.main-card {
  flex-grow: 1;
  background: white;
  border-radius: 16px;
  border: 1.5px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  min-height: 400px;
  overflow: hidden;
}

.main-header {
  padding: 18px 22px;
  border-bottom: 1.5px solid #e8edff;
  background: #fafbff;
}

.main-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.table-responsive { width: 100%; overflow-x: auto; }

.vantix-table { width: 100%; border-collapse: collapse; font-family: inherit; }

.vantix-table th {
  padding: 12px 18px;
  text-align: left;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  border-bottom: 1.5px solid #e8edff;
  background: #fafbff;
}

.vantix-table td {
  padding: 14px 18px;
  font-size: 14px;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.clickable-row { cursor: pointer; transition: background 0.15s; }
.clickable-row:hover { background: #fafbff !important; }

/* Empty States */
.empty-state { text-align: center; padding: 40px 20px; color: #94a3b8; }
.select-prompt {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  height: 100%; min-height: 300px;
}
.empty-icon { font-size: 36px; display: block; margin-bottom: 12px; }

/* Badges */
.time-text { font-family: 'Courier New', monospace; font-size: 14px; }

.vt-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
}

.shift-morning  { background: #ede9fe; color: #5b21b6; }
.shift-afternoon{ background: #fef3c7; color: #92400e; }
.shift-full     { background: #d1fae5; color: #065f46; }
.badge-gray     { background: #f1f5f9; color: #64748b; }
.badge-success  { background: #d1fae5; color: #065f46; }
.badge-warning  { background: #fef3c7; color: #92400e; }
.badge-danger   { background: #fee2e2; color: #991b1b; }
.badge-default  { background: #f1f5f9; color: #64748b; }

.text-danger  { color: #dc2626; }
.text-success { color: #059669; }
.fw-600       { font-weight: 600; }

/* Modal */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(15, 23, 42, 0.45);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 460px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  animation: modalIn 0.25s ease-out;
  overflow: hidden;
}

@keyframes modalIn {
  from { transform: translateY(-16px) scale(0.97); opacity: 0; }
  to   { transform: translateY(0) scale(1); opacity: 1; }
}

.modal-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-header-custom h3 { margin: 0; font-size: 17px; font-weight: 700; color: #0f172a; }

.close-btn {
  background: #f1f5f9;
  border: none;
  width: 30px; height: 30px;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  color: #64748b;
  display: flex; align-items: center; justify-content: center;
}
.close-btn:hover { background: #fee2e2; color: #dc2626; }

.info-group { margin-bottom: 12px; font-size: 14px; color: #334155; }
.info-group label { color: #64748b; display: inline-block; width: 110px; font-size: 13px; }

.time-breakdown {
  display: flex; gap: 16px;
  background: #f8faff;
  padding: 16px;
  border-radius: 12px;
  border: 1.5px solid #e8edff;
}

.time-box { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; }
.box-label { font-size: 12px; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; }
.box-value { font-size: 20px; font-weight: 700; color: #0f172a; font-family: 'Courier New', monospace; }
.box-alert { font-size: 12px; font-weight: 700; }

.warning-text {
  font-size: 13px; color: #92400e; line-height: 1.5;
  background: #fefce8; padding: 12px 14px;
  border-radius: 10px; border: 1px solid #fde68a;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
.mt-3 { margin-top: 20px; }

/* Buttons */
.btn {
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}
.btn-primary:hover { box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4); transform: translateY(-1px); }

.btn-outline {
  background: white;
  border: 1.5px solid #e2e8f0;
  color: #475569;
}
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }

.btn-danger {
  background: linear-gradient(135deg, #f43f5e, #dc2626);
  color: white;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.25);
}
.btn-danger:hover { box-shadow: 0 6px 16px rgba(220, 38, 38, 0.35); transform: translateY(-1px); }

/* Toast */
.alert-toast {
  padding: 12px 18px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
}
.alert-toast.success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; }
.alert-toast.error   { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>