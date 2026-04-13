<template>
  <div class="container-fluid p-4">
    <div class="row justify-content-center">
      <div class="col-md-10 col-lg-8">
        <div class="card shadow-sm border-0">
          <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold"><i class="bi bi-mailbox2 me-2"></i>Thông báo của tôi</h5>
            <div class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" @click="markAllAsRead">
                <i class="bi bi-check2-all me-1"></i>Đọc hết
              </button>
              <button class="btn btn-outline-danger btn-sm" @click="clearNormalNotes">
                <i class="bi bi-eraser me-1"></i>Dọn dẹp
              </button>
            </div>
          </div>

          <div class="card-body p-0">
            <ul class="nav nav-tabs px-3 pt-2 bg-light border-bottom-0">
              <li class="nav-item">
                <button class="nav-link" :class="{active: filter === 'all'}" @click="filter = 'all'">Tất cả</button>
              </li>
              <li class="nav-item">
                <button class="nav-link" :class="{active: filter === 'unread'}" @click="filter = 'unread'">Chưa đọc</button>
              </li>
              <li class="nav-item">
                <button class="nav-link" :class="{active: filter === 'starred'}" @click="filter = 'starred'">Đã lưu ⭐</button>
              </li>
            </ul>

            <div class="notif-container" style="min-height: 400px;">
              <div v-if="filteredNotes.length === 0" class="text-center py-5 text-muted">
                <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                Không tìm thấy thông báo nào
              </div>

              <div v-for="note in filteredNotes" :key="note.id"
                   class="notif-row p-3 border-bottom d-flex align-items-center"
                   :class="{ 'unread-bg': !note.read }">

                <div :class="getIconClass(note.type)" class="me-3">
                  <i :class="getIcon(note.type)" class="fs-4"></i>
                </div>

                <div class="flex-grow-1" @click="readNote(note)" style="cursor: pointer;">
                  <div class="d-flex justify-content-between">
                    <h6 class="mb-1 fw-bold text-dark">{{ note.title }}</h6>
                    <small class="text-muted">{{ formatFullTime(note.createdAt) }}</small>
                  </div>
                  <p class="mb-0 text-secondary" style="font-size: 14px;">{{ note.message }}</p>
                </div>

                <div class="ms-3 d-flex gap-2">
                  <button class="btn btn-link p-0" @click="toggleStar(note)">
                    <i :class="note.starred ? 'bi bi-star-fill text-warning' : 'bi bi-star'" class="fs-5"></i>
                  </button>
                  <button class="btn btn-link p-0 text-danger" @click="deleteNote(note.id)">
                    <i class="bi bi-trash fs-5"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

const auth = useAuthStore();
const notes = ref([]);
const filter = ref('all');

const fetchNotes = async () => {
  try {
    const res = await axios.get(`/api/notifications/my?userId=${auth.user.id}`);
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
    await axios.put(`/api/notifications/${note.id}/read`);
    note.read = true;
  }
  // Chuyển hướng nếu có targetUrl
};

const toggleStar = async (note) => {
  await axios.put(`/api/notifications/${note.id}/star`);
  note.starred = !note.starred;
};

const deleteNote = async (id) => {
  if (!confirm('Xóa thông báo này?')) return;
  await axios.delete(`/api/notifications/${id}`);
  notes.value = notes.value.filter(n => n.id !== id);
};

const markAllAsRead = async () => {
  await axios.put(`/api/notifications/read-all?userId=${auth.user.id}`);
  notes.value.forEach(n => n.read = true);
};

const clearNormalNotes = async () => {
  if (!confirm('Xóa tất cả thông báo trừ mục đã lưu?')) return;
  await axios.delete(`/api/notifications/clear-all?userId=${auth.user.id}`);
  notes.value = notes.value.filter(n => n.starred);
};

// UI Helpers
const getIcon = (t) => t === 'SUMMON' ? 'bi-exclamation-octagon' : (t === 'TASK' ? 'bi-list-task' : 'bi-info-circle');
const getIconClass = (t) => t === 'SUMMON' ? 'text-danger' : (t === 'TASK' ? 'text-primary' : 'text-info');
const formatFullTime = (d) => new Date(d).toLocaleString();

onMounted(fetchNotes);
</script>

<style scoped>
.unread-bg { background-color: #f8fbff; }
.notif-row { transition: 0.2s; }
.notif-row:hover { background-color: #f1f3f5; }
.nav-link { color: #6c757d; border: none; font-weight: 500; }
.nav-link.active { color: #0d6efd !important; background: none !important; border-bottom: 2px solid #0d6efd !important; }
</style>