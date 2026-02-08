<template>
  <div class="container py-4">

    <!-- TITLE -->
    <h4 class="fw-bold mb-3">
      <i class="bi bi-diagram-3-fill me-2"></i> Quản lý Department
    </h4>

    <!-- SEARCH + ADD -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="input-group w-50">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input
            v-model="searchText"
            class="form-control"
            placeholder="Tìm kiếm department..."
        />
      </div>

      <button
          class="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#departmentModal"
          @click="openCreate"
      >
        <i class="bi bi-plus-circle me-1"></i> Thêm mới
      </button>
    </div>

    <!-- TABLE -->
    <div class="card shadow-sm border-0 mb-3">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-uppercase small text-secondary">
          <tr>
            <th class="text-center" style="width:60px">#</th>

            <th class="sortable" @click="sort('name')">
              Department <i class="bi ms-1" :class="sortIcon('name')"></i>
            </th>

            <th class="sortable" @click="sort('description')">
              Mô tả <i class="bi ms-1" :class="sortIcon('description')"></i>
            </th>

            <th class="text-end" style="width:140px">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(d, index) in paginatedData" :key="d.id">
            <td class="text-center text-muted fw-semibold">
              {{ (currentPage - 1) * pageSize + index + 1 }}
            </td>

            <td class="fw-semibold">
              {{ d.name }}
            </td>

            <td class="text-muted small">
              {{ d.description || '-' }}
            </td>

            <td class="text-end">
              <div class="btn-group btn-group-sm">
                <button
                    class="btn btn-outline-warning"
                    data-bs-toggle="modal"
                    data-bs-target="#departmentModal"
                    @click="openEdit(d)"
                >
                  <i class="bi bi-pencil-square"></i>
                </button>

                <button
                    class="btn btn-outline-danger"
                    @click="remove(d)"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </td>
          </tr>

          <tr v-if="paginatedData.length === 0">
            <td colspan="4" class="text-center text-muted py-4">
              Không có dữ liệu
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- PAGINATION -->
    <div class="d-flex justify-content-between align-items-center">
      <div class="d-flex align-items-center gap-2">
        <span>Xem</span>
        <select v-model.number="pageSize" class="form-select w-auto">
          <option :value="10">10</option>
          <option :value="25">25</option>
        </select>
        <span>mục</span>
      </div>

      <ul class="pagination mb-0">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="changePage(currentPage - 1)">‹</button>
        </li>

        <li
            v-for="p in totalPages"
            :key="p"
            class="page-item"
            :class="{ active: p === currentPage }"
        >
          <button class="page-link" @click="changePage(p)">
            {{ p }}
          </button>
        </li>

        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="changePage(currentPage + 1)">›</button>
        </li>
      </ul>
    </div>

    <!-- MODAL -->
    <div class="modal fade" id="departmentModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? 'Cập nhật Department' : 'Thêm Department' }}
            </h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">

            <div class="mb-3">
              <label class="form-label">Tên Department</label>
              <input
                  v-model="form.name"
                  class="form-control"
                  :class="{ 'is-invalid': !!errors.name }"
              />
              <small class="text-danger">{{ errors.name }}</small>
            </div>

            <div class="mb-3">
              <label class="form-label">Mô tả</label>
              <textarea
                  v-model="form.description"
                  rows="3"
                  class="form-control"
              ></textarea>
            </div>

          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal" ref="btnCancel">
              Hủy
            </button>
            <button class="btn btn-primary" @click="save">
              Lưu
            </button>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import DepartmentService from '@/services/department.service'
import { useSearch } from '@/composables/useSearch'
import { useMultiSort } from '@/composables/useMultiSort'
import { usePagination } from '@/composables/usePagination'
import { useToast } from '@/composables/useToast'

const departments = ref([])
const isEdit = ref(false)

/* SEARCH */
const { searchText, filteredData } = useSearch(departments, ['name', 'description'])

/* SORT */
const { sort, sortIcon, sortedData } = useMultiSort(filteredData)

/* PAGINATION */
const {
  currentPage,
  pageSize,
  totalPages,
  paginatedData,
  changePage
} = usePagination(sortedData)

/* FORM */
const form = ref({
  id: null,
  name: '',
  description: ''
})

const errors = reactive({})
const btnCancel = ref(null)
const { showToast } = useToast()

/* LOAD */
const loadData = async () => {
  departments.value = (await DepartmentService.getAll()).data
}

/* VALIDATE */
const validate = () => {
  Object.keys(errors).forEach(k => delete errors[k])

  if (!form.value.name || !form.value.name.trim()) {
    errors.name = 'Tên department bắt buộc'
  }

  const exists = departments.value.some(d =>
      d.name.toLowerCase() === form.value.name.toLowerCase()
      && d.id !== form.value.id
  )

  if (exists) {
    errors.name = 'Department đã tồn tại'
  }

  return Object.keys(errors).length === 0
}

/* MODAL */
const openCreate = () => {
  isEdit.value = false
  Object.assign(form.value, { id: null, name: '', description: '' })
  Object.keys(errors).forEach(k => delete errors[k])
}

const openEdit = (d) => {
  isEdit.value = true
  Object.assign(form.value, d)
  Object.keys(errors).forEach(k => delete errors[k])
}

/* SAVE */
const save = async () => {
  if (!validate()) return

  try {
    if (isEdit.value) {
      await DepartmentService.update(form.value.id, form.value)
      showToast('Cập nhật department thành công')
    } else {
      await DepartmentService.create(form.value)
      showToast('Tạo department thành công')
    }

    await loadData()
    btnCancel.value.click()
  } catch {
    showToast('Có lỗi xảy ra', 'danger')
  }
}

/* DELETE */
const remove = async (d) => {
  if (!confirm(`Xóa department "${d.name}"?`)) return
  await DepartmentService.delete(d.id)
  showToast('Đã xóa department')
  loadData()
}

watch([searchText, pageSize], () => currentPage.value = 1)
onMounted(loadData)
</script>

<style scoped>
.sortable {
  cursor: pointer;
  user-select: none;
}
</style>
