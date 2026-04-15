<template>
  <div class="appointment-card shadow-lg border-0 rounded-4 overflow-hidden bg-white mx-auto transition-all">
    <div v-if="showList" class="position-fixed top-0 start-0 w-100 h-100" @click="showList = false" style="z-index: 1040;"></div>

    <div class="card-header py-3 border-0 transition-all text-center" :class="headerStyles.bg" style="z-index: 1041; position: relative;">
      <h6 class="mb-0 d-flex align-items-center justify-content-center fw-bold text-uppercase letter-spacing-1">
        <i class="bi me-2 fs-5" :class="headerStyles.icon"></i>
        {{ headerStyles.title }}
      </h6>
    </div>

    <div class="card-body p-4" style="z-index: 1041; position: relative;">
      <div class="mb-4">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Tính chất</label>
        <div class="priority-group d-flex p-1 bg-light rounded-3">
          <button v-for="opt in priorityOptions" :key="opt.value"
                  @click="form.priority = opt.value"
                  class="btn btn-sm flex-grow-1 border-0 rounded-2 transition-all py-2"
                  :class="form.priority === opt.value ? opt.activeClass : 'text-muted'">
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div class="mb-4">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Phạm vi gửi</label>
        <div class="d-flex gap-2">
          <select v-model="selectedRole" class="form-select border-0 bg-light shadow-none" :disabled="isAllMode">
            <option value="ALL">-- Toàn bộ nhân sự --</option>
            <option v-for="role in roles" :key="role.id" :value="role.name">{{ role.name }}</option>
          </select>
          <button @click="toggleAllMode" class="btn btn-sm px-3 shadow-sm transition-all"
                  :class="isAllMode ? 'btn-warning w-50' : 'btn-outline-primary'">
            <i class="bi" :class="isAllMode ? 'bi-person-dash' : 'bi-people-fill'"></i>
            {{ isAllMode ? 'Hủy chọn' : 'Gửi nhóm' }}
          </button>
        </div>
      </div>

      <div class="mb-4 animate__animated animate__fadeIn" v-if="!isAllMode">
        <label class="form-label small fw-bold text-muted text-uppercase mb-2">Người nhận cụ thể</label>
        <div class="position-relative" style="z-index: 1050;">
          <div class="multi-select-container border rounded-3 p-2 bg-light shadow-sm min-vh-10">
            <div class="d-flex flex-wrap gap-2">
              <span v-for="id in selectedIds" :key="id"
                    class="employee-tag badge rounded-pill bg-white text-dark border d-flex align-items-center gap-2 py-2 px-3 shadow-sm">
                <span class="fw-medium">{{ getEmployeeName(id) }}</span>
                <i class="bi bi-x-circle-fill text-danger cursor-pointer" @click="removeRecipient(id)"></i>
              </span>
              <input v-model="searchQuery" type="text" class="flex-grow-1 border-0 bg-transparent shadow-none p-1"
                     placeholder="Tìm tên nhân viên..." @focus="showList = true" style="outline: none; min-width: 150px;">
            </div>
          </div>

          <div v-if="showList && filteredEmployees.length > 0"
               class="search-dropdown shadow-lg border-0 rounded-3 position-absolute start-0 end-0 bg-white mt-2 overflow-hidden animate__animated animate__fadeInUp">
            <div class="dropdown-header p-2 bg-light small text-muted border-bottom">
              Tìm thấy {{ filteredEmployees.length }} nhân viên
            </div>
            <div class="scroll-area" style="max-height: 250px; overflow-y: auto;">
              <div v-for="e in filteredEmployees" :key="e[0]"
                   class="search-item p-3 d-flex justify-content-between align-items-center transition-all border-bottom"
                   @click="addRecipient(e[0])">
                <div class="d-flex align-items-center gap-3">
                  <div class="avatar-sm bg-primary-subtle text-primary rounded-circle d-flex align-items-center justify-content-center fw-bold">
                    {{ e[1].charAt(0) }}
                  </div>
                  <div>
                    <div class="fw-bold text-dark small">{{ e[1] }}</div>
                    <div class="text-muted extra-small">{{ e[2] }}</div>
                  </div>
                </div>
                <i class="bi bi-plus-lg text-primary"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mb-4">
        <div class="row g-3">
          <div class="col-12">
            <div class="form-floating shadow-sm">
              <input v-model="form.location" class="form-control border-0 bg-light" id="loc" :placeholder="headerStyles.locPlaceholder" />
              <label for="loc" class="text-muted small">Địa điểm / Hình thức</label>
            </div>
          </div>
          <div class="col-12">
            <div class="form-floating shadow-sm">
              <textarea v-model="form.reason" class="form-control border-0 bg-light" id="reason" style="height: 100px" :placeholder="headerStyles.msgPlaceholder"></textarea>
              <label for="reason" class="text-muted small">Nội dung trao đổi</label>
            </div>
          </div>
        </div>
      </div>

      <button @click="handleSummon" class="btn btn-lg w-100 fw-bold shadow transition-all py-3 rounded-3"
              :class="headerStyles.btnClass" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        <i v-else class="bi bi-send-check-fill me-2 fs-5"></i>
        {{ loading ? 'ĐANG GỬI...' : headerStyles.btnLabel }}
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
const showList = ref(false);
const selectedIds = ref([]); // Mảng chứa các ID đã chọn

const form = ref({ location: '', reason: '', priority: 'NORMAL' });

const priorityOptions = [
  { value: 'NORMAL', label: 'Lời mời', activeClass: 'btn-info text-white' },
  { value: 'MEETING', label: 'Hẹn gặp', activeClass: 'btn-primary text-white' },
  { value: 'URGENT', label: 'Gấp', activeClass: 'btn-danger text-white' }
];

const headerStyles = computed(() => {
  switch (form.value.priority) {
    case 'URGENT':
      return { bg: 'bg-danger', icon: 'bi-megaphone-fill', title: 'LỆNH TRIỆU TẬP KHẨN CẤP', btnClass: 'btn-danger', btnLabel: 'PHÁT LỆNH NGAY', locPlaceholder: 'Vị trí khẩn cấp...', msgPlaceholder: 'Lý do triệu tập...' };
    case 'MEETING':
      return { bg: 'bg-primary', icon: 'bi-calendar-check-fill', title: 'LỊCH HẸN GẶP MẶT', btnClass: 'btn-primary', btnLabel: 'GỬI LỊCH HẸN', locPlaceholder: 'Phòng họp, Meet...', msgPlaceholder: 'Nội dung trao đổi...' };
    default:
      return { bg: 'bg-info', icon: 'bi-chat-dots-fill', title: 'GỬI LỜI MỜI / THÔNG BÁO', btnClass: 'btn-info text-white', btnLabel: 'GỬI LỜI MỜI', locPlaceholder: 'Địa điểm bất kỳ...', msgPlaceholder: 'Nội dung thông báo...' };
  }
});

const filteredEmployees = computed(() => {
  // Lọc bỏ những người đã nằm trong danh sách "Thẻ" (selectedIds)
  let list = employees.value.filter(e => !selectedIds.value.includes(e[0]));

  // Nếu không tìm kiếm, trả về toàn bộ list (không giới hạn số lượng)
  if (!searchQuery.value) return list;

  // Lọc theo tên khi người dùng gõ
  return list.filter(e => e[1].toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const addRecipient = (id) => {
  if (!selectedIds.value.includes(id)) selectedIds.value.push(id);
  searchQuery.value = '';
  showList.value = false;
};

const removeRecipient = (id) => {
  selectedIds.value = selectedIds.value.filter(i => i !== id);
};

const getEmployeeName = (id) => {
  const emp = employees.value.find(e => e[0] === id);
  return emp ? emp[1] : '';
};

const fetchRoles = async () => {
  const res = await axios.get('/api/notifications/roles', { headers: { Authorization: `Bearer ${auth.token}` } });
  roles.value = res.data;
};

const fetchEmployees = async () => {
  const res = await axios.get('/api/notifications/recipient-list', {
    params: { roleName: selectedRole.value },
    headers: { Authorization: `Bearer ${auth.token}` }
  });
  employees.value = res.data;
};

watch(selectedRole, fetchEmployees);

const toggleAllMode = () => {
  isAllMode.value = !isAllMode.value;
  if (isAllMode.value) selectedIds.value = [];
};

const handleSummon = async () => {
  if (!form.value.location || !form.value.reason) return alert('Vui lòng điền đủ thông tin!');
  if (!isAllMode.value && selectedIds.value.length === 0) return alert('Hãy chọn ít nhất một người nhận!');

  if (!confirm(`Xác nhận gửi ${headerStyles.value.title.toLowerCase()}?`)) return;

  loading.value = true;
  try {
    // Nếu gửi nhiều người, ta sẽ gửi một mảng ID xuống Backend
    const endpoint = isAllMode.value ? '/api/notifications/summon-bulk' : '/api/notifications/summon-multi';
    await axios.post(endpoint, {
      recipientIds: isAllMode.value ? null : selectedIds.value,
      roleName: isAllMode.value ? selectedRole.value : null,
      location: form.value.location,
      reason: form.value.reason,
      priority: form.value.priority
    }, { headers: { Authorization: `Bearer ${auth.token}` } });

    alert('Thành công!');
    form.value.location = ''; form.value.reason = ''; selectedIds.value = [];
  } catch (err) { alert('Lỗi: ' + (err.response?.data || err.message)); }
  finally { loading.value = false; }
};

onMounted(() => { fetchRoles(); fetchEmployees(); });
</script>

<style scoped>
.appointment-card { max-width: 520px; border-radius: 20px !important; }
.priority-group { background: #f1f3f5; }
.transition-all { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.letter-spacing-1 { letter-spacing: 0.5px; }

/* Tag nhân viên */
.employee-tag { font-size: 13px; transition: all 0.2s; border: 1px solid #e9ecef !important; }
.employee-tag:hover { transform: translateY(-1px); box-shadow: 0 4px 6px rgba(0,0,0,0.05) !important; }
.text-danger-hover:hover { color: #dc3545 !important; }

/* Dropdown tìm kiếm mỏng nhẹ */
.search-dropdown { max-height: 280px; overflow-y: auto; border: 1px solid #f1f3f5 !important; }
.search-item { cursor: pointer; border-bottom: 1px solid #f8f9fa; }
.search-item:hover { background-color: #f0f7ff; }

/* Avatar nhỏ */
.avatar-sm { width: 32px; height: 32px; font-size: 12px; }
.extra-small { font-size: 10px; }
.fs-7 { font-size: 13.5px; }

/* Floating label chỉnh sửa màu */
.form-floating > .form-control:focus ~ label { color: #0d6efd; }
.form-control:focus { background-color: #fff !important; box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.1) !important; }
.search-dropdown {
  max-height: 300px; /* Giới hạn chiều cao dropdown để không che hết form */
  overflow-y: auto;  /* Tự động hiện thanh cuộn khi danh sách dài */
  z-index: 1050;     /* Đảm bảo dropdown luôn nổi lên trên cùng */
  box-shadow: 0 10px 25px rgba(0,0,0,0.1); /* Thêm đổ bóng cho sang */
}

/* Làm thanh cuộn nhìn xịn hơn (Tùy chọn) */
.search-dropdown::-webkit-scrollbar {
  width: 6px;
}
.search-dropdown::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}
</style>