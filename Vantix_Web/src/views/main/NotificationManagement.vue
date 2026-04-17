<template>
  <div class="corporate-wrapper py-3">
    <div v-if="showList" class="position-fixed top-0 start-0 w-100 h-100 z-10" @click="showList = false"></div>

    <div class="corp-card mx-auto position-relative z-20">

      <div class="corp-header d-flex align-items-center border-bottom px-4 py-3" :class="headerStyles.headerBorder">
        <i class="bi fs-5 me-2" :class="[headerStyles.icon, headerStyles.iconColor]"></i>
        <h6 class="mb-0 fw-bold text-dark text-uppercase">{{ headerStyles.title }}</h6>
      </div>

      <div class="corp-body p-4">
        <div class="mb-4">
          <label class="form-label corp-label mb-2">Loại thông báo <span class="text-danger">*</span></label>
          <div class="d-flex bg-light p-1 rounded-1 border">
            <button v-for="opt in priorityOptions" :key="opt.value"
                    @click="form.priority = opt.value"
                    class="btn btn-sm flex-grow-1 fw-medium transition-all rounded-1 border-0"
                    :class="form.priority === opt.value ? opt.activeClass : 'text-muted hover-bg-gray'">
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="mb-4">
          <label class="form-label corp-label mb-2">Phòng ban / Nhóm nhận <span class="text-danger">*</span></label>
          <div class="d-flex gap-2">
            <select v-model="selectedRole" class="form-select corp-input shadow-none" :disabled="isAllMode">
              <option value="ALL">-- Toàn bộ nhân sự công ty --</option>
              <option v-for="role in roles" :key="role.id" :value="role.name">{{ role.name }}</option>
            </select>
            <button @click="toggleAllMode" class="btn px-3 transition-all fw-medium corp-btn-toggle"
                    :class="isAllMode ? 'btn-outline-danger' : 'btn-outline-primary'">
              <i class="bi me-1" :class="isAllMode ? 'bi-x-circle' : 'bi-people'"></i>
              {{ isAllMode ? 'Hủy gửi nhóm' : 'Gửi theo nhóm' }}
            </button>
          </div>
        </div>

        <div class="mb-4 animate__animated animate__fadeIn" v-if="!isAllMode">
          <label class="form-label corp-label mb-2">Nhân viên cụ thể (Tùy chọn)</label>
          <div class="position-relative">
            <div class="corp-input p-1 d-flex flex-wrap gap-1 align-items-center cursor-text" @click="$refs.searchInput.focus()">

              <span v-for="id in selectedIds" :key="id"
                    class="corp-chip d-flex align-items-center gap-2 py-1 px-2 rounded-1">
                <span class="small fw-medium text-dark">{{ getEmployeeName(id) }}</span>
                <i class="bi bi-x cursor-pointer text-secondary hover-dark fs-6 line-height-1" @click.stop="removeRecipient(id)"></i>
              </span>

              <input ref="searchInput" v-model="searchQuery" type="text" class="border-0 bg-transparent shadow-none p-1 flex-grow-1 outline-none text-dark small"
                     placeholder="Tìm kiếm nhân viên..." @focus="showList = true">
            </div>

            <div v-if="showList && filteredEmployees.length > 0"
                 class="corp-dropdown position-absolute start-0 end-0 mt-1 overflow-hidden bg-white">
              <div class="p-2 dropdown-header text-muted small fw-medium border-bottom bg-light">
                Kết quả tìm kiếm ({{ filteredEmployees.length }})
              </div>
              <div class="scroll-area">
                <div v-for="e in filteredEmployees" :key="e[0]"
                     class="search-item px-3 py-2 d-flex justify-content-between align-items-center border-bottom"
                     @click="addRecipient(e[0])">
                  <div class="d-flex align-items-center gap-2">
                    <div class="corp-avatar bg-light text-primary fw-bold border">
                      {{ e[1].charAt(0) }}
                    </div>
                    <div>
                      <div class="fw-medium text-dark small">{{ e[1] }}</div>
                      <div class="text-muted" style="font-size: 11px;">{{ e[2] }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-12">
            <label class="form-label corp-label mb-2">Địa điểm / Hình thức</label>
            <input v-model="form.location" class="form-control corp-input shadow-none small" :placeholder="headerStyles.locPlaceholder" />
          </div>
          <div class="col-12">
            <label class="form-label corp-label mb-2">Nội dung chi tiết <span class="text-danger">*</span></label>
            <textarea v-model="form.reason" class="form-control corp-input shadow-none small" rows="3" :placeholder="headerStyles.msgPlaceholder"></textarea>
          </div>
        </div>

        <div class="border-top pt-3 d-flex justify-content-end">
          <button @click="handleSummon" class="btn fw-medium px-4 py-2 transition-all corp-btn-submit d-flex align-items-center gap-2"
                  :class="headerStyles.btnClass" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <i v-else class="bi bi-send"></i>
            {{ loading ? 'Đang xử lý...' : headerStyles.btnLabel }}
          </button>
        </div>
      </div>
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
const selectedIds = ref([]);

const form = ref({ location: '', reason: '', priority: 'NORMAL' });

// Style buttons theo chuẩn UI Doanh nghiệp (Xanh Navy, Đỏ Cảnh báo)
const priorityOptions = [
  { value: 'NORMAL', label: 'Thông báo thường', activeClass: 'bg-white text-dark shadow-sm border' },
  { value: 'MEETING', label: 'Mời họp / Hẹn gặp', activeClass: 'bg-primary text-white shadow-sm' },
  { value: 'URGENT', label: 'Lệnh khẩn cấp', activeClass: 'bg-danger text-white shadow-sm' }
];

const headerStyles = computed(() => {
  switch (form.value.priority) {
    case 'URGENT':
      return { headerBorder: 'border-danger-left', iconColor: 'text-danger', icon: 'bi-exclamation-triangle', title: 'Lệnh triệu tập khẩn', btnClass: 'btn-danger', btnLabel: 'Phát lệnh ngay', locPlaceholder: 'VD: Phòng họp A, Sảnh...', msgPlaceholder: 'Nêu rõ lý do triệu tập khẩn cấp...' };
    case 'MEETING':
      return { headerBorder: 'border-primary-left', iconColor: 'text-primary', icon: 'bi-calendar-event', title: 'Lịch hẹn gặp mặt', btnClass: 'btn-primary', btnLabel: 'Gửi lời mời', locPlaceholder: 'VD: Link Google Meet, Phòng HR...', msgPlaceholder: 'Nêu nội dung buổi họp...' };
    default:
      return { headerBorder: 'border-secondary-left', iconColor: 'text-secondary', icon: 'bi-envelope', title: 'Tạo thông báo mới', btnClass: 'btn-dark', btnLabel: 'Gửi thông báo', locPlaceholder: 'VD: Bất kỳ...', msgPlaceholder: 'Nội dung thông báo chung...' };
  }
});

const filteredEmployees = computed(() => {
  let list = employees.value.filter(e => !selectedIds.value.includes(e[0]));
  if (!searchQuery.value) return list;
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
  // Kiểm tra dữ liệu rỗng
  if (!form.value.location || !form.value.reason) {
    return alert('Vui lòng điền đủ thông tin bắt buộc (*)!');
  }
  if (!isAllMode.value && selectedIds.value.length === 0) {
    return alert('Hãy chọn ít nhất một người nhận!');
  }

  if (!confirm(`Xác nhận gửi ${headerStyles.value.title}?`)) return;

  loading.value = true;
  try {
    const endpoint = isAllMode.value
        ? '/api/notifications/summon-bulk'
        : '/api/notifications/summon-multi';

    // Đóng gói dữ liệu chuẩn DTO
    const payload = {
      roleName: isAllMode.value ? selectedRole.value : null,
      recipientIds: isAllMode.value ? null : selectedIds.value,
      location: form.value.location,
      reason: form.value.reason,
      priority: form.value.priority
    };

    // Gửi lên Backend
    await axios.post(endpoint, payload, {
      headers: { Authorization: `Bearer ${auth.token}` }
    });

    alert('Gửi thông báo thành công!');

    // Reset form sau khi gửi
    form.value.location = '';
    form.value.reason = '';
    selectedIds.value = [];
  } catch (err) {
    alert('Lỗi hệ thống: ' + (err.response?.data || err.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => { fetchRoles(); fetchEmployees(); });
</script>

<style scoped>
/* GIAO DIỆN CHUẨN DOANH NGHIỆP (ENTERPRISE) */
.corp-card {
  max-width: 560px;
  background-color: #ffffff;
  border: 1px solid #dfe1e6;
  border-radius: 4px;
  /* Đảm bảo bóng mờ không quá đậm gây rối mắt khi chồng lớp */
  box-shadow: 0 4px 8px -2px rgba(9, 30, 66, 0.05);
  position: relative;
  z-index: 1; /* Ưu tiên thấp nhất so với Header */
}

.z-10 { z-index: 100; }
.z-20 { z-index: 101; }
.transition-all { transition: all 0.2s ease; }
.outline-none:focus { outline: none; }
.cursor-text { cursor: text; }
.cursor-pointer { cursor: pointer; }
.line-height-1 { line-height: 1; }

/* HEADER BORDER TRÁI CHUẨN STATUS */
.border-danger-left { border-left: 4px solid #dc3545 !important; }
.border-primary-left { border-left: 4px solid #0d6efd !important; }
.border-secondary-left { border-left: 4px solid #6c757d !important; }

/* LABEL VÀ TEXT */
.corp-label {
  font-size: 13px;
  font-weight: 600;
  color: #42526e; /* Xám xanh Atlassian */
}

/* INPUT FORM */
.corp-input {
  background-color: #fafbfc;
  border: 2px solid #dfe1e6;
  border-radius: 4px;
  color: #172b4d;
  transition: background-color 0.2s, border-color 0.2s;
  font-size: 14px;
}
.corp-input:focus-within, .corp-input:focus {
  background-color: #ffffff;
  border-color: #4c9aff; /* Xanh focus rõ ràng */
}

/* TOGGLE BUTTONS */
.hover-bg-gray:hover { background-color: #ebecf0; color: #172b4d !important; }
.corp-btn-toggle { border-radius: 4px; font-size: 13px; }

/* CHIPS (THẺ NHÂN VIÊN) */
.corp-chip {
  background-color: #ebecf0; /* Nền xám nhạt chuẩn chip */
  border: 1px solid transparent;
}
.corp-chip:hover { background-color: #dfe1e6; }
.hover-dark:hover { color: #172b4d !important; }

/* DROPDOWN TÌM KIẾM */
.corp-dropdown {
  border: 1px solid #dfe1e6;
  border-radius: 4px;
  box-shadow: 0 8px 12px rgba(9, 30, 66, 0.15), 0 0 1px rgba(9, 30, 66, 0.31);
  z-index: 1060;
}
.scroll-area { max-height: 240px; overflow-y: auto; }
.scroll-area::-webkit-scrollbar { width: 8px; }
.scroll-area::-webkit-scrollbar-thumb { background: #c1c7d0; border-radius: 4px; }
.search-item { cursor: pointer; transition: background 0.1s; }
.search-item:hover { background-color: #f4f5f7; border-left: 2px solid #0d6efd; padding-left: 14px !important; }

/* AVATAR DOANH NGHIỆP */
.corp-avatar {
  width: 28px; height: 28px;
  border-radius: 4px; /* Avatar vuông bo nhẹ chứ không tròn xoe */
  display: flex; align-items: center; justify-content: center;
  font-size: 12px;
}

/* NÚT SUBMIT */
.corp-btn-submit {
  border-radius: 4px;
  font-size: 14px;
}
</style>