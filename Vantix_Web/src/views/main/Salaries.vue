<script setup>
import {ref, reactive, computed, onMounted, watch} from 'vue'
import salariesService from '@/services/salaries.service'
import {useToast} from 'vue-toastification'

const toast = useToast()

const monthInput = ref(null)
const departments = ref([])
const salaries = ref([])

const filters = reactive({keyword: '', status: '', month: '2026-03', department: ''})
const detailModal = reactive({show: false, data: {}})

// State UI và Phân trang
const pagination = reactive({page: 0, size: 10})

// State cho Modal Gửi Duyệt Tất Cả
const showConfirmModal = ref(false)
const isSubmittingAll = ref(false)

// State cho Modal Chốt Lương Tháng
const showFinalizeModal = ref(false)
const isFinalizing = ref(false)

// State cho Modal Tính Lương Tự Động (MỚI)
const showGenerateModal = ref(false)
const isGenerating = ref(false)

const fetchSalaries = async () => {
  if (!filters.month) return
  const [year, month] = filters.month.split('-')
  try {
    const res = await salariesService.getSalaries(parseInt(month), parseInt(year))
    if (res.data) {
      salaries.value = res.data
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách bảng lương", error)
    salaries.value = []
  }
}

onMounted(async () => {
  try {
    const res = await salariesService.getDepartmentNames()
    if (res.data) {
      departments.value = res.data
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách phòng ban", error)
  }
  await fetchSalaries()
})

watch(() => filters.month, () => {
  fetchSalaries()
})

const filteredSalaries = computed(() => {
  return salaries.value.filter(s => {
    const empName = s.employeeName || ''
    const empId = s.employeeId ? s.employeeId.toString() : ''

    const matchName = empName.toLowerCase().includes(filters.keyword.toLowerCase()) || empId.includes(filters.keyword)
    const matchStatus = filters.status ? s.status === filters.status : true
    const matchDept = filters.department ? s.department === filters.department : true

    return matchName && matchStatus && matchDept
  })
})

watch([() => filters.keyword, () => filters.status, () => filters.department, () => filters.month], () => {
  pagination.page = 0
})

const totalElements = computed(() => filteredSalaries.value.length)
const totalPages = computed(() => Math.ceil(totalElements.value / pagination.size))

const paginatedSalaries = computed(() => {
  const start = pagination.page * pagination.size
  const end = start + pagination.size
  return filteredSalaries.value.slice(start, end)
})

const startEntry = computed(() => totalElements.value === 0 ? 0 : pagination.page * pagination.size + 1)
const endEntry = computed(() => Math.min((pagination.page + 1) * pagination.size, totalElements.value))

const visiblePages = computed(() => {
  const delta = 2, pages = []
  const start = Math.max(0, pagination.page - delta)
  const end = Math.min(totalPages.value - 1, pagination.page + delta)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function goPage(page) {
  if (page >= 0 && page < totalPages.value) {
    pagination.page = page
  }
}

function onSizeChange() {
  pagination.page = 0
}

const totalPayrollCost = computed(() => filteredSalaries.value.reduce((sum, s) => sum + (s.netSalary || 0), 0))
const pendingCount = computed(() => filteredSalaries.value.filter(s => s.status === 'PENDING').length)
const paidCount = computed(() => filteredSalaries.value.filter(s => s.status === 'PAID').length)

// --- XỬ LÝ: GỬI DUYỆT TẤT CẢ ---
function handleSubmitAllPending() {
  if (!filters.month) return
  showConfirmModal.value = true
}

async function confirmSubmitAllPending() {
  showConfirmModal.value = false
  isSubmittingAll.value = true
  const [year, month] = filters.month.split('-')

  try {
    const res = await salariesService.submitAllToPending(parseInt(month), parseInt(year))
    toast.success(res.data)
    await fetchSalaries()
  } catch (error) {
    const errorMsg = error.response?.data || "Có lỗi xảy ra khi cập nhật trạng thái!"
    toast.error(errorMsg)
  } finally {
    isSubmittingAll.value = false
  }
}

// --- XỬ LÝ: TÍNH LƯƠNG TỰ ĐỘNG (MỚI) ---
function openAutoGenerateModal() {
  if (!filters.month) return
  showGenerateModal.value = true
}

async function confirmAutoGenerateSalaries() {
  if (!filters.month) return

  showGenerateModal.value = false
  isGenerating.value = true
  const [year, month] = filters.month.split('-')

  try {
    const res = await salariesService.generateSalaries(parseInt(month), parseInt(year))
    toast.success(res.data)
    await fetchSalaries() // Load lại bảng ngay sau khi tính xong
  } catch (error) {
    toast.error(error.response?.data || "Có lỗi xảy ra khi chạy thuật toán tính lương!")
  } finally {
    isGenerating.value = false
  }
}

// --- XỬ LÝ: CHỐT LƯƠNG THÁNG ---
function openFinalizeModal() {
  if (!filters.month) return
  showFinalizeModal.value = true
}

async function confirmFinalizePayroll() {
  if (!filters.month) return

  showFinalizeModal.value = false
  isFinalizing.value = true
  const [year, month] = filters.month.split('-')

  try {
    const res = await salariesService.finalizePayrollBatch(parseInt(month), parseInt(year))
    toast.success(`Đã chốt thành công: ${res.data.batchName} với tổng chi phí ${formatCurrency(res.data.totalNetAmount)}`)
  } catch (error) {
    toast.error(error.response?.data || "Có lỗi xảy ra khi chốt lương!")
  } finally {
    isFinalizing.value = false
  }
}

// --- TIỆN ÍCH ---
function openMonthPicker() {
  if (monthInput.value && typeof monthInput.value.showPicker === 'function') {
    try {
      monthInput.value.showPicker();
    } catch (e) {
    }
  }
}

function formatCurrency(val) {
  if (val == null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(val)
}

function formatMonth(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `Tháng ${d.getMonth() + 1}/${d.getFullYear()}`
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : '?'
}

function getStatusClass(status) {
  const map = {
    'DRAFT': 'status-draft', 'PENDING': 'status-pending', 'APPROVED': 'status-approved', 'PAID': 'status-paid'
  }
  return map[status] || 'status-draft'
}

const GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
]

function avatarGradient(name) {
  if (!name) return GRADIENTS[0]
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return GRADIENTS[Math.abs(hash) % GRADIENTS.length]
}

function openDetail(salary) {
  detailModal.data = {...salary}
  detailModal.show = true
}

function updateStatus(data, newStatus) {
  data.status = newStatus
  const idx = salaries.value.findIndex(s => s.salaryId === data.salaryId)
  if (idx !== -1) salaries.value[idx].status = newStatus
  toast.success(`Cập nhật trạng thái thành: ${newStatus}`)
}
</script>

<template>
  <div class="salary-management">
    <div class="page-header">
      <div class="header-left">
        <div class="title-icon bg-success-gradient">
          <i class="bi bi-cash-coin"></i>
        </div>
        <div>
          <h1 class="page-title">Quản lý Bảng Lương</h1>
          <p class="page-description">Tính toán, xét duyệt và thanh toán lương nhân viên</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn-outline">
          <i class="bi bi-download"></i>
          <span>Xuất File Bảng Lương</span>
        </button>

        <button class="btn-primary" style="background: #4f46e5; border: none;" @click="openAutoGenerateModal"
                :disabled="isGenerating">
          <span v-if="isGenerating" class="spinner-border spinner-border-sm me-2" role="status"
                aria-hidden="true"></span>
          <i v-else class="bi bi-cpu"></i>
          <span>Tính Lương Tự Động</span>
        </button>

        <button class="btn-primary bg-success-gradient" @click="openFinalizeModal" :disabled="isFinalizing">
          <span v-if="isFinalizing" class="spinner-border spinner-border-sm me-2" role="status"
                aria-hidden="true"></span>
          <i v-else class="bi bi-calculator"></i>
          <span>Chốt Lương Tháng</span>
        </button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total-icon"><i class="bi bi-wallet2"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ formatCurrency(totalPayrollCost) }}</span>
          <span class="stat-label">Tổng quỹ lương tháng này</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending-icon"><i class="bi bi-hourglass-split"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ pendingCount }}</span>
          <span class="stat-label">Đang chờ duyệt (Pending)</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon paid-icon"><i class="bi bi-check2-all"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ paidCount }}</span>
          <span class="stat-label">Đã thanh toán (Paid)</span>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-content">
        <div class="search-wrapper">
          <i class="bi bi-search search-icon"></i>
          <input v-model="filters.keyword" type="text" class="search-input"
                 placeholder="Tìm theo tên hoặc ID nhân viên..."/>
        </div>

        <div class="input-wrapper month-picker">
          <i class="bi bi-calendar-month text-muted ms-2"></i>
          <input ref="monthInput" type="month" v-model="filters.month" class="filter-select custom-month"
                 @click="openMonthPicker" @keydown.prevent/>
        </div>

        <div class="select-wrapper">
          <select v-model="filters.department" class="filter-select">
            <option value="">Tất cả Phòng ban</option>
            <option v-for="dept in departments" :key="dept.departmentName" :value="dept.departmentName">
              {{ dept.departmentName }}
            </option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>

        <div class="select-wrapper">
          <select v-model="filters.status" class="filter-select">
            <option value="">Tất cả Trạng thái</option>
            <option value="DRAFT">Nháp (Draft)</option>
            <option value="PENDING">Chờ duyệt (Pending)</option>
            <option value="APPROVED">Đã duyệt (Approved)</option>
            <option value="PAID">Đã thanh toán (Paid)</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>

        <button
            class="btn-submit-all"
            @click="handleSubmitAllPending"
            :disabled="isSubmittingAll || salaries.length === 0"
            title="Chuyển tất cả bảng lương tháng này sang Chờ duyệt"
        >
          <span v-if="isSubmittingAll" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          <i v-else class="bi bi-send-check"></i>
          Gửi duyệt tất cả
        </button>

        <div class="select-wrapper size-select">
          <select v-model.number="pagination.size" class="filter-select" @change="onSizeChange">
            <option :value="10">10 / trang</option>
            <option :value="20">20 / trang</option>
            <option :value="50">50 / trang</option>
            <option :value="100">100 / trang</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-responsive">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-num">ID</th>
            <th>NHÂN VIÊN</th>
            <th>THÁNG LƯƠNG</th>
            <th>NGÀY CÔNG (TT/C)</th>
            <th>TỔNG THU NHẬP</th>
            <th>TỔNG KHẤU TRỪ</th>
            <th>THỰC NHẬN (NET)</th>
            <th>TRẠNG THÁI</th>
            <th class="col-actions text-center">THAO TÁC</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="paginatedSalaries.length === 0">
            <td colspan="9" class="text-center py-4 text-muted">Không tìm thấy dữ liệu bảng lương nào.</td>
          </tr>
          <tr v-for="salary in paginatedSalaries" :key="salary.id" class="table-row">
            <td class="col-num text-muted">#{{ salary.id }}</td>
            <td>
              <div class="user-cell">
                <div class="avatar" :style="{ background: avatarGradient(salary.employeeName) }">
                  {{ getInitials(salary.employeeName) }}
                </div>
                <div class="user-info">
                  <span class="user-name">{{ salary.employeeName }}</span>
                  <span class="user-email">ID: EMP{{ salary.employeeId }} - {{ salary.department }}</span>
                </div>
              </div>
            </td>
            <td class="font-medium text-muted">{{ formatMonth(salary.salaryMonth) }}</td>
            <td>
              <span class="badge-light">{{ salary.actualWorkDays }} / {{ salary.standardWorkDays }}</span>
            </td>
            <td class="text-success font-medium">+{{ formatCurrency(salary.totalIncome) }}</td>
            <td class="text-danger font-medium">-{{ formatCurrency(salary.totalDeduction) }}</td>
            <td>
              <span class="salary-net-badge">{{ formatCurrency(salary.netSalary) }}</span>
            </td>
            <td>
              <span :class="['status-badge', getStatusClass(salary.status)]">
                <span class="status-dot"></span>
                {{ salary.status }}
              </span>
            </td>
            <td>
              <div class="action-buttons justify-content-center">
                <button class="action-btn view-btn" @click="openDetail(salary)" title="Xem Phiếu Lương Chi Tiết">
                  <i class="bi bi-file-earmark-spreadsheet"></i>
                </button>
                <button v-if="salary.status === 'DRAFT' || salary.status === 'PENDING'" class="action-btn edit-btn"
                        title="Cập nhật nhanh">
                  <i class="bi bi-pencil"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 0" class="pagination-bar">
        <span class="pagination-info"> Hiển thị {{ startEntry }}–{{ endEntry }} trong số {{
            totalElements
          }} bản ghi</span>
        <div class="page-controls">
          <button :disabled="pagination.page === 0" @click="goPage(0)"><i class="bi bi-chevron-double-left"></i>
          </button>
          <button :disabled="pagination.page === 0" @click="goPage(pagination.page - 1)"><i
              class="bi bi-chevron-left"></i></button>
          <button v-for="p in visiblePages" :key="p" :class="{ active: p === pagination.page }" @click="goPage(p)">
            {{ p + 1 }}
          </button>
          <button :disabled="pagination.page >= totalPages - 1" @click="goPage(pagination.page + 1)"><i
              class="bi bi-chevron-right"></i></button>
          <button :disabled="pagination.page >= totalPages - 1" @click="goPage(totalPages - 1)"><i
              class="bi bi-chevron-double-right"></i></button>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="showGenerateModal" class="modal-overlay" @click.self="showGenerateModal = false">
          <div class="modal-container" style="max-width: 420px;">
            <div class="modal-header" style="border-bottom: none; padding-bottom: 0;">
              <div class="modal-title-group">
                <div class="modal-icon" style="background: #e0e7ff; color: #4f46e5;">
                  <i class="bi bi-cpu"></i>
                </div>
                <div>
                  <h3 style="font-size: 18px;">Tính Lương Tự Động</h3>
                </div>
              </div>
              <button class="modal-close" @click="showGenerateModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="modal-body" style="background: white; padding-top: 16px;">
              <p style="color: #475569; font-size: 14.5px; margin-bottom: 0; line-height: 1.5;">
                Bạn có chắc chắn muốn hệ thống quét dữ liệu chấm công và tự động tính toán bảng lương
                <strong>{{ formatMonth(filters.month + '-01') }}</strong> cho toàn bộ nhân viên không?
              </p>
            </div>

            <div class="modal-footer" style="justify-content: flex-end; gap: 10px; border-top: none; padding-top: 0;">
              <button class="btn-secondary" @click="showGenerateModal = false" :disabled="isGenerating">Hủy bỏ</button>
              <button class="btn-primary" style="background: #4f46e5; border: none;"
                      @click="confirmAutoGenerateSalaries" :disabled="isGenerating">
                <span v-if="isGenerating" class="spinner-border spinner-border-sm me-2" role="status"
                      aria-hidden="true"></span>
                Đồng ý Chạy
              </button>
            </div>
          </div>
        </div>
      </transition>

      <transition name="modal-fade">
        <div v-if="detailModal.show" class="modal-overlay" @click.self="detailModal.show = false">
          <div class="modal-container modal-lg">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon bg-success-light text-success">
                  <i class="bi bi-receipt"></i>
                </div>
                <div>
                  <h3>Chi Tiết Phiếu Lương #{{ detailModal.data.id }}</h3>
                  <p>Tháng: {{ formatMonth(detailModal.data.salaryMonth) }} - Nhân viên:
                    <strong>{{ detailModal.data.employeeName }}</strong></p>
                </div>
              </div>
              <button class="modal-close" @click="detailModal.show = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="modal-body payslip-body">
              <div class="payslip-grid">
                <div class="payslip-col">
                  <h4 class="payslip-col-title text-success"><i class="bi bi-plus-circle"></i> KHOẢN THU NHẬP</h4>
                  <ul class="payslip-list">
                    <li><span>Lương cơ bản (Base)</span><strong>{{
                        formatCurrency(detailModal.data.baseSalarySnapshot)
                      }}</strong></li>
                    <li><span>Ngày công thực tế</span><strong>{{ detailModal.data.actualWorkDays }} ngày</strong></li>
                    <li><span>Phụ cấp (Allowance)</span><strong>{{
                        formatCurrency(detailModal.data.allowance)
                      }}</strong></li>
                    <li><span>Thưởng (Bonus)</span><strong>{{ formatCurrency(detailModal.data.bonus) }}</strong></li>
                    <li class="payslip-divider"></li>
                    <li class="payslip-total"><span>TỔNG THU NHẬP</span><strong
                        class="text-success">{{ formatCurrency(detailModal.data.totalIncome) }}</strong></li>
                  </ul>
                </div>

                <div class="payslip-col">
                  <h4 class="payslip-col-title text-danger"><i class="bi bi-dash-circle"></i> KHOẢN KHẤU TRỪ</h4>
                  <ul class="payslip-list">
                    <li><span>Bảo hiểm Xã hội (BHXH)</span><strong>{{
                        formatCurrency(detailModal.data.bhxhAmount)
                      }}</strong></li>
                    <li><span>Bảo hiểm Y tế (BHYT)</span><strong>{{
                        formatCurrency(detailModal.data.bhytAmount)
                      }}</strong></li>
                    <li><span>Bảo hiểm Thất nghiệp (BHTN)</span><strong>{{
                        formatCurrency(detailModal.data.bhtnAmount)
                      }}</strong></li>
                    <li><span>Thuế TNCN (Tax)</span><strong>{{ formatCurrency(detailModal.data.taxAmount) }}</strong>
                    </li>
                    <li class="payslip-divider"></li>
                    <li class="payslip-total"><span>TỔNG KHẤU TRỪ</span><strong
                        class="text-danger">{{ formatCurrency(detailModal.data.totalDeduction) }}</strong></li>
                  </ul>
                </div>
              </div>

              <div class="payslip-net-box">
                <div class="net-left">
                  <span>THỰC NHẬN (NET SALARY)</span>
                  <div class="status-wrap mt-1">
                    Trạng thái:
                    <span :class="['status-badge', getStatusClass(detailModal.data.status)]"
                          style="transform: scale(0.9); transform-origin: left;">
                      <span class="status-dot"></span> {{ detailModal.data.status }}
                    </span>
                  </div>
                </div>
                <div class="net-right text-success">
                  {{ formatCurrency(detailModal.data.netSalary) }}
                </div>
              </div>

              <div class="payslip-note mt-3" v-if="detailModal.data.note">
                <strong><i class="bi bi-pencil-square"></i> Ghi chú:</strong>
                <p>{{ detailModal.data.note }}</p>
              </div>
            </div>

            <div class="modal-footer justify-content-between">
              <div>
                <button class="btn-outline"><i class="bi bi-printer"></i> In Phiếu Lương</button>
              </div>
              <div class="d-flex gap-2">
                <button class="btn-secondary" @click="detailModal.show = false">Đóng</button>
                <button v-if="detailModal.data.status === 'DRAFT'" class="btn-primary"
                        @click="updateStatus(detailModal.data, 'PENDING')">Gửi Duyệt
                </button>
                <button v-if="detailModal.data.status === 'PENDING'" class="btn-primary"
                        @click="updateStatus(detailModal.data, 'APPROVED')">Duyệt Bảng Lương
                </button>
                <button v-if="detailModal.data.status === 'APPROVED'" class="btn-primary bg-success-gradient"
                        @click="updateStatus(detailModal.data, 'PAID')">Đánh dấu Đã Thanh Toán
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <transition name="modal-fade">
        <div v-if="showConfirmModal" class="modal-overlay" @click.self="showConfirmModal = false">
          <div class="modal-container" style="max-width: 420px;">
            <div class="modal-header" style="border-bottom: none; padding-bottom: 0;">
              <div class="modal-title-group">
                <div class="modal-icon" style="background: #fffbeb; color: #d97706;">
                  <i class="bi bi-exclamation-triangle"></i>
                </div>
                <div>
                  <h3 style="font-size: 18px;">Xác nhận gửi duyệt</h3>
                </div>
              </div>
              <button class="modal-close" @click="showConfirmModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="modal-body" style="background: white; padding-top: 16px;">
              <p style="color: #475569; font-size: 14.5px; margin-bottom: 0; line-height: 1.5;">
                Bạn có chắc chắn muốn chuyển toàn bộ bảng lương <strong>{{
                  formatMonth(filters.month + '-01')
                }}</strong> sang trạng thái <span
                  style="color: #b45309; font-weight: 700; background: #fef3c7; padding: 2px 6px; border-radius: 4px;">Chờ Duyệt (PENDING)</span>?
              </p>
            </div>

            <div class="modal-footer" style="justify-content: flex-end; gap: 10px; border-top: none; padding-top: 0;">
              <button class="btn-secondary" @click="showConfirmModal = false" :disabled="isSubmittingAll">Hủy bỏ
              </button>
              <button class="btn-primary" style="background: #f59e0b; border: none;" @click="confirmSubmitAllPending"
                      :disabled="isSubmittingAll">
                Đồng ý Gửi duyệt
              </button>
            </div>
          </div>
        </div>
      </transition>

      <transition name="modal-fade">
        <div v-if="showFinalizeModal" class="modal-overlay" @click.self="showFinalizeModal = false">
          <div class="modal-container" style="max-width: 420px;">
            <div class="modal-header" style="border-bottom: none; padding-bottom: 0;">
              <div class="modal-title-group">
                <div class="modal-icon" style="background: #eef2ff; color: #3b82f6;">
                  <i class="bi bi-calculator"></i>
                </div>
                <div>
                  <h3 style="font-size: 18px;">Chốt Lương Tháng</h3>
                </div>
              </div>
              <button class="modal-close" @click="showFinalizeModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <div class="modal-body" style="background: white; padding-top: 16px;">
              <p style="color: #475569; font-size: 14.5px; margin-bottom: 0; line-height: 1.5;">
                Bạn có chắc chắn muốn tạo đợt chốt lương tổng hợp cho <strong>{{
                  formatMonth(filters.month + '-01')
                }}</strong> để gửi Giám đốc xem xét?
              </p>
            </div>

            <div class="modal-footer" style="justify-content: flex-end; gap: 10px; border-top: none; padding-top: 0;">
              <button class="btn-secondary" @click="showFinalizeModal = false" :disabled="isFinalizing">Hủy bỏ</button>
              <button class="btn-primary" style="background: #3b82f6; border: none;" @click="confirmFinalizePayroll"
                      :disabled="isFinalizing">
                <span v-if="isFinalizing" class="spinner-border spinner-border-sm me-2" role="status"
                      aria-hidden="true"></span>
                Đồng ý Chốt
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

.bg-success-gradient {
  background: linear-gradient(135deg, #10b981, #059669) !important;
  color: white;
  border: none;
}

.bg-success-light {
  background: #d1fae5;
}

.text-success {
  color: #059669 !important;
}

.text-danger {
  color: #dc2626 !important;
}

.font-medium {
  font-weight: 600;
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

.total-icon {
  background: #ede9fe;
  color: #7c3aed;
}

.pending-icon {
  background: #fef3c7;
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
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.filter-content {
  padding: 14px 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.search-wrapper {
  position: relative;
  flex: 1;
  min-width: 240px;
}

.search-icon {
  position: absolute;
  left: 13px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 13px;
  pointer-events: none;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 9px 32px 9px 38px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  background: #fafbff;
  transition: all 0.2s ease;
  font-family: inherit;
  color: #0f172a;
}

.search-input:focus {
  outline: none;
  border-color: #10b981;
  background: white;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.input-wrapper, .select-wrapper {
  position: relative;
  min-width: 140px;
}

.size-select {
  min-width: 110px;
  flex-shrink: 0;
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

.btn-submit-all {
  padding: 9px 16px;
  background: #fffbeb;
  color: #b45309;
  border: 1.5px solid #fde68a;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.btn-submit-all:hover:not(:disabled) {
  background: #fef3c7;
  border-color: #fcd34d;
  color: #92400e;
  transform: translateY(-1px);
}

.btn-submit-all:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #94a3b8;
}

.custom-month {
  padding-left: 36px;
  padding-right: 12px;
  caret-color: transparent;
}

.custom-month:focus {
  outline: none;
  border-color: #10b981;
}

.month-picker i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  z-index: 1;
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

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
}

.table-container {
  background: white;
  border-radius: 18px;
  border: 1px solid #e8edff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
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
  padding: 13px 16px;
  font-size: 13px;
  color: #334155;
  vertical-align: middle;
  border-bottom: 1px solid #f1f5f9;
}

.table-row:hover {
  background: #fafbff;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #0f172a;
  font-size: 13.5px;
}

.user-email {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 1px;
}

.badge-light {
  padding: 4px 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.salary-net-badge {
  display: inline-block;
  padding: 6px 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
  border-radius: 8px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  font-size: 14px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: capitalize;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-draft {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.status-draft .status-dot {
  background: #94a3b8;
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
  transition: all 0.2s ease;
}

.view-btn {
  background: #e0f2fe;
  color: #0284c7;
}

.view-btn:hover {
  background: #0284c7;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(2, 132, 199, 0.3);
}

.edit-btn {
  background: #ede9fe;
  color: #7c3aed;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: white;
  border-top: 1.5px solid #e8edff;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.page-controls {
  display: flex;
  gap: 6px;
  align-items: center;
}

.page-controls button {
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-controls button:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #10b981;
  color: #10b981;
}

.page-controls button.active {
  background: #10b981;
  color: #fff;
  border-color: #10b981;
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.2);
}

.page-controls button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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
  max-width: 480px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.modal-lg {
  max-width: 750px;
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
}

.modal-header p {
  font-size: 12px;
  color: #64748b;
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

.payslip-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.payslip-col {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.payslip-col-title {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.5px;
}

.payslip-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payslip-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #475569;
}

.payslip-list li strong {
  color: #0f172a;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13.5px;
}

.payslip-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 4px 0;
}

.payslip-total {
  font-weight: 700 !important;
  font-size: 14px !important;
}

.payslip-net-box {
  background: linear-gradient(to right, #1e293b, #0f172a);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.2);
}

.net-left span {
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.net-right {
  font-size: 28px;
  font-weight: 800;
  font-family: 'JetBrains Mono', monospace;
}

.payslip-note {
  background: #fffbeb;
  border: 1px solid #fde68a;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  color: #92400e;
}

.payslip-note p {
  margin-top: 4px;
  margin-bottom: 0;
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

.mt-1 {
  margin-top: 4px;
}

.mt-3 {
  margin-top: 16px;
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
  .payslip-grid {
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

  .pagination-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>