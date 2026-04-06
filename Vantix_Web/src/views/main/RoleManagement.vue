<template>
  <div class="role-management">

    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Roles</h1>
        <p class="page-desc">{{ pagination.totalElements }} roles · {{ allPermissions.length }} permissions · {{ groupedPermissions.length }} groups</p>
      </div>
      <div class="header-actions">
        <button v-if="canCreate" class="btn-primary" data-bs-toggle="modal" data-bs-target="#roleModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> New Role
        </button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <i class="bi bi-search"></i>
        <input v-model="filters.keyword" type="text" placeholder="Search role name or description..." @input="onSearch" />
        <button v-if="filters.keyword" class="clear-btn" @click="clearSearch"><i class="bi bi-x"></i></button>
      </div>
      <div class="select-wrapper">
        <select v-model.number="pagination.size" @change="onSizeChange">
          <option :value="10">10 / page</option>
          <option :value="20">20 / page</option>
          <option :value="50">50 / page</option>
          <option :value="100">100 / page</option>
        </select>
        <i class="bi bi-chevron-down"></i>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card">
      <div v-if="loading" class="state-center">
        <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
        <span>Loading...</span>
      </div>

      <div v-else-if="roles.length === 0" class="state-center">
        <i class="bi bi-shield empty-icon"></i>
        <p class="empty-title">No roles found</p>
        <p class="empty-sub">Create a role to start assigning permissions.</p>
        <button class="btn-primary" data-bs-toggle="modal" data-bs-target="#roleModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Create role
        </button>
      </div>

      <div v-else>
        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th class="th-num">#</th>
              <th class="sortable" @click="toggleSort('name')">Role <i :class="'bi ' + getSortIcon('name')"></i></th>
              <th class="sortable" @click="toggleSort('description')">Description <i :class="'bi ' + getSortIcon('description')"></i></th>
              <th>Permissions</th>
              <th class="sortable" @click="toggleSort('createdAt')">Created <i :class="'bi ' + getSortIcon('createdAt')"></i></th>
              <th class="th-actions">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(role, index) in roles" :key="role.id">
              <td class="td-num">{{ pagination.page * pagination.size + index + 1 }}</td>
              <td>
                <div class="role-cell">
                  <div class="avatar" :style="{ background: avatarGradient(role.name) }">{{ getInitials(role.name) }}</div>
                  <div>
                    <div class="role-name">{{ role.name }}</div>
                  </div>
                </div>
              </td>
              <td class="td-desc">{{ role.description || '—' }}</td>
              <td>
                <div class="perm-list">
                  <span v-for="perm in role.permissions?.slice(0, 3)" :key="perm" class="perm-tag">{{ formatShortPermission(perm) }}</span>
                  <span v-if="role.permissions?.length > 3" class="perm-tag perm-more">+{{ role.permissions.length - 3 }}</span>
                  <span v-if="!role.permissions?.length" class="text-muted">—</span>
                </div>
              </td>
              <td class="td-meta">{{ formatDate(role.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="canEdit" class="icon-btn" data-bs-toggle="modal" data-bs-target="#roleModal" @click="openEdit(role)" title="Edit">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button v-if="canDelete" class="icon-btn danger" data-bs-toggle="modal" data-bs-target="#deleteRoleModal" @click="confirmDelete(role)" title="Delete">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="pagination.totalPages > 0" class="pagination-bar">
          <span class="pagination-info">{{ startEntry }}–{{ endEntry }} of {{ pagination.totalElements }}</span>
          <div class="page-controls">
            <button :disabled="pagination.page === 0" @click="goPage(0)"><i class="bi bi-chevron-double-left"></i></button>
            <button :disabled="pagination.page === 0" @click="goPage(pagination.page - 1)"><i class="bi bi-chevron-left"></i></button>
            <button v-for="p in visiblePages" :key="p" :class="{ active: p === pagination.page }" @click="goPage(p)">{{ p + 1 }}</button>
            <button :disabled="pagination.last" @click="goPage(pagination.page + 1)"><i class="bi bi-chevron-right"></i></button>
            <button :disabled="pagination.last" @click="goPage(pagination.totalPages - 1)"><i class="bi bi-chevron-double-right"></i></button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div class="modal fade" id="roleModal" tabindex="-1" aria-labelledby="roleModalLabel" aria-hidden="true" ref="roleModalEl" data-bs-backdrop="static">
      <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title" id="roleModalLabel">{{ modal.isEdit ? 'Edit Role' : 'New Role' }}</h5>
              <p class="modal-subtitle">{{ modal.isEdit ? 'Update role and permissions' : 'Create a new role with permissions' }}</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="modal-body-custom">
            <div v-if="errors.general" class="alert-error">
              <i class="bi bi-exclamation-triangle"></i> {{ errors.general }}
            </div>

            <div class="field" :class="{ 'field-error': errors.name }">
              <label>Role Name <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-tag"></i>
                <input v-model="form.name" type="text" placeholder="Enter role name" @input="clearError('name')" />
              </div>
              <span v-if="errors.name" class="err-msg">{{ errors.name }}</span>
            </div>

            <div class="field">
              <label>Description</label>
              <textarea v-model="form.description" rows="2" class="textarea-field" placeholder="Describe what this role can do..."></textarea>
            </div>

            <div class="field" :class="{ 'field-error': errors.permissionIds }">
              <label>Permissions <span class="req">*</span></label>

              <div class="perms-panel">
                <div class="perms-panel-header">
                  <span class="perms-count"><strong>{{ form.permissionIds?.length || 0 }}</strong> selected</span>
                  <button type="button" class="link-btn" @click="toggleAllPermissions">
                    {{ allPermissionsSelected ? 'Deselect All' : 'Select All' }}
                  </button>
                </div>

                <div class="perms-search">
                  <i class="bi bi-search"></i>
                  <input v-model="permissionSearch" type="text" placeholder="Search permissions..." />
                </div>

                <div class="perms-list">
                  <div v-if="filteredGroupedPermissions.length === 0" class="no-results">
                    <i class="bi bi-search"></i>
                    <p>No permissions found</p>
                  </div>

                  <div v-for="group in filteredGroupedPermissions" :key="group.name" class="perm-group">
                    <div class="group-row" @click="toggleGroup(group.name)">
                      <input type="checkbox" :checked="isGroupFullySelected(group)" :indeterminate="isGroupPartiallySelected(group)" @click.stop="toggleGroupSelection(group)" />
                      <div class="group-icon"><i :class="group.icon"></i></div>
                      <span class="group-name">{{ group.displayName }}</span>
                      <span class="group-count" :class="{ active: getGroupSelectedCount(group) > 0 }">{{ getGroupSelectedCount(group) }}/{{ group.permissions.length }}</span>
                      <i class="bi bi-chevron-down group-chevron" :class="{ rotated: isGroupExpanded(group.name) }"></i>
                    </div>

                    <transition name="expand">
                      <div v-show="isGroupExpanded(group.name)" class="group-items">
                        <label v-for="perm in group.permissions" :key="perm.id" class="perm-item">
                          <input type="checkbox" :value="perm.id" v-model="form.permissionIds" />
                          <div class="perm-info">
                            <span class="perm-name">{{ formatPermissionName(perm.name) }}</span>
                            <span v-if="perm.description" class="perm-desc">{{ perm.description }}</span>
                          </div>
                        </label>
                      </div>
                    </transition>
                  </div>
                </div>
              </div>
              <span v-if="errors.permissionIds" class="err-msg">{{ errors.permissionIds }}</span>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn-primary" :disabled="submitting" @click="submitForm">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else>
                <i :class="modal.isEdit ? 'bi bi-check-lg' : 'bi bi-plus-lg'"></i>
                {{ modal.isEdit ? 'Save Changes' : 'Create Role' }}
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal fade" id="deleteRoleModal" tabindex="-1" aria-hidden="true" ref="deleteModalEl">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">Delete Role</h5>
              <p class="modal-subtitle">This action cannot be undone</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>
          <div class="modal-body-custom delete-body">
            <div class="del-avatar" :style="deleteModal.role ? { background: avatarGradient(deleteModal.role.name) } : {}">
              {{ deleteModal.role ? getInitials(deleteModal.role.name) : '' }}
            </div>
            <p class="del-name">{{ deleteModal.role?.name }}</p>
            <p class="del-sub">{{ deleteModal.role?.description || 'No description' }}</p>
            <p class="del-warn">Permanently delete this role?</p>
          </div>
          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn-danger" :disabled="submitting" @click="doDelete">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else><i class="bi bi-trash"></i> Delete</template>
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Modal } from 'bootstrap'
import roleService from '@/services/role.service.js'
import permissionService from '@/services/permission.service.js'
import { useToast } from 'vue-toastification'
import { useAuthStore } from '@/stores/auth.store.js'

const toast = useToast()
const auth = useAuthStore()

// ==================== PERMISSIONS ====================
const canView = computed(() => auth.can('ROLE_VIEW'))
const canCreate = computed(() => auth.can('ROLE_CREATE'))
const canEdit = computed(() => auth.can('ROLE_UPDATE'))
const canDelete = computed(() => auth.can('ROLE_DELETE'))

// ==================== STATE ====================
const roles = ref([])
const allPermissions = ref([])
const loading = ref(false)
const submitting = ref(false)
const showExportDropdown = ref(false)
const exportDropdown = ref(null)
const expandedGroups = ref(['User Management', 'Role Management', 'Employee Management', 'Department Management'])
const permissionSearch = ref('')

// Modal refs
const roleModalEl = ref(null)
const deleteModalEl = ref(null)
let bsRoleModal = null
let bsDeleteModal = null

// Store the previously focused element to restore focus when modal closes
let previouslyFocusedElement = null

// Filters & Pagination
const filters = reactive({ keyword: '' })
const sort = reactive({ sortBy: 'createdAt', sortDir: 'desc' })
const pagination = reactive({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: false })

// Form state
const modal = reactive({ isEdit: false, roleId: null })
const errors = reactive({})
const form = reactive({ name: '', description: '', permissionIds: [] })
const deleteModal = reactive({ role: null })

// ==================== FOCUS MANAGEMENT ====================
function saveFocus() {
  previouslyFocusedElement = document.activeElement
}

function restoreFocus() {
  if (previouslyFocusedElement && previouslyFocusedElement.focus) {
    nextTick(() => {
      previouslyFocusedElement.focus()
      previouslyFocusedElement = null
    })
  }
}

// ==================== MODAL HANDLERS ====================
function openCreate() {
  saveFocus()
  resetForm()
  modal.isEdit = false
  modal.roleId = null

  // Set focus after the modal is shown
  if (bsRoleModal) {
    bsRoleModal.show()
    setTimeout(() => {
      const modalElement = roleModalEl.value
      if (modalElement) {
        const nameInput = modalElement.querySelector('input[placeholder="Enter role name"]')
        if (nameInput) nameInput.focus()
      }
    }, 150)
  }
}

function openEdit(role) {
  saveFocus()
  resetForm()
  form.name = role.name
  form.description = role.description || ''

  // Map permission names to IDs
  const permissionNameToId = new Map(allPermissions.value.map(p => [p.name, p.id]))
  form.permissionIds = (role.permissions || [])
      .map(name => permissionNameToId.get(name))
      .filter(id => id !== undefined)

  modal.isEdit = true
  modal.roleId = role.id

  if (bsRoleModal) {
    bsRoleModal.show()
    setTimeout(() => {
      const modalElement = roleModalEl.value
      if (modalElement) {
        const nameInput = modalElement.querySelector('input[placeholder="Enter role name"]')
        if (nameInput) nameInput.focus()
      }
    }, 150)
  }
}

function confirmDelete(role) {
  saveFocus()
  deleteModal.role = role
  if (bsDeleteModal) {
    bsDeleteModal.show()
    setTimeout(() => {
      const modalElement = deleteModalEl.value
      if (modalElement) {
        const cancelBtn = modalElement.querySelector('.btn-ghost')
        if (cancelBtn) cancelBtn.focus()
      }
    }, 150)
  }
}

// ==================== SORT FUNCTIONS ====================
function toggleSort(column) {
  if (sort.sortBy === column) {
    sort.sortDir = sort.sortDir === 'desc' ? 'asc' : 'desc'
  } else {
    sort.sortBy = column
    sort.sortDir = 'desc'
  }
  fetchRoles()
}

function getSortIcon(column) {
  if (sort.sortBy !== column) return 'bi-arrow-down-up'
  return sort.sortDir === 'desc' ? 'bi-arrow-down' : 'bi-arrow-up'
}

// ==================== PERMISSION GROUPS CONFIG ====================
// Thứ tự quan trọng: prefix dài hơn phải đặt trước prefix ngắn hơn có cùng root
// (ví dụ: CONTRACT_ANNEX trước CONTRACT, LEAVE_TYPE trước LEAVE)
const PERM_GROUPS = [
  { prefix: 'USER',       displayName: 'User Management',       icon: 'bi bi-people-fill' },
  { prefix: 'PERMISSION', displayName: 'Permission Management', icon: 'bi bi-key-fill' },
  { prefix: 'ROLE',       displayName: 'Role Management',       icon: 'bi bi-shield-lock-fill' },
  { prefix: 'EMPLOYEE',   displayName: 'Employee Management',   icon: 'bi bi-person-badge-fill' },
  { prefix: 'DEPARTMENT', displayName: 'Department Management', icon: 'bi bi-diagram-3-fill' },
  { prefix: 'POSITION',   displayName: 'Position Management',   icon: 'bi bi-person-gear' },
  { prefix: 'ATTENDANCE', displayName: 'Attendance',            icon: 'bi bi-calendar-check-fill' },
  { prefix: 'LEAVE',      displayName: 'Leave Management',      icon: 'bi bi-calendar-x-fill' },
  { prefix: 'CONTRACT',   displayName: 'Contract Management',   icon: 'bi bi-file-text-fill' },
  { prefix: 'SCHEDULE',   displayName: 'Schedule Management',   icon: 'bi bi-calendar3' },
  { prefix: 'SALARY',     displayName: 'Payroll & Salary',      icon: 'bi bi-cash-coin' },
  { prefix: 'REPORT',     displayName: 'Reports',               icon: 'bi bi-bar-chart-fill' },
  { prefix: 'SYSTEM',     displayName: 'System Configuration',  icon: 'bi bi-gear-fill' },
]

// ==================== COMPUTED ====================
const groupedPermissions = computed(() => {
  // Map<prefix → group object> — dùng Map để giữ thứ tự chèn
  const groupMap = new Map(
    PERM_GROUPS.map(g => [
      g.prefix,
      { name: g.displayName, displayName: g.displayName, icon: g.icon, permissions: [] }
    ])
  )
  const otherGroup = { name: 'Other', displayName: 'Other', icon: 'bi bi-grid-3x3-gap-fill', permissions: [] }

  allPermissions.value.forEach(perm => {
    const upper = perm.name.toUpperCase()
    let matched = false
    for (const [prefix, group] of groupMap) {
      // Kiểm tra prefix + '_' để tránh nhầm lẫn giữa ROLE và REPORT
      if (upper === prefix || upper.startsWith(prefix + '_')) {
        group.permissions.push(perm)
        matched = true
        break
      }
    }
    if (!matched) otherGroup.permissions.push(perm)
  })

  const result = [...groupMap.values(), otherGroup].filter(g => g.permissions.length > 0)
  result.forEach(g => g.permissions.sort((a, b) => a.name.localeCompare(b.name)))
  return result
})

const filteredGroupedPermissions = computed(() => {
  if (!permissionSearch.value.trim()) return groupedPermissions.value
  const term = permissionSearch.value.toLowerCase().trim()
  return groupedPermissions.value
      .map(group => ({
        ...group,
        permissions: group.permissions.filter(perm =>
            perm.name.toLowerCase().includes(term) ||
            (perm.description && perm.description.toLowerCase().includes(term))
        )
      }))
      .filter(group => group.permissions.length > 0)
})

const allPermissionsSelected = computed(() =>
    form.permissionIds?.length === allPermissions.value.length && allPermissions.value.length > 0
)

const startEntry = computed(() =>
    pagination.totalElements === 0 ? 0 : pagination.page * pagination.size + 1
)

const endEntry = computed(() =>
    Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)
)

const visiblePages = computed(() => {
  const delta = 2
  const start = Math.max(0, pagination.page - delta)
  const end = Math.min(pagination.totalPages - 1, pagination.page + delta)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// ==================== PERMISSION GROUP HANDLERS ====================
function isGroupExpanded(groupName) {
  return expandedGroups.value.includes(groupName)
}

function toggleGroup(groupName) {
  const index = expandedGroups.value.indexOf(groupName)
  if (index > -1) expandedGroups.value.splice(index, 1)
  else expandedGroups.value.push(groupName)
}

function getGroupSelectedCount(group) {
  if (!form.permissionIds?.length) return 0
  const groupIds = new Set(group.permissions.map(p => p.id))
  return form.permissionIds.filter(id => groupIds.has(id)).length
}

function isGroupFullySelected(group) {
  if (!form.permissionIds?.length) return false
  return group.permissions.map(p => p.id).every(id => form.permissionIds.includes(id))
}

function isGroupPartiallySelected(group) {
  if (!form.permissionIds?.length) return false
  const groupIds = group.permissions.map(p => p.id)
  const selected = groupIds.filter(id => form.permissionIds.includes(id)).length
  return selected > 0 && selected < groupIds.length
}

function toggleGroupSelection(group) {
  const groupIds = group.permissions.map(p => p.id)
  if (isGroupFullySelected(group)) {
    form.permissionIds = form.permissionIds.filter(id => !groupIds.includes(id))
  } else {
    form.permissionIds = [...new Set([...form.permissionIds, ...groupIds])]
  }
}

function toggleAllPermissions() {
  if (allPermissionsSelected.value) {
    form.permissionIds = []
  } else {
    form.permissionIds = allPermissions.value.map(p => p.id)
  }
}

// ==================== FORM HANDLERS ====================
function resetForm() {
  form.name = ''
  form.description = ''
  form.permissionIds = []
  permissionSearch.value = ''
  Object.keys(errors).forEach(key => delete errors[key])
}

function clearError(field) {
  delete errors[field]
}

// ==================== API CALLS ====================
async function fetchAllPermissions() {
  try {
    const response = await permissionService.getAll({ page: 0, size: 1000 })
    allPermissions.value = response.data?.content || response.data?.data || response.data || []
  } catch (error) {
    toast.error('Failed to load permissions')
    console.error(error)
  }
}

async function fetchRoles() {
  if (!canView.value) return

  loading.value = true
  try {
    const { data } = await roleService.getAll({
      keyword: filters.keyword || undefined,
      page: pagination.page,
      size: pagination.size,
      sortBy: sort.sortBy,
      sortDir: sort.sortDir
    })
    roles.value = data.content || []
    pagination.totalElements = data.totalElements || 0
    pagination.totalPages = data.totalPages || 0
    pagination.last = data.last || false
  } catch (error) {
    toast.error('Failed to load roles')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  // Check permissions before submit
  if (modal.isEdit && !canEdit.value) {
    toast.error('You do not have permission to edit roles')
    return
  }
  if (!modal.isEdit && !canCreate.value) {
    toast.error('You do not have permission to create roles')
    return
  }

  submitting.value = true
  Object.keys(errors).forEach(k => delete errors[k])

  try {
    if (modal.isEdit) {
      await roleService.update(modal.roleId, {
        name: form.name,
        description: form.description,
        permissionIds: form.permissionIds
      })

      // Update permissions for current user if their role was modified
      const currentUserRole = auth.user?.role
      if (currentUserRole === form.name) {
        await auth.refreshPermissions()
        toast.info('Your permissions have been updated. Some features may change.', {
          timeout: 5000
        })
      }

      toast.success('Role updated successfully')
    } else {
      await roleService.create({
        name: form.name,
        description: form.description,
        permissionIds: form.permissionIds
      })
      toast.success('Role created successfully')
    }

    pagination.page = 0
    await fetchRoles()
    bsRoleModal.hide()

  } catch (err) {
    if (err?.errors) {
      Object.assign(errors, err.errors)
    } else if (err?.status === 403) {
      errors.general = 'You do not have permission to perform this action'
      toast.error('Permission denied')
    } else {
      errors.general = err?.message || 'Something went wrong'
      toast.error(errors.general)
    }
  } finally {
    submitting.value = false
  }
}

async function doDelete() {
  // Check delete permission
  if (!canDelete.value) {
    toast.error('You do not have permission to delete roles')
    return
  }

  submitting.value = true
  try {
    await roleService.delete(deleteModal.role.id)

    // Check if current user's role is being deleted
    if (auth.user?.role === deleteModal.role.name) {
      await auth.refreshPermissions()
      toast.warning('Your role has been deleted. You will be logged out.', {
        timeout: 3000
      })
      setTimeout(() => auth.logout(), 3000)
    }

    await fetchRoles()
    toast.success(`Role ${deleteModal.role.name} deleted`)
    bsDeleteModal.hide()

  } catch (error) {
    if (error?.status === 403) {
      toast.error('You do not have permission to delete roles')
    } else {
      toast.error('Failed to delete role')
    }
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// ==================== UTILITIES ====================
let searchTimer = null
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 0
    fetchRoles()
  }, 400)
}

function clearSearch() {
  filters.keyword = ''
  pagination.page = 0
  fetchRoles()
}

function goPage(page) {
  pagination.page = page
  fetchRoles()
}

function onSizeChange() {
  pagination.page = 0
  fetchRoles()
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function formatShortPermission(name) {
  return name
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase())
      .substring(0, 15)
}

function formatPermissionName(name) {
  return name
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase())
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

function toggleExportDropdown() {
  showExportDropdown.value = !showExportDropdown.value
}

async function exportRoles(format) {
  showExportDropdown.value = false
  toast.info(`Exporting as ${format.toUpperCase()}...`)
  // Implement export logic here
}

// ==================== CLICK OUTSIDE ====================
const handleClickOutside = (e) => {
  if (exportDropdown.value && !exportDropdown.value.contains(e.target)) {
    showExportDropdown.value = false
  }
}

// ==================== AVATAR GRADIENTS ====================
const GRADIENTS = [
  'linear-gradient(135deg,#6366f1,#818cf8)',
  'linear-gradient(135deg,#6366f1,#a5b4fc)',
  'linear-gradient(135deg,#6366f1,#93c5fd)',
  'linear-gradient(135deg,#6366f1,#a5f3fc)',
  'linear-gradient(135deg,#6366f1,#f9a8d4)',
  'linear-gradient(135deg,#6366f1,#fdba74)',
  'linear-gradient(135deg,#6366f1,#c4b5fd)'
]

function avatarGradient(name) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return GRADIENTS[Math.abs(hash) % GRADIENTS.length]
}

// ==================== LIFECYCLE ====================
onMounted(() => {
  // Check view permission
  if (!canView.value) {
    toast.error('You do not have permission to view roles')
    return
  }

  document.addEventListener('click', handleClickOutside)

  // Initialize Bootstrap modals
  if (roleModalEl.value) {
    bsRoleModal = new Modal(roleModalEl.value)

    // Handle modal show event
    roleModalEl.value.addEventListener('show.bs.modal', () => {
      saveFocus()
    })

    // Handle modal hide event
    roleModalEl.value.addEventListener('hidden.bs.modal', () => {
      resetForm()
      restoreFocus()
    })
  }

  if (deleteModalEl.value) {
    bsDeleteModal = new Modal(deleteModalEl.value)

    deleteModalEl.value.addEventListener('hidden.bs.modal', () => {
      restoreFocus()
    })
  }

  fetchRoles()
  fetchAllPermissions()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)

  // Clean up modal event listeners
  if (roleModalEl.value) {
    roleModalEl.value.removeEventListener('hidden.bs.modal', resetForm)
    roleModalEl.value.removeEventListener('hidden.bs.modal', restoreFocus)
    roleModalEl.value.removeEventListener('show.bs.modal', saveFocus)
  }
  if (deleteModalEl.value) {
    deleteModalEl.value.removeEventListener('hidden.bs.modal', restoreFocus)
  }

  if (bsRoleModal) bsRoleModal.dispose()
  if (bsDeleteModal) bsDeleteModal.dispose()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }

.role-management {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
}

.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 24px; font-weight: 700; letter-spacing: -0.5px; color: #111; }
.page-desc { font-size: 13px; color: #888; margin-top: 2px; }
.header-actions { display: flex; gap: 8px; align-items: center; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-primary:hover:not(:disabled) {
  background: #4f52e0;
  transform: translateY(-1px);
}
.btn-primary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff;
  color: #444;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-ghost:hover {
  background: #f5f5f5;
  border-color: #ccc;
  color: #6366f1;
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #dc2626;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
  transform: translateY(-1px);
}
.btn-danger:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.dropdown-wrapper { position: relative; }
.chevron-icon { transition: transform 0.2s; font-size: 11px; }
.chevron-icon.rotate { transform: rotate(180deg); }

.dropdown-menu-custom {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 150px;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  padding: 6px;
  z-index: 1050;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.dropdown-item-custom {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  background: none;
  border: none;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.1s;
  text-align: left;
}
.dropdown-item-custom:hover {
  background: #f5f5f5;
  color: #6366f1;
}

.dropdown-fade-enter-active, .dropdown-fade-leave-active { transition: all 0.15s ease; }
.dropdown-fade-enter-from, .dropdown-fade-leave-to { opacity: 0; transform: translateY(-6px); }

.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }

.search-wrapper { position: relative; flex: 1; min-width: 220px; }
.search-wrapper > .bi-search { position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: #aaa; font-size: 13px; pointer-events: none; }
.search-wrapper input { width: 100%; padding: 8px 34px 8px 34px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13px; background: #fff; font-family: inherit; color: #111; transition: all 0.2s; }
.search-wrapper input::placeholder { color: #bbb; }
.search-wrapper input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }

.clear-btn { position: absolute; right: 8px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #aaa; cursor: pointer; padding: 2px 4px; font-size: 14px; display: flex; align-items: center; }
.clear-btn:hover { color: #6366f1; }

.select-wrapper { position: relative; min-width: 130px; }
.select-wrapper select { width: 100%; padding: 8px 30px 8px 11px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13px; background: #fff; appearance: none; cursor: pointer; font-family: inherit; color: #333; transition: all 0.2s; }
.select-wrapper select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.select-wrapper .bi-chevron-down { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #aaa; font-size: 11px; pointer-events: none; }

.table-card { background: #fff; border-radius: 12px; border: 1px solid #e8e8e8; overflow: hidden; }

.state-center { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; gap: 10px; color: #888; }
.empty-icon { font-size: 36px; color: #ccc; }
.empty-title { font-size: 16px; font-weight: 600; color: #333; }
.empty-sub { font-size: 13px; color: #999; }

.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
thead tr { border-bottom: 1px solid #f0f0f0; }
th { padding: 11px 16px; font-size: 11.5px; font-weight: 600; color: #999; text-transform: uppercase; letter-spacing: 0.5px; text-align: left; white-space: nowrap; background: #fafafa; }
th.sortable { cursor: pointer; user-select: none; }
th.sortable:hover { color: #6366f1; }
th.sortable i { font-size: 10px; margin-left: 4px; }
.th-num { width: 48px; }
.th-actions { width: 80px; }

tbody tr { border-bottom: 1px solid #f5f5f5; transition: background 0.1s; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: #fafafa; }
td { padding: 12px 16px; font-size: 13.5px; color: #333; vertical-align: middle; }
.td-num { color: #bbb; font-size: 12px; }
.td-meta { color: #888; font-size: 12.5px; }
.td-desc { color: #888; font-size: 13px; max-width: 220px; }

.role-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.role-name { font-size: 13.5px; font-weight: 600; color: #111; }
.role-code { font-size: 11.5px; color: #bbb; margin-top: 1px; font-family: monospace; }

.perm-list { display: flex; flex-wrap: wrap; gap: 4px; }
.perm-tag { padding: 2px 8px; background: #f3f4f6; color: #555; border-radius: 5px; font-size: 11.5px; font-weight: 500; white-space: nowrap; }
.perm-more { background: #e5e7eb; color: #777; }

.row-actions { display: flex; gap: 4px; }
.icon-btn { width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border: none; background: none; border-radius: 6px; cursor: pointer; color: #888; transition: all 0.15s; font-size: 13px; }
.icon-btn:hover { background: #f0f0f0; color: #6366f1; }
.icon-btn.danger:hover { background: #fef2f2; color: #dc2626; }

.pagination-bar { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-top: 1px solid #f0f0f0; flex-wrap: wrap; gap: 10px; }
.pagination-info { font-size: 12.5px; color: #888; }
.page-controls { display: flex; gap: 4px; align-items: center; }
.page-controls button { min-width: 30px; height: 30px; padding: 0 6px; border: 1px solid #e5e5e5; border-radius: 6px; background: #fff; color: #555; font-size: 12px; font-family: inherit; cursor: pointer; transition: all 0.1s; display: flex; align-items: center; justify-content: center; }
.page-controls button:hover:not(:disabled) { background: #f5f5f5; border-color: #6366f1; color: #6366f1; }
.page-controls button.active { background: #6366f1; color: #fff; border-color: #6366f1; }
.page-controls button:disabled { opacity: 0.35; cursor: not-allowed; }

/* Modal Overrides */
.modal-custom { border: none; border-radius: 14px; overflow: hidden; font-family: 'DM Sans', sans-serif; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }

.modal-header-custom { display: flex; justify-content: space-between; align-items: flex-start; padding: 20px 24px 16px; border-bottom: 1px solid #f0f0f0; background: #fff; }
.modal-header-custom .modal-title { font-size: 16px; font-weight: 700; color: #111; margin: 0; font-family: 'DM Sans', sans-serif; }
.modal-subtitle { font-size: 12.5px; color: #999; margin: 3px 0 0; }

.btn-close-custom { background: none; border: none; color: #aaa; cursor: pointer; font-size: 14px; padding: 2px; display: flex; align-items: center; line-height: 1; }
.btn-close-custom:hover { color: #6366f1; }

.modal-body-custom { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; background: #fff; }
.modal-footer-custom { display: flex; gap: 8px; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid #f0f0f0; background: #fff; }

/* Form Fields */
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #333; }
.req { color: #dc2626; }
.field.field-error .input-wrap input { border-color: #dc2626; }
.err-msg { font-size: 12px; color: #dc2626; }

.alert-error { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; font-size: 13px; color: #dc2626; }

.input-wrap { position: relative; display: flex; align-items: center; }
.input-wrap > i { position: absolute; left: 11px; color: #bbb; font-size: 13px; pointer-events: none; }
.input-wrap input { width: 100%; padding: 9px 12px 9px 34px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #111; background: #fff; transition: all 0.2s; }
.input-wrap input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.input-wrap input::placeholder { color: #ccc; }

.textarea-field { width: 100%; padding: 9px 12px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #111; background: #fff; resize: vertical; transition: all 0.2s; }
.textarea-field:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.textarea-field::placeholder { color: #ccc; }

/* Permissions Panel */
.perms-panel { border: 1px solid #e5e5e5; border-radius: 10px; overflow: hidden; }

.perms-panel-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: #fafafa; border-bottom: 1px solid #f0f0f0; }
.perms-count { font-size: 13px; color: #666; }
.perms-count strong { color: #111; }

.link-btn { background: none; border: none; color: #555; font-size: 12.5px; font-weight: 600; cursor: pointer; padding: 4px 8px; border-radius: 6px; font-family: inherit; transition: all 0.2s; }
.link-btn:hover { background: #f0f0f0; color: #6366f1; }

.perms-search { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-bottom: 1px solid #f5f5f5; }
.perms-search i { color: #ccc; font-size: 12px; flex-shrink: 0; }
.perms-search input { flex: 1; border: none; outline: none; font-size: 13px; background: transparent; color: #111; font-family: inherit; }
.perms-search input:focus { color: #6366f1; }
.perms-search input::placeholder { color: #ccc; }

.perms-list { max-height: 280px; overflow-y: auto; }
.perms-list::-webkit-scrollbar { width: 3px; }
.perms-list::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 2px; }

.perm-group { border-bottom: 1px solid #f5f5f5; }
.perm-group:last-child { border-bottom: none; }

.group-row { display: flex; align-items: center; gap: 8px; padding: 9px 14px; background: #fafafa; cursor: pointer; user-select: none; transition: background 0.1s; }
.group-row:hover { background: #f3f3f3; }
.group-row input[type="checkbox"] { width: 15px; height: 15px; cursor: pointer; flex-shrink: 0; accent-color: #6366f1; }

.group-icon { width: 24px; height: 24px; border-radius: 6px; background: #efefef; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 12px; color: #555; }
.group-name { flex: 1; font-size: 13px; font-weight: 600; color: #222; }
.group-count { font-size: 11px; padding: 2px 7px; background: #efefef; border-radius: 20px; color: #777; font-weight: 600; white-space: nowrap; transition: all 0.15s; }
.group-count.active { background: #6366f1; color: #fff; }
.group-chevron { font-size: 10px; color: #aaa; transition: transform 0.2s; flex-shrink: 0; }
.group-chevron.rotated { transform: rotate(180deg); }

.group-items { padding: 4px 8px; background: #fff; display: flex; flex-direction: column; gap: 1px; }

.perm-item { display: flex; align-items: flex-start; gap: 10px; padding: 8px 10px; border-radius: 7px; cursor: pointer; transition: background 0.1s; }
.perm-item:hover { background: #f8f8f8; }
.perm-item input[type="checkbox"] { margin-top: 2px; width: 15px; height: 15px; cursor: pointer; flex-shrink: 0; accent-color: #6366f1; }

.perm-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.perm-name { font-size: 13px; font-weight: 500; color: #222; word-break: break-word; }
.perm-desc { font-size: 11.5px; color: #999; word-break: break-word; }

.no-results { padding: 40px 20px; text-align: center; color: #aaa; }
.no-results i { font-size: 24px; display: block; margin-bottom: 8px; }
.no-results p { font-size: 13px; }

/* Delete Modal */
.delete-body { text-align: center; }
.del-avatar { width: 54px; height: 54px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; font-weight: 700; margin: 0 auto 12px; }
.del-name { font-size: 15px; font-weight: 700; color: #111; }
.del-sub { font-size: 12.5px; color: #999; margin-top: 2px; }
.del-warn { font-size: 13px; color: #666; margin-top: 12px; }

.spin-sm { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.expand-enter-active, .expand-leave-active { transition: all 0.15s ease; }
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-6px); }

@media (max-width: 768px) {
  .role-management { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; }
  .search-wrapper { min-width: 100%; }
  .pagination-bar { flex-direction: column; align-items: flex-start; }
}
</style>