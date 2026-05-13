<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { employeeApi, departmentApi, positionApi, roleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const employees = ref([])
const employeePhotoUrls = ref({})
const departments = ref([])
const positions = ref([])
const roles = ref([])
const keyword = ref('')
const saving = ref(false)
const filters = reactive({
  departmentId: null,
  gender: '',
  status: '',
})
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitleKey = ref('employee.add')
const formRef = ref(null)
const selectedEmployee = ref(null)
const editingEmployee = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })
const canLoadDepartments = computed(() => auth.hasPermission('DEPARTMENT_VIEW'))
const canLoadPositions = computed(() => auth.hasPermission('POSITION_VIEW'))
const canLoadRoles = computed(() => (
  auth.hasPermission('ROLE_VIEW')
  || auth.hasPermission('EMPLOYEE_CREATE')
  || auth.hasPermission('EMPLOYEE_UPDATE')
  || auth.hasPermission('USER_CREATE')
  || auth.hasPermission('USER_UPDATE')
))

const defaultForm = {
  employeeCode: '',
  fullName: '',
  dateOfBirth: '',
  gender: 'MALE',
  citizenId: '',
  phoneNumber: '',
  personalEmail: '',
  address: '',
  departmentId: null,
  positionId: null,
  userId: null,
  createUserAccount: true,
  accountUsername: '',
  accountEmail: '',
  accountPassword: 'User@123',
  accountRoleId: null,
  joinDate: '',
  status: 'ACTIVE',
  bankAccount: '',
  taxCode: '',
  insuranceNumber: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
}

const form = reactive({ ...defaultForm })
const editingId = ref(null)
const photoFiles = ref([])
const documentFiles = ref([])
const currentDocuments = computed(() => editingEmployee.value?.documents || [])

const rules = computed(() => ({
  employeeCode: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  fullName: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  gender: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
  accountPassword: [{
    validator: (_rule, value, callback) => {
      if (form.createUserAccount && value && value.length < 6) {
        callback(new Error(settings.t('profile.passwordLength')))
        return
      }
      callback()
    },
    trigger: 'blur',
  }],
}))

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

onMounted(async () => {
  await fetchData()
  await openEditFromQuery()
})

onUnmounted(revokeEmployeePhotos)

watch(employees, loadEmployeePhotos)
watch(() => route.query.edit, () => openEditFromQuery())

async function fetchData() {
  loading.value = true
  try {
    const empRes = await employeeApi.list(pagedEmployeeParams())
    applyPage(empRes.data, employees, pagination)
    loadEmployeeLookups()
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function loadEmployeeLookups() {
  const requests = []

  if (canLoadDepartments.value) {
    requests.push(departmentApi.list().then(res => { departments.value = Array.isArray(res.data) ? res.data : res.data.content || [] }))
  } else {
    departments.value = []
  }

  if (canLoadPositions.value) {
    requests.push(positionApi.list().then(res => { positions.value = Array.isArray(res.data) ? res.data : res.data.content || [] }))
  } else {
    positions.value = []
  }

  if (canLoadRoles.value && !roles.value.length) {
    requests.push(loadRoles())
  }

  await Promise.allSettled(requests)
}

async function loadRoles() {
  const res = await roleApi.list()
  roles.value = Array.isArray(res.data) ? res.data : res.data.content || []
}

async function ensureRolesLoaded() {
  if (!canLoadRoles.value || roles.value.length) return
  try {
    await loadRoles()
  } catch {
    roles.value = []
  }
}

function pagedEmployeeParams() {
  const params = {
    keyword: keyword.value,
    ...pageParams(pagination),
  }

  if (filters.departmentId) {
    params.departmentId = filters.departmentId
  }
  if (filters.gender) {
    params.gender = filters.gender
  }
  if (filters.status) {
    params.status = filters.status
  }

  return params
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleResetFilters() {
  keyword.value = ''
  filters.departmentId = null
  filters.gender = ''
  filters.status = ''
  pagination.page = 1
  fetchData()
}

function handlePageChange(page) {
  pagination.page = page
  fetchData()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchData()
}

async function openCreate() {
  await ensureRolesLoaded()
  Object.assign(form, defaultForm)
  resetUploadFiles()
  editingEmployee.value = null
  form.accountRoleId = defaultEmployeeRoleId()
  editingId.value = null
  dialogTitleKey.value = 'employee.add'
  dialogVisible.value = true
}

async function openEdit(row) {
  await ensureRolesLoaded()
  editingId.value = row.id
  resetUploadFiles()
  const employee = await loadEmployeeForEdit(row)
  editingEmployee.value = employee
  Object.assign(form, {
    employeeCode: employee.employeeCode,
    fullName: employee.fullName,
    dateOfBirth: employee.dateOfBirth,
    gender: employee.gender,
    citizenId: employee.citizenId,
    phoneNumber: employee.phoneNumber,
    personalEmail: employee.personalEmail,
    address: employee.address,
    departmentId: employee.departmentId,
    positionId: employee.positionId,
    userId: employee.userId || null,
    createUserAccount: false,
    accountUsername: '',
    accountEmail: '',
    accountPassword: 'User@123',
    accountRoleId: defaultEmployeeRoleId(),
    joinDate: employee.joinDate,
    status: employee.status,
    bankAccount: employee.bankAccount,
    taxCode: employee.taxCode,
    insuranceNumber: employee.insuranceNumber,
    emergencyContactName: employee.emergencyContactName,
    emergencyContactPhone: employee.emergencyContactPhone,
  })
  dialogTitleKey.value = 'employee.edit'
  dialogVisible.value = true
}

async function openEditFromQuery() {
  const id = route.query.edit
  if (!id || dialogVisible.value) return

  const row = employees.value.find(employee => String(employee.id) === String(id)) || { id }
  await openEdit(row)
  router.replace({ path: '/employees', query: {} })
}

async function loadEmployeeForEdit(row) {
  try {
    const { data } = await employeeApi.get(row.id)
    return data
  } catch {
    return row
  }
}

function openDetail(row) {
  router.push(`/employees/${row.id}`)
}

async function loadEmployeePhotos(rows = []) {
  revokeEmployeePhotos()
  const nextUrls = {}

  await Promise.allSettled(rows
    .filter(employee => employee.photoUrl)
    .map(async employee => {
      const response = await employeeApi.downloadPhoto(employee.id)
      nextUrls[employee.id] = URL.createObjectURL(response.data)
    }))

  employeePhotoUrls.value = nextUrls
}

function revokeEmployeePhotos() {
  Object.values(employeePhotoUrls.value).forEach(url => URL.revokeObjectURL(url))
  employeePhotoUrls.value = {}
}

function employeePhoto(row) {
  return employeePhotoUrls.value[row.id] || ''
}

function employeeInitial(row) {
  return row.fullName?.trim()?.charAt(0) || row.employeeCode?.trim()?.charAt(0) || '?'
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = employeePayload()
    let response
    if (editingId.value) {
      response = await employeeApi.update(editingId.value, payload)
      await uploadEmployeeFiles(response.data.id)
      ElMessage.success(settings.t('common.updated'))
    } else {
      response = await employeeApi.create(payload)
      await uploadEmployeeFiles(response.data.id)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    resetUploadFiles()
    editingEmployee.value = null
    await fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('employee.deleteConfirm')} "${row.fullName}"?`, settings.t('common.confirm'), { type: 'warning' })
    await employeeApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    fetchData()
  } catch {
    // cancelled
  }
}

function statusTag(status) {
  return statusOptions.find(s => s.value === status) || { label: status, type: 'info' }
}

function genderLabel(g) {
  const option = genderOptions.find(o => o.value === g)
  return option ? settings.t(option.labelKey) : g
}

function deptName(id) {
  return departments.value.find(d => d.id === id)?.name || '-'
}

function posName(id) {
  return positions.value.find(p => p.id === id)?.name || '-'
}

function defaultEmployeeRoleId() {
  return roles.value.find(role => role.name === 'Employee')?.id || roles.value[0]?.id || null
}

function displayValue(value) {
  return value || '-'
}

function statusLabel(status) {
  const tag = statusTag(status)
  return tag.labelKey ? settings.t(tag.labelKey) : status || '-'
}

function accountLabel(row) {
  if (!row.accountUsername) return settings.t('employee.noAccount')
  return row.accountRoleName
    ? `${row.accountUsername} (${row.accountRoleName})`
    : row.accountUsername
}

function employeePayload() {
  const payload = { ...form }
  if (!payload.createUserAccount) {
    payload.accountUsername = ''
    payload.accountEmail = ''
    payload.accountPassword = ''
    payload.accountRoleId = null
  }
  return payload
}

function beforeUpload() {
  return false
}

function handlePhotoChange(_file, files) {
  photoFiles.value = files.slice(-1)
}

function handlePhotoRemove(_file, files) {
  photoFiles.value = files
}

function handleDocumentChange(_file, files) {
  documentFiles.value = files
}

function handleDocumentRemove(_file, files) {
  documentFiles.value = files
}

function resetUploadFiles() {
  photoFiles.value = []
  documentFiles.value = []
}

async function uploadEmployeeFiles(employeeId) {
  const photo = photoFiles.value.map(item => item.raw).filter(Boolean)[0]
  const documents = documentFiles.value.map(item => item.raw).filter(Boolean)

  if (photo) {
    await employeeApi.uploadPhoto(employeeId, photo)
  }

  if (documents.length) {
    await employeeApi.uploadDocuments(employeeId, documents)
  }
}

async function refreshEditingEmployee() {
  if (!editingId.value) return
  const { data } = await employeeApi.get(editingId.value)
  editingEmployee.value = data
}

async function deleteExistingPhoto() {
  if (!editingEmployee.value?.photoUrl) return

  try {
    await ElMessageBox.confirm(settings.t('employee.deletePhotoConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await employeeApi.deletePhoto(editingEmployee.value.id)
    await refreshEditingEmployee()
    await fetchData()
    ElMessage.success(settings.t('common.deleted'))
  } catch {}
}

async function downloadExistingDocument(file) {
  try {
    const response = await employeeApi.downloadDocument(file.id)
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = url
    link.download = fileNameFromDisposition(response.headers['content-disposition']) || file.originalFileName
    window.document.body.appendChild(link)
    link.click()
    window.document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error(settings.t('task.downloadFailed'))
  }
}

async function deleteExistingDocument(file) {
  try {
    await ElMessageBox.confirm(`${settings.t('employee.deleteDocumentConfirm')} "${file.originalFileName}"?`, settings.t('common.confirm'), { type: 'warning' })
    await employeeApi.deleteDocument(editingEmployee.value.id, file.id)
    await refreshEditingEmployee()
    await fetchData()
    ElMessage.success(settings.t('common.deleted'))
  } catch {}
}

function fileNameFromDisposition(disposition) {
  if (!disposition) return ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return match?.[1] || ''
}

function formatFileSize(size) {
  if (!size) return '0 KB'
  if (size < 1024 * 1024) return `${Math.ceil(size / 1024)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const exportColumns = computed(() => [
  { prop: 'employeeCode', label: settings.t('employee.employeeCode') },
  { prop: 'fullName', label: settings.t('employee.fullName') },
  { label: settings.t('employee.gender'), format: row => genderLabel(row.gender) },
  { label: settings.t('employee.department'), format: row => deptName(row.departmentId) },
  { label: settings.t('employee.position'), format: row => posName(row.positionId) },
  { prop: 'phoneNumber', label: settings.t('employee.phone') },
  { prop: 'personalEmail', label: settings.t('employee.personalEmail') },
  { prop: 'joinDate', label: settings.t('employee.hireDate') },
  { label: settings.t('employee.status'), format: row => statusLabel(row.status) },
  { label: settings.t('employee.loginAccount'), format: row => accountLabel(row) },
])

async function fetchAllEmployees() {
  const params = {
    keyword: keyword.value,
    page: 1,
    size: 10000,
  }
  if (filters.departmentId) params.departmentId = filters.departmentId
  if (filters.gender) params.gender = filters.gender
  if (filters.status) params.status = filters.status
  const res = await employeeApi.list(params)
  return Array.isArray(res.data) ? res.data : res.data.content || []
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div class="employee-filters">
        <el-input
          v-model="keyword"
          :placeholder="settings.t('employee.search')"
          clearable
          class="employee-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="filters.departmentId"
          clearable
          filterable
          class="employee-filter"
          :placeholder="settings.t('employee.department')"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>

        <el-select
          v-model="filters.gender"
          clearable
          class="employee-filter"
          :placeholder="settings.t('employee.gender')"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="g in genderOptions" :key="g.value" :label="settings.t(g.labelKey)" :value="g.value" />
        </el-select>

        <el-select
          v-model="filters.status"
          clearable
          class="employee-filter"
          :placeholder="settings.t('employee.status')"
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
        <ExportActions
          filename="employees"
          title="Employees List"
          :columns="exportColumns"
          :fetch-all="fetchAllEmployees"
        />
        <el-button v-if="auth.hasPermission('EMPLOYEE_CREATE')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> {{ settings.t('employee.add') }}
        </el-button>
      </div>
    </div>

    <el-table :data="employees" v-loading="loading" stripe style="width: 100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="employeeCode" :label="settings.t('employee.employeeCode')" width="110" />
      <el-table-column prop="fullName" :label="settings.t('employee.fullName')" min-width="160">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <el-avatar
              :size="30"
              :src="employeePhoto(row)"
              style="background:var(--vx-primary);color:white;font-size:12px;flex-shrink:0"
            >
              {{ employeeInitial(row) }}
            </el-avatar>
            <span>{{ row.fullName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('employee.gender')" width="90">
        <template #default="{ row }">{{ genderLabel(row.gender) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('employee.department')" min-width="130">
        <template #default="{ row }">{{ deptName(row.departmentId) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('employee.position')" min-width="130">
        <template #default="{ row }">{{ posName(row.positionId) }}</template>
      </el-table-column>
      <el-table-column prop="phoneNumber" :label="settings.t('employee.phone')" width="120" />
      <el-table-column :label="settings.t('employee.status')" width="130">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status).type" size="small" effect="light">
            {{ statusTag(row.status).labelKey ? settings.t(statusTag(row.status).labelKey) : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('employee.loginAccount')" min-width="160">
        <template #default="{ row }">{{ accountLabel(row) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('common.actions')" width="170" fixed="right">
        <template #default="{ row }">
          <el-button text type="info" size="small" @click="openDetail(row)">
            <el-icon><View /></el-icon>
          </el-button>
          <el-button v-if="auth.hasPermission('EMPLOYEE_UPDATE')" text type="primary" size="small" @click="openEdit(row)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button v-if="auth.hasPermission('EMPLOYEE_DELETE')" text type="danger" size="small" @click="handleDelete(row)">
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

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="settings.t(dialogTitleKey)"
      width="860px"
      destroy-on-close
      class="vx-dialog employee-dialog"
      align-center
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><User /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.employeeCode') }} · {{ settings.t('employee.fullName') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.employeeCode')" prop="employeeCode">
                <el-input v-model="form.employeeCode" :disabled="!!editingId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.fullName')" prop="fullName">
                <el-input v-model="form.fullName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.dateOfBirth')">
                <el-date-picker v-model="form.dateOfBirth" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.gender')" prop="gender">
                <el-select v-model="form.gender" style="width:100%">
                  <el-option v-for="g in genderOptions" :key="g.value" :label="settings.t(g.labelKey)" :value="g.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.citizenId')">
                <el-input v-model="form.citizenId" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Phone /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.contactInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.phone') }} · {{ settings.t('employee.personalEmail') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.phoneNumber')">
                <el-input v-model="form.phoneNumber" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.personalEmail')">
                <el-input v-model="form.personalEmail" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item :label="settings.t('employee.address')">
                <el-input v-model="form.address" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><OfficeBuilding /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.workInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.department') }} · {{ settings.t('employee.position') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.department')">
                <el-select v-model="form.departmentId" clearable style="width:100%" :placeholder="settings.t('employee.selectDepartment')">
                  <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.position')">
                <el-select v-model="form.positionId" clearable style="width:100%" :placeholder="settings.t('employee.selectPosition')">
                  <el-option v-for="p in positions" :key="p.id" :label="p.name" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.hireDate')">
                <el-date-picker v-model="form.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.status')">
                <el-select v-model="form.status" style="width:100%">
                  <el-option v-for="s in statusOptions" :key="s.value" :label="settings.t(s.labelKey)" :value="s.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Key /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('employee.accountInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.createLoginAccount') }}</p>
            </div>
            <el-switch
              v-if="!form.userId"
              v-model="form.createUserAccount"
              class="form-section-switch"
            />
          </div>
          <el-alert
            v-if="form.userId"
            :title="settings.t('employee.accountAlreadyLinked')"
            type="success"
            show-icon
            :closable="false"
          />
          <el-row v-else-if="form.createUserAccount" :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.accountUsername')">
                <el-input
                  v-model="form.accountUsername"
                  :placeholder="settings.t('employee.defaultUsernameHint')"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.accountEmail')">
                <el-input
                  v-model="form.accountEmail"
                  :placeholder="settings.t('employee.defaultEmailHint')"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.accountPassword')" prop="accountPassword">
                <el-input v-model="form.accountPassword" show-password />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.accountRole')">
                <el-select v-model="form.accountRoleId" clearable style="width:100%" :placeholder="settings.t('employee.selectRole')">
                  <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.additionalInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.bankAccount') }} · {{ settings.t('employee.emergencyContact') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.bankAccount')">
                <el-input v-model="form.bankAccount" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.taxCode')">
                <el-input v-model="form.taxCode" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.insuranceNumber')">
                <el-input v-model="form.insuranceNumber" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.emergencyContact')">
                <el-input v-model="form.emergencyContactName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.emergencyPhone')">
                <el-input v-model="form.emergencyContactPhone" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Upload /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('employee.documents') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.uploadOnSaveHint') }}</p>
            </div>
          </div>
          <div v-if="editingId" class="current-files">
            <div class="current-file-group">
              <div class="current-file-title">{{ settings.t('employee.currentPhoto') }}</div>
              <div v-if="editingEmployee?.photoUrl" class="current-file-row">
                <el-icon><Picture /></el-icon>
                <span>{{ editingEmployee.photoOriginalFileName || settings.t('employee.uploadPhoto') }}</span>
                <el-button text type="danger" size="small" @click="deleteExistingPhoto">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div v-else class="current-file-empty">{{ settings.t('employee.noPhoto') }}</div>
            </div>

            <div class="current-file-group">
              <div class="current-file-title">{{ settings.t('employee.currentDocuments') }}</div>
              <div v-if="currentDocuments.length" class="current-file-list">
                <div v-for="file in currentDocuments" :key="file.id" class="current-file-row">
                  <el-icon><Document /></el-icon>
                  <span>{{ file.originalFileName }}</span>
                  <small>{{ formatFileSize(file.fileSize) }}</small>
                  <el-button text type="primary" size="small" @click="downloadExistingDocument(file)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <el-button text type="danger" size="small" @click="deleteExistingDocument(file)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <div v-else class="current-file-empty">{{ settings.t('employee.noDocuments') }}</div>
            </div>
          </div>

          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.uploadPhoto')">
                <el-upload
                  v-model:file-list="photoFiles"
                  accept="image/*"
                  :limit="1"
                  :auto-upload="false"
                  :before-upload="beforeUpload"
                  :on-change="handlePhotoChange"
                  :on-remove="handlePhotoRemove"
                >
                  <el-button>
                    <el-icon><Picture /></el-icon>
                    {{ settings.t('employee.uploadPhoto') }}
                  </el-button>
                </el-upload>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('employee.documents')">
                <el-upload
                  v-model:file-list="documentFiles"
                  multiple
                  :auto-upload="false"
                  :before-upload="beforeUpload"
                  :on-change="handleDocumentChange"
                  :on-remove="handleDocumentRemove"
                >
                  <el-button>
                    <el-icon><Upload /></el-icon>
                    {{ settings.t('task.upload') }}
                  </el-button>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      :title="settings.t('employee.details')"
      size="560px"
      destroy-on-close
    >
      <div v-if="selectedEmployee" class="employee-detail">
        <div class="detail-header">
          <el-avatar :size="52" class="detail-avatar">
            {{ selectedEmployee.fullName?.charAt(0) || '?' }}
          </el-avatar>
          <div class="detail-title">
            <h3>{{ selectedEmployee.fullName }}</h3>
            <p>{{ selectedEmployee.employeeCode }}</p>
          </div>
          <el-tag :type="statusTag(selectedEmployee.status).type" effect="light">
            {{ statusLabel(selectedEmployee.status) }}
          </el-tag>
        </div>

        <h4 class="detail-section-title">{{ settings.t('common.basicInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('employee.employeeCode')">
            {{ displayValue(selectedEmployee.employeeCode) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.fullName')">
            {{ displayValue(selectedEmployee.fullName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.gender')">
            {{ genderLabel(selectedEmployee.gender) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.dateOfBirth')">
            {{ displayValue(selectedEmployee.dateOfBirth) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.citizenId')">
            {{ displayValue(selectedEmployee.citizenId) }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('common.workInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('employee.department')">
            {{ selectedEmployee.departmentName || deptName(selectedEmployee.departmentId) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.position')">
            {{ selectedEmployee.positionName || posName(selectedEmployee.positionId) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.hireDate')">
            {{ displayValue(selectedEmployee.joinDate) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.terminationDate')">
            {{ displayValue(selectedEmployee.terminationDate) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.status')">
            {{ statusLabel(selectedEmployee.status) }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('employee.accountInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('employee.loginAccount')">
            {{ accountLabel(selectedEmployee) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.accountEmail')">
            {{ displayValue(selectedEmployee.accountEmail) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.accountRole')">
            {{ displayValue(selectedEmployee.accountRoleName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.accountStatus')">
            {{ displayValue(selectedEmployee.accountStatus) }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-section-title">{{ settings.t('common.personalInfo') }}</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('employee.phoneNumber')">
            {{ displayValue(selectedEmployee.phoneNumber) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.personalEmail')">
            {{ displayValue(selectedEmployee.personalEmail) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.address')">
            {{ displayValue(selectedEmployee.address) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.bankAccount')">
            {{ displayValue(selectedEmployee.bankAccount) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.taxCode')">
            {{ displayValue(selectedEmployee.taxCode) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.insuranceNumber')">
            {{ displayValue(selectedEmployee.insuranceNumber) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.emergencyContact')">
            {{ displayValue(selectedEmployee.emergencyContactName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.emergencyPhone')">
            {{ displayValue(selectedEmployee.emergencyContactPhone) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.employee-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.employee-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.employee-search {
  width: 280px;
}

.employee-filter {
  width: 170px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
}

.detail-avatar {
  background: var(--vx-primary);
  color: #FFFFFF;
  font-weight: 700;
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

.form-section-switch {
  margin-left: auto;
}

.current-files {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.current-file-group {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-surface);
  padding: 10px 12px;
}

.current-file-title {
  color: var(--vx-text);
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
}

.current-file-list {
  display: grid;
  gap: 6px;
}

.current-file-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.current-file-row span {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.current-file-row small,
.current-file-empty {
  color: var(--vx-text-secondary);
  font-size: 12px;
}

:deep(.employee-dialog .el-dialog__body) {
  max-height: min(70vh, 680px);
  overflow-y: auto;
}

:deep(.employee-dialog .el-form-item) {
  margin-bottom: 12px;
}

:deep(.employee-dialog .el-form-item__label) {
  padding-bottom: 3px;
  font-weight: 600;
}

@media (max-width: 760px) {
  .employee-filters,
  .employee-search,
  .employee-filter {
    width: 100%;
  }

  .form-section {
    padding: 12px 12px 0;
  }

  .current-files {
    grid-template-columns: 1fr;
  }
}
</style>
