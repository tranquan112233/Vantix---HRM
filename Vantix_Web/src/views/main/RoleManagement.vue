<template>
  <div class="container py-4">
    <div class="card shadow rounded border-0">
      <div class="card-body p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-shield-lock me-2 text-primary"></i>
              Role Management
            </h4>
            <small class="text-muted">
              Manage system roles and permissions
            </small>
          </div>

          <button class="btn btn-primary" @click="openAdd">
            <i class="bi bi-plus-lg"></i>
            Add Role
          </button>
        </div>

        <!-- SEARCH + PAGE SIZE -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div class="input-group w-25">
            <span class="input-group-text bg-white border-end-0">
              <i class="bi bi-search text-muted"></i>
            </span>
            <input
                type="text"
                class="form-control border-start-0"
                placeholder="Search..."
                v-model="search"
            />
          </div>

          <div class="d-flex align-items-center gap-2">
            <span class="small text-muted">Show</span>
            <select class="form-select form-select-sm w-auto" v-model.number="pageSize">
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
            </select>
            <span class="small text-muted">entries</span>
          </div>
        </div>

        <!-- TABLE -->
        <div class="table-responsive">
          <table class="table align-middle">
            <thead>
            <tr>
              <th @click="sortBy('id')" class="sortable">
                #
                <i :class="getSortIcon('id')" class="ms-1"></i>
              </th>

              <th @click="sortBy('name')" class="sortable">
                Role
                <i :class="getSortIcon('name')" class="ms-1"></i>
              </th>

              <th @click="sortBy('description')" class="sortable">
                Description
                <i :class="getSortIcon('description')" class="ms-1"></i>
              </th>

              <th class="text-end">Action</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="(role, index) in paginatedData" :key="role.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="fw-semibold">{{ role.roleName }}</td>
              <td class="text-muted">{{ role.description }}</td>

              <td class="text-end">
                <button class="action-btn edit-btn" @click="openEdit(role)">
                  <i class="bi bi-pencil"></i>
                </button>

                <button class="action-btn delete-btn" @click="removeRole(role.id)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="4" class="text-center text-muted py-4">
                No roles found
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- PAGINATION -->
        <div class="d-flex justify-content-between align-items-center mt-4">
          <div class="text-muted small">
            Showing {{ startItem }} - {{ endItem }}
            of {{ sortedData.length }} entries
          </div>

          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-light"
                    :disabled="currentPage === 1"
                    @click="prevPage">«</button>

            <button
                v-for="page in visiblePages"
                :key="page"
                class="btn btn-sm"
                :class="page === currentPage ? 'btn-secondary' : 'btn-light'"
                @click="currentPage = page">
              {{ page }}
            </button>

            <button class="btn btn-sm btn-light"
                    :disabled="currentPage === totalPages"
                    @click="nextPage">»</button>
          </div>
        </div>

      </div>
    </div>

    <!-- MODAL -->
    <div class="modal fade" id="roleModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              {{ isEdit ? "Edit Role" : "Create Role" }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label fw-semibold">Role Name</label>
              <input type="text" class="form-control" :class="{ 'is-invalid': errors.roleName || errors.general }" v-model="form.roleName"/>
              <div class="invalid-feedback">
                {{ errors.roleName || errors.general }}
              </div>
            </div>

            <div>
              <label class="form-label fw-semibold">Description</label>
              <textarea class="form-control" :class="{ 'is-invalid': errors.description }" rows="2" v-model="form.description"></textarea>
              <div class="invalid-feedback">
                {{ errors.description }}
              </div>
            </div>
          </div>

          <div class="modal-footer border-0">
            <button class="btn btn-light" @click="closeModal">Cancel</button>
            <button class="btn btn-primary" @click="saveRole">
              {{ isEdit ? "Update" : "Save" }}
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
/* =====================================================
   IMPORT
===================================================== */
import { ref, watch, onMounted } from "vue"
import * as bootstrap from "bootstrap"
import roleService from "@/services/role.service.js"

import { useToast } from "@/composables/useToast"
import { useSearch } from "@/composables/useSearch"
import { useSort } from "@/composables/useSort"
import { usePagination } from "@/composables/usePagination"
import {useErrorHandler} from "@/composables/useErrorHandler.js";

/* =====================================================
   STATE
===================================================== */

// Danh sách role
const roles = ref([])

// Loading khi gọi API
const loading = ref(false)

// Toast notification
const { showToast } = useToast()

// Search & pagination
const search = ref("")
const pageSize = ref(5)

// Form + mode
const isEdit = ref(false)
const form = ref({
  id: null,
  roleName: "",
  description: ""
})

// Error
const { errors, handleError } = useErrorHandler()

// Bootstrap modal instance
let modalInstance = null

/* =====================================================
   FETCH DATA
===================================================== */

async function fetchRoles() {
  loading.value = true
  try {
    const { data } = await roleService.getAll()
    roles.value = data
  } catch (err) {
    console.error("Fetch roles error:", err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  modalInstance = new bootstrap.Modal(
      document.getElementById("roleModal")
  )
  fetchRoles()
})

/* =====================================================
   SEARCH
===================================================== */

const { filteredData } = useSearch(roles, search)

/* =====================================================
   SORT
===================================================== */

const { sortedData, sortBy, getSortIcon } =
    useSort(filteredData, "id")

/* =====================================================
   PAGINATION
===================================================== */

const {
  currentPage,
  totalPages,
  paginatedData,
  visiblePages,
  startItem,
  endItem,
  nextPage,
  prevPage
} = usePagination(sortedData, pageSize)

// Reset về trang 1 khi search thay đổi
watch(search, () => (currentPage.value = 1))

/* =====================================================
   FORM HELPER
===================================================== */

// Reset form
function resetForm() {
  form.value = { id: null, roleName: "", description: "" }
  errors.value = {}
}

// Xóa lỗi khi user nhập lại
watch(form, () => (errors.value = {}), { deep: true })

/* =====================================================
   CRUD
===================================================== */

// Mở modal thêm mới
function openAdd() {
  isEdit.value = false
  resetForm()
  modalInstance.show()
}

// Mở modal chỉnh sửa
function openEdit(role) {
  isEdit.value = true
  form.value = { ...role }
  errors.value = {}
  modalInstance.show()
}

// Lưu role (create / update)
async function saveRole() {
  errors.value = {}

  try {
    if (isEdit.value) {
      await roleService.update(form.value.id, form.value)
      showToast("Role updated successfully!", "success")
    } else {
      await roleService.create(form.value)
      showToast("Role created successfully!", "success")
    }

    modalInstance.hide()
    fetchRoles()

  } catch (err) {
    handleError(err)
  }
}

// Xóa role
async function removeRole(id) {
  if (!confirm("Are you sure?")) return

  try {
    await roleService.delete(id)
    fetchRoles()
    showToast("Role deleted successfully!", "success")
  } catch (err) {
    console.error("Delete error:", err)
  }
}

// Đóng modal
function closeModal() {
  modalInstance.hide()
}

</script>

<style scoped>
.sortable {
  cursor: pointer;
}
/* ACTION BUTTON BASE */
.action-btn {
  border: none;
  background: transparent;
  padding: 6px;
  font-size: 16px;
  transition: all 0.2s ease;
  outline: none;
  box-shadow: none;
}

/* remove bootstrap focus */
.action-btn:focus,
.action-btn:active {
  outline: none;
  box-shadow: none;
  background: transparent;
}

/* EDIT */
.edit-btn {
  color: #6c757d;
}

.edit-btn:hover {
  color: #0d6efd;
  transform: scale(1.15);
}

/* DELETE */
.delete-btn {
  color: #6c757d;
}

.delete-btn:hover {
  color: #dc3545;
  transform: scale(1.15);
}
</style>