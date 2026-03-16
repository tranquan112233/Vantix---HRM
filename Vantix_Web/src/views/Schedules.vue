<script setup>
import {ref, computed, nextTick, onMounted, onUnmounted} from 'vue';

// --- CẤU HÌNH & DỮ LIỆU ---
const currentYear = new Date().getFullYear();
const selectedYear = ref(currentYear);

// Danh sách nhân viên
const employees = ref([
  {id: 101, name: 'Nguyễn Văn A', code: 'NV001', selected: false},
  {id: 102, name: 'Trần Thị B', code: 'NV002', selected: false},
  {id: 103, name: 'Lê Hoàng C', code: 'NV003', selected: false},
  {id: 104, name: 'Phạm D', code: 'NV004', selected: false},
  {id: 105, name: 'Vũ Văn E', code: 'NV005', selected: false},
]);

const selectAll = computed({
  get: () => employees.value.length > 0 && employees.value.every(emp => emp.selected),
  set: (value) => {
    employees.value.forEach(emp => emp.selected = value);
  }
});

const selectedCount = computed(() => employees.value.filter(e => e.selected).length);

// --- XỬ LÝ LỊCH VÀ ACCORDION ---
const expandedMonths = ref([new Date().getMonth() + 1]);

onMounted(() => {
  setTimeout(() => {
    const currentMonth = expandedMonths.value[0];
    const monthElement = document.getElementById(`month-card-${currentMonth}`);
    if (monthElement) {
      monthElement.scrollIntoView({behavior: 'smooth', block: 'center'});
    }
  }, 100);

  // Thêm sự kiện bắt nhả chuột toàn cục để xử lý việc "Kéo Thả"
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
    expandedMonths.value.push(month);
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
const shiftOptions = [
  {id: 'Hành Chính', label: 'Hành Chính'},
  {id: 'Ca Sáng', label: 'Ca Sáng'},
  {id: 'Ca Chiều', label: 'Ca Chiều'}
];

const schedules = ref({});
const selectedShift = ref('Hành Chính');

// Khai báo state cho việc kéo thả
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

const getShiftBadgeClass = (shift) => {
  if (shift === 'Hành Chính') return 'badge-hc';
  if (shift === 'Ca Sáng') return 'badge-morning';
  if (shift === 'Ca Chiều') return 'badge-afternoon';
  return '';
};

// 1. Khi bấm chuột xuống
const startDrag = (month, day) => {
  if (!day || isDaySunday(month, day)) return; // Bỏ qua nếu bấm vào ô trống hoặc Chủ Nhật
  dragState.value = {
    active: true,
    month,
    startDay: day,
    endDay: day,
    shift: selectedShift.value
  };
};

// 2. Khi lướt qua các ô khác trong lúc giữ chuột
const onDragOver = (month, day) => {
  if (!dragState.value.active || dragState.value.month !== month || !day) return;
  dragState.value.endDay = day; // Cập nhật ngày đích liên tục
};

// 3. Khi nhả chuột ra (Xử lý Lưu)
const endDrag = () => {
  if (!dragState.value.active) return;
  const {month, startDay, endDay, shift} = dragState.value;

  if (month && startDay && endDay) {
    if (!schedules.value[month]) schedules.value[month] = {};

    if (startDay === endDay) {
      // Trường hợp 1: Chỉ click 1 ô (Giữ nguyên tính năng Bật/Tắt)
      if (schedules.value[month][startDay] === shift) {
        schedules.value[month][startDay] = null;
      } else {
        schedules.value[month][startDay] = shift;
      }
    } else {
      // Trường hợp 2: Kéo thả 1 dải ngày
      const minDay = Math.min(startDay, endDay);
      const maxDay = Math.max(startDay, endDay);
      for (let d = minDay; d <= maxDay; d++) {
        if (!isDaySunday(month, d)) { // Trừ Chủ Nhật
          schedules.value[month][d] = shift;
        }
      }
    }
  }

  // Tắt trạng thái kéo
  dragState.value.active = false;
};

// Kiểm tra xem 1 ngày có đang nằm trong vùng kéo chuột preview không
const isInDragRange = (month, day) => {
  if (!dragState.value.active || dragState.value.month !== month || !day) return false;
  if (isDaySunday(month, day)) return false; // Không preview cho Chủ Nhật

  const minDay = Math.min(dragState.value.startDay, dragState.value.endDay);
  const maxDay = Math.max(dragState.value.startDay, dragState.value.endDay);
  return day >= minDay && day <= maxDay;
};

// Lấy ca để hiển thị (Nếu đang kéo thì hiển thị ca Preview, nếu không thì hiển thị ca thật)
const getEffectiveShift = (month, day) => {
  if (isInDragRange(month, day)) return dragState.value.shift;
  return getDayShift(month, day);
};

// Nút chọn nhanh T2 - T7
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
</script>

<template>
  <div class="page-wrapper">
    <div class="schedules-layout">

      <div class="sidebar content-card">
        <div class="sidebar-header">
          <h3>Nhân viên ({{ selectedCount }}/{{ employees.length }})</h3>
        </div>

        <div class="employee-list-container">
          <label class="employee-item select-all-item">
            <input type="checkbox" v-model="selectAll" class="custom-checkbox"/>
            <span class="fw-600">Chọn tất cả</span>
          </label>
          <hr class="divider"/>

          <div class="employee-list">
            <label v-for="emp in employees" :key="emp.id" class="employee-item">
              <input type="checkbox" v-model="emp.selected" class="custom-checkbox"/>
              <div class="emp-info">
                <span class="emp-name">{{ emp.name }}</span>
                <span class="emp-code">{{ emp.code }}</span>
              </div>
            </label>
          </div>
        </div>
      </div>

      <div class="main-content content-card">
        <div class="card-header">
          <div class="header-title">
            <span class="header-icon">🗓️</span>
            <div>
              <h2>Lịch làm việc tổng hợp</h2>
              <p>Phân ca làm việc cho nhân viên được chọn</p>
            </div>
          </div>
          <div class="header-filters">
            <div class="search-box">
              <select v-model="selectedYear" class="flat-select">
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

                <div class="quick-actions">
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
                      🗑️ Xóa lịch
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
                          { 'is-clickable': day },
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
                            {{ getEffectiveShift(month, day) }}
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
.page-wrapper {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  font-family: 'Inter', 'Segoe UI', sans-serif;
  box-sizing: border-box;
  scroll-behavior: smooth;
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

.divider {
  border: none;
  border-top: 1px solid #ebeef5;
  margin: 8px 0 12px 0;
}

.employee-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.employee-item:hover {
  background-color: #f5f7fa;
}

.select-all-item {
  background-color: #f0f7ff;
  color: #409eff;
}

.select-all-item:hover {
  background-color: #e6f1fc;
}

.custom-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #409eff;
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

/* CSS LƯỚI LỊCH: Vô hiệu hóa bôi đen text khi kéo thả */
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

/* ĐÃ XÓA VIỀN ĐỨT NÉT, CHỈ GIỮ LẠI MÀU NỀN MỜ */
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