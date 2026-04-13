<template>
  <div class="summon-card shadow border-0 rounded-4 overflow-hidden bg-white mx-auto">
    <div class="card-header bg-danger text-white py-3 border-0">
      <h6 class="mb-0 d-flex align-items-center fw-bold">
        <i class="bi bi-megaphone-fill me-2 fs-5"></i> LỆNH TRIỆU TẬP NHANH
      </h6>
    </div>

    <div class="card-body p-4">
      <div class="mb-3">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Phạm vi triệu tập</label>
        <div class="d-flex gap-2">
          <select v-model="selectedRole" class="form-select shadow-sm" :disabled="isAllMode">
            <option value="ALL">-- Tất cả chức vụ --</option>
            <option v-for="role in roles" :key="role.id" :value="role.name">
              {{ role.name }}
            </option>
          </select>
          <button @click="toggleAllMode" class="btn btn-sm text-nowrap shadow-sm"
                  :class="isAllMode ? 'btn-warning' : 'btn-outline-primary'">
            <i class="bi" :class="isAllMode ? 'bi-person-x' : 'bi-people-fill'"></i>
            {{ isAllMode ? 'Hủy chọn tất cả' : 'Gọi tất cả' }}
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
          <option v-for="e in filteredEmployees" :key="e[0]" :value="e[0]">
            {{ e[1] }} ({{ e[2] }})
          </option>
        </select>
        <div v-if="loadingList" class="text-center mt-2">
          <div class="spinner-border spinner-border-sm text-primary"></div>
        </div>
      </div>

      <div v-else class="alert alert-soft-danger py-3 mb-3 border-0 shadow-sm small">
        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
        <span>Đang phát lệnh cho: <strong>{{ selectedRole === 'ALL' ? 'Toàn công ty' : 'Nhóm ' + selectedRole }}</strong></span>
      </div>

      <div class="mb-3 pt-3 border-top">
        <div class="row g-2">
          <div class="col-12 mb-2">
            <label class="form-label small fw-bold">Địa điểm</label>
            <input v-model="form.location" class="form-control shadow-sm" placeholder="Phòng họp 1, Sảnh chính..." />
          </div>
          <div class="col-12">
            <label class="form-label small fw-bold">Lý do/Nội dung</label>
            <textarea v-model="form.reason" class="form-control shadow-sm" rows="3" placeholder="Nêu lý do triệu tập..."></textarea>
          </div>
        </div>
      </div>

      <button @click="handleSummon" class="btn btn-danger btn-lg w-100 fw-bold shadow-sm py-2" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <i v-else class="bi bi-send-fill me-2"></i>
        {{ loading ? 'ĐANG XỬ LÝ...' : (isAllMode ? 'PHÁT LỆNH HÀNG LOẠT' : 'PHÁT LỆNH NGAY') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

const auth = useAuthStore();
const roles = ref([]); // Danh sách Role từ DB
const employees = ref([]);
const isAllMode = ref(false);
const selectedRole = ref('ALL');
const searchQuery = ref('');
const loading = ref(false);
const loadingList = ref(false);
const form = ref({ recipientId: '', location: '', reason: '' });

// 1. Lọc nhân viên theo ô search
const filteredEmployees = computed(() => {
  if (!searchQuery.value) return employees.value;
  return employees.value.filter(e => e[1].toLowerCase().includes(searchQuery.value.toLowerCase()));
});

// 2. Lấy danh sách Roles từ DB
const fetchRoles = async () => {
  try {
    const res = await axios.get('/api/notifications/roles', {
      headers: { Authorization: `Bearer ${auth.token}` }
    });
    roles.value = res.data;
  } catch (err) {
    console.error("Lỗi tải Roles:", err);
  }
};

// 3. Lấy nhân viên theo Role (DB Query trực tiếp)
const fetchEmployees = async () => {
  loadingList.value = true;
  try {
    const res = await axios.get('/api/notifications/recipient-list', {
      params: { roleName: selectedRole.value },
      headers: { Authorization: `Bearer ${auth.token}` }
    });
    employees.value = res.data;
    form.value.recipientId = '';
  } catch (err) {
    console.error("Lỗi tải danh sách NV:", err);
  } finally {
    loadingList.value = false;
  }
};

// Theo dõi thay đổi của Role để gọi lại danh sách nhân viên
watch(selectedRole, fetchEmployees);

const toggleAllMode = () => {
  isAllMode.value = !isAllMode.value;
  if (isAllMode.value) form.value.recipientId = '';
};

const handleSummon = async () => {
  if (!form.value.location || !form.value.reason) return alert('Điền đủ địa điểm và nội dung!');
  if (!isAllMode.value && !form.value.recipientId) return alert('Chọn nhân viên cụ thể!');

  if (!confirm('Xác nhận phát lệnh triệu tập này?')) return;

  loading.value = true;
  try {
    const endpoint = isAllMode.value ? '/api/notifications/summon-bulk' : '/api/notifications/summon';
    await axios.post(endpoint, null, {
      params: {
        recipientId: isAllMode.value ? null : form.value.recipientId,
        roleName: isAllMode.value ? selectedRole.value : null,
        location: form.value.location,
        reason: form.value.reason
      },
      headers: { Authorization: `Bearer ${auth.token}` }
    });
    alert('Thành công!');
    form.value.location = ''; form.value.reason = '';
  } catch (err) {
    alert('Lỗi: ' + (err.response?.data || err.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRoles();      // Load Roles trước
  fetchEmployees();  // Load nhân viên mặc định (ALL)
});
</script>

<style scoped>
.alert-soft-danger { background: #fff5f5; color: #dc3545; border-left: 4px solid #dc3545 !important; }
.summon-card { max-width: 500px; }
.form-select, .form-control { border-radius: 8px; border: 1px solid #e0e0e0; }
.form-select:focus, .form-control:focus { border-color: #dc3545; box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.15); }
</style>