<template>
  <div class="container py-4">

    <!-- TITLE -->
    <h4 class="fw-bold mb-3">
      <i class="bi bi-people-fill me-2"></i> Quản lý User
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
            placeholder="Tìm theo username hoặc email"
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
    <div class="card shadow-sm mb-3">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-dark">
        <tr>
          <th class="sortable" @click="sort('username')">
            Username
            <i class="bi ms-1" :class="sortIcon('username')"></i>
          </th>

          <th class="sortable" @click="sort('email')">
            Email
            <i class="bi ms-1" :class="sortIcon('email')"></i>
          </th>

          <th>Role</th>

          <th class="sortable" @click="sort('status')">
            Status
            <i class="bi ms-1" :class="sortIcon('status')"></i>
          </th>

          <th class="sortable" @click="sort('lastLogin')">
            Last Login
            <i class="bi ms-1" :class="sortIcon('lastLogin')"></i>
          </th>

          <th class="text-end">Hành động</th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="u in paginatedUsers" :key="u.id">
          <td>{{ u.username }}</td>
          <td>{{ u.email || '-' }}</td>
          <td>{{ u.role?.roleName }}</td>

          <td>
            <span
                class="badge"
                :class="u.status === 'ACTIVE' ? 'bg-success' : 'bg-danger'"
            >
              {{ u.status }}
            </span>
          </td>

          <td>{{ formatDate(u.lastLogin) }}</td>

          <td class="text-end">
            <button
                class="btn btn-sm btn-warning me-2"
                data-bs-toggle="modal"
                data-bs-target="#userModal"
                @click="openEdit(u)"
            >
              <i class="bi bi-pencil"></i>
            </button>

            <button
                class="btn btn-sm"
                :class="u.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'"
                @click="toggleLock(u)"
            >
              <i
                  class="bi"
                  :class="u.status === 'ACTIVE'
                  ? 'bi-lock-fill'
                  : 'bi-unlock-fill'"
              ></i>
            </button>
          </td>
        </tr>

        <tr v-if="paginatedUsers.length === 0">
          <td colspan="6" class="text-center text-muted py-3">
            Không có dữ liệu
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- FOOTER -->
    <div class="d-flex justify-content-between align-items-center">

      <!-- PAGE SIZE -->
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-eye"></i>
        <span>Xem</span>
        <select v-model.number="pageSize" class="form-select w-auto">
          <option :value="10">10</option>
          <option :value="25">25</option>
        </select>
        <span>mục</span>
      </div>

      <!-- PAGINATION -->
      <ul class="pagination mb-0">
        <li class="page-item" :class="{ disabled: currentPage === 1 }">
          <button class="page-link" @click="changePage(currentPage - 1)">
            <i class="bi bi-chevron-left"></i>
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
            <i class="bi bi-chevron-right"></i>
          </button>
        </li>
      </ul>
    </div>

    <!-- ================= MODAL ================= -->
    <div
        class="modal fade"
        id="userModal"
        tabindex="-1"
        data-bs-backdrop="static"
        data-bs-keyboard="false"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">

          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi" :class="isEdit ? 'bi-pencil-square' : 'bi-person-plus-fill'"></i>
              {{ isEdit ? 'Cập nhật User' : 'Thêm User' }}
            </h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">Username</label>
              <input v-model="form.username" class="form-control" />
            </div>

            <div class="mb-3">
              <label class="form-label">Email</label>
              <input v-model="form.email" type="email" class="form-control" />
            </div>

            <div class="mb-3" v-if="!isEdit">
              <label class="form-label">Password</label>
              <input v-model="form.passwordHash" type="password" class="form-control" />
            </div>

            <div class="mb-3">
              <label class="form-label">Role</label>
              <select v-model="form.roleId" class="form-select">
                <option disabled value="">-- Chọn role --</option>
                <option v-for="r in roles" :key="r.id" :value="r.id">
                  {{ r.roleName }}
                </option>
              </select>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">
              Hủy
            </button>
            <button
                class="btn btn-primary"
                data-bs-dismiss="modal"
                @click="save"
            >
              Lưu
            </button>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import UserService from '@/assets/services/user.service'
import RoleService from '@/assets/services/role.service'

const users = ref([])
const roles = ref([])
const isEdit = ref(false)

const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const sortKey = ref('')
const sortDir = ref('asc')

const form = ref({
  id: null,
  username: '',
  email: '',
  passwordHash: '',
  roleId: ''
})

const loadData = async () => {
  users.value = (await UserService.getAll()).data
  roles.value = (await RoleService.getAll()).data
}

/* SORT */
const sort = (key) => {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortDir.value = 'asc'
  }
}

const sortIcon = (key) => {
  if (sortKey.value !== key) return 'bi-arrow-down-up'
  return sortDir.value === 'asc' ? 'bi-caret-up-fill' : 'bi-caret-down-fill'
}

const sortedUsers = computed(() => {
  if (!sortKey.value) return users.value
  return [...users.value].sort((a, b) => {
    const x = a[sortKey.value] || ''
    const y = b[sortKey.value] || ''
    return sortDir.value === 'asc'
        ? x.toString().localeCompare(y.toString())
        : y.toString().localeCompare(x.toString())
  })
})

/* SEARCH */
const filteredUsers = computed(() => {
  const k = searchText.value.toLowerCase()
  return sortedUsers.value.filter(u =>
      u.username.toLowerCase().includes(k) ||
      (u.email || '').toLowerCase().includes(k)
  )
})

/* PAGINATION */
const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value))
)

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const changePage = (p) => {
  if (p >= 1 && p <= totalPages.value) currentPage.value = p
}

watch([searchText, pageSize], () => currentPage.value = 1)

/* MODAL DATA */
const openCreate = () => {
  isEdit.value = false
  form.value = { id: null, username: '', email: '', passwordHash: '', roleId: '' }
}

const openEdit = (u) => {
  isEdit.value = true
  form.value = {
    id: u.id,
    username: u.username,
    email: u.email,
    passwordHash: '',
    roleId: u.role?.id
  }
}

/* SAVE */
const save = async () => {
  // Validate đơn giản
  if (!form.value.username?.trim()) {
    alert('Vui lòng nhập username')
    return
  }
  if (!isEdit.value && !form.value.passwordHash?.trim()) {
    alert('Vui lòng nhập password')
    return
  }
  if (!form.value.roleId) {
    alert('Vui lòng chọn role')
    return
  }

  // Map roleId -> role object theo yêu cầu backend
  const payload = {
    username: form.value.username,
    email: form.value.email || null,
    role: { id: form.value.roleId }
  }

  try {
    if (isEdit.value) {
      await UserService.update(form.value.id, payload)
    } else {
      await UserService.create({
        ...payload,
        passwordHash: form.value.passwordHash
      })
    }
    await loadData()
  } catch (e) {
    // Tùy chọn: hiển thị lỗi từ server (trùng username/email, ...)
    const msg = e?.response?.data?.message || 'Lưu thất bại'
    alert(msg)
  }
}

const toggleLock = async (u) => {
  await UserService.toggleLock(u.id)
  loadData()
}

const formatDate = (v) => v ? new Date(v).toLocaleString() : '-'

onMounted(loadData)
</script>

<style scoped>
.sortable {
  cursor: pointer;
  user-select: none;
}
</style>
