<template>
  <div class="container py-4">
    <div class="card shadow rounded border-0">
      <div class="card-body p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-briefcase-fill me-2 text-primary"></i>
              Employee Management
            </h4>
            <small class="text-muted">
              Manage company employees
            </small>
          </div>

          <button class="btn btn-primary" @click="openAdd">
            <i class="bi bi-plus-lg"></i>
            Add Employee
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
              <th @click="sortBy('employeeId')" class="sortable">
                #
                <i :class="getSortIcon('employeeId')" class="ms-1"></i>
              </th>

              <th @click="sortBy('fullName')" class="sortable">
                Full Name
                <i :class="getSortIcon('fullName')" class="ms-1"></i>
              </th>

              <th @click="sortBy('gender')" class="sortable">
                Gender
                <i :class="getSortIcon('gender')" class="ms-1"></i>
              </th>

              <th @click="sortBy('birthDate')" class="sortable">
                Birth Date
                <i :class="getSortIcon('birthDate')" class="ms-1"></i>
              </th>

              <th @click="sortBy('phone')" class="sortable">
                Phone Number
                <i :class="getSortIcon('phone')" class="ms-1"></i>
              </th>

              <th @click="sortBy('address')" class="sortable">
                Address
                <i :class="getSortIcon('address')" class="ms-1"></i>
              </th>

              <th @click="sortBy('departmentName')" class="sortable">
                Department
                <i :class="getSortIcon('departmentName')" class="ms-1"></i>
              </th>

              <th @click="sortBy('positionName')" class="sortable">
                Position
                <i :class="getSortIcon('positionName')" class="ms-1"></i>
              </th>

              <th @click="sortBy('workStatus')" class="sortable">
                Status
                <i :class="getSortIcon('workStatus')" class="ms-1"></i>
              </th>

              <th class="text-end">Action</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="(emp, index) in paginatedData" :key="emp.employeeId">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="fw-semibold">{{ emp.fullName }}</td>
              <td>{{ emp.gender }}</td>
              <td>{{ emp.birthDate }}</td>
              <td>{{ emp.phone }}</td>
              <td>{{ emp.address }}</td>
              <td>{{ emp.departmentName }}</td>
              <td>{{ emp.positionName }}</td>
              <td>{{ emp.workStatus }}</td>

              <td class="text-end">
                <button class="action-btn edit-btn" @click="openEdit(emp)">
                  <i class="bi bi-pencil"></i>
                </button>

                <button class="action-btn delete-btn"
                        @click="removeEmployee(emp.employeeId)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="10" class="text-center text-muted py-4">
                No employees found
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
    <div class="modal fade" data-bs-backdrop="static" id="employeeModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content rounded-4 border-0 shadow">

          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? "Update Employee" : "Create Employee" }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>

          <div class="modal-body">

            <!-- CREATE MODE: USER INFO -->
            <template v-if="!isEdit">

              <h6 class="fw-bold text-primary mb-3">User Information</h6>

              <div class="row">

                <div class="col-md-6 mb-3">
                  <label class="form-label">Username</label>
                  <input type="text"
                         class="form-control"
                         v-model="form.username"
                         :class="{ 'is-invalid': errors.username }" />
                  <div class="invalid-feedback">
                    {{ errors.username }}
                  </div>
                </div>

                <div class="col-md-6 mb-3">
                  <label class="form-label">Email</label>
                  <input type="text"
                         class="form-control"
                         v-model="form.email"
                         :class="{ 'is-invalid': errors.email }" />
                  <div class="invalid-feedback">
                    {{ errors.email }}
                  </div>
                </div>

                <div class="col-md-6 mb-3">
                  <label class="form-label">Password</label>
                  <input type="password"
                         class="form-control"
                         v-model="form.password"
                         :class="{ 'is-invalid': errors.password }" />
                  <div class="invalid-feedback">
                    {{ errors.password }}
                  </div>
                </div>

                <div class="col-md-6 mb-3">
                  <label class="form-label">Role</label>
                  <select class="form-select"
                          v-model="form.roleId"
                          :class="{ 'is-invalid': errors.roleId }">
                    <option :value="null">Select Role</option>
                    <option v-for="r in roleOptions"
                            :key="r.roleId"
                            :value="r.roleId">
                      {{ r.roleName }}
                    </option>
                  </select>
                  <div class="invalid-feedback">
                    {{ errors.roleId }}
                  </div>
                </div>

              </div>

              <hr/>
            </template>

            <!-- EMPLOYEE INFO -->
            <h6 class="fw-bold text-success mb-3">Employee Information</h6>

            <div class="row">

              <div class="col-md-6 mb-3">
                <label class="form-label">Full Name</label>
                <input type="text"
                       class="form-control"
                       v-model="form.fullName"
                       :class="{ 'is-invalid': errors.fullName }"/>
                <div class="invalid-feedback">{{ errors.fullName }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Gender</label>
                <select class="form-select"
                        v-model="form.gender"
                        :class="{ 'is-invalid': errors.gender }">
                  <option value="">Select</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
                <div class="invalid-feedback">{{ errors.gender }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Birth Date</label>
                <input type="date"
                       class="form-control"
                       v-model="form.birthDate"
                       :class="{ 'is-invalid': errors.birthDate }"/>
                <div class="invalid-feedback">{{ errors.birthDate }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Phone</label>
                <input type="text"
                       class="form-control"
                       v-model="form.phone"
                       :class="{ 'is-invalid': errors.phone }"/>
                <div class="invalid-feedback">{{ errors.phone }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Address</label>
                <input type="text"
                       class="form-control"
                       v-model="form.address"
                       :class="{ 'is-invalid': errors.address }"/>
                <div class="invalid-feedback">{{ errors.address }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Department</label>
                <select class="form-select"
                        v-model="form.departmentId"
                        :class="{ 'is-invalid': errors.departmentId }">
                  <option disabled value="">Select</option>
                  <option v-for="d in departmentOptions"
                          :key="d.departmentId"
                          :value="d.departmentId">
                    {{ d.departmentName }}
                  </option>
                </select>
                <div class="invalid-feedback">{{ errors.departmentId }}</div>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Position</label>
                <select class="form-select"
                        v-model="form.positionId"
                        :class="{ 'is-invalid': errors.positionId }">
                  <option disabled value="">Select</option>
                  <option v-for="p in positionOptions"
                          :key="p.positionId"
                          :value="p.positionId">
                    {{ p.positionName }}
                  </option>
                </select>
                <div class="invalid-feedback">{{ errors.positionId }}</div>
              </div>

            </div>

          </div>

          <div class="modal-footer border-0">
            <button class="btn btn-light" @click="closeModal">Cancel</button>
            <button class="btn btn-primary" @click="saveEmployee">
              {{ isEdit ? "Update" : "Create" }}
            </button>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue"
import * as bootstrap from "bootstrap"

import employeeService from "@/services/employee.service.js";
import positionService from "@/services/position.service.js"
import departmentService from "@/services/department.service.js";
import roleService from "@/services/role.service.js";

import { useToast } from "@/composables/useToast"
import { useSearch } from "@/composables/useSearch"
import { useSort } from "@/composables/useSort"
import { usePagination } from "@/composables/usePagination"
import { useErrorHandler } from "@/composables/useErrorHandler"

const employees = ref([])
const positionOptions = ref([])
const departmentOptions = ref([])
const roleOptions = ref([])

const loading = ref(false)

const { showToast } = useToast()

const search = ref("")
const pageSize = ref(5)

const isEdit = ref(false)
const form = ref(getDefaultForm())

const { errors, handleError } = useErrorHandler()

let modalInstance = null

async function fetchEmployees() {
  loading.value = true
  try {
    const { data } = await employeeService.getAll()
    employees.value = data
  } catch (err) {
    console.error("Fetch employees error:", err)
  } finally {
    loading.value = false
  }
}

async function fetchPositions() {
  try {
    const { data } = await positionService.getAll()
    positionOptions.value = data
  } catch (err) {
    console.error("Fetch positions error:", err)
  }
}

async function fetchDepartments() {
  try {
    const { data } = await departmentService.getAll()
    departmentOptions.value = data
  } catch (err) {
    console.error("Fetch departments error:", err)
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
      document.getElementById("employeeModal")
  )
  fetchEmployees()
  fetchPositions()
  fetchDepartments()
  fetchRoles()
})

/* SEARCH */
const { filteredData } = useSearch(employees, search)

const { sortedData, sortBy, getSortIcon } =
    useSort(filteredData, "employeeId")

/* PAGINATION */
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

watch(search, () => (currentPage.value = 1))

function getDefaultForm() {
  return {
    username: "",
    email: "",
    password: "",
    roleId: "",
    fullName: "",
    gender: null,
    birthDate: "",
    phone: "",
    address: "",
    departmentId: "",
    positionId: "",
    workStatus: "WORKING",
  }
}

function resetForm() {
  form.value = getDefaultForm()
  errors.value = {}
}

watch(form, () => (errors.value = {}), { deep: true })

function openAdd() {
  isEdit.value = false
  resetForm()
  modalInstance.show()
}

function openEdit(emp) {
  isEdit.value = true
  form.value = {
    employeeId: emp.employeeId,
    fullName: emp.fullName,
    gender: emp.gender,
    birthDate: emp.birthDate,
    phone: emp.phone,
    address: emp.address,
    departmentId: emp.departmentId,
    positionId: emp.positionId,
    workStatus: emp.workStatus,
  }
  errors.value = {}
  modalInstance.show()
}

async function saveEmployee() {
  errors.value = {}

  try {
    if (isEdit.value) {
      await employeeService.update(form.value.employeeId, form.value)
      showToast("Employee updated successfully!", "success")
    } else {
      await employeeService.create(form.value)
      showToast("Employee created successfully!", "success")
    }

    modalInstance.hide()
    fetchEmployees()

  } catch (err) {
    handleError(err)
  }
}

async function removeEmployee(id) {
  if (!confirm("Are you sure?")) return

  try {
    await employeeService.delete(id)
    fetchEmployees()
    showToast("Employee deleted successfully!", "success")
  } catch (err) {
    console.error(err)
  }
}

function closeModal() {
  modalInstance.hide()
}
</script>

<style scoped>
.sortable {
  cursor: pointer;
}

.action-btn {
  border: none;
  background: transparent;
  padding: 6px;
  font-size: 16px;
  transition: all 0.2s ease;
  outline: none;
  box-shadow: none;
}

.action-btn:focus,
.action-btn:active {
  outline: none;
  box-shadow: none;
  background: transparent;
}

.edit-btn {
  color: #6c757d;
}

.edit-btn:hover {
  color: #0d6efd;
  transform: scale(1.15);
}

.delete-btn {
  color: #6c757d;
}

.delete-btn:hover {
  color: #dc3545;
  transform: scale(1.15);
}
</style>