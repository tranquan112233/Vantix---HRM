<script setup>
import {ref, computed, onMounted, watch} from 'vue';
import {useRouter} from 'vue-router';
import contractService from "../assets/service/contract.service.js";
import positionsService from "../assets/service/positions.service.js";

const router = useRouter();

// --- STATE ---
const contracts = ref([]);
const dbPositions = ref([]);
const loading = ref(false);
const showModal = ref(false);
const message = ref('');
const messageType = ref('success');

// 🌟 STATE BỘ LỌC ĐA TẦNG
const currentFilter = ref('ALL');
const searchQuery = ref('');
const selectedType = ref('ALL');
const selectedPosition = ref('ALL');
const minSalary = ref(null);
const maxSalary = ref(null);

// Form Model
const form = ref({
  contractId: null,
  employeeId: '',
  type: 'YEAR_1',
  startDate: '',
  endDate: '',
  position: '',
  baseSalary: 0,
  status: 'ACTIVE'
});

// 🌟 STATE CHO CUSTOM DROPDOWN VỊ TRÍ
const positionSearch = ref('');
const showPositionDropdown = ref(false);

watch(positionSearch, (newVal) => {
  form.value.position = newVal;
});

const filteredFormPositions = computed(() => {
  if (!positionSearch.value) return dbPositions.value;
  const query = positionSearch.value.toLowerCase();
  return dbPositions.value.filter(pos => {
    const name = pos.positionName || pos.name || pos;
    return name.toLowerCase().includes(query);
  });
});

const selectPosition = (pos) => {
  const name = pos.positionName || pos.name || pos;
  positionSearch.value = name;
  form.value.position = name;
  showPositionDropdown.value = false;
};

// --- HELPER FORMATTERS ---
const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(value);
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString('vi-VN') : 'Vô thời hạn';
const getTypeLabel = (type) => {
  const map = {'YEAR_1': '1 Năm', 'YEAR_3': '3 Năm', 'INDEFINITE': 'Vô thời hạn'};
  return map[type] || type;
};

const availablePositions = computed(() => {
  const positions = contracts.value.map(c => c.position).filter(Boolean);
  return [...new Set(positions)];
});

// --- COMPUTED STATS ---
const totalContracts = computed(() => contracts.value.length);
const activeContracts = computed(() => contracts.value.filter(c => c.status === 'ACTIVE').length);
const expiredContracts = computed(() => contracts.value.filter(c => c.status === 'EXPIRED').length);

// --- LOGIC LỌC ĐA TẦNG ---
const filteredContracts = computed(() => {
  let result = contracts.value;

  if (currentFilter.value !== 'ALL') result = result.filter(c => c.status === currentFilter.value);
  if (selectedType.value !== 'ALL') result = result.filter(c => c.type === selectedType.value);
  if (selectedPosition.value !== 'ALL') result = result.filter(c => c.position === selectedPosition.value);

  if (minSalary.value !== null && minSalary.value !== '') {
    result = result.filter(c => c.baseSalary >= Number(minSalary.value));
  }
  if (maxSalary.value !== null && maxSalary.value !== '') {
    result = result.filter(c => c.baseSalary <= Number(maxSalary.value));
  }

  if (searchQuery.value.trim() !== '') {
    const query = searchQuery.value.toLowerCase().trim();
    result = result.filter(c => {
      const nameMatch = c.employee?.fullName?.toLowerCase().includes(query);
      const idMatch = c.contractId?.toString().includes(query);
      return nameMatch || idMatch;
    });
  }
  return result;
});

const isFilterActive = computed(() => {
  return currentFilter.value !== 'ALL' || selectedType.value !== 'ALL' ||
      selectedPosition.value !== 'ALL' || searchQuery.value !== '' ||
      (minSalary.value !== null && minSalary.value !== '') ||
      (maxSalary.value !== null && maxSalary.value !== '');
});

const clearFilters = () => {
  currentFilter.value = 'ALL';
  searchQuery.value = '';
  selectedType.value = 'ALL';
  selectedPosition.value = 'ALL';
  minSalary.value = null;
  maxSalary.value = null;
};

// --- AUTO-CALCULATE END DATE LOGIC ---
watch([() => form.value.startDate, () => form.value.type], ([newStart, newType]) => {
  if (!newStart || newType === 'INDEFINITE') {
    form.value.endDate = '';
    return;
  }
  const date = new Date(newStart);
  if (newType === 'YEAR_1') date.setFullYear(date.getFullYear() + 1);
  else if (newType === 'YEAR_3') date.setFullYear(date.getFullYear() + 3);

  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  form.value.endDate = `${yyyy}-${mm}-${dd}`;
});


// --- METHODS CALL API THẬT ---
const fetchContracts = async () => {
  loading.value = true;
  try {
    const response = await contractService.getAllContracts();
    contracts.value = response.data;
  } catch (error) {
    console.error("Lỗi API Contracts:", error);
  } finally {
    loading.value = false;
  }
};

const fetchPositions = async () => {
  try {
    const response = await positionsService.getAllPositions();
    dbPositions.value = response.data;
  } catch (error) {
    console.error("Lỗi API Positions:", error);
  }
};

const openCreateModal = () => {
  positionSearch.value = '';
  showPositionDropdown.value = false;

  form.value = {
    contractId: null,
    employeeId: '',
    type: 'YEAR_1',
    startDate: '',
    endDate: '',
    position: '',
    baseSalary: 0,
    status: 'ACTIVE'
  };
  showModal.value = true;
};

// 🌟 ĐÃ XÓA CODE DEMO TẠO MỚI, THAY BẰNG API THẬT
const handleSubmit = async () => {
  if (!form.value.position.trim()) {
    alert("Vui lòng chọn hoặc nhập vị trí công việc!");
    return;
  }

  const payload = {
    type: form.value.type,
    position: form.value.position,
    startDate: form.value.startDate,
    endDate: form.value.endDate || null,
    baseSalary: Number(form.value.baseSalary),
    status: form.value.status,
    employee: {
      employeeId: Number(form.value.employeeId)
    }
  };

  try {
    loading.value = true;

    // GỌI API THÊM MỚI XUỐNG DATABASE
    await contractService.createContract(payload);

    // NẾU THÊM THÀNH CÔNG -> GỌI LẠI API LẤY DANH SÁCH MỚI NHẤT
    await fetchContracts();

    showMessage('Tạo hợp đồng thành công!', 'success');
    showModal.value = false;
  } catch (error) {
    console.error("Lỗi khi tạo hợp đồng:", error);
    // Bắt lỗi từ BE trả về (Ví dụ: ID nhân viên không tồn tại)
    const errorMsg = error.response?.data || 'Có lỗi xảy ra, vui lòng kiểm tra lại ID nhân viên!';
    showMessage(errorMsg, 'warning');
  } finally {
    loading.value = false;
  }
};

// 🌟 ĐÃ XÓA CODE DEMO XÓA, THAY BẰNG API THẬT
const deleteContract = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa hợp đồng này? Chú ý: Hành động này không thể hoàn tác.')) {
    try {
      loading.value = true;

      // GỌI API XÓA DỮ LIỆU THẬT
      await contractService.deleteContract(id);

      // LỌC BỎ HỢP ĐỒNG ĐÃ XÓA KHỎI UI MÀ KHÔNG CẦN TẢI LẠI TRANG
      contracts.value = contracts.value.filter(c => c.contractId !== id);

      showMessage('Đã xóa hợp đồng thành công.', 'success');
    } catch (error) {
      console.error("Lỗi khi xóa hợp đồng:", error);
      showMessage('Lỗi: Không thể xóa hợp đồng này (có thể do có phụ lục liên kết).', 'warning');
    } finally {
      loading.value = false;
    }
  }
};

const viewAnnex = (id) => router.push({name: 'ContractAnnex', params: {id}});

const showMessage = (msg, type = 'success') => {
  message.value = msg;
  messageType.value = type;
  setTimeout(() => message.value = '', 4000);
};

onMounted(() => {
  fetchContracts();
  fetchPositions();
});
</script>

<template>
  <div class="management-page">
    <div class="container">

      <div class="page-header">
        <div>
          <h1 class="page-title">Hợp Đồng Nhân Sự</h1>
          <p class="page-subtitle">Quản lý và theo dõi trạng thái hợp đồng lao động</p>
        </div>
        <button class="btn-primary" @click="openCreateModal()" :disabled="loading">
          <span class="plus-icon">+</span> Thêm Hợp Đồng Mới
        </button>
      </div>

      <div v-if="loading" class="global-loader">
        Đang xử lý dữ liệu...
      </div>

      <div class="stat-cards">
        <div class="stat-card" :class="{ active: currentFilter === 'ALL' }" @click="currentFilter = 'ALL'">
          <div class="stat-icon bg-blue">📄</div>
          <div class="stat-info">
            <span class="stat-label">Tổng số hợp đồng</span>
            <h3 class="stat-value text-blue">{{ totalContracts }}</h3>
          </div>
        </div>
        <div class="stat-card" :class="{ active: currentFilter === 'ACTIVE' }" @click="currentFilter = 'ACTIVE'">
          <div class="stat-icon bg-green">✅</div>
          <div class="stat-info">
            <span class="stat-label">Đang hiệu lực</span>
            <h3 class="stat-value text-green">{{ activeContracts }}</h3>
          </div>
        </div>
        <div class="stat-card" :class="{ active: currentFilter === 'EXPIRED' }" @click="currentFilter = 'EXPIRED'">
          <div class="stat-icon bg-orange">⚠️</div>
          <div class="stat-info">
            <span class="stat-label">Đã hết hạn</span>
            <h3 class="stat-value text-orange">{{ expiredContracts }}</h3>
          </div>
        </div>
      </div>

      <transition name="fade">
        <div v-if="message" :class="['alert', messageType]">{{ message }}</div>
      </transition>

      <div class="content-panel">
        <div class="filter-panel">
          <div class="filter-row">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input v-model="searchQuery" type="text" placeholder="Tìm ID hoặc Tên nhân viên..."/>
              <button v-if="searchQuery" class="clear-input-btn" @click="searchQuery = ''">✖</button>
            </div>
            <select v-model="selectedType" class="form-select">
              <option value="ALL">Tất cả loại HĐ</option>
              <option value="YEAR_1">1 Năm</option>
              <option value="YEAR_3">3 Năm</option>
              <option value="INDEFINITE">Vô thời hạn</option>
            </select>
            <select v-model="selectedPosition" class="form-select">
              <option value="ALL">Tất cả vị trí</option>
              <option v-for="pos in availablePositions" :key="pos" :value="pos">{{ pos }}</option>
            </select>
          </div>
          <div class="filter-row mt-10">
            <div class="salary-range-box">
              <span class="range-label">Mức lương:</span>
              <input v-model="minSalary" type="number" placeholder="Từ (VNĐ)" class="form-input" min="0"/>
              <span class="separator">-</span>
              <input v-model="maxSalary" type="number" placeholder="Đến (VNĐ)" class="form-input" min="0"/>
            </div>
            <button v-if="isFilterActive" class="btn-reset" @click="clearFilters">Xóa bộ lọc</button>
          </div>
        </div>

        <div class="table-responsive">
          <table class="data-table">
            <thead>
            <tr>
              <th width="8%">ID</th>
              <th width="20%">NHÂN VIÊN</th>
              <th width="15%">VỊ TRÍ</th>
              <th width="12%">LOẠI HĐ</th>
              <th width="18%">THỜI HẠN</th>
              <th width="12%">LƯƠNG CƠ BẢN</th>
              <th width="10%">TRẠNG THÁI</th>
              <th width="5%" class="text-center"></th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="filteredContracts.length === 0">
              <td colspan="8" class="empty-state">
                <div class="empty-icon">📂</div>
                <p>Không tìm thấy hợp đồng nào.</p>
              </td>
            </tr>
            <tr v-for="c in filteredContracts" :key="c.contractId">
              <td class="text-muted">#{{ c.contractId }}</td>
              <td class="font-medium text-dark">{{ c.employee?.fullName || 'Unknown' }}</td>
              <td class="text-muted">{{ c.position }}</td>
              <td><span class="badge badge-light-blue">{{ getTypeLabel(c.type) }}</span></td>
              <td>
                <div class="date-range">
                  <span>{{ formatDate(c.startDate) }}</span>
                  <span class="date-arrow">→</span>
                  <span>{{ formatDate(c.endDate) }}</span>
                </div>
              </td>
              <td class="font-medium text-success">{{ formatCurrency(c.baseSalary) }}</td>
              <td><span :class="['badge', c.status === 'ACTIVE' ? 'badge-success' : 'badge-danger']">{{
                  c.status === 'ACTIVE' ? 'Hiệu lực' : 'Hết hạn'
                }}</span></td>
              <td class="text-center">
                <div class="action-menu">
                  <button class="icon-btn" @click="viewAnnex(c.contractId)" title="Phụ lục">👁️</button>
                  <button class="icon-btn delete" @click="deleteContract(c.contractId)" title="Xóa" :disabled="loading">
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Tạo Hợp Đồng Mới</h3>
          <button class="close-modal" @click="showModal = false">✖</button>
        </div>
        <form @submit.prevent="handleSubmit" class="contract-form">
          <div class="form-group">
            <label>ID nhân viên</label>
            <input v-model="form.employeeId" type="number" class="form-input" required
                   placeholder="Nhập ID nhân viên..." min="1"/>
          </div>

          <div class="form-row">
            <div class="form-group dropdown-container">
              <label>Vị trí</label>
              <div class="custom-combobox">
                <input
                    type="text"
                    v-model="positionSearch"
                    @focus="showPositionDropdown = true"
                    @blur="showPositionDropdown = false"
                    class="form-input"
                    placeholder="Gõ để tìm hoặc nhập vị trí..."
                    required
                />
                <span class="combo-icon">▼</span>

                <ul v-if="showPositionDropdown" class="dropdown-list">
                  <li v-if="filteredFormPositions.length === 0" class="dropdown-empty">
                    Bấm "Lưu" để thêm vị trí mới này
                  </li>
                  <li
                      v-for="pos in filteredFormPositions"
                      :key="pos.positionId || pos.id || pos"
                      @mousedown.prevent="selectPosition(pos)"
                      class="dropdown-item"
                  >
                    {{ pos.positionName || pos.name || pos }}
                  </li>
                </ul>
              </div>
            </div>

            <div class="form-group">
              <label>Loại HĐ</label>
              <select v-model="form.type" class="form-select">
                <option value="YEAR_1">1 Năm</option>
                <option value="YEAR_3">3 Năm</option>
                <option value="INDEFINITE">Vô thời hạn</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Ngày bắt đầu</label>
              <input v-model="form.startDate" type="date" class="form-input" required/>
            </div>
            <div class="form-group">
              <label>Ngày kết thúc</label>
              <input v-model="form.endDate" type="date" class="form-input" disabled/>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Lương cơ bản (VND)</label>
              <input v-model="form.baseSalary" type="number" class="form-input" required min="0"/>
            </div>
            <div class="form-group">
              <label>Trạng thái</label>
              <select v-model="form.status" class="form-select">
                <option value="ACTIVE">Hiệu lực</option>
                <option value="EXPIRED">Hết hạn</option>
              </select>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="showModal = false" :disabled="loading">Hủy</button>
            <button type="submit" class="btn-primary" :disabled="loading">
              {{ loading ? 'Đang lưu...' : 'Lưu Hợp Đồng' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* RESET & VARIABLES */
* {
  box-sizing: border-box;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.management-page {
  min-height: 100vh;
  background-color: #f4f7f8;
  padding: 30px 0;
  color: #334155;
}

.container {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 5px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.global-loader {
  background: #eff6ff;
  color: #1d4ed8;
  padding: 10px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 20px;
  font-weight: 500;
  font-size: 14px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.btn-primary {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.2);
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border-color: #3b82f6;
  ring: 2px solid #3b82f6;
  background-color: #f0fdfa;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.bg-blue {
  background: #eff6ff;
  color: #3b82f6;
}

.bg-green {
  background: #f0fdf4;
  color: #22c55e;
}

.bg-orange {
  background: #fff7ed;
  color: #f97316;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin: 4px 0 0 0;
}

.text-blue {
  color: #1e40af;
}

.text-green {
  color: #166534;
}

.text-orange {
  color: #9a3412;
}

.content-panel {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.filter-panel {
  padding: 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafafa;
}

.filter-row {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
}

.mt-10 {
  margin-top: 15px;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-box input {
  width: 100%;
  padding: 10px 35px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
}

.search-box input:focus {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 14px;
}

.clear-input-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
}

.form-select, .form-input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 14px;
  color: #334155;
  outline: none;
  background: white;
  transition: all 0.2s;
}

.form-select:focus, .form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:disabled {
  background-color: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
  border-color: #e2e8f0;
}

.salary-range-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
}

.range-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.salary-range-box .form-input {
  border: none;
  padding: 6px;
  width: 120px;
  background: transparent;
}

.salary-range-box .form-input:focus {
  box-shadow: none;
  border-bottom: 1px solid #3b82f6;
  border-radius: 0;
}

.separator {
  color: #cbd5e1;
}

.btn-reset {
  background: white;
  color: #ef4444;
  border: 1px solid #fca5a5;
  padding: 8px 15px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset:hover {
  background: #fef2f2;
}

.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: #f8fafc;
  padding: 14px 20px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #e2e8f0;
}

.data-table td {
  padding: 16px 20px;
  font-size: 14px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.data-table tbody tr:hover {
  background-color: #f8fafc;
}

.font-medium {
  font-weight: 500;
}

.text-dark {
  color: #0f172a;
}

.text-muted {
  color: #64748b;
}

.text-success {
  color: #15803d;
  font-family: 'Courier New', Courier, monospace;
  font-weight: 600;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.date-arrow {
  color: #cbd5e1;
  font-size: 12px;
}

.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.badge-success {
  background: #dcfce7;
  color: #166534;
}

.badge-danger {
  background: #fee2e2;
  color: #991b1b;
}

.badge-light-blue {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.action-menu {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: 0.2s;
}

.icon-btn:hover:not(:disabled) {
  background: #e2e8f0;
}

.icon-btn.delete:hover:not(:disabled) {
  background: #fee2e2;
}

.icon-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 40px !important;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 16px;
  width: 100%;
  max-width: 550px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.close-modal {
  background: none;
  border: none;
  font-size: 18px;
  color: #94a3b8;
  cursor: pointer;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

label {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 25px;
}

.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-weight: 500;
  font-size: 14px;
  border-left: 4px solid;
}

.success {
  background: #dcfce7;
  color: #166534;
  border-color: #22c55e;
}

.warning {
  background: #fef08a;
  color: #854d0e;
  border-color: #eab308;
}

.danger {
  background: #fee2e2;
  color: #991b1b;
  border-color: #ef4444;
}

.dropdown-container {
  position: relative;
}

.custom-combobox {
  position: relative;
  display: flex;
  align-items: center;
}

.combo-icon {
  position: absolute;
  right: 12px;
  font-size: 10px;
  color: #64748b;
  pointer-events: none;
}

.dropdown-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 100;
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  margin-top: 4px;
  padding: 5px 0;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  list-style: none;
}

.dropdown-item {
  padding: 10px 15px;
  font-size: 14px;
  color: #334155;
  cursor: pointer;
  transition: background 0.1s;
}

.dropdown-item:hover {
  background: #eff6ff;
  color: #2563eb;
  font-weight: 500;
}

.dropdown-empty {
  padding: 10px 15px;
  font-size: 13px;
  color: #94a3b8;
  font-style: italic;
  text-align: center;
}
</style>