<template>
  <div class="department-management mgmt-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Phòng ban</h1>
        <p class="page-desc">{{ pagination.totalElements }} phòng ban · {{ totalEmployees }} nhân viên</p>
      </div>
      <div class="header-actions">
        <button v-if="canCreate" class="btn-primary" data-bs-toggle="modal" data-bs-target="#departmentModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Thêm phòng ban
        </button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <i class="bi bi-search"></i>
        <input v-model="filters.keyword" type="text" placeholder="Tìm tên phòng ban..." @input="onSearch" />
        <button v-if="filters.keyword" class="clear-btn" @click="clearSearch"><i class="bi bi-x"></i></button>
      </div>
      <div class="select-wrapper">
        <select v-model.number="pagination.size" @change="onSizeChange">
          <option :value="10">10 / trang</option>
          <option :value="20">20 / trang</option>
          <option :value="50">50 / trang</option>
          <option :value="100">100 / trang</option>
        </select>
        <i class="bi bi-chevron-down"></i>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card">
      <div v-if="loading" class="state-center">
        <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
        <span>Đang tải...</span>
      </div>

      <div v-else-if="departments.length === 0" class="state-center">
        <i class="bi bi-diagram-3 empty-icon"></i>
        <p class="empty-title">Không tìm thấy phòng ban</p>
        <p class="empty-sub">Tạo phòng ban mới để bắt đầu.</p>
        <button class="btn-primary" data-bs-toggle="modal" data-bs-target="#departmentModal" @click="openCreate">
          <i class="bi bi-plus-lg"></i> Tạo phòng ban
        </button>
      </div>

      <div v-else>
        <div class="table-scroll">
          <table>
            <thead>
            <tr>
              <th class="th-num">#</th>
              <th class="sortable" @click="toggleSort('name')">Phòng ban <i :class="'bi ' + getSortIcon('name')"></i></th>
              <th>Mô tả</th>
              <th class="sortable" @click="toggleSort('managerName')">Trưởng phòng <i :class="'bi ' + getSortIcon('managerName')"></i></th>
              <th class="sortable" @click="toggleSort('employeeCount')">Nhân viên <i :class="'bi ' + getSortIcon('employeeCount')"></i></th>
              <th class="sortable" @click="toggleSort('createdAt')">Ngày tạo <i :class="'bi ' + getSortIcon('createdAt')"></i></th>
              <th class="th-actions">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(dept, index) in departments" :key="dept.id" :data-department-id="dept.id">
              <td class="td-num">{{ pagination.page * pagination.size + index + 1 }}</td>
              <td>
                <div class="dept-cell">
                  <div class="avatar" :style="{ background: avatarGradient(dept.name) }">{{ getInitials(dept.name) }}</div>
                  <div>
                    <div class="dept-name">{{ dept.name }}</div>
                  </div>
                </div>
              </td>
              <td class="td-desc">{{ dept.description || '—' }}</td>
              <td>
                  <span v-if="dept.managerName" class="manager-badge">
                    <i class="bi bi-person-badge"></i> {{ dept.managerName }}
                  </span>
                <span v-else class="text-muted">—</span>
              </td>
              <td class="td-meta">{{ dept.employeeCount || 0 }}</td>
              <td class="td-meta">{{ formatDate(dept.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="canEdit" class="icon-btn" data-bs-toggle="modal" data-bs-target="#departmentModal" @click="openEdit(dept)" title="Sửa">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button v-if="canDelete" class="icon-btn danger" data-bs-toggle="modal" data-bs-target="#deleteDepartmentModal" @click="confirmDelete(dept)" title="Xóa">
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
          <span class="pagination-info">{{ startEntry }}–{{ endEntry }} / {{ pagination.totalElements }}</span>
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
    <div class="modal fade" id="departmentModal" tabindex="-1" aria-labelledby="departmentModalLabel" aria-hidden="true" ref="departmentModalEl">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">{{ modal.isEdit ? 'Sửa phòng ban' : 'Thêm phòng ban' }}</h5>
              <p class="modal-subtitle">{{ modal.isEdit ? 'Cập nhật thông tin phòng ban' : 'Tạo phòng ban mới' }}</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="modal-body-custom">
            <div v-if="errors.general" class="alert-error">
              <i class="bi bi-exclamation-triangle"></i> {{ errors.general }}
            </div>

            <div class="field" :class="{ 'field-error': errors.name }">
              <label>Tên phòng ban <span class="req">*</span></label>
              <div class="input-wrap">
                <i class="bi bi-building"></i>
                <input v-model="form.name" type="text" placeholder="Nhập tên phòng ban" @input="clearError('name')" />
              </div>
              <span v-if="errors.name" class="err-msg">{{ errors.name }}</span>
            </div>

            <div class="field">
              <label>Mô tả</label>
              <textarea v-model="form.description" rows="3" class="textarea-field" placeholder="Mô tả phòng ban..."></textarea>
            </div>

            <div class="field">
              <label>Trưởng phòng</label>
              <div class="select-wrap">
                <select v-model="form.managerId">
                  <option :value="null">— Chọn trưởng phòng —</option>
                  <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ emp.fullName }}</option>
                </select>
                <i class="bi bi-chevron-down"></i>
              </div>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn-primary" :disabled="submitting" @click="submitForm">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else>
                <i :class="modal.isEdit ? 'bi bi-check-lg' : 'bi bi-plus-lg'"></i>
                {{ modal.isEdit ? 'Lưu thay đổi' : 'Tạo phòng ban' }}
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div class="modal fade" id="deleteDepartmentModal" tabindex="-1" aria-hidden="true" ref="deleteModalEl">
      <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content modal-custom">
          <div class="modal-header-custom">
            <div>
              <h5 class="modal-title">Xóa phòng ban</h5>
              <p class="modal-subtitle">Hành động này không thể hoàn tác</p>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal"><i class="bi bi-x-lg"></i></button>
          </div>
          <div class="modal-body-custom delete-body">
            <div class="del-avatar" :style="deleteModal.department ? { background: avatarGradient(deleteModal.department.name) } : {}">
              {{ deleteModal.department ? getInitials(deleteModal.department.name) : '' }}
            </div>
            <p class="del-name">{{ deleteModal.department?.name }}</p>
            <p class="del-sub">{{ deleteModal.department?.description || 'Không có mô tả' }}</p>
            <p v-if="deleteModal.department?.employeeCount > 0" class="del-warn warning">
              <i class="bi bi-exclamation-triangle"></i> Phòng ban này có {{ deleteModal.department.employeeCount }} nhân viên. Không thể xóa!
            </p>
            <p v-else class="del-warn">Xóa vĩnh viễn phòng ban này?</p>
          </div>
          <div class="modal-footer-custom">
            <button type="button" class="btn-ghost" data-bs-dismiss="modal">Hủy</button>
            <button type="button" class="btn-danger" :disabled="submitting || deleteModal.department?.employeeCount > 0" @click="doDelete">
              <span v-if="submitting" class="spin-sm"></span>
              <template v-else><i class="bi bi-trash"></i> Xóa</template>
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
import departmentService from '@/services/department.service.js'
import employeeService from '@/services/employee.service.js'
import { useToast } from '@/utils/toast'
import { useAuthStore } from '@/stores/auth.store.js'

const toast = useToast()
const auth = useAuthStore()

const departments = ref([])
const employees = ref([])
const loading = ref(false)
const submitting = ref(false)

const canCreate = computed(() => auth.can('DEPARTMENT_CREATE'))
const canEdit = computed(() => auth.can('DEPARTMENT_UPDATE'))
const canDelete = computed(() => auth.can('DEPARTMENT_DELETE'))

const departmentModalEl = ref(null)
const deleteModalEl = ref(null)
let bsDepartmentModal = null
let bsDeleteModal = null

const filters = reactive({ keyword: '' })
const sort = reactive({ sortBy: 'createdAt', sortDir: 'desc' })
const pagination = reactive({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: false })

const totalEmployees = computed(() => departments.value.reduce((sum, d) => sum + (d.employeeCount || 0), 0))

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
  bsDepartmentModal = new Modal(departmentModalEl.value)
  bsDeleteModal = new Modal(deleteModalEl.value)
  departmentModalEl.value.addEventListener('hidden.bs.modal', () => { resetForm() })
  fetchDepartments()
  fetchEmployees()
})

onUnmounted(() => {
  bsDepartmentModal?.dispose()
  bsDeleteModal?.dispose()
})

async function fetchEmployees() {
  try {
    const response = await employeeService.getAll({ size: 1000 })
    employees.value = response.data?.content || []
  } catch (error) {
    console.error('Không thể tải nhân viên:', error)
  }
}

async function fetchDepartments() {
  loading.value = true
  try {
    const { data } = await departmentService.getAll({
      keyword: filters.keyword || undefined,
      page: pagination.page,
      size: pagination.size,
      sortBy: sort.sortBy,
      sortDir: sort.sortDir
    })
    departments.value = data.content || []
    pagination.totalElements = data.totalElements || 0
    pagination.totalPages = data.totalPages || 0
    pagination.last = data.last || false
  } catch (error) {
    toast.error('Không thể tải danh sách phòng ban')
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
    fetchDepartments()
  }, 400)
}

function clearSearch() {
  filters.keyword = ''
  pagination.page = 0
  fetchDepartments()
}

function toggleSort(col) {
  if (sort.sortBy === col) {
    sort.sortDir = sort.sortDir === 'desc' ? 'asc' : 'desc'
  } else {
    sort.sortBy = col
    sort.sortDir = 'desc'
  }
  fetchDepartments()
}

function getSortIcon(col) {
  if (sort.sortBy !== col) return 'bi-arrow-down-up'
  return sort.sortDir === 'desc' ? 'bi-arrow-down' : 'bi-arrow-up'
}

function goPage(page) {
  pagination.page = page
  fetchDepartments()
}

function onSizeChange() {
  pagination.page = 0
  fetchDepartments()
}

const modal = reactive({ isEdit: false, departmentId: null })
const errors = reactive({})
const form = reactive({ name: '', description: '', managerId: null })
const deleteModal = reactive({ department: null })

function resetForm() {
  form.name = ''
  form.description = ''
  form.managerId = null
  Object.keys(errors).forEach(key => delete errors[key])
}

function clearError(field) {
  delete errors[field]
}

function openCreate() {
  resetForm()
  modal.isEdit = false
  modal.departmentId = null
  bsDepartmentModal.show()
}

function openEdit(dept) {
  resetForm()
  form.name = dept.name
  form.description = dept.description || ''
  form.managerId = dept.managerId
  modal.isEdit = true
  modal.departmentId = dept.id
  bsDepartmentModal.show()
}

function confirmDelete(dept) {
  deleteModal.department = dept
  bsDeleteModal.show()
}

async function submitForm() {
  submitting.value = true
  Object.keys(errors).forEach(key => delete errors[key])

  try {
    if (modal.isEdit) {
      await departmentService.update(modal.departmentId, form)
      toast.success('Đã cập nhật phòng ban')
    } else {
      await departmentService.create(form)
      toast.success('Đã tạo phòng ban')
    }

    pagination.page = 0
    await fetchDepartments()
    bsDepartmentModal.hide()
  } catch (err) {
    if (err?.errors) {
      Object.assign(errors, err.errors)
    } else {
      errors.general = err?.message || 'Đã xảy ra lỗi'
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
    await departmentService.delete(deleteModal.department.id)
    await fetchDepartments()
    toast.success(`Đã xóa phòng ban ${deleteModal.department.name}`)
    bsDeleteModal.hide()
  } catch (error) {
    toast.error(error?.message || 'Không thể xóa phòng ban')
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

* { margin: 0; padding: 0; box-sizing: border-box; }

.department-management {
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
tbody tr.highlight-row { background: #f0fdf4; }
td { padding: 12px 16px; font-size: 13.5px; color: #333; vertical-align: middle; }
.td-num { color: #bbb; font-size: 12px; }
.td-meta { color: #888; font-size: 12.5px; }
.td-desc { color: #888; font-size: 13px; max-width: 220px; }

.dept-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.dept-name { font-size: 13.5px; font-weight: 600; color: #111; }

.manager-badge { display: inline-flex; align-items: center; gap: 4px; padding: 3px 8px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #555; }
.manager-badge i { font-size: 10px; color: #6366f1; }

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
.input-wrap input { width: 100%; padding: 9px 12px 9px 34px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #111; background: #fff; transition: all 0.2s; }
.input-wrap input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.input-wrap input::placeholder { color: #ccc; }

.textarea-field { width: 100%; padding: 9px 12px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #111; background: #fff; resize: vertical; transition: all 0.2s; }
.textarea-field:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.textarea-field::placeholder { color: #ccc; }

.select-wrap { position: relative; }
.select-wrap select { width: 100%; padding: 9px 30px 9px 11px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px; font-family: inherit; color: #333; background: #fff; appearance: none; cursor: pointer; transition: all 0.2s; }
.select-wrap select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99,102,241,0.1); }
.select-wrap .bi-chevron-down { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); color: #aaa; font-size: 11px; pointer-events: none; }

.delete-body { text-align: center; }
.del-avatar { width: 54px; height: 54px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 22px; font-weight: 700; margin: 0 auto 12px; }
.del-name { font-size: 15px; font-weight: 700; color: #111; }
.del-sub { font-size: 12.5px; color: #999; margin-top: 2px; }
.del-warn { font-size: 13px; color: #666; margin-top: 12px; }
.del-warn.warning { color: #dc2626; background: #fef2f2; padding: 8px; border-radius: 8px; }
.del-warn.warning i { margin-right: 6px; }

.spin-sm { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.text-muted { color: #999; }

@media (max-width: 768px) {
  .department-management { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; }
  .search-wrapper { min-width: 100%; }
  .pagination-bar { flex-direction: column; align-items: flex-start; }
}
</style>
