<template>
  <div class="employee-management">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Employees</h1>
        <p class="page-desc">{{ pagination.totalElements }} employees · {{ workingCount }} working · {{ resignedCount }} resigned</p>
      </div>
      <div class="header-actions">
        <button v-if="canCreate" class="btn-primary" data-bs-toggle="modal" data-bs-target="#employeeModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> New Employee
        </button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <i class="bi bi-search"></i>
        <input v-model="filters.keyword" type="text" placeholder="Search by name, username or email..." @input="onSearch" />
        <button v-if="filters.keyword" class="clear-btn" @click="clearSearch"><i class="bi bi-x"></i></button>
      </div>
      <div class="select-wrapper">
        <select v-model="filters.workStatus" @change="onFilterChange">
          <option value="">All Status</option>
          <option value="WORKING">Working</option>
          <option value="RESIGNED">Resigned</option>
        </select>
        <i class="bi bi-chevron-down"></i>
      </div>
      <div class="select-wrapper">
        <select v-model="filters.departmentId" @change="onFilterChange">
          <option :value="null">All Departments</option>
          <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
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

      <div v-else-if="employees.length === 0" class="state-center">
        <i class="bi bi-people empty-icon"></i>
        <p class="empty-title">No employees found</p>
        <p class="empty-sub">Create an employee to get started.</p>
        <button class="btn-primary" data-bs-toggle="modal" data-bs-target="#employeeModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Create employee
        </button>
      </div>

      <div v-else>
        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th class="th-num">#</th>
              <th class="sortable" @click="toggleSort('fullName')">Employee <i :class="'bi ' + getSortIcon('fullName')"></i></th>
              <th>Department / Position</th>
              <th>Contact</th>
              <th class="sortable" @click="toggleSort('workStatus')">Status <i :class="'bi ' + getSortIcon('workStatus')"></i></th>
              <th class="sortable" @click="toggleSort('createdAt')">Created <i :class="'bi ' + getSortIcon('createdAt')"></i></th>
              <th class="th-actions">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(emp, index) in employees" :key="emp.id" :data-employee-id="emp.id">
              <td class="td-num">{{ pagination.page * pagination.size + index + 1 }}</td>
              <td>
                <div class="employee-cell">
                  <div class="avatar" :style="{ background: avatarGradient(emp.fullName) }">{{ getInitials(emp.fullName) }}</div>
                  <div>
                    <div class="employee-name">{{ emp.fullName }}</div>
                    <div class="employee-username"><i class="bi bi-person"></i> {{ emp.username }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="org-info">
                  <div class="dept-info"><i class="bi bi-building"></i> {{ emp.departmentName || '—' }}</div>
                  <div class="position-info"><i class="bi bi-person-badge"></i> {{ emp.positionName || '—' }}</div>
                </div>
              </td>
              <td>
                <div class="contact-info">
                  <div><i class="bi bi-envelope"></i> {{ emp.email }}</div>
                  <div><i class="bi bi-telephone"></i> {{ emp.phone }}</div>
                </div>
              </td>
              <td>
                  <span :class="['status-badge', emp.workStatus === 'WORKING' ? 'working' : 'resigned']">
                    <span class="dot"></span>
                    {{ emp.workStatus === 'WORKING' ? 'Working' : 'Resigned' }}
                  </span>
              </td>
              <td class="td-meta">{{ formatDate(emp.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="canEdit" class="icon-btn" data-bs-toggle="modal" data-bs-target="#employeeModal" @click="openEdit(emp)" title="Edit">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button v-if="canDelete" class="icon-btn danger" data-bs-toggle="modal" data-bs-target="#deleteEmployeeModal" @click="confirmDelete(emp)" title="Delete">
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

    <!-- Create/Edit Modal - Scrollable vừa phải -->
    <div class="modal fade" id="employeeModal" tabindex="-1" aria-labelledby="employeeModalLabel" aria-hidden="true" ref="employeeModalEl">
      <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">{{ modal.isEdit ? 'Edit Employee' : 'New Employee' }}</h5>
              <p class="modal-subtitle">{{ modal.isEdit ? 'Update employee information' : 'Create a new employee account' }}</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="modal-body-custom">
            <div v-if="errors.general" class="alert-error">
              <i class="bi bi-exclamation-triangle"></i> {{ errors.general }}
            </div>

            <!-- User Information Section -->
            <div class="section-title">
              <i class="bi bi-person-circle"></i>
              <span>Account Information</span>
            </div>

            <div class="form-row">
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
            </div>

            <div v-if="!modal.isEdit" class="field" :class="{ 'field-error': errors.password }">
              <label>Password <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-lock"></i>
                <input :type="showPassword ? 'text' : 'password'" v-model="form.password" placeholder="Enter password (min 6 characters)" @input="clearError('password')" />
                <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
                  <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                </button>
              </div>
              <span v-if="errors.password" class="err-msg">{{ errors.password }}</span>
            </div>

            <div class="field" :class="{ 'field-error': errors.roleId }">
              <label>Role <span class="req">*</span></label>
              <div class="select-wrap">
                <select v-model="form.roleId" @change="clearError('roleId')">
                  <option :value="null">— Select role —</option>
                  <option v-for="role in roles" :key="role.id" :value="role.id">{{ role.name }}</option>
                </select>
                <i class="bi bi-chevron-down"></i>
              </div>
              <span v-if="errors.roleId" class="err-msg">{{ errors.roleId }}</span>
            </div>

            <!-- Personal Information Section -->
            <div class="section-title">
              <i class="bi bi-person-vcard"></i>
              <span>Personal Information</span>
            </div>

            <div class="form-row">
              <div class="field" :class="{ 'field-error': errors.fullName }">
                <label>Full Name <span class="req">*</span></label>
                <div class="input-wrap">
                  <i class="bi bi-person"></i>
                  <input v-model="form.fullName" type="text" placeholder="Enter full name" @input="clearError('fullName')" />
                </div>
                <span v-if="errors.fullName" class="err-msg">{{ errors.fullName }}</span>
              </div>

              <div class="field" :class="{ 'field-error': errors.gender }">
                <label>Gender <span class="req">*</span></label>
                <div class="select-wrap">
                  <select v-model="form.gender" @change="clearError('gender')">
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                  <i class="bi bi-chevron-down"></i>
                </div>
                <span v-if="errors.gender" class="err-msg">{{ errors.gender }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="field" :class="{ 'field-error': errors.birthDate }">
                <label>Birth Date <span class="req">*</span></label>
                <div class="input-wrap">
                  <i class="bi bi-calendar"></i>
                  <input v-model="form.birthDate" type="date" @input="clearError('birthDate')" />
                </div>
                <span v-if="errors.birthDate" class="err-msg">{{ errors.birthDate }}</span>
              </div>

              <div class="field" :class="{ 'field-error': errors.phone }">
                <label>Phone <span class="req">*</span></label>
                <div class="input-wrap">
                  <i class="bi bi-telephone"></i>
                  <input v-model="form.phone" type="tel" placeholder="Enter phone number" @input="clearError('phone')" />
                </div>
                <span v-if="errors.phone" class="err-msg">{{ errors.phone }}</span>
              </div>
            </div>

            <div class="field" :class="{ 'field-error': errors.address }">
              <label>Address <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-geo-alt"></i>
                <input v-model="form.address" type="text" placeholder="Enter address" @input="clearError('address')" />
              </div>
              <span v-if="errors.address" class="err-msg">{{ errors.address }}</span>
            </div>

            <!-- Organization Section -->
            <div class="section-title">
              <i class="bi bi-building"></i>
              <span>Organization</span>
            </div>

            <div class="form-row">
              <div class="field" :class="{ 'field-error': errors.departmentId }">
                <label>Department <span class="req">*</span></label>
                <div class="select-wrap">
                  <select v-model="form.departmentId" @change="onDepartmentChange">
                    <option :value="null">— Select department —</option>
                    <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
                  </select>
                  <i class="bi bi-chevron-down"></i>
                </div>
                <span v-if="errors.departmentId" class="err-msg">{{ errors.departmentId }}</span>
              </div>

              <div class="field" :class="{ 'field-error': errors.positionId }">
                <label>Position <span class="req">*</span></label>
                <div class="select-wrap">
                  <select v-model="form.positionId" :disabled="!form.departmentId">
                    <option :value="null">— Select position —</option>
                    <option v-for="pos in filteredPositions" :key="pos.id" :value="pos.id">{{ pos.name }}</option>
                  </select>
                  <i class="bi bi-chevron-down"></i>
                </div>
                <span v-if="errors.positionId" class="err-msg">{{ errors.positionId }}</span>
              </div>
            </div>

            <div v-if="modal.isEdit" class="field">
              <label>Work Status</label>
              <div class="select-wrap">
                <select v-model="form.workStatus">
                  <option value="WORKING">Working</option>
                  <option value="RESIGNED">Resigned</option>
                </select>
                <i class="bi bi-chevron-down"></i>
              </div>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn-primary" :disabled="submitting" @click="submitForm">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else>
                <i :class="modal.isEdit ? 'bi bi-check-lg' : 'bi bi-plus-lg'"></i>
                {{ modal.isEdit ? 'Save Changes' : 'Create Employee' }}
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal fade" id="deleteEmployeeModal" tabindex="-1" aria-hidden="true" ref="deleteModalEl">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">Delete Employee</h5>
              <p class="modal-subtitle">This action cannot be undone</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>
          <div class="modal-body-custom delete-body">
            <div class="del-avatar" :style="deleteModal.employee ? { background: avatarGradient(deleteModal.employee.fullName) } : {}">
              {{ deleteModal.employee ? getInitials(deleteModal.employee.fullName) : '' }}
            </div>
            <p class="del-name">{{ deleteModal.employee?.fullName }}</p>
            <p class="del-email">{{ deleteModal.employee?.email }}</p>
            <p class="del-warn">Permanently delete this employee and their user account?</p>
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { Modal } from 'bootstrap'
import employeeService from '@/services/employee.service.js'
import departmentService from '@/services/department.service.js'
import positionService from '@/services/position.service.js'
import roleService from '@/services/role.service.js'
import { useToast } from 'vue-toastification'
import { useAuthStore } from '@/stores/auth.store.js'

const toast = useToast()
const auth = useAuthStore()

const employees = ref([])
const departments = ref([])
const positions = ref([])
const roles = ref([])
const loading = ref(false)
const submitting = ref(false)
const showPassword = ref(false)

const canCreate = computed(() => auth.can('EMPLOYEE_CREATE'))
const canEdit = computed(() => auth.can('EMPLOYEE_UPDATE'))
const canDelete = computed(() => auth.can('EMPLOYEE_DELETE'))

const employeeModalEl = ref(null)
const deleteModalEl = ref(null)
let bsEmployeeModal = null
let bsDeleteModal = null

const filters = reactive({ keyword: '', workStatus: '', departmentId: null })
const sort = reactive({ sortBy: 'createdAt', sortDir: 'desc' })
const pagination = reactive({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: false })

const workingCount = computed(() => employees.value.filter(e => e.workStatus === 'WORKING').length)
const resignedCount = computed(() => employees.value.filter(e => e.workStatus === 'RESIGNED').length)

const filteredPositions = computed(() => {
  if (!form.departmentId) return []
  return positions.value.filter(p => p.departmentId === form.departmentId)
})

const startEntry = computed(() => pagination.totalElements === 0 ? 0 : pagination.page * pagination.size + 1)
const endEntry = computed(() => Math.min((pagination.page + 1) * pagination.size, pagination.totalElements))

const visiblePages = computed(() => {
  const delta = 2
  const start = Math.max(0, pagination.page - delta)
  const end = Math.min(pagination.totalPages - 1, pagination.page + delta)
  const pages = []
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

onMounted(() => {
  bsEmployeeModal = new Modal(employeeModalEl.value)
  bsDeleteModal = new Modal(deleteModalEl.value)
  employeeModalEl.value.addEventListener('hidden.bs.modal', () => { resetForm() })
  fetchDepartments()
  fetchRoles()
  fetchEmployees()
})

onUnmounted(() => {
  bsEmployeeModal?.dispose()
  bsDeleteModal?.dispose()
})

async function fetchRoles() {
  try {
    const response = await roleService.getAll({ size: 100 })
    roles.value = response.data?.content || []
  } catch (error) {
    console.error('Failed to fetch roles:', error)
  }
}

async function fetchDepartments() {
  try {
    const response = await departmentService.getAll({ size: 1000 })
    departments.value = response.data?.content || []
  } catch (error) {
    console.error('Failed to fetch departments:', error)
  }
}

async function fetchPositionsByDepartment(departmentId) {
  try {
    const response = await positionService.getByDepartment(departmentId)
    positions.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch positions:', error)
  }
}

async function fetchEmployees() {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword || undefined,
      workStatus: filters.workStatus || undefined,
      departmentId: filters.departmentId || undefined,
      page: pagination.page,
      size: pagination.size,
      sortBy: sort.sortBy,
      sortDir: sort.sortDir
    }
    const { data } = await employeeService.getAll(params)
    employees.value = data.content || []
    pagination.totalElements = data.totalElements || 0
    pagination.totalPages = data.totalPages || 0
    pagination.last = data.last || false
  } catch (error) {
    toast.error('Failed to load employees')
    console.error(error)
  } finally {
    loading.value = false
  }
}

let searchTimer = null
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    pagination.page = 0
    fetchEmployees()
  }, 400)
}

function onFilterChange() {
  pagination.page = 0
  fetchEmployees()
}

function clearSearch() {
  filters.keyword = ''
  pagination.page = 0
  fetchEmployees()
}

function toggleSort(col) {
  if (sort.sortBy === col) {
    sort.sortDir = sort.sortDir === 'desc' ? 'asc' : 'desc'
  } else {
    sort.sortBy = col
    sort.sortDir = 'desc'
  }
  fetchEmployees()
}

function getSortIcon(col) {
  if (sort.sortBy !== col) return 'bi-arrow-down-up'
  return sort.sortDir === 'desc' ? 'bi-arrow-down' : 'bi-arrow-up'
}

function goPage(page) {
  pagination.page = page
  fetchEmployees()
}

function onSizeChange() {
  pagination.page = 0
  fetchEmployees()
}

const modal = reactive({ isEdit: false, employeeId: null })
const errors = reactive({})
const form = reactive({
  username: '', password: '', email: '', roleId: null,
  fullName: '', gender: 'MALE', birthDate: '', phone: '', address: '',
  departmentId: null, positionId: null, workStatus: 'WORKING'
})
const deleteModal = reactive({ employee: null })

function resetForm() {
  form.username = ''
  form.password = ''
  form.email = ''
  form.roleId = null
  form.fullName = ''
  form.gender = 'MALE'
  form.birthDate = ''
  form.phone = ''
  form.address = ''
  form.departmentId = null
  form.positionId = null
  form.workStatus = 'WORKING'
  showPassword.value = false
  Object.keys(errors).forEach(key => delete errors[key])
}

function clearError(field) {
  delete errors[field]
}

async function onDepartmentChange() {
  form.positionId = null
  if (form.departmentId) {
    await fetchPositionsByDepartment(form.departmentId)
  } else {
    positions.value = []
  }
}

function openCreate() {
  resetForm()
  modal.isEdit = false
  modal.employeeId = null
  bsEmployeeModal.show()
}

function openEdit(emp) {
  resetForm()
  form.username = emp.username
  form.email = emp.email
  form.roleId = emp.roleId
  form.fullName = emp.fullName
  form.gender = emp.gender || 'MALE'
  form.birthDate = emp.birthDate
  form.phone = emp.phone
  form.address = emp.address
  form.departmentId = emp.departmentId
  form.positionId = emp.positionId
  form.workStatus = emp.workStatus
  modal.isEdit = true
  modal.employeeId = emp.id
  if (emp.departmentId) {
    fetchPositionsByDepartment(emp.departmentId)
  }
  bsEmployeeModal.show()
}

function confirmDelete(emp) {
  deleteModal.employee = emp
  bsDeleteModal.show()
}

async function submitForm() {
  if (!form.departmentId) {
    errors.departmentId = 'Please select a department'
    return
  }
  if (!form.positionId) {
    errors.positionId = 'Please select a position'
    return
  }

  submitting.value = true
  Object.keys(errors).forEach(key => delete errors[key])

  try {
    const submitData = { ...form }
    if (modal.isEdit) {
      delete submitData.password
      await employeeService.update(modal.employeeId, submitData)
      toast.success('Employee updated successfully')
    } else {
      await employeeService.create(submitData)
      toast.success('Employee created successfully')
    }

    pagination.page = 0
    await fetchEmployees()
    bsEmployeeModal.hide()
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

async function doDelete() {
  submitting.value = true
  try {
    await employeeService.delete(deleteModal.employee.id)
    await fetchEmployees()
    toast.success(`Employee ${deleteModal.employee.fullName} deleted`)
    bsDeleteModal.hide()
  } catch (error) {
    toast.error(error?.message || 'Failed to delete employee')
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

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.employee-management {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #111;
}

.page-desc {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* Buttons */
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

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-wrapper {
  position: relative;
  flex: 1;
  min-width: 220px;
}

.search-wrapper > .bi-search {
  position: absolute;
  left: 11px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 13px;
  pointer-events: none;
}

.search-wrapper input {
  width: 100%;
  padding: 8px 34px 8px 34px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  font-family: inherit;
  color: #111;
  transition: all 0.2s;
}

.search-wrapper input::placeholder {
  color: #bbb;
}

.search-wrapper input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
  padding: 2px 4px;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.clear-btn:hover {
  color: #6366f1;
}

.select-wrapper {
  position: relative;
  min-width: 130px;
}

.select-wrapper select {
  width: 100%;
  padding: 8px 30px 8px 11px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  appearance: none;
  cursor: pointer;
  font-family: inherit;
  color: #333;
  transition: all 0.2s;
}

.select-wrapper select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.select-wrapper .bi-chevron-down {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 11px;
  pointer-events: none;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.state-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 10px;
  color: #888;
}

.empty-icon {
  font-size: 36px;
  color: #ccc;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.empty-sub {
  font-size: 13px;
  color: #999;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead tr {
  border-bottom: 1px solid #f0f0f0;
}

th {
  padding: 11px 16px;
  font-size: 11.5px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-align: left;
  white-space: nowrap;
  background: #fafafa;
}

th.sortable {
  cursor: pointer;
  user-select: none;
}

th.sortable:hover {
  color: #6366f1;
}

th.sortable i {
  font-size: 10px;
  margin-left: 4px;
}

.th-num {
  width: 48px;
}

.th-actions {
  width: 80px;
}

tbody tr {
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.1s;
}

tbody tr:last-child {
  border-bottom: none;
}

tbody tr:hover {
  background: #fafafa;
}

td {
  padding: 12px 16px;
  font-size: 13.5px;
  color: #333;
  vertical-align: middle;
}

.td-num {
  color: #bbb;
  font-size: 12px;
}

.td-meta {
  color: #888;
  font-size: 12.5px;
}

.td-desc {
  color: #888;
  font-size: 13px;
  max-width: 220px;
}

/* Avatar */
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

/* Row Actions */
.row-actions {
  display: flex;
  gap: 4px;
}

.icon-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  color: #888;
  transition: all 0.15s;
  font-size: 13px;
}

.icon-btn:hover {
  background: #f0f0f0;
  color: #6366f1;
}

.icon-btn.danger:hover {
  background: #fef2f2;
  color: #dc2626;
}

/* Pagination */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  font-size: 12.5px;
  color: #888;
}

.page-controls {
  display: flex;
  gap: 4px;
  align-items: center;
}

.page-controls button {
  min-width: 30px;
  height: 30px;
  padding: 0 6px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  background: #fff;
  color: #555;
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.1s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-controls button:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #6366f1;
  color: #6366f1;
}

.page-controls button.active {
  background: #6366f1;
  color: #fff;
  border-color: #6366f1;
}

.page-controls button:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* Modal */
.modal-custom {
  border: none;
  border-radius: 14px;
  overflow: hidden;
  font-family: 'DM Sans', sans-serif;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-header-custom {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.modal-header-custom .modal-title {
  font-size: 16px;
  font-weight: 700;
  color: #111;
  margin: 0;
}

.modal-subtitle {
  font-size: 12.5px;
  color: #999;
  margin: 3px 0 0;
}

.btn-close-custom {
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
  font-size: 14px;
  padding: 2px;
  display: flex;
  align-items: center;
  line-height: 1;
}

.btn-close-custom:hover {
  color: #6366f1;
}

.modal-body-custom {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #fff;
  max-height: 70vh;
  overflow-y: auto;
}

.modal-footer-custom {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

/* Form Fields */
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.req {
  color: #dc2626;
}

.field.field-error .input-wrap input,
.field.field-error .select-wrap select {
  border-color: #dc2626;
}

.err-msg {
  font-size: 12px;
  color: #dc2626;
}

.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  font-size: 13px;
  color: #dc2626;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrap > i {
  position: absolute;
  left: 11px;
  color: #bbb;
  font-size: 13px;
  pointer-events: none;
}

.input-wrap input {
  width: 100%;
  padding: 9px 12px 9px 34px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #111;
  background: #fff;
  transition: all 0.2s;
}

.input-wrap input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.input-wrap input::placeholder {
  color: #ccc;
}

.textarea-field {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #111;
  background: #fff;
  resize: vertical;
  transition: all 0.2s;
}

.textarea-field:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.select-wrap {
  position: relative;
}

.select-wrap select {
  width: 100%;
  padding: 9px 30px 9px 11px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #333;
  background: #fff;
  appearance: none;
  cursor: pointer;
  transition: all 0.2s;
}

.select-wrap select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.select-wrap .bi-chevron-down {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 11px;
  pointer-events: none;
}

/* Delete Modal */
.delete-body {
  text-align: center;
}

.del-avatar {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 auto 12px;
}

.del-name {
  font-size: 15px;
  font-weight: 700;
  color: #111;
}

.del-email {
  font-size: 12.5px;
  color: #999;
  margin-top: 2px;
}

.del-warn {
  font-size: 13px;
  color: #666;
  margin-top: 12px;
}

.del-warn.warning {
  color: #dc2626;
  background: #fef2f2;
  padding: 8px;
  border-radius: 8px;
}

.del-warn.warning i {
  margin-right: 6px;
}

/* Spinner */
.spin-sm {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.text-muted {
  color: #999;
}

/* Employee specific styles */
.employee-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.employee-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #111;
}

.employee-username {
  font-size: 11.5px;
  color: #999;
  margin-top: 2px;
}

.employee-username i {
  font-size: 10px;
  margin-right: 2px;
}

.org-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.dept-info,
.position-info {
  font-size: 12.5px;
  color: #555;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dept-info i,
.position-info i {
  font-size: 11px;
  color: #6366f1;
  width: 16px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 12.5px;
  color: #555;
}

.contact-info i {
  font-size: 11px;
  color: #888;
  width: 20px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.working {
  background: #f0fdf4;
  color: #16a34a;
}

.status-badge.resigned {
  background: #fef2f2;
  color: #dc2626;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  padding: 8px 0 4px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.section-title:first-of-type {
  margin-top: 0;
}

.section-title i {
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.toggle-pw {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: #bbb;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 2px;
}

.toggle-pw:hover {
  color: #6366f1;
}

/* Modal Size */
.modal-lg {
  max-width: 800px;
}

@media (max-width: 768px) {
  .employee-management {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-wrapper {
    min-width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .pagination-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .modal-lg {
    max-width: 95%;
    margin: 0.5rem;
  }
}
</style>