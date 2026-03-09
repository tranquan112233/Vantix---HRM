<template>
  <div class="container py-4">
    <div class="card shadow rounded border-0">
      <div class="card-body p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-diagram-3-fill me-2 text-primary"></i>
              Department Management
            </h4>
            <small class="text-muted">
              Manage company departments
            </small>
          </div>

          <button class="btn btn-primary" @click="openAdd">
            <i class="bi bi-plus-lg"></i>
            Add Department
          </button>
        </div>

        <!-- SEARCH -->
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
              <th>#</th>
              <th @click="sortBy('departmentName')" class="sortable">
                Department
                <i :class="getSortIcon('departmentName')" class="ms-1"></i>
              </th>
              <th @click="sortBy('description')" class="sortable">
                Description
                <i :class="getSortIcon('description')" class="ms-1"></i>
              </th>
              <th class="text-center">
                Employees
              </th>
              <th class="text-end">Action</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="(department, index) in paginatedData" :key="department.departmentId">

              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>

              <td class="fw-semibold">
                {{ department.departmentName }}
              </td>

              <td class="text-muted">
                {{ department.description }}
              </td>

              <td class="text-muted text-center">
                  {{ getEmployeeCount(department.departmentId) }}
              </td>

              <td class="text-end">

                <!-- VIEW -->
                <button class="action-btn view-btn"
                        @click="viewEmployees(department)">
                  <i class="bi bi-eye"></i>
                </button>

                <!-- EDIT -->
                <button class="action-btn edit-btn"
                        @click="openEdit(department)">
                  <i class="bi bi-pencil"></i>
                </button>

                <!-- DELETE -->
                <button class="action-btn delete-btn"
                        @click="removeDepartment(department.departmentId)">
                  <i class="bi bi-trash"></i>
                </button>

              </td>

            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="5" class="text-center text-muted py-4">
                No departments found
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

    <!-- MODAL CREATE / EDIT -->
    <div class="modal fade" data-bs-backdrop="static" id="departmentModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content rounded-4 border-0 shadow">

          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              {{ isEdit ? "Edit Department" : "Create Department" }}
            </h5>
            <button type="button"
                    class="btn-close btn-close-white"
                    @click="closeModal"></button>
          </div>

          <div class="modal-body">

            <div class="mb-3">
              <label class="form-label fw-semibold">
                Department Name
              </label>

              <input
                  type="text"
                  class="form-control"
                  v-model="form.departmentName"
                  placeholder="Enter department name"
              />
            </div>

            <div>
              <label class="form-label fw-semibold">
                Description
              </label>

              <textarea
                  class="form-control"
                  rows="2"
                  v-model="form.description">
              </textarea>

            </div>

          </div>

          <div class="modal-footer border-0">

            <button class="btn btn-light"
                    @click="closeModal">
              Cancel
            </button>

            <button class="btn btn-primary"
                    @click="saveDepartment">
              {{ isEdit ? "Update" : "Save" }}
            </button>

          </div>

        </div>
      </div>
    </div>

    <!-- MODAL VIEW EMPLOYEES -->
    <div class="modal fade" id="employeeModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content rounded-4 border-0 shadow">

          <div class="modal-header">
            <h5 class="modal-title">
              Employees - {{ selectedDepartment?.departmentName }}
            </h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">

            <div class="table-responsive">
              <table class="table align-middle">

                <thead>
                <tr>
                  <th>#</th>
                  <th>Full Name</th>
                  <th>Gender</th>
                  <th>Phone</th>
                  <th>Address</th>
                  <th>Position</th>
                  <th>Status</th>
                </tr>
                </thead>

                <tbody>

                <tr v-for="(emp,index) in departmentEmployees"
                    :key="emp.employeeId">

                  <td>{{ index + 1 }}</td>

                  <td class="fw-semibold">
                    {{ emp.fullName }}
                  </td>

                  <td>
                <span class="badge"
                      :class="emp.gender === 'MALE'
                      ? 'bg-primary'
                      : emp.gender === 'FEMALE'
                      ? 'bg-danger'
                      : 'bg-warning'">
                  {{ emp.gender }}
                </span>
                  </td>

                  <td>{{ emp.phone }}</td>

                  <td>{{ emp.address }}</td>

                  <td>
                <span class="badge bg-secondary">
                  {{ emp.positionName }}
                </span>
                  </td>

                  <td>
                <span class="badge"
                      :class="emp.workStatus === 'WORKING'
                      ? 'bg-success'
                      : 'bg-danger'">
                  {{ emp.workStatus }}
                </span>
                  </td>

                </tr>

                <tr v-if="departmentEmployees.length === 0">
                  <td colspan="7" class="text-center text-muted py-4">
                    No employees found
                  </td>
                </tr>

                </tbody>

              </table>
            </div>

          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue"
import * as bootstrap from "bootstrap"

import departmentService from "@/services/department.service.js"
import employeeService from "@/services/employee.service.js"

import { useToast } from "@/composables/useToast"
import { useSearch } from "@/composables/useSearch"
import { useSort } from "@/composables/useSort"
import { usePagination } from "@/composables/usePagination"

const employees = ref([])
const departments = ref([])

const selectedDepartment = ref(null)
const departmentEmployees = ref([])

const search = ref("")
const pageSize = ref(5)

const { showToast } = useToast()

let modalInstance = null
let employeeModal = null

const isEdit = ref(false)

const form = ref({
  departmentId: null,
  departmentName: "",
  description: ""
})

async function fetchDepartments() {

  const deptRes = await departmentService.getAll()
  const empRes = await employeeService.getAll()

  departments.value = deptRes.data
  employees.value = empRes.data
}

function getEmployeeCount(departmentId) {
  return employees.value.filter(
      e => e.departmentId === departmentId
  ).length
}

function viewEmployees(department) {

  selectedDepartment.value = department

  departmentEmployees.value = employees.value.filter(
      e => e.departmentId === department.departmentId
  )

  employeeModal.show()
}

onMounted(() => {

  modalInstance = new bootstrap.Modal(
      document.getElementById("departmentModal")
  )

  employeeModal = new bootstrap.Modal(
      document.getElementById("employeeModal")
  )

  fetchDepartments()
})

/* SEARCH */
const { filteredData } = useSearch(departments, search)

/* SORT */
const { sortedData, sortBy, getSortIcon } =
    useSort(filteredData, "departmentId")

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

watch(search, () => currentPage.value = 1)

function openAdd() {
  isEdit.value = false
  form.value = {
    departmentId:null,
    departmentName:"",
    description:""
  }
  modalInstance.show()
}

function openEdit(department) {
  isEdit.value = true
  form.value = { ...department }
  modalInstance.show()
}

async function saveDepartment() {

  if (isEdit.value) {
    await departmentService.update(
        form.value.departmentId,
        form.value
    )
    showToast("Department updated successfully","success")
  }
  else {
    await departmentService.create(form.value)
    showToast("Department created successfully","success")
  }

  modalInstance.hide()
  fetchDepartments()
}

async function removeDepartment(id) {

  if (!confirm("Are you sure?")) return

  await departmentService.delete(id)

  fetchDepartments()

  showToast("Department deleted successfully","success")
}

function closeModal() {
  modalInstance.hide()
}
</script>

<style scoped>
.sortable {
  cursor: pointer;
}

.action-btn{
  border:none;
  background:transparent;
  font-size:16px;
  padding:6px;
  transition:0.2s;
}

.view-btn{
  color:#6c757d;
}

.view-btn:hover{
  color:#0d6efd;
  transform:scale(1.15);
}

.edit-btn{
  color:#6c757d;
}

.edit-btn:hover{
  color:#0d6efd;
  transform:scale(1.15);
}

.delete-btn{
  color:#6c757d;
}

.delete-btn:hover{
  color:#dc3545;
  transform:scale(1.15);
}

</style>