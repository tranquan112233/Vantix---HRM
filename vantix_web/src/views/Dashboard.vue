<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Document, Money, Warning } from '@element-plus/icons-vue'
import { dashboardApi } from '@/api'

const router = useRouter()
const loading = ref(true)
const stats = ref(emptyStats())

const statusLabels = {
  PROBATION: 'Thử việc',
  ACTIVE: 'Đang làm',
  UNPAID_LEAVE: 'Nghỉ không lương',
  RESIGNED: 'Đã nghỉ',
  TERMINATED: 'Chấm dứt',
  TODO: 'Chưa làm',
  DOING: 'Đang làm',
  DONE: 'Hoàn thành',
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy',
  ON_TIME: 'Đúng giờ',
  LATE: 'Đi muộn',
  EARLY_LEAVE: 'Về sớm',
  LATE_AND_EARLY: 'Muộn/về sớm',
  ABSENT: 'Vắng',
  DRAFT: 'Nháp',
  CALCULATED: 'Đã tính',
  PAID: 'Đã trả',
}

const palette = ['#2563eb', '#16a34a', '#f59e0b', '#dc2626', '#7c3aed', '#0891b2', '#db2777']

const activeEmployees = computed(() =>
  (stats.value.employeesByStatus?.ACTIVE || 0) + (stats.value.employeesByStatus?.PROBATION || 0)
)
const employeeStatusEntries = computed(() => mapEntries(stats.value.employeesByStatus))
const taskEntries = computed(() => mapEntries(stats.value.tasksByStatus))
const attendanceEntries = computed(() => mapEntries(stats.value.todayAttendanceByStatus))
const departmentEntries = computed(() => mapEntries(stats.value.employeesByDepartment).slice(0, 6))
const attendanceDays = computed(() => stats.value.attendanceLast7Days || [])
const maxDepartmentCount = computed(() => Math.max(1, ...departmentEntries.value.map(item => item.value)))
const maxDailyAttendance = computed(() => Math.max(1, ...attendanceDays.value.map(day => day.total || 0)))
const taskCompletionRate = computed(() => {
  if (!stats.value.totalTasks) return 0
  return Math.round((stats.value.completedTasks / stats.value.totalTasks) * 100)
})
const attendanceRiskCount = computed(() => stats.value.todayLateRecords + stats.value.todayAbsentRecords)
const actionItems = computed(() => [
  {
    label: 'Đơn nghỉ chờ duyệt',
    value: stats.value.pendingLeaveRequests,
    tone: 'warning',
    icon: Calendar,
    path: '/leave-requests',
  },
  {
    label: 'Công việc quá hạn',
    value: stats.value.overdueTasks,
    tone: 'danger',
    icon: Warning,
    path: '/tasks',
  },
  {
    label: 'Hợp đồng sắp hết hạn',
    value: stats.value.expiringContracts,
    tone: 'primary',
    icon: Document,
    path: '/contracts',
  },
  {
    label: 'Dòng lương tháng này',
    value: stats.value.currentPayrollRows,
    tone: 'success',
    icon: Money,
    path: '/payrolls',
  },
])

onMounted(loadStats)

async function loadStats() {
  loading.value = true
  try {
    const { data } = await dashboardApi.stats()
    stats.value = { ...emptyStats(), ...data }
  } catch {
    stats.value = emptyStats()
  } finally {
    loading.value = false
  }
}

function emptyStats() {
  return {
    totalEmployees: 0,
    totalDepartments: 0,
    totalPositions: 0,
    activeUsers: 0,
    totalTasks: 0,
    openTasks: 0,
    completedTasks: 0,
    overdueTasks: 0,
    pendingLeaveRequests: 0,
    todayAttendanceRecords: 0,
    todayLateRecords: 0,
    todayAbsentRecords: 0,
    activeContracts: 0,
    expiringContracts: 0,
    currentPayrollRows: 0,
    currentPayrollStatus: null,
    employeesByStatus: {},
    employeesByDepartment: {},
    tasksByStatus: {},
    leaveRequestsByStatus: {},
    todayAttendanceByStatus: {},
    attendanceLast7Days: [],
    recentEmployees: [],
    recentTasks: [],
    upcomingLeaveRequests: [],
    expiringContractAlerts: [],
  }
}

function mapEntries(source = {}) {
  return Object.entries(source || {}).map(([key, value], index) => ({
    key,
    label: statusLabels[key] || key,
    value,
    color: palette[index % palette.length],
  }))
}

function percent(value, total) {
  if (!total) return 0
  return Math.round((value / total) * 100)
}

function statusType(status) {
  const map = {
    ACTIVE: 'success',
    PROBATION: 'warning',
    TODO: 'info',
    DOING: 'warning',
    DONE: 'success',
    PENDING: 'warning',
    APPROVED: 'success',
    CALCULATED: 'success',
    PAID: 'success',
    DRAFT: 'info',
    REJECTED: 'danger',
    TERMINATED: 'danger',
    ABSENT: 'danger',
    LATE: 'warning',
    LATE_AND_EARLY: 'danger',
  }
  return map[status] || 'info'
}

function formatDate(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(value))
}

function shortDate(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit' }).format(new Date(value))
}

function navigate(path) {
  router.push(path).catch(() => {})
}
</script>

<template>
  <div v-loading="loading" class="dashboard-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="eyebrow">Tổng quan vận hành</span>
        <h1>Dashboard nhân sự hôm nay</h1>
        <p>Theo dõi nhanh nhân sự, chấm công, công việc, hợp đồng và kỳ lương hiện tại.</p>
      </div>
      <div class="hero-stats">
        <button class="hero-stat" @click="navigate('/employees')">
          <span>Nhân sự đang làm</span>
          <strong>{{ activeEmployees }}</strong>
        </button>
        <button class="hero-stat" @click="navigate('/attendance')">
          <span>Ghi nhận hôm nay</span>
          <strong>{{ stats.todayAttendanceRecords }}</strong>
        </button>
        <button class="hero-stat" @click="navigate('/payrolls')">
          <span>Kỳ lương</span>
          <strong>{{ statusLabels[stats.currentPayrollStatus] || 'Chưa tạo' }}</strong>
        </button>
      </div>
    </section>

    <section class="action-grid">
      <button
        v-for="item in actionItems"
        :key="item.label"
        class="action-card"
        :class="`tone-${item.tone}`"
        @click="navigate(item.path)"
      >
        <span class="action-icon">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
        </span>
        <span class="action-copy">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </span>
      </button>
    </section>

    <section class="dashboard-grid main-grid">
      <article class="page-card people-card">
        <div class="card-heading">
          <div>
            <h3>Nhân sự</h3>
            <p>{{ stats.totalEmployees }} hồ sơ · {{ stats.totalDepartments }} phòng ban · {{ stats.totalPositions }} chức vụ</p>
          </div>
          <el-button text type="primary" @click="navigate('/employees')">Xem</el-button>
        </div>

        <div class="status-strip">
          <div v-for="item in employeeStatusEntries" :key="item.key" class="status-pill">
            <span class="dot" :style="{ background: item.color }" />
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <div class="department-list">
          <div v-for="item in departmentEntries" :key="item.key" class="department-row">
            <div class="department-label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track">
              <span class="bar-fill" :style="{ width: `${percent(item.value, maxDepartmentCount)}%`, background: item.color }" />
            </div>
          </div>
          <el-empty v-if="!departmentEntries.length" description="Chưa có dữ liệu phòng ban" :image-size="68" />
        </div>
      </article>

      <article class="page-card attendance-card">
        <div class="card-heading">
          <div>
            <h3>Chấm công</h3>
            <p>{{ attendanceRiskCount }} trường hợp cần kiểm tra hôm nay</p>
          </div>
          <div class="tag-row">
            <el-tag type="warning" effect="light">{{ stats.todayLateRecords }} muộn</el-tag>
            <el-tag type="danger" effect="light">{{ stats.todayAbsentRecords }} vắng</el-tag>
          </div>
        </div>

        <div class="attendance-bars">
          <div v-for="day in attendanceDays" :key="day.date" class="day-column">
            <div class="day-stack" :style="{ height: `${Math.max(8, percent(day.total, maxDailyAttendance))}%` }">
              <span class="on-time" :style="{ height: `${percent(day.onTime, day.total)}%` }" />
              <span class="late" :style="{ height: `${percent(day.late, day.total)}%` }" />
              <span class="absent" :style="{ height: `${percent(day.absent, day.total)}%` }" />
            </div>
            <span>{{ shortDate(day.date) }}</span>
          </div>
        </div>

        <div class="status-strip compact">
          <div v-for="item in attendanceEntries" :key="item.key" class="status-pill">
            <span class="dot" :style="{ background: item.color }" />
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </article>
    </section>

    <section class="dashboard-grid secondary-grid">
      <article class="page-card task-card">
        <div class="card-heading">
          <div>
            <h3>Công việc</h3>
            <p>{{ taskCompletionRate }}% hoàn thành · {{ stats.openTasks }} việc đang mở</p>
          </div>
          <el-progress type="dashboard" :percentage="taskCompletionRate" :width="76" />
        </div>

        <div class="task-status">
          <div v-for="item in taskEntries" :key="item.key" class="task-row">
            <div class="department-label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track">
              <span class="bar-fill" :style="{ width: `${percent(item.value, stats.totalTasks)}%`, background: item.color }" />
            </div>
          </div>
          <el-empty v-if="!taskEntries.length" description="Chưa có công việc" :image-size="68" />
        </div>
      </article>

      <article class="page-card finance-card">
        <div class="card-heading">
          <div>
            <h3>Lương & hợp đồng</h3>
            <p>{{ stats.activeContracts }} hợp đồng đang hiệu lực</p>
          </div>
          <el-tag :type="statusType(stats.currentPayrollStatus)" effect="light">
            {{ statusLabels[stats.currentPayrollStatus] || 'Chưa có kỳ lương' }}
          </el-tag>
        </div>

        <div class="finance-metrics">
          <button @click="navigate('/contracts')">
            <span>Sắp hết hạn 30 ngày</span>
            <strong>{{ stats.expiringContracts }}</strong>
          </button>
          <button @click="navigate('/payrolls')">
            <span>Dòng lương tháng này</span>
            <strong>{{ stats.currentPayrollRows }}</strong>
          </button>
        </div>

        <div class="alert-list">
          <div v-for="contract in stats.expiringContractAlerts" :key="contract.id" class="alert-item">
            <div>
              <strong>{{ contract.employeeName || '-' }}</strong>
              <span>{{ contract.contractCode }} · {{ contract.departmentName || 'Chưa có phòng ban' }}</span>
            </div>
            <el-tag type="warning" effect="light">{{ formatDate(contract.endDate) }}</el-tag>
          </div>
          <el-empty v-if="!stats.expiringContractAlerts?.length" description="Không có hợp đồng sắp hết hạn" :image-size="68" />
        </div>
      </article>
    </section>

    <section class="dashboard-grid lists-grid">
      <article class="page-card list-card">
        <div class="card-heading">
          <div>
            <h3>Công việc gần đây</h3>
            <p>Ưu tiên theo hạn xử lý</p>
          </div>
        </div>
        <div class="item-list">
          <div v-for="task in stats.recentTasks" :key="task.id" class="list-item">
            <div class="item-main">
              <strong>{{ task.title }}</strong>
              <span>{{ task.assigneeName || 'Chưa giao' }} · hạn {{ formatDate(task.dueDate) }}</span>
            </div>
            <el-tag :type="statusType(task.status)" size="small" effect="light">
              {{ statusLabels[task.status] || task.status }}
            </el-tag>
          </div>
          <el-empty v-if="!stats.recentTasks?.length" description="Chưa có công việc" :image-size="68" />
        </div>
      </article>

      <article class="page-card list-card">
        <div class="card-heading">
          <div>
            <h3>Lịch nghỉ sắp tới</h3>
            <p>{{ stats.pendingLeaveRequests }} đơn chờ duyệt</p>
          </div>
        </div>
        <div class="item-list">
          <div v-for="leave in stats.upcomingLeaveRequests" :key="leave.id" class="list-item">
            <div class="date-tile">
              <strong>{{ shortDate(leave.startDate) }}</strong>
              <span>{{ shortDate(leave.endDate) }}</span>
            </div>
            <div class="item-main">
              <strong>{{ leave.employeeName || '-' }}</strong>
              <span>{{ leave.departmentName || 'Chưa có phòng ban' }}</span>
            </div>
            <el-tag :type="statusType(leave.status)" size="small" effect="light">
              {{ statusLabels[leave.status] || leave.status }}
            </el-tag>
          </div>
          <el-empty v-if="!stats.upcomingLeaveRequests?.length" description="Chưa có lịch nghỉ sắp tới" :image-size="68" />
        </div>
      </article>

      <article class="page-card list-card">
        <div class="card-heading">
          <div>
            <h3>Nhân viên mới</h3>
            <p>Hồ sơ vừa được tạo</p>
          </div>
        </div>
        <div class="item-list">
          <div v-for="emp in stats.recentEmployees" :key="emp.id" class="list-item">
            <el-avatar :size="36" class="avatar">{{ emp.fullName?.charAt(0) || '?' }}</el-avatar>
            <div class="item-main">
              <strong>{{ emp.fullName }}</strong>
              <span>{{ emp.employeeCode }} · {{ emp.departmentName || 'Chưa có phòng ban' }}</span>
            </div>
            <el-tag :type="statusType(emp.status)" size="small" effect="light">
              {{ statusLabels[emp.status] || emp.status }}
            </el-tag>
          </div>
          <el-empty v-if="!stats.recentEmployees?.length" description="Không có nhân viên mới" :image-size="68" />
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 42%);
  gap: 22px;
  align-items: stretch;
  padding: 24px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-surface);
  box-shadow: var(--vx-shadow-sm);
}

.hero-copy {
  min-width: 0;
}

.eyebrow {
  display: inline-flex;
  margin-bottom: 8px;
  color: var(--vx-primary);
  font-size: var(--vx-font-size-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-copy h1 {
  margin: 0;
  color: var(--vx-text);
  font-size: 30px;
  line-height: 1.15;
}

.hero-copy p {
  max-width: 640px;
  margin: 10px 0 0;
  color: var(--vx-text-secondary);
  line-height: 1.55;
}

.hero-stats,
.action-grid,
.dashboard-grid {
  display: grid;
  gap: 14px;
}

.hero-stats {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.hero-stat,
.action-card,
.finance-metrics button {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
}

.hero-stat {
  min-width: 0;
  padding: 16px;
}

.hero-stat span,
.action-copy span,
.finance-metrics span,
.card-heading p,
.item-main span,
.alert-item span,
.date-tile span {
  color: var(--vx-text-secondary);
}

.hero-stat strong {
  display: block;
  margin-top: 8px;
  color: var(--vx-text);
  font-size: 24px;
  line-height: 1.15;
}

.action-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.action-card {
  min-height: 86px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.action-card:hover,
.hero-stat:hover,
.finance-metrics button:hover {
  transform: translateY(-1px);
  box-shadow: var(--vx-shadow-sm);
}

.action-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  border-radius: 8px;
  font-size: 22px;
  background: #eff6ff;
  color: #2563eb;
}

.tone-warning .action-icon { background: #fffbeb; color: #b45309; }
.tone-danger .action-icon { background: #fef2f2; color: #b91c1c; }
.tone-success .action-icon { background: #f0fdf4; color: #15803d; }

.action-copy strong {
  display: block;
  margin-top: 2px;
  color: var(--vx-text);
  font-size: 28px;
  line-height: 1;
}

.main-grid,
.secondary-grid {
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
}

.lists-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.page-card {
  min-width: 0;
  border-radius: 8px;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.card-heading h3 {
  margin: 0 0 4px;
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  font-weight: 800;
}

.card-heading p {
  margin: 0;
  font-size: var(--vx-font-size-sm);
}

.status-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.status-strip.compact {
  margin: 16px 0 0;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 10px;
  border: 1px solid var(--vx-border);
  border-radius: 999px;
  background: #f8fafc;
  color: var(--vx-text);
  font-size: var(--vx-font-size-xs);
}

.status-pill strong {
  font-size: var(--vx-font-size-sm);
}

.dot {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  border-radius: 50%;
}

.department-list,
.task-status,
.alert-list,
.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.department-row,
.task-row {
  display: grid;
  gap: 7px;
}

.department-label {
  display: flex;
  align-items: center;
  gap: 10px;
}

.department-label span {
  min-width: 0;
  overflow: hidden;
  color: var(--vx-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.department-label strong {
  margin-left: auto;
  color: var(--vx-text);
}

.bar-track {
  height: 9px;
  overflow: hidden;
  border-radius: 999px;
  background: #e5e7eb;
}

.bar-fill {
  display: block;
  min-width: 4px;
  height: 100%;
  border-radius: inherit;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.attendance-bars {
  height: 218px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  align-items: end;
  gap: 10px;
}

.day-column {
  height: 100%;
  display: grid;
  grid-template-rows: 1fr auto;
  align-items: end;
  gap: 8px;
  color: var(--vx-text-secondary);
  text-align: center;
  font-size: var(--vx-font-size-xs);
}

.day-stack {
  min-height: 8px;
  display: flex;
  flex-direction: column-reverse;
  overflow: hidden;
  border-radius: 8px 8px 4px 4px;
  background: #e5e7eb;
}

.on-time { background: #16a34a; }
.late { background: #f59e0b; }
.absent { background: #dc2626; }

.finance-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.finance-metrics button {
  padding: 16px;
}

.finance-metrics strong {
  display: block;
  margin-top: 5px;
  color: var(--vx-text);
  font-size: 28px;
}

.alert-item,
.list-item {
  min-height: 58px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--vx-border);
}

.alert-item:last-child,
.list-item:last-child {
  border-bottom: none;
}

.alert-item > div,
.item-main {
  flex: 1;
  min-width: 0;
}

.alert-item strong,
.alert-item span,
.item-main strong,
.item-main span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alert-item strong,
.item-main strong {
  color: var(--vx-text);
  font-weight: 700;
}

.alert-item span,
.item-main span {
  margin-top: 3px;
  font-size: var(--vx-font-size-xs);
}

.date-tile {
  width: 54px;
  min-height: 46px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: #f8fafc;
}

.date-tile strong {
  color: var(--vx-text);
  font-size: var(--vx-font-size-xs);
}

.date-tile span {
  font-size: var(--vx-font-size-2xs);
}

.avatar {
  flex: 0 0 auto;
  background: var(--vx-primary);
  color: #fff;
  font-weight: 800;
}

@media (max-width: 1220px) {
  .hero-panel,
  .main-grid,
  .secondary-grid {
    grid-template-columns: 1fr;
  }

  .lists-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .hero-panel {
    padding: 18px;
  }

  .action-grid,
  .finance-metrics {
    grid-template-columns: 1fr;
  }

  .hero-copy h1 {
    font-size: 24px;
  }
}
</style>
