<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeApi, payrollApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const settings = useSettingsStore()

const loading = ref(false)
const employee = ref(null)
const photoUrl = ref('')
const payrolls = ref([])
const payrollLoading = ref(false)

const employeeId = computed(() => route.params.id)
const canUpdate = computed(() => auth.hasPermission('EMPLOYEE_UPDATE'))
const canViewPayroll = computed(() => auth.hasPermission('PAYROLL_VIEW') || auth.hasPermission('PAYROLL_MANAGE'))
const latestPayroll = computed(() => payrolls.value[0] || null)

const statusOptions = [
  { value: 'PROBATION', labelKey: 'status.probation', type: 'warning' },
  { value: 'ACTIVE', labelKey: 'status.active', type: 'success' },
  { value: 'UNPAID_LEAVE', labelKey: 'status.unpaidLeave', type: 'info' },
  { value: 'RESIGNED', labelKey: 'status.resigned', type: 'danger' },
  { value: 'TERMINATED', labelKey: 'status.terminated', type: 'danger' },
]

const genderOptions = [
  { value: 'MALE', labelKey: 'gender.male' },
  { value: 'FEMALE', labelKey: 'gender.female' },
  { value: 'OTHER', labelKey: 'gender.other' },
]

const payrollStatusOptions = [
  { value: 'DRAFT', labelKey: 'payroll.status.DRAFT' },
  { value: 'CALCULATED', labelKey: 'payroll.status.CALCULATED' },
  { value: 'APPROVED', labelKey: 'payroll.status.APPROVED' },
  { value: 'PAID', labelKey: 'payroll.status.PAID' },
  { value: 'CANCELLED', labelKey: 'payroll.status.CANCELLED' },
]

onMounted(loadEmployee)
onUnmounted(revokePhotoUrl)

async function loadEmployee() {
  loading.value = true
  try {
    const { data } = await employeeApi.get(employeeId.value)
    employee.value = data
    await loadPhoto()
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }

  if (employee.value && canViewPayroll.value) {
    loadPayrolls()
  }
}

async function loadPayrolls() {
  payrollLoading.value = true
  try {
    const { data } = await payrollApi.byEmployee(employeeId.value)
    payrolls.value = Array.isArray(data)
      ? data.slice().sort((a, b) => payrollPeriodValue(b) - payrollPeriodValue(a))
      : []
  } catch {
    payrolls.value = []
    payrollLoading.value = false
    return
  } finally {
    payrollLoading.value = false
  }
}

async function loadPhoto() {
  revokePhotoUrl()
  if (!employee.value?.photoUrl) return

  try {
    const response = await employeeApi.downloadPhoto(employee.value.id)
    photoUrl.value = URL.createObjectURL(response.data)
  } catch {
    photoUrl.value = ''
  }
}

function revokePhotoUrl() {
  if (photoUrl.value) {
    URL.revokeObjectURL(photoUrl.value)
    photoUrl.value = ''
  }
}

async function downloadDocument(document) {
  try {
    const response = await employeeApi.downloadDocument(document.id)
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = url
    link.download = fileNameFromDisposition(response.headers['content-disposition']) || document.originalFileName
    window.document.body.appendChild(link)
    link.click()
    window.document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error(settings.t('task.downloadFailed'))
  }
}

function fileNameFromDisposition(disposition) {
  if (!disposition) return ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return match?.[1] || ''
}

function statusTag(status) {
  return statusOptions.find(s => s.value === status) || { label: status, type: 'info' }
}

function statusLabel(status) {
  const tag = statusTag(status)
  return tag.labelKey ? settings.t(tag.labelKey) : status || '-'
}

function genderLabel(gender) {
  const option = genderOptions.find(o => o.value === gender)
  return option ? settings.t(option.labelKey) : gender || '-'
}

function displayValue(value) {
  return value || '-'
}

function accountLabel(row) {
  if (!row.accountUsername) return settings.t('employee.noAccount')
  return row.accountRoleName ? `${row.accountUsername} (${row.accountRoleName})` : row.accountUsername
}

function accountStatusLabel(status) {
  return status ? settings.t(`status.${status.toLowerCase()}`) : '-'
}

function formatFileSize(size) {
  if (!size) return '0 KB'
  if (size < 1024 * 1024) return `${Math.ceil(size / 1024)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function payrollPeriodValue(row) {
  return Number(row.periodYear || 0) * 100 + Number(row.periodMonth || 0)
}

function periodLabel(row) {
  if (!row.periodMonth || !row.periodYear) return '-'
  return `${String(row.periodMonth).padStart(2, '0')}/${row.periodYear}`
}

function payrollStatusLabel(status) {
  const option = payrollStatusOptions.find(item => item.value === status)
  return option ? settings.t(option.labelKey) : status || '-'
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value || 0))
}

function openEdit() {
  router.push({ path: '/employees', query: { edit: employee.value.id } })
}
</script>

<template>
  <div class="employee-detail-page" v-loading="loading">
    <div class="detail-toolbar">
      <el-button @click="router.push('/employees')">
        <el-icon><ArrowLeft /></el-icon>
        {{ settings.t('employee.backToList') }}
      </el-button>
      <el-button v-if="canUpdate && employee" type="primary" @click="openEdit">
        <el-icon><Edit /></el-icon>
        {{ settings.t('employee.edit') }}
      </el-button>
    </div>

    <template v-if="employee">
      <section class="page-card detail-hero">
        <div class="photo-box">
          <el-avatar v-if="!photoUrl" :size="96" class="detail-avatar">
            {{ employee.fullName?.charAt(0) || '?' }}
          </el-avatar>
          <img v-else :src="photoUrl" class="employee-photo" :alt="employee.fullName" />
        </div>

        <div class="hero-info">
          <h2>{{ employee.fullName }}</h2>
          <p>{{ employee.employeeCode }} · {{ employee.departmentName || '-' }} · {{ employee.positionName || '-' }}</p>
          <el-tag :type="statusTag(employee.status).type" effect="light">
            {{ statusLabel(employee.status) }}
          </el-tag>
        </div>
      </section>

      <div class="detail-grid">
        <section class="page-card">
          <h3 class="section-title">{{ settings.t('common.basicInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('employee.employeeCode')">{{ displayValue(employee.employeeCode) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.fullName')">{{ displayValue(employee.fullName) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.gender')">{{ genderLabel(employee.gender) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.dateOfBirth')">{{ displayValue(employee.dateOfBirth) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.citizenId')">{{ displayValue(employee.citizenId) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('common.workInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('employee.department')">{{ displayValue(employee.departmentName) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.position')">{{ displayValue(employee.positionName) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.hireDate')">{{ displayValue(employee.joinDate) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.terminationDate')">{{ displayValue(employee.terminationDate) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.status')">{{ statusLabel(employee.status) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('common.contactInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('employee.phoneNumber')">{{ displayValue(employee.phoneNumber) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.personalEmail')">{{ displayValue(employee.personalEmail) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.address')">{{ displayValue(employee.address) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.emergencyContact')">{{ displayValue(employee.emergencyContactName) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.emergencyPhone')">{{ displayValue(employee.emergencyContactPhone) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('employee.accountInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('employee.loginAccount')">{{ accountLabel(employee) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.accountEmail')">{{ displayValue(employee.accountEmail) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.accountRole')">{{ displayValue(employee.accountRoleName) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.accountStatus')">{{ accountStatusLabel(employee.accountStatus) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('common.additionalInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('employee.bankAccount')">{{ displayValue(employee.bankAccount) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.taxCode')">{{ displayValue(employee.taxCode) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('employee.insuranceNumber')">{{ displayValue(employee.insuranceNumber) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card documents-panel">
          <div class="section-heading">
            <h3 class="section-title">{{ settings.t('employee.documents') }}</h3>
            <span>{{ employee.documents?.length || 0 }}</span>
          </div>

          <el-empty v-if="!employee.documents?.length" :description="settings.t('employee.noDocuments')" />
          <div v-else class="document-list">
            <div v-for="document in employee.documents" :key="document.id" class="document-row">
              <el-icon><Document /></el-icon>
              <div class="document-main">
                <strong>{{ document.originalFileName }}</strong>
                <span>{{ formatFileSize(document.fileSize) }}</span>
              </div>
              <el-button text type="primary" size="small" @click="downloadDocument(document)">
                <el-icon><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </section>

        <section v-if="canViewPayroll" class="page-card payroll-panel" v-loading="payrollLoading">
          <div class="section-heading">
            <h3 class="section-title">{{ settings.t('employee.payroll') }}</h3>
            <span>{{ payrolls.length }}</span>
          </div>

          <div v-if="latestPayroll" class="payroll-summary">
            <div>
              <span>{{ settings.t('payroll.period') }}</span>
              <strong>{{ periodLabel(latestPayroll) }}</strong>
            </div>
            <div>
              <span>{{ settings.t('payroll.grossIncome') }}</span>
              <strong>{{ formatMoney(latestPayroll.grossIncome) }}</strong>
            </div>
            <div>
              <span>{{ settings.t('payroll.netIncome') }}</span>
              <strong>{{ formatMoney(latestPayroll.netIncome) }}</strong>
            </div>
            <div>
              <span>{{ settings.t('employee.status') }}</span>
              <strong>{{ payrollStatusLabel(latestPayroll.status) }}</strong>
            </div>
          </div>

          <el-empty v-if="!payrolls.length" :description="settings.t('employee.noPayroll')" />
          <el-table v-else :data="payrolls" stripe style="width:100%">
            <el-table-column :label="settings.t('payroll.period')" width="100">
              <template #default="{ row }">{{ periodLabel(row) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('payroll.baseSalary')" min-width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('payroll.grossIncome')" min-width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.grossIncome) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('payroll.totalEmployeeInsurance')" min-width="140" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalEmployeeInsurance) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('payroll.personalIncomeTax')" min-width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.personalIncomeTax) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('payroll.netIncome')" min-width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.netIncome) }}</template>
            </el-table-column>
            <el-table-column :label="settings.t('employee.status')" width="120">
              <template #default="{ row }">{{ payrollStatusLabel(row.status) }}</template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.employee-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-toolbar {
  display: flex;
  justify-content: flex-start;
}

.detail-hero {
  display: flex;
  align-items: center;
  gap: 20px;
}

.photo-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.detail-avatar {
  background: var(--vx-primary);
  color: #fff;
  font-size: 34px;
  font-weight: 700;
}

.employee-photo {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--vx-border);
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-info h2 {
  margin: 0 0 4px;
  color: var(--vx-text);
  font-size: 24px;
}

.hero-info p {
  color: var(--vx-text-secondary);
  margin: 0 0 10px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.documents-panel {
  grid-column: 1 / -1;
}

.payroll-panel {
  grid-column: 1 / -1;
}

.section-title {
  color: var(--vx-text);
  font-size: var(--vx-font-size-md);
  font-weight: 700;
  margin: 0 0 12px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-heading span {
  color: var(--vx-text-secondary);
}

.payroll-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.payroll-summary div {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 10px 12px;
  background: var(--vx-bg);
}

.payroll-summary span {
  display: block;
  color: var(--vx-text-secondary);
  font-size: 12px;
  margin-bottom: 4px;
}

.payroll-summary strong {
  color: var(--vx-text);
}

.document-list {
  display: grid;
  gap: 8px;
}

.document-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
}

.document-main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.document-main strong {
  color: var(--vx-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-main span {
  color: var(--vx-text-secondary);
  font-size: 12px;
}

@media (max-width: 860px) {
  .detail-hero,
  .detail-grid,
  .payroll-summary {
    grid-template-columns: 1fr;
  }

  .detail-hero {
    align-items: flex-start;
  }
}
</style>
