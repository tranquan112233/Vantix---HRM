<template>
  <div class="my-notifications mgmt-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title"><i class="bi bi-mailbox2 me-2"></i>Thông báo của tôi</h2>
        <p class="page-desc">Danh sách tất cả thông báo được gửi đến bạn.</p>
      </div>
      <div class="header-actions">
        <button class="btn-ghost btn-sm" @click="markAllAsRead">
          <i class="bi bi-check2-all"></i> Đọc hết
        </button>
        <button class="btn-ghost btn-sm btn-ghost-danger" @click="clearNormalNotes">
          <i class="bi bi-eraser"></i> Dọn dẹp
        </button>
      </div>
    </div>

    <div class="table-card">
      <div class="filter-tabs">
        <button class="filter-tab" :class="{ active: filter === 'all' }" @click="filter = 'all'">Tất cả</button>
        <button class="filter-tab" :class="{ active: filter === 'unread' }" @click="filter = 'unread'">Chưa đọc</button>
        <button class="filter-tab" :class="{ active: filter === 'starred' }" @click="filter = 'starred'">Đã lưu ⭐</button>
      </div>

      <div class="notif-container">
        <div v-if="filteredNotes.length === 0" class="empty-state">
          <i class="bi bi-inbox empty-icon"></i>
          <p class="empty-title">Không tìm thấy thông báo nào</p>
          <p class="empty-sub">Khi có thông báo mới, nó sẽ hiển thị tại đây.</p>
        </div>

        <div v-for="note in filteredNotes" :key="note.id"
             class="notif-row"
             :class="{ 'unread': !note.read }">

          <div class="notif-type-icon" :class="getIconClass(note.type)">
            <i :class="getIcon(note.type)"></i>
          </div>

          <div class="notif-body" @click="readNote(note)">
            <div class="notif-head">
              <span class="notif-title">{{ note.title }}</span>
              <span class="notif-time">{{ formatFullTime(note.createdAt) }}</span>
            </div>
            <p class="notif-msg">{{ note.message }}</p>
          </div>

          <div class="notif-row-actions">
            <button class="icon-btn icon-btn-sm" @click="toggleStar(note)" :title="note.starred ? 'Bỏ lưu' : 'Lưu'">
              <i :class="note.starred ? 'bi bi-star-fill text-warning' : 'bi bi-star'"></i>
            </button>
            <button class="icon-btn icon-btn-sm danger" @click="deleteNote(note.id)" title="Xóa">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth.store';
import notifService from '@/services/notification.service';

const auth = useAuthStore();
const notes = ref([]);
const filter = ref('all');

const fetchNotes = async () => {
  if (!auth.user && auth.token) await auth.fetchMe();
  if (!auth.user?.id) return;

  try {
    const res = await notifService.getMyNotifications(auth.user.id);
    notes.value = res.data;
  } catch (err) { console.error(err); }
};

const filteredNotes = computed(() => {
  if (filter.value === 'unread') return notes.value.filter(n => !n.read);
  if (filter.value === 'starred') return notes.value.filter(n => n.starred);
  return notes.value;
});

const readNote = async (note) => {
  if (!note.read) {
    await notifService.markAsRead(note.id);
    note.read = true;
  }
  // Chuyển hướng nếu có targetUrl
};

// MyNotifications.vue

const deleteNote = async (id) => {
  // 1. Tìm thông báo trong danh sách hiện tại
  const targetNote = notes.value.find(n => n.id === id);

  // 2. Nếu thông báo đang được đánh sao (starred), chặn xóa
  if (targetNote && targetNote.starred) {
    alert("Thông báo này đã được lưu (⭐). Bạn cần bỏ lưu trước khi xóa!");
    return; // Dừng hàm tại đây
  }

  // 3. Nếu không có sao, tiến hành hỏi xác nhận và xóa như bình thường
  if (!confirm('Xóa thông báo này?')) return;

  try {
    await notifService.delete(id);
    notes.value = notes.value.filter(n => n.id !== id);
  } catch (err) {
    console.error("Lỗi khi xóa:", err);
    alert("Không thể xóa thông báo này.");
  }
};

const toggleStar = async (note) => {
  try {
    await notifService.toggleStar(note.id);
    note.starred = !note.starred;
  } catch (err) {
    console.error("Lỗi khi thay đổi trạng thái sao:", err);
  }
};

const markAllAsRead = async () => {
  if (!auth.user?.id) return;
  await notifService.markAllAsRead(auth.user.id);
  notes.value.forEach(n => n.read = true);
};

const clearNormalNotes = async () => {
  if (!auth.user?.id) return;
  if (!confirm('Xóa tất cả thông báo trừ mục đã lưu?')) return;
  await notifService.clearAllExceptStarred(auth.user.id);
  notes.value = notes.value.filter(n => n.starred);
};

// UI Helpers
const getIcon = (t) => t === 'SUMMON' ? 'bi-exclamation-octagon' : (t === 'TASK' ? 'bi-list-task' : 'bi-info-circle');
const getIconClass = (t) => t === 'SUMMON' ? 'text-danger' : (t === 'TASK' ? 'text-primary' : 'text-info');
const formatFullTime = (d) => new Date(d).toLocaleString();

onMounted(fetchNotes);
</script>

<style scoped>
.filter-tabs {
  display: flex;
  gap: 4px;
  padding: 10px 16px 0;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-white);
}

.filter-tab {
  padding: 10px 14px;
  border: none;
  background: transparent;
  color: var(--text-muted-dark);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color var(--transition), border-color var(--transition);
}

.filter-tab:hover {
  color: var(--primary-color);
}

.filter-tab.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.notif-container {
  min-height: 400px;
}

.notif-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  transition: background var(--transition);
}

.notif-row:last-child {
  border-bottom: none;
}

.notif-row:hover {
  background: var(--surface-muted);
}

.notif-row.unread {
  background: var(--primary-color-light);
  border-left: 3px solid var(--primary-color);
}

.notif-type-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 18px;
}

.notif-type-icon.text-danger  { background: var(--danger-bg-light); color: var(--danger-color); }
.notif-type-icon.text-primary { background: var(--primary-color-light); color: var(--primary-color); }
.notif-type-icon.text-info    { background: var(--info-bg-light); color: var(--info-color); }

.notif-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.notif-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.notif-title {
  color: var(--text-dark);
  font-size: 14px;
  font-weight: 700;
}

.notif-time {
  flex-shrink: 0;
  color: var(--text-dim);
  font-size: 11.5px;
  font-weight: 500;
}

.notif-msg {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.55;
}

.notif-row-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.icon-btn-sm {
  width: 32px !important;
  height: 32px !important;
}

.btn-ghost-danger:hover:not(:disabled) {
  background: var(--danger-bg-light) !important;
  color: var(--danger-color) !important;
  border-color: var(--danger-border-light) !important;
}
</style>
