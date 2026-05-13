<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  workScheduleApi,
  shiftApi,
  workLocationApi,
  employeeApi,
  departmentApi,
} from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const auth = useAuthStore()
const settings = useSettingsStore()

const activeTab = ref('schedules')
const viewMode = ref('list')
const calendarMonth = ref(startOfMonth(new Date()))
const loading = ref(false)

function startOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

const schedules = ref([])
const shifts = ref([])
const locations = ref([])
const employees = ref([])
const departments = ref([])

const schedulePage = ref(1)
const schedulePageSize = ref(10)
const shiftPage = ref(1)
const shiftPageSize = ref(10)
const locationPage = ref(1)
const locationPageSize = ref(10)

const pagedSchedules = computed(() => {
  const s = (schedulePage.value - 1) * schedulePageSize.value
  return schedules.value.slice(s, s + schedulePageSize.value)
})
const pagedShifts = computed(() => {
  const s = (shiftPage.value - 1) * shiftPageSize.value
  return shifts.value.slice(s, s + shiftPageSize.value)
})
const pagedLocations = computed(() => {
  const s = (locationPage.value - 1) * locationPageSize.value
  return locations.value.slice(s, s + locationPageSize.value)
})

function handleScheduleSizeChange(size) { schedulePageSize.value = size; schedulePage.value = 1 }
function handleShiftSizeChange(size) { shiftPageSize.value = size; shiftPage.value = 1 }
function handleLocationSizeChange(size) { locationPageSize.value = size; locationPage.value = 1 }

const canViewAll = computed(() => auth.hasPermission('SCHEDULE_VIEW_ALL'))
const canCreate = computed(() => auth.hasPermission('SCHEDULE_CREATE'))
const canUpdate = computed(() => auth.hasPermission('SCHEDULE_UPDATE'))
const canDelete = computed(() => auth.hasPermission('SCHEDULE_DELETE'))

const canShiftView = computed(() => auth.hasPermission('SHIFT_VIEW'))
const canShiftCreate = computed(() => auth.hasPermission('SHIFT_CREATE'))
const canShiftUpdate = computed(() => auth.hasPermission('SHIFT_UPDATE'))
const canShiftDelete = computed(() => auth.hasPermission('SHIFT_DELETE'))

const canLocView = computed(() => auth.hasPermission('WORK_LOCATION_VIEW'))
const canLocCreate = computed(() => auth.hasPermission('WORK_LOCATION_CREATE'))
const canLocUpdate = computed(() => auth.hasPermission('WORK_LOCATION_UPDATE'))
const canLocDelete = computed(() => auth.hasPermission('WORK_LOCATION_DELETE'))

function toISODate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function defaultRange() {
  const start = new Date()
  const end = new Date()
  end.setDate(end.getDate() + 30)
  return [toISODate(start), toISODate(end)]
}

const filters = reactive({
  range: defaultRange(),
  employeeId: null,
  departmentId: null,
})

// ---- Schedule dialog ----
const scheduleDialog = reactive({
  visible: false,
  editingId: null,
  form: {
    employeeId: null,
    shiftId: null,
    locationId: null,
    workDate: toISODate(new Date()),
    note: '',
  },
})

// ---- Bulk schedule dialog ----
const bulkDialog = reactive({
  visible: false,
  submitting: false,
  targetMode: 'EMPLOYEES',
  form: {
    employeeIds: [],
    departmentIds: [],
    shiftId: null,
    locationId: null,
    fromDate: toISODate(new Date()),
    toDate: toISODate(new Date(Date.now() + 6 * 86400000)),
    daysOfWeek: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
    skipExisting: true,
    skipPublicHolidays: true,
    note: '',
  },
})
const bulkFormRef = ref(null)
const bulkRules = computed(() => ({
  shiftId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  fromDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  toDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  daysOfWeek: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

const weekdayOptions = computed(() => [
  { value: 'MONDAY', label: settings.t('weekday.MONDAY') },
  { value: 'TUESDAY', label: settings.t('weekday.TUESDAY') },
  { value: 'WEDNESDAY', label: settings.t('weekday.WEDNESDAY') },
  { value: 'THURSDAY', label: settings.t('weekday.THURSDAY') },
  { value: 'FRIDAY', label: settings.t('weekday.FRIDAY') },
  { value: 'SATURDAY', label: settings.t('weekday.SATURDAY') },
  { value: 'SUNDAY', label: settings.t('weekday.SUNDAY') },
])

function openBulkCreate() {
  loadScheduleLookups()
  bulkDialog.targetMode = 'EMPLOYEES'
  Object.assign(bulkDialog.form, {
    employeeIds: [],
    departmentIds: [],
    shiftId: null,
    locationId: null,
    fromDate: toISODate(new Date()),
    toDate: toISODate(new Date(Date.now() + 6 * 86400000)),
    daysOfWeek: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
    skipExisting: true,
    skipPublicHolidays: true,
    note: '',
  })
  bulkDialog.visible = true
}

function selectAllEmployees() {
  bulkDialog.form.employeeIds = employees.value.map(e => e.id)
}

function selectAllDepartments() {
  bulkDialog.form.departmentIds = departments.value.map(d => d.id)
}

async function submitBulk() {
  const valid = await bulkFormRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    shiftId: bulkDialog.form.shiftId,
    locationId: bulkDialog.form.locationId || null,
    fromDate: bulkDialog.form.fromDate,
    toDate: bulkDialog.form.toDate,
    daysOfWeek: bulkDialog.form.daysOfWeek,
    skipExisting: bulkDialog.form.skipExisting,
    skipPublicHolidays: bulkDialog.form.skipPublicHolidays,
    note: bulkDialog.form.note || null,
  }
  if (bulkDialog.targetMode === 'EMPLOYEES') {
    payload.employeeIds = bulkDialog.form.employeeIds
  } else {
    payload.departmentIds = bulkDialog.form.departmentIds
  }

  const targetList = bulkDialog.targetMode === 'EMPLOYEES'
    ? payload.employeeIds
    : payload.departmentIds
  if (!targetList?.length) {
    ElMessage.warning(settings.t('schedule.bulk.selectTarget'))
    return
  }

  bulkDialog.submitting = true
  try {
    const { data } = await workScheduleApi.bulkCreate(payload)
    ElMessage.success(
      `${settings.t('schedule.bulk.done')}: ${data.created} ${settings.t('schedule.bulk.created')}, ${data.skipped} ${settings.t('schedule.bulk.skipped')}`
    )
    bulkDialog.visible = false
    loadSchedules()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    bulkDialog.submitting = false
  }
}
const scheduleFormRef = ref(null)
const scheduleRules = computed(() => ({
  employeeId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  shiftId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  workDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

// ---- Shift dialog ----
const shiftDialog = reactive({
  visible: false,
  editingId: null,
  form: { code: '', name: '', startTime: '08:00:00', endTime: '17:00:00', description: '' },
})
const shiftFormRef = ref(null)
const shiftRules = computed(() => ({
  code: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  name: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  startTime: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  endTime: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

// ---- Location dialog ----
const locationDialog = reactive({
  visible: false,
  editingId: null,
  form: { name: '', address: '', latitude: null, longitude: null, radiusMeters: 100 },
})
const locationFormRef = ref(null)
const locationRules = computed(() => ({
  name: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  latitude: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  longitude: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  radiusMeters: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

onMounted(async () => {
  await Promise.all([loadSchedules(), loadShifts(), loadLocations(), loadScheduleLookups()])
})

async function loadScheduleLookups() {
  if (!canViewAll.value && !canCreate.value) {
    employees.value = []
    departments.value = []
    return
  }

  await Promise.allSettled([loadEmployees(), loadDepartments()])
}

async function loadSchedules() {
  loading.value = true
  try {
    const [from, to] = filters.range || []
    const params = { from, to }
    let data
    if (canViewAll.value) {
      if (filters.employeeId) params.employeeId = filters.employeeId
      if (filters.departmentId) params.departmentId = filters.departmentId
      const res = await workScheduleApi.list(params)
      data = res.data
    } else {
      const res = await workScheduleApi.my(params)
      data = res.data
    }
    schedules.value = Array.isArray(data) ? data : []
    schedulePage.value = 1
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
    schedules.value = []
  } finally {
    loading.value = false
  }
}

async function loadShifts() {
  if (!canShiftView.value) return
  try {
    const { data } = await shiftApi.list()
    shifts.value = Array.isArray(data) ? data : []
    shiftPage.value = 1
  } catch {
    shifts.value = []
  }
}

async function loadLocations() {
  if (!canLocView.value) return
  try {
    const { data } = await workLocationApi.list()
    locations.value = Array.isArray(data) ? data : []
    locationPage.value = 1
  } catch {
    locations.value = []
  }
}

async function loadEmployees() {
  if (!canViewAll.value && !canCreate.value) return
  try {
    const { data } = await employeeApi.list()
    employees.value = Array.isArray(data) ? data : data.content || []
  } catch {
    employees.value = []
  }
}

async function loadDepartments() {
  if (!canViewAll.value && !canCreate.value) return
  try {
    const { data } = await departmentApi.list()
    departments.value = Array.isArray(data) ? data : data.content || []
  } catch {
    departments.value = []
  }
}

function resetFilters() {
  filters.range = defaultRange()
  filters.employeeId = null
  filters.departmentId = null
  loadSchedules()
}

// ---- Schedule CRUD ----
function openScheduleCreate() {
  loadScheduleLookups()
  scheduleDialog.editingId = null
  Object.assign(scheduleDialog.form, {
    employeeId: null,
    shiftId: null,
    locationId: null,
    workDate: toISODate(new Date()),
    note: '',
  })
  scheduleDialog.visible = true
}

function openScheduleEdit(row) {
  loadScheduleLookups()
  scheduleDialog.editingId = row.id
  Object.assign(scheduleDialog.form, {
    employeeId: row.employeeId,
    shiftId: row.shiftId,
    locationId: row.locationId || null,
    workDate: row.workDate,
    note: row.note || '',
  })
  scheduleDialog.visible = true
}

async function saveSchedule() {
  const valid = await scheduleFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (scheduleDialog.editingId) {
      await workScheduleApi.update(scheduleDialog.editingId, scheduleDialog.form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await workScheduleApi.create(scheduleDialog.form)
      ElMessage.success(settings.t('common.created'))
    }
    scheduleDialog.visible = false
    loadSchedules()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function deleteSchedule(row) {
  try {
    await ElMessageBox.confirm(
      `${settings.t('schedule.deleteConfirm')} (${row.employeeName} - ${row.workDate})?`,
      settings.t('common.confirm'),
      { type: 'warning' }
    )
    await workScheduleApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    loadSchedules()
  } catch {}
}

// ---- Shift CRUD ----
function openShiftCreate() {
  shiftDialog.editingId = null
  Object.assign(shiftDialog.form, {
    code: '', name: '', startTime: '08:00:00', endTime: '17:00:00', description: '',
  })
  shiftDialog.visible = true
}

function openShiftEdit(row) {
  shiftDialog.editingId = row.id
  Object.assign(shiftDialog.form, {
    code: row.code,
    name: row.name,
    startTime: row.startTime,
    endTime: row.endTime,
    description: row.description || '',
  })
  shiftDialog.visible = true
}

async function saveShift() {
  const valid = await shiftFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (shiftDialog.editingId) {
      await shiftApi.update(shiftDialog.editingId, shiftDialog.form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await shiftApi.create(shiftDialog.form)
      ElMessage.success(settings.t('common.created'))
    }
    shiftDialog.visible = false
    loadShifts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function deleteShift(row) {
  try {
    await ElMessageBox.confirm(
      `${settings.t('shift.deleteConfirm')} "${row.name}"?`,
      settings.t('common.confirm'),
      { type: 'warning' }
    )
    await shiftApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    loadShifts()
  } catch {}
}

// ---- Location CRUD ----
function openLocationCreate() {
  locationDialog.editingId = null
  Object.assign(locationDialog.form, {
    name: '', address: '', latitude: null, longitude: null, radiusMeters: 100,
  })
  locationDialog.visible = true
}

function openLocationEdit(row) {
  locationDialog.editingId = row.id
  Object.assign(locationDialog.form, {
    name: row.name,
    address: row.address || '',
    latitude: row.latitude,
    longitude: row.longitude,
    radiusMeters: row.radiusMeters,
  })
  locationDialog.visible = true
}

function useCurrentLocation() {
  if (!navigator.geolocation) {
    ElMessage.warning(settings.t('attendance.geoUnsupported'))
    return
  }
  navigator.geolocation.getCurrentPosition(
    pos => {
      locationDialog.form.latitude = Number(pos.coords.latitude.toFixed(6))
      locationDialog.form.longitude = Number(pos.coords.longitude.toFixed(6))
      ElMessage.success(settings.t('attendance.locationFetched'))
    },
    err => ElMessage.error(err.message),
    { enableHighAccuracy: true, timeout: 15000 }
  )
}

async function saveLocation() {
  const valid = await locationFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (locationDialog.editingId) {
      await workLocationApi.update(locationDialog.editingId, locationDialog.form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await workLocationApi.create(locationDialog.form)
      ElMessage.success(settings.t('common.created'))
    }
    locationDialog.visible = false
    loadLocations()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function deleteLocation(row) {
  try {
    await ElMessageBox.confirm(
      `${settings.t('location.deleteConfirm')} "${row.name}"?`,
      settings.t('common.confirm'),
      { type: 'warning' }
    )
    await workLocationApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    loadLocations()
  } catch {}
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString()
}

function formatTime(value) {
  if (!value) return '-'
  if (typeof value === 'string') return value.slice(0, 5)
  const d = new Date(value)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// ---- Calendar view ----
const todayISO = computed(() => toISODate(new Date()))

function normalizeDateKey(value) {
  if (!value) return ''
  if (typeof value === 'string') return value.slice(0, 10)
  const d = value instanceof Date ? value : new Date(value)
  return Number.isNaN(d.getTime()) ? '' : toISODate(d)
}

const schedulesByDate = computed(() => {
  const map = {}
  for (const s of schedules.value) {
    const key = normalizeDateKey(s.workDate)
    if (!key) continue
    if (!map[key]) map[key] = []
    map[key].push(s)
  }
  return map
})

const calendarGrid = computed(() => {
  const base = calendarMonth.value
  const year = base.getFullYear()
  const month = base.getMonth()
  const firstOfMonth = new Date(year, month, 1)
  const offset = (firstOfMonth.getDay() + 6) % 7
  const gridStart = new Date(year, month, 1 - offset)
  const days = []
  for (let i = 0; i < 42; i++) {
    const d = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + i)
    const iso = toISODate(d)
    days.push({
      date: d,
      iso,
      day: d.getDate(),
      inMonth: d.getMonth() === month,
      isToday: iso === todayISO.value,
      isWeekend: d.getDay() === 0 || d.getDay() === 6,
      items: schedulesByDate.value[iso] || [],
    })
  }
  return days
})

const calendarTitle = computed(() => {
  const d = calendarMonth.value
  const locale = settings.language === 'vi' ? 'vi-VN' : 'en-US'
  return new Intl.DateTimeFormat(locale, { month: 'long', year: 'numeric' }).format(d)
})

const weekdayHeaders = computed(() => {
  const locale = settings.language === 'vi' ? 'vi-VN' : 'en-US'
  const fmt = new Intl.DateTimeFormat(locale, { weekday: 'short' })
  const list = []
  for (let i = 0; i < 7; i++) {
    list.push(fmt.format(new Date(2024, 0, 1 + i)))
  }
  return list
})

function syncCalendarRange() {
  const d = calendarMonth.value
  const year = d.getFullYear()
  const month = d.getMonth()
  const firstOfMonth = new Date(year, month, 1)
  const offset = (firstOfMonth.getDay() + 6) % 7
  const gridStart = new Date(year, month, 1 - offset)
  const gridEnd = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + 41)
  filters.range = [toISODate(gridStart), toISODate(gridEnd)]
  loadSchedules()
}

function changeMonth(delta) {
  const d = calendarMonth.value
  calendarMonth.value = new Date(d.getFullYear(), d.getMonth() + delta, 1)
  syncCalendarRange()
}

function goToThisMonth() {
  calendarMonth.value = startOfMonth(new Date())
  syncCalendarRange()
}

function setViewMode(mode) {
  viewMode.value = mode
  if (mode === 'calendar') syncCalendarRange()
}

function openCalendarCreate(cell) {
  if (!canCreate.value) return
  loadScheduleLookups()
  scheduleDialog.editingId = null
  Object.assign(scheduleDialog.form, {
    employeeId: null,
    shiftId: null,
    locationId: null,
    workDate: cell.iso,
    note: '',
  })
  scheduleDialog.visible = true
}

function cellChipType(item) {
  const hash = (item.shiftName || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  const palette = ['primary', 'success', 'warning', 'danger', 'info']
  return palette[hash % palette.length]
}
</script>

<template>
  <div class="page-card schedule-page">
    <el-tabs v-model="activeTab" class="schedule-tabs">
      <el-tab-pane name="schedules">
        <template #label>
          <span class="tab-label"><el-icon><Calendar /></el-icon>{{ settings.t('schedule.tabSchedules') }}</span>
        </template>
        <div class="filter-bar">
          <div class="filter-group">
            <el-radio-group :model-value="viewMode" class="view-toggle" @update:model-value="setViewMode">
              <el-radio-button value="list">
                <el-icon><List /></el-icon>
              </el-radio-button>
              <el-radio-button value="calendar">
                <el-icon><Calendar /></el-icon>
              </el-radio-button>
            </el-radio-group>
            <el-date-picker v-if="viewMode === 'list'" v-model="filters.range" type="daterange" value-format="YYYY-MM-DD"
              :range-separator="'-'" :start-placeholder="settings.t('leave.startDate')"
              :end-placeholder="settings.t('leave.endDate')" @change="loadSchedules" />
            <el-select v-if="canViewAll" v-model="filters.employeeId" filterable clearable
              :placeholder="settings.t('attendance.allEmployees')" style="width:220px"
              @change="loadSchedules">
              <el-option v-for="e in employees" :key="e.id"
                :label="`${e.fullName} (${e.employeeCode})`" :value="e.id" />
            </el-select>
            <el-select v-if="canViewAll" v-model="filters.departmentId" filterable clearable
              :placeholder="settings.t('employee.department')" style="width:200px"
              @change="loadSchedules">
              <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
            <el-button @click="resetFilters">
              <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
            </el-button>
          </div>
          <div class="filter-actions">
            <el-button v-if="canCreate" @click="openBulkCreate">
              <el-icon><Calendar /></el-icon> {{ settings.t('schedule.bulk.add') }}
            </el-button>
            <el-button v-if="canCreate" type="primary" @click="openScheduleCreate">
              <el-icon><Plus /></el-icon> {{ settings.t('schedule.add') }}
            </el-button>
          </div>
        </div>

        <template v-if="viewMode === 'list'">
          <el-table :data="pagedSchedules" v-loading="loading" stripe style="width:100%" size="small"
            :empty-text="settings.t('common.noData')">
            <el-table-column :label="settings.t('attendance.date')" width="120">
              <template #default="{ row }">{{ formatDate(row.workDate) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('leave.employeeName')" min-width="180">
              <template #default="{ row }">
                <div>{{ row.employeeName }}</div>
                <div class="muted">{{ row.employeeCode }}</div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('employee.department')" min-width="140" prop="departmentName" />
            <el-table-column :label="settings.t('attendance.shift')" min-width="160">
              <template #default="{ row }">
                <div>{{ row.shiftName || '-' }}</div>
                <div class="muted" v-if="row.shiftStart">
                  {{ formatTime(row.shiftStart) }} - {{ formatTime(row.shiftEnd) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('attendance.location')" min-width="160">
              <template #default="{ row }">
                <div>{{ row.locationName || '-' }}</div>
                <div class="muted" v-if="row.locationRadiusMeters">
                  {{ row.locationRadiusMeters }} m
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="settings.t('common.description')" min-width="160" prop="note" />
            <el-table-column :label="settings.t('common.actions')" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canUpdate" text type="primary" size="small" @click="openScheduleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button v-if="canDelete" text type="danger" size="small" @click="deleteSchedule(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="schedulePage"
              v-model:page-size="schedulePageSize"
              :total="schedules.length"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleScheduleSizeChange"
            />
          </div>
        </template>

        <div v-else class="calendar-wrap" v-loading="loading">
          <div class="calendar-toolbar">
            <div class="calendar-nav">
              <el-button circle size="small" @click="changeMonth(-1)">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <strong class="calendar-title">{{ calendarTitle }}</strong>
              <el-button circle size="small" @click="changeMonth(1)">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
              <el-button size="small" plain @click="goToThisMonth">
                {{ settings.t('leave.today') }}
              </el-button>
            </div>
            <span class="calendar-hint muted">
              {{ schedules.length }} {{ settings.t('schedule.tabSchedules').toLowerCase() }}
            </span>
          </div>

          <div class="calendar-grid-head">
            <div v-for="(name, idx) in weekdayHeaders" :key="idx" class="calendar-weekday">
              {{ name }}
            </div>
          </div>

          <div class="calendar-grid">
            <div
              v-for="cell in calendarGrid"
              :key="cell.iso"
              class="calendar-cell"
              :class="{
                'is-other-month': !cell.inMonth,
                'is-today': cell.isToday,
                'is-weekend': cell.isWeekend,
                'is-clickable': canCreate,
              }"
              @click="openCalendarCreate(cell)"
            >
              <div class="calendar-cell-head">
                <span class="calendar-day-num">{{ cell.day }}</span>
                <span v-if="cell.items.length" class="calendar-count">{{ cell.items.length }}</span>
              </div>
              <div class="calendar-chip-list">
                <div
                  v-for="item in cell.items.slice(0, 3)"
                  :key="item.id"
                  class="calendar-chip"
                  :class="`chip-${cellChipType(item)}`"
                  @click.stop="canUpdate ? openScheduleEdit(item) : null"
                >
                  <span class="chip-time">{{ formatTime(item.shiftStart) }}</span>
                  <span class="chip-name">{{ item.employeeName || item.shiftName }}</span>
                </div>
                <div v-if="cell.items.length > 3" class="calendar-more">
                  +{{ cell.items.length - 3 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="canShiftView" name="shifts">
        <template #label>
          <span class="tab-label"><el-icon><Clock /></el-icon>{{ settings.t('schedule.tabShifts') }}</span>
        </template>
        <div class="filter-bar">
          <div />
          <el-button v-if="canShiftCreate" type="primary" @click="openShiftCreate">
            <el-icon><Plus /></el-icon> {{ settings.t('shift.add') }}
          </el-button>
        </div>
        <el-table :data="pagedShifts" stripe size="small" :empty-text="settings.t('common.noData')">
          <el-table-column prop="code" :label="settings.t('shift.code')" width="140" />
          <el-table-column prop="name" :label="settings.t('shift.name')" min-width="180" />
          <el-table-column :label="settings.t('shift.startTime')" width="120">
            <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('shift.endTime')" width="120">
            <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="description" :label="settings.t('common.description')" min-width="200" />
          <el-table-column :label="settings.t('common.actions')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canShiftUpdate" text type="primary" size="small" @click="openShiftEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button v-if="canShiftDelete" text type="danger" size="small" @click="deleteShift(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="shiftPage"
            v-model:page-size="shiftPageSize"
            :total="shifts.length"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleShiftSizeChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="canLocView" name="locations">
        <template #label>
          <span class="tab-label"><el-icon><LocationFilled /></el-icon>{{ settings.t('schedule.tabLocations') }}</span>
        </template>
        <div class="filter-bar">
          <div />
          <el-button v-if="canLocCreate" type="primary" @click="openLocationCreate">
            <el-icon><Plus /></el-icon> {{ settings.t('location.add') }}
          </el-button>
        </div>
        <el-table :data="pagedLocations" stripe size="small" :empty-text="settings.t('common.noData')">
          <el-table-column prop="name" :label="settings.t('location.name')" min-width="180" />
          <el-table-column prop="address" :label="settings.t('employee.address')" min-width="220" />
          <el-table-column :label="settings.t('location.coordinates')" min-width="220">
            <template #default="{ row }">
              <code>{{ row.latitude }}, {{ row.longitude }}</code>
            </template>
          </el-table-column>
          <el-table-column :label="settings.t('attendance.allowedRadius')" width="140">
            <template #default="{ row }">{{ row.radiusMeters }} m</template>
          </el-table-column>
          <el-table-column :label="settings.t('common.actions')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canLocUpdate" text type="primary" size="small" @click="openLocationEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button v-if="canLocDelete" text type="danger" size="small" @click="deleteLocation(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="locationPage"
            v-model:page-size="locationPageSize"
            :total="locations.length"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleLocationSizeChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Schedule dialog -->
    <el-dialog v-model="scheduleDialog.visible"
      :title="settings.t(scheduleDialog.editingId ? 'schedule.edit' : 'schedule.add')"
      width="600px" destroy-on-close class="vx-dialog schedule-dialog" align-center>
      <el-form ref="scheduleFormRef" :model="scheduleDialog.form" :rules="scheduleRules" label-position="top">
        <section class="dialog-section">
          <div class="dialog-section-head">
            <el-icon class="dialog-section-icon"><User /></el-icon>
            <span>{{ settings.t('leave.employeeName') }}</span>
          </div>
          <el-form-item prop="employeeId">
            <el-select v-model="scheduleDialog.form.employeeId" filterable style="width:100%"
              :placeholder="settings.t('department.selectEmployee')">
              <el-option v-for="e in employees" :key="e.id"
                :label="`${e.fullName} (${e.employeeCode})`" :value="e.id" />
            </el-select>
          </el-form-item>
        </section>

        <section class="dialog-section">
          <div class="dialog-section-head">
            <el-icon class="dialog-section-icon"><Clock /></el-icon>
            <span>{{ settings.t('attendance.shift') }}</span>
          </div>
          <el-row :gutter="12">
            <el-col :span="24">
              <el-form-item :label="settings.t('attendance.shift')" prop="shiftId">
                <el-select v-model="scheduleDialog.form.shiftId" filterable style="width:100%"
                  :placeholder="settings.t('schedule.selectShift')">
                  <el-option v-for="s in shifts" :key="s.id"
                    :label="`${s.name} (${formatTime(s.startTime)} - ${formatTime(s.endTime)})`"
                    :value="s.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('attendance.date')" prop="workDate">
                <el-date-picker v-model="scheduleDialog.form.workDate" type="date" value-format="YYYY-MM-DD"
                  style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('attendance.location')">
                <el-select v-model="scheduleDialog.form.locationId" filterable clearable style="width:100%"
                  :placeholder="settings.t('schedule.selectLocation')">
                  <el-option v-for="l in locations" :key="l.id"
                    :label="`${l.name} (${l.radiusMeters} m)`" :value="l.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="settings.t('common.description')">
            <el-input v-model="scheduleDialog.form.note" type="textarea" :rows="2" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialog.visible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveSchedule">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Shift dialog -->
    <el-dialog v-model="shiftDialog.visible"
      :title="settings.t(shiftDialog.editingId ? 'shift.edit' : 'shift.add')"
      width="520px" destroy-on-close class="vx-dialog schedule-dialog" align-center>
      <el-form ref="shiftFormRef" :model="shiftDialog.form" :rules="shiftRules" label-position="top">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('shift.code')" prop="code">
              <el-input v-model="shiftDialog.form.code" :disabled="!!shiftDialog.editingId" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('shift.name')" prop="name">
              <el-input v-model="shiftDialog.form.name" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('shift.startTime')" prop="startTime">
              <el-time-picker v-model="shiftDialog.form.startTime" value-format="HH:mm:ss"
                format="HH:mm" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('shift.endTime')" prop="endTime">
              <el-time-picker v-model="shiftDialog.form.endTime" value-format="HH:mm:ss"
                format="HH:mm" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="settings.t('common.description')">
              <el-input v-model="shiftDialog.form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="shiftDialog.visible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveShift">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Bulk schedule dialog -->
    <el-dialog v-model="bulkDialog.visible" :title="settings.t('schedule.bulk.add')"
      width="720px" destroy-on-close class="vx-dialog schedule-dialog" align-center>
      <el-form ref="bulkFormRef" :model="bulkDialog.form" :rules="bulkRules" label-position="top">
        <section class="dialog-section">
          <div class="dialog-section-head">
            <el-icon class="dialog-section-icon"><UserFilled /></el-icon>
            <span>{{ settings.t('schedule.bulk.target') }}</span>
          </div>
          <el-radio-group v-model="bulkDialog.targetMode" class="target-radio">
            <el-radio-button value="EMPLOYEES">{{ settings.t('schedule.bulk.byEmployees') }}</el-radio-button>
            <el-radio-button value="DEPARTMENTS">{{ settings.t('schedule.bulk.byDepartments') }}</el-radio-button>
          </el-radio-group>

          <el-form-item v-if="bulkDialog.targetMode === 'EMPLOYEES'"
            :label="settings.t('leave.employeeName')">
            <div class="bulk-select-row">
              <el-select v-model="bulkDialog.form.employeeIds" multiple filterable collapse-tags
                collapse-tags-tooltip :placeholder="settings.t('department.selectEmployee')"
                style="flex:1">
                <el-option v-for="e in employees" :key="e.id"
                  :label="`${e.fullName} (${e.employeeCode})`" :value="e.id" />
              </el-select>
              <el-button size="small" @click="selectAllEmployees">
                {{ settings.t('role.selectAll') }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item v-else :label="settings.t('employee.department')">
            <div class="bulk-select-row">
              <el-select v-model="bulkDialog.form.departmentIds" multiple filterable collapse-tags
                collapse-tags-tooltip :placeholder="settings.t('employee.selectDepartment')"
                style="flex:1">
                <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
              <el-button size="small" @click="selectAllDepartments">
                {{ settings.t('role.selectAll') }}
              </el-button>
            </div>
          </el-form-item>
        </section>

        <section class="dialog-section">
          <div class="dialog-section-head">
            <el-icon class="dialog-section-icon"><Clock /></el-icon>
            <span>{{ settings.t('attendance.shift') }} · {{ settings.t('attendance.location') }}</span>
          </div>
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('attendance.shift')" prop="shiftId">
                <el-select v-model="bulkDialog.form.shiftId" filterable style="width:100%"
                  :placeholder="settings.t('schedule.selectShift')">
                  <el-option v-for="s in shifts" :key="s.id"
                    :label="`${s.name} (${formatTime(s.startTime)} - ${formatTime(s.endTime)})`"
                    :value="s.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('attendance.location')">
                <el-select v-model="bulkDialog.form.locationId" filterable clearable style="width:100%"
                  :placeholder="settings.t('schedule.selectLocation')">
                  <el-option v-for="l in locations" :key="l.id"
                    :label="`${l.name} (${l.radiusMeters} m)`" :value="l.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="dialog-section">
          <div class="dialog-section-head">
            <el-icon class="dialog-section-icon"><Calendar /></el-icon>
            <span>{{ settings.t('leave.startDate') }} · {{ settings.t('leave.endDate') }}</span>
          </div>
          <el-row :gutter="12">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.startDate')" prop="fromDate">
                <el-date-picker v-model="bulkDialog.form.fromDate" type="date"
                  value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.endDate')" prop="toDate">
                <el-date-picker v-model="bulkDialog.form.toDate" type="date"
                  value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item :label="settings.t('schedule.bulk.daysOfWeek')" prop="daysOfWeek">
            <el-checkbox-group v-model="bulkDialog.form.daysOfWeek" class="weekday-group">
              <el-checkbox-button v-for="d in weekdayOptions" :key="d.value" :value="d.value">
                {{ d.label }}
              </el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="bulkDialog.form.skipExisting">
              {{ settings.t('schedule.bulk.skipExisting') }}
            </el-checkbox>
            <el-checkbox v-model="bulkDialog.form.skipPublicHolidays">
              {{ settings.t('schedule.bulk.skipPublicHolidays') }}
            </el-checkbox>
          </el-form-item>

          <el-form-item :label="settings.t('common.description')">
            <el-input v-model="bulkDialog.form.note" type="textarea" :rows="2" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="bulkDialog.visible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="bulkDialog.submitting" @click="submitBulk">
          {{ settings.t('common.save') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Location dialog -->
    <el-dialog v-model="locationDialog.visible"
      :title="settings.t(locationDialog.editingId ? 'location.edit' : 'location.add')"
      width="600px" destroy-on-close class="vx-dialog schedule-dialog" align-center>
      <el-form ref="locationFormRef" :model="locationDialog.form" :rules="locationRules" label-position="top">
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item :label="settings.t('location.name')" prop="name">
              <el-input v-model="locationDialog.form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="settings.t('employee.address')">
              <el-input v-model="locationDialog.form.address" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('attendance.latitude')" prop="latitude">
              <el-input-number v-model="locationDialog.form.latitude" :precision="6"
                :step="0.0001" :min="-90" :max="90" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="settings.t('attendance.longitude')" prop="longitude">
              <el-input-number v-model="locationDialog.form.longitude" :precision="6"
                :step="0.0001" :min="-180" :max="180" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-button size="small" plain @click="useCurrentLocation">
              <el-icon><LocationFilled /></el-icon> {{ settings.t('location.useCurrent') }}
            </el-button>
          </el-col>
          <el-col :span="24" style="margin-top:14px">
            <el-form-item :label="settings.t('location.radiusMeters')" prop="radiusMeters">
              <el-input-number v-model="locationDialog.form.radiusMeters" :min="10" :step="10"
                style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="locationDialog.visible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveLocation">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.schedule-tabs {
  --el-tabs-header-height: 48px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 16px;
  margin-bottom: 14px;
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-radius: 12px;
}

.filter-group {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

code {
  background: var(--el-fill-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.dialog-section {
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-radius: 12px;
  padding: 14px 16px 2px;
  margin-bottom: 14px;
}

.dialog-section:last-child {
  margin-bottom: 0;
}

.dialog-section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vx-text);
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 10px;
}

.dialog-section-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  color: var(--vx-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.bulk-select-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  width: 100%;
}

.target-radio {
  margin-bottom: 14px;
}

.weekday-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

:deep(.schedule-dialog .el-dialog__body) {
  max-height: min(70vh, 680px);
  overflow-y: auto;
}

:deep(.schedule-dialog .el-form-item) {
  margin-bottom: 12px;
}

:deep(.schedule-dialog .el-form-item__label) {
  padding-bottom: 3px;
  font-weight: 600;
}

.view-toggle :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
}

.calendar-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.calendar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--vx-text);
  min-width: 160px;
  text-align: center;
  text-transform: capitalize;
}

.calendar-hint {
  font-size: 13px;
}

.calendar-grid-head {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-bottom: none;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}

.calendar-weekday {
  padding: 10px 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--vx-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-auto-rows: minmax(110px, auto);
  border: 1px solid var(--vx-border);
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  background: var(--vx-border);
  gap: 1px;
}

.calendar-cell {
  background: #FFFFFF;
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 110px;
  transition: background 0.15s;
  position: relative;
}

.calendar-cell.is-clickable {
  cursor: pointer;
}

.calendar-cell.is-clickable:hover {
  background: color-mix(in srgb, var(--vx-primary) 4%, #FFFFFF);
}

.calendar-cell.is-other-month {
  background: var(--vx-bg);
}

.calendar-cell.is-other-month .calendar-day-num {
  color: var(--vx-text-secondary);
  opacity: 0.5;
}

.calendar-cell.is-weekend:not(.is-other-month) {
  background: color-mix(in srgb, var(--vx-bg) 60%, #FFFFFF);
}

.calendar-cell.is-today {
  background: color-mix(in srgb, var(--vx-primary) 6%, #FFFFFF);
}

.calendar-cell.is-today .calendar-day-num {
  background: var(--vx-primary);
  color: #FFFFFF;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.calendar-cell-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-day-num {
  font-size: 13px;
  font-weight: 600;
  color: var(--vx-text);
}

.calendar-count {
  font-size: 11px;
  font-weight: 600;
  color: var(--vx-primary);
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  border-radius: 8px;
  padding: 1px 6px;
  min-width: 18px;
  text-align: center;
}

.calendar-chip-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
  overflow: hidden;
}

.calendar-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.4;
  cursor: pointer;
  overflow: hidden;
  border: 1px solid transparent;
}

.calendar-chip .chip-time {
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
}

.calendar-chip .chip-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip-primary {
  background: color-mix(in srgb, #4F46E5 12%, transparent);
  color: #3730A3;
  border-color: color-mix(in srgb, #4F46E5 24%, transparent);
}

.chip-success {
  background: color-mix(in srgb, #10B981 12%, transparent);
  color: #065F46;
  border-color: color-mix(in srgb, #10B981 24%, transparent);
}

.chip-warning {
  background: color-mix(in srgb, #F59E0B 14%, transparent);
  color: #92400E;
  border-color: color-mix(in srgb, #F59E0B 28%, transparent);
}

.chip-danger {
  background: color-mix(in srgb, #EF4444 12%, transparent);
  color: #991B1B;
  border-color: color-mix(in srgb, #EF4444 24%, transparent);
}

.chip-info {
  background: color-mix(in srgb, #0EA5E9 12%, transparent);
  color: #075985;
  border-color: color-mix(in srgb, #0EA5E9 24%, transparent);
}

.calendar-chip:hover {
  filter: brightness(0.96);
}

.calendar-more {
  font-size: 11px;
  color: var(--vx-text-secondary);
  padding: 0 6px;
}

@media (max-width: 760px) {
  .calendar-grid {
    grid-auto-rows: minmax(80px, auto);
  }

  .calendar-cell {
    min-height: 80px;
    padding: 4px 5px;
  }

  .calendar-chip .chip-name {
    display: none;
  }
}
</style>
