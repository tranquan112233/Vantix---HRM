<script setup>
import { ref, onMounted } from 'vue'
import UserService from '../../assets/services/user.service.js'
import DepartmentService from '../../assets/services/department.service.js'
import RoleService from '../../assets/services/role.service.js'

/* ================= DATA ================= */
const users = ref([])
const departments = ref([])
const roles = ref([])

const isEdit = ref(false)

/* ===== FORM ===== */
const form = ref({
  userID: null,
  username: '',
  password: '',
  fullName: '',
  gender: 'Male',
  email: '',
  phone: '',
  address: '',
  status: 'Working',
  roleID: null,
  departmentID: null
})

/* ================= LOAD DATA ================= */
const loadUsers = async () => {
  const res = await UserService.getAll()
  users.value = res.data
}

const loadDepartments = async () => {
  const res = await DepartmentService.getAll()
  departments.value = res.data
}

const loadRoles = async () => {
  const res = await RoleService.getAll()
  roles.value = res.data
}

onMounted(() => {
  loadUsers()
  loadDepartments()
  loadRoles()
})

/* ================= FORM ACTION ================= */
const openAdd = () => {
  isEdit.value = false
  form.value = {
    userID: null,
    username: '',
    password: '',
    fullName: '',
    gender: 'Male',
    email: '',
    phone: '',
    address: '',
    status: 'Working',
    roleID: null,
    departmentID: null
  }
}

const openEdit = (item) => {
  isEdit.value = true
  form.value = {
    userID: item.userID,
    username: item.username,
    password: '',
    fullName: item.fullName,
    gender: item.gender,
    email: item.email,
    phone: item.phone,
    address: item.address,
    status: item.status,
    roleID: item.role?.roleID,
    departmentID: item.department?.departmentID
  }
}

/* ================= SAVE ================= */
const save = async () => {
  if (!form.value.fullName || !form.value.username) {
    alert('Vui lòng nhập đầy đủ thông tin bắt buộc')
    return
  }

  try {
    if (isEdit.value) {
      await UserService.update(form.value.userID, form.value)
    } else {
      await UserService.create(form.value)
    }

    document.getElementById('closeUserModal').click()
    await loadUsers()
  } catch (e) {
    alert(e.response?.data || 'Lưu nhân viên thất bại')
  }
}

/* ================= DELETE ================= */
const remove = async (id) => {
  if (!confirm('Xóa nhân viên này?')) return

  try {
    await UserService.delete(id)
    await loadUsers()
  } catch (e) {
    alert(e.response?.data || 'Không thể xóa nhân viên')
  }
}
</script>

<template>
  <div>
    <!-- ===== HEADER ===== -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-semibold mb-0">Quản lý nhân viên</h4>
      <button
          class="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#userModal"
          @click="openAdd"
      >
        <i class="bi bi-plus-circle me-1"></i> Thêm nhân viên
      </button>
    </div>

    <!-- ===== TABLE ===== -->
    <div class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th>#</th>
            <th>Họ tên</th>
            <th>Giới tính</th>
            <th>Email</th>
            <th>Điện thoại</th>
            <th>Trạng thái</th>
            <th>Chức vụ</th>
            <th>Phòng ban</th>
            <th class="text-end">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-if="users.length === 0">
            <td colspan="9" class="text-center text-muted py-4">
              Chưa có nhân viên
            </td>
          </tr>

          <tr v-for="(u, i) in users" :key="u.userID">
            <td>{{ i + 1 }}</td>
            <td class="fw-medium">{{ u.fullName }}</td>
            <td>{{ u.gender }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.phone }}</td>
            <td>
              <span
                  class="badge"
                  :class="{
                  'bg-success': u.status === 'Working',
                  'bg-warning': u.status === 'OnLeave',
                  'bg-secondary': u.status === 'Resigned'
                }"
              >
                {{ u.status }}
              </span>
            </td>
            <td>{{ u.role?.roleName }}</td>
            <td>{{ u.department?.departmentName }}</td>

            <td class="text-end">
              <button
                  class="btn btn-sm btn-outline-primary me-2"
                  data-bs-toggle="modal"
                  data-bs-target="#userModal"
                  @click="openEdit(u)"
              >
                <i class="bi bi-pencil"></i>
              </button>

              <button
                  class="btn btn-sm btn-outline-danger"
                  @click="remove(u.userID)"
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
    <div class="modal fade" id="userModal">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? 'Cập nhật nhân viên' : 'Thêm nhân viên' }}
            </h5>
            <button
                id="closeUserModal"
                class="btn-close"
                data-bs-dismiss="modal"
            ></button>
          </div>

          <div class="modal-body row g-3">
            <div class="col-md-6">
              <label class="form-label">Username</label>
              <input v-model="form.username" class="form-control">
            </div>

            <div class="col-md-6" v-if="!isEdit">
              <label class="form-label">Mật khẩu</label>
              <input type="password" v-model="form.password" class="form-control">
            </div>

            <div class="col-md-6">
              <label class="form-label">Họ tên</label>
              <input v-model="form.fullName" class="form-control">
            </div>

            <div class="col-md-6">
              <label class="form-label">Giới tính</label>
              <select v-model="form.gender" class="form-select">
                <option value="Male">Nam</option>
                <option value="Female">Nữ</option>
                <option value="Other">Khác</option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Email</label>
              <input v-model="form.email" class="form-control">
            </div>

            <div class="col-md-6">
              <label class="form-label">Điện thoại</label>
              <input v-model="form.phone" class="form-control">
            </div>

            <div class="col-12">
              <label class="form-label">Địa chỉ</label>
              <input v-model="form.address" class="form-control">
            </div>

            <div class="col-md-6">
              <label class="form-label">Chức vụ</label>
              <select v-model="form.roleID" class="form-select">
                <option disabled value="">-- Chọn chức vụ --</option>
                <option
                    v-for="r in roles"
                    :key="r.roleID"
                    :value="r.roleID"
                >
                  {{ r.roleName }}
                </option>
              </select>
            </div>

            <div class="col-md-6">
              <label class="form-label">Phòng ban</label>
              <select v-model="form.departmentID" class="form-select">
                <option disabled value="">-- Chọn phòng ban --</option>
                <option
                    v-for="d in departments"
                    :key="d.departmentID"
                    :value="d.departmentID"
                >
                  {{ d.departmentName }}
                </option>
              </select>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
            <button class="btn btn-primary" @click="save">
              {{ isEdit ? 'Lưu thay đổi' : 'Thêm mới' }}
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
