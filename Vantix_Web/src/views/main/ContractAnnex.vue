<script setup>
import {ref, reactive, onMounted, computed, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useToast} from 'vue-toastification';

// Import service
import contractAnnexService from '@/services/contractannex.service';

const route = useRoute();
const router = useRouter();
const toast = useToast();

const contractId = route.params.id;

// --- STATE ---
const annexes = ref([]);
const showModal = ref(false);
const loading = ref(false);
const loadingForm = ref(false);

// State quản lý danh sách chức vụ
const positionList = ref([]);
const loadingPositions = ref(false);

const deleteModal = reactive({show: false, id: null});

// Thông tin cơ bản về Contract
const contractInfo = ref({
  contractId: contractId,
  employeeName: 'Đang tải...',
  currentPosition: '—',
  currentSalary: 0
});

// 🌟 Lấy ngày hôm nay dưới chuẩn YYYY-MM-DD để giới hạn ô input Date 🌟
const todayFormatted = computed(() => {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
});

// Form Model
const form = ref({
  effectiveDate: todayFormatted.value, // Khởi tạo mặc định là hôm nay
  newSalary: null,
  newPositions: '',
  content: '',
  isActive: true,
  contractId: Number(contractId)
});

// 🌟 THEO DÕI NGÀY ĐỂ AUTO ĐỔI TRẠNG THÁI 🌟
watch(() => form.value.effectiveDate, (newVal) => {
  if (!newVal) return;
  // Nếu ngày được chọn lớn hơn ngày hôm nay -> Ép false (Chờ duyệt)
  if (newVal > todayFormatted.value) {
    form.value.isActive = false;
  } else {
    // Nếu là hôm nay -> Ép true (Áp dụng ngay)
    form.value.isActive = true;
  }
});

// Computed Formatting tiền tệ (Thêm dấu chấm tự động khi nhập)
const newSalaryFormatted = computed({
  get: () => form.value.newSalary ? new Intl.NumberFormat('vi-VN').format(form.value.newSalary) : '',
  set: (val) => {
    const rawValue = val.toString().replace(/\D/g, '');
    form.value.newSalary = rawValue ? Number(rawValue) : null;
  }
});

// --- HELPER FORMATTERS ---
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(val);
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString('vi-VN', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric'
}) : '—';
const getInitials = (name) => name && name !== 'Đang tải...' ? name.charAt(0).toUpperCase() : 'U';

const GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
];

const avatarGradient = (name) => {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return GRADIENTS[Math.abs(hash) % GRADIENTS.length];
};

// --- METHODS TƯƠNG TÁC API ---
const fetchAnnexes = async () => {
  loading.value = true;
  try {
    const response = await contractAnnexService.getByContractId(contractId);
    const data = response.data;

    contractInfo.value = {
      contractId: data.contractId,
      employeeName: data.employeeName || 'Không xác định',
      currentPosition: data.currentPosition || '—',
      currentSalary: data.currentSalary || 0
    };

    annexes.value = data.annexes || [];

  } catch (error) {
    const errorMsg = error.response?.data?.message || error.response?.data || error.message;
    toast.error('Lỗi tải dữ liệu: ' + errorMsg);
  } finally {
    loading.value = false;
  }
};

const openCreateModal = async () => {
  form.value = {
    effectiveDate: todayFormatted.value, // Đặt lại về hôm nay mỗi khi mở
    newSalary: null,
    newPositions: '',
    content: '',
    isActive: true,
    contractId: Number(contractId)
  };

  showModal.value = true;

  if (positionList.value.length === 0) {
    loadingPositions.value = true;
    try {
      const response = await contractAnnexService.getPositions();
      positionList.value = response.data;
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.response?.data || error.message;
      toast.error('Không thể tải danh sách chức vụ: ' + errorMsg);
    } finally {
      loadingPositions.value = false;
    }
  }
};

const handleSubmit = async () => {
  const hasSalary = form.value.newSalary !== null && form.value.newSalary !== '';
  const hasPosition = form.value.newPositions !== null && form.value.newPositions.trim() !== '';

  if (!hasSalary && !hasPosition) {
    toast.warning("Vui lòng nhập ít nhất Mức lương mới hoặc Chức vụ mới!");
    return;
  }

  loadingForm.value = true;
  try {
    const payload = {
      contractId: Number(contractId),
      effectiveDate: form.value.effectiveDate,
      newSalary: hasSalary ? Number(form.value.newSalary) : null,
      newPositions: hasPosition ? form.value.newPositions.trim() : null,
      content: form.value.content,
      active: form.value.isActive,
      isActive: form.value.isActive
    };

    await contractAnnexService.create(payload);
    await fetchAnnexes();

    showModal.value = false;
    toast.success('Đã thêm phụ lục thành công!');

  } catch (error) {
    showModal.value = false;

    let errorMsg = 'Không thể tạo phụ lục mới. Vui lòng thử lại!';
    if (error.response && error.response.data) {
      const errData = error.response.data;
      if (typeof errData === 'string') {
        errorMsg = errData;
      } else if (typeof errData === 'object') {
        errorMsg = errData.message || errData.error || errorMsg;
      }
    }

    toast.error(errorMsg);
  } finally {
    loadingForm.value = false;
  }
};

const toggleAnnexStatus = async (annexId) => {
  try {
    loading.value = true;
    await contractAnnexService.updateStatus(annexId);
    await fetchAnnexes();
    toast.success('Cập nhật trạng thái phụ lục thành công!');
  } catch (error) {
    let errorMsg = 'Lỗi cập nhật trạng thái phụ lục.';
    if (error.response && error.response.data) {
      errorMsg = typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || errorMsg);
    }
    toast.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

const confirmDelete = (id) => {
  deleteModal.id = id;
  deleteModal.show = true;
};

const executeDelete = async () => {
  try {
    await contractAnnexService.delete(deleteModal.id);
    await fetchAnnexes();

    deleteModal.show = false;
    toast.success('Đã xóa phụ lục khỏi hệ thống.');
  } catch (error) {
    deleteModal.show = false;

    let errorMsg = 'Xóa phụ lục thất bại!';
    if (error.response && error.response.data) {
      errorMsg = typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || errorMsg);
    }
    toast.error(errorMsg);
  }
};

const goBack = () => {
  router.push('/contracts');
};

onMounted(() => {
  if (contractId) {
    fetchAnnexes();
  } else {
    toast.error('Không tìm thấy ID hợp đồng!');
    goBack();
  }
});
</script>

<template>
  <div class="annex-management">
    <div class="page-header">
      <div class="header-left">
        <div class="title-icon">
          <i class="bi bi-journal-text"></i>
        </div>
        <div>
          <h1 class="page-title">Phụ Lục Hợp Đồng</h1>
          <p class="page-description">Quản lý các thay đổi và phụ lục của Hợp đồng #{{ contractId }}</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn-outline" @click="goBack">
          <i class="bi bi-arrow-left"></i>
          <span>Quay lại</span>
        </button>
        <button class="btn-primary" @click="openCreateModal">
          <i class="bi bi-plus-lg"></i>
          <span>Thêm Phụ Lục</span>
        </button>
      </div>
    </div>

    <div class="filter-card info-card">
      <div class="info-avatar" :style="{ background: avatarGradient(contractInfo.employeeName) }">
        {{ getInitials(contractInfo.employeeName) }}
      </div>
      <div class="info-content">
        <h3 class="info-name">{{ contractInfo.employeeName }}</h3>
        <p class="info-sub">Hợp đồng gốc: <strong>#{{ contractInfo.contractId }}</strong></p>
      </div>
      <div class="info-stats">
        <div class="info-stat-item">
          <i class="bi bi-briefcase text-muted"></i>
          <span class="stat-label">Chức vụ hiện tại:</span>
          <span class="stat-value font-medium">{{ contractInfo.currentPosition || '—' }}</span>
        </div>
        <div class="info-stat-item">
          <i class="bi bi-cash-stack text-success"></i>
          <span class="stat-label">Lương hiện tại:</span>
          <span class="salary-text">{{ formatCurrency(contractInfo.currentSalary || 0) }}</span>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner-ring"></div>
        <p>Đang tải dữ liệu phụ lục...</p>
      </div>

      <div v-else-if="annexes.length === 0" class="empty-state">
        <div class="empty-icon"><i class="bi bi-journal-x"></i></div>
        <h3>Chưa có phụ lục nào</h3>
        <p>Hợp đồng này hiện tại chưa có bất kỳ sự thay đổi hay phụ lục nào.</p>
        <button class="btn-primary" @click="openCreateModal">
          <i class="bi bi-plus-lg"></i>
          Tạo phụ lục đầu tiên
        </button>
      </div>

      <div v-else class="table-responsive">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-num">ID</th>
            <th>NGÀY HIỆU LỰC</th>
            <th>LƯƠNG MỚI</th>
            <th>CHỨC VỤ MỚI</th>
            <th>NỘI DUNG THAY ĐỔI</th>
            <th>TRẠNG THÁI</th>
            <th class="col-actions text-center">THAO TÁC</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="a in annexes" :key="a.annexId" class="table-row">
            <td class="col-num font-medium">#{{ a.annexId }}</td>
            <td>
              <span class="date-cell">
                <i class="bi bi-calendar-check"></i>
                {{ formatDate(a.effectiveDate) }}
              </span>
            </td>
            <td>
              <span v-if="a.newSalary" class="salary-text">{{ formatCurrency(a.newSalary) }}</span>
              <span v-else class="text-muted">—</span>
            </td>
            <td>
              <span v-if="a.newPositions" class="role-badge">
                <i class="bi bi-person-lines-fill"></i>
                {{ a.newPositions }}
              </span>
              <span v-else class="text-muted">—</span>
            </td>
            <td class="content-cell">{{ a.content }}</td>
            <td>
              <span :class="['status-badge', a.active ? 'status-active' : 'status-locked']">
                <span class="status-dot"></span>
                {{ a.active ? 'Áp dụng' : 'Chờ duyệt / Hủy' }}
              </span>
            </td>
            <td>
              <div class="action-buttons justify-content-center">
                <button class="action-btn edit-btn" @click="toggleAnnexStatus(a.annexId)" title="Đổi trạng thái">
                  <i class="bi bi-arrow-repeat"></i>
                </button>
                <button class="action-btn delete-btn" @click="confirmDelete(a.annexId)" title="Xóa phụ lục">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- CREATE MODAL -->
    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
          <div class="modal-container">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon modal-icon-create">
                  <i class="bi bi-journal-plus"></i>
                </div>
                <div>
                  <h3>Thêm Phụ Lục Mới</h3>
                  <p>Cập nhật thay đổi cho Hợp đồng #{{ contractId }}</p>
                </div>
              </div>
              <button class="modal-close" @click="showModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <form @submit.prevent="handleSubmit">
              <div class="modal-body">
                <div class="form-row">
                  <!-- 🌟 GẮN THUỘC TÍNH MIN VÀO INPUT DATE 🌟 -->
                  <div class="form-group">
                    <label>Ngày hiệu lực <span class="required">*</span></label>
                    <div class="input-wrapper">
                      <i class="bi bi-calendar-event"></i>
                      <input v-model="form.effectiveDate" type="date" :min="todayFormatted" required/>
                    </div>
                  </div>

                  <!-- 🌟 Ô TRẠNG THÁI BIẾN THÀNH READ-ONLY 🌟 -->
                  <div class="form-group">
                    <label>Trạng thái tự động</label>
                    <div class="input-wrapper status-readonly-wrapper">
                      <i class="bi"
                         :class="form.isActive ? 'bi-check-circle-fill status-active-icon' : 'bi-clock-history status-pending-icon'"></i>
                      <input type="text" :value="form.isActive ? 'Áp dụng ngay' : 'Chờ đến ngày'" disabled
                             class="status-readonly-input"/>
                    </div>
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>Mức lương mới (VNĐ)</label>
                    <div class="input-wrapper">
                      <i class="bi bi-cash"></i>
                      <input v-model="newSalaryFormatted" type="text" placeholder="Bỏ trống nếu không đổi"/>
                    </div>
                  </div>
                  <div class="form-group">
                    <label>Chức vụ mới</label>
                    <div class="select-wrapper-modal">
                      <select v-model="form.newPositions" class="modal-select" :disabled="loadingPositions">
                        <option value="">Bỏ trống nếu không đổi</option>
                        <option v-for="(pos, index) in positionList" :key="index" :value="pos.positionName">
                          {{ pos.positionName }}
                        </option>
                      </select>
                      <i class="bi bi-chevron-down select-icon-modal"></i>
                    </div>
                  </div>
                </div>

                <div class="form-group">
                  <label>Nội dung / Lý do <span class="required">*</span></label>
                  <div class="input-wrapper textarea-wrapper">
                    <i class="bi bi-card-text"></i>
                    <textarea v-model="form.content" rows="3" placeholder="Ví dụ: Tăng lương định kỳ năm..."
                              required></textarea>
                  </div>
                </div>
              </div>

              <div class="modal-footer">
                <button type="button" class="btn-secondary" @click="showModal = false" :disabled="loadingForm">Hủy
                </button>
                <button type="submit" class="btn-primary" :disabled="loadingForm">
                  <i class="bi bi-save"></i>
                  Lưu Phụ Lục
                </button>
              </div>
            </form>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- DELETE CONFIRM MODAL -->
    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="deleteModal.show" class="modal-overlay" @click.self="deleteModal.show = false">
          <div class="modal-container modal-sm">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon modal-icon-danger">
                  <i class="bi bi-exclamation-triangle-fill"></i>
                </div>
                <div>
                  <h3>Xóa Phụ Lục</h3>
                  <p>Hành động không thể hoàn tác</p>
                </div>
              </div>
              <button class="modal-close" @click="deleteModal.show = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div class="modal-body delete-body">
              <div class="delete-avatar">
                <i class="bi bi-trash3"></i>
              </div>
              <p class="delete-name">Phụ lục #{{ deleteModal.id }}</p>
              <p class="delete-warning">Bạn có chắc chắn muốn xóa phụ lục này khỏi hệ thống vĩnh viễn không?</p>
            </div>
            <div class="modal-footer">
              <button class="btn-secondary" @click="deleteModal.show = false">Hủy</button>
              <button class="btn-danger" @click="executeDelete">
                <i class="bi bi-trash"></i>
                Đồng ý Xóa
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.annex-management {
  padding: 28px 32px;
  min-height: 100vh;
  background: #f0f4ff;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  flex-shrink: 0;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.page-description {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* Thẻ thông tin HĐ */
.info-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e8edff;
  margin-bottom: 24px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  flex-wrap: wrap;
}

.info-avatar {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  min-width: 200px;
}

.info-name {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.info-sub {
  font-size: 13px;
  color: #64748b;
}

.info-sub strong {
  color: #4f46e5;
}

.info-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  border-left: 1.5px solid #f1f5f9;
  padding-left: 24px;
}

.info-stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
}

.info-stat-item i {
  font-size: 16px;
}

.stat-label {
  color: #64748b;
}

.stat-value {
  color: #0f172a;
}

/* Nút bấm */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
  font-family: inherit;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: white;
  color: #475569;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-outline:hover {
  background: #f8fafc;
  border-color: #c7d2fe;
  color: #4f46e5;
}

.btn-secondary {
  padding: 9px 18px;
  background: #f1f5f9;
  color: #475569;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  background: linear-gradient(135deg, #f43f5e, #dc2626);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.25);
  font-family: inherit;
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.35);
}

/* Bảng */
.table-container {
  background: white;
  border-radius: 18px;
  border: 1px solid #e8edff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.loading-state, .empty-state {
  padding: 80px 20px;
  text-align: center;
  color: #64748b;
}

.spinner-ring {
  width: 40px;
  height: 40px;
  border: 3px solid #e8edff;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-icon {
  width: 72px;
  height: 72px;
  background: #f0f4ff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #6366f1;
  margin: 0 auto 20px;
}

.empty-state h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 20px;
}

.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead tr {
  background: #fafbff;
  border-bottom: 1.5px solid #e8edff;
}

.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 11.5px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  white-space: nowrap;
}

.data-table td {
  padding: 14px 16px;
  font-size: 13px;
  color: #334155;
  vertical-align: middle;
  border-bottom: 1px solid #f1f5f9;
}

.table-row:hover {
  background: #fafbff;
}

.col-num {
  width: 60px;
}

.text-muted {
  color: #94a3b8;
}

.font-medium {
  font-weight: 600;
  color: #475569;
}

/* Status Badges */
.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  background: #f0f4ff;
  color: #4f46e5;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #e0e7ff;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.status-locked {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-active .status-dot {
  background: #10b981;
}

.status-locked .status-dot {
  background: #ef4444;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

.date-cell i {
  color: #c7d2fe;
  font-size: 14px;
}

.salary-text {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13.5px;
  font-weight: 600;
  color: #059669;
  background: #f0fdf4;
  padding: 4px 8px;
  border-radius: 6px;
}

.content-cell {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 6px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  transition: all 0.2s;
}

.edit-btn {
  background: #ede9fe;
  color: #7c3aed;
}

.edit-btn:hover {
  background: #7c3aed;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(124, 58, 237, 0.3);
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;
}

.delete-btn:hover {
  background: #dc2626;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.3);
}

/* Modals */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 540px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.modal-sm {
  max-width: 380px;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1.5px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal-title-group {
  display: flex;
  align-items: center;
  gap: 14px;
}

.modal-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  flex-shrink: 0;
}

.modal-icon-create {
  background: #ede9fe;
  color: #7c3aed;
}

.modal-icon-danger {
  background: #fee2e2;
  color: #dc2626;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.modal-header p {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 14px;
  color: #94a3b8;
  cursor: pointer;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border-radius: 8px;
  width: 30px;
  height: 30px;
}

.modal-close:hover {
  color: #475569;
  background: #f1f5f9;
  transform: rotate(90deg);
}

.modal-body {
  padding: 22px 24px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1.5px solid #f1f5f9;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* Form Groups */
.form-group {
  margin-bottom: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 7px;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.required {
  color: #f43f5e;
}

.input-wrapper {
  position: relative;
}

.input-wrapper > i:first-child {
  position: absolute;
  left: 13px;
  top: 12px;
  color: #c7d2fe;
  font-size: 14px;
  pointer-events: none;
  z-index: 1;
}

.input-wrapper input, .input-wrapper textarea {
  width: 100%;
  padding: 10px 12px 10px 38px;
  border: 1.5px solid #e8edff;
  border-radius: 11px;
  font-size: 13.5px;
  background: #fafbff;
  transition: all 0.2s ease;
  font-family: inherit;
  color: #0f172a;
  outline: none;
}

.input-wrapper textarea {
  resize: vertical;
  min-height: 80px;
}

.input-wrapper input:focus, .input-wrapper textarea:focus {
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.select-wrapper-modal {
  position: relative;
}

.modal-select {
  width: 100%;
  padding: 10px 32px 10px 13px;
  border: 1.5px solid #e8edff;
  border-radius: 11px;
  font-size: 13.5px;
  background: #fafbff;
  cursor: pointer;
  appearance: none;
  transition: all 0.2s ease;
  font-family: inherit;
  color: #334155;
  font-weight: 500;
  outline: none;
}

.modal-select:focus {
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.select-icon-modal {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #c7d2fe;
  font-size: 11px;
  pointer-events: none;
}

/* 🌟 Custom Status Readonly CSS 🌟 */
.status-readonly-input {
  background: #f8fafc !important;
  color: #64748b !important;
  font-weight: 600;
  border-color: #e2e8f0 !important;
  cursor: not-allowed;
}

.status-active-icon {
  color: #10b981 !important; /* Xanh lá */
}

.status-pending-icon {
  color: #f59e0b !important; /* Vàng cam */
}


/* Modal Xóa */
.delete-body {
  text-align: center;
  padding: 24px 20px;
}

.delete-avatar {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dc2626;
  background: #fee2e2;
  font-size: 26px;
  margin: 0 auto 14px;
}

.delete-name {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.delete-warning {
  font-size: 13px;
  color: #64748b;
}

/* Transitions */
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-active .modal-container, .modal-fade-leave-active .modal-container {
  transition: transform 0.25s ease;
}

.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-container {
  transform: scale(0.94) translateY(16px);
}

.modal-fade-leave-to .modal-container {
  transform: scale(0.94) translateY(16px);
}

@media (max-width: 768px) {
  .annex-management {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .info-stats {
    border-left: none;
    padding-left: 0;
    margin-top: 10px;
    flex-direction: column;
    gap: 8px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>