<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { positionApi, departmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const auth = useAuthStore()
const loading = ref(false)
const positions = ref([])
const departments = ref([])
const keyword = ref('')
const filterDepartmentId = ref(null)
const dialogVisible = ref(false)
const dialogTitleKey = ref('')
const formRef = ref(null)
const editingId = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })
const canLoadDepartments = computed(() => auth.hasPermission('DEPARTMENT_VIEW'))

const defaultForm = { code: '', name: '', description: '', departmentId: null }
const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  code: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  name: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = { keyword: keyword.value, ...pageParams(pagination) }
    if (filterDepartmentId.value) params.departmentId = filterDepartmentId.value
    const posRes = await positionApi.list(params)
    applyPage(posRes.data, positions, pagination)
    loadDepartments()
  } catch { ElMessage.error(settings.t('common.loadFailed')) }
  finally { loading.value = false }
}

async function loadDepartments() {
  if (!canLoadDepartments.value) {
    departments.value = []
    return
  }

  try {
    const deptRes = await departmentApi.list()
    departments.value = Array.isArray(deptRes.data) ? deptRes.data : deptRes.data.content || []
  } catch {
    departments.value = []
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  keyword.value = ''
  filterDepartmentId.value = null
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

function deptName(id) {
  return departments.value.find(d => d.id === id)?.name || '-'
}

function openCreate() {
  Object.assign(form, defaultForm)
  editingId.value = null
  dialogTitleKey.value = 'position.add'
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { code: row.code, name: row.name, description: row.description || '', departmentId: row.departmentId })
  dialogTitleKey.value = 'position.edit'
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await positionApi.update(editingId.value, form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await positionApi.create(form)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong')) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('position.deleteConfirm')} "${row.name}"?`, settings.t('common.confirm'), { type: 'warning' })
    await positionApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    fetchData()
  } catch {}
}

const exportColumns = computed(() => [
  { prop: 'code', label: settings.t('position.code') },
  { prop: 'name', label: settings.t('position.name') },
  { label: settings.t('employee.department'), format: row => deptName(row.departmentId) },
  { prop: 'description', label: settings.t('common.description') },
])

async function fetchAllPositions() {
  const params = { keyword: keyword.value, page: 1, size: 10000 }
  if (filterDepartmentId.value) params.departmentId = filterDepartmentId.value
  const res = await positionApi.list(params)
  return Array.isArray(res.data) ? res.data : res.data.content || []
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <el-input v-model="keyword" :placeholder="settings.t('position.search')" clearable style="width:260px"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select
          v-model="filterDepartmentId"
          clearable
          filterable
          :placeholder="settings.t('employee.selectDepartment')"
          style="width:220px"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
        </el-button>
      </div>
      <div style="display:flex;gap:8px">
        <ExportActions
          filename="positions"
          title="Positions"
          :columns="exportColumns"
          :fetch-all="fetchAllPositions"
        />
        <el-button v-if="auth.hasPermission('POSITION_CREATE')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> {{ settings.t('position.add') }}
        </el-button>
      </div>
    </div>

    <el-table :data="positions" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="code" :label="settings.t('position.code')" width="120" />
      <el-table-column prop="name" :label="settings.t('position.name')" min-width="200" />
      <el-table-column :label="settings.t('employee.department')" min-width="180">
        <template #default="{ row }">{{ deptName(row.departmentId) }}</template>
      </el-table-column>
      <el-table-column prop="description" :label="settings.t('common.description')" min-width="200" />
      <el-table-column :label="settings.t('common.actions')" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.hasPermission('POSITION_UPDATE')" text type="primary" size="small" @click="openEdit(row)"><el-icon><Edit /></el-icon></el-button>
          <el-button v-if="auth.hasPermission('POSITION_DELETE')" text type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon></el-button>
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
            <el-icon class="form-section-icon"><Postcard /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('position.name') }} &middot; {{ settings.t('position.positionCode') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="10">
              <el-form-item :label="settings.t('position.positionCode')" prop="code">
                <el-input v-model="form.code" :disabled="!!editingId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="14">
              <el-form-item :label="settings.t('position.name')" prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="settings.t('employee.department')">
            <el-select v-model="form.departmentId" clearable style="width:100%" :placeholder="settings.t('employee.selectDepartment')">
              <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.additionalInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('common.description') }}</p>
            </div>
          </div>
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
