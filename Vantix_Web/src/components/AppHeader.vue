<script setup>
import { ref } from 'vue'


// ===== MOCK DATA (sau này thay bằng axios) =====
const notifications = ref([
  {
    id: 1,
    title: 'Điều chỉnh giá dịch vụ tên miền .NET',
    content: 'Áp dụng từ ngày 17/01/2026',
    time: '09:31 AM | 16/01/2026',
    read: false
  },
  {
    id: 2,
    title: 'Cập nhật chính sách bảo mật',
    content: 'Áp dụng theo Nghị định 13/2023/NĐ-CP',
    time: '04:23 AM | 31/12/2025',
    read: true
  }
])

// ===== USER MOCK =====
const user = ref({
  fullName: 'Trần Văn A',
  role: 'Admin'
})

// ===== COMPUTED =====
const unreadCount = () =>
    notifications.value.filter(n => !n.read).length

const isAdmin = () => user.value.role === 'Admin'

// ===== METHODS =====
const markAsRead = (item) => {
  item.read = true
}

const logout = () => {
  alert('Đăng xuất')
}
</script>

<template>
  <nav class="navbar navbar-expand bg-white shadow-sm px-4">
    <!-- Logo -->
    <router-link to="/home" class="navbar-brand fw-semibold text-primary">
      VANTIX HRM
    </router-link>

    <div class="ms-auto d-flex align-items-center gap-3">

      <!-- 🔔 Notification -->
      <div class="dropdown">
        <button
            class="btn btn-light position-relative rounded-circle"
            data-bs-toggle="dropdown"
        >
          <i class="bi bi-bell fs-5"></i>

          <span
              v-if="unreadCount() > 0"
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
          >
            {{ unreadCount() }}
          </span>
        </button>

        <div
            class="dropdown-menu dropdown-menu-end p-0 shadow"
            style="width: 360px; max-height: 420px; overflow-y: auto"
        >
          <div class="px-3 py-2 fw-semibold border-bottom">
            Thông báo
          </div>

          <div v-if="notifications.length === 0" class="p-3 text-muted text-center">
            Không có thông báo
          </div>

          <div
              v-for="item in notifications"
              :key="item.id"
              class="px-3 py-2 border-bottom notification-item"
              :class="{ unread: !item.read }"
              @click="markAsRead(item)"
          >
            <div class="fw-semibold">{{ item.title }}</div>
            <div class="small text-muted">{{ item.content }}</div>
            <div class="small text-muted mt-1">{{ item.time }}</div>
          </div>
        </div>
      </div>

      <!-- 👤 User -->
      <div class="dropdown">
        <button
            class="btn user-btn d-flex align-items-center gap-2"
            data-bs-toggle="dropdown"
        >
          <span class="avatar">
            {{ user.fullName.charAt(0) }}
          </span>
          <span class="fw-medium">{{ user.fullName }}</span>
          <i class="bi bi-chevron-down small"></i>
        </button>

        <ul class="dropdown-menu dropdown-menu-end shadow">
          <li>
            <router-link class="dropdown-item" to="/home/profile">
              Thông tin cá nhân
            </router-link>
          </li>

          <li v-if="isAdmin()">
            <router-link class="dropdown-item fw-semibold text-primary" to="/admin">
              Admin Panel
            </router-link>
          </li>

          <li><hr class="dropdown-divider"></li>

          <li>
            <button class="dropdown-item text-danger" @click="logout">
              Đăng xuất
            </button>
          </li>
        </ul>
      </div>

    </div>
  </nav>
</template>


<style scoped>
.notification-item {
  cursor: pointer;
}

.notification-item.unread {
  background: #f0f6ff;
}

.notification-item:hover {
  background: #eef3f9;
}

/* ===== USER BUTTON ===== */
.user-btn {
  background: transparent;
  border: none;
  padding: 6px 10px;
}

.user-btn:hover {
  background: #f5f7fa;
}

/* ===== AVATAR ===== */
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #0d6efd;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
</style>


