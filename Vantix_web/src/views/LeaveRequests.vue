<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, leaveRequestApi, profileApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import {
  LEAVE_STATUSES,
  LEAVE_TYPES,
  WORK_CONDITIONS,
  calculateAnnualLeaveEntitlement,
  countLeaveWorkingDays,
  completeYearsBetween,
  estimateWorkedMonthsInYear,
  formatISODate,
  getHolidayMap,
  isDateInRange,
  toISO,
} from '@/utils/vietnamLeaveCalendar'

const PERMISSIONS = {
  view: ['LEAVE_REQUEST_VIEW', 'LEAVE_REQUEST_MANAGE'],
  create: ['LEAVE_REQUEST_CREATE', 'LEAVE_REQUEST_MANAGE'],
  update: ['LEAVE_REQUEST_UPDATE', 'LEAVE_REQUEST_MANAGE'],
  cancel: ['LEAVE_REQUEST_CANCEL', 'LEAVE_REQUEST_MANAGE'],
  viewAll: ['LEAVE_REQUEST_VIEW_ALL', 'LEAVE_REQUEST_MANAGE'],
  approve: ['LEAVE_REQUEST_APPROVE', 'LEAVE_REQUEST_MANAGE'],
}

const MAX_ACTIVE_LEAVE_REQUESTS_PER_YEAR = 12

const auth = useAuthStore()
const settings = useSettingsStore()
const today = new Date()
const selectedYear = ref(today.getFullYear())
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const leaveRequests = ref([])
const employees = ref([])
const currentEmployee = ref(null)
const scope = ref('MINE')

const filters = reactive({
  keyword: '',
  status: '',
  type: '',
})

const calendarOptions = reactive({
  workweek: 'FIVE_DAYS',
})

const defaultForm = {
  employeeId: null,
  employeeCode: '',
  employeeName: '',
  departmentId: null,
  departmentName: '',
  positionId: null,
  positionName: '',
  type: 'ANNUAL',
  startDate: toISO(today),
  endDate: toISO(today),
  dayUnit: 'FULL',
  reason: '',
  handoverEmployeeId: null,
  handoverTo: '',
  emergencyContact: '',
}

const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  employeeId: canApprove.value
    ? [{ required: true, message: settings.t('common.required'), trigger: 'change' }]
    : [],
  type: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  startDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  endDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  reason: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

const roleText = computed(() => String(auth.role || '').toLowerCase())
const usernameText = computed(() => String(auth.username || '').toLowerCase())
const isAdmin = computed(() => roleText.value.includes('admin') || usernameText.value === 'admin')
const canCreate = computed(() => isAdmin.value || auth.canAccess(PERMISSIONS.create, 'any'))
const canViewOwn = computed(() => isAdmin.value || auth.canAccess(PERMISSIONS.view, 'any') || canCreate.value)
const canUpdateOwn = computed(() => isAdmin.value || auth.canAccess(PERMISSIONS.update, 'any'))
const canCancelOwn = computed(() => isAdmin.value || auth.canAccess(PERMISSIONS.cancel, 'any'))
const canApprove = computed(() => (
  isAdmin.value || auth.canAccess(PERMISSIONS.approve, 'any')
))
const canViewAll = computed(() => (
  canApprove.value || auth.canAccess(PERMISSIONS.viewAll, 'any')
))
const currentUserKey = computed(() => String(auth.user?.id || auth.username || 'anonymous'))
const currentUserName = computed(() => auth.username || settings.t('leave.currentEmployee'))
const currentEmployeeId = computed(() => String(currentEmployee.value?.id || ''))

const holidayMap = computed(() => getHolidayMap(selectedYear.value, calendarOptions))
const leaveContext = computed(() => ({
  year: selectedYear.value,
  workweek: calendarOptions.workweek,
  holidayMap: holidayMap.value,
}))

const workweekOptions = computed(() => [
  { value: 'FIVE_DAYS', label: settings.t('leave.workweekFive') },
  { value: 'SIX_DAYS', label: settings.t('leave.workweekSix') },
])

const activeEmployees = computed(() => employees.value
  .filter(employee => !['RESIGNED', 'TERMINATED'].includes(employee.status))
  .sort((a, b) => String(a.fullName || '').localeCompare(String(b.fullName || ''))))

const employeeOptions = computed(() => activeEmployees.value.map(employee => ({
  value: employee.id,
  label: employeeLabel(employee),
})))

const selectedLeaveEmployee = computed(() => {
  if (canApprove.value) {
    return employeeById(form.employeeId)
  }

  return currentEmployee.value
})

const handoverCandidates = computed(() => {
  const selected = selectedLeaveEmployee.value
  const selectedId = String(selected?.id || '')
  const candidates = activeEmployees.value.filter(employee => String(employee.id) !== selectedId)

  if (!selected?.departmentId) {
    return candidates
  }

  const sameDepartment = candidates.filter(employee => (
    String(employee.departmentId || '') === String(selected.departmentId)
  ))

  return sameDepartment.length ? sameDepartment : candidates
})

const handoverOptions = computed(() => handoverCandidates.value.map(employee => ({
  value: employee.id,
  label: employeeLabel(employee),
})))

const leaveTypeOptions = computed(() => LEAVE_TYPES.map(type => ({
  value: type.value,
  label: settings.t(`leave.type.${type.value}`),
})))

const leaveStatusOptions = computed(() => LEAVE_STATUSES.map(status => ({
  value: status.value,
  label: settings.t(`leave.status.${status.value}`),
  type: status.type,
})))

const scopeOptions = computed(() => [
  { value: 'MINE', label: settings.t('leave.myRequests') },
  ...(canViewAll.value ? [{ value: 'ALL', label: settings.t('leave.allRequests') }] : []),
])

const visibleRequests = computed(() => {
  if (!canViewOwn.value && !canViewAll.value) {
    return []
  }

  return leaveRequests.value
  .filter(request => canViewAll.value && scope.value === 'ALL' ? true : isOwnRequest(request))
  .filter(request => requestIntersectsSelectedYear(request))
  .filter(request => !filters.status || request.status === filters.status)
  .filter(request => !filters.type || request.type === filters.type)
  .filter(request => {
    const keyword = filters.keyword.trim().toLowerCase()
    if (!keyword) return true
    return [
      request.employeeName,
      request.employeeCode,
      request.departmentName,
      request.positionName,
      request.reason,
      request.handoverTo,
      request.emergencyContact,
    ].some(value => String(value || '').toLowerCase().includes(keyword))
  })
  .sort((a, b) => compareRequestDate(b, a))
})

const page = ref(1)
const pageSize = ref(10)
const pagedRequests = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return visibleRequests.value.slice(start, start + pageSize.value)
})
function handleSizeChange(size) { pageSize.value = size; page.value = 1 }
watch(visibleRequests, () => { page.value = 1 })

const pendingCount = computed(() => visibleRequests.value.filter(item => item.status === 'PENDING').length)
const approvedCount = computed(() => visibleRequests.value.filter(item => item.status === 'APPROVED').length)
const declinedCount = computed(() => visibleRequests.value.filter(item => ['REJECTED', 'CANCELLED'].includes(item.status)).length)
const myApprovedAnnualDays = computed(() => leaveRequests.value
  .filter(item => isOwnRequest(item) && item.status === 'APPROVED' && item.type === 'ANNUAL')
  .filter(requestIntersectsSelectedYear)
  .reduce((total, item) => total + requestDays(item), 0))
const myPendingAnnualDays = computed(() => leaveRequests.value
  .filter(item => isOwnRequest(item) && item.status === 'PENDING' && item.type === 'ANNUAL')
  .filter(requestIntersectsSelectedYear)
  .reduce((total, item) => total + requestDays(item), 0))
const annualEntitlement = computed(() => employeeAnnualEntitlement(currentEmployee.value))
const selectedAnnualEntitlement = computed(() => employeeAnnualEntitlement(selectedLeaveEmployee.value))
const remainingAnnualDays = computed(() => (
  Math.max(0, Math.round((annualEntitlement.value - myApprovedAnnualDays.value - myPendingAnnualDays.value) * 100) / 100)
))
const selectedRemainingAnnualDays = computed(() => {
  const employeeId = selectedLeaveEmployee.value?.id
  const usedOrPending = leaveRequests.value
    .filter(item => String(item.employeeId || '') === String(employeeId || ''))
    .filter(item => ['APPROVED', 'PENDING'].includes(item.status) && item.type === 'ANNUAL')
    .filter(item => !editingId.value || String(item.id) !== String(editingId.value))
    .filter(requestIntersectsSelectedYear)
    .reduce((total, item) => total + requestDays(item), 0)

  return Math.max(0, Math.round((selectedAnnualEntitlement.value - usedOrPending) * 100) / 100)
})

const selectedActiveRequestCount = computed(() => {
  const employeeId = selectedLeaveEmployee.value?.id

  return leaveRequests.value
    .filter(item => String(item.employeeId || '') === String(employeeId || ''))
    .filter(item => ['APPROVED', 'PENDING'].includes(item.status))
    .filter(item => !editingId.value || String(item.id) !== String(editingId.value))
    .filter(requestIntersectsSelectedYear)
    .length
})

const formEstimatedDays = computed(() => countLeaveWorkingDays(form, leaveContext.value))

watch(canViewAll, allowed => {
  if (!allowed) {
    scope.value = 'MINE'
    return
  }

  if (scope.value === 'MINE') {
    scope.value = 'ALL'
  }
}, { immediate: true })

watch(scope, () => {
  loadRequests()
})

watch(
  () => form.employeeId,
  () => {
    if (
      form.handoverEmployeeId &&
      !handoverOptions.value.some(employee => employee.value === form.handoverEmployeeId)
    ) {
      form.handoverEmployeeId = null
      form.handoverTo = ''
    }
  }
)

onMounted(() => {
  bootstrapData()
})

function createId() {
  return globalThis.crypto?.randomUUID
    ? globalThis.crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function bootstrapData() {
  loading.value = true
  await Promise.all([
    fetchProfile(),
    fetchEmployees(),
  ])
  await loadRequests()
  loading.value = false
}

async function loadRequests() {
  loading.value = true
  try {
    const { data } = await leaveRequestApi.list({
      scope: canViewAll.value && scope.value === 'ALL' ? 'ALL' : 'MINE',
    })
    leaveRequests.value = Array.isArray(data) ? data.map(normalizeRequest).filter(Boolean) : []
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
    leaveRequests.value = []
  } finally {
    loading.value = false
  }
}

async function fetchProfile() {
  try {
    const { data } = await profileApi.get()
    currentEmployee.value = data.employee || null
  } catch {
    currentEmployee.value = null
  }
}

async function fetchEmployees() {
  try {
    const { data } = await employeeApi.list({ page: 1, size: 10000 })
    employees.value = Array.isArray(data)
      ? data
      : data.content || data.items || []
  } catch {
    employees.value = []
  }
}

function normalizeRequest(request) {
  if (!request?.startDate || !request?.endDate) {
    return null
  }

  const employee = employeeById(request.employeeId) || employeeByName(request.employeeName)

  return {
    id: request.id || createId(),
    employeeId: employee?.id || request.employeeId || null,
    employeeCode: employee?.employeeCode || request.employeeCode || '',
    employeeName: employee?.fullName || request.employeeName || request.createdByName || settings.t('leave.currentEmployee'),
    departmentId: employee?.departmentId || request.departmentId || null,
    departmentName: employee?.departmentName || request.departmentName || '',
    positionId: employee?.positionId || request.positionId || null,
    positionName: employee?.positionName || request.positionName || '',
    type: request.type || 'ANNUAL',
    status: request.status || 'PENDING',
    startDate: request.startDate,
    endDate: request.endDate,
    dayUnit: request.dayUnit === 'HALF' ? 'HALF' : 'FULL',
    reason: request.reason || '',
    handoverEmployeeId: request.handoverEmployeeId || null,
    handoverTo: request.handoverTo || '',
    emergencyContact: request.emergencyContact || '',
    createdById: request.createdById || request.userId || '',
    createdByName: request.createdByName || request.employeeName || '',
    submittedAt: request.submittedAt || new Date().toISOString(),
    decidedAt: request.decidedAt || '',
    decidedBy: request.decidedBy || '',
    decisionNote: request.decisionNote || '',
  }
}

function resetForm() {
  const employee = currentEmployee.value
  Object.assign(form, {
    ...defaultForm,
    ...employeePayload(employee),
    startDate: toISO(today),
    endDate: toISO(today),
  })
}

function openCreate() {
  if (!canCreate.value) return

  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(request) {
  if (!canEditRequest(request)) return
  editingId.value = request.id
  Object.assign(form, {
    employeeId: request.employeeId || employeeByName(request.employeeName)?.id || null,
    employeeCode: request.employeeCode || '',
    employeeName: request.employeeName,
    departmentId: request.departmentId || null,
    departmentName: request.departmentName || '',
    positionId: request.positionId || null,
    positionName: request.positionName || '',
    type: request.type,
    startDate: request.startDate,
    endDate: request.endDate,
    dayUnit: request.dayUnit,
    reason: request.reason,
    handoverEmployeeId: request.handoverEmployeeId || employeeIdByHandoverLabel(request.handoverTo),
    handoverTo: request.handoverTo,
    emergencyContact: request.emergencyContact,
  })
  dialogVisible.value = true
}

async function saveRequest() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.startDate > form.endDate) {
    ElMessage.error(settings.t('leave.invalidRange'))
    return
  }

  const leaveEmployee = canApprove.value
    ? employeeById(form.employeeId)
    : currentEmployee.value

  if (!leaveEmployee) {
    ElMessage.error(settings.t('leave.employeeProfileRequired'))
    return
  }

  if (form.type === 'ANNUAL' && formEstimatedDays.value > selectedRemainingAnnualDays.value) {
    ElMessage.error(`${settings.t('leave.remainingAnnual')}: ${selectedRemainingAnnualDays.value} ${settings.t('leave.days')}`)
    return
  }

  if (selectedActiveRequestCount.value >= MAX_ACTIVE_LEAVE_REQUESTS_PER_YEAR) {
    ElMessage.error(`Mỗi năm chỉ được tạo tối đa ${MAX_ACTIVE_LEAVE_REQUESTS_PER_YEAR} đơn nghỉ đang chờ duyệt hoặc đã duyệt`)
    return
  }

  const leaveEmployeePayload = employeePayload(leaveEmployee)
  const payload = requestPayload(leaveEmployeePayload.employeeId)

  try {
    if (editingId.value) {
      await leaveRequestApi.update(editingId.value, payload)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await leaveRequestApi.create(payload)
      ElMessage.success(settings.t('leave.submitted'))
    }
    await loadRequests()
    dialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

function requestPayload(employeeId) {
  return {
    employeeId,
    type: form.type,
    startDate: form.startDate,
    endDate: form.endDate,
    dayUnit: form.dayUnit,
    reason: form.reason,
    handoverEmployeeId: form.handoverEmployeeId,
    emergencyContact: form.emergencyContact,
  }
}

async function approveRequest(request) {
  if (!canApproveRequest(request)) return

  try {
    await ElMessageBox.confirm(settings.t('leave.approveConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await leaveRequestApi.approve(request.id)
    await loadRequests()
    ElMessage.success(settings.t('leave.approved'))
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
    }
  }
}

async function rejectRequest(request) {
  if (!canApproveRequest(request)) return

  try {
    const { value } = await ElMessageBox.prompt(
      settings.t('leave.rejectReasonPrompt'),
      settings.t('leave.rejectRequest'),
      {
        confirmButtonText: settings.t('leave.reject'),
        cancelButtonText: settings.t('common.cancel'),
        inputType: 'textarea',
      }
    )
    await leaveRequestApi.reject(request.id, { note: value || '' })
    await loadRequests()
    ElMessage.success(settings.t('leave.rejected'))
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
    }
  }
}

async function cancelRequest(request) {
  if (!canCancelRequest(request)) return

  try {
    await ElMessageBox.confirm(settings.t('leave.cancelConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await leaveRequestApi.cancel(request.id)
    await loadRequests()
    ElMessage.success(settings.t('leave.cancelled'))
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
    }
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  filters.type = ''
}

function employeeIdByHandoverLabel(label) {
  if (!label) {
    return null
  }

  return employeeOptions.value.find(employee => employee.label === label)?.value || null
}

function employeeById(id) {
  if (!id) {
    return null
  }

  return employees.value.find(employee => String(employee.id) === String(id)) || null
}

function employeeByName(name) {
  if (!name) {
    return null
  }

  return employees.value.find(employee => employee.fullName === name || employeeLabel(employee) === name) || null
}

function employeePayload(employee) {
  if (!employee) {
    return {
      employeeId: null,
      employeeCode: '',
      employeeName: '',
      departmentId: null,
      departmentName: '',
      positionId: null,
      positionName: '',
    }
  }

  return {
    employeeId: employee.id,
    employeeCode: employee.employeeCode || '',
    employeeName: employee.fullName || '',
    departmentId: employee.departmentId || null,
    departmentName: employee.departmentName || '',
    positionId: employee.positionId || null,
    positionName: employee.positionName || '',
  }
}

function employeeLabel(employee) {
  const code = employee.employeeCode ? ` (${employee.employeeCode})` : ''
  const department = employee.departmentName ? ` - ${employee.departmentName}` : ''
  return `${employee.fullName || settings.t('leave.currentEmployee')}${code}${department}`
}

function employeeAnnualEntitlement(employee = {}) {
  const baseDays = WORK_CONDITIONS[0].baseDays
  const serviceYears = completeYearsBetween(employee?.joinDate, `${selectedYear.value}-12-31`)
  const workedMonths = estimateWorkedMonthsInYear(
    selectedYear.value,
    employee?.joinDate,
    employee?.terminationDate
  )

  return calculateAnnualLeaveEntitlement({ baseDays, serviceYears, workedMonths }).entitlement
}

function isOwnRequest(request) {
  if (request.employeeId && currentEmployeeId.value) {
    return String(request.employeeId) === currentEmployeeId.value
  }

  return request.createdBy === Number(currentUserKey.value)
    || request.createdById === currentUserKey.value
    || request.createdByName === currentUserName.value
    || request.employeeName === currentUserName.value
}

function requestIntersectsSelectedYear(request) {
  const startOfYear = `${selectedYear.value}-01-01`
  const endOfYear = `${selectedYear.value}-12-31`
  return isDateInRange(startOfYear, request.startDate, request.endDate)
    || isDateInRange(endOfYear, request.startDate, request.endDate)
    || request.startDate?.startsWith(`${selectedYear.value}-`)
    || request.endDate?.startsWith(`${selectedYear.value}-`)
}

function canEditRequest(request) {
  return request.status === 'PENDING' && isOwnRequest(request) && canUpdateOwn.value
}

function canCancelRequest(request) {
  return request.status === 'PENDING' && isOwnRequest(request) && canCancelOwn.value
}

function canApproveRequest(request) {
  return canApprove.value && request.status === 'PENDING' && !isOwnRequest(request)
}

function statusMeta(statusValue) {
  return leaveStatusOptions.value.find(item => item.value === statusValue) || leaveStatusOptions.value[0]
}

function typeLabel(typeValue) {
  return leaveTypeOptions.value.find(item => item.value === typeValue)?.label || typeValue
}

function requestDays(request) {
  return countLeaveWorkingDays(request, leaveContext.value)
}

function compareRequestDate(left, right) {
  return String(left.submittedAt || left.startDate).localeCompare(String(right.submittedAt || right.startDate))
}

function formatDateTime(value) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return formatISODate(value)
  }

  return `${formatISODate(toISO(date))} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <div class="leave-requests-page">
    <div class="page-card request-toolbar">
      <div class="request-filters">
        <el-input
          v-model="filters.keyword"
          :placeholder="settings.t('leave.searchRequests')"
          clearable
          class="request-search"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-input-number v-model="selectedYear" :min="2021" :max="2035" controls-position="right" />

        <el-select
          v-if="canViewAll"
          v-model="scope"
          class="filter-select"
          :placeholder="settings.t('leave.scope')"
        >
          <el-option
            v-for="option in scopeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select v-model="filters.status" clearable class="filter-select" :placeholder="settings.t('leave.statusLabel')">
          <el-option
            v-for="status in leaveStatusOptions"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>

        <el-select v-model="filters.type" clearable class="filter-select" :placeholder="settings.t('leave.type')">
          <el-option
            v-for="type in leaveTypeOptions"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>

        <el-select v-model="calendarOptions.workweek" class="filter-select" :placeholder="settings.t('leave.workweek')">
          <el-option
            v-for="option in workweekOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-button @click="resetFilters">
          <el-icon><RefreshLeft /></el-icon>
          {{ settings.t('common.reset') }}
        </el-button>
      </div>

      <el-button v-if="canCreate" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        {{ settings.t('leave.createRequest') }}
      </el-button>
    </div>

    <div class="stat-grid request-stats">
      <div class="stat-card">
        <div class="stat-icon stat-pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ pendingCount }}</h3>
          <p>{{ canApprove && scope === 'ALL' ? settings.t('leave.waitingApproval') : settings.t('leave.pendingRequests') }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-approved">
          <el-icon><Checked /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ approvedCount }}</h3>
          <p>{{ settings.t('leave.approvedRequests') }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-declined">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ declinedCount }}</h3>
          <p>{{ settings.t('leave.declinedRequests') }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-annual">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ annualEntitlement }}</h3>
          <p>{{ settings.t('leave.annualEntitlement') }}</p>
          <small>{{ settings.t('leave.remainingAnnual') }}: {{ remainingAnnualDays }}</small>
        </div>
      </div>
    </div>

    <section class="page-card request-table-card">
      <div class="request-table-header">
        <div>
          <h3>{{ settings.t('leave.requestManagement') }}</h3>
          <p>{{ settings.t(canApprove ? 'leave.managerWorkflowHint' : 'leave.employeeWorkflowHint') }}</p>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="pagedRequests"
        stripe
        style="width:100%"
        :empty-text="settings.t('leave.noRequests')"
      >
        <el-table-column prop="employeeName" :label="settings.t('leave.employeeName')" min-width="170" />
        <el-table-column prop="employeeCode" :label="settings.t('employee.employeeCode')" width="120" />
        <el-table-column prop="departmentName" :label="settings.t('employee.department')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="positionName" :label="settings.t('employee.position')" min-width="140" show-overflow-tooltip />
        <el-table-column :label="settings.t('leave.type')" min-width="150">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column :label="settings.t('leave.period')" min-width="180">
          <template #default="{ row }">
            {{ formatISODate(row.startDate) }} - {{ formatISODate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('leave.leaveDays')" width="120">
          <template #default="{ row }">{{ requestDays(row) }}</template>
        </el-table-column>
        <el-table-column :label="settings.t('leave.statusLabel')" width="130">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" effect="light" size="small">
              {{ statusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('leave.submittedAt')" min-width="150">
          <template #default="{ row }">{{ formatDateTime(row.submittedAt) }}</template>
        </el-table-column>
        <el-table-column prop="reason" :label="settings.t('leave.reason')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="handoverTo" :label="settings.t('leave.handoverTo')" min-width="180" show-overflow-tooltip />
        <el-table-column :label="settings.t('leave.approver')" min-width="150">
          <template #default="{ row }">
            <span>{{ row.decidedBy || '-' }}</span>
            <small v-if="row.decisionNote" class="decision-note">{{ row.decisionNote }}</small>
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('common.actions')" width="190" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEditRequest(row)" text type="primary" size="small" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button v-if="canCancelRequest(row)" text type="info" size="small" @click="cancelRequest(row)">
              <el-icon><Close /></el-icon>
            </el-button>
            <el-button v-if="canApproveRequest(row)" text type="success" size="small" @click="approveRequest(row)">
              <el-icon><Check /></el-icon>
            </el-button>
            <el-button v-if="canApproveRequest(row)" text type="danger" size="small" @click="rejectRequest(row)">
              <el-icon><CloseBold /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="visibleRequests.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="settings.t(editingId ? 'leave.editRequest' : 'leave.createRequest')"
      width="820px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><UserFilled /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('leave.leaveEmployee') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('leave.type') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col v-if="canApprove" :xs="24" :sm="14">
              <el-form-item :label="settings.t('leave.leaveEmployee')" prop="employeeId">
                <el-select
                  v-model="form.employeeId"
                  clearable
                  filterable
                  style="width:100%"
                  :placeholder="settings.t('leave.selectLeaveEmployee')"
                >
                  <el-option
                    v-for="employee in employeeOptions"
                    :key="employee.value"
                    :label="employee.label"
                    :value="employee.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="canApprove ? 10 : 24">
              <el-form-item :label="settings.t('leave.type')" prop="type">
                <el-select v-model="form.type" style="width:100%">
                  <el-option
                    v-for="type in leaveTypeOptions"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Calendar /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('leave.startDate') }} &mdash; {{ settings.t('leave.endDate') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('leave.dayUnit') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.startDate')" prop="startDate">
                <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.endDate')" prop="endDate">
                <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="settings.t('leave.dayUnit')">
            <el-radio-group v-model="form.dayUnit">
              <el-radio-button value="FULL">{{ settings.t('leave.fullDay') }}</el-radio-button>
              <el-radio-button value="HALF">{{ settings.t('leave.halfDay') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <div class="form-section-footer">
            <el-icon><Clock /></el-icon>
            <span>{{ settings.t('leave.estimatedDays') }}:</span>
            <strong>{{ formEstimatedDays }}</strong>
            <span v-if="form.type === 'ANNUAL'" class="annual-limit">
              {{ settings.t('leave.remainingAnnual') }}: {{ selectedRemainingAnnualDays }} / {{ selectedAnnualEntitlement }}
            </span>
          </div>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('leave.reason') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('leave.handoverTo') }} &middot; {{ settings.t('leave.emergencyContact') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('leave.reason')" prop="reason">
            <el-input v-model="form.reason" type="textarea" :rows="3" />
          </el-form-item>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.handoverTo')">
                <el-select
                  v-model="form.handoverEmployeeId"
                  clearable
                  filterable
                  style="width:100%"
                  :placeholder="settings.t('leave.selectHandoverEmployee')"
                >
                  <el-option
                    v-for="employee in handoverOptions"
                    :key="employee.value"
                    :label="employee.label"
                    :value="employee.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('leave.emergencyContact')">
                <el-input v-model="form.emergencyContact" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveRequest">{{ settings.t('leave.submitRequest') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.leave-requests-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.request-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.request-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.request-search {
  width: 280px;
}

.filter-select {
  width: 190px;
}

.request-stats {
  margin-bottom: 0;
}

.stat-pending {
  background: #FFFBEB;
  color: #D97706;
}

.stat-approved {
  background: #ECFDF5;
  color: #059669;
}

.stat-declined {
  background: #FEF2F2;
  color: #DC2626;
}

.stat-annual {
  background: #EEF2FF;
  color: #4F46E5;
}

.stat-info small {
  display: block;
  margin-top: 3px;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
}

.request-table-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-table-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.request-table-header h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  font-weight: 700;
}

.request-table-header p {
  margin-top: 4px;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-sm);
}

.decision-note {
  display: block;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
  line-height: 1.35;
  margin-top: 2px;
}

.form-two-cols {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-two-cols.single-field {
  grid-template-columns: 1fr;
}

.form-two-cols :deep(.el-date-editor),
.form-two-cols :deep(.el-select) {
  width: 100%;
}

.request-estimate {
  min-height: 44px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8FAFC;
}

.request-estimate span {
  color: var(--vx-text-secondary);
}

.request-estimate strong {
  color: var(--vx-primary);
  font-size: var(--vx-font-size-xl);
}

.annual-limit {
  margin-left: auto;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-sm);
}

@media (max-width: 860px) {
  .request-toolbar,
  .request-filters,
  .request-search,
  .filter-select {
    width: 100%;
  }

  .form-two-cols {
    grid-template-columns: 1fr;
  }

  .annual-limit {
    width: 100%;
    margin-left: 0;
  }
}
</style>
