<script setup>
import {ref, computed, nextTick, onMounted, onUnmounted, watch} from 'vue';
import ScheduleService from '@/services/schedule.service';
import { useAuthStore } from '@/stores/auth.store';

// --- CẤU HÌNH & DỮ LIỆU ---
const currentYear = new Date().getFullYear();
const selectedYear = ref(currentYear);

const auth = useAuthStore();
const currentViewerId  = computed(() => auth.user?.employeeId ?? null);
const canManageSchedule = computed(() => auth.can('SCHEDULE_CREATE'));
const canUpdateStatus   = computed(() => auth.can('SCHEDULE_STATUS_UPDATE'));

const employees = ref([]);
const selectedEmployeeId = ref(null);
const selectedEmployee = computed(() => employees.value.find(emp => emp.id === selectedEmployeeId.value) || null);

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
  if (!currentViewerId.value) return;
  try {
    const response = await ScheduleService.getSchedules(currentViewerId.value, month, year);

    let staffList = response.data;

    // Phân quyền dựa trên field manager từ backend trả về
    const viewerData = staffList.find(emp => emp.employeeId === currentViewerId.value);
    isManager.value = viewerData?.manager ?? false;

    // Manager xem lịch toàn bộ nhân viên (không hiển thị chính mình trong sidebar)
    if (isManager.value) {
      staffList = staffList.filter(emp => emp.employeeId !== currentViewerId.value);
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
    showMessage("Không thể tải dữ liệu lịch làm việc.", 'error');
  }
};

watch(selectedEmployeeId, () => {
  if (expandedMonths.value.length > 0) {
    loadScheduleToCalendar(expandedMonths.value[0]);
  }
});

onMounted(async () => {
  if (!auth.user && auth.token) await auth.fetchMe();
  await fetchSchedules(expandedMonths.value[0], selectedYear.value);

  setTimeout(() => {
    const currentMonth = expandedMonths.value[0];
    const monthElement = document.getElementById(`month-card-${currentMonth}`);
    if (monthElement) {
      monthElement.scrollIntoView({behavior: 'smooth', block: 'center'});
    }
  }, 100);

  window.addEventListener('mouseup', endDrag);
});

watch(currentViewerId, (newVal) => {
  if (newVal) fetchSchedules(expandedMonths.value[0], selectedYear.value);
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
    showMessage("Vui lòng chọn một nhân viên trước khi lưu.", 'warning');
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
    showMessage("Lưu lịch làm việc thành công.", 'success');
    await fetchSchedules(month, selectedYear.value);
  } catch (error) {
    console.error("Lỗi khi lưu lịch:", error);
    showMessage("Có lỗi xảy ra khi lưu lịch.", 'error');
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
    showMessage(`Đã ${actionText.toLowerCase()} thành công.`, 'success');
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);
    showMessage("Có lỗi xảy ra khi đổi trạng thái.", 'error');
  }
};
</script>

<template>
  <div class="schedule-page mgmt-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Lịch làm việc</h1>
        <p class="page-desc">
          {{ isManager ? 'Phân ca cho nhân viên đang chọn và chốt lịch làm việc theo tháng.' : 'Xem lịch làm việc cá nhân theo từng tháng.' }}
        </p>
      </div>
      <div class="header-actions">
        <span v-if="isManager && selectedEmployee" class="role-badge selected-employee">
          <i class="bi bi-person-circle"></i>
          {{ selectedEmployee.name }}
        </span>
        <div class="select-wrapper compact-select">
          <select v-model="selectedYear" @change="fetchSchedules(expandedMonths[0], selectedYear)">
            <option v-for="y in 5" :key="y" :value="currentYear - 2 + y">Năm {{ currentYear - 2 + y }}</option>
          </select>
          <i class="bi bi-chevron-down"></i>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="message" :class="['alert-toast', messageType]">
        {{ message }}
      </div>
    </transition>

    <div class="schedule-layout">
      <aside v-if="isManager" class="sidebar-card team-sidebar">
        <div class="panel-heading">
          <i class="bi bi-people"></i>
          <span>Nhân viên</span>
        </div>
        <p class="panel-copy">Chọn nhân viên để phân ca, lưu và khóa lịch theo từng tháng.</p>

        <div v-if="employees.length === 0" class="empty-sidebar">
          <i class="bi bi-people"></i>
          <span>Đang tải dữ liệu hoặc chưa có nhân viên khả dụng.</span>
        </div>

        <div v-else class="employee-list">
          <label
            v-for="emp in employees"
            :key="emp.id"
            :class="['employee-item', { 'is-selected-emp': selectedEmployeeId === emp.id }]"
          >
            <input
              v-model="selectedEmployeeId"
              type="radio"
              name="employeeSelection"
              :value="emp.id"
              class="custom-radio"
            />
            <div class="emp-info">
              <span class="emp-name">{{ emp.name }}</span>
              <span class="emp-code">{{ emp.code }}</span>
            </div>
          </label>
        </div>
      </aside>

      <div class="content-card schedule-content">
        <div class="card-header">
          <div class="header-title">
            <span class="header-icon"><i class="bi bi-calendar3"></i></span>
            <div>
              <h2>{{ isManager ? 'Lịch làm việc tổng hợp' : 'Lịch làm việc cá nhân' }}</h2>
              <p>{{ isManager ? 'Phân ca cho nhân viên đang chọn bằng thao tác kéo thả trên lịch.' : 'Theo dõi các ca làm việc đã được phân cho bạn.' }}</p>
            </div>
          </div>
        </div>

        <div class="months-container">
          <div v-for="month in 12" :key="month" :id="'month-card-' + month" class="month-card">
            <div class="month-header" :class="{ 'is-open': isMonthExpanded(month) }" @click="toggleMonth(month)">
              <div class="month-title">
                <h3>Tháng {{ month }}</h3>
                <span class="month-badge">Năm {{ selectedYear }}</span>
                <span
                  v-if="isMonthExpanded(month) && isManager && getCurrentScheduleStatus()"
                  :class="['status-badge', getCurrentScheduleStatus() === 'LOCKED' ? 'resigned' : 'open']"
                >
                  {{ getCurrentScheduleStatus() === 'LOCKED' ? 'Đã khóa' : 'Đang mở' }}
                </span>
              </div>
              <i :class="['bi month-toggle', isMonthExpanded(month) ? 'bi-chevron-up' : 'bi-chevron-down']"></i>
            </div>

            <transition name="slide-fade">
              <div v-if="isMonthExpanded(month)" class="month-body">
                <div v-if="isManager" class="quick-actions">
                  <div class="shift-selector">
                    <span class="selector-label">Ca làm</span>
                    <div class="select-wrapper shift-select-wrap">
                      <select v-model="selectedShift">
                        <option v-for="shift in shiftOptions" :key="shift.id" :value="shift.id">
                          {{ shift.label }}
                        </option>
                      </select>
                      <i class="bi bi-chevron-down"></i>
                    </div>
                  </div>

                  <div class="action-buttons">
                    <button v-if="canManageSchedule" class="btn-ghost" @click="applyMonToSat(month)">
                      <i class="bi bi-calendar-week"></i>
                      Chọn T2 - T7
                    </button>
                    <button v-if="canManageSchedule" class="btn-ghost danger-ghost" @click="clearMonth(month)">
                      <i class="bi bi-eraser"></i>
                      Xóa lưới
                    </button>
                    <button v-if="canManageSchedule" class="btn-primary" @click="saveSchedule(month)">
                      <i class="bi bi-floppy"></i>
                      Lưu lịch
                    </button>

                    <button
                      v-if="canUpdateStatus && getCurrentScheduleStatus() === 'OPEN'"
                      class="btn-ghost warning-ghost"
                      @click="toggleLockStatus(month)"
                    >
                      <i class="bi bi-lock"></i>
                      Chốt lịch
                    </button>

                    <button
                      v-if="canUpdateStatus && getCurrentScheduleStatus() === 'LOCKED'"
                      class="btn-danger"
                      @click="toggleLockStatus(month)"
                    >
                      <i class="bi bi-unlock"></i>
                      Mở khóa
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
.schedule-page {
  gap: 22px;
}

.schedule-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.team-sidebar,
.schedule-content {
  padding: 24px;
}

.team-sidebar {
  position: sticky;
  top: 32px;
}

.panel-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  color: #222;
  font-size: 14px;
  font-weight: 700;
}

.panel-heading i {
  color: var(--primary-color);
}

.panel-copy {
  margin: 0 0 18px;
  color: #888;
  font-size: 13px;
  line-height: 1.7;
}

.compact-select {
  min-width: 150px;
}

.selected-employee {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.empty-sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 28px 12px;
  color: var(--text-muted);
  text-align: center;
}

.empty-sidebar i {
  font-size: 28px;
  color: var(--text-dim);
}

.employee-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.employee-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  background: #fafafa;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.employee-item:hover {
  background: #f7f7ff;
  border-color: #d8def7;
  transform: translateY(-1px);
}

.is-selected-emp {
  background: #ede9fe;
  border-color: #c7d2fe;
}

.custom-radio {
  width: 16px;
  height: 16px;
  margin: 0;
  accent-color: #6366f1;
}

.emp-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.emp-name {
  color: var(--text-dark);
  font-size: 13px;
  font-weight: 700;
}

.emp-code {
  color: var(--text-dim);
  font-size: 11px;
}

.months-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.month-card {
  border: 1px solid #f0f0f0;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.month-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  background: #fafafa;
  cursor: pointer;
  transition: background 0.2s ease;
}

.month-header:hover {
  background: #f5f5fb;
}

.month-header.is-open {
  background: #f7f7ff;
}

.month-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.month-title h3 {
  margin: 0;
  color: #222;
  font-size: 15px;
  font-weight: 700;
}

.month-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f2f4f7;
  color: #475467;
  font-size: 11px;
  font-weight: 700;
}

.month-toggle {
  color: #888;
  font-size: 12px;
}

.month-body {
  padding: 18px;
  border-top: 1px solid #f0f0f0;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px;
  margin-bottom: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  background: #fafafa;
}

.shift-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.selector-label {
  color: #555;
  font-size: 13px;
  font-weight: 600;
}

.shift-select-wrap {
  min-width: 180px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.warning-ghost {
  border-color: #fedf89;
  color: #b54708;
}

.warning-ghost:hover {
  background: #fffaeb;
  border-color: #f7c65c;
  color: #8a5a00;
}

.danger-ghost {
  border-color: #fecdca;
  color: #b42318;
}

.danger-ghost:hover {
  background: #fef3f2;
  border-color: #fda29b;
  color: #912018;
}

.date-status-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.calendar-cell.is-selected {
  background: #ecfdf3 !important;
}

.calendar-cell.is-previewing {
  background: #eef4ff !important;
}

.calendar-cell.is-sunday {
  background: #fafafa;
}

.calendar-cell.is-clickable:hover {
  box-shadow: inset 0 0 0 1px rgba(70, 95, 255, 0.24);
  background: #f8fbff;
}

.slide-fade-enter-active {
  transition: all 0.24s ease;
}

.slide-fade-leave-active {
  transition: all 0.18s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 992px) {
  .schedule-layout {
    grid-template-columns: 1fr;
  }

  .team-sidebar {
    position: static;
  }

  .action-buttons {
    justify-content: flex-start;
  }
}
</style>
