<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { userApi, roleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const loading = ref(false)
const users = ref([])
const roles = ref([])
const auth = useAuthStore()
const keyword = ref('')
const filterRoleId = ref(null)
const filterStatus = ref(null)
const dialogVisible = ref(false)
const dialogTitleKey = ref('')
const formRef = ref(null)
const editingId = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })

const defaultForm = { username: '', email: '', password: '', roleId: null, status: 'ACTIVE' }
const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  username: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  email: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
    { type: 'email', message: settings.t('common.emailInvalid'), trigger: 'blur' },
  ],
  password: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
  roleId: [{ required: true, message: settings.t('common.required'), trigger: 'change' }],
}))

const statusOptions = [
  { value: 'ACTIVE', labelKey: 'status.active', type: 'success' },
  { value: 'LOCKED', labelKey: 'status.locked', type: 'danger' },
]

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = { keyword: keyword.value, ...pageParams(pagination) }
    if (filterRoleId.value) params.roleId = filterRoleId.value
    if (filterStatus.value) params.status = filterStatus.value
    const [userRes, roleRes] = await Promise.all([
      userApi.list(params),
      roleApi.list(),
    ])
    applyPage(userRes.data, users, pagination)
    roles.value = roleRes.data
  } catch { ElMessage.error(settings.t('common.loadFailed')) }
  finally { loading.value = false }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  keyword.value = ''
  filterRoleId.value = null
  filterStatus.value = null
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

function roleName(id) {
  return roles.value.find(r => r.id === id)?.name || '-'
}

function openCreate() {
  Object.assign(form, defaultForm)
  editingId.value = null
  dialogTitleKey.value = 'user.add'
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    email: row.email,
    password: '',
    roleId: row.roleId,
    status: row.status,
  })
  dialogTitleKey.value = 'user.edit'
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = { ...form }
    if (editingId.value && !payload.password) {
      delete payload.password
    }
    if (editingId.value) {
      await userApi.update(editingId.value, payload)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await userApi.create(payload)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    await auth.refreshCurrentUser()
    fetchData()
  } catch (e) { ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong')) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('user.deleteConfirm')} "${row.username}"?`, settings.t('common.confirm'), { type: 'warning' })
    await userApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    await auth.refreshCurrentUser()
    fetchData()
  } catch {}
}

const exportColumns = computed(() => [
  { prop: 'username', label: settings.t('user.username') },
  { prop: 'email', label: settings.t('user.email') },
  { label: settings.t('user.role'), format: row => row.roleName || roleName(row.roleId) },
  {
    label: settings.t('employee.status'),
    format: row => settings.t(row.status === 'ACTIVE' ? 'status.active' : 'status.locked'),
  },
])

async function fetchAllUsers() {
  const params = { keyword: keyword.value, page: 1, size: 10000 }
  if (filterRoleId.value) params.roleId = filterRoleId.value
  if (filterStatus.value) params.status = filterStatus.value
  const res = await userApi.list(params)
  return Array.isArray(res.data) ? res.data : res.data.content || []
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <el-input v-model="keyword" :placeholder="settings.t('user.search')" clearable style="width:240px"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select
          v-model="filterRoleId"
          clearable
          filterable
          :placeholder="settings.t('user.role')"
          style="width:180px"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-select
          v-model="filterStatus"
          clearable
          :placeholder="settings.t('employee.status')"
          style="width:160px"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option v-for="s in statusOptions" :key="s.value" :label="settings.t(s.labelKey)" :value="s.value" />
        </el-select>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
        </el-button>
      </div>
      <div style="display:flex;gap:8px">
        <ExportActions
          filename="users"
          title="Users"
          :columns="exportColumns"
          :fetch-all="fetchAllUsers"
        />
        <el-button v-if="auth.hasPermission('USER_CREATE')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> {{ settings.t('user.add') }}
        </el-button>
      </div>
    </div>

    <el-table :data="users" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="username" :label="settings.t('user.username')" width="160">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <el-avatar :size="30" style="background:var(--vx-primary);color:white;font-size:12px">
              {{ row.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <span>{{ row.username }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="email" :label="settings.t('user.email')" min-width="220" />
      <el-table-column :label="settings.t('user.role')" width="140">
        <template #default="{ row }">
          <el-tag effect="light" size="small">{{ row.roleName || roleName(row.roleId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('employee.status')" width="130">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small" effect="light">
            {{ row.status === 'ACTIVE' ? settings.t('status.active') : settings.t('status.locked') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('common.actions')" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.hasPermission('USER_UPDATE')" text type="primary" size="small" @click="openEdit(row)"><el-icon><Edit /></el-icon></el-button>
          <el-button v-if="auth.hasPermission('USER_DELETE')" text type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon></el-button>
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
      width="640px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="editingId ? {} : rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><User /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('user.username') }} &middot; {{ settings.t('user.email') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('user.username')" prop="username">
                <el-input v-model="form.username" :disabled="!!editingId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('user.email')" prop="email">
                <el-input v-model="form.email" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item :label="editingId ? settings.t('user.newPassword') : settings.t('user.password')" prop="password">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Key /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('user.role') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('employee.status') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="12">
              <el-form-item :label="settings.t('user.role')" prop="roleId">
                <el-select v-model="form.roleId" style="width:100%" :placeholder="settings.t('user.selectRole')">
                  <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
                </el-select>
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ settings.t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
