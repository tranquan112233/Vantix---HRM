<template>
  <div class="container py-4">
    <div class="card shadow rounded border-0">
      <div class="card-body p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-people me-2 text-primary"></i>
              User Management
            </h4>
            <small class="text-muted">
              Manage system users
            </small>
          </div>

          <button class="btn btn-primary" @click="openAdd">
            <i class="bi bi-plus-lg"></i>
            Add User
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
            <select class="form-select form-select-sm w-auto"
                    v-model.number="pageSize">
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

              <th @click="sortBy('username')" class="sortable">
                Username
                <i :class="getSortIcon('username')" class="ms-1"></i>
              </th>

              <th @click="sortBy('email')" class="sortable">
                Email
                <i :class="getSortIcon('email')" class="ms-1"></i>
              </th>

              <th @click="sortBy('roleName')" class="sortable">Role
                <i :class="getSortIcon('lastLogin')" class="ms-1"></i>
              </th>

              <th @click="sortBy('lastLogin')" class="sortable">
                Last Login
                <i :class="getSortIcon('lastLogin')" class="ms-1"></i>
              </th>

              <th>Status</th>

              <th class="text-end">Action</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="(user, index) in paginatedData" :key="user.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="fw-semibold">{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>
                <span class="badge bg-primary">
                  {{ user.roleName }}
                </span>
              </td>

              <td>
                <span v-if="user.lastLogin">
                  {{ formatDate(user.lastLogin) }}
                </span>
                <span v-else class="text-muted">Never</span>
              </td>

              <td>
                <span class="badge"
                      :class="user.status === 'ACTIVE'
                        ? 'bg-success'
                        : 'bg-danger'">
                  {{ user.status }}
                </span>
              </td>

              <td class="text-end">
                <button class="action-btn edit-btn"
                        @click="openEdit(user)">
                  <i class="bi bi-pencil"></i>
                </button>

                <button v-if="user.status === 'ACTIVE'"
                        class="action-btn lock-btn"
                        @click="lockUser(user.id)">
                  <i class="bi bi-lock"></i>
                </button>

                <button v-else
                        class="action-btn unlock-btn"
                        @click="unlockUser(user.id)">
                  <i class="bi bi-unlock"></i>
                </button>
              </td>
            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="7"
                  class="text-center text-muted py-4">
                No users found
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

            <button v-for="page in visiblePages"
                    :key="page"
                    class="btn btn-sm"
                    :class="page === currentPage
                      ? 'btn-secondary'
                      : 'btn-light'"
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
    <div class="modal fade" id="userModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content rounded-4 border-0 shadow">

          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              {{ isEdit ? "Edit User" : "Create User" }}
            </h5>
            <button type="button"
                    class="btn-close btn-close-white"
                    @click="closeModal"></button>
          </div>

          <div class="modal-body">

            <div class="mb-3">
              <label class="form-label fw-semibold">Username</label>
              <input type="text"
                     class="form-control"
                     :class="{ 'is-invalid': errors.username || errors.general }"
                     v-model="form.username"/>
              <div class="invalid-feedback">
                {{ errors.username || errors.general }}
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Email</label>
              <input type="email"
                     class="form-control"
                     :class="{ 'is-invalid': errors.email }"
                     v-model="form.email"/>
              <div class="invalid-feedback">
                {{ errors.email }}
              </div>
            </div>

            <div class="mb-3" v-if="!isEdit">
              <label class="form-label fw-semibold">Password</label>
              <input type="password"
                     class="form-control"
                     :class="{ 'is-invalid': errors.password }"
                     v-model="form.password"/>
              <div class="invalid-feedback">
                {{ errors.password }}
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Role</label>
              <select class="form-select"
                      :class="{ 'is-invalid': errors.roleId }"
                      v-model="form.roleId">

                <option disabled value="">-- Select Role --</option>

                <option v-for="r in roleOptions"
                        :key="r.id"
                        :value="r.id">
                  {{ r.roleName }}
                </option>
              </select>

              <div class="invalid-feedback">
                {{ errors.roleId }}
              </div>
            </div>

            <div class="mb-3" v-if="isEdit">
              <label class="form-label fw-semibold">Status</label>
              <select class="form-select"
                      v-model="form.status">
                <option value="ACTIVE">ACTIVE</option>
                <option value="LOCKED">LOCKED</option>
              </select>
            </div>

          </div>

          <div class="modal-footer border-0">
            <button class="btn btn-light" @click="closeModal">
              Cancel
            </button>
            <button class="btn btn-primary" @click="saveUser">
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

import userService from "@/services/user.service"
import roleService from "@/services/role.service"

import { useToast } from "@/composables/useToast"
import { useSearch } from "@/composables/useSearch"
import { useSort } from "@/composables/useSort"
import { usePagination } from "@/composables/usePagination"

/* =====================================================
   STATE
===================================================== */

// Data
const users = ref([])
const roleOptions = ref([])

// UI State
const loading = ref(false)
const errors = ref({})
const { showToast } = useToast()

// Search + Pagination
const search = ref("")
const pageSize = ref(5)

// Form + Mode
const isEdit = ref(false)
const form = ref(getDefaultForm())

// Bootstrap modal
let modalInstance = null

/* =====================================================
   FETCH DATA
===================================================== */

async function fetchUsers() {
  loading.value = true
  try {
    const { data } = await userService.getAll()
    users.value = data
  } catch (err) {
    console.error("Fetch users error:", err)
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    const { data } = await roleService.getAll()
    roleOptions.value = data
  } catch (err) {
    console.error("Fetch roles error:", err)
  }
}

onMounted(() => {
  modalInstance = new bootstrap.Modal(
      document.getElementById("userModal")
  )
  fetchUsers()
  fetchRoles()
})

/* =====================================================
   SEARCH
===================================================== */

const { filteredData } = useSearch(users, search)

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

// Reset page khi search thay đổi
watch(search, () => (currentPage.value = 1))

/* =====================================================
   FORM HELPER
===================================================== */

// Default form
function getDefaultForm() {
  return {
    id: null,
    username: "",
    email: "",
    password: "",
    roleId: "",
    status: "ACTIVE"
  }
}

// Reset form
function resetForm() {
  form.value = getDefaultForm()
  errors.value = {}
}

// Clear error khi user nhập lại
watch(form, () => (errors.value = {}), { deep: true })

/* =====================================================
   CRUD
===================================================== */

function openAdd() {
  isEdit.value = false
  resetForm()
  modalInstance.show()
}

function openEdit(user) {
  isEdit.value = true
  form.value = { ...user, password: "" }
  errors.value = {}
  modalInstance.show()
}

async function saveUser() {
  errors.value = {}

  try {
    if (isEdit.value) {
      await userService.update(form.value.id, form.value)
      showToast("User updated successfully!", "success")
    } else {
      await userService.create(form.value)
      showToast("User created successfully!", "success")
    }

    modalInstance.hide()
    fetchUsers()

  } catch (err) {
    handleError(err)
  }
}

async function lockUser(id) {
  try {
    await userService.lock(id)
    showToast("User locked successfully!", "success")
    fetchUsers()
  } catch (err) {
    console.error("Lock error:", err)
  }
}

async function unlockUser(id) {
  try {
    await userService.unlock(id)
    showToast("User unlocked successfully!", "success")
    fetchUsers()
  } catch (err) {
    console.error("Unlock error:", err)
  }
}

function closeModal() {
  modalInstance.hide()
}

/* =====================================================
   ERROR HANDLER
===================================================== */

function handleError(err) {
  const data = err.response?.data

  if (!data) {
    errors.value.general = "Server error"
    return
  }

  if (data.validationErrors) {
    errors.value = { ...data.validationErrors }
  } else {
    errors.value.general = data.message
  }
}

/* =====================================================
   UTIL
===================================================== */

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleString("vi-VN")
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

/* Lock */
.lock-btn {
  color: #6c757d;
}
.unlock-btn {
  color: #6c757d;
}

.lock-btn:hover {
  color: #dc3545;
  transform: scale(1.15);
}
.unlock-btn:hover {
  color: #5fac65;
  transform: scale(1.15);
}
</style>