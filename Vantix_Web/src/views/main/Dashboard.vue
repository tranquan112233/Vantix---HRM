<template>
  <div class="dashboard mgmt-page">
    <!-- Header -->
    <div class="page-header dash-header">
      <div>
        <h1 class="page-title dash-title">Bảng điều khiển</h1>
        <p class="page-desc dash-sub">{{ greeting }}, <strong>{{ auth.user?.fullName || auth.user?.username }}</strong></p>
      </div>
      <div class="header-actions">
        <span class="status-badge open dash-date">
          <span class="dot"></span>
          {{ todayStr }}
        </span>
      </div>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in stats" :key="s.label">
        <div class="stat-icon" :style="{ background: s.bg }">
          <i :class="'bi ' + s.icon"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">
            <span v-if="loading.stats">—</span>
            <span v-else>{{ s.value.toLocaleString('vi-VN') }}</span>
          </div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
        <div class="stat-trend" :class="s.trendClass" v-if="s.trend">
          <i class="bi bi-arrow-up-right"></i> {{ s.trend }}
        </div>
      </div>
    </div>

    <!-- Content Row -->
    <div class="content-row">
      <!-- Recent Hires -->
      <div class="card-block recent-block">
        <div class="block-header">
          <span class="block-title"><i class="bi bi-person-plus"></i> Nhân viên mới</span>
          <router-link to="/employees" class="block-link">Xem tất cả <i class="bi bi-arrow-right"></i></router-link>
        </div>
        <div v-if="loading.recent" class="state-center-sm">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>
        <div v-else-if="recentEmployees.length === 0" class="state-center-sm">
          <i class="bi bi-people" style="font-size:28px;color:#ccc"></i>
          <p style="color:#aaa;margin-top:6px;font-size:13px">Chưa có nhân viên</p>
        </div>
        <ul v-else class="hire-list">
          <li v-for="emp in recentEmployees" :key="emp.id" class="hire-item">
            <div class="hire-avatar" :style="{ background: gradient(emp.fullName) }">
              {{ emp.fullName?.charAt(0)?.toUpperCase() }}
            </div>
            <div class="hire-info">
              <div class="hire-name">{{ emp.fullName }}</div>
              <div class="hire-meta">{{ emp.positionName || '—' }} · {{ emp.departmentName || '—' }}</div>
            </div>
            <div class="hire-date">{{ fmtDate(emp.createdAt) }}</div>
          </li>
        </ul>
      </div>

      <!-- Departments -->
      <div class="card-block dept-block">
        <div class="block-header">
          <span class="block-title"><i class="bi bi-diagram-3"></i> Phòng ban</span>
          <router-link to="/departments" class="block-link">Quản lý <i class="bi bi-arrow-right"></i></router-link>
        </div>
        <div v-if="loading.depts" class="state-center-sm">
          <div class="spinner-border spinner-border-sm text-secondary"></div>
        </div>
        <ul v-else class="dept-list">
          <li v-for="dept in departments" :key="dept.id" class="dept-item">
            <div class="dept-dot" :style="{ background: gradient(dept.name) }"></div>
            <span class="dept-name">{{ dept.name }}</span>
            <span class="dept-manager" v-if="dept.managerName">
              <i class="bi bi-person-fill"></i> {{ dept.managerName }}
            </span>
          </li>
        </ul>
      </div>

      <!-- Quick Links -->
      <div class="card-block quick-block">
        <div class="block-header">
          <span class="block-title"><i class="bi bi-lightning"></i> Truy cập nhanh</span>
        </div>
        <div class="quick-grid">
          <router-link v-for="q in quickLinks" :key="q.to" :to="q.to" class="quick-item">
            <div class="quick-icon" :style="{ background: q.bg }">
              <i :class="'bi ' + q.icon"></i>
            </div>
            <span>{{ q.label }}</span>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store.js'
import employeeService from '@/services/employee.service.js'
import departmentService from '@/services/department.service.js'

const auth = useAuthStore()

const loading = reactive({ stats: true, recent: true, depts: true })

const totalEmployees = ref(0)
const totalWorking   = ref(0)
const totalResigned  = ref(0)
const totalDepts     = ref(0)
const recentEmployees = ref([])
const departments     = ref([])

// ── Computed ────────────────────────────────────────────────────────────────

const stats = computed(() => [
  {
    label: 'Tổng nhân viên',
    value: totalEmployees.value,
    icon: 'bi-people-fill',
    bg: 'linear-gradient(135deg,#6366f1,#818cf8)',
    trend: null
  },
  {
    label: 'Đang làm việc',
    value: totalWorking.value,
    icon: 'bi-person-check-fill',
    bg: 'linear-gradient(135deg,#10b981,#34d399)',
    trend: null
  },
  {
    label: 'Đã nghỉ việc',
    value: totalResigned.value,
    icon: 'bi-person-dash-fill',
    bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)',
    trend: null
  },
  {
    label: 'Phòng ban',
    value: totalDepts.value,
    icon: 'bi-diagram-3-fill',
    bg: 'linear-gradient(135deg,#3b82f6,#60a5fa)',
    trend: null
  },
])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return 'Chào buổi sáng'
  if (h < 18) return 'Chào buổi chiều'
  return 'Chào buổi tối'
})

const todayStr = computed(() =>
  new Date().toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })
)

const quickLinks = [
  { label: 'Nhân viên',   to: '/employees',    icon: 'bi-people',         bg: 'linear-gradient(135deg,#6366f1,#818cf8)' },
  { label: 'Hợp đồng',    to: '/contracts',    icon: 'bi-file-text',      bg: 'linear-gradient(135deg,#10b981,#34d399)' },
  { label: 'Chấm công',   to: '/attendances',  icon: 'bi-calendar-check', bg: 'linear-gradient(135deg,#3b82f6,#60a5fa)' },
  { label: 'Lịch làm',    to: '/schedules',    icon: 'bi-calendar3',      bg: 'linear-gradient(135deg,#8b5cf6,#a78bfa)' },
  { label: 'Lương',       to: '/salaries',     icon: 'bi-cash-coin',      bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)' },
  { label: 'Duyệt nghỉ',  to: '/leaves-manager',icon: 'bi-inbox',         bg: 'linear-gradient(135deg,#ef4444,#f87171)' },
]

// ── Lifecycle ────────────────────────────────────────────────────────────────

onMounted(() => {
  fetchStats()
  fetchRecentEmployees()
  fetchDepartments()
})

async function fetchStats() {
  loading.stats = true
  try {
    const [allRes, workRes, resignRes] = await Promise.all([
      employeeService.getAll({ page: 0, size: 1 }),
      employeeService.getAll({ page: 0, size: 1, workStatus: 'WORKING' }),
      employeeService.getAll({ page: 0, size: 1, workStatus: 'RESIGNED' }),
    ])
    totalEmployees.value = allRes.data?.totalElements ?? 0
    totalWorking.value   = workRes.data?.totalElements ?? 0
    totalResigned.value  = resignRes.data?.totalElements ?? 0
  } catch (e) {
    console.error('Dashboard stats error', e)
  } finally {
    loading.stats = false
  }
}

async function fetchRecentEmployees() {
  loading.recent = true
  try {
    const { data } = await employeeService.getAll({ page: 0, size: 6, sortBy: 'createdAt', sortDir: 'desc' })
    recentEmployees.value = data?.content ?? []
  } catch (e) {
    console.error('Recent employees error', e)
  } finally {
    loading.recent = false
  }
}

async function fetchDepartments() {
  loading.depts = true
  try {
    const { data } = await departmentService.getAll({ page: 0, size: 50 })
    departments.value    = data?.content ?? []
    totalDepts.value     = data?.totalElements ?? 0
  } catch (e) {
    console.error('Departments error', e)
  } finally {
    loading.depts = false
  }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

const GRADIENTS = [
  'linear-gradient(135deg,#6366f1,#818cf8)',
  'linear-gradient(135deg,#10b981,#34d399)',
  'linear-gradient(135deg,#3b82f6,#60a5fa)',
  'linear-gradient(135deg,#f59e0b,#fbbf24)',
  'linear-gradient(135deg,#8b5cf6,#a78bfa)',
  'linear-gradient(135deg,#ef4444,#f87171)',
  'linear-gradient(135deg,#06b6d4,#67e8f9)',
]
function gradient(name = '') {
  let h = 0
  for (let i = 0; i < name.length; i++) h = name.charCodeAt(i) + ((h << 5) - h)
  return GRADIENTS[Math.abs(h) % GRADIENTS.length]
}

function fmtDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }

.dashboard {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
}

/* Header */
.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 8px;
}
.dash-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #111;
}
.dash-sub {
  font-size: 13.5px;
  color: #777;
  margin-top: 3px;
}
.dash-date {
  font-size: 13px;
  color: #999;
  font-weight: 500;
  text-transform: capitalize;
}

/* Stat Cards */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
@media (max-width: 1024px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .stat-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
}
.stat-body { flex: 1; }
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #111;
  line-height: 1;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 12.5px;
  color: #888;
  font-weight: 500;
}
.stat-trend {
  position: absolute;
  top: 14px;
  right: 14px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: 20px;
  background: #f0fdf4;
  color: #16a34a;
}

/* Content Row */
.content-row {
  display: grid;
  grid-template-columns: 1fr 320px 260px;
  gap: 16px;
  align-items: start;
}
@media (max-width: 1200px) { .content-row { grid-template-columns: 1fr 1fr; } }
@media (max-width: 768px)  { .content-row { grid-template-columns: 1fr; } }

.card-block {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.block-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}
.block-link {
  font-size: 12.5px;
  color: #6366f1;
  text-decoration: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}
.block-link:hover { text-decoration: underline; }

.state-center-sm {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 0;
  gap: 8px;
}

/* Recent Hires */
.hire-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
.hire-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  transition: background .15s;
}
.hire-item:hover { background: #f4f4ff; }
.hire-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
  flex-shrink: 0;
}
.hire-info { flex: 1; min-width: 0; }
.hire-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #111;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hire-meta {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.hire-date { font-size: 11.5px; color: #bbb; flex-shrink: 0; }

/* Departments */
.dept-list { list-style: none; display: flex; flex-direction: column; gap: 10px; }
.dept-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}
.dept-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dept-name {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: #222;
}
.dept-manager {
  font-size: 11.5px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Quick Links */
.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-radius: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  text-decoration: none;
  color: #333;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  transition: all .15s;
}
.quick-item:hover {
  background: #f4f4ff;
  border-color: #c7d2fe;
  transform: translateY(-2px);
}
.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
}
</style>
