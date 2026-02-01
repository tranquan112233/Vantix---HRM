<script setup>
import { ref, onMounted } from 'vue'
import DepartmentService from "../../assets/services/department.service.js";

/* ================= DATA ================= */
const departments = ref([])
const form = ref({
  departmentID: null,
  departmentName: '',
  description: ''
})

const isEdit = ref(false)

/* ===== VIEW EMPLOYEES (XEM NHANH) ===== */
const selectedDepartment = ref(null)
const employees = ref([]) // sau này gọi API employee

/* ================= LOAD DATA ================= */
const loadDepartments = async () => {
  try {
    const res = await DepartmentService.getAll()
    departments.value = res.data
  } catch (e) {
    alert('Không tải được danh sách phòng ban')
  }
}

onMounted(loadDepartments)

/* ================= FORM ================= */
const openAdd = () => {
  isEdit.value = false
  form.value = {
    departmentID: null,
    departmentName: '',
    description: ''
  }
}

const openEdit = (item) => {
  isEdit.value = true
  form.value = { ...item }
}

/* ================= SAVE ================= */
const save = async () => {
  if (!form.value.departmentName) {
    alert('Tên phòng ban không được để trống')
    return
  }

  try {
    if (isEdit.value) {
      await DepartmentService.update(
          form.value.departmentID,
          form.value
      )
    } else {
      await DepartmentService.create(form.value)
    }

    document.getElementById('closeDepartmentModal').click()
    await loadDepartments()
  } catch (e) {
    alert('Lưu phòng ban thất bại')
  }
}

/* ================= DELETE ================= */
const remove = async (id) => {
  if (!confirm('Xóa phòng ban này?')) return

  try {
    await DepartmentService.delete(id)
    await loadDepartments()
  } catch (e) {
    alert(e.response?.data || 'Không thể xóa phòng ban')
  }
}

/* ================= XEM NHANH NHÂN VIÊN ================= */
const openEmployees = (department) => {
  selectedDepartment.value = department

  // TODO: gọi API /employees?departmentId=
  employees.value = []
}
</script>

<template>
  <div>
    <!-- ===== HEADER ===== -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-semibold mb-0">Quản lý phòng ban</h4>
      <button
          class="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#departmentModal"
          @click="openAdd"
      >
        <i class="bi bi-plus-circle me-1"></i> Thêm phòng ban
      </button>
    </div>

    <!-- ===== TABLE ===== -->
    <div class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th>#</th>
            <th>Tên phòng ban</th>
            <th>Mô tả</th>
            <th class="text-end">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="departments.length === 0">
            <td colspan="4" class="text-center text-muted py-4">
              Chưa có phòng ban
            </td>
          </tr>

          <tr v-for="(item, index) in departments" :key="item.departmentID">
            <td>{{ index + 1 }}</td>
            <td class="fw-medium">{{ item.departmentName }}</td>
            <td class="text-muted">{{ item.description }}</td>
            <td class="text-end">
              <!-- 👁 Xem nhanh -->
              <button
                  class="btn btn-sm btn-outline-secondary me-2"
                  data-bs-toggle="modal"
                  data-bs-target="#employeeModal"
                  @click="openEmployees(item)"
              >
                <i class="bi bi-eye"></i>
              </button>

              <!-- ✏️ Sửa -->
              <button
                  class="btn btn-sm btn-outline-primary me-2"
                  data-bs-toggle="modal"
                  data-bs-target="#departmentModal"
                  @click="openEdit(item)"
              >
                <i class="bi bi-pencil"></i>
              </button>

              <!-- 🗑 Xóa -->
              <button
                  class="btn btn-sm btn-outline-danger"
                  @click="remove(item.departmentID)"
              >
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===== MODAL ADD / EDIT ===== -->
    <div class="modal fade" id="departmentModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? 'Cập nhật phòng ban' : 'Thêm phòng ban' }}
            </h5>
            <button
                id="closeDepartmentModal"
                class="btn-close"
                data-bs-dismiss="modal"
            ></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">Tên phòng ban</label>
              <input
                  v-model="form.departmentName"
                  class="form-control"
                  placeholder="VD: Phòng Nhân Sự"
              />
            </div>

            <div class="mb-3">
              <label class="form-label">Mô tả</label>
              <textarea
                  v-model="form.description"
                  class="form-control"
                  rows="3"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" data-bs-dismiss="modal">
              Hủy
            </button>
            <button class="btn btn-primary" @click="save">
              {{ isEdit ? 'Lưu thay đổi' : 'Thêm mới' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== MODAL VIEW EMPLOYEES ===== -->
    <div class="modal fade" id="employeeModal">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title">
              Nhân viên –
              <span class="text-primary">
                {{ selectedDepartment?.departmentName }}
              </span>
            </h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <table class="table align-middle">
              <thead class="table-light">
              <tr>
                <th>#</th>
                <th>Họ tên</th>
                <th>Chức vụ</th>
                <th>Trạng thái</th>
              </tr>
              </thead>

              <tbody>
              <tr v-if="employees.length === 0">
                <td colspan="4" class="text-center text-muted py-3">
                  Không có nhân viên
                </td>
              </tr>

              <tr v-for="(emp, index) in employees" :key="emp.userID">
                <td>{{ index + 1 }}</td>
                <td class="fw-medium">{{ emp.fullName }}</td>
                <td>{{ emp.role }}</td>
                <td>
                    <span
                        class="badge"
                        :class="{
                        'bg-success': emp.status === 'Working',
                        'bg-warning': emp.status === 'OnLeave',
                        'bg-secondary': emp.status === 'Resigned'
                      }"
                    >
                      {{ emp.status }}
                    </span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" data-bs-dismiss="modal">
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
