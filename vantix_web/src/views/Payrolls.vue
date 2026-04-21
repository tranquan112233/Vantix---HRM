<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { payrollApi, departmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const auth = useAuthStore()

const periodsLoading = ref(false)
const rowsLoading = ref(false)
const periods = ref([])
const rows = ref([])
const departments = ref([])
const selectedPeriodId = ref(null)
const keyword = ref('')
const filterDepartmentId = ref(null)
const filterYear = ref(null)

const periodDialogVisible = ref(false)
const editingPeriodId = ref(null)
const periodFormRef = ref(null)
const periodDefaultForm = {
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  standardWorkDays: 26,
  startDate: '',
  endDate: '',
  note: '',
}
const periodForm = reactive({ ...periodDefaultForm })
const periodRules = computed(() => ({
  year: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  month: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

const adjustVisible = ref(false)
const adjustRow = ref(null)
const adjustForm = reactive({
  actualWorkDays: 0,
  paidLeaveDays: 0,
  unpaidLeaveDays: 0,
  overtimeHoursWeekday: 0,
  overtimeHoursWeekend: 0,
  overtimeHoursHoliday: 0,
  overtimeHoursNight: 0,
  dependents: 0,
  bonus: 0,
  commission: 0,
  otherDeductions: 0,
  responsibilityAllowance: 0,
  mealAllowance: 0,
  transportAllowance: 0,
  phoneAllowance: 0,
  otherAllowance: 0,
  note: '',
})

const detailVisible = ref(false)
const selectedRow = ref(null)

const periodStatusOptions = [
  { value: 'DRAFT', labelKey: 'payroll.status.DRAFT', type: 'info' },
  { value: 'CALCULATED', labelKey: 'payroll.status.CALCULATED', type: 'warning' },
  { value: 'APPROVED', labelKey: 'payroll.status.APPROVED', type: 'success' },
  { value: 'PAID', labelKey: 'payroll.status.PAID', type: 'success' },
  { value: 'CANCELLED', labelKey: 'payroll.status.CANCELLED', type: 'danger' },
]

const selectedPeriod = computed(() => periods.value.find(p => p.id === selectedPeriodId.value))

const payrollSummary = computed(() => {
  const totals = rows.value.reduce((acc, row) => {
    acc.gross += Number(row.grossIncome || 0)
    acc.net += Number(row.netIncome || 0)
    acc.employeeInsurance += Number(row.totalEmployeeInsurance || 0)
    acc.employerCost += Number(row.totalEmployerCost || 0)
    return acc
  }, { gross: 0, net: 0, employeeInsurance: 0, employerCost: 0 })

  return [
    {
      label: settings.t('payroll.employeeCount'),
      value: rows.value.length,
      icon: 'User',
      tone: 'primary',
    },
    {
      label: settings.t('payroll.totalGross'),
      value: formatMoney(totals.gross),
      icon: 'Wallet',
      tone: 'info',
    },
    {
      label: settings.t('payroll.totalInsurance'),
      value: formatMoney(totals.employeeInsurance),
      icon: 'Umbrella',
      tone: 'warning',
    },
    {
      label: settings.t('payroll.totalNet'),
      value: formatMoney(totals.net),
      icon: 'Money',
      tone: 'success',
    },
    {
      label: settings.t('payroll.totalEmployerCost'),
      value: formatMoney(totals.employerCost),
      icon: 'OfficeBuilding',
      tone: 'danger',
    },
  ]
})

const payrollReportTitle = computed(() => {
  if (!selectedPeriod.value) return settings.t('payroll.reportTitle')
  return `${settings.t('payroll.reportTitle')} ${selectedPeriod.value.year}-${String(selectedPeriod.value.month).padStart(2, '0')}`
})

const payrollReportFilename = computed(() => {
  if (!selectedPeriod.value) return 'payroll-report'
  return `payroll-${selectedPeriod.value.year}-${String(selectedPeriod.value.month).padStart(2, '0')}`
})

const payrollReportRows = computed(() => {
  const mappedRows = rows.value.map(row => ({
    ...row,
    statusLabel: periodStatusLabel(row.status),
  }))

  if (!mappedRows.length) return []

  const totals = rows.value.reduce((acc, row) => {
    acc.actualWorkDays += Number(row.actualWorkDays || 0)
    acc.paidLeaveDays += Number(row.paidLeaveDays || 0)
    acc.unpaidLeaveDays += Number(row.unpaidLeaveDays || 0)
    acc.baseSalary += Number(row.baseSalary || 0)
    acc.insuranceSalary += Number(row.insuranceSalary || 0)
    acc.workingDaysSalary += Number(row.workingDaysSalary || 0)
    acc.overtimePay += Number(row.overtimePay || 0)
    acc.totalAllowance += Number(row.totalAllowance || 0)
    acc.bonus += Number(row.bonus || 0)
    acc.commission += Number(row.commission || 0)
    acc.grossIncome += Number(row.grossIncome || 0)
    acc.totalEmployeeInsurance += Number(row.totalEmployeeInsurance || 0)
    acc.personalIncomeTax += Number(row.personalIncomeTax || 0)
    acc.otherDeductions += Number(row.otherDeductions || 0)
    acc.netIncome += Number(row.netIncome || 0)
    acc.employerInsurance += Number(row.employerInsurance || 0)
    acc.totalEmployerCost += Number(row.totalEmployerCost || 0)
    return acc
  }, {
    actualWorkDays: 0,
    paidLeaveDays: 0,
    unpaidLeaveDays: 0,
    baseSalary: 0,
    insuranceSalary: 0,
    workingDaysSalary: 0,
    overtimePay: 0,
    totalAllowance: 0,
    bonus: 0,
    commission: 0,
    grossIncome: 0,
    totalEmployeeInsurance: 0,
    personalIncomeTax: 0,
    otherDeductions: 0,
    netIncome: 0,
    employerInsurance: 0,
    totalEmployerCost: 0,
  })

  return [
    ...mappedRows,
    {
      employeeCode: '',
      employeeName: settings.t('payroll.totalRow'),
      departmentName: '',
      positionName: '',
      contractCode: '',
      statusLabel: '',
      ...totals,
    },
  ]
})

const payrollReportColumns = computed(() => [
  { prop: 'employeeCode', label: settings.t('employee.employeeCode') },
  { prop: 'employeeName', label: settings.t('employee.fullName') },
  { prop: 'departmentName', label: settings.t('payroll.department') },
  { prop: 'positionName', label: settings.t('contract.position') },
  { prop: 'contractCode', label: settings.t('payroll.contract') },
  { prop: 'actualWorkDays', label: settings.t('payroll.actualWorkDays') },
  { prop: 'paidLeaveDays', label: settings.t('payroll.paidLeaveDays') },
  { prop: 'unpaidLeaveDays', label: settings.t('payroll.unpaidLeaveDays') },
  { prop: 'baseSalary', label: settings.t('payroll.baseSalary'), format: row => formatMoney(row.baseSalary) },
  { prop: 'insuranceSalary', label: settings.t('payroll.insuranceSalary'), format: row => formatMoney(row.insuranceSalary) },
  { prop: 'workingDaysSalary', label: settings.t('payroll.workingDaysSalary'), format: row => formatMoney(row.workingDaysSalary) },
  { prop: 'overtimePay', label: settings.t('payroll.overtimePay'), format: row => formatMoney(row.overtimePay) },
  { prop: 'totalAllowance', label: settings.t('payroll.allowances'), format: row => formatMoney(row.totalAllowance) },
  { prop: 'bonus', label: settings.t('payroll.bonus'), format: row => formatMoney(row.bonus) },
  { prop: 'commission', label: settings.t('payroll.commission'), format: row => formatMoney(row.commission) },
  { prop: 'grossIncome', label: settings.t('payroll.grossIncome'), format: row => formatMoney(row.grossIncome) },
  { prop: 'totalEmployeeInsurance', label: settings.t('payroll.totalEmployeeInsurance'), format: row => formatMoney(row.totalEmployeeInsurance) },
  { prop: 'personalIncomeTax', label: settings.t('payroll.personalIncomeTax'), format: row => formatMoney(row.personalIncomeTax) },
  { prop: 'otherDeductions', label: settings.t('payroll.otherDeductions'), format: row => formatMoney(row.otherDeductions) },
  { prop: 'netIncome', label: settings.t('payroll.netIncome'), format: row => formatMoney(row.netIncome) },
  { prop: 'employerInsurance', label: settings.t('payroll.employerInsurance'), format: row => formatMoney(row.employerInsurance) },
  { prop: 'totalEmployerCost', label: settings.t('payroll.totalEmployerCost'), format: row => formatMoney(row.totalEmployerCost) },
  { prop: 'statusLabel', label: settings.t('payroll.status') },
])

const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)
const yearOptions = computed(() => {
  const current = new Date().getFullYear()
  return Array.from({ length: 6 }, (_, i) => current - 3 + i)
})

onMounted(() => {
  fetchPeriods()
  loadDepartments()
})

async function loadDepartments() {
  if (!auth.hasPermission('DEPARTMENT_VIEW')) return
  try {
    const res = await departmentApi.list()
    departments.value = Array.isArray(res.data) ? res.data : res.data.content || []
  } catch {
    departments.value = []
  }
}

async function fetchPeriods() {
  periodsLoading.value = true
  try {
    const params = {}
    if (filterYear.value) params.year = filterYear.value
    const res = await payrollApi.listPeriods(params)
    periods.value = Array.isArray(res.data) ? res.data : []
    if (!selectedPeriodId.value && periods.value.length) {
      selectedPeriodId.value = periods.value[0].id
      fetchRows()
    } else if (selectedPeriodId.value && !periods.value.some(p => p.id === selectedPeriodId.value)) {
      selectedPeriodId.value = periods.value[0]?.id ?? null
      if (selectedPeriodId.value) fetchRows()
      else rows.value = []
    }
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    periodsLoading.value = false
  }
}

async function fetchRows() {
  if (!selectedPeriodId.value) {
    rows.value = []
    return
  }
  rowsLoading.value = true
  try {
    const params = { periodId: selectedPeriodId.value }
    if (keyword.value) params.keyword = keyword.value
    if (filterDepartmentId.value) params.departmentId = filterDepartmentId.value
    const res = await payrollApi.list(params)
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    rowsLoading.value = false
  }
}

function selectPeriod(p) {
  selectedPeriodId.value = p.id
  fetchRows()
}

function openCreatePeriod() {
  Object.assign(periodForm, periodDefaultForm)
  editingPeriodId.value = null
  periodDialogVisible.value = true
}

function openEditPeriod(p) {
  editingPeriodId.value = p.id
  Object.assign(periodForm, {
    year: p.year,
    month: p.month,
    standardWorkDays: p.standardWorkDays || 26,
    startDate: p.startDate || '',
    endDate: p.endDate || '',
    note: p.note || '',
  })
  periodDialogVisible.value = true
}

async function savePeriod() {
  const valid = await periodFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingPeriodId.value) {
      await payrollApi.updatePeriod(editingPeriodId.value, periodForm)
      ElMessage.success(settings.t('common.updated'))
    } else {
      const res = await payrollApi.createPeriod(periodForm)
      ElMessage.success(settings.t('common.created'))
      if (res.data?.id) selectedPeriodId.value = res.data.id
    }
    periodDialogVisible.value = false
    fetchPeriods()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function deletePeriod(p) {
  try {
    await ElMessageBox.confirm(
      settings.t('payroll.deletePeriodConfirm'),
      settings.t('common.confirm'),
      { type: 'warning' },
    )
    await payrollApi.deletePeriod(p.id)
    ElMessage.success(settings.t('common.deleted'))
    if (selectedPeriodId.value === p.id) selectedPeriodId.value = null
    fetchPeriods()
  } catch {
    // cancelled
  }
}

async function generateRows() {
  if (!selectedPeriodId.value) return
  try {
    await ElMessageBox.confirm(settings.t('payroll.generateConfirm'),
      settings.t('common.confirm'), { type: 'warning' })
    await payrollApi.generate(selectedPeriodId.value)
    ElMessage.success(settings.t('common.updated'))
    fetchPeriods()
    fetchRows()
  } catch {
    // cancelled
  }
}

async function recalculate() {
  if (!selectedPeriodId.value) return
  try {
    await ElMessageBox.confirm(settings.t('payroll.recalculateConfirm'),
      settings.t('common.confirm'), { type: 'warning' })
    await payrollApi.recalculate(selectedPeriodId.value)
    ElMessage.success(settings.t('common.updated'))
    fetchPeriods()
    fetchRows()
  } catch {
    // cancelled
  }
}

async function approve() {
  if (!selectedPeriodId.value) return
  try {
    await ElMessageBox.confirm(settings.t('payroll.approveConfirm'),
      settings.t('common.confirm'), { type: 'warning' })
    await payrollApi.approve(selectedPeriodId.value)
    ElMessage.success(settings.t('common.updated'))
    fetchPeriods()
    fetchRows()
  } catch {
    // cancelled
  }
}

async function markPaid() {
  if (!selectedPeriodId.value) return
  try {
    await ElMessageBox.confirm(settings.t('payroll.markPaidConfirm'),
      settings.t('common.confirm'), { type: 'warning' })
    await payrollApi.markPaid(selectedPeriodId.value)
    ElMessage.success(settings.t('common.updated'))
    fetchPeriods()
    fetchRows()
  } catch {
    // cancelled
  }
}

function openAdjust(row) {
  adjustRow.value = row
  Object.assign(adjustForm, {
    actualWorkDays: toNumber(row.actualWorkDays),
    paidLeaveDays: toNumber(row.paidLeaveDays),
    unpaidLeaveDays: toNumber(row.unpaidLeaveDays),
    overtimeHoursWeekday: toNumber(row.overtimeHoursWeekday),
    overtimeHoursWeekend: toNumber(row.overtimeHoursWeekend),
    overtimeHoursHoliday: toNumber(row.overtimeHoursHoliday),
    overtimeHoursNight: toNumber(row.overtimeHoursNight),
    dependents: row.dependents || 0,
    bonus: toNumber(row.bonus),
    commission: toNumber(row.commission),
    otherDeductions: toNumber(row.otherDeductions),
    responsibilityAllowance: toNumber(row.responsibilityAllowance),
    mealAllowance: toNumber(row.mealAllowance),
    transportAllowance: toNumber(row.transportAllowance),
    phoneAllowance: toNumber(row.phoneAllowance),
    otherAllowance: toNumber(row.otherAllowance),
    note: row.note || '',
  })
  adjustVisible.value = true
}

async function submitAdjust() {
  try {
    await payrollApi.adjust(adjustRow.value.id, adjustForm)
    ElMessage.success(settings.t('common.updated'))
    adjustVisible.value = false
    fetchRows()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

function openDetail(row) {
  selectedRow.value = row
  detailVisible.value = true
}

function periodStatusTag(v) {
  return periodStatusOptions.find(s => s.value === v) || { labelKey: '', type: 'info' }
}

function periodStatusLabel(v) {
  const o = periodStatusTag(v)
  return o.labelKey ? settings.t(o.labelKey) : v || '-'
}

function formatMoney(v) {
  if (v == null) return '-'
  return new Intl.NumberFormat('vi-VN').format(Number(v))
}

function toNumber(v) {
  if (v == null || v === '') return 0
  return Number(v)
}

function displayValue(v) {
  return v || '-'
}

function canGenerate(p) {
  return p && (p.status === 'DRAFT' || p.status === 'CALCULATED')
}

function canApprove(p) {
  return p && p.status === 'CALCULATED'
}

function canMarkPaid(p) {
  return p && p.status === 'APPROVED'
}

function canAdjust(p) {
  return p && p.status !== 'PAID' && p.status !== 'CANCELLED'
}
</script>

<template>
  <div class="payroll-layout">
    <!-- Left: period list -->
    <aside class="period-panel page-card">
      <div class="period-header">
        <h3 class="period-title">{{ settings.t('payroll.periods') }}</h3>
        <el-button
          v-if="auth.hasPermission('PAYROLL_CREATE') || auth.hasPermission('PAYROLL_MANAGE')"
          type="primary"
          size="small"
          @click="openCreatePeriod"
        >
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>

      <el-select
        v-model="filterYear"
        clearable
        :placeholder="settings.t('payroll.year')"
        class="period-year-filter"
        @change="fetchPeriods"
        @clear="fetchPeriods"
      >
        <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
      </el-select>

      <div v-loading="periodsLoading" class="period-list">
        <div
          v-for="p in periods"
          :key="p.id"
          class="period-item"
          :class="{ active: selectedPeriodId === p.id }"
          @click="selectPeriod(p)"
        >
          <div class="period-item-main">
            <strong>{{ p.year }}-{{ String(p.month).padStart(2, '0') }}</strong>
            <el-tag :type="periodStatusTag(p.status).type" size="small" effect="light">
              {{ periodStatusLabel(p.status) }}
            </el-tag>
          </div>
          <div class="period-item-actions">
            <el-button
              v-if="auth.hasPermission('PAYROLL_UPDATE') || auth.hasPermission('PAYROLL_MANAGE')"
              text
              size="small"
              @click.stop="openEditPeriod(p)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-if="auth.hasPermission('PAYROLL_DELETE') || auth.hasPermission('PAYROLL_MANAGE')"
              text
              type="danger"
              size="small"
              @click.stop="deletePeriod(p)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="!periods.length && !periodsLoading" class="period-empty">
          {{ settings.t('payroll.noPeriod') }}
        </div>
      </div>
    </aside>

    <!-- Right: payroll rows -->
    <section class="rows-panel page-card">
      <div v-if="!selectedPeriod" class="rows-empty">
        {{ settings.t('payroll.selectPeriod') }}
      </div>
      <template v-else>
        <div class="rows-header">
          <div class="rows-header-title">
            <h2>{{ selectedPeriod.year }}-{{ String(selectedPeriod.month).padStart(2, '0') }}</h2>
            <el-tag :type="periodStatusTag(selectedPeriod.status).type" effect="light">
              {{ periodStatusLabel(selectedPeriod.status) }}
            </el-tag>
            <span class="period-range">
              {{ displayValue(selectedPeriod.startDate) }} - {{ displayValue(selectedPeriod.endDate) }}
            </span>
          </div>
          <div class="rows-header-actions">
            <ExportActions
              :filename="payrollReportFilename"
              :title="payrollReportTitle"
              :columns="payrollReportColumns"
              :rows="payrollReportRows"
            />
            <el-button
              v-if="(auth.hasPermission('PAYROLL_CREATE') || auth.hasPermission('PAYROLL_MANAGE')) && canGenerate(selectedPeriod)"
              type="primary"
              @click="generateRows"
            >
              <el-icon><MagicStick /></el-icon>
              {{ settings.t('payroll.generate') }}
            </el-button>
            <el-button
              v-if="(auth.hasPermission('PAYROLL_UPDATE') || auth.hasPermission('PAYROLL_MANAGE')) && canAdjust(selectedPeriod)"
              @click="recalculate"
            >
              <el-icon><Refresh /></el-icon>
              {{ settings.t('payroll.recalculate') }}
            </el-button>
            <el-button
              v-if="(auth.hasPermission('PAYROLL_APPROVE') || auth.hasPermission('PAYROLL_MANAGE')) && canApprove(selectedPeriod)"
              type="success"
              @click="approve"
            >
              <el-icon><CircleCheck /></el-icon>
              {{ settings.t('payroll.approve') }}
            </el-button>
            <el-button
              v-if="(auth.hasPermission('PAYROLL_PAY') || auth.hasPermission('PAYROLL_MANAGE')) && canMarkPaid(selectedPeriod)"
              type="warning"
              @click="markPaid"
            >
              <el-icon><Money /></el-icon>
              {{ settings.t('payroll.markPaid') }}
            </el-button>
          </div>
        </div>

        <div class="payroll-summary">
          <div v-for="item in payrollSummary" :key="item.label" class="summary-item" :class="`summary-${item.tone}`">
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

        <div class="rows-filters">
          <el-input
            v-model="keyword"
            :placeholder="settings.t('payroll.searchEmployee')"
            clearable
            class="rows-search"
            @keyup.enter="fetchRows"
            @clear="fetchRows"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select
            v-model="filterDepartmentId"
            clearable
            filterable
            class="rows-filter"
            :placeholder="settings.t('payroll.department')"
            @change="fetchRows"
            @clear="fetchRows"
          >
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </div>

        <el-table :data="rows" v-loading="rowsLoading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
          <el-table-column prop="employeeCode" :label="settings.t('payroll.employee')" width="130" fixed="left" />
          <el-table-column :label="settings.t('payroll.employee')" min-width="170" fixed="left">
            <template #default="{ row }">
              <div>
                <div style="font-weight:600">{{ row.employeeName }}</div>
                <div style="color:var(--vx-text-secondary);font-size:12px">{{ row.departmentName || '-' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.actualWorkDays')" width="110" align="right">
            <template #default="{ row }">{{ row.actualWorkDays ?? '-' }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.contract')" width="135">
            <template #default="{ row }">{{ row.contractCode || '-' }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.grossIncome')" width="130" align="right">
            <template #default="{ row }">{{ formatMoney(row.grossIncome) }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.totalEmployeeInsurance')" width="140" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalEmployeeInsurance) }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.personalIncomeTax')" width="130" align="right">
            <template #default="{ row }">{{ formatMoney(row.personalIncomeTax) }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.netIncome')" width="140" align="right">
            <template #default="{ row }">
              <strong>{{ formatMoney(row.netIncome) }}</strong>
            </template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.totalEmployerCost')" width="150" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalEmployerCost) }}</template>
          </el-table-column>
          <el-table-column :label="settings.t('payroll.status')" width="115">
            <template #default="{ row }">
              <el-tag :type="periodStatusTag(row.status).type" size="small" effect="light">
                {{ periodStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="settings.t('common.actions')" width="130" fixed="right">
            <template #default="{ row }">
              <el-button text type="info" size="small" @click="openDetail(row)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button
                v-if="(auth.hasPermission('PAYROLL_UPDATE') || auth.hasPermission('PAYROLL_MANAGE')) && canAdjust(selectedPeriod)"
                text
                type="primary"
                size="small"
                @click="openAdjust(row)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </section>

    <!-- Period create/edit dialog -->
    <el-dialog
      v-model="periodDialogVisible"
      :title="editingPeriodId ? settings.t('payroll.editPeriod') : settings.t('payroll.addPeriod')"
      width="520px"
      destroy-on-close
    >
      <el-form ref="periodFormRef" :model="periodForm" :rules="periodRules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="settings.t('payroll.year')" prop="year">
              <el-select v-model="periodForm.year" style="width:100%">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="settings.t('payroll.month')" prop="month">
              <el-select v-model="periodForm.month" style="width:100%">
                <el-option v-for="m in monthOptions" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="settings.t('payroll.startDate')">
              <el-date-picker v-model="periodForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="settings.t('payroll.endDate')">
              <el-date-picker v-model="periodForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="settings.t('payroll.standardWorkDays')">
              <el-input-number v-model="periodForm.standardWorkDays" :min="1" :max="31" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="settings.t('payroll.note')">
              <el-input v-model="periodForm.note" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="periodDialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="savePeriod">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Adjust dialog -->
    <el-dialog
      v-model="adjustVisible"
      :title="settings.t('payroll.adjustTitle')"
      width="820px"
      destroy-on-close
    >
      <div v-if="adjustRow" class="adjust-header">
        <strong>{{ adjustRow.employeeName }}</strong>
        <span class="adjust-subtitle">{{ adjustRow.employeeCode }} · {{ adjustRow.departmentName }}</span>
      </div>
      <el-form :model="adjustForm" label-position="top">
        <section class="form-section">
          <h4 class="form-section-title">{{ settings.t('payroll.inputs') }}</h4>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.actualWorkDays')">
                <el-input-number v-model="adjustForm.actualWorkDays" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.paidLeaveDays')">
                <el-input-number v-model="adjustForm.paidLeaveDays" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.unpaidLeaveDays')">
                <el-input-number v-model="adjustForm.unpaidLeaveDays" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="6">
              <el-form-item :label="settings.t('payroll.otWeekday')">
                <el-input-number v-model="adjustForm.overtimeHoursWeekday" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="6">
              <el-form-item :label="settings.t('payroll.otWeekend')">
                <el-input-number v-model="adjustForm.overtimeHoursWeekend" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="6">
              <el-form-item :label="settings.t('payroll.otHoliday')">
                <el-input-number v-model="adjustForm.overtimeHoursHoliday" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="6">
              <el-form-item :label="settings.t('payroll.otNight')">
                <el-input-number v-model="adjustForm.overtimeHoursNight" :min="0" :step="0.5" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.dependents')">
                <el-input-number v-model="adjustForm.dependents" :min="0" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.bonus')">
                <el-input-number v-model="adjustForm.bonus" :min="0" :step="100000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.commission')">
                <el-input-number v-model="adjustForm.commission" :min="0" :step="100000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('payroll.otherDeductions')">
                <el-input-number v-model="adjustForm.otherDeductions" :min="0" :step="100000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <h4 class="form-section-title">{{ settings.t('payroll.allowances') }}</h4>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.responsibilityAllowance')">
                <el-input-number v-model="adjustForm.responsibilityAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.mealAllowance')">
                <el-input-number v-model="adjustForm.mealAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.transportAllowance')">
                <el-input-number v-model="adjustForm.transportAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.phoneAllowance')">
                <el-input-number v-model="adjustForm.phoneAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('contract.otherAllowance')">
                <el-input-number v-model="adjustForm.otherAllowance" :min="0" :step="50000" style="width:100%" controls-position="right" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="settings.t('payroll.note')">
                <el-input v-model="adjustForm.note" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitAdjust">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Detail drawer -->
    <el-drawer v-model="detailVisible" :title="settings.t('payroll.adjustTitle')" size="580px" destroy-on-close>
      <div v-if="selectedRow" class="payroll-detail">
        <div class="detail-header">
          <el-icon class="detail-icon"><Money /></el-icon>
          <div class="detail-title">
            <h3>{{ selectedRow.employeeName }}</h3>
            <p>{{ selectedRow.employeeCode }} · {{ selectedRow.departmentName || '-' }}</p>
          </div>
        </div>

        <h4 class="detail-section-title">{{ settings.t('payroll.inputs') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('payroll.baseSalary')">{{ formatMoney(selectedRow.baseSalary) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.insuranceSalary')">{{ formatMoney(selectedRow.insuranceSalary) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.contract')">{{ displayValue(selectedRow.contractCode) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('contract.position')">{{ displayValue(selectedRow.positionName) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.actualWorkDays')">{{ selectedRow.actualWorkDays }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.paidLeaveDays')">{{ selectedRow.paidLeaveDays }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.unpaidLeaveDays')">{{ selectedRow.unpaidLeaveDays }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.dependents')">{{ selectedRow.dependents }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('payroll.results') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('payroll.workingDaysSalary')">{{ formatMoney(selectedRow.workingDaysSalary) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.overtimePay')">{{ formatMoney(selectedRow.overtimePay) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.allowances')">{{ formatMoney(selectedRow.totalAllowance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.bonus')">{{ formatMoney(selectedRow.bonus) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.commission')">{{ formatMoney(selectedRow.commission) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.grossIncome')"><strong>{{ formatMoney(selectedRow.grossIncome) }}</strong></el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('payroll.insuranceBreakdown') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('payroll.socialInsurance')">{{ formatMoney(selectedRow.socialInsurance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.healthInsurance')">{{ formatMoney(selectedRow.healthInsurance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.unemploymentInsurance')">{{ formatMoney(selectedRow.unemploymentInsurance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.totalEmployeeInsurance')">{{ formatMoney(selectedRow.totalEmployeeInsurance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.taxableIncome')">{{ formatMoney(selectedRow.taxableIncome) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.personalIncomeTax')">{{ formatMoney(selectedRow.personalIncomeTax) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.otherDeductions')">{{ formatMoney(selectedRow.otherDeductions) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.netIncome')">
            <strong style="color:var(--vx-primary)">{{ formatMoney(selectedRow.netIncome) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.employerInsurance')">{{ formatMoney(selectedRow.employerInsurance) }}</el-descriptions-item>
          <el-descriptions-item :label="settings.t('payroll.totalEmployerCost')">{{ formatMoney(selectedRow.totalEmployerCost) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.payroll-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.period-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.period-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.period-title {
  font-size: var(--vx-font-size-md);
  font-weight: 700;
  color: var(--vx-text);
  margin: 0;
}

.period-year-filter {
  width: 100%;
}

.period-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
}

.period-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--vx-border);
  cursor: pointer;
  transition: all 0.15s ease;
}

.period-item:hover {
  background: color-mix(in srgb, var(--vx-primary) 6%, transparent);
  border-color: color-mix(in srgb, var(--vx-primary) 35%, transparent);
}

.period-item.active {
  background: color-mix(in srgb, var(--vx-primary) 10%, transparent);
  border-color: var(--vx-primary);
}

.period-item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.period-item-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.period-item:hover .period-item-actions,
.period-item.active .period-item-actions {
  opacity: 1;
}

.period-empty {
  color: var(--vx-text-secondary);
  text-align: center;
  padding: 30px 10px;
  font-size: 13px;
}

.rows-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
  overflow: hidden;
}

.rows-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--vx-text-secondary);
}

.rows-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.rows-header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.rows-header-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.period-range {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-sm);
}

.rows-header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.rows-filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.payroll-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(140px, 1fr));
  gap: 12px;
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
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
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

.rows-search {
  width: 260px;
}

.rows-filter {
  width: 200px;
}

.adjust-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.adjust-subtitle {
  color: var(--vx-text-secondary);
  font-size: 13px;
}

.payroll-detail {
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
  background: var(--vx-bg);
  border: 1px solid var(--vx-border);
  border-radius: 12px;
  padding: 16px 18px 4px;
  margin-bottom: 14px;
}

.form-section-title {
  color: var(--vx-text);
  font-size: var(--vx-font-size-md);
  font-weight: 700;
  margin: 0 0 12px;
}

@media (max-width: 980px) {
  .payroll-layout {
    grid-template-columns: 1fr;
  }

  .payroll-summary {
    grid-template-columns: 1fr;
  }

  .rows-search,
  .rows-filter {
    width: 100%;
  }
}
</style>
