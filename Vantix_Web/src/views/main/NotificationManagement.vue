<template>
  <div class="appointment-card shadow border-0 rounded-4 overflow-hidden bg-white mx-auto transition-all">
    <div class="card-header py-3 border-0 transition-all" :class="headerStyles.bg">
      <h6 class="mb-0 d-flex align-items-center fw-bold">
        <i class="bi me-2 fs-5" :class="headerStyles.icon"></i>
        {{ headerStyles.title }}
      </h6>
    </div>

    <div class="card-body p-4">
      <div class="mb-3">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Tính chất thông báo</label>
        <div class="d-flex gap-2">
          <button v-for="opt in priorityOptions" :key="opt.value"
                  @click="form.priority = opt.value"
                  class="btn btn-sm flex-grow-1 border shadow-sm transition-all"
                  :class="form.priority === opt.value ? opt.activeClass : 'btn-light text-muted'">
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div class="mb-3 pt-2 border-top">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Đối tượng tiếp nhận</label>
        <div class="d-flex gap-2">
          <select v-model="selectedRole" class="form-select shadow-sm" :disabled="isAllMode">
            <option value="ALL">-- Toàn bộ nhân sự --</option>
            <option v-for="role in roles" :key="role.id" :value="role.name">
              {{ role.name }}
            </option>
          </select>
          <button @click="toggleAllMode" class="btn btn-sm text-nowrap shadow-sm"
                  :class="isAllMode ? 'btn-warning' : 'btn-outline-primary'">
            <i class="bi" :class="isAllMode ? 'bi-person-dash' : 'bi-people-fill'"></i>
            {{ isAllMode ? 'Hủy chọn nhóm' : 'Gửi theo nhóm' }}
          </button>
        </div>
      </div>

      <div class="mb-3 animate__animated animate__fadeIn" v-if="!isAllMode">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Chọn nhân viên cụ thể</label>
        <div class="input-group input-group-sm mb-2 shadow-sm">
          <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
          <input v-model="searchQuery" type="text" class="form-control" placeholder="Tìm tên nhân viên...">
        </div>
        <select v-model="form.recipientId" class="form-select shadow-sm" style="height: 45px;">
          <option value="" disabled>-- Chọn người nhận ({{ filteredEmployees.length }}) --</option>
          <option v-for="e in filteredEmployees" :key="e[0]" :value="e[0]">{{ e[1] }} ({{ e[2] }})</option>
        </select>
      </div>

      <div class="mb-3 pt-2 border-top">
        <div class="row g-2">
          <div class="col-12 mb-2">
            <label class="form-label small fw-bold text-muted">Địa điểm / Hình thức</label>
            <input v-model="form.location" class="form-control shadow-sm" :placeholder="headerStyles.locPlaceholder" />
          </div>
          <div class="col-12">
            <label class="form-label small fw-bold text-muted">Nội dung trao đổi</label>
            <textarea v-model="form.reason" class="form-control shadow-sm" rows="3" :placeholder="headerStyles.msgPlaceholder"></textarea>
          </div>
        </div>
      </div>

      <button @click="handleSummon" class="btn btn-lg w-100 fw-bold shadow-sm py-2 transition-all"
              :class="headerStyles.btnClass" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <i v-else class="bi bi-send-fill me-2"></i>
        {{ loading ? 'ĐANG XỬ LÝ...' : headerStyles.btnLabel }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

const auth = useAuthStore();
const roles = ref([]);
const employees = ref([]);
const isAllMode = ref(false);
const selectedRole = ref('ALL');
const searchQuery = ref('');
const loading = ref(false);
const loadingList = ref(false);

// Thêm trường priority vào form
const form = ref({ recipientId: '', location: '', reason: '', priority: 'NORMAL' });

const priorityOptions = [
  { value: 'NORMAL', label: 'Lời mời', activeClass: 'btn-info text-white' },
  { value: 'MEETING', label: 'Hẹn gặp', activeClass: 'btn-primary text-white' },
  { value: 'URGENT', label: 'Gấp', activeClass: 'btn-danger text-white' }
];

// Logic đổi Style dựa trên mức độ đã chọn
const headerStyles = computed(() => {
  switch (form.value.priority) {
    case 'URGENT':
      return {
        bg: 'bg-danger', icon: 'bi-megaphone-fill', title: 'LỆNH TRIỆU TẬP KHẨN CẤP',
        btnClass: 'btn-danger', btnLabel: 'PHÁT LỆNH NGAY',
        locPlaceholder: 'Phòng ban, Văn phòng quản lý...', msgPlaceholder: 'Lý do triệu tập khẩn cấp...'
      };
    case 'MEETING':
      return {
        bg: 'bg-primary', icon: 'bi-calendar-check-fill', title: 'LỊCH HẸN GẶP MẶT',
        btnClass: 'btn-primary', btnLabel: 'GỬI LỊCH HẸN',
        locPlaceholder: 'Phòng họp, Google Meet...', msgPlaceholder: 'Nội dung cuộc hẹn trao đổi...'
      };
    default:
      return {
        bg: 'bg-info', icon: 'bi-chat-dots-fill', title: 'GỬI LỜI MỜI / THÔNG BÁO',
        btnClass: 'btn-info text-white', btnLabel: 'GỬI LỜI MỜI',
        locPlaceholder: 'Sảnh chính, Cafe, hoặc vị trí bất kỳ...', msgPlaceholder: 'Lời mời trao đổi nhẹ nhàng...'
      };
  }
});

const filteredEmployees = computed(() => {
  if (!searchQuery.value) return employees.value;
  return employees.value.filter(e => e[1].toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const fetchRoles = async () => {
  try {
    const res = await axios.get('/api/notifications/roles', { headers: { Authorization: `Bearer ${auth.token}` } });
    roles.value = res.data;
  } catch (err) { console.error("Lỗi tải Roles:", err); }
};

const fetchEmployees = async () => {
  loadingList.value = true;
  try {
    const res = await axios.get('/api/notifications/recipient-list', {
      params: { roleName: selectedRole.value },
      headers: { Authorization: `Bearer ${auth.token}` }
    });
    employees.value = res.data;
    form.value.recipientId = '';
  } catch (err) { console.error("Lỗi tải danh sách NV:", err); }
  finally { loadingList.value = false; }
};

watch(selectedRole, fetchEmployees);

const toggleAllMode = () => {
  isAllMode.value = !isAllMode.value;
  if (isAllMode.value) form.value.recipientId = '';
};

const handleSummon = async () => {
  if (!form.value.location || !form.value.reason) return alert('Vui lòng nhập đầy đủ thông tin!');
  if (!isAllMode.value && !form.value.recipientId) return alert('Vui lòng chọn nhân viên!');

  if (!confirm(`Xác nhận gửi ${headerStyles.value.title.toLowerCase()}?`)) return;

  loading.value = true;
  try {
    const endpoint = isAllMode.value ? '/api/notifications/summon-bulk' : '/api/notifications/summon';
    await axios.post(endpoint, null, {
      params: {
        recipientId: isAllMode.value ? null : form.value.recipientId,
        roleName: isAllMode.value ? selectedRole.value : null,
        location: form.value.location,
        reason: form.value.reason,
        priority: form.value.priority // Gửi thêm mức độ lên Backend
      },
      headers: { Authorization: `Bearer ${auth.token}` }
    });
    alert('Thực hiện thành công!');
    form.value.location = ''; form.value.reason = '';
  } catch (err) {
    alert('Lỗi: ' + (err.response?.data || err.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRoles();
  fetchEmployees();
});
</script>

<style scoped>
.transition-all { transition: all 0.3s ease; }
.appointment-card { max-width: 500px; }
.form-select, .form-control { border-radius: 8px; border: 1px solid #e0e0e0; }
.form-select:focus, .form-control:focus { border-color: #0d6efd; box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.15); }
</style>