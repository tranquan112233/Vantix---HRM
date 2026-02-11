<script setup>
import {ref, computed, onMounted} from 'vue';
import {useRouter} from 'vue-router'; // Import router để điều hướng

const router = useRouter();

// --- GIẢ LẬP SERVICE ---
const mockContracts = [
  {
    contractId: 101,
    employee: {id: 1, name: 'Nguyễn Văn A'},
    type: 'INDEFINITE',
    startDate: '2023-01-01',
    endDate: null,
    position: 'Backend Developer',
    baseSalary: 25000000,
    status: 'ACTIVE'
  },
  {
    contractId: 102,
    employee: {id: 2, name: 'Trần Thị B'},
    type: 'YEAR_1',
    startDate: '2024-01-01',
    endDate: '2025-01-01',
    position: 'HR Specialist',
    baseSalary: 15000000,
    status: 'ACTIVE'
  },
  {
    contractId: 103,
    employee: {id: 3, name: 'Lê Văn C'},
    type: 'YEAR_1',
    startDate: '2022-06-01',
    endDate: '2023-06-01',
    position: 'Intern',
    baseSalary: 5000000,
    status: 'EXPIRED'
  }
];

// --- STATE ---
const contracts = ref([]);
const loading = ref(false);
const showModal = ref(false);
const message = ref('');
const messageType = ref('success');

// Form Model (Chỉ dùng để tạo mới)
const form = ref({
  contractId: null,
  employeeName: '',
  type: 'YEAR_1',
  startDate: '',
  endDate: '',
  position: '',
  baseSalary: 0,
  status: 'ACTIVE'
});

// --- HELPER FORMATTERS ---
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(value);
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'Vô thời hạn';
  const date = new Date(dateStr);
  return date.toLocaleDateString('vi-VN');
};

const getTypeLabel = (type) => {
  const map = {
    'YEAR_1': '1 Năm',
    'YEAR_3': '3 Năm',
    'INDEFINITE': 'Vô thời hạn'
  };
  return map[type] || type;
};

// --- COMPUTED STATS ---
const totalContracts = computed(() => contracts.value.length);
const activeContracts = computed(() => contracts.value.filter(c => c.status === 'ACTIVE').length);
const expiredContracts = computed(() => contracts.value.filter(c => c.status === 'EXPIRED').length);

// --- METHODS ---
const fetchContracts = async () => {
  loading.value = true;
  setTimeout(() => {
    contracts.value = mockContracts;
    loading.value = false;
  }, 500);
};

// Chỉ mở Modal để thêm mới (Không truyền tham số contract vào nữa)
const openCreateModal = () => {
  form.value = {
    contractId: null,
    employeeName: '',
    type: 'YEAR_1',
    startDate: '',
    endDate: '',
    position: '',
    baseSalary: 0,
    status: 'ACTIVE'
  };
  showModal.value = true;
};

// Xử lý tạo mới hợp đồng
const handleSubmit = () => {
  // Logic thêm mới (Giả lập)
  const newContract = {
    ...form.value,
    contractId: Math.floor(Math.random() * 1000) + 1000, // ID ngẫu nhiên
    employee: {name: form.value.employeeName}
  };

  contracts.value.unshift(newContract); // Thêm lên đầu danh sách
  showMessage('Tạo hợp đồng mới thành công!', 'success');
  showModal.value = false;
};

// Chuyển hướng sang trang Phụ lục
const viewAnnex = (contractId) => {
  // Điều hướng đến route: /contract/:id/annex
  router.push({name: 'ContractAnnex', params: {id: contractId}});
};

const deleteContract = (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa hợp đồng này? Hành động này không thể hoàn tác.')) {
    contracts.value = contracts.value.filter(c => c.contractId !== id);
    showMessage('Đã xóa hợp đồng.', 'warning');
  }
};

const showMessage = (msg, type = 'success') => {
  message.value = msg;
  messageType.value = type;
  setTimeout(() => message.value = '', 3000);
};

onMounted(() => fetchContracts());
</script>

<template>
  <div class="management-page">
    <div class="container">
      <h1 class="page-title">Quản Lý Hợp Đồng Nhân Sự</h1>

      <div class="card-grid">
        <div class="card">
          <div class="icon">📄</div>
          <h3>{{ totalContracts }}</h3>
          <p>Tổng số hợp đồng</p>
        </div>

        <div class="card">
          <div class="icon">✅</div>
          <h3>{{ activeContracts }}</h3>
          <p>Đang hiệu lực</p>
        </div>

        <div class="card">
          <div class="icon">⚠️</div>
          <h3>{{ expiredContracts }}</h3>
          <p>Đã hết hạn</p>
        </div>
      </div>

      <transition name="fade">
        <div v-if="message" :class="['alert', messageType]">
          {{ message }}
        </div>
      </transition>

      <div class="table-container">
        <div class="table-header">
          <h3>Danh sách hợp đồng</h3>
          <button class="btn-primary" @click="openCreateModal()">
            + Thêm Mới
          </button>
        </div>

        <table>
          <thead>
          <tr>
            <th>ID</th>
            <th>Nhân Viên</th>
            <th>Vị Trí</th>
            <th>Loại HĐ</th>
            <th>Ngày Hiệu Lực</th>
            <th>Lương Cơ Bản</th>
            <th>Trạng Thái</th>
            <th>Thao Tác</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="contracts.length === 0">
            <td colspan="8" class="empty-cell">Chưa có dữ liệu hợp đồng.</td>
          </tr>
          <tr v-for="c in contracts" :key="c.contractId">
            <td>#{{ c.contractId }}</td>
            <td class="font-bold">{{ c.employee?.name || 'Unknown' }}</td>
            <td>{{ c.position }}</td>
            <td>
              <span class="type-badge">{{ getTypeLabel(c.type) }}</span>
            </td>
            <td>
              {{ formatDate(c.startDate) }} <br>
              <span class="sub-text">đến {{ formatDate(c.endDate) }}</span>
            </td>
            <td class="salary-text">{{ formatCurrency(c.baseSalary) }}</td>
            <td>
                <span :class="['status-badge', c.status]">
                  {{ c.status === 'ACTIVE' ? 'Hiệu lực' : 'Hết hạn' }}
                </span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="btn-icon view" @click="viewAnnex(c.contractId)" title="Xem Phụ lục">
                  👁️
                </button>

                <button class="btn-icon delete" @click="deleteContract(c.contractId)" title="Xóa Hợp đồng">
                  🗑️
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Tạo Hợp Đồng Mới</h3>
        <p class="modal-subtitle">Lưu ý: Hợp đồng sau khi tạo chỉ có thể thêm Phụ lục, không thể sửa trực tiếp.</p>

        <form @submit.prevent="handleSubmit" class="contract-form">
          <div class="form-group">
            <label>Tên nhân viên</label>
            <input v-model="form.employeeName" type="text" required placeholder="Nhập tên nhân viên..."/>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Vị trí</label>
              <input v-model="form.position" type="text" required placeholder="Vd: Backend Dev"/>
            </div>
            <div class="form-group">
              <label>Loại HĐ</label>
              <select v-model="form.type">
                <option value="YEAR_1">1 Năm</option>
                <option value="YEAR_3">3 Năm</option>
                <option value="INDEFINITE">Vô thời hạn</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Ngày bắt đầu</label>
              <input v-model="form.startDate" type="date" required/>
            </div>
            <div class="form-group">
              <label>Ngày kết thúc</label>
              <input v-model="form.endDate" type="date" :disabled="form.type === 'INDEFINITE'"/>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Lương cơ bản (VND)</label>
              <input v-model="form.baseSalary" type="number" min="0" required/>
            </div>
            <div class="form-group">
              <label>Trạng thái</label>
              <select v-model="form.status">
                <option value="ACTIVE">Hiệu lực</option>
                <option value="EXPIRED">Hết hạn</option>
              </select>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="showModal = false">Hủy</button>
            <button type="submit" class="btn-confirm">Lưu Hợp Đồng</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* BACKGROUND & LAYOUT */
.management-page {
  min-height: 100vh;
  padding: 40px 0;
  background: linear-gradient(135deg, #f0f7ff, #ffffff);
  display: flex;
  justify-content: center;
}

.container {
  width: 90%;
  max-width: 1000px;
}

.page-title {
  text-align: center;
  color: #1a237e;
  margin-bottom: 30px;
  font-weight: 700;
}

/* CARDS */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 25px;
  text-align: center;
  border: 1px solid #e3f2fd;
  box-shadow: 0 8px 20px rgba(33, 150, 243, 0.05);
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-5px);
  border-color: #2196f3;
}

.icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.card h3 {
  color: #0d47a1;
  font-size: 24px;
  margin: 5px 0;
}

.card p {
  color: #78909c;
  font-size: 14px;
}

/* TABLE STYLES */
.table-container {
  background: white;
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.03);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-left: 4px solid #2196f3;
  padding-left: 15px;
}

.btn-primary {
  background: #2196f3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #1976d2;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #f8fbff;
  color: #546e7a;
  padding: 15px;
  text-align: left;
  font-size: 13px;
  text-transform: uppercase;
}

td {
  padding: 15px;
  border-bottom: 1px solid #f1f1f1;
  color: #37474f;
  font-size: 14px;
  vertical-align: middle;
}

.font-bold {
  font-weight: 600;
  color: #1565c0;
}

.sub-text {
  font-size: 12px;
  color: #90a4ae;
}

.salary-text {
  font-family: 'Courier New', Courier, monospace;
  font-weight: 600;
  color: #2e7d32;
}

/* BADGES */
.status-badge {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.ACTIVE {
  background: #e8f5e9;
  color: #2e7d32;
}

.EXPIRED {
  background: #ffebee;
  color: #c62828;
}

.type-badge {
  background: #e3f2fd;
  color: #0277bd;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

/* ACTION BUTTONS */
.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px; /* Tăng size icon một chút cho dễ bấm */
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s;
}

/* Style riêng cho nút View Annex */
.btn-icon.view:hover {
  background: #e3f2fd;
  transform: scale(1.1);
}

.btn-icon.delete:hover {
  background: #ffebee;
  transform: scale(1.1);
}

/* MODAL & FORM */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(4px);
  z-index: 999;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.modal-subtitle {
  font-size: 13px;
  color: #ef6c00; /* Màu cam cảnh báo */
  margin-top: -10px;
  margin-bottom: 20px;
  font-style: italic;
}

.contract-form {
  margin-top: 10px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

label {
  font-size: 13px;
  color: #546e7a;
  margin-bottom: 5px;
  font-weight: 600;
}

input, select {
  padding: 10px;
  border: 1px solid #cfd8dc;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

input:focus, select:focus {
  border-color: #2196f3;
  box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.1);
}

.modal-actions {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-confirm {
  background: #2196f3;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.btn-cancel {
  background: #f5f5f5;
  color: #616161;
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

/* ALERTS */
.alert {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
  font-weight: 600;
}

.success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.warning {
  background: #fff3e0;
  color: #ef6c00;
  border: 1px solid #ffe0b2;
}

@keyframes popIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>