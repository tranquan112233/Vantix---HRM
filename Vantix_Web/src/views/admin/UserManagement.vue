<template>
  <div class="container py-4">

    <!-- TITLE -->
    <h4 class="fw-bold mb-3">
      <i class="bi bi-people-fill me-2"></i> Quản lý User
    </h4>

    <!-- SEARCH + ADD -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="input-group w-25">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input
            v-model="searchText"
            class="form-control"
            placeholder="Tìm kiếm..."
        />
      </div>

      <button
          class="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#userModal"
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

            <th @click="sort('username')" class="sortable">
              Username <i class="bi ms-1" :class="sortIcon('username')"></i>
            </th>

            <th @click="sort('email')" class="sortable">
              Email <i class="bi ms-1" :class="sortIcon('email')"></i>
            </th>

            <th @click="sort('role.roleName')" class="sortable">
              Role <i class="bi ms-1" :class="sortIcon('role.roleName')"></i>
            </th>

            <th @click="sort('status')" class="sortable text-center">
              Status <i class="bi ms-1" :class="sortIcon('status')"></i>
            </th>

            <th @click="sort('lastLogin')" class="sortable">
              Last Login <i class="bi ms-1" :class="sortIcon('lastLogin')"></i>
            </th>

            <th class="text-end" style="width:140px">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="(u, index) in paginatedData" :key="u.id">

            <!-- STT -->
            <td class="text-center text-muted fw-semibold">
              {{ (currentPage - 1) * pageSize + index + 1 }}
            </td>

            <!-- USERNAME -->
            <td class="fw-semibold">
              {{ u.username }}
            </td>

            <!-- EMAIL -->
            <td class="text-muted small">
              {{ u.email || '-' }}
            </td>

            <!-- ROLE -->
            <td>
    <span class="badge bg-secondary-subtle text-dark">
      {{ u.role?.roleName }}
    </span>
            </td>

            <!-- STATUS -->
            <td class="text-center">
    <span
        class="badge px-3"
        :class="u.status === 'ACTIVE'
        ? 'bg-success-subtle text-success'
        : 'bg-danger-subtle text-danger'"
    >
      {{ u.status }}
    </span>
            </td>

            <!-- LAST LOGIN -->
            <td class="text-muted small">
              {{ formatDate(u.lastLogin) }}
            </td>

            <!-- ACTION -->
            <td class="text-end">
              <div class="btn-group btn-group-sm">

                <button
                    class="btn btn-outline-warning"
                    data-bs-toggle="modal"
                    data-bs-target="#userModal"
                    @click="openEdit(u)"
                >
                  <i class="bi bi-pencil-square"></i>
                </button>

                <button
                    class="btn"
                    :class="u.status === 'ACTIVE'
          ? 'btn-outline-danger'
          : 'btn-outline-success'"
                    @click="toggleLock(u)"
                >
                  <i
                      class="bi"
                      :class="u.status === 'ACTIVE'
            ? 'bi-lock-fill'
            : 'bi-unlock-fill'"
                  ></i>
                </button>

              </div>
            </td>
          </tr>

          <tr v-if="paginatedData.length === 0">
            <td colspan="7" class="text-center text-muted py-4">
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
          <button class="page-link" @click="changePage(currentPage - 1)">
            ‹
          </button>
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
          <button class="page-link" @click="changePage(currentPage + 1)">
            ›
          </button>
        </li>
      </ul>
    </div>

    <!-- MODAL -->
    <div class="modal fade" id="userModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEdit ? 'Cập nhật User' : 'Thêm User' }}
            </h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">

            <div class="mb-3">
              <label class="form-label">Username</label>
              <div class="position-relative">
                <input
                  v-model="form.username"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': !!errors.username }"
                />
              </div>
              <small class="text-danger">{{ errors.username }}</small>
            </div>

            <div class="mb-3">
              <label class="form-label">Email</label>
              <div class="position-relative">
                <input
                  v-model="form.email"
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': !!errors.email }"
                />
              </div>
              <small class="text-danger">{{ errors.email }}</small>
            </div>

            <div class="mb-3" v-if="!isEdit">
              <label class="form-label">Password</label>
              <div class="position-relative">
                <input
                  v-model="form.passwordHash"
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': !!errors.passwordHash }"
                />
              </div>
              <small class="text-danger">{{ errors.passwordHash }}</small>
            </div>

            <div class="mb-3">
              <label class="form-label">Role</label>
              <div class="position-relative">
                <select
                  v-model="form.roleId"
                  class="form-select"
                  :class="{ 'is-invalid': !!errors.roleId }"
                >
                  <option disabled value="">-- Chọn role --</option>
                  <option v-for="r in roles" :key="r.id" :value="r.id">
                    {{ r.roleName }}
                  </option>
                </select>
              </div>
              <small class="text-danger">{{ errors.roleId }}</small>
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

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import UserService from '@/services/user.service'
import RoleService from '@/services/role.service'

import { useSearch } from '@/composables/useSearch'
import { useMultiSort } from '@/composables/useMultiSort'
import { usePagination } from '@/composables/usePagination'
import { useToast } from '@/composables/useToast'

/* DATA */
const users = ref([])
const roles = ref([])
const isEdit = ref(false)

/* SEARCH */
const { searchText, filteredData } = useSearch(users, [
  'username',
  'email',
  'role.roleName'
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
  username: '',
  email: '',
  passwordHash: '',
  roleId: ''
})

const errors = reactive({})

/* TOAST */
const { showToast } = useToast()

/* VALIDATE */
const validate = () => {
  Object.keys(errors).forEach(k => delete errors[k])

  const norm = (v) => (v ?? '').toString().trim().toLowerCase()

  // Basic checks
  if (!form.value.username || !form.value.username.trim()) {
    errors.username = 'Vui lòng nhập username'
  }
  if (!form.value.email || !form.value.email.trim()) {
    errors.email = 'Vui lòng nhập email'
  }
  if (!form.value.roleId) {
    errors.roleId = 'Vui lòng chọn role'
  }
  if (!isEdit.value && (!form.value.passwordHash || !form.value.passwordHash.trim())) {
    errors.passwordHash = 'Vui lòng nhập mật khẩu'
  }
  if (form.value.email && !/^\S+@\S+\.\S+$/.test(form.value.email)) {
    errors.email = 'Email không hợp lệ'
  }

  // Duplicate checks (case-insensitive)
  const currentId = form.value.id
  const usernameTaken = form.value.username && users.value.some(u =>
    norm(u.username) === norm(form.value.username) && u.id !== currentId
  )
  if (!errors.username && usernameTaken) {
    errors.username = 'Username đã tồn tại'
  }

  if (form.value.email && !errors.email) {
    const emailTaken = users.value.some(u =>
      norm(u.email) === norm(form.value.email) && u.id !== currentId
    )
    if (emailTaken) {
      errors.email = 'Email đã tồn tại'
    }
  }

  return Object.keys(errors).length === 0
}

/* LOAD */
const loadData = async () => {
  users.value = (await UserService.getAll()).data
  roles.value = (await RoleService.getAll()).data
}

/* MODAL */
const openCreate = () => {
  isEdit.value = false
  Object.assign(form.value, {
    id: null,
    username: '',
    email: '',
    passwordHash: '',
    roleId: ''
  })
  Object.keys(errors).forEach(k => delete errors[k])
}

const openEdit = (u) => {
  isEdit.value = true
  Object.assign(form.value, {
    id: u.id,
    username: u.username,
    email: u.email,
    passwordHash: '',
    roleId: u.role?.id
  })
  Object.keys(errors).forEach(k => delete errors[k])
}
/*Button cancel in modal*/
const btnCancel = ref(null)
/* SAVE */
const save = async () => {
  if (!validate()) return

  try {
    const payload = {
      username: form.value.username,
      email: form.value.email,
      role: { id: form.value.roleId }
    }

    if (isEdit.value) {
      await UserService.update(form.value.id, payload)
      showToast('Cập nhật thành công','success')
    } else {
      await UserService.create({
        ...payload,
        passwordHash: form.value.passwordHash
      })
      showToast('Tạo user thành công','success')
    }

    await loadData()
    btnCancel.value.click()


  } catch {
    showToast('Có lỗi xảy ra', 'danger')
  }
}

/* LOCK */
const toggleLock = async (u) => {
  await UserService.toggleLock(u.id)
  showToast('Đã cập nhật trạng thái','success')
  loadData()
}

/* UTILS */
const formatDate = v => v ? new Date(v).toLocaleString() : '-'

watch([searchText, pageSize], () => currentPage.value = 1)
onMounted(loadData)
</script>

<style scoped>
.sortable {
  cursor: pointer;
  user-select: none;
}

.table-hover tbody tr:hover {
  background-color: #f8f9fa;
}

.badge {
  font-weight: 500;
}

</style>
