<template>
  <div class="container py-4">
    <div class="card shadow rounded border-0">
      <div class="card-body p-4">

        <!-- HEADER -->
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-briefcase-fill me-2 text-primary"></i>
              Position Management
            </h4>
            <small class="text-muted">
              Manage company positions
            </small>
          </div>

          <button class="btn btn-primary" @click="openAdd">
            <i class="bi bi-plus-lg"></i>
            Add Position
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
              <th @click="sortBy('positionId')" class="sortable">
                #
                <i :class="getSortIcon('positionId')" class="ms-1"></i>
              </th>

              <th @click="sortBy('positionName')" class="sortable">
                Position Name
                <i :class="getSortIcon('positionName')" class="ms-1"></i>
              </th>

              <th class="text-end">Action</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="(position, index) in paginatedData" :key="position.positionId">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="fw-semibold">{{ position.positionName }}</td>

              <td class="text-end">
                <button class="action-btn edit-btn" @click="openEdit(position)">
                  <i class="bi bi-pencil"></i>
                </button>

                <button class="action-btn delete-btn" @click="removePosition(position.positionId)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="3" class="text-center text-muted py-4">
                No positions found
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
    <div class="modal fade" data-bs-backdrop="static" id="positionModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? "Edit Position" : "Create Position" }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label fw-semibold">Position Name</label>
              <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': errors.positionName || errors.general }"
                  v-model="form.positionName"
                  placeholder="Enter position name"
              />
              <div class="invalid-feedback">
                {{ errors.positionName || errors.general }}
              </div>
            </div>
          </div>

          <div class="modal-footer border-0">
            <button class="btn btn-light" @click="closeModal">Cancel</button>
            <button class="btn btn-primary" @click="savePosition">
              {{ isEdit ? "Update" : "Save" }}
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
import positionService from "@/services/position.service.js"

import { useToast } from "@/composables/useToast"
import { useSearch } from "@/composables/useSearch"
import { useSort } from "@/composables/useSort"
import { usePagination } from "@/composables/usePagination"
import { useErrorHandler } from "@/composables/useErrorHandler"

const positions = ref([])
const loading = ref(false)

const { showToast } = useToast()

const search = ref("")
const pageSize = ref(5)

const isEdit = ref(false)
const form = ref({
  positionId: null,
  positionName: ""
})

const { errors, handleError } = useErrorHandler()

let modalInstance = null

async function fetchPositions() {
  loading.value = true
  try {
    const { data } = await positionService.getAll()
    positions.value = data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  modalInstance = new bootstrap.Modal(
      document.getElementById("positionModal")
  )
  fetchPositions()
})

/* SEARCH */
const { filteredData } = useSearch(positions, search)

/* SORT */
const { sortedData, sortBy, getSortIcon } =
    useSort(filteredData, "positionId")

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

function resetForm() {
  form.value = { positionId: null, positionName: "" }
  errors.value = {}
}

watch(form, () => (errors.value = {}), { deep: true })

function openAdd() {
  isEdit.value = false
  resetForm()
  modalInstance.show()
}

function openEdit(position) {
  isEdit.value = true
  form.value = { ...position }
  errors.value = {}
  modalInstance.show()
}

async function savePosition() {
  errors.value = {}

  try {
    if (isEdit.value) {
      await positionService.update(form.value.positionId, form.value)
      showToast("Position updated successfully!", "success")
    } else {
      await positionService.create(form.value)
      showToast("Position created successfully!", "success")
    }

    modalInstance.hide()
    fetchPositions()

  } catch (err) {
    handleError(err)
  }
}

async function removePosition(id) {
  if (!confirm("Are you sure?")) return

  try {
    await positionService.delete(id)
    fetchPositions()
    showToast("Position deleted successfully!", "success")
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