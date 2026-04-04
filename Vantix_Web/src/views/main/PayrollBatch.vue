<script setup>
import {ref, reactive, computed, onMounted} from 'vue'
import {useToast} from 'vue-toastification'
import payrollbatchService from '@/services/payrollbatch.service'

const toast = useToast()

// 1. Khởi tạo mảng rỗng để hoàn toàn nhận dữ liệu từ Database
const batches = ref([])

const filters = reactive({year: '2026', status: ''})
const detailModal = reactive({show: false, data: {}})
const confirmModal = reactive({show: false, type: '', data: {}})
const isProcessing = ref(false)

// --- 2. GỌI API LẤY DỮ LIỆU TỪ BACKEND ---
const fetchBatches = async () => {
  try {
    const res = await payrollbatchService.getAllBatches()
    // Gán trực tiếp dữ liệu từ server trả về
    if (res.data) {
      batches.value = res.data
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách đợt lương:", error)
    toast.error("Không thể tải danh sách đợt lương từ server!")
  }
}

onMounted(() => {
  fetchBatches()
})

// --- LỌC DỮ LIỆU ---
const filteredBatches = computed(() => {
  return batches.value.filter(b => {
    // Xử lý cẩn thận nếu salaryMonth là mảng (Spring Data trả về kiểu này đôi khi)
    let salaryMonthStr = ''
    if (Array.isArray(b.salaryMonth)) {
      // Nếu là mảng [2026, 3, 1], lấy phần tử đầu tiên làm năm
      salaryMonthStr = `${b.salaryMonth[0]}`
    } else {
      salaryMonthStr = String(b.salaryMonth || '')
    }

    const matchYear = salaryMonthStr.includes(filters.year)
    const matchStatus = filters.status ? b.status === filters.status : true
    return matchYear && matchStatus
  })
})

// --- THỐNG KÊ ---
const totalPendingAmount = computed(() => {
  return batches.value
      .filter(b => b.status === 'PENDING')
      .reduce((sum, b) => sum + (b.totalNetAmount || 0), 0)
})

const totalApprovedAmount = computed(() => {
  return batches.value
      .filter(b => b.status === 'APPROVED' || b.status === 'COMPLETED')
      .reduce((sum, b) => sum + (b.totalNetAmount || 0), 0)
})

// --- XỬ LÝ HÀNH ĐỘNG ---
function openDetail(batch) {
  detailModal.data = {...batch}
  detailModal.show = true
}

function openConfirm(batch, type) {
  confirmModal.data = batch
  confirmModal.type = type
  confirmModal.show = true
}

async function handleConfirmAction() {
  isProcessing.value = true
  const actionName = confirmModal.type === 'APPROVE' ? 'Phê duyệt' : 'Từ chối'
  const newStatus = confirmModal.type === 'APPROVE' ? 'APPROVED' : 'REJECTED'

  try {
    // Gợi ý: Thay setTimeout bằng API gọi xuống Backend: await payrollbatchService.updateStatus(confirmModal.data.batchId, newStatus)
    setTimeout(() => {
      const idx = batches.value.findIndex(b => b.batchId === confirmModal.data.batchId)
      if (idx !== -1) {
        batches.value[idx].status = newStatus
      }
      toast.success(`Đã ${actionName.toLowerCase()} ${confirmModal.data.batchName} thành công!`)
      confirmModal.show = false
      isProcessing.value = false
    }, 600)

  } catch (error) {
    toast.error(`Có lỗi xảy ra khi ${actionName.toLowerCase()}!`)
    isProcessing.value = false
  }
}

// --- FORMATTERS ---
function formatCurrency(val) {
  if (val == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(val)
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  // Kiểm tra nếu Spring Boot trả về dạng mảng [yyyy, mm, dd, hh, mm, ss]
  if (Array.isArray(dateStr)) {
    const [y, m, d, h, min, s] = dateStr
    const dateObj = new Date(y, m - 1, d, h || 0, min || 0, s || 0)
    return dateObj.toLocaleString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  }

  const d = new Date(dateStr)
  return d.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function getStatusClass(status) {
  const map = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected',
    'COMPLETED': 'status-paid'
  }
  return map[status] || 'status-draft'
}

function getStatusLabel(status) {
  const map = {
    'PENDING': 'Chờ phê duyệt',
    'APPROVED': 'Đã phê duyệt',
    'REJECTED': 'Đã từ chối',
    'COMPLETED': 'Đã chi trả'
  }
  return map[status] || status
}
</script>

<template>
  <div class="salary-management">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <div class="title-icon bg-director-gradient">
          <i class="bi bi-safe"></i>
        </div>
        <div>
          <h1 class="page-title">Phê duyệt Quỹ Lương</h1>
          <p class="page-description">Giám đốc xem xét và phê duyệt đợt chi trả lương hàng tháng</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn-outline">
          <i class="bi bi-journal-arrow-down"></i>
          <span>Xuất Báo Cáo Tổng Hợp</span>
        </button>
      </div>
    </div>

    <!-- Metrics -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon pending-icon"><i class="bi bi-hourglass-split"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ formatCurrency(totalPendingAmount) }}</span>
          <span class="stat-label">Tổng tiền đang chờ duyệt</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon paid-icon"><i class="bi bi-check-circle"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ formatCurrency(totalApprovedAmount) }}</span>
          <span class="stat-label">Đã duyệt / Đã chi (Năm nay)</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-card">
      <div class="filter-content">
        <div class="select-wrapper">
          <select v-model="filters.year" class="filter-select">
            <option value="2026">Năm 2026</option>
            <option value="2025">Năm 2025</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>

        <div class="select-wrapper">
          <select v-model="filters.status" class="filter-select">
            <option value="">Tất cả Trạng thái</option>
            <option value="PENDING">Chờ phê duyệt</option>
            <option value="APPROVED">Đã phê duyệt</option>
            <option value="REJECTED">Đã từ chối</option>
            <option value="COMPLETED">Đã chi trả</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>
      </div>
    </div>

    <!-- CARD GRID MỚI (Lọc trực tiếp, bỏ phân trang) -->
    <div v-if="filteredBatches.length === 0" class="empty-state-grid">
      <i class="bi bi-folder-x empty-icon"></i>
      <p>Không có đợt lương nào trong hệ thống.</p>
    </div>

    <div v-else class="batch-grid">
      <div v-for="batch in filteredBatches" :key="batch.batchId" class="batch-card">
        <div class="card-header-top">
          <span class="batch-id">#BATCH-{{ batch.batchId }}</span>
          <span :class="['status-badge', getStatusClass(batch.status)]">
            <span class="status-dot"></span>
            {{ getStatusLabel(batch.status) }}
          </span>
        </div>

        <div class="card-main-body">
          <h3 class="batch-name">{{ batch.batchName }}</h3>

          <div class="batch-meta">
            <div class="meta-item">
              <i class="bi bi-calendar3 text-muted"></i>
              <span>{{ formatDateTime(batch.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <i class="bi bi-people text-muted"></i>
              <span>{{ batch.totalEmployees }} nhân sự</span>
            </div>
          </div>

          <div class="batch-amount-box">
            <span class="amount-label">TỔNG QUỸ LƯƠNG</span>
            <span class="amount-value" :class="batch.status === 'REJECTED' ? 'text-danger' : 'text-success'">
              {{ formatCurrency(batch.totalNetAmount) }}
            </span>
          </div>
        </div>

        <div class="card-footer-bottom">
          <button class="btn-card-action view" @click="openDetail(batch)">
            <i class="bi bi-eye"></i> Xem chi tiết
          </button>

          <div class="quick-actions" v-if="batch.status === 'PENDING'">
            <button class="btn-icon-action reject" @click="openConfirm(batch, 'REJECT')" title="Từ chối">
              <i class="bi bi-x-lg"></i>
            </button>
            <button class="btn-icon-action approve" @click="openConfirm(batch, 'APPROVE')" title="Phê duyệt">
              <i class="bi bi-check-lg"></i> Duyệt
            </button>
          </div>
        </div>
      </div>
    </div>

    <teleport to="body">
      <!-- Modal Xem Chi tiết Đợt Lương -->
      <transition name="modal-fade">
        <div v-if="detailModal.show" class="modal-overlay" @click.self="detailModal.show = false">
          <div class="modal-container" style="max-width: 850px;">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon bg-director-light" style="color: #4f46e5;">
                  <i class="bi bi-file-earmark-ruled"></i>
                </div>
                <div>
                  <h3>Báo cáo {{ detailModal.data.batchName }}</h3>
                  <p>Trạng thái: <strong>{{ getStatusLabel(detailModal.data.status) }}</strong></p>
                </div>
              </div>
              <button class="modal-close" @click="detailModal.show = false"><i class="bi bi-x-lg"></i></button>
            </div>

            <div class="modal-body payslip-body">
              <!-- Summary Box -->
              <div class="payslip-net-box mb-4" style="background: linear-gradient(135deg, #1e1b4b, #312e81);">
                <div class="net-left">
                  <span style="color: #c7d2fe;">TỔNG QUỸ LƯƠNG CẦN CHI</span>
                  <div class="mt-1" style="color: white; font-size: 14px;">
                    <i class="bi bi-people"></i> Số lượng: <strong>{{ detailModal.data.totalEmployees }} nhân
                    sự</strong>
                  </div>
                </div>
                <div class="net-right" style="color: #34d399;">
                  {{ formatCurrency(detailModal.data.totalNetAmount) }}
                </div>
              </div>

              <!-- Fake Table for Details -->
              <h4 style="font-size: 14px; font-weight: 700; color: #334155; margin-bottom: 12px;">CHI TIẾT NHÂN SỰ</h4>
              <div style="border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;">
                <table class="data-table" style="margin: 0; width: 100%; border-collapse: collapse;">
                  <thead style="background: #f8fafc; border-bottom: 1px solid #e2e8f0;">
                  <tr>
                    <th style="padding: 10px 16px; text-align: left; font-size: 12px; color: #64748b;">Nhân viên</th>
                    <th style="padding: 10px 16px; text-align: left; font-size: 12px; color: #64748b;">Phòng ban</th>
                    <th style="padding: 10px 16px; text-align: right; font-size: 12px; color: #64748b;">Thực nhận
                      (Net)
                    </th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr style="border-bottom: 1px solid #f1f5f9;">
                    <td style="padding: 10px 16px; font-size: 13.5px; color: #0f172a; font-weight: 500;">Nguyễn Văn
                      Bảo
                    </td>
                    <td style="padding: 10px 16px;"><span class="badge-light">Phòng Kỹ Thuật</span></td>
                    <td style="padding: 10px 16px; text-align: right; font-weight: 600; color: #059669;">14.625.000 ₫
                    </td>
                  </tr>
                  <tr style="border-bottom: 1px solid #f1f5f9;">
                    <td style="padding: 10px 16px; font-size: 13.5px; color: #0f172a; font-weight: 500;">Trần Thị Hà
                    </td>
                    <td style="padding: 10px 16px;"><span class="badge-light">Phòng Nhân Sự</span></td>
                    <td style="padding: 10px 16px; text-align: right; font-weight: 600; color: #059669;">10.149.090 ₫
                    </td>
                  </tr>
                  <tr>
                    <td colspan="3"
                        style="text-align: center; padding: 12px; color: #64748b; font-style: italic; font-size: 12px;">
                      Đang hiển thị 2 / {{ detailModal.data.totalEmployees }} nhân sự... (Tích hợp API lấy chi tiết sau)
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="modal-footer justify-content-between">
              <div>
                <button class="btn-outline"><i class="bi bi-printer"></i> In Báo Cáo</button>
              </div>
              <div class="d-flex gap-2">
                <button class="btn-secondary" @click="detailModal.show = false">Đóng</button>
                <button v-if="detailModal.data.status === 'PENDING'" class="btn-primary"
                        style="background: #ef4444; border: none;" @click="openConfirm(detailModal.data, 'REJECT')">
                  Từ chối
                </button>
                <button v-if="detailModal.data.status === 'PENDING'" class="btn-primary"
                        style="background: #10b981; border: none;" @click="openConfirm(detailModal.data, 'APPROVE')">
                  Phê duyệt Quỹ lương
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- Modal Xác nhận Phê duyệt/Từ chối -->
      <transition name="modal-fade">
        <div v-if="confirmModal.show" class="modal-overlay" @click.self="!isProcessing && (confirmModal.show = false)">
          <div class="modal-container" style="max-width: 420px;">
            <div class="modal-header" style="border-bottom: none; padding-bottom: 0;">
              <div class="modal-title-group">
                <div class="modal-icon"
                     :style="{ background: confirmModal.type === 'APPROVE' ? '#d1fae5' : '#fee2e2', color: confirmModal.type === 'APPROVE' ? '#059669' : '#dc2626' }">
                  <i :class="confirmModal.type === 'APPROVE' ? 'bi bi-check-circle' : 'bi bi-x-circle'"></i>
                </div>
                <div>
                  <h3 style="font-size: 18px;">
                    {{ confirmModal.type === 'APPROVE' ? 'Xác nhận Phê duyệt' : 'Xác nhận Từ chối' }}</h3>
                </div>
              </div>
              <button class="modal-close" @click="!isProcessing && (confirmModal.show = false)"><i
                  class="bi bi-x-lg"></i></button>
            </div>

            <div class="modal-body" style="background: white; padding-top: 16px;">
              <p v-if="confirmModal.type === 'APPROVE'"
                 style="color: #475569; font-size: 14.5px; line-height: 1.5; margin: 0;">
                Giám đốc đồng ý phê duyệt chi trả <strong
                  class="text-success">{{ formatCurrency(confirmModal.data.totalNetAmount) }}</strong> cho đợt <strong>{{
                  confirmModal.data.batchName
                }}</strong>? Trạng thái sẽ chuyển cho Kế toán giải ngân.
              </p>
              <p v-else style="color: #475569; font-size: 14.5px; line-height: 1.5; margin: 0;">
                Giám đốc từ chối đợt <strong>{{ confirmModal.data.batchName }}</strong>? Bảng lương sẽ được trả về trạng
                thái yêu cầu tính toán lại.
              </p>
            </div>

            <div class="modal-footer" style="justify-content: flex-end; gap: 10px; border-top: none; padding-top: 0;">
              <button class="btn-secondary" @click="confirmModal.show = false" :disabled="isProcessing">Hủy bỏ</button>
              <button class="btn-primary"
                      :style="{ background: confirmModal.type === 'APPROVE' ? '#10b981' : '#ef4444', border: 'none' }"
                      @click="handleConfirmAction" :disabled="isProcessing">
                <span v-if="isProcessing" class="spinner-border spinner-border-sm me-2"></span>
                {{ confirmModal.type === 'APPROVE' ? 'Đồng ý Phê duyệt' : 'Xác nhận Từ chối' }}
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
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.salary-management {
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

.bg-director-gradient {
  background: linear-gradient(135deg, #4f46e5, #3730a3) !important;
  color: white;
  border: none;
}

.bg-director-light {
  background: #eef2ff;
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
  line-height: 1.2;
  margin-bottom: 2px;
}

.page-description {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.text-success {
  color: #059669 !important;
}

.text-danger {
  color: #dc2626 !important;
}

.mb-4 {
  margin-bottom: 24px;
}

.me-1 {
  margin-right: 4px;
}

.me-2 {
  margin-right: 8px;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  flex: 1;
  min-width: 200px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.pending-icon {
  background: #fffbeb;
  color: #d97706;
}

.paid-icon {
  background: #d1fae5;
  color: #059669;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 3px;
  font-weight: 500;
}

.filter-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e8edff;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.filter-content {
  padding: 14px 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.select-wrapper {
  position: relative;
  min-width: 160px;
}

.filter-select {
  width: 100%;
  padding: 9px 32px 9px 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  background: #fafbff;
  cursor: pointer;
  appearance: none;
  font-family: inherit;
  color: #334155;
  font-weight: 500;
}

.select-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 12px;
  pointer-events: none;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
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
  transition: all 0.2s ease;
  font-family: inherit;
}

.btn-outline:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
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
  font-family: inherit;
  border: none;
}

/* GRID CARD - GIAO DIỆN LƯỚI */
.batch-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.empty-state-grid {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  border: 1px dashed #e2e8f0;
}

.empty-icon {
  font-size: 40px;
  color: #cbd5e1;
  display: block;
  margin-bottom: 12px;
}

.empty-state-grid p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.batch-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e8edff;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
}

.batch-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: #c7d2fe;
}

.card-header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-id {
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.card-main-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.batch-name {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.batch-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

.meta-item i {
  font-size: 14px;
  width: 16px;
  text-align: center;
}

.batch-amount-box {
  background: #f8fafc;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 20px;
  border: 1px solid #f1f5f9;
}

.amount-label {
  font-size: 11.5px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.5px;
}

.amount-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 22px;
  font-weight: 800;
}

.card-footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1.5px dashed #f1f5f9;
}

.btn-card-action {
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-card-action:hover {
  background: #f1f5f9;
  color: #4f46e5;
}

.quick-actions {
  display: flex;
  gap: 8px;
}

.btn-icon-action {
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-icon-action.approve {
  background: #d1fae5;
  color: #059669;
}

.btn-icon-action.approve:hover {
  background: #10b981;
  color: white;
  transform: translateY(-2px);
}

.btn-icon-action.reject {
  background: #fee2e2;
  color: #dc2626;
  padding: 6px 10px;
}

.btn-icon-action.reject:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-2px);
}

/* Badges */
.badge-light {
  padding: 4px 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-pending {
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fde68a;
}

.status-pending .status-dot {
  background: #f59e0b;
}

.status-approved {
  background: #eff6ff;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.status-approved .status-dot {
  background: #3b82f6;
}

.status-paid {
  background: #d1fae5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.status-paid .status-dot {
  background: #10b981;
}

.status-rejected {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.status-rejected .status-dot {
  background: #ef4444;
}

/* Modal Styles */
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
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  overflow: hidden;
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

.modal-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
  margin: 0;
}

.modal-header p {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
  margin-bottom: 0;
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
  transition: all 0.2s ease;
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
  padding: 24px;
  background: #fafbff;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1.5px solid #f1f5f9;
  display: flex;
  align-items: center;
  background: white;
}

.payslip-net-box {
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 25px rgba(30, 27, 75, 0.15);
}

.net-left span {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.net-right {
  font-size: 28px;
  font-weight: 800;
  font-family: 'JetBrains Mono', monospace;
}

.d-flex {
  display: flex;
}

.gap-2 {
  gap: 8px;
}

.justify-content-between {
  justify-content: space-between;
}

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
  .batch-grid {
    grid-template-columns: 1fr;
  }

  .payslip-net-box {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .modal-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .d-flex.gap-2 {
    flex-direction: column;
  }
}
</style>