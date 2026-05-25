<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contractApi, employeeApi, positionApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import {
  contractFilename,
  downloadBlob,
  exportContractDoc,
  printContract,
} from '@/utils/contractDocument'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const settings = useSettingsStore()

const loading = ref(false)
const contract = ref(null)
const employees = ref([])
const positions = ref([])

const terminateVisible = ref(false)
const renewVisible = ref(false)
const terminateForm = reactive({
  terminatedDate: '',
  terminationReason: '',
})
const renewForm = reactive({
  newEndDate: '',
})

const canUpdate = computed(() => auth.hasPermission('CONTRACT_UPDATE'))
const canDelete = computed(() => auth.hasPermission('CONTRACT_DELETE'))
const contractId = computed(() => route.params.id)
const signedFile = computed(() => {
  const row = contract.value || {}
  return row.signedFile || row.signedDocument || row.contractFile || row.file || null
})
const signedFileName = computed(() => (
  signedFile.value?.originalFileName ||
  signedFile.value?.fileName ||
  contract.value?.signedFileName ||
  contract.value?.signedDocumentName ||
  contract.value?.contractFileName ||
  ''
))
const hasSignedFile = computed(() => Boolean(signedFileName.value || signedFile.value?.id || contract.value?.signedFileUrl))
const canActivate = computed(() => contract.value?.status === 'DRAFT' && hasSignedFile.value)
const canRenew = computed(() => Boolean(
  contract.value?.endDate &&
  (contract.value?.status === 'ACTIVE' || contract.value?.status === 'EXPIRED')
))
const canLiquidate = computed(() => contract.value?.status === 'EXPIRED' || contract.value?.status === 'TERMINATED')
const canSoftDelete = computed(() => contract.value?.status === 'DRAFT' || contract.value?.status === 'LIQUIDATED')

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

onMounted(() => {
  loadContract()
  loadLookups()
})

async function loadContract() {
  loading.value = true
  try {
    const { data } = await contractApi.get(contractId.value)
    contract.value = data
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function loadLookups() {
  const [empRes, posRes] = await Promise.allSettled([
    loadAllEmployees(),
    positionApi.list({ page: 1, size: 1000 }),
  ])
  if (empRes.status === 'fulfilled') {
    employees.value = empRes.value
  }
  if (posRes.status === 'fulfilled') {
    const data = posRes.value.data
    positions.value = Array.isArray(data) ? data : data.content || []
  }
}

async function loadAllEmployees() {
  const size = 200
  let page = 0
  let totalPages = 1
  const all = []

  while (page < totalPages) {
    const { data } = await employeeApi.list({ page, size })
    const rows = Array.isArray(data) ? data : data?.content || []
    all.push(...rows)

    if (Array.isArray(data)) {
      break
    }

    totalPages = Math.max(data?.totalPages || 0, 1)
    page += 1
  }

  return all
}

function typeLabel(value) {
  const option = contractTypeOptions.find(item => item.value === value)
  return option ? settings.t(option.labelKey) : value || '-'
}

function statusTag(value) {
  return statusOptions.find(item => item.value === value) || { type: 'info', labelKey: '' }
}

function statusLabel(value) {
  const option = statusTag(value)
  return option.labelKey ? settings.t(option.labelKey) : value || '-'
}

function employeeName(id) {
  const employee = employees.value.find(item => item.id === id)
  if (!employee) return contract.value?.employeeName || '-'
  return `${employee.employeeCode} - ${employee.fullName}`
}

function positionName(id) {
  return positions.value.find(item => item.id === id)?.name || contract.value?.positionName || '-'
}

function displayValue(value) {
  return value || '-'
}

function formatMoney(value) {
  if (value == null || value === '') return '-'
  return new Intl.NumberFormat('vi-VN').format(Number(value))
}

function formatFileSize(size) {
  if (!size) return ''
  if (size < 1024 * 1024) return `${Math.ceil(size / 1024)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function fileMeta() {
  const size = signedFile.value?.fileSize || contract.value?.signedFileSize
  return [signedFileName.value, formatFileSize(size)].filter(Boolean).join(' · ')
}

function exportDoc() {
  if (!contract.value) return
  exportContractDoc(contract.value, contractHelpers())
}

function printPdf() {
  if (!contract.value) return
  printContract(contract.value, contractHelpers())
}

function contractHelpers() {
  return {
    t: settings.t,
    typeLabel,
    employeeName,
    positionName,
  }
}

async function downloadSignedFile() {
  try {
    const response = await contractApi.downloadSignedFile(contractId.value)
    downloadBlob(response, signedFileName.value || contractFilename(contract.value, 'pdf'))
  } catch {
    ElMessage.error(settings.t('task.downloadFailed'))
  }
}

// FE nút Duyệt HD ở trang chi tiết -> contractApi.activate -> BE ContractController.activate.
async function handleActivate() {
  try {
    await ElMessageBox.confirm(settings.t('contract.activateConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await contractApi.activate(contractId.value)
    await loadContract()
    ElMessage.success(settings.t('common.updated'))
  } catch {}
}

function openRenew() {
  renewForm.newEndDate = contract.value?.endDate || ''
  renewVisible.value = true
}

// FE trang chi tiết gửi yêu cầu gia hạn -> contractApi.renew -> BE ContractController.renew.
async function submitRenew() {
  try {
    await ElMessageBox.confirm(settings.t('contract.renewConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await contractApi.renew(contractId.value, {
      newEndDate: renewForm.newEndDate,
    })
    renewVisible.value = false
    await loadContract()
    ElMessage.success(settings.t('common.updated'))
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

function openTerminate() {
  terminateForm.terminatedDate = new Date().toISOString().slice(0, 10)
  terminateForm.terminationReason = ''
  terminateVisible.value = true
}

async function submitTerminate() {
  try {
    await contractApi.terminate(contractId.value, {
      terminatedDate: terminateForm.terminatedDate,
      reason: terminateForm.terminationReason,
    })
    terminateVisible.value = false
    await loadContract()
    ElMessage.success(settings.t('common.updated'))
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function handleLiquidate() {
  try {
    await ElMessageBox.confirm(settings.t('contract.liquidateConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await contractApi.liquidate(contractId.value)
    await loadContract()
    ElMessage.success(settings.t('common.updated'))
  } catch {}
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(`${settings.t('contract.deleteConfirm')} "${contract.value.contractCode}"?`, settings.t('common.confirm'), { type: 'warning' })
    await contractApi.delete(contractId.value)
    ElMessage.success(settings.t('common.deleted'))
    router.push('/contracts')
  } catch {}
}

function renewDateDisabled(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const oldEndDate = contract.value?.endDate ? new Date(contract.value.endDate) : null
  if (oldEndDate) {
    oldEndDate.setHours(0, 0, 0, 0)
  }

  return date.getTime() <= today.getTime() || (oldEndDate && date.getTime() <= oldEndDate.getTime())
}
</script>

<template>
  <div class="contract-detail-page" v-loading="loading">
    <div class="detail-toolbar">
      <el-button @click="router.push('/contracts')">
        <el-icon><ArrowLeft /></el-icon>
        {{ settings.t('contract.backToList') }}
      </el-button>

      <div class="toolbar-actions" v-if="contract">
        <el-button @click="exportDoc">
          <el-icon><Download /></el-icon>
          {{ settings.t('contract.exportForSigning') }}
        </el-button>
        <el-button @click="printPdf">
          <el-icon><Printer /></el-icon>
          PDF
        </el-button>
        <el-button
          v-if="canUpdate && contract.status === 'DRAFT'"
          type="success"
          :disabled="!canActivate"
          @click="handleActivate"
        >
          <el-icon><CircleCheck /></el-icon>
          {{ settings.t('contract.activate') }}
        </el-button>
        <el-button
          v-if="canUpdate && canRenew"
          type="primary"
          @click="openRenew"
        >
          <el-icon><RefreshRight /></el-icon>
          {{ settings.t('contract.renew') }}
        </el-button>
        <el-button
          v-if="canUpdate && contract.status === 'ACTIVE'"
          type="warning"
          @click="openTerminate"
        >
          <el-icon><CloseBold /></el-icon>
          {{ settings.t('contract.terminate') }}
        </el-button>
        <el-button
          v-if="canUpdate && canLiquidate"
          type="warning"
          @click="handleLiquidate"
        >
          <el-icon><Finished /></el-icon>
          {{ settings.t('contract.liquidate') }}
        </el-button>
        <el-button v-if="canDelete && canSoftDelete" type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          {{ settings.t('common.delete') }}
        </el-button>
      </div>
    </div>

    <template v-if="contract">
      <section class="page-card contract-hero">
        <div class="contract-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="hero-info">
          <h2>{{ contract.contractCode }}</h2>
          <p>{{ employeeName(contract.employeeId) }} · {{ positionName(contract.positionId) }}</p>
          <div class="hero-tags">
            <el-tag :type="statusTag(contract.status).type" effect="light">
              {{ statusLabel(contract.status) }}
            </el-tag>
            <el-tag effect="plain">{{ typeLabel(contract.contractType) }}</el-tag>
          </div>
        </div>
      </section>

      <div class="detail-grid">
        <section class="page-card">
          <h3 class="section-title">{{ settings.t('contract.basicInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('contract.contractCode')">{{ displayValue(contract.contractCode) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.employee')">{{ employeeName(contract.employeeId) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.position')">{{ positionName(contract.positionId) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.contractType')">{{ typeLabel(contract.contractType) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.status')">{{ statusLabel(contract.status) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.signedDate')">{{ displayValue(contract.signedDate) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.startDate')">{{ displayValue(contract.startDate) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.endDate')">{{ displayValue(contract.endDate) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.probationMonths')">{{ displayValue(contract.probationMonths) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('contract.salaryInfo') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('contract.baseSalary')">{{ formatMoney(contract.baseSalary) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.insuranceSalary')">{{ formatMoney(contract.insuranceSalary) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.responsibilityAllowance')">{{ formatMoney(contract.responsibilityAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.mealAllowance')">{{ formatMoney(contract.mealAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.transportAllowance')">{{ formatMoney(contract.transportAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.phoneAllowance')">{{ formatMoney(contract.phoneAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.otherAllowance')">{{ formatMoney(contract.otherAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.totalAllowance')">{{ formatMoney(contract.totalAllowance) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.totalGrossSalary')">{{ formatMoney(contract.totalGrossSalary) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card">
          <h3 class="section-title">{{ settings.t('contract.workTerms') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="settings.t('contract.standardWorkDays')">{{ displayValue(contract.standardWorkDays) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.hoursPerDay')">{{ displayValue(contract.hoursPerDay) }}</el-descriptions-item>
            <el-descriptions-item :label="settings.t('contract.noticePeriodDays')">{{ displayValue(contract.noticePeriodDays) }}</el-descriptions-item>
            <el-descriptions-item v-if="contract.terminatedDate" :label="settings.t('contract.terminatedDate')">{{ contract.terminatedDate }}</el-descriptions-item>
            <el-descriptions-item v-if="contract.terminationReason" :label="settings.t('contract.terminationReason')">{{ contract.terminationReason }}</el-descriptions-item>
            <el-descriptions-item v-if="contract.note" :label="settings.t('contract.note')">{{ contract.note }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <section class="page-card signed-file-panel">
          <div class="section-heading">
            <h3 class="section-title">{{ settings.t('contract.signedFile') }}</h3>
            <el-tag v-if="hasSignedFile" type="success" effect="light">{{ settings.t('contract.fileSaved') }}</el-tag>
          </div>

          <div v-if="hasSignedFile" class="signed-file-row">
            <el-icon><Document /></el-icon>
            <div>
              <strong>{{ fileMeta() }}</strong>
              <span>{{ settings.t('contract.signedFileHint') }}</span>
            </div>
            <el-button text type="primary" @click="downloadSignedFile">
              <el-icon><Download /></el-icon>
            </el-button>
          </div>
          <div v-else class="signed-file-empty">
            {{ settings.t('common.noData') }}
          </div>
        </section>
      </div>
    </template>

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

    <el-dialog v-model="renewVisible" :title="settings.t('contract.renewTitle')" width="480px" destroy-on-close class="vx-dialog" align-center>
      <el-form label-position="top">
        <el-form-item :label="settings.t('contract.newEndDate')">
          <el-date-picker
            v-model="renewForm.newEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="renewDateDisabled"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitRenew">{{ settings.t('contract.renew') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.contract-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.contract-hero {
  display: flex;
  align-items: center;
  gap: 18px;
}

.contract-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--vx-primary) 12%, #fff);
  color: var(--vx-primary);
  font-size: 30px;
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
  margin: 0 0 10px;
  color: var(--vx-text-secondary);
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.signed-file-panel {
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
  gap: 12px;
  margin-bottom: 12px;
}

.section-heading .section-title {
  margin: 0;
}

.signed-file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  margin-bottom: 14px;
}

.signed-file-row > div {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.signed-file-row strong {
  overflow: hidden;
  color: var(--vx-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.signed-file-row span {
  color: var(--vx-text-secondary);
  font-size: 12px;
}

.signed-file-empty {
  padding: 16px;
  border: 1px dashed var(--vx-border);
  border-radius: 8px;
  color: var(--vx-text-secondary);
  text-align: center;
}

@media (max-width: 860px) {
  .contract-hero,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .contract-hero {
    align-items: flex-start;
  }
}
</style>
