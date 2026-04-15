<template>
  <header class="app-header">
    <button class="icon-btn" title="Toggle sidebar" @click="$emit('toggle-sidebar')">
      <i class="bi bi-list fs-5"></i>
    </button>
    <div class="header-right">

      <div class="dropdown me-2">
        <button class="icon-btn position-relative" data-bs-toggle="dropdown" aria-expanded="false">
          <i class="bi bi-bell fs-5"></i>
          <span v-if="unreadCount > 0"
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                style="font-size: 0.6rem;">
            {{ unreadCount }}
          </span>
        </button>
        <div class="dropdown-menu dropdown-menu-end notification-panel shadow p-0">
          <div class="notif-header p-2 border-bottom d-flex justify-content-between align-items-center bg-light">
            <span class="fw-bold small">Thông báo</span>
            <div class="d-flex gap-2">
              <button v-if="unreadCount > 0" class="btn-action-text text-primary" @click="markAllAsRead">Đọc hết
              </button>
              <button v-if="notifications.length > 0" class="btn-action-text text-danger"
                      @click="clearAllExceptStarred">Dọn dẹp
              </button>
            </div>
          </div>

          <div class="notif-list" style="max-height: 400px; overflow-y: auto;">
            <div v-if="notifications.length === 0" class="p-4 text-center text-muted">
              <i class="bi bi-bell-slash d-block fs-2 mb-2"></i>
              Hộp thư trống
            </div>

            <div v-for="note in notifications" :key="note.id"
                 class="notif-item p-2 border-bottom d-flex align-items-center gap-2"
                 :class="{ 'unread': !note.read }">

              <div class="d-flex align-items-start gap-2 flex-grow-1" @click="handleNotifClick(note)"
                   style="cursor: pointer;">
                <div :class="getNotifIconClass(note.type)">
                  <i :class="getNotifIcon(note.type)"></i>
                </div>
                <div class="flex-grow-1">
                  <div class="notif-title fw-bold" style="font-size: 12px;">{{ note.title }}</div>
                  <div class="notif-msg text-muted" style="font-size: 11.5px; line-height: 1.2;">{{
                      note.message
                    }}
                  </div>
                  <small class="text-dim" style="font-size: 10px;">{{ formatTime(note.createdAt) }}</small>
                </div>
              </div>

              <div class="notif-actions d-flex flex-column gap-1">
                <button class="action-btn" @click.stop="toggleStar(note)"
                        :title="note.starred ? 'Bỏ lưu' : 'Lưu thông báo'">
                  <i :class="note.starred ? 'bi bi-star-fill text-warning' : 'bi bi-star'"></i>
                </button>
                <button class="action-btn text-danger" @click.stop="deleteNotif(note.id)" title="Xóa">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>

          <RouterLink to="/my-notifications"
                      class="notif-footer p-2 d-block text-center border-top text-decoration-none">
            Xem tất cả thông báo của tôi
          </RouterLink>
        </div>
      </div>

      <div class="dropdown">
        <button class="user-btn dropdown-toggle" data-bs-toggle="dropdown">
          <div class="avatar">{{ firstLetter }}</div>
          <div class="user-info">
            <span class="user-name">{{ username }}</span>
            <span class="user-email text-truncate">{{ email }}</span>
          </div>
          <i class="bi bi-chevron-down caret"></i>
        </button>
        <div class="dropdown-menu dropdown-menu-end user-panel shadow p-2">
          <RouterLink to="/profile" class="drop-item">
            <i class="bi bi-person-circle text-muted"></i> Hồ sơ
          </RouterLink>
          <RouterLink v-if="isAdmin" to="/notifications" class="drop-item">
            <i class="bi bi-gear text-muted"></i> Quản lý hệ thống
          </RouterLink>
          <hr class="dropdown-divider my-1">
          <button class="drop-item text-danger w-100" @click="logout">
            <i class="bi bi-box-arrow-right"></i> Đăng xuất
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRouter, RouterLink} from 'vue-router'
import {useAuthStore} from '@/stores/auth.store'
import notifService from '@/services/notification.service'
import axios from 'axios'

defineEmits(['toggle-sidebar'])
const router = useRouter()
const auth = useAuthStore()

const username = computed(() => auth.user?.username ?? 'User')
const email = computed(() => auth.user?.email ?? '')
const firstLetter = computed(() => username.value.charAt(0).toUpperCase())
const isAdmin = computed(() => auth.userRole === 'ADMIN')

const notifications = ref([])
const unreadCount = ref(0)

const fetchNotifications = async () => {
  if (!auth.user?.id) return
  try {
    const res = await axios.get(`/api/notifications/my?userId=${auth.user.id}`)
    notifications.value = res.data
    unreadCount.value = notifications.value.filter(n => !n.read).length
  } catch (err) {
    console.error('Lỗi lấy thông báo:', err)
  }
}

onMounted(async () => {
  await fetchNotifications();
  if (auth.user?.id) {
    notifService.connect(auth.user.id, (newNote) => {
      notifications.value = [newNote, ...notifications.value];
      unreadCount.value = notifications.value.filter(n => !n.read).length;
      if (newNote.type === 'SUMMON') {
        alert(`⚡ LỆNH TRIỆU TẬP: ${newNote.message}`);
      }
    });
  }
});

onUnmounted(() => {
  notifService.disconnect()
})

const handleNotifClick = async (note) => {
  if (!note.read) {
    await axios.put(`/api/notifications/${note.id}/read`)
    note.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (note.targetUrl) router.push(note.targetUrl)
}

const toggleStar = async (note) => {
  try {
    await axios.put(`/api/notifications/${note.id}/star`)
    note.starred = !note.starred
  } catch (err) {
    console.error(err)
  }
}

const deleteNotif = async (id) => {
  if (!confirm('Xóa thông báo này?')) return
  try {
    await axios.delete(`/api/notifications/${id}`)
    notifications.value = notifications.value.filter(n => n.id !== id)
    unreadCount.value = notifications.value.filter(n => !n.read).length
  } catch (err) {
    console.error(err)
  }
}

// Xóa hết trừ những cái có sao (Starred)
const clearAllExceptStarred = async () => {
  if (!confirm('Bạn có muốn xóa toàn bộ thông báo thường và chỉ giữ lại các mục đã lưu?')) return
  try {
    await axios.delete(`/api/notifications/clear-all?userId=${auth.user.id}`)
    // Lọc lại mảng ngay lập tức trên giao diện
    notifications.value = notifications.value.filter(n => n.starred)
    unreadCount.value = notifications.value.filter(n => !n.read).length
  } catch (err) {
    console.error(err)
  }
}

const markAllAsRead = async () => {
  try {
    await axios.put(`/api/notifications/read-all?userId=${auth.user.id}`)
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  } catch (err) {
    console.error(err)
  }
}

const getNotifIcon = (type) => {
  switch (type) {
    case 'SUMMON':
      return 'bi-exclamation-triangle-fill'
    case 'TASK':
      return 'bi-clipboard-check'
    case 'LEAVE':
      return 'bi-calendar-event'
    default:
      return 'bi-info-circle'
  }
}

const getNotifIconClass = (type) => {
  const base = 'notif-icon-circle '
  if (type === 'SUMMON') return base + 'bg-danger-subtle text-danger'
  if (type === 'TASK') return base + 'bg-primary-subtle text-primary'
  return base + 'bg-info-subtle text-info'
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'}) + ' ' + date.toLocaleDateString()
}

const logout = () => {
  auth.logout();
  router.push('/auth/login')
}
</script>

<style scoped>
.notification-panel {
  width: 340px;
  border-radius: 12px !important;
  overflow: hidden;
  border: 1px solid var(--border);
}

.notif-header {
  background: #f8f9fa;
}

.btn-action-text {
  background: none;
  border: none;
  font-size: 11px;
  font-weight: 600;
  padding: 0;
  cursor: pointer;
}

.btn-action-text:hover {
  text-decoration: underline;
}

.notif-item {
  position: relative;
  transition: all 0.2s ease;
}

.notif-item:hover {
  background: #f8f9fa;
}

.notif-item.unread {
  background: #f0f7ff;
  border-left: 3px solid #0d6efd;
}

.notif-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
  padding-right: 4px;
}

.notif-item:hover .notif-actions {
  opacity: 1;
}

.action-btn {
  background: transparent;
  border: none;
  padding: 4px;
  font-size: 14px;
  color: #adb5bd;
  cursor: pointer;
  border-radius: 4px;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #212529;
}

.bi-star-fill {
  color: #ffc107 !important;
}

.notif-icon-circle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-footer {
  font-size: 12px;
  font-weight: 600;
  color: #0d6efd;
  background: #fff;
}

.notif-footer:hover {
  background: #f8f9fa;
}

.app-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 8px;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f1f3f5;
  color: #212529;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: transparent;
  padding: 4px;
  cursor: pointer;
  border-radius: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #0d6efd;
  color: #fff;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.user-info {
  text-align: left;
  line-height: 1.2;
  display: flex;
  flex-direction: column;
  max-width: 120px;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #343a40;
}

.user-email {
  font-size: 11px;
  color: #adb5bd;
}

.drop-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  font-size: 13.5px;
  color: #343a40;
  text-decoration: none;
  border-radius: 6px;
  border: none;
  background: none;
  text-align: left;
}

.drop-item:hover {
  background: #f8f9fa;
}
</style>