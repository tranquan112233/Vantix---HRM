<template>
  <div class="user-management">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Users</h1>
        <p class="page-desc">{{ pagination.totalElements }} total · {{ activeCount }} active · {{ lockedCount }} locked</p>
      </div>
      <div class="header-actions">
        <div class="dropdown-wrapper" ref="exportDropdown">
          <button class="btn-ghost" @click.stop="toggleExportDropdown">
            <i class="bi bi-download"></i> Export
            <i class="bi bi-chevron-down chevron-icon" :class="{ rotate: showExportDropdown }"></i>
          </button>
          <transition name="dropdown-fade">
            <div v-if="showExportDropdown" class="dropdown-menu-custom">
              <button class="dropdown-item-custom" @click="exportUsers('csv')"><i class="bi bi-filetype-csv"></i> CSV</button>
              <button class="dropdown-item-custom" @click="exportUsers('excel')"><i class="bi bi-filetype-xlsx"></i> Excel</button>
              <button class="dropdown-item-custom" @click="exportUsers('pdf')"><i class="bi bi-filetype-pdf"></i> PDF</button>
            </div>
          </transition>
        </div>
        <button v-if="canCreate" class="btn-primary" data-bs-toggle="modal" data-bs-target="#userModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> New User
        </button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <i class="bi bi-search"></i>
        <input v-model="filters.keyword" type="text" placeholder="Search username or email..." @input="onSearch" />
        <button v-if="filters.keyword" class="clear-btn" @click="clearSearch"><i class="bi bi-x"></i></button>
      </div>
      <div class="select-wrapper">
        <select v-model="filters.status" @change="fetchUsers">
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="LOCKED">Locked</option>
        </select>
        <i class="bi bi-chevron-down"></i>
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

      <div v-else-if="users.length === 0" class="state-center">
        <i class="bi bi-people empty-icon"></i>
        <p class="empty-title">No users found</p>
        <p class="empty-sub">Create the first user to get started.</p>
        <button class="btn-primary" data-bs-toggle="modal" data-bs-target="#userModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Create user
        </button>
      </div>

      <div v-else>
        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th class="th-num">#</th>
              <th class="sortable" @click="toggleSort('username')">User <i :class="'bi ' + getSortIcon('username')"></i></th>
              <th class="sortable" @click="toggleSort('roleName')">Role <i :class="'bi ' + getSortIcon('roleName')"></i></th>
              <th class="sortable" @click="toggleSort('status')">Status <i :class="'bi ' + getSortIcon('status')"></i></th>
              <th class="sortable" @click="toggleSort('lastActive')">Last Active <i :class="'bi ' + getSortIcon('lastActive')"></i></th>
              <th class="sortable" @click="toggleSort('createdAt')">Created <i :class="'bi ' + getSortIcon('createdAt')"></i></th>
              <th class="th-actions">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(user, index) in users" :key="user.id" :data-user-id="user.id">
              <td class="td-num">{{ pagination.page * pagination.size + index + 1 }}</td>
              <td>
                <div class="user-cell">
                  <div class="avatar" :style="{ background: avatarGradient(user.username) }">{{ getInitials(user.username) }}</div>
                  <div>
                    <div class="user-name">{{ user.username }}</div>
                    <div class="user-email">{{ user.email }}</div>
                  </div>
                </div>
              </td>
              <td>
                <span v-if="user.roleName" class="role-badge">{{ user.roleName }}</span>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                  <span :class="['status-badge', user.status === 'ACTIVE' ? 'active' : 'locked']">
                    <span class="dot"></span>
                    {{ user.status === 'ACTIVE' ? 'Active' : 'Locked' }}
                  </span>
              </td>
              <td class="td-meta">{{ formatTime(user.lastActive) }}</td>
              <td class="td-meta">{{ formatDate(user.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="canChangeStatus" class="icon-btn" @click="toggleStatus(user)" :title="user.status === 'ACTIVE' ? 'Lock' : 'Unlock'">
                    <i :class="user.status === 'ACTIVE' ? 'bi bi-lock' : 'bi bi-unlock'"></i>
                  </button>
                  <button v-if="canEdit" class="icon-btn" data-bs-toggle="modal" data-bs-target="#userModal" @click="openEdit(user)" title="Edit">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button v-if="canDelete" class="icon-btn danger" data-bs-toggle="modal" data-bs-target="#deleteUserModal" @click="confirmDelete(user)" title="Delete">
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
          <span class="pagination-info"> Show {{ startEntry }}–{{ endEntry }} of {{ pagination.totalElements }} entries</span>
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
    <div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true" ref="userModalEl">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title" id="userModalLabel">{{ modal.isEdit ? 'Edit User' : 'New User' }}</h5>
              <p class="modal-subtitle">{{ modal.isEdit ? 'Update user information' : 'Create a new account' }}</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="modal-body-custom">
            <div v-if="errors.general" class="alert-error">
              <i class="bi bi-exclamation-triangle"></i> {{ errors.general }}
            </div>

            <div class="field" :class="{ 'field-error': errors.username }">
              <label>Username <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-person"></i>
                <input v-model="form.username" type="text" placeholder="Enter username" @input="clearError('username')" />
              </div>
              <span v-if="errors.username" class="err-msg">{{ errors.username }}</span>
            </div>

            <div class="field" :class="{ 'field-error': errors.email }">
              <label>Email <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-envelope"></i>
                <input v-model="form.email" type="email" placeholder="Enter email" @input="clearError('email')" />
              </div>
              <span v-if="errors.email" class="err-msg">{{ errors.email }}</span>
            </div>

            <div v-if="!modal.isEdit" class="field" :class="{ 'field-error': errors.password }">
              <label>Password <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-lock"></i>
                <input class="field-input" v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="Enter password" @input="clearError('password')" />
                <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
                  <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                </button>
              </div>
              <span v-if="errors.password" class="err-msg">{{ errors.password }}</span>
            </div>

            <div class="form-row">
              <div class="field" :class="{ 'field-error': errors.roleId }">
                <label>Role</label>
                <div class="select-wrap">
                  <select v-model="form.roleId">
                    <option :value="null">— Select role —</option>
                    <option v-for="role in roles" :key="role.id" :value="role.id">{{ role.name }}</option>
                  </select>
                  <i class="bi bi-chevron-down"></i>
                </div>
                <span v-if="errors.roleId" class="err-msg">{{ errors.roleId }}</span>
              </div>

              <div v-if="modal.isEdit" class="field">
                <label>Status</label>
                <div class="select-wrap">
                  <select v-model="form.status">
                    <option value="ACTIVE">Active</option>
                    <option value="LOCKED">Locked</option>
                  </select>
                  <i class="bi bi-chevron-down"></i>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn-primary" :disabled="submitting" @click="submitForm">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else>
                <i :class="modal.isEdit ? 'bi bi-check-lg' : 'bi bi-plus-lg'"></i>
                {{ modal.isEdit ? 'Save Changes' : 'Create User' }}
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal fade" id="deleteUserModal" tabindex="-1" aria-hidden="true" ref="deleteModalEl">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">Delete User</h5>
              <p class="modal-subtitle">This action cannot be undone</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>
          <div class="modal-body-custom delete-body">
            <div class="del-avatar" :style="deleteModal.user ? { background: avatarGradient(deleteModal.user.username) } : {}">
              {{ deleteModal.user ? getInitials(deleteModal.user.username) : '' }}
            </div>
            <p class="del-name">{{ deleteModal.user?.username }}</p>
            <p class="del-email">{{ deleteModal.user?.email }}</p>
            <p class="del-warn">Permanently delete this user?</p>
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
import { useAuthStore } from "@/stores/auth.store.js";
import userService from '@/services/user.service.js'
import roleService from '@/services/role.service.js'
import { useToast } from 'vue-toastification'

// Import XLSX and jsPDF dynamically to avoid build issues
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'

const toast = useToast()

const users = ref([])
const roles = ref([])
const loading = ref(false)
const submitting = ref(false)
const showPassword = ref(false)
const showExportDropdown = ref(false)
const exportDropdown = ref(null)
const auth = useAuthStore()

const canCreate = computed(() => auth.can('USER_CREATE'))
const canEdit = computed(() => auth.can('USER_UPDATE'))
const canDelete = computed(() => auth.can('USER_DELETE'))
const canChangeStatus = computed(() => auth.can('USER_STATUS_UPDATE'))

const userModalEl = ref(null)
const deleteModalEl = ref(null)
let bsUserModal = null
let bsDeleteModal = null

const filters = reactive({ keyword: '', status: '' })
const sort = reactive({ sortBy: 'createdAt', sortDir: 'desc' })
const pagination = reactive({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: false })

const activeCount = computed(() => users.value.filter(u => u.status === 'ACTIVE').length)
const lockedCount = computed(() => users.value.filter(u => u.status === 'LOCKED').length)
const startEntry = computed(() => pagination.totalElements === 0 ? 0 : pagination.page * pagination.size + 1)
const endEntry = computed(() => Math.min((pagination.page + 1) * pagination.size, pagination.totalElements))
const visiblePages = computed(() => {
  const delta = 2, pages = []
  const start = Math.max(0, pagination.page - delta)
  const end = Math.min(pagination.totalPages - 1, pagination.page + delta)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const handleClickOutside = (e) => {
  if (exportDropdown.value && !exportDropdown.value.contains(e.target)) showExportDropdown.value = false
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  bsUserModal = new Modal(userModalEl.value)
  bsDeleteModal = new Modal(deleteModalEl.value)
  userModalEl.value.addEventListener('hidden.bs.modal', () => { resetForm(); showPassword.value = false })
  fetchUsers()
  fetchRoles()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  bsUserModal?.dispose()
  bsDeleteModal?.dispose()
})

function toggleExportDropdown() { showExportDropdown.value = !showExportDropdown.value }

async function exportUsers(format) {
  // Show loading toast
  const loadingToast = toast.info(`Exporting ${format.toUpperCase()}...`, { timeout: false })

  try {
    // Fetch all users for export
    const res = await userService.getAll({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      sortBy: sort.sortBy,
      sortDir: sort.sortDir,
      size: 10000 // Large number to get all records
    })

    const allUsers = res.data.content || []

    if (allUsers.length === 0) {
      toast.warning('No users to export')
      toast.dismiss(loadingToast)
      return
    }

    // Process data for export
    const exportData = allUsers.map(u => ({
      ID: u.id,
      Username: u.username,
      Email: u.email,
      Role: u.roleName || 'No Role',
      Status: u.status === 'ACTIVE' ? 'Active' : 'Locked',
      'Last Active': u.lastActive ? formatDateTime(u.lastActive) : 'Never',
      'Created At': u.createdAt ? formatDateTime(u.createdAt) : 'Unknown'
    }))

    // Export based on format
    switch(format) {
      case 'csv':
        exportToCSV(exportData)
        break
      case 'excel':
        await exportToExcel(exportData)
        break
      case 'pdf':
        await exportToPDF(exportData)
        break
      default:
        throw new Error('Unsupported format')
    }

    toast.dismiss(loadingToast)
    toast.success(`Successfully exported ${allUsers.length} users as ${format.toUpperCase()}`)
    showExportDropdown.value = false

  } catch (error) {
    toast.dismiss(loadingToast)
    console.error('Export error:', error)
    toast.error(`Failed to export as ${format.toUpperCase()}: ${error.message || 'Unknown error'}`)
  }
}

function exportToCSV(data) {
  try {
    if (!data || data.length === 0) {
      throw new Error('No data to export')
    }

    // Get headers from first object
    const headers = Object.keys(data[0])

    // Convert data to CSV rows
    const rows = data.map(row =>
        headers.map(header => {
          let value = row[header]
          // Handle null/undefined values
          if (value === null || value === undefined) value = ''
          // Convert to string and escape quotes
          value = String(value).replace(/"/g, '""')
          // Wrap in quotes if contains comma, newline, or quote
          if (value.includes(',') || value.includes('\n') || value.includes('"')) {
            value = `"${value}"`
          }
          return value
        }).join(',')
    )

    // Combine headers and rows
    const csvContent = [headers.join(','), ...rows].join('\n')

    // Add BOM for UTF-8 encoding
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
    downloadFile(blob, `users_${getTimestamp()}.csv`)

  } catch (error) {
    console.error('CSV export error:', error)
    throw new Error('Failed to generate CSV file')
  }
}

function exportToExcel(data) {
  try {
    if (!data || data.length === 0) {
      throw new Error('No data to export')
    }

    // Create worksheet
    const worksheet = XLSX.utils.json_to_sheet(data)

    // Auto-size columns (optional)
    const maxWidth = 50
    const colWidths = Object.keys(data[0]).map(key => {
      const maxLen = Math.max(
          key.length,
          ...data.map(row => String(row[key] || '').length)
      )
      return { wch: Math.min(maxLen, maxWidth) }
    })
    worksheet['!cols'] = colWidths

    // Create workbook
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Users')

    // Export file
    XLSX.writeFile(workbook, `users_${getTimestamp()}.xlsx`)

  } catch (error) {
    console.error('Excel export error:', error)
    throw new Error('Failed to generate Excel file')
  }
}

function exportToPDF(data) {
  try {
    if (!data || data.length === 0) {
      throw new Error('No data to export')
    }

    // Create PDF document (landscape for better fit)
    const doc = new jsPDF({
      orientation: 'landscape',
      unit: 'mm',
      format: 'a4'
    })

    // Add title
    doc.setFontSize(16)
    doc.setFont('helvetica', 'bold')
    doc.text('User List Report', 14, 15)

    // Add metadata
    doc.setFontSize(9)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated: ${new Date().toLocaleString()}`, 14, 22)
    doc.text(`Total Users: ${data.length}`, 14, 28)

    // Add filters info if any
    let filterText = ''
    if (filters.keyword) filterText += `Search: "${filters.keyword}" `
    if (filters.status) filterText += `Status: ${filters.status} `
    if (filterText) {
      doc.text(`Filters: ${filterText}`, 14, 34)
      autoTable(doc, { startY: 40 })
    } else {
      autoTable(doc, { startY: 34 })
    }

    // Prepare table data
    const tableData = data.map(user => [
      user.ID,
      user.Username,
      user.Email,
      user.Role,
      user.Status,
      user['Last Active'],
      user['Created At']
    ])

    // Generate table
    autoTable(doc, {
      head: [['ID', 'Username', 'Email', 'Role', 'Status', 'Last Active', 'Created At']],
      body: tableData,
      startY: filterText ? 40 : 34,
      theme: 'striped',
      styles: {
        fontSize: 8,
        cellPadding: 3,
        font: 'helvetica'
      },
      headStyles: {
        fillColor: [99, 102, 241],
        textColor: [255, 255, 255],
        fontStyle: 'bold',
        fontSize: 8
      },
      alternateRowStyles: {
        fillColor: [248, 248, 248]
      },
      columnStyles: {
        0: { cellWidth: 20 }, // ID
        1: { cellWidth: 35 }, // Username
        2: { cellWidth: 55 }, // Email
        3: { cellWidth: 30 }, // Role
        4: { cellWidth: 25 }, // Status
        5: { cellWidth: 35 }, // Last Active
        6: { cellWidth: 35 }  // Created At
      },
      margin: { left: 14, right: 14 }
    })

    // Add page numbers
    const pageCount = doc.internal.getNumberOfPages()
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i)
      doc.setFontSize(8)
      doc.setTextColor(128, 128, 128)
      doc.text(`Page ${i} of ${pageCount}`, doc.internal.pageSize.width - 20, doc.internal.pageSize.height - 10)
    }

    // Save PDF
    doc.save(`users_${getTimestamp()}.pdf`)

  } catch (error) {
    console.error('PDF export error:', error)
    throw new Error('Failed to generate PDF file')
  }
}

function formatDateTime(isoString) {
  if (!isoString) return ''
  try {
    const date = new Date(isoString)
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })
  } catch {
    return isoString
  }
}

function getTimestamp() {
  return new Date().toISOString()
      .slice(0, 19)
      .replace(/:/g, '-')
      .replace('T', '_')
}

function downloadFile(blob, filename) {
  // Create download link
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

async function fetchRoles() {
  try {
    const response = await roleService.getAll({ page: 0, size: 100 })
    roles.value = response.data.content || []
  } catch (error) {
    console.error('Failed to fetch roles:', error)
  }
}

async function fetchUsers() {
  loading.value = true
  try {
    const { data } = await userService.getAll({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      page: pagination.page,
      size: pagination.size,
      sortBy: sort.sortBy,
      sortDir: sort.sortDir
    })
    users.value = data.content || []
    pagination.totalElements = data.totalElements || 0
    pagination.totalPages = data.totalPages || 0
    pagination.last = data.last || false
  } catch (error) {
    toast.error('Failed to load users')
    console.error('Fetch users error:', error)
  } finally {
    loading.value = false
  }
}

let searchTimer = null
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 0
    fetchUsers()
  }, 400)
}

function clearSearch() {
  filters.keyword = ''
  pagination.page = 0
  fetchUsers()
}

function toggleSort(col) {
  if (sort.sortBy === col) {
    sort.sortDir = sort.sortDir === 'desc' ? 'asc' : 'desc'
  } else {
    sort.sortBy = col
    sort.sortDir = 'desc'
  }
  fetchUsers()
}

function getSortIcon(col) {
  if (sort.sortBy !== col) return 'bi-arrow-down-up'
  return sort.sortDir === 'desc' ? 'bi-arrow-down' : 'bi-arrow-up'
}

function goPage(page) {
  pagination.page = page
  fetchUsers()
}

function onSizeChange() {
  pagination.page = 0
  fetchUsers()
}

async function toggleStatus(user) {
  const nextStatus = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE'
  try {
    const { data } = await userService.changeStatus(user.id, nextStatus)
    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) users.value[index] = data
    toast.success(`User ${nextStatus === 'ACTIVE' ? 'unlocked' : 'locked'} successfully`)
  } catch (error) {
    toast.error('Failed to change user status')
    console.error('Status change error:', error)
  }
}

const modal = reactive({ isEdit: false, userId: null })
const errors = reactive({})
const form = reactive({ username: '', password: '', email: '', roleId: null, status: 'ACTIVE' })

function resetForm() {
  form.username = ''
  form.password = ''
  form.email = ''
  form.roleId = null
  form.status = 'ACTIVE'
  Object.keys(errors).forEach(key => delete errors[key])
}

function clearError(field) {
  delete errors[field]
}

function openCreate() {
  resetForm()
  modal.isEdit = false
  modal.userId = null
}

function openEdit(user) {
  resetForm()
  form.username = user.username
  form.email = user.email
  form.roleId = user.roleId
  form.status = user.status
  modal.isEdit = true
  modal.userId = user.id
}

async function submitForm() {
  submitting.value = true
  Object.keys(errors).forEach(key => delete errors[key])

  try {
    if (modal.isEdit) {
      const { data } = await userService.update(modal.userId, {
        username: form.username,
        email: form.email,
        roleId: form.roleId,
        status: form.status
      })
      const index = users.value.findIndex(u => u.id === modal.userId)
      if (index !== -1) users.value[index] = data
      toast.success('User updated successfully')
    } else {
      const { data: newUser } = await userService.create({
        ...form,
        status: 'ACTIVE'
      })
      pagination.page = 0
      await fetchUsers()
      await nextTick()

      const row = document.querySelector(`tr[data-user-id="${newUser.id}"]`)
      if (row) {
        row.classList.add('highlight-row')
        setTimeout(() => row.classList.remove('highlight-row'), 3000)
        row.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
      toast.success(`User "${newUser.username}" created successfully`)
    }
    bsUserModal.hide()
  } catch (err) {
    if (err?.errors) {
      Object.assign(errors, err.errors)
    } else {
      errors.general = err?.message || 'Something went wrong'
      toast.error(errors.general)
    }
    console.error('Submit error:', err)
  } finally {
    submitting.value = false
  }
}

const deleteModal = reactive({ user: null })
function confirmDelete(user) {
  deleteModal.user = user
}

async function doDelete() {
  submitting.value = true
  try {
    await userService.delete(deleteModal.user.id)
    users.value = users.value.filter(u => u.id !== deleteModal.user.id)
    pagination.totalElements--

    if (users.value.length === 0 && pagination.page > 0) {
      pagination.page--
      await fetchUsers()
    }

    toast.success(`User ${deleteModal.user.username} deleted successfully`)
    bsDeleteModal.hide()
  } catch (error) {
    toast.error('Failed to delete user')
    console.error('Delete error:', error)
  } finally {
    submitting.value = false
  }
}

function formatDate(iso) {
  if (!iso) return '—'
  try {
    return new Date(iso).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch {
    return '—'
  }
}

function formatTime(iso) {
  if (!iso) return 'Never'
  try {
    const diff = Math.floor((Date.now() - new Date(iso)) / 1000)
    if (diff < 60) return 'Just now'
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
    if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`
    return formatDate(iso)
  } catch {
    return '—'
  }
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

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
</script>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }

.field-input::-ms-reveal,
.field-input::-ms-clear { display: none; }

.user-management {
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
.th-actions { width: 110px; }

tbody tr { border-bottom: 1px solid #f5f5f5; transition: background 0.1s; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: #fafafa; }
tbody tr.highlight-row { background: #f0fdf4; }
td { padding: 12px 16px; font-size: 13.5px; color: #333; vertical-align: middle; }
.td-num { color: #bbb; font-size: 12px; }
.td-meta { color: #888; font-size: 12.5px; }

.user-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.user-name { font-size: 13.5px; font-weight: 600; color: #111; }
.user-email { font-size: 12px; color: #999; margin-top: 1px; }

.role-badge { display: inline-flex; align-items: center; padding: 3px 9px; background: #f3f4f6; color: #444; border-radius: 6px; font-size: 12px; font-weight: 500; }
.status-badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.status-badge.active { background: #f0fdf4; color: #16a34a; }
.status-badge.locked { background: #fef2f2; color: #dc2626; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

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

.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #333; }
.req { color: #dc2626; }
.field.field-error .input-wrap input,
.field.field-error .select-wrap select { border-color: #dc2626; }
.err-msg { font-size: 12px; color: #dc2626; }

.alert-error { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; font-size: 13px; color: #dc2626; }

.input-wrap { position: relative; display: flex; align-items: center; }
.input-wrap > i { position: absolute; left: 11px; color: #bbb; font-size: 13px; pointer-events: none; }
.input-wrap input { width: 100%; padding: 9px 36px 9px 34px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #111; background: #fff; transition: all 0.2s; }
.input-wrap input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.input-wrap input::placeholder { color: #ccc; }

.toggle-pw { position: absolute; right: 10px; background: none; border: none; color: #bbb; cursor: pointer; display: flex; align-items: center; padding: 2px; }
.toggle-pw:hover { color: #6366f1; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.select-wrap { position: relative; }
.select-wrap select { width: 100%; padding: 9px 30px 9px 11px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #333; background: #fff; appearance: none; cursor: pointer; transition: all 0.2s; }
.select-wrap select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.select-wrap .bi-chevron-down { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #aaa; font-size: 11px; pointer-events: none; }

.delete-body { text-align: center; }
.del-avatar { width: 54px; height: 54px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; font-weight: 700; margin: 0 auto 12px; }
.del-name { font-size: 15px; font-weight: 700; color: #111; }
.del-email { font-size: 12.5px; color: #999; margin-top: 2px; }
.del-warn { font-size: 13px; color: #666; margin-top: 12px; }

.spin-sm { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .user-management { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; }
  .search-wrapper { min-width: 100%; }
  .form-row { grid-template-columns: 1fr; }
  .pagination-bar { flex-direction: column; align-items: flex-start; }
}
</style>