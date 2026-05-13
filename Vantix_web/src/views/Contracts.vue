<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { contractApi, employeeApi, positionApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'

const settings = useSettingsStore()
const auth = useAuthStore()
const router = useRouter()

const loading = ref(false)
const contracts = ref([])
const employees = ref([])
const positions = ref([])

const keyword = ref('')
const filters = reactive({
  contractType: '',
  status: '',
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const terminateVisible = ref(false)
const dialogTitleKey = ref('contract.add')
const formRef = ref(null)
const selectedContract = ref(null)
const editingId = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })

const terminateForm = reactive({
  terminatedDate: '',
  terminationReason: '',
})

const defaultForm = {
  contractCode: '',
  employeeId: null,
  positionId: null,
  contractType: 'FIXED_TERM',
  status: 'DRAFT',
  signedDate: '',
  startDate: '',
  endDate: '',
  probationMonths: null,
  baseSalary: 0,
  insuranceSalary: 0,
  responsibilityAllowance: 0,
  mealAllowance: 0,
  transportAllowance: 0,
  phoneAllowance: 0,
  otherAllowance: 0,
  standardWorkDays: 26,
  hoursPerDay: 8,
  noticePeriodDays: 30,
  note: '',
}
const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  contractCode: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  employeeId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  contractType: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  startDate: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  baseSalary: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

const contractTypeOptions = [
  { value: 'INDEFINITE', labelKey: 'contract.type.INDEFINITE' },
  { value: 'FIXED_TERM', labelKey: 'contract.type.FIXED_TERM' },
  { value: 'PROBATION', labelKey: 'contract.type.PROBATION' },
  { value: 'SEASONAL', labelKey: 'contract.type.SEASONAL' },
  { value: 'APPRENTICESHIP', labelKey: 'contract.type.APPRENTICESHIP' },
  { value: 'PART_TIME', labelKey: 'contract.type.PART_TIME' },
  { value: 'SERVICE', labelKey: 'contract.type.SERVICE' },
]

const statusOptions = [
  { value: 'DRAFT', labelKey: 'contract.status.DRAFT', type: 'info' },
  { value: 'ACTIVE', labelKey: 'contract.status.ACTIVE', type: 'success' },
  { value: 'EXPIRED', labelKey: 'contract.status.EXPIRED', type: 'warning' },
  { value: 'TERMINATED', labelKey: 'contract.status.TERMINATED', type: 'danger' },
  { value: 'LIQUIDATED', labelKey: 'contract.status.LIQUIDATED', type: 'info' },
]

const summary = computed(() => {
  const active = contracts.value.filter(c => c.status === 'ACTIVE')
  const draft = contracts.value.filter(c => c.status === 'DRAFT')
  const expiring = contracts.value.filter(isExpiringSoon)
  const activeGross = active.reduce((sum, c) => sum + Number(c.totalGrossSalary || c.baseSalary || 0), 0)

  return [
    {
      label: settings.t('contract.totalContracts'),
      value: pagination.total || contracts.value.length,
      icon: 'Document',
      tone: 'primary',
    },
    {
      label: settings.t('contract.activeContracts'),
      value: active.length,
      icon: 'CircleCheck',
      tone: 'success',
    },
    {
      label: settings.t('contract.draftContracts'),
      value: draft.length,
      icon: 'EditPen',
      tone: 'info',
    },
    {
      label: settings.t('contract.expiringSoon'),
      value: expiring.length,
      icon: 'Warning',
      tone: 'warning',
    },
    {
      label: settings.t('contract.activeGrossPayroll'),
      value: formatMoney(activeGross),
      icon: 'Money',
      tone: 'danger',
    },
  ]
})

onMounted(() => {
  fetchData()
  loadLookups()
})

async function loadLookups() {
  try {
    const [empRes, posRes] = await Promise.allSettled([
      employeeApi.list({ page: 1, size: 1000 }),
      positionApi.list(),
    ])
    if (empRes.status === 'fulfilled') {
      const data = empRes.value.data
      employees.value = Array.isArray(data) ? data : data.content || []
    }
    if (posRes.status === 'fulfilled') {
      const data = posRes.value.data
      positions.value = Array.isArray(data) ? data : data.content || []
    }
  } catch {
    // ignore lookup failures
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { keyword: keyword.value, ...pageParams(pagination) }
    if (filters.contractType) params.contractType = filters.contractType
    if (filters.status) params.status = filters.status
    const res = await contractApi.list(params)
    applyPage(res.data, contracts, pagination)
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleResetFilters() {
  keyword.value = ''
  filters.contractType = ''
  filters.status = ''
  pagination.page = 1
  fetchData()
}

function handlePageChange(p) { pagination.page = p; fetchData() }
function handleSizeChange(s) { pagination.size = s; pagination.page = 1; fetchData() }

function openCreate() {
  Object.assign(form, defaultForm)
  editingId.value = null
  dialogTitleKey.value = 'contract.add'
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    contractCode: row.contractCode,
    employeeId: row.employeeId,
    positionId: row.positionId,
    contractType: row.contractType,
    status: row.status,
    signedDate: row.signedDate,
    startDate: row.startDate,
    endDate: row.endDate,
    probationMonths: row.probationMonths,
    baseSalary: toNumber(row.baseSalary),
    insuranceSalary: toNumber(row.insuranceSalary),
    responsibilityAllowance: toNumber(row.responsibilityAllowance),
    mealAllowance: toNumber(row.mealAllowance),
    transportAllowance: toNumber(row.transportAllowance),
    phoneAllowance: toNumber(row.phoneAllowance),
    otherAllowance: toNumber(row.otherAllowance),
    standardWorkDays: row.standardWorkDays || 26,
    hoursPerDay: toNumber(row.hoursPerDay) || 8,
    noticePeriodDays: row.noticePeriodDays || 30,
    note: row.note || '',
  })
  dialogTitleKey.value = 'contract.edit'
  dialogVisible.value = true
}

function openDetail(row) {
  router.push(`/contracts/${row.id}`)
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await contractApi.update(editingId.value, form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await contractApi.create(form)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('contract.deleteConfirm')} "${row.contractCode}"?`,
      settings.t('common.confirm'), { type: 'warning' })
    await contractApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleActivate(row) {
  try {
    await ElMessageBox.confirm(settings.t('contract.activateConfirm'),
      settings.t('common.confirm'), { type: 'warning' })
    await contractApi.activate(row.id)
    ElMessage.success(settings.t('common.updated'))
    fetchData()
  } catch {
    // cancelled
  }
}

function openTerminate(row) {
  selectedContract.value = row
  terminateForm.terminatedDate = new Date().toISOString().slice(0, 10)
  terminateForm.terminationReason = ''
  terminateVisible.value = true
}

async function submitTerminate() {
  try {
    await contractApi.terminate(selectedContract.value.id, {
      terminatedDate: terminateForm.terminatedDate,
      reason: terminateForm.terminationReason,
    })
    ElMessage.success(settings.t('common.updated'))
    terminateVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

function typeLabel(v) {
  const o = contractTypeOptions.find(t => t.value === v)
  return o ? settings.t(o.labelKey) : v
}

function statusTag(v) {
  return statusOptions.find(s => s.value === v) || { labelKey: '', type: 'info' }
}

function statusLabel(v) {
  const o = statusTag(v)
  return o.labelKey ? settings.t(o.labelKey) : v || '-'
}

function empName(id) {
  const e = employees.value.find(x => x.id === id)
  return e ? `${e.employeeCode} - ${e.fullName}` : '-'
}

function posName(id) {
  return positions.value.find(p => p.id === id)?.name || '-'
}

function formatMoney(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('vi-VN').format(Number(v))
}

function toNumber(v) {
  if (v == null || v === '') return 0
  return Number(v)
}

function displayValue(v) { return v || '-' }

function daysUntil(dateValue) {
  if (!dateValue) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const date = new Date(dateValue)
  date.setHours(0, 0, 0, 0)
  return Math.ceil((date - today) / 86400000)
}

function isExpiringSoon(row) {
  const days = daysUntil(row.endDate)
  return row.status === 'ACTIVE' && days != null && days >= 0 && days <= 30
}

const endDateDisabled = computed(() => form.contractType === 'INDEFINITE')
</script>

<template>
  <div class="page-card">
    <div class="contract-summary">
      <div v-for="item in summary" :key="item.label" class="summary-item" :class="`summary-${item.tone}`">
        <div class="summary-icon">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="summary-content">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </div>

    <div class="table-toolbar">
      <div class="contract-filters">
        <el-input
          v-model="keyword"
          :placeholder="settings.t('contract.search')"
          clearable
          class="contract-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="filters.contractType"
          clearable
          class="contract-filter"
          :placeholder="settings.t('contract.contractType')"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="t in contractTypeOptions" :key="t.value" :label="settings.t(t.labelKey)" :value="t.value" />
        </el-select>

        <el-select
          v-model="filters.status"
          clearable
          class="contract-filter"
          :placeholder="settings.t('contract.status')"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="s in statusOptions" :key="s.value" :label="settings.t(s.labelKey)" :value="s.value" />
        </el-select>

        <el-button @click="handleResetFilters">
          <el-icon><RefreshLeft /></el-icon>
          {{ settings.t('common.reset') }}
        </el-button>
      </div>

      <div style="display:flex;gap:8px">
        <el-button v-if="auth.hasPermission('CONTRACT_CREATE')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> {{ settings.t('contract.add') }}
        </el-button>
      </div>
    </div>

    <el-table :data="contracts" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="contractCode" :label="settings.t('contract.contractCode')" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">{{ row.contractCode }}</el-button>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.employee')" min-width="200">
        <template #default="{ row }">
          <div>
            <div style="font-weight:600">{{ row.employeeName || '-' }}</div>
            <div style="color:var(--vx-text-secondary);font-size:12px">{{ row.employeeCode }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.position')" min-width="130">
        <template #default="{ row }">{{ row.positionName || posName(row.positionId) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.contractType')" width="160">
        <template #default="{ row }">{{ typeLabel(row.contractType) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.startDate')" width="115" prop="startDate" />
      <el-table-column :label="settings.t('contract.endDate')" width="115">
        <template #default="{ row }">
          <div class="contract-end-date">
            <span>{{ row.endDate || '-' }}</span>
            <el-tag v-if="isExpiringSoon(row)" type="warning" size="small" effect="light">
              {{ settings.t('contract.expiringSoon') }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.baseSalary')" width="140" align="right">
        <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('contract.status')" width="130">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type" size="small" effect="light">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('common.actions')" width="210" fixed="right">
        <template #default="{ row }">
          <el-button text type="info" size="small" @click="openDetail(row)">
            <el-icon><View /></el-icon>
          </el-button>
          <el-button
            v-if="auth.hasPermission('CONTRACT_UPDATE') && row.status === 'DRAFT'"
            text
            type="success"
            size="small"
            @click="handleActivate(row)"
          >
            <el-icon><CircleCheck /></el-icon>
          </el-button>
          <el-button v-if="auth.hasPermission('CONTRACT_UPDATE')" text type="primary" size="small" @click="openEdit(row)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button
            v-if="auth.hasPermission('CONTRACT_UPDATE') && row.status === 'ACTIVE'"
            text
            type="warning"
            size="small"
            @click="openTerminate(row)"
          >
            <el-icon><CloseBold /></el-icon>
          </el-button>
          <el-button v-if="auth.hasPermission('CONTRACT_DELETE')" text type="danger" size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- Create / edit dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="settings.t(dialogTitleKey)"
      width="860px"
      destroy-on-close
      class="vx-dialog contract-dialog"
      align-center
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('contract.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('contract.contractCode') }} · {{ settings.t('contract.employee') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.contractCode')" prop="contractCode">
                <el-input v-model="form.contractCode" :disabled="!!editingId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.employee')" prop="employeeId">
                <el-select v-model="form.employeeId" filterable style="width:100%" :placeholder="settings.t('contract.selectEmployee')">
                  <el-option v-for="e in employees" :key="e.id" :label="`${e.employeeCode} - ${e.fullName}`" :value="e.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.position')">
                <el-select v-model="form.positionId" clearable style="width:100%" :placeholder="settings.t('contract.selectPosition')">
                  <el-option v-for="p in positions" :key="p.id" :label="p.name" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.contractType')" prop="contractType">
                <el-select v-model="form.contractType" style="width:100%">
                  <el-option v-for="t in contractTypeOptions" :key="t.value" :label="settings.t(t.labelKey)" :value="t.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.status')">
                <el-select v-model="form.status" style="width:100%">
                  <el-option v-for="s in statusOptions" :key="s.value" :label="settings.t(s.labelKey)" :value="s.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.signedDate')">
                <el-date-picker v-model="form.signedDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.startDate')" prop="startDate">
                <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.endDate')">
                <el-date-picker v-model="form.endDate" :disabled="endDateDisabled" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.probationMonths')">
                <el-input-number v-model="form.probationMonths" :min="0" :max="6" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Money /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('contract.salaryInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('contract.baseSalary') }} · {{ settings.t('contract.insuranceSalary') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.baseSalary')" prop="baseSalary">
                <el-input-number v-model="form.baseSalary" :min="0" :step="100000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.insuranceSalary')">
                <el-input-number v-model="form.insuranceSalary" :min="0" :step="100000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.responsibilityAllowance')">
                <el-input-number v-model="form.responsibilityAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.mealAllowance')">
                <el-input-number v-model="form.mealAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.transportAllowance')">
                <el-input-number v-model="form.transportAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.phoneAllowance')">
                <el-input-number v-model="form.phoneAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('contract.otherAllowance')">
                <el-input-number v-model="form.otherAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Clock /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('contract.workTerms') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('contract.standardWorkDays') }} · {{ settings.t('contract.hoursPerDay') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.standardWorkDays')">
                <el-input-number v-model="form.standardWorkDays" :min="1" :max="31" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.hoursPerDay')">
                <el-input-number v-model="form.hoursPerDay" :min="1" :max="24" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.noticePeriodDays')">
                <el-input-number v-model="form.noticePeriodDays" :min="0" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="settings.t('contract.note')">
                <el-input v-model="form.note" type="textarea" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Detail drawer -->
    <el-drawer v-model="detailVisible" :title="settings.t('contract.details')" size="560px" destroy-on-close>
      <div v-if="selectedContract" class="contract-detail">
        <div class="detail-header">
          <el-icon class="detail-icon"><Document /></el-icon>
          <div class="detail-title">
            <h3>{{ selectedContract.contractCode }}</h3>
            <p>{{ empName(selectedContract.employeeId) }}</p>
          </div>
          <el-tag :type="statusTag(selectedContract.status).type" effect="light">
            {{ statusLabel(selectedContract.status) }}
          </el-tag>
        </div>

        <h4 class="detail-section-title">{{ settings.t('contract.basicInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('contract.contractType')">{{ typeLabel(selectedContract.contractType) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.position')">{{ selectedContract.positionName || posName(selectedContract.positionId) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.signedDate')">{{ displayValue(selectedContract.signedDate) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.startDate')">{{ displayValue(selectedContract.startDate) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.endDate')">{{ displayValue(selectedContract.endDate) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.probationMonths')">{{ displayValue(selectedContract.probationMonths) }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('contract.salaryInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('contract.baseSalary')">{{ formatMoney(selectedContract.baseSalary) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.insuranceSalary')">{{ formatMoney(selectedContract.insuranceSalary) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.responsibilityAllowance')">{{ formatMoney(selectedContract.responsibilityAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.mealAllowance')">{{ formatMoney(selectedContract.mealAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.transportAllowance')">{{ formatMoney(selectedContract.transportAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.phoneAllowance')">{{ formatMoney(selectedContract.phoneAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.otherAllowance')">{{ formatMoney(selectedContract.otherAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.totalAllowance')">{{ formatMoney(selectedContract.totalAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.totalGrossSalary')">{{ formatMoney(selectedContract.totalGrossSalary) }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('contract.workTerms') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('contract.standardWorkDays')">{{ displayValue(selectedContract.standardWorkDays) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.hoursPerDay')">{{ displayValue(selectedContract.hoursPerDay) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.noticePeriodDays')">{{ displayValue(selectedContract.noticePeriodDays) }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedContract.terminatedDate" :label="settings.t('contract.terminatedDate')">
            {{ selectedContract.terminatedDate }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedContract.terminationReason" :label="settings.t('contract.terminationReason')">
            {{ selectedContract.terminationReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedContract.note" :label="settings.t('contract.note')">
            {{ selectedContract.note }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- Terminate dialog -->
    <el-dialog v-model="terminateVisible" :title="settings.t('contract.terminateTitle')" width="480px" destroy-on-close class="vx-dialog" align-center>
      <el-form label-position="top">
        <el-form-item :label="settings.t('contract.terminatedDate')">
          <el-date-picker v-model="terminateForm.terminatedDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item :label="settings.t('contract.terminationReason')">
          <el-input v-model="terminateForm.terminationReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="terminateVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="danger" @click="submitTerminate">{{ settings.t('contract.terminate') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.contract-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(140px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 14px;
  background: var(--vx-card-bg);
}

.summary-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.summary-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-content span {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-content strong {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  line-height: 1.2;
}

.summary-primary .summary-icon {
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  color: var(--vx-primary);
}

.summary-success .summary-icon {
  background: color-mix(in srgb, var(--vx-success) 12%, transparent);
  color: var(--vx-success);
}

.summary-info .summary-icon {
  background: color-mix(in srgb, var(--vx-info) 12%, transparent);
  color: var(--vx-info);
}

.summary-warning .summary-icon {
  background: color-mix(in srgb, var(--vx-warning) 15%, transparent);
  color: var(--vx-warning);
}

.summary-danger .summary-icon {
  background: color-mix(in srgb, var(--vx-danger) 10%, transparent);
  color: var(--vx-danger);
}

.contract-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.contract-search {
  width: 260px;
}

.contract-filter {
  width: 170px;
}

.contract-end-date {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.contract-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
}

.detail-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  color: var(--vx-primary);
  font-size: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-title {
  flex: 1;
  min-width: 0;
}

.detail-title h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  margin-bottom: 2px;
}

.detail-title p {
  color: var(--vx-text-secondary);
}

.detail-section-title {
  color: var(--vx-text);
  font-size: var(--vx-font-size-md);
  font-weight: 700;
  margin-top: 4px;
}

.form-section {
  background: var(--vx-card-bg);
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 14px 16px 2px;
  margin-bottom: 12px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.form-section-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: color-mix(in srgb, var(--vx-primary) 12%, transparent);
  color: var(--vx-primary);
  font-size: 16px;
  flex-shrink: 0;
}

.form-section-title {
  color: var(--vx-text);
  font-size: var(--vx-font-size-sm);
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.form-section-subtitle {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-2xs);
  margin: 2px 0 0;
}

:deep(.contract-dialog .el-dialog__body) {
  max-height: min(70vh, 680px);
  overflow-y: auto;
}

:deep(.contract-dialog .el-form-item) {
  margin-bottom: 12px;
}

:deep(.contract-dialog .el-form-item__label) {
  padding-bottom: 3px;
  font-weight: 600;
}

@media (max-width: 760px) {
  .contract-summary {
    grid-template-columns: 1fr;
  }

  .contract-filters,
  .contract-search,
  .contract-filter {
    width: 100%;
  }

  .form-section {
    padding: 12px 12px 0;
  }
}
</style>
