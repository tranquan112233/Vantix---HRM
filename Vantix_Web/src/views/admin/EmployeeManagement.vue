<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import EmployeeService from '@/services/employee.service'
import DepartmentService from '@/services/department.service'
import PositionService from '@/services/position.service'
import RoleService from '@/services/role.service'
import UserService from '@/services/user.service'

import { useSearch } from '@/composables/useSearch'
import { useMultiSort } from '@/composables/useMultiSort'
import { usePagination } from '@/composables/usePagination'
import { useToast } from '@/composables/useToast'

/* DATA */
const employees = ref([])
const departments = ref([])
const positions = ref([])
const roles = ref([])
const isEdit = ref(false)

/* SEARCH */
const { searchText, filteredData } = useSearch(employees, [
  'fullName',
  'phone',
  'department.name',
  'position.name',
  'workStatus'
])

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

  // employee
  fullName: '',
  gender: 'MALE',
  birthDate: null,
  phone: '',
  address: '',
  workStatus: 'WORKING',
  departmentId: null,
  positionId: null,

  // account
  username: '',
  email: '',
  password: '',
  roleId: null
})

const errors = reactive({})
const btnCancel = ref(null)
const { showToast } = useToast()

/* LOAD */
const loadData = async () => {
  employees.value = (await EmployeeService.getAll()).data
  departments.value = (await DepartmentService.getAll()).data
  positions.value = (await PositionService.getAll()).data
  roles.value = (await RoleService.getAll()).data
}

/* VALIDATE */
const validate = async () => {

  Object.keys(errors).forEach(k => delete errors[k])

  /* employee validate */

  if (!form.value.fullName?.trim())
    errors.fullName = 'Vui lòng nhập họ tên'

  if (!form.value.departmentId)
    errors.departmentId = 'Vui lòng chọn phòng ban'

  if (!form.value.positionId)
    errors.positionId = 'Vui lòng chọn chức vụ'

  if (form.value.phone &&
      !/^[0-9+\-\s()]{8,20}$/.test(form.value.phone))
    errors.phone = 'Số điện thoại không hợp lệ'


  /* account validatess ONLY CREATE */

  if (!isEdit.value) {

    if (!form.value.username?.trim())
      errors.username = 'Vui lòng nhập username'

    if (!form.value.email?.trim())
      errors.email = 'Vui lòng nhập email'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email))
      errors.email = 'Email không hợp lệ'

    if (!form.value.password)
      errors.password = 'Vui lòng nhập password'

    if (!form.value.roleId)
      errors.roleId = 'Vui lòng chọn role'


    /* CHECK EXISTING */

    if (!errors.username) {

      const existsUsername =
          await UserService.existsUsername(form.value.username)

      if (existsUsername.data)
        errors.username = 'Username đã tồn tại'
    }

    if (!errors.email) {

      const existsEmail =
          await UserService.existsEmail(form.value.email)

      if (existsEmail.data)
        errors.email = 'Email đã tồn tại'
    }

  }

  return Object.keys(errors).length === 0
}

/* OPEN CREATE */
const openCreate = () => {

  isEdit.value = false

  Object.assign(form.value, {

    id: null,

    fullName: '',
    gender: 'MALE',
    birthDate: null,
    phone: '',
    address: '',
    workStatus: 'WORKING',
    departmentId: null,
    positionId: null,

    username: '',
    email: '',
    password: '',
    roleId: null
  })

  Object.keys(errors).forEach(k => delete errors[k])
}

/* OPEN EDIT */
const openEdit = (e) => {

  isEdit.value = true

  Object.assign(form.value, {

    id: e.id,

    fullName: e.fullName,
    gender: e.gender || 'MALE',
    birthDate: e.birthDate || null,
    phone: e.phone || '',
    address: e.address || '',
    workStatus: e.workStatus || 'WORKING',

    departmentId: e.department?.id ?? null,
    positionId: e.position?.id ?? null,

    // account readonly
    username: e.user?.username || '',
    email: e.user?.email || '',
    password: '',
    roleId: e.user?.role?.id || null
  })

  Object.keys(errors).forEach(k => delete errors[k])
}

/* SAVE */
const save = async () => {

  if (!(await validate()))
    return

  const payload = {

    fullName: form.value.fullName,
    gender: form.value.gender,
    birthDate: form.value.birthDate,
    phone: form.value.phone,
    address: form.value.address,
    workStatus: form.value.workStatus,
    departmentId: form.value.departmentId,
    positionId: form.value.positionId
  }

  if (!isEdit.value) {

    payload.username = form.value.username
    payload.email = form.value.email
    payload.password = form.value.password
    payload.roleId = form.value.roleId
  }

  try {

    if (isEdit.value)
      await EmployeeService.update(form.value.id, payload)
    else
      await EmployeeService.create(payload)

    showToast(
        isEdit.value
            ? 'Cập nhật thành công'
            : 'Tạo thành công',
        'success'
    )

    await loadData()

    btnCancel.value.click()

  }
  catch (e) {

    showToast(
        e?.response?.data?.message ||
        'Có lỗi xảy ra',
        'danger'
    )
  }
}

/* DELETE */
const remove = async (e) => {

  if (!confirm(`Xóa nhân viên "${e.fullName}"?`))
    return

  await EmployeeService.remove(e.id)

  showToast('Đã xóa nhân viên', 'success')

  loadData()
}

/* UTILS */
const formatDateOnly = (v) =>
    v ? new Date(v).toLocaleDateString() : '-'

const genderLabel = (g) =>
    g === 'MALE' ? 'Nam' :
        g === 'FEMALE' ? 'Nữ' : 'Khác'


watch([searchText, pageSize],
    () => currentPage.value = 1)

onMounted(loadData)

</script>

<template>
  <div class="container py-4">
    <!-- TITLE -->
    <h4 class="fw-bold mb-3">
      <i class="bi bi-people-fill me-2"></i> Quản lý Nhân viên
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
          placeholder="Tìm theo họ tên / SĐT / phòng ban / chức vụ..."
        />
      </div>

      <button
        class="btn btn-primary"
        data-bs-toggle="modal"
        data-bs-target="#employeeModal"
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

              <th class="sortable" @click="sort('fullName')">
                Họ tên <i class="bi ms-1" :class="sortIcon('fullName')"></i>
              </th>

              <th class="sortable text-center" @click="sort('gender')">
                Giới tính <i class="bi ms-1" :class="sortIcon('gender')"></i>
              </th>

              <th class="text-center">
                Ngày sinh
              </th>

              <th class="sortable" @click="sort('phone')">
                SĐT <i class="bi ms-1" :class="sortIcon('phone')"></i>
              </th>

              <th class="sortable" @click="sort('department.name')">
                Phòng ban <i class="bi ms-1" :class="sortIcon('department.name')"></i>
              </th>

              <th class="sortable" @click="sort('position.name')">
                Chức vụ <i class="bi ms-1" :class="sortIcon('position.name')"></i>
              </th>

              <th class="sortable text-center" @click="sort('workStatus')">
                Trạng thái <i class="bi ms-1" :class="sortIcon('workStatus')"></i>
              </th>

              <th class="text-end" style="width:140px">Hành động</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="(e, index) in paginatedData" :key="e.id">
              <td class="text-center text-muted fw-semibold">
                {{ (currentPage - 1) * pageSize + index + 1 }}
              </td>

              <td class="fw-semibold">
                {{ e.fullName }}
              </td>

              <td class="text-center">
                <span class="badge bg-secondary-subtle text-dark">
                  {{ genderLabel(e.gender) }}
                </span>
              </td>

              <td class="text-center text-muted">
                {{ formatDateOnly(e.birthDate) }}
              </td>

              <td class="text-muted">
                {{ e.phone || '-' }}
              </td>

              <td>
                <span class="badge bg-primary-subtle text-primary">
                  {{ e.department?.name || '-' }}
                </span>
              </td>

              <td>
                <span class="badge bg-info-subtle text-info">
                  {{ e.position?.name || '-' }}
                </span>
              </td>

              <td class="text-center">
                <span
                  class="badge px-3"
                  :class="e.workStatus === 'WORKING'
                    ? 'bg-success-subtle text-success'
                    : 'bg-danger-subtle text-danger'"
                >
                  {{ e.workStatus === 'WORKING' ? 'Đang làm' : 'Nghỉ việc' }}
                </span>
              </td>

              <td class="text-end">
                <div class="btn-group btn-group-sm">
                  <button
                    class="btn btn-outline-warning"
                    data-bs-toggle="modal"
                    data-bs-target="#employeeModal"
                    @click="openEdit(e)"
                  >
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <button class="btn btn-outline-danger" @click="remove(e)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="paginatedData.length === 0">
              <td colspan="9" class="text-center text-muted py-4">
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
          <button class="page-link" @click="changePage(p)">{{ p }}</button>
        </li>

        <li class="page-item" :class="{ disabled: currentPage === totalPages }">
          <button class="page-link" @click="changePage(currentPage + 1)">›</button>
        </li>
      </ul>
    </div>

    <!-- MODAL -->
    <div class="modal fade" id="employeeModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <div>
              <h5 class="modal-title mb-0">
                {{ isEdit ? 'Cập nhật Nhân viên' : 'Thêm Nhân viên' }}
              </h5>
              <div class="text-muted small">Vui lòng nhập đầy đủ thông tin</div>
            </div>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <div class="row g-4">
              <!-- THÔNG TIN CÁ NHÂN -->
              <div class="col-12 col-lg-6">
                <div class="fw-semibold mb-2">Thông tin cá nhân</div>

                <div class="row g-3">
                  <div class="col-12">
                    <label class="form-label">Họ tên</label>
                    <input
                      v-model="form.fullName"
                      class="form-control"
                      :class="{ 'is-invalid': !!errors.fullName }"
                      placeholder="Nhập họ và tên"
                    />
                    <div v-if="errors.fullName" class="invalid-feedback d-block">
                      {{ errors.fullName }}
                    </div>
                  </div>

                  <div class="col-12 col-md-6">
                    <label class="form-label">Giới tính</label>
                    <select v-model="form.gender" class="form-select">
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
                    </select>
                  </div>

                  <div class="col-12 col-md-6">
                    <label class="form-label">Ngày sinh</label>
                    <input v-model="form.birthDate" type="date" class="form-control" />
                  </div>

                  <div class="col-12">
                    <label class="form-label">Số điện thoại</label>
                    <input
                      v-model="form.phone"
                      class="form-control"
                      :class="{ 'is-invalid': !!errors.phone }"
                      placeholder="VD: 0901234567"
                    />
                    <div v-if="errors.phone" class="invalid-feedback d-block">
                      {{ errors.phone }}
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label">Địa chỉ</label>
                    <input v-model="form.address" class="form-control" placeholder="Nhập địa chỉ" />
                  </div>
                </div>
              </div>

              <!-- THÔNG TIN CÔNG VIỆC -->
              <div class="col-12 col-lg-6">
                <div class="fw-semibold mb-2">Thông tin công việc</div>

                <div class="row g-3">
                  <div class="col-12">
                    <label class="form-label">Trạng thái</label>
                    <select v-model="form.workStatus" class="form-select">
                      <option value="WORKING">Đang làm</option>
                      <option value="RESIGNED">Nghỉ việc</option>
                    </select>
                  </div>

                  <div class="col-12">
                    <label class="form-label">Phòng ban</label>
                    <select
                      v-model.number="form.departmentId"
                      class="form-select"
                      :class="{ 'is-invalid': !!errors.departmentId }"
                    >
                      <option disabled :value="null">-- Chọn phòng ban --</option>
                      <option v-for="d in departments" :key="d.id" :value="d.id">
                        {{ d.name }}
                      </option>
                    </select>
                    <div v-if="errors.departmentId" class="invalid-feedback d-block">
                      {{ errors.departmentId }}
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label">Chức vụ</label>
                    <select
                      v-model.number="form.positionId"
                      class="form-select"
                      :class="{ 'is-invalid': !!errors.positionId }"
                    >
                      <option disabled :value="null">-- Chọn chức vụ --</option>
                      <option v-for="p in positions" :key="p.id" :value="p.id">
                        {{ p.name }}
                      </option>
                    </select>
                    <div v-if="errors.positionId" class="invalid-feedback d-block">
                      {{ errors.positionId }}
                    </div>
                  </div>

                  <div class="col-12">
                    <div class="alert alert-light border mb-0 small">
                      Mẹo: Bạn có thể cập nhật phòng ban/chức vụ bất cứ lúc nào.
                    </div>
                  </div>
                </div>
              </div>
              <!--Thông tin tài khoản-->
              <div class="col-12" v-if="!isEdit">

                <div class="fw-semibold mb-2">
                  Thông tin tài khoản
                </div>

                <div class="row g-3">

                  <!-- USERNAME -->
                  <div class="col-12 col-md-6">
                    <label class="form-label">
                      Username
                    </label>

                    <input
                        v-model="form.username"
                        class="form-control"
                        :class="{ 'is-invalid': errors.username }"
                    />

                    <div v-if="errors.username"
                         class="invalid-feedback d-block">
                      {{ errors.username }}
                    </div>
                  </div>


                  <!-- EMAIL -->
                  <div class="col-12 col-md-6">
                    <label class="form-label">
                      Email
                    </label>

                    <input
                        v-model="form.email"
                        class="form-control"
                        :class="{ 'is-invalid': errors.email }"
                    />

                    <div v-if="errors.email"
                         class="invalid-feedback d-block">
                      {{ errors.email }}
                    </div>
                  </div>


                  <!-- PASSWORD -->
                  <div class="col-12 col-md-6">
                    <label class="form-label">
                      Password
                    </label>

                    <input
                        type="password"
                        v-model="form.password"
                        class="form-control"
                        :class="{ 'is-invalid': errors.password }"
                    />

                    <div v-if="errors.password"
                         class="invalid-feedback d-block">
                      {{ errors.password }}
                    </div>
                  </div>


                  <!-- ROLE -->
                  <div class="col-12 col-md-6">
                    <label class="form-label">
                      Role
                    </label>

                    <select
                        v-model.number="form.roleId"
                        class="form-select"
                        :class="{ 'is-invalid': errors.roleId }"
                    >

                      <option :value="null">
                        -- Chọn Role --
                      </option>

                      <option
                          v-for="r in roles"
                          :key="r.id"
                          :value="r.id"
                      >
                        {{ r.roleName }}
                      </option>

                    </select>

                    <div v-if="errors.roleId"
                         class="invalid-feedback d-block">
                      {{ errors.roleId }}
                    </div>

                  </div>

                </div>

              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal" ref="btnCancel">Hủy</button>
            <button class="btn btn-primary" @click="save">Lưu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>