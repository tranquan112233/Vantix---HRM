<script setup>
import {ref, computed, nextTick, onMounted, onUnmounted, watch} from 'vue';
import ScheduleService from '@/services/schedule.service';

// --- CẤU HÌNH & DỮ LIỆU ---
const currentYear = new Date().getFullYear();
const selectedYear = ref(currentYear);

// ĐỂ TEST: Đổi ID ở đây để xem giao diện thay đổi
// ID 13: Trưởng phòng (Hà Gia Bảo) -> Thấy Sidebar + Tool phân ca
// ID 8: Nhân viên (Vũ Thị Giang) -> Bị ẩn Sidebar, chỉ xem lịch
const currentViewerId = ref(13);

const employees = ref([]);
const selectedEmployeeId = ref(null);

// Biến nhận diện Trưởng phòng hay Nhân viên
const isManager = ref(false);

// --- HỆ THỐNG THÔNG BÁO (TOAST) ---
const message = ref('');
const messageType = ref('success');
let messageTimeout = null;

const showMessage = (text, type = 'success') => {
  message.value = text;
  messageType.value = type;
  if (messageTimeout) clearTimeout(messageTimeout);
  messageTimeout = setTimeout(() => {
    message.value = '';
  }, 5000);
};

// --- XỬ LÝ LỊCH VÀ ACCORDION ---
const expandedMonths = ref([new Date().getMonth() + 1]);

const loadScheduleToCalendar = (month) => {
  schedules.value[month] = {};

  const emp = employees.value.find(e => e.id === selectedEmployeeId.value);

  if (emp && emp.monthlySchedule && emp.monthlySchedule.dailySchedules) {
    emp.monthlySchedule.dailySchedules.forEach(daily => {
      const day = parseInt(daily.workDate.split('-')[2], 10);
      schedules.value[month][day] = daily.shiftId;
    });
  }
};

const fetchSchedules = async (month, year) => {
  try {
    const response = await ScheduleService.getSchedules(currentViewerId.value, month, year);

    let staffList = response.data;

    // Phân quyền dựa trên dữ liệu Backend trả về
    if (staffList.length > 1) {
      isManager.value = true;
      staffList = staffList.filter(emp => emp.employeeId !== currentViewerId.value);
    } else {
      isManager.value = false;
    }

    employees.value = staffList.map(emp => ({
      id: emp.employeeId,
      name: emp.fullName,
      code: emp.employeeCode || `NV${emp.employeeId}`,
      monthlySchedule: emp.monthlySchedule
    }));

    if (employees.value.length > 0 && !selectedEmployeeId.value) {
      selectedEmployeeId.value = employees.value[0].id;
    }

    loadScheduleToCalendar(month);

  } catch (error) {
    console.error("Lỗi khi tải dữ liệu lịch làm việc:", error);
    showMessage("❌ Lỗi kết nối khi tải dữ liệu!", 'error');
  }
};

watch(selectedEmployeeId, () => {
  if (expandedMonths.value.length > 0) {
    loadScheduleToCalendar(expandedMonths.value[0]);
  }
});

onMounted(() => {
  fetchSchedules(expandedMonths.value[0], selectedYear.value);

  setTimeout(() => {
    const currentMonth = expandedMonths.value[0];
    const monthElement = document.getElementById(`month-card-${currentMonth}`);
    if (monthElement) {
      monthElement.scrollIntoView({behavior: 'smooth', block: 'center'});
    }
  }, 100);

  window.addEventListener('mouseup', endDrag);
});

onUnmounted(() => {
  window.removeEventListener('mouseup', endDrag);
});

const toggleMonth = async (month) => {
  const index = expandedMonths.value.indexOf(month);
  if (index > -1) {
    expandedMonths.value.splice(index, 1);
  } else {
    expandedMonths.value = [month];
    await fetchSchedules(month, selectedYear.value);

    await nextTick();
    const monthElement = document.getElementById(`month-card-${month}`);
    if (monthElement) {
      monthElement.scrollIntoView({behavior: 'smooth', block: 'center'});
    }
  }
};

const isMonthExpanded = (month) => expandedMonths.value.includes(month);

const getCalendarDays = (year, month) => {
  const firstDay = new Date(year, month - 1, 1);
  const lastDay = new Date(year, month, 0);

  let startDayOfWeek = firstDay.getDay();
  startDayOfWeek = (startDayOfWeek + 6) % 7;

  const days = [];
  for (let i = 0; i < startDayOfWeek; i++) {
    days.push(null);
  }
  for (let d = 1; d <= lastDay.getDate(); d++) {
    days.push(d);
  }
  return days;
};

// --- LOGIC PHÂN CA LÀM VIỆC & KÉO THẢ ---
// Cập nhật lại đúng ID khớp với Database
const shiftOptions = [
  {id: 1, label: 'Ca Sáng'},
  {id: 2, label: 'Ca Chiều'},
  {id: 3, label: 'Hành Chính'}
];

const schedules = ref({});
// Mặc định chọn Hành Chính (ID 3)
const selectedShift = ref(3);

const dragState = ref({
  active: false,
  month: null,
  startDay: null,
  endDay: null,
  shift: null
});

const isDaySunday = (month, day) => {
  if (!day) return false;
  return new Date(selectedYear.value, month - 1, day).getDay() === 0;
};

const getDayShift = (month, day) => {
  if (!schedules.value[month] || !day) return null;
  return schedules.value[month][day];
};

// Đổi lại màu Badge khớp với đúng ID mới
const getShiftBadgeClass = (shiftId) => {
  if (shiftId === 3) return 'badge-hc';        // 3: Hành chính -> Xanh lá
  if (shiftId === 1) return 'badge-morning';   // 1: Ca sáng -> Xanh dương
  if (shiftId === 2) return 'badge-afternoon'; // 2: Ca chiều -> Cam
  return '';
};

const getShiftName = (shiftId) => {
  const shift = shiftOptions.find(s => s.id === shiftId);
  return shift ? shift.label : '';
};

const startDrag = (month, day) => {
  if (!isManager.value || !day || isDaySunday(month, day)) return;
  dragState.value = {
    active: true,
    month,
    startDay: day,
    endDay: day,
    shift: selectedShift.value
  };
};

const onDragOver = (month, day) => {
  if (!dragState.value.active || dragState.value.month !== month || !day) return;
  dragState.value.endDay = day;
};

const endDrag = () => {
  if (!dragState.value.active) return;
  const {month, startDay, endDay, shift} = dragState.value;

  if (month && startDay && endDay) {
    if (!schedules.value[month]) schedules.value[month] = {};

    if (startDay === endDay) {
      if (schedules.value[month][startDay] === shift) {
        schedules.value[month][startDay] = null;
      } else {
        schedules.value[month][startDay] = shift;
      }
    } else {
      const minDay = Math.min(startDay, endDay);
      const maxDay = Math.max(startDay, endDay);
      for (let d = minDay; d <= maxDay; d++) {
        if (!isDaySunday(month, d)) {
          schedules.value[month][d] = shift;
        }
      }
    }
  }
  dragState.value.active = false;
};

const isInDragRange = (month, day) => {
  if (!dragState.value.active || dragState.value.month !== month || !day) return false;
  if (isDaySunday(month, day)) return false;

  const minDay = Math.min(dragState.value.startDay, dragState.value.endDay);
  const maxDay = Math.max(dragState.value.startDay, dragState.value.endDay);
  return day >= minDay && day <= maxDay;
};

const getEffectiveShift = (month, day) => {
  if (isInDragRange(month, day)) return dragState.value.shift;
  return getDayShift(month, day);
};

const applyMonToSat = (month) => {
  if (!schedules.value[month]) schedules.value[month] = {};
  const daysInMonth = new Date(selectedYear.value, month, 0).getDate();
  for (let d = 1; d <= daysInMonth; d++) {
    if (!isDaySunday(month, d)) {
      schedules.value[month][d] = selectedShift.value;
    }
  }
};

const clearMonth = (month) => {
  if (schedules.value[month]) {
    schedules.value[month] = {};
  }
};

const saveSchedule = async (month) => {
  if (!selectedEmployeeId.value) {
    showMessage("⚠️ Vui lòng chọn một nhân viên trước khi lưu!", 'warning');
    return;
  }

  const dailyData = [];
  const monthData = schedules.value[month];

  if (monthData) {
    for (const [day, shiftId] of Object.entries(monthData)) {
      if (shiftId) {
        const workDate = `${selectedYear.value}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        dailyData.push({
          workDate: workDate,
          shiftId: shiftId
        });
      }
    }
  }

  try {
    await ScheduleService.saveDailySchedules(selectedEmployeeId.value, month, selectedYear.value, dailyData);
    showMessage("🎉 Lưu lịch làm việc thành công!", 'success');
    await fetchSchedules(month, selectedYear.value);
  } catch (error) {
    console.error("Lỗi khi lưu lịch:", error);
    showMessage("❌ Có lỗi xảy ra khi lưu lịch!", 'error');
  }
};

const getCurrentScheduleStatus = () => {
  const emp = employees.value.find(e => e.id === selectedEmployeeId.value);
  if (emp && emp.monthlySchedule) {
    return emp.monthlySchedule.status;
  }
  return null;
};

const toggleLockStatus = async (month) => {
  const emp = employees.value.find(e => e.id === selectedEmployeeId.value);
  if (!emp || !emp.monthlySchedule) return;

  const currentStatus = emp.monthlySchedule.status;
  const newStatus = currentStatus === 'OPEN' ? 'LOCKED' : 'OPEN';
  const actionText = newStatus === 'LOCKED' ? 'Chốt Lịch' : 'Mở Khóa';

  try {
    await ScheduleService.updateStatus(emp.monthlySchedule.monthlyScheduleId, newStatus);
    emp.monthlySchedule.status = newStatus;
    showMessage(`✅ Đã ${actionText} thành công!`, 'success');
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    showMessage("❌ Có lỗi xảy ra khi đổi trạng thái!", 'error');
  }
};
</script>

<template>
  <div class="page-wrapper">

    <transition name="fade">
      <div v-if="message" :class="['alert-toast', messageType]">
        {{ message }}
      </div>
    </transition>

    <div class="schedules-layout">
      <div class="sidebar content-card" v-if="isManager">
        <div class="sidebar-header">
          <h3>Nhân viên ({{ employees.length }})</h3>
        </div>

        <div class="employee-list-container">
          <div v-if="employees.length === 0" class="text-muted" style="padding: 12px; text-align: center;">
            Đang tải dữ liệu hoặc không có nhân viên...
          </div>

          <template v-else>
            <div class="employee-list">
              <label
                  v-for="emp in employees"
                  :key="emp.id"
                  :class="['employee-item', { 'is-selected-emp': selectedEmployeeId === emp.id }]"
              >
                <input
                    type="radio"
                    name="employeeSelection"
                    :value="emp.id"
                    v-model="selectedEmployeeId"
                    class="custom-radio"
                />
                <div class="emp-info">
                  <span class="emp-name">{{ emp.name }}</span>
                  <span class="emp-code">{{ emp.code }}</span>
                </div>
              </label>
            </div>
          </template>
        </div>
      </div>

      <div class="main-content content-card">
        <div class="card-header">
          <div class="header-title">
            <span class="header-icon">🗓️</span>
            <div>
              <h2>{{ isManager ? 'Lịch làm việc tổng hợp' : 'Lịch làm việc cá nhân' }}</h2>
              <p>{{ isManager ? 'Phân ca làm việc cho nhân viên đang chọn' : 'Xem chi tiết lịch làm việc của bạn' }}</p>
            </div>
          </div>
          <div class="header-filters">
            <div class="search-box">
              <select v-model="selectedYear" class="flat-select"
                      @change="fetchSchedules(expandedMonths[0], selectedYear)">
                <option v-for="y in 5" :key="y" :value="currentYear - 2 + y">Năm {{ currentYear - 2 + y }}</option>
              </select>
            </div>
          </div>
        </div>

        <div class="months-container">
          <div
              v-for="month in 12"
              :key="month"
              :id="'month-card-' + month"
              class="month-card"
          >
            <div class="month-header" @click="toggleMonth(month)" :class="{ 'is-open': isMonthExpanded(month) }">
              <div class="month-title">
                <h3>Tháng {{ month }}</h3>
                <span class="month-badge">Năm {{ selectedYear }}</span>
              </div>
              <div class="toggle-icon">
                {{ isMonthExpanded(month) ? '▲' : '▼' }}
              </div>
            </div>

            <transition name="slide-fade">
              <div v-if="isMonthExpanded(month)" class="month-body">

                <div class="quick-actions" v-if="isManager">
                  <div class="shift-selector">
                    <span class="fw-600">Phân ca:</span>
                    <select v-model="selectedShift" class="flat-select shift-select">
                      <option v-for="shift in shiftOptions" :key="shift.id" :value="shift.id">
                        {{ shift.label }}
                      </option>
                    </select>
                  </div>
                  <div class="action-buttons">
                    <button class="btn btn-primary" @click="applyMonToSat(month)">
                      ✅ Chọn T2 - T7
                    </button>
                    <button class="btn btn-outline text-danger" @click="clearMonth(month)">
                      🗑️ Xóa lưới
                    </button>
                    <button class="btn btn-primary" style="background-color: #67c23a;" @click="saveSchedule(month)">
                      💾 Lưu Lịch
                    </button>

                    <button
                        v-if="getCurrentScheduleStatus() === 'OPEN'"
                        class="btn btn-outline"
                        style="border-color: #e6a23c; color: #e6a23c;"
                        @click="toggleLockStatus(month)"
                    >
                      🔓 Đang Nháp (Bấm để Chốt)
                    </button>

                    <button
                        v-if="getCurrentScheduleStatus() === 'LOCKED'"
                        class="btn btn-primary"
                        style="background-color: #f56c6c; border-color: #f56c6c;"
                        @click="toggleLockStatus(month)"
                    >
                      🔒 Đã Chốt (Bấm để Mở)
                    </button>
                  </div>
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
                        v-for="(day, index) in getCalendarDays(selectedYear, month)"
                        :key="index"
                        :class="[
                          'calendar-cell',
                          { 'is-clickable': day && isManager },
                          { 'is-selected': getEffectiveShift(month, day) },
                          { 'is-sunday': isDaySunday(month, day) },
                          { 'is-previewing': isInDragRange(month, day) }
                        ]"
                        @mousedown.prevent="startDrag(month, day)"
                        @mouseenter="onDragOver(month, day)"
                    >
                      <template v-if="day">
                        <div class="date-num">{{ day }}</div>
                        <div class="date-status-text">
                          <span
                              v-if="getEffectiveShift(month, day)"
                              :class="['vt-badge', getShiftBadgeClass(getEffectiveShift(month, day))]"
                          >
                            {{ getShiftName(getEffectiveShift(month, day)) }}
                          </span>
                          <span v-else class="text-muted">Chưa xếp</span>
                        </div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </transition>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* CSS CƠ BẢN */
.page-wrapper {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  font-family: 'Inter', 'Segoe UI', sans-serif;
  box-sizing: border-box;
  scroll-behavior: smooth;
}

/* CSS CHO TOAST NOTIFICATION */
.alert-toast {
  padding: 12px 20px;
  border-radius: 6px;
  margin-bottom: 24px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* CSS COMPONENT KHÁC */
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

.fw-600 {
  font-weight: 600;
}

.schedules-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
  padding: 20px;
  position: sticky;
  top: 24px;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.sidebar-header {
  margin-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.employee-list-container {
  display: flex;
  flex-direction: column;
}

.employee-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
  margin-bottom: 4px;
}

.employee-item:hover {
  background-color: #f5f7fa;
}

.is-selected-emp {
  background-color: #f0f7ff !important;
  border: 1px solid #c6e2ff;
}

.custom-radio {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #409eff;
  margin: 0;
}

.emp-info {
  display: flex;
  flex-direction: column;
}

.emp-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.emp-code {
  font-size: 12px;
  color: #909399;
}

.months-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  scroll-margin-top: 20px;
}

.month-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  scroll-margin-top: 24px;
}

.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #fafafa;
  cursor: pointer;
  transition: background-color 0.2s;
}

.month-header:hover {
  background-color: #f0f2f5;
}

.month-header.is-open {
  background-color: #f0f7ff;
  border-bottom: 1px solid #ebeef5;
}

.month-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.month-title h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.month-badge {
  font-size: 12px;
  background: #e6f1fc;
  color: #409eff;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.toggle-icon {
  color: #909399;
  font-size: 12px;
}

.month-body {
  padding: 20px;
  background-color: #fff;
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from, .slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}

.shift-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shift-select {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 6px 12px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
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

.text-danger {
  color: #f56c6c !important;
}

.text-danger:hover {
  color: white !important;
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
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
  user-select: none;
}

.calendar-cell {
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease;
  background-color: #fafafa;
}

.calendar-cell:nth-child(7n) {
  border-right: none;
}

.calendar-cell.is-clickable {
  cursor: pointer;
}

.calendar-cell.is-clickable:hover {
  box-shadow: inset 0 0 0 2px #409eff;
  opacity: 0.9;
}

.calendar-cell.is-selected {
  background-color: #f4fcf0 !important;
}

.calendar-cell.is-previewing {
  background-color: #eef2f9 !important;
}

.vt-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
  font-size: 12px;
}

.badge-hc {
  background: #e1f3d8;
  color: #529b2e;
}

.badge-morning {
  background: #e6f1fc;
  color: #409eff;
}

.badge-afternoon {
  background: #fdf0e6;
  color: #e6a23c;
}

.is-sunday .date-num {
  color: #f56c6c;
}

.date-num {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.date-status-text {
  font-size: 12px;
  margin-top: auto;
}

.text-muted {
  color: #c0c4cc;
  font-weight: normal;
}

@media (max-width: 992px) {
  .schedules-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: static;
  }

  .quick-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .action-buttons {
    justify-content: flex-end;
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
}
</style>