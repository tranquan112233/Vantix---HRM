<script setup>
import {ref, onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router';

const route = useRoute();
const router = useRouter();
const contractId = route.params.id; // Lấy ID hợp đồng từ URL

// --- STATE ---
const annexes = ref([]);
const showModal = ref(false);
const loading = ref(false);
const message = ref(''); // State thông báo
const messageType = ref('success');

// Giả lập thông tin hợp đồng gốc
const contractInfo = ref({
  contractId: contractId,
  employeeName: 'Đang tải...',
  currentPosition: '...',
  currentSalary: 0
});

// Form Model
const form = ref({
  effectiveDate: '',
  newSalary: 0,
  newPositions: '',
  content: '',
  isActive: true
});

// --- HELPER FORMATTERS ---
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(val);
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString('vi-VN') : '-';

// --- METHODS ---
const fetchAnnexes = () => {
  loading.value = true;
  // Giả lập API
  setTimeout(() => {
    if (contractId == '101') {
      contractInfo.value = {
        contractId: 101,
        employeeName: 'Nguyễn Văn A',
        currentPosition: 'Backend Dev',
        currentSalary: 25000000
      };
      annexes.value = [
        {
          annexId: 1,
          effectiveDate: '2023-06-01',
          newSalary: 27000000,
          newPositions: 'Team Lead',
          content: 'Tăng lương định kỳ',
          isActive: true
        }
      ];
    } else {
      contractInfo.value = {
        contractId: contractId,
        employeeName: 'Nhân viên Demo',
        currentPosition: 'Staff',
        currentSalary: 10000000
      };
      annexes.value = [];
    }
    loading.value = false;
  }, 500);
};

const handleSubmit = () => {
  const newAnnex = {
    ...form.value,
    annexId: Math.floor(Math.random() * 10000)
  };
  annexes.value.unshift(newAnnex);
  showModal.value = false;
  form.value = {effectiveDate: '', newSalary: 0, newPositions: '', content: '', isActive: true};
  showMessage('Đã thêm phụ lục thành công!', 'success');
};

// --- HÀM XÓA PHỤ LỤC (MỚI THÊM) ---
const deleteAnnex = (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa phụ lục này không?')) {
    annexes.value = annexes.value.filter(a => a.annexId !== id);
    showMessage('Đã xóa phụ lục.', 'warning');
  }
};

const showMessage = (msg, type = 'success') => {
  message.value = msg;
  messageType.value = type;
  setTimeout(() => message.value = '', 3000);
};

const goBack = () => {
  router.push({name: 'Contract'});
};

onMounted(() => {
  fetchAnnexes();
});
</script>

<template>
  <div class="annex-page">
    <div class="container">

      <div class="header-section">
        <button class="btn-back" @click="goBack">⬅ Quay lại</button>
        <div class="contract-info-card">
          <h2>Hợp đồng #{{ contractInfo.contractId }} - {{ contractInfo.employeeName }}</h2>
          <div class="info-badges">
            <span class="badge position">Chức vụ hiện tại: {{ contractInfo.currentPosition }}</span>
            <span class="badge salary">Lương hiện tại: {{ formatCurrency(contractInfo.currentSalary) }}</span>
          </div>
        </div>
      </div>

      <transition name="fade">
        <div v-if="message" :class="['alert', messageType]">
          {{ message }}
        </div>
      </transition>

      <div class="table-container">
        <div class="table-header">
          <h3>Danh sách Phụ lục (Contract Annexes)</h3>
          <button class="btn-primary" @click="showModal = true">
            + Thêm Phụ Lục
          </button>
        </div>

        <table>
          <thead>
          <tr>
            <th>ID</th>
            <th>Ngày Hiệu Lực</th>
            <th>Lương Mới</th>
            <th>Chức Vụ Mới</th>
            <th>Nội Dung Thay Đổi</th>
            <th>Trạng Thái</th>
            <th>Thao Tác</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="annexes.length === 0">
            <td colspan="7" class="empty-cell">Chưa có phụ lục nào cho hợp đồng này.</td>
          </tr>
          <tr v-for="a in annexes" :key="a.annexId">
            <td>#{{ a.annexId }}</td>
            <td>{{ formatDate(a.effectiveDate) }}</td>
            <td class="salary-text">{{ a.newSalary ? formatCurrency(a.newSalary) : '-' }}</td>
            <td>{{ a.newPositions || '-' }}</td>
            <td>{{ a.content }}</td>
            <td>
                <span :class="['status-badge', a.isActive ? 'active' : 'inactive']">
                  {{ a.isActive ? 'Áp dụng' : 'Hủy' }}
                </span>
            </td>
            <td>
              <button class="btn-icon delete" @click="deleteAnnex(a.annexId)" title="Xóa phụ lục">
                🗑️
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Thêm Phụ Lục Mới</h3>
        <p class="modal-subtitle">Cập nhật thay đổi cho Hợp đồng #{{ contractId }}</p>

        <form @submit.prevent="handleSubmit">
          <div class="form-row">
            <div class="form-group">
              <label>Ngày hiệu lực</label>
              <input v-model="form.effectiveDate" type="date" required/>
            </div>
            <div class="form-group">
              <label>Trạng thái</label>
              <select v-model="form.isActive">
                <option :value="true">Áp dụng ngay</option>
                <option :value="false">Chờ duyệt</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Mức lương mới (VND)</label>
              <input v-model="form.newSalary" type="number" placeholder="Bỏ trống nếu không đổi"/>
            </div>
            <div class="form-group">
              <label>Chức vụ mới</label>
              <input v-model="form.newPositions" type="text" placeholder="Bỏ trống nếu không đổi"/>
            </div>
          </div>

          <div class="form-group">
            <label>Nội dung / Lý do</label>
            <textarea v-model="form.content" rows="3" placeholder="Ví dụ: Tăng lương định kỳ năm 2026..."
                      required></textarea>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="showModal = false">Hủy</button>
            <button type="submit" class="btn-confirm">Lưu Phụ Lục</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.annex-page {
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

/* Tăng chiều rộng một chút cho thoải mái */

/* HEADER STYLES */
.header-section {
  margin-bottom: 25px;
}

.btn-back {
  background: none;
  border: none;
  color: #546e7a;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 10px;
  font-weight: 600;
}

.btn-back:hover {
  color: #1976d2;
  text-decoration: underline;
}

.contract-info-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left: 5px solid #ff9800;
}

.contract-info-card h2 {
  margin: 0 0 10px 0;
  color: #e65100;
  font-size: 20px;
}

.info-badges {
  display: flex;
  gap: 10px;
}

.badge {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.badge.position {
  background: #fff3e0;
  color: #ef6c00;
}

.badge.salary {
  background: #e8f5e9;
  color: #2e7d32;
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

.salary-text {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #2e7d32;
}

/* BUTTON STYLES (NEW) */
.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-icon.delete:hover {
  background: #ffebee;
  transform: scale(1.1);
}

/* MODAL STYLES */
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
  max-width: 550px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  animation: popIn 0.3s;
}

.modal-subtitle {
  color: #78909c;
  font-size: 14px;
  margin-bottom: 20px;
  margin-top: -10px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
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

input, select, textarea {
  padding: 10px;
  border: 1px solid #cfd8dc;
  border-radius: 8px;
  outline: none;
  font-family: inherit;
}

input:focus, textarea:focus {
  border-color: #2196f3;
  box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.1);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-confirm {
  background: #ff9800;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.btn-confirm:hover {
  background: #f57c00;
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

.btn-primary {
  background: #2196f3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
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

.status-badge.active {
  color: #2e7d32;
  background: #e8f5e9;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.inactive {
  color: #c62828;
  background: #ffebee;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
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