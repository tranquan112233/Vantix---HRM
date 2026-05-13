<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi, employeeApi, workScheduleApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const auth = useAuthStore()
const settings = useSettingsStore()

const loading = ref(false)
const checking = ref(false)
const locating = ref(false)
const today = ref(null)
const todaySchedule = ref(null)
const history = ref([])
const employees = ref([])
const position = ref(null)
const positionError = ref('')

const page = ref(1)
const pageSize = ref(10)
const total = computed(() => history.value.length)
const pagedHistory = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return history.value.slice(start, start + pageSize.value)
})

const canViewAll = computed(() => auth.hasPermission('ATTENDANCE_VIEW_ALL'))

const filters = reactive({
  employeeId: null,
  range: defaultRange(),
})

function defaultRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  return [toISODate(start), toISODate(end)]
}

function toISODate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const statusOptions = {
  ON_TIME: { label: 'attendance.status.ON_TIME', type: 'success' },
  LATE: { label: 'attendance.status.LATE', type: 'warning' },
  EARLY_LEAVE: { label: 'attendance.status.EARLY_LEAVE', type: 'warning' },
  LATE_AND_EARLY: { label: 'attendance.status.LATE_AND_EARLY', type: 'danger' },
  ABSENT: { label: 'attendance.status.ABSENT', type: 'danger' },
  PENDING: { label: 'attendance.status.PENDING', type: 'info' },
  MISSING_CHECKOUT: { label: 'attendance.status.MISSING_CHECKOUT', type: 'danger' },
}

onMounted(async () => {
  await Promise.all([
    loadToday(),
    loadTodaySchedule(),
    loadHistory(),
    canViewAll.value ? loadEmployees() : Promise.resolve(),
  ])
  obtainPosition()
})

async function loadToday() {
  try {
    const { data, status } = await attendanceApi.today()
    today.value = status === 204 ? null : data
  } catch {
    today.value = null
  }
}

async function loadTodaySchedule() {
  try {
    const { data, status } = await workScheduleApi.today()
    todaySchedule.value = status === 204 ? null : data
  } catch {
    todaySchedule.value = null
  }
}

async function loadHistory() {
  loading.value = true
  try {
    const [from, to] = filters.range || []
    const params = { from, to }
    let data
    if (canViewAll.value) {
      params.employeeId = filters.employeeId || undefined
      const res = await attendanceApi.list(params)
      data = res.data
    } else {
      const res = await attendanceApi.my(params)
      data = res.data
    }
    history.value = Array.isArray(data) ? data : []
    page.value = 1
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
    history.value = []
  } finally {
    loading.value = false
  }
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
}

async function loadEmployees() {
  try {
    const { data } = await employeeApi.list({ page: 1, size: 10000 })
    employees.value = Array.isArray(data) ? data : data.content || []
  } catch {
    employees.value = []
  }
}

function obtainPosition() {
  if (!navigator.geolocation) {
    positionError.value = settings.t('attendance.geoUnsupported')
    return
  }
  locating.value = true
  positionError.value = ''
  navigator.geolocation.getCurrentPosition(
    pos => {
      position.value = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
        accuracy: pos.coords.accuracy,
      }
      locating.value = false
    },
    err => {
      positionError.value = err.message || settings.t('attendance.geoDenied')
      locating.value = false
    },
    { enableHighAccuracy: true, timeout: 15000 }
  )
}

async function handleCheckIn() {
  if (!position.value) {
    ElMessage.warning(settings.t('attendance.needLocation'))
    obtainPosition()
    return
  }
  checking.value = true
  try {
    await attendanceApi.checkIn({
      latitude: position.value.latitude,
      longitude: position.value.longitude,
    })
    ElMessage.success(settings.t('attendance.checkedIn'))
    await Promise.all([loadToday(), loadHistory()])
  } catch (e) {
    ElMessage.error(attendanceErrorMessage(e))
  } finally {
    checking.value = false
  }
}

async function handleCheckOut() {
  if (!position.value) {
    ElMessage.warning(settings.t('attendance.needLocation'))
    obtainPosition()
    return
  }
  checking.value = true
  try {
    await attendanceApi.checkOut({
      latitude: position.value.latitude,
      longitude: position.value.longitude,
    })
    ElMessage.success(settings.t('attendance.checkedOut'))
    await Promise.all([loadToday(), loadHistory()])
  } catch (e) {
    ElMessage.error(attendanceErrorMessage(e))
  } finally {
    checking.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'
  const d = new Date(value)
  return d.toLocaleDateString()
}

function formatDateTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  return d.toLocaleString()
}

function formatTime(value) {
  if (!value) return '-'
  if (typeof value === 'string' && value.length <= 8) return value.slice(0, 5)
  const d = new Date(value)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatDistance(value) {
  if (value === null || value === undefined) return '-'
  return `${Math.round(value)} m`
}

function attendanceErrorMessage(error) {
  const message = error.response?.data?.message
  const match = message?.match(/You are (\d+) meters away from (.+) \(allowed (\d+) m\)/)
  if (match) {
    return settings.t('attendance.locationOutOfRange', {
      distance: match[1],
      location: match[2],
      radius: match[3],
    })
  }
  return message || settings.t('common.somethingWrong')
}

function statusType(status) {
  return statusOptions[status]?.type || 'info'
}

function statusLabel(status) {
  return settings.t(statusOptions[status]?.label || 'common.noData')
}

const shiftLabel = computed(() => {
  if (!todaySchedule.value) return null
  const { shiftName, shiftStart, shiftEnd } = todaySchedule.value
  if (!shiftName) return null
  return `${shiftName} (${formatTime(shiftStart)} - ${formatTime(shiftEnd)})`
})

function resetFilters() {
  filters.employeeId = null
  filters.range = defaultRange()
  loadHistory()
}
</script>

<template>
  <div class="attendance-page">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <div class="page-card today-card">
          <div class="today-hero">
            <div class="today-hero-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="today-hero-body">
              <span class="today-hero-label">{{ settings.t('attendance.today') }}</span>
              <strong class="today-hero-date">{{ formatDate(new Date()) }}</strong>
            </div>
            <el-tag
              v-if="today?.status"
              :type="statusType(today.status)"
              effect="dark"
              round
            >
              {{ statusLabel(today.status) }}
            </el-tag>
          </div>

          <div class="info-block">
            <div class="info-block-head">
              <el-icon class="info-block-icon"><Calendar /></el-icon>
              <span>{{ settings.t('attendance.shift') }}</span>
            </div>
            <div v-if="todaySchedule" class="info-block-body">
              <div class="info-row">
                <strong>{{ shiftLabel || '-' }}</strong>
              </div>
              <div v-if="todaySchedule.locationName" class="info-row">
                <el-icon class="info-sub-icon"><LocationFilled /></el-icon>
                <span>{{ todaySchedule.locationName }}</span>
                <el-tag size="small" effect="plain" round>
                  ± {{ todaySchedule.locationRadiusMeters }} m
                </el-tag>
              </div>
              <div v-if="todaySchedule.locationAddress" class="info-row muted">
                {{ todaySchedule.locationAddress }}
              </div>
            </div>
            <el-alert
              v-else
              type="warning"
              :closable="false"
              :title="settings.t('attendance.noScheduleToday')"
              show-icon
            />
          </div>

          <div class="info-block">
            <div class="info-block-head">
              <el-icon class="info-block-icon"><LocationFilled /></el-icon>
              <span>{{ settings.t('attendance.yourLocation') }}</span>
              <el-button
                class="info-block-action"
                size="small"
                text
                :loading="locating"
                @click="obtainPosition"
              >
                <el-icon><Refresh /></el-icon> {{ settings.t('attendance.refreshLocation') }}
              </el-button>
            </div>
            <div v-if="position" class="geo-grid">
              <div class="geo-item">
                <span class="geo-key">{{ settings.t('attendance.latitude') }}</span>
                <code>{{ position.latitude.toFixed(6) }}</code>
              </div>
              <div class="geo-item">
                <span class="geo-key">{{ settings.t('attendance.longitude') }}</span>
                <code>{{ position.longitude.toFixed(6) }}</code>
              </div>
              <div class="geo-item">
                <span class="geo-key">{{ settings.t('attendance.accuracy') }}</span>
                <span class="geo-value">± {{ Math.round(position.accuracy) }} m</span>
              </div>
            </div>
            <div v-else-if="positionError" class="geo-error">{{ positionError }}</div>
            <div v-else-if="locating" class="geo-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              {{ settings.t('attendance.locating') }}
            </div>
          </div>

          <div class="check-grid">
            <div class="check-item">
              <div class="check-item-head">
                <el-icon><Right /></el-icon>
                <span>{{ settings.t('attendance.checkInAt') }}</span>
              </div>
              <strong class="check-item-time">
                {{ today?.checkInAt ? formatTime(today.checkInAt) : '--:--' }}
              </strong>
              <span
                v-if="today?.checkInDistance !== null && today?.checkInDistance !== undefined"
                class="check-item-distance"
              >
                {{ formatDistance(today?.checkInDistance) }}
              </span>
            </div>
            <div class="check-item">
              <div class="check-item-head">
                <el-icon><Back /></el-icon>
                <span>{{ settings.t('attendance.checkOutAt') }}</span>
              </div>
              <strong class="check-item-time">
                {{ today?.checkOutAt ? formatTime(today.checkOutAt) : '--:--' }}
              </strong>
              <span
                v-if="today?.checkOutDistance !== null && today?.checkOutDistance !== undefined"
                class="check-item-distance"
              >
                {{ formatDistance(today?.checkOutDistance) }}
              </span>
            </div>
          </div>

          <div class="check-actions">
            <el-button
              type="primary"
              size="large"
              :loading="checking"
              :disabled="!!today?.checkInAt || !todaySchedule"
              @click="handleCheckIn"
            >
              <el-icon><Right /></el-icon> {{ settings.t('attendance.checkIn') }}
            </el-button>
            <el-button
              type="success"
              size="large"
              :loading="checking"
              :disabled="!today?.checkInAt || !!today?.checkOutAt"
              @click="handleCheckOut"
            >
              <el-icon><Back /></el-icon> {{ settings.t('attendance.checkOut') }}
            </el-button>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="14">
        <div class="page-card history-card">
          <div class="history-header">
            <div class="history-title">
              <el-icon class="history-icon"><Document /></el-icon>
              <h3>{{ settings.t('attendance.history') }}</h3>
            </div>
            <el-tag type="info" round effect="plain">{{ total }}</el-tag>
          </div>

          <div class="table-toolbar">
            <div class="history-filters">
              <el-date-picker
                v-model="filters.range"
                type="daterange"
                value-format="YYYY-MM-DD"
                :range-separator="'-'"
                :start-placeholder="settings.t('leave.startDate')"
                :end-placeholder="settings.t('leave.endDate')"
                @change="loadHistory"
              />
              <el-select
                v-if="canViewAll"
                v-model="filters.employeeId"
                filterable
                clearable
                :placeholder="settings.t('attendance.allEmployees')"
                style="width:240px"
                @change="loadHistory"
              >
                <el-option
                  v-for="e in employees"
                  :key="e.id"
                  :label="`${e.fullName} (${e.employeeCode})`"
                  :value="e.id"
                />
              </el-select>
              <el-button @click="resetFilters">
                <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
              </el-button>
            </div>
          </div>

          <el-table
            :data="pagedHistory"
            v-loading="loading"
            stripe
            style="width:100%"
            :empty-text="settings.t('common.noData')"
            size="small"
          >
            <el-table-column :label="settings.t('attendance.date')" width="110">
              <template #default="{ row }">{{ formatDate(row.workDate) }}</template>
            </el-table-column>
            <el-table-column
              v-if="canViewAll"
              :label="settings.t('leave.employeeName')"
              min-width="180"
            >
              <template #default="{ row }">
                <div>{{ row.employeeName }}</div>
                <div class="muted">{{ row.employeeCode }}</div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('attendance.shift')" min-width="140">
              <template #default="{ row }">
                <div>{{ row.shiftName || '-' }}</div>
                <div class="muted" v-if="row.shiftStart">
                  {{ formatTime(row.shiftStart) }} - {{ formatTime(row.shiftEnd) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('attendance.checkIn')" min-width="150">
              <template #default="{ row }">
                <div>{{ formatDateTime(row.checkInAt) }}</div>
                <div class="muted" v-if="row.checkInDistance !== null && row.checkInDistance !== undefined">
                  {{ formatDistance(row.checkInDistance) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('attendance.checkOut')" min-width="150">
              <template #default="{ row }">
                <div>{{ formatDateTime(row.checkOutAt) }}</div>
                <div class="muted" v-if="row.checkOutDistance !== null && row.checkOutDistance !== undefined">
                  {{ formatDistance(row.checkOutDistance) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('attendance.statusLabel')" width="140">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small" effect="light" round>
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.attendance-page {
  padding-bottom: 24px;
}

.today-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.today-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg,
    color-mix(in srgb, var(--vx-primary) 14%, transparent),
    color-mix(in srgb, var(--vx-primary) 4%, transparent));
  border: 1px solid color-mix(in srgb, var(--vx-primary) 18%, transparent);
}

.today-hero-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--vx-primary);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.today-hero-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.today-hero-label {
  color: var(--vx-text-secondary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.today-hero-date {
  color: var(--vx-text);
  font-size: 18px;
  font-weight: 700;
  margin-top: 2px;
}

.info-block {
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-block-head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vx-text);
  font-weight: 600;
  font-size: 13px;
}

.info-block-icon {
  color: var(--vx-primary);
}

.info-block-action {
  margin-left: auto;
}

.info-block-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  color: var(--vx-text);
}

.info-row.muted {
  color: var(--vx-text-secondary);
  font-size: 13px;
}

.info-sub-icon {
  color: var(--vx-text-secondary);
  font-size: 14px;
}

.geo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.geo-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.geo-key {
  font-size: 11px;
  color: var(--vx-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.geo-value {
  font-weight: 600;
  font-size: 13px;
}

.geo-error {
  color: var(--el-color-danger);
  font-size: 13px;
}

.geo-loading {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--vx-text-secondary);
  font-size: 13px;
}

code {
  background: var(--el-fill-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.check-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.check-item {
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.check-item-head {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--vx-text-secondary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.check-item-time {
  font-size: 22px;
  font-weight: 700;
  color: var(--vx-text);
  font-variant-numeric: tabular-nums;
}

.check-item-distance {
  font-size: 12px;
  color: var(--vx-text-secondary);
}

.check-actions {
  display: flex;
  gap: 12px;
}

.check-actions .el-button {
  flex: 1;
}

.history-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.history-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  color: var(--vx-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.history-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.history-filters {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.muted {
  color: var(--vx-text-secondary);
  font-size: 12px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 640px) {
  .geo-grid {
    grid-template-columns: 1fr 1fr;
  }

  .check-grid {
    grid-template-columns: 1fr;
  }
}
</style>
