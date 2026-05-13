<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { attendanceApi, makeupCheckoutApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const PERMISSIONS = {
  view: ['MAKEUP_CHECKOUT_VIEW', 'MAKEUP_CHECKOUT_MANAGE'],
  create: ['MAKEUP_CHECKOUT_CREATE', 'MAKEUP_CHECKOUT_MANAGE'],
  approve: ['MAKEUP_CHECKOUT_APPROVE', 'MAKEUP_CHECKOUT_MANAGE'],
  viewAll: ['MAKEUP_CHECKOUT_APPROVE', 'MAKEUP_CHECKOUT_MANAGE', 'ATTENDANCE_VIEW_ALL'],
}

const STATUS_TYPES = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'info',
}

const auth = useAuthStore()
const settings = useSettingsStore()

const requests = ref([])
const openAttendances = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const scope = ref('MINE')
const filters = reactive({ status: '' })

const defaultForm = {
  attendanceId: null,
  requestedCheckOutAt: '',
  reason: '',
}
const form = reactive({ ...defaultForm })

const rules = {
  attendanceId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  requestedCheckOutAt: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  reason: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}

const canCreate = computed(() => auth.canAccess(PERMISSIONS.create, 'any'))
const canApprove = computed(() => auth.canAccess(PERMISSIONS.approve, 'any'))
const canViewAll = computed(() => auth.canAccess(PERMISSIONS.viewAll, 'any'))

const statusOptions = computed(() => ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'].map(value => ({
  value,
  label: settings.t(`makeup.status.${value}`),
})))

const scopeOptions = computed(() => {
  const opts = [{ value: 'MINE', label: settings.t('makeup.scope.MINE') }]
  if (canViewAll.value) {
    opts.push({ value: 'ALL', label: settings.t('makeup.scope.ALL') })
  }
  return opts
})

const visibleRequests = computed(() => requests.value)

const attendanceOptions = computed(() => openAttendances.value.map(a => ({
  value: a.id,
  label: `${a.workDate} - ${settings.t('makeup.checkInAt')}: ${formatDateTime(a.checkInAt)}`,
})))

watch(canViewAll, allowed => {
  if (!allowed) scope.value = 'MINE'
}, { immediate: true })

watch([scope, () => filters.status], () => {
  loadRequests()
})

onMounted(() => {
  loadRequests()
  loadOpenAttendances()
})

async function loadRequests() {
  loading.value = true
  try {
    const params = { scope: canViewAll.value && scope.value === 'ALL' ? 'ALL' : 'MINE' }
    if (filters.status) params.status = filters.status
    const { data } = await makeupCheckoutApi.list(params)
    requests.value = Array.isArray(data) ? data : []
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
    requests.value = []
  } finally {
    loading.value = false
  }
}

async function loadOpenAttendances() {
  try {
    const { data } = await attendanceApi.my({})
    const items = Array.isArray(data) ? data : []
    openAttendances.value = items.filter(a => a.checkInAt && !a.checkOutAt)
  } catch {
    openAttendances.value = []
  }
}

function openCreate() {
  if (!canCreate.value) return
  Object.assign(form, defaultForm)
  dialogVisible.value = true
}

function onAttendanceChange(id) {
  const att = openAttendances.value.find(a => a.id === id)
  if (att && att.workDate && !form.requestedCheckOutAt) {
    form.requestedCheckOutAt = `${att.workDate}T17:30:00`
  }
}

async function submitRequest() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await makeupCheckoutApi.create({
      attendanceId: form.attendanceId,
      requestedCheckOutAt: form.requestedCheckOutAt,
      reason: form.reason,
    })
    ElMessage.success(settings.t('makeup.submitted'))
    dialogVisible.value = false
    await Promise.all([loadRequests(), loadOpenAttendances()])
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(settings.t('makeup.approveConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await makeupCheckoutApi.approve(row.id)
    ElMessage.success(settings.t('makeup.approved'))
    await loadRequests()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function reject(row) {
  try {
    const { value } = await ElMessageBox.prompt(
      settings.t('makeup.rejectPrompt'),
      settings.t('makeup.reject'),
      {
        confirmButtonText: settings.t('makeup.reject'),
        cancelButtonText: settings.t('common.cancel'),
        inputType: 'textarea',
      }
    )
    await makeupCheckoutApi.reject(row.id, { note: value || '' })
    ElMessage.success(settings.t('makeup.rejected'))
    await loadRequests()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm(settings.t('makeup.cancelConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await makeupCheckoutApi.cancel(row.id)
    ElMessage.success(settings.t('makeup.cancelled'))
    await loadRequests()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

function isOwn(row) {
  const username = auth.username
  return scope.value === 'MINE'
    || (username && row.employeeName && row.employeeName === username)
}

function canApproveRow(row) {
  return canApprove.value && row.status === 'PENDING' && !isOwn(row)
}

function canCancelRow(row) {
  return row.status === 'PENDING' && isOwn(row)
}

function statusLabel(status) {
  return settings.t(`makeup.status.${status}`)
}

function statusType(status) {
  return STATUS_TYPES[status] || 'info'
}

function formatDateTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const pad = n => String(n).padStart(2, '0')
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<template>
  <div class="makeup-page">
    <div class="page-card toolbar">
      <div>
        <h3>{{ settings.t('makeup.title') }}</h3>
        <p class="subtitle">{{ settings.t('makeup.subtitle') }}</p>
      </div>
      <el-button v-if="canCreate" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        {{ settings.t('makeup.create') }}
      </el-button>
    </div>

    <div class="page-card filter-row">
      <el-select v-if="canViewAll" v-model="scope" class="filter-select">
        <el-option v-for="opt in scopeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="filters.status" clearable class="filter-select" :placeholder="settings.t('makeup.status')">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </div>

    <section class="page-card">
      <el-table v-loading="loading" :data="visibleRequests" stripe style="width:100%" :empty-text="settings.t('makeup.empty')">
        <el-table-column prop="employeeName" :label="settings.t('leave.employeeName')" min-width="160" />
        <el-table-column prop="workDate" :label="settings.t('makeup.workDate')" width="120" />
        <el-table-column :label="settings.t('makeup.checkInAt')" min-width="160">
          <template #default="{ row }">{{ formatDateTime(row.checkInAt) }}</template>
        </el-table-column>
        <el-table-column :label="settings.t('makeup.requestedCheckOutAt')" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.requestedCheckOutAt) }}</template>
        </el-table-column>
        <el-table-column prop="reason" :label="settings.t('makeup.reason')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="settings.t('makeup.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('makeup.decisionNote')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.decisionNote || '-' }}</template>
        </el-table-column>
        <el-table-column :label="settings.t('common.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canApproveRow(row)" text type="success" size="small" @click="approve(row)">
              <el-icon><Check /></el-icon>
            </el-button>
            <el-button v-if="canApproveRow(row)" text type="danger" size="small" @click="reject(row)">
              <el-icon><CloseBold /></el-icon>
            </el-button>
            <el-button v-if="canCancelRow(row)" text type="info" size="small" @click="cancel(row)">
              <el-icon><Close /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="settings.t('makeup.create')"
      width="640px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="settings.t('makeup.attendance')" prop="attendanceId">
          <el-select
            v-model="form.attendanceId"
            filterable
            style="width:100%"
            :placeholder="attendanceOptions.length ? settings.t('makeup.selectAttendance') : settings.t('makeup.noOpenAttendance')"
            :disabled="!attendanceOptions.length"
            @change="onAttendanceChange"
          >
            <el-option
              v-for="opt in attendanceOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="settings.t('makeup.requestedCheckOutAt')" prop="requestedCheckOutAt">
          <el-date-picker
            v-model="form.requestedCheckOutAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="DD/MM/YYYY HH:mm"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item :label="settings.t('makeup.reason')" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!attendanceOptions.length" @click="submitRequest">
          {{ settings.t('makeup.create') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.makeup-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.toolbar h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  font-weight: 700;
}

.subtitle {
  margin-top: 4px;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-sm);
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  width: 220px;
}
</style>
