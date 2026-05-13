<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { departmentApi, employeeApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const auth = useAuthStore()
const loading = ref(false)
const departments = ref([])
const employees = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitleKey = ref('')
const formRef = ref(null)
const editingId = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })
const canLoadEmployees = computed(() => auth.hasPermission('EMPLOYEE_VIEW'))

const defaultForm = { code: '', name: '', description: '', headEmployeeId: null }
const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  code: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  name: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

const headCandidates = computed(() =>
  employees.value.filter(employee => !['RESIGNED', 'TERMINATED'].includes(employee.status))
)

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const departmentRes = await departmentApi.list({ keyword: keyword.value, ...pageParams(pagination) })
    applyPage(departmentRes.data, departments, pagination)
    loadEmployees()
  } catch { ElMessage.error(settings.t('common.loadFailed')) }
  finally { loading.value = false }
}

async function loadEmployees() {
  if (!canLoadEmployees.value) {
    employees.value = []
    return
  }

  try {
    const employeeRes = await employeeApi.list()
    employees.value = Array.isArray(employeeRes.data) ? employeeRes.data : employeeRes.data.content || []
  } catch {
    employees.value = []
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  keyword.value = ''
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

function openCreate() {
  Object.assign(form, defaultForm)
  editingId.value = null
  dialogTitleKey.value = 'department.add'
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    code: row.code,
    name: row.name,
    description: row.description || '',
    headEmployeeId: row.headEmployeeId || null,
  })
  dialogTitleKey.value = 'department.edit'
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await departmentApi.update(editingId.value, form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await departmentApi.create(form)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong')) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('department.deleteConfirm')} "${row.name}"?`, settings.t('common.confirm'), { type: 'warning' })
    await departmentApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    fetchData()
  } catch {}
}

function employeeLabel(employee) {
  return `${employee.fullName} (${employee.employeeCode})`
}

const exportColumns = computed(() => [
  { prop: 'code', label: settings.t('department.code') },
  { prop: 'name', label: settings.t('department.name') },
  { label: settings.t('department.head'), format: row => headEmployeeName(row) },
  { prop: 'description', label: settings.t('common.description') },
])

async function fetchAllDepartments() {
  const res = await departmentApi.list({ keyword: keyword.value, page: 1, size: 10000 })
  return Array.isArray(res.data) ? res.data : res.data.content || []
}

function headEmployeeName(row) {
  if (row.headEmployeeName) {
    return `${row.headEmployeeName}${row.headEmployeeCode ? ` (${row.headEmployeeCode})` : ''}`
  }

  const employee = employees.value.find(item => item.id === row.headEmployeeId)
  return employee ? employeeLabel(employee) : '-'
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <el-input v-model="keyword" :placeholder="settings.t('department.search')" clearable style="width:300px"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
        </el-button>
      </div>
      <div style="display:flex;gap:8px">
        <ExportActions
          filename="departments"
          title="Departments"
          :columns="exportColumns"
          :fetch-all="fetchAllDepartments"
        />
        <el-button v-if="auth.hasPermission('DEPARTMENT_CREATE')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> {{ settings.t('department.add') }}
        </el-button>
      </div>
    </div>

    <el-table :data="departments" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="code" :label="settings.t('department.code')" width="120" />
      <el-table-column prop="name" :label="settings.t('department.name')" min-width="200" />
      <el-table-column :label="settings.t('department.head')" min-width="220">
        <template #default="{ row }">
          <span>{{ headEmployeeName(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="settings.t('common.description')" min-width="250" />
      <el-table-column :label="settings.t('common.actions')" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.hasPermission('DEPARTMENT_UPDATE')" text type="primary" size="small" @click="openEdit(row)"><el-icon><Edit /></el-icon></el-button>
          <el-button v-if="auth.hasPermission('DEPARTMENT_DELETE')" text type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon></el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="settings.t(dialogTitleKey)"
      width="620px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><OfficeBuilding /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('department.name') }} &middot; {{ settings.t('department.departmentCode') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="10">
              <el-form-item :label="settings.t('department.departmentCode')" prop="code">
                <el-input v-model="form.code" :disabled="!!editingId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="14">
              <el-form-item :label="settings.t('department.name')" prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.additionalInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('department.head') }} &middot; {{ settings.t('common.description') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('department.head')">
            <el-select
              v-model="form.headEmployeeId"
              clearable
              filterable
              style="width:100%"
              :placeholder="settings.t('department.selectEmployee')"
            >
              <el-option
                v-for="employee in headCandidates"
                :key="employee.id"
                :label="employeeLabel(employee)"
                :value="employee.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="settings.t('common.description')">
            <el-input v-model="form.description" type="textarea" :rows="3" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
