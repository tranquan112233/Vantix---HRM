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
                    <button v-if="canManageSchedule" class="btn btn-primary" @click="applyMonToSat(month)">
                      ✅ Chọn T2 - T7
                    </button>
                    <button v-if="canManageSchedule" class="btn btn-outline text-danger" @click="clearMonth(month)">
                      🗑️ Xóa lưới
                    </button>
                    <button v-if="canManageSchedule" class="btn btn-primary" style="background-color: #67c23a;" @click="saveSchedule(month)">
                      💾 Lưu Lịch
                    </button>

                    <button
                        v-if="canUpdateStatus && getCurrentScheduleStatus() === 'OPEN'"
                        class="btn btn-outline"
                        style="border-color: #e6a23c; color: #e6a23c;"
                        @click="toggleLockStatus(month)"
                    >
                      🔓 Đang Nháp (Bấm để Chốt)
                    </button>

                    <button
                        v-if="canUpdateStatus && getCurrentScheduleStatus() === 'LOCKED'"
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
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* { box-sizing: border-box; }

/* ── Base ── */
.page-wrapper {
  padding: 28px 32px;
  background: #f0f4ff;
  min-height: 100vh;
  font-family: 'Plus Jakarta Sans', sans-serif;
  scroll-behavior: smooth;
}

/* ── Toast ── */
.alert-toast {
  padding: 12px 18px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
}
.alert-toast.success { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; }
.alert-toast.error   { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.alert-toast.warning { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ── Cards ── */
.content-card {
  background: white;
  border-radius: 16px;
  border: 1.5px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 22px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-title { display: flex; align-items: center; gap: 14px; }

.header-icon {
  width: 46px; height: 46px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.32);
  flex-shrink: 0;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.4px;
}

.header-title p { margin: 2px 0 0 0; font-size: 13px; color: #64748b; }

.search-box {
  display: flex;
  align-items: center;
  background: #fafbff;
  border: 1.5px solid #e8edff;
  border-radius: 10px;
  padding: 0 12px;
  transition: border-color 0.2s;
}
.search-box:hover { border-color: #a5b4fc; }

.flat-select {
  border: none; background: transparent;
  padding: 8px 4px; font-size: 14px; color: #334155;
  outline: none; cursor: pointer; min-width: 100px;
  font-family: inherit;
}

.fw-600 { font-weight: 600; }

/* ── Layout ── */
.schedules-layout { display: flex; gap: 22px; align-items: flex-start; }

.sidebar {
  width: 270px; flex-shrink: 0;
  padding: 18px;
  position: sticky; top: 24px;
}

.main-content { flex: 1; min-width: 0; }

.sidebar-header {
  margin-bottom: 14px;
  border-bottom: 1.5px solid #e8edff;
  padding-bottom: 12px;
}
.sidebar-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #0f172a; }

.employee-list-container { display: flex; flex-direction: column; gap: 3px; }

.employee-item {
  display: flex;
  align-items: center;
  padding: 10px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 10px;
}
.employee-item:hover { background: #eef2ff; }

.is-selected-emp {
  background: #eef2ff !important;
  border: 1.5px solid #a5b4fc;
}

.custom-radio { width: 16px; height: 16px; cursor: pointer; accent-color: #6366f1; margin: 0; }

.emp-info { display: flex; flex-direction: column; }
.emp-name { font-size: 13px; color: #1e293b; font-weight: 600; }
.emp-code { font-size: 11px; color: #94a3b8; }

/* ── Month Cards ── */
.months-container { display: flex; flex-direction: column; gap: 12px; scroll-margin-top: 20px; }

.month-card {
  border: 1.5px solid #e8edff;
  border-radius: 14px;
  overflow: hidden;
  background: white;
  scroll-margin-top: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}

.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafbff;
  cursor: pointer;
  transition: background 0.2s;
}
.month-header:hover { background: #f0f4ff; }
.month-header.is-open { background: #eef2ff; border-bottom: 1.5px solid #e8edff; }

.month-title { display: flex; align-items: center; gap: 12px; }
.month-title h3 { margin: 0; font-size: 15px; font-weight: 700; color: #0f172a; }

.month-badge {
  font-size: 11px;
  background: #ede9fe;
  color: #5b21b6;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 700;
}

.toggle-icon { color: #64748b; font-size: 12px; }

.month-body { padding: 18px; background: white; }

.slide-fade-enter-active { transition: all 0.3s ease-out; }
.slide-fade-leave-active { transition: all 0.2s ease-in; }
.slide-fade-enter-from, .slide-fade-leave-to { transform: translateY(-10px); opacity: 0; }

/* ── Quick Actions ── */
.quick-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8faff;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1.5px solid #e8edff;
}

.shift-selector { display: flex; align-items: center; gap: 12px; }

.shift-select {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  padding: 7px 12px;
  font-weight: 600;
  font-family: inherit;
  font-size: 13px;
  color: #334155;
  transition: border-color 0.2s;
}
.shift-select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }

.action-buttons { display: flex; gap: 10px; }

/* ── Buttons ── */
.btn {
  padding: 8px 16px;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  box-shadow: 0 2px 8px rgba(99,102,241,0.25);
}
.btn-primary:hover { box-shadow: 0 5px 14px rgba(99,102,241,0.4); transform: translateY(-1px); }

.btn-outline {
  background: white;
  border: 1.5px solid #e2e8f0;
  color: #475569;
}
.btn-outline:hover { background: #fafbff; border-color: #a5b4fc; color: #4f46e5; }

.text-danger { color: #dc2626 !important; border-color: #fecaca !important; }
.text-danger:hover {
  color: white !important;
  background: linear-gradient(135deg, #f43f5e, #dc2626) !important;
  border-color: transparent !important;
  box-shadow: 0 2px 8px rgba(220,38,38,0.25);
}

/* ── Calendar ── */
.calendar-wrapper {
  border: 1.5px solid #e8edff;
  border-radius: 12px;
  overflow: hidden;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: #fafbff;
  border-bottom: 1.5px solid #e8edff;
}

.weekday {
  padding: 11px;
  text-align: center;
  font-weight: 700;
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.sunday-col { color: #dc2626; }

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(80px, auto);
  user-select: none;
}

.calendar-cell {
  border-right: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  transition: all 0.15s ease;
  background: #fefefe;
}
.calendar-cell:nth-child(7n) { border-right: none; }
.calendar-cell.is-clickable { cursor: pointer; }
.calendar-cell.is-clickable:hover { box-shadow: inset 0 0 0 2px #6366f1; background: #fafbff; }
.calendar-cell.is-selected { background: #f0fdf4 !important; }
.calendar-cell.is-previewing { background: #eef2ff !important; }

/* ── Badges ── */
.vt-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 11px;
}

.badge-hc       { background: #d1fae5; color: #065f46; }
.badge-morning  { background: #ede9fe; color: #5b21b6; }
.badge-afternoon{ background: #fef3c7; color: #92400e; }

.is-sunday .date-num { color: #dc2626; }
.date-num { font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 6px; }
.date-status-text { font-size: 11px; margin-top: auto; }
.text-muted { color: #cbd5e1; font-weight: 400; }

/* ── Responsive ── */
@media (max-width: 992px) {
  .schedules-layout { flex-direction: column; }
  .sidebar { width: 100%; position: static; }
  .quick-actions { flex-direction: column; gap: 12px; align-items: stretch; }
  .action-buttons { justify-content: flex-end; }
  .calendar-header .weekday { font-size: 10px; padding: 7px 3px; }
  .calendar-cell { padding: 4px; }
  .date-status-text { font-size: 9px; }
}
</style>