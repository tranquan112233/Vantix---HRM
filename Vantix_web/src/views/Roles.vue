<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { roleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const settings = useSettingsStore()
const loading = ref(false)
const roles = ref([])
const allPermissions = ref([])
const auth = useAuthStore()
const keyword = ref('')
const permKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitleKey = ref('')
const formRef = ref(null)
const editingId = ref(null)
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })

const defaultForm = { name: '', description: '', permissionIds: [] }
const form = reactive({ ...defaultForm })

const rules = computed(() => ({
  name: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const [roleRes, permRes] = await Promise.all([
      roleApi.list({ keyword: keyword.value, ...pageParams(pagination) }),
      roleApi.permissions(),
    ])
    applyPage(roleRes.data, roles, pagination)
    allPermissions.value = permRes.data
  } catch { ElMessage.error(settings.t('common.loadFailed')) }
  finally { loading.value = false }
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

// Group permissions by module prefix
const permissionGroups = computed(() => {
  const groups = {}
  for (const p of allPermissions.value) {
    const parts = p.name.split('_')
    const module = parts.length > 1 ? parts.slice(0, parts.length - 1).join('_') : 'OTHER'
    if (!groups[module]) groups[module] = []
    groups[module].push(p)
  }
  return Object.entries(groups)
    .map(([name, permissions]) => ({ name, permissions }))
    .sort((a, b) => a.name.localeCompare(b.name))
})

const filteredPermissionGroups = computed(() => {
  const q = permKeyword.value.trim().toLowerCase()
  if (!q) return permissionGroups.value
  return permissionGroups.value
    .map(g => ({
      name: g.name,
      permissions: g.permissions.filter(p => p.name.toLowerCase().includes(q)),
    }))
    .filter(g => g.permissions.length > 0)
})

const allPermissionIds = computed(() => allPermissions.value.map(p => p.id))

const selectedCount = computed(() => form.permissionIds.length)

const isAllSelected = computed(() =>
  allPermissionIds.value.length > 0 &&
  form.permissionIds.length === allPermissionIds.value.length
)

const isAllIndeterminate = computed(() =>
  form.permissionIds.length > 0 && !isAllSelected.value
)

function groupSelectedCount(group) {
  return group.permissions.filter(p => form.permissionIds.includes(p.id)).length
}

function isGroupAllSelected(group) {
  return group.permissions.length > 0 && groupSelectedCount(group) === group.permissions.length
}

function isGroupIndeterminate(group) {
  const count = groupSelectedCount(group)
  return count > 0 && count < group.permissions.length
}

function toggleGroup(group, checked) {
  const ids = group.permissions.map(p => p.id)
  if (checked) {
    const merged = new Set([...form.permissionIds, ...ids])
    form.permissionIds = Array.from(merged)
  } else {
    form.permissionIds = form.permissionIds.filter(id => !ids.includes(id))
  }
}

function toggleAll(checked) {
  form.permissionIds = checked ? [...allPermissionIds.value] : []
}

function openCreate() {
  Object.assign(form, { ...defaultForm, permissionIds: [] })
  permKeyword.value = ''
  editingId.value = null
  dialogTitleKey.value = 'role.add'
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    description: row.description || '',
    permissionIds: (row.permissions || []).map(p => p.id),
  })
  permKeyword.value = ''
  dialogTitleKey.value = 'role.edit'
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await roleApi.update(editingId.value, form)
      ElMessage.success(settings.t('common.updated'))
    } else {
      await roleApi.create(form)
      ElMessage.success(settings.t('common.created'))
    }
    dialogVisible.value = false
    await auth.refreshCurrentUser()
    fetchData()
  } catch (e) { ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong')) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`${settings.t('role.deleteConfirm')} "${row.name}"?`, settings.t('common.confirm'), { type: 'warning' })
    await roleApi.delete(row.id)
    ElMessage.success(settings.t('common.deleted'))
    await auth.refreshCurrentUser()
    fetchData()
  } catch {}
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <el-input v-model="keyword" :placeholder="settings.t('role.search')" clearable style="width:300px"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
        </el-button>
      </div>
      <el-button v-if="auth.hasPermission('ROLE_CREATE')" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> {{ settings.t('role.add') }}
      </el-button>
    </div>

    <el-table :data="roles" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column prop="name" :label="settings.t('role.name')" width="160" />
      <el-table-column prop="description" :label="settings.t('common.description')" min-width="250" />
      <el-table-column :label="settings.t('role.permissions')" min-width="300">
        <template #default="{ row }">
          <div style="display:flex;flex-wrap:wrap;gap:4px">
            <el-tag v-for="p in (row.permissions || []).slice(0, 5)" :key="p.id" size="small" effect="plain">
              {{ p.name }}
            </el-tag>
            <el-tag v-if="(row.permissions || []).length > 5" size="small" type="info" effect="plain">
              +{{ row.permissions.length - 5 }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('common.actions')" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="auth.hasPermission('ROLE_UPDATE')" text type="primary" size="small" @click="openEdit(row)"><el-icon><Edit /></el-icon></el-button>
          <el-button v-if="auth.hasPermission('ROLE_DELETE')" text type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon></el-button>
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
      width="760px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Key /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('role.name') }} &middot; {{ settings.t('common.description') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('role.name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item :label="settings.t('common.description')">
            <el-input v-model="form.description" type="textarea" :rows="2" />
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Lock /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('role.permissions') }}</h4>
              <p class="form-section-subtitle">{{ selectedCount }} / {{ allPermissionIds.length }}</p>
            </div>
          </div>
          <el-form-item>
            <div class="permission-toolbar">
              <el-input
                v-model="permKeyword"
                :placeholder="settings.t('role.searchPermission')"
                clearable
                size="small"
                style="width:260px"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-checkbox
                :model-value="isAllSelected"
                :indeterminate="isAllIndeterminate"
                @change="toggleAll"
              >
                {{ settings.t('role.selectAll') }}
              </el-checkbox>
              <span class="permission-count">
                {{ selectedCount }} / {{ allPermissionIds.length }}
              </span>
            </div>
            <div class="permission-groups">
              <div v-for="group in filteredPermissionGroups" :key="group.name" class="perm-group">
                <div class="perm-group-header">
                  <el-checkbox
                    :model-value="isGroupAllSelected(group)"
                    :indeterminate="isGroupIndeterminate(group)"
                    @change="(checked) => toggleGroup(group, checked)"
                  >
                    <span class="perm-group-title">{{ group.name }}</span>
                  </el-checkbox>
                  <span class="perm-group-count">
                    {{ groupSelectedCount(group) }} / {{ group.permissions.length }}
                  </span>
                </div>
                <el-checkbox-group v-model="form.permissionIds" class="perm-group-items">
                  <el-checkbox v-for="p in group.permissions" :key="p.id" :value="p.id" :label="p.name" />
                </el-checkbox-group>
              </div>
              <el-empty
                v-if="!filteredPermissionGroups.length"
                :description="settings.t('common.noData')"
                :image-size="60"
              />
            </div>
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

<style scoped>
.permission-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
  width: 100%;
}
.permission-count {
  margin-left: auto;
  font-size: var(--vx-font-size-xs);
  color: var(--vx-text-secondary);
  font-variant-numeric: tabular-nums;
}
.permission-groups {
  width: 100%;
  max-height: 360px;
  overflow-y: auto;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 12px 16px;
}
.perm-group {
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--vx-border);
}
.perm-group:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.perm-group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.perm-group-count {
  margin-left: auto;
  font-size: var(--vx-font-size-2xs);
  color: var(--vx-text-secondary);
  font-variant-numeric: tabular-nums;
}
.perm-group-title {
  font-size: var(--vx-font-size-sm);
  font-weight: 600;
  color: var(--vx-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.perm-group-items {
  padding-left: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
}
</style>
