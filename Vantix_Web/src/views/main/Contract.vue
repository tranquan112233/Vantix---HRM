<template>
  <div class="contract-management mgmt-page">
    <div class="page-header">
      <div class="header-left">
        <div class="title-icon">
          <i class="bi bi-file-earmark-text-fill"></i>
        </div>
        <div>
          <h1 class="page-title">Hợp Đồng Nhân Sự</h1>
          <p class="page-description">Quản lý và theo dõi trạng thái hợp đồng lao động</p>
        </div>
      </div>
      <div class="header-actions">
        <button v-if="isFilterActive" class="btn-outline" @click="clearFilters">
          <i class="bi bi-funnel-fill"></i>
          <span>Xóa bộ lọc</span>
        </button>
        <button v-if="canCreate" class="btn-primary" @click="openCreateModal" :disabled="loading">
          <i class="bi bi-plus-lg"></i>
          <span>Thêm Hợp Đồng</span>
        </button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card" :class="{ 'active-stat': currentFilter === 'ALL' }" @click="currentFilter = 'ALL'">
        <div class="stat-icon total-icon"><i class="bi bi-file-earmark-text"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ totalContracts }}</span>
          <span class="stat-label">Tổng số HĐ</span>
        </div>
      </div>
      <div class="stat-card" :class="{ 'active-stat': currentFilter === 'ACTIVE' }" @click="currentFilter = 'ACTIVE'">
        <div class="stat-icon active-icon"><i class="bi bi-check-circle-fill"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ activeContracts }}</span>
          <span class="stat-label">Đang hiệu lực</span>
        </div>
      </div>
      <div class="stat-card" :class="{ 'active-stat': currentFilter === 'EXPIRED' }" @click="currentFilter = 'EXPIRED'">
        <div class="stat-icon locked-icon"><i class="bi bi-x-circle-fill"></i></div>
        <div class="stat-content">
          <span class="stat-value">{{ expiredContracts }}</span>
          <span class="stat-label">Đã hết hạn</span>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-content">
        <!-- 🌟 Khung tìm kiếm được bóp ngắn lại -->
        <div class="search-wrapper">
          <i class="bi bi-search search-icon"></i>
          <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Tìm ID NV hoặc Tên..."
          />
          <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <div class="select-wrapper">
          <select v-model="selectedType" class="filter-select">
            <option value="ALL">Tất cả loại HĐ</option>
            <option value="YEAR_1">1 Năm</option>
            <option value="YEAR_3">3 Năm</option>
            <option value="INDEFINITE">Vô thời hạn</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>

        <div class="select-wrapper">
          <select v-model="selectedPosition" class="filter-select">
            <option value="ALL">Tất cả vị trí</option>
            <option v-for="pos in availablePositions" :key="pos" :value="pos">{{ pos }}</option>
          </select>
          <i class="bi bi-chevron-down select-icon"></i>
        </div>

        <!-- 🌟 Khung lương được kéo giãn ra và hỗ trợ tự động điền dấu chấm -->
        <div class="salary-range-wrapper">
          <i class="bi bi-cash-coin search-icon"></i>
          <input v-model="minSalaryFormatted" type="text" placeholder="Lương từ" class="salary-input"/>
          <span class="salary-separator">-</span>
          <input v-model="maxSalaryFormatted" type="text" placeholder="Đến" class="salary-input"/>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner-ring"></div>
        <p>Đang tải dữ liệu hợp đồng...</p>
      </div>

      <div v-else-if="filteredContracts.length === 0" class="empty-state">
        <div class="empty-icon"><i class="bi bi-folder-x"></i></div>
        <h3>Không tìm thấy hợp đồng nào</h3>
        <p>Thử thay đổi bộ lọc hoặc tạo mới một hợp đồng.</p>
        <button v-if="canCreate" class="btn-primary" @click="openCreateModal">
          <i class="bi bi-plus-lg"></i>
          Thêm hợp đồng
        </button>
      </div>

      <div v-else class="table-responsive">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-num">#</th>
            <th>NHÂN VIÊN</th>
            <th>VỊ TRÍ</th>
            <th>LOẠI HĐ</th>
            <th>THỜI HẠN</th>
            <th>LƯƠNG CƠ BẢN</th>
            <th>TRẠNG THÁI</th>
            <th class="col-actions text-center">THAO TÁC</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(c, index) in paginatedContracts" :key="c.contractId" class="table-row">
            <td class="col-num text-muted">{{ pageStart + index + 1 }}</td>

            <td>
              <div class="user-cell">
                <div class="avatar" :style="{ background: c.avatarBg }">
                  {{ c.initials }}
                </div>
                <div class="user-info">
                  <span class="user-name">{{ c.fullName || 'Chưa rõ tên' }}</span>
                  <span class="user-email">ID: #{{ c.employeeId || 'N/A' }}</span>
                </div>
              </div>
            </td>

            <td class="text-muted font-medium">{{ c.position }}</td>

            <td>
              <span class="role-badge">
                <i class="bi bi-file-text"></i>
                {{ c.typeLabel }}
              </span>
            </td>

            <td>
              <div class="time-cell">
                <span>{{ c.startDateLabel }}</span>
                <i class="bi bi-arrow-right-short text-muted"></i>
                <span>{{ c.endDateLabel }}</span>
              </div>
            </td>

            <td>
              <span class="salary-text">{{ c.salaryLabel }}</span>
            </td>

            <td>
              <span :class="['status-badge', c.status === 'ACTIVE' ? 'status-active' : 'status-locked']">
                <span class="status-dot"></span>
                {{ c.status === 'ACTIVE' ? 'Hiệu lực' : 'Hết hạn' }}
              </span>
            </td>
            <td>
              <div class="action-buttons justify-content-center">
                <button v-if="canStatusUpdate" class="action-btn edit-btn" @click="toggleContractStatus(c.contractId)" title="Đổi trạng thái">
                  <i class="bi bi-arrow-repeat"></i>
                </button>
                <button class="action-btn view-btn" @click="viewAnnex(c.contractId)" title="Xem Phụ lục">
                  <i class="bi bi-eye"></i>
                </button>
                <button v-if="canDelete" class="action-btn delete-btn" @click="confirmDeleteContract(c.contractId)" title="Xóa">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="filteredContracts.length > pageSize" class="pagination-bar">
        <div class="pagination-info">
          Hiển thị {{ pageStart + 1 }}-{{ pageEnd }} / {{ filteredContracts.length }} hợp đồng
        </div>
        <div class="pagination-actions">
          <button class="page-btn" :disabled="currentPage === 1" @click="currentPage -= 1">
            <i class="bi bi-chevron-left"></i>
          </button>
          <span class="page-current">Trang {{ currentPage }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage += 1">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
          <div class="modal-container">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon modal-icon-create">
                  <i class="bi bi-file-earmark-plus"></i>
                </div>
                <div>
                  <h3>Tạo Hợp Đồng Mới</h3>
                  <p>Nhập thôngợp đồng cho nhân viên</p>
                </div>
              </div>
              <button class="modal-close" @click="showModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>

            <form @submit.prevent="handleSubmit">
              <div class="modal-body">
                <div class="form-group">
                  <label>ID Nhân viên <span class="required">*</span></label>
                  <div class="input-wrapper">
                    <i class="bi bi-person-badge"></i>
                    <input v-model="form.employeeId" type="number" placeholder="Nhập ID nhân viên..." required min="1"/>
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group custom-dropdown-group">
                    <label>Vị trí công tác <span class="required">*</span></label>
                    <div class="input-wrapper">
                      <i class="bi bi-briefcase"></i>
                      <input
                          type="text"
                          v-model="positionSearch"
                          @focus="showPositionDropdown = true"
                          @blur="delayHidePositionDropdown"
                          placeholder="Chọn hoặc nhập vị trí..."
                          required
                      />
                      <i class="bi bi-chevron-down select-icon-modal"></i>
                    </div>
                    <transition name="dropdown-fade">
                      <ul v-if="showPositionDropdown" class="custom-dropdown-list">
                        <li v-if="filteredFormPositions.length === 0" class="dropdown-empty">
                          <i class="bi bi-info-circle"></i> Sẽ tạo mới vị trí này
                        </li>
                        <li v-for="pos in filteredFormPositions" :key="pos.positionId || pos.id || pos"
                            @mousedown.prevent="selectPosition(pos)" class="dropdown-item">
                          {{ pos.positionName || pos.name || pos }}
                        </li>
                      </ul>
                    </transition>
                  </div>

                  <div class="form-group">
                    <label>Loại Hợp Đồng</label>
                    <div class="select-wrapper-modal">
                      <select v-model="form.type" class="modal-select">
                        <option value="YEAR_1">1 Năm</option>
                        <option value="YEAR_3">3 Năm</option>
                        <option value="INDEFINITE">Vô thời hạn</option>
                      </select>
                      <i class="bi bi-chevron-down select-icon-modal"></i>
                    </div>
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>Ngày bắt đầu <span class="required">*</span></label>
                    <div class="input-wrapper">
                      <i class="bi bi-calendar-event"></i>
                      <input v-model="form.startDate" type="date" required/>
                    </div>
                  </div>
                  <div class="form-group">
                    <label>Ngày kết thúc</label>
                    <div class="input-wrapper">
                      <i class="bi bi-calendar-check"></i>
                      <input v-model="form.endDate" type="date" disabled class="bg-light"/>
                    </div>
                  </div>
                </div>

                <div class="form-row">
                  <!-- 🌟 Lương trong Form cũng hỗ trợ auto format dấu chấm -->
                  <div class="form-group">
                    <label>Lương cơ bản (VNĐ) <span class="required">*</span></label>
                    <div class="input-wrapper">
                      <i class="bi bi-cash"></i>
                      <input v-model="baseSalaryFormatted" type="text" placeholder="0" required/>
                    </div>
                  </div>
                  <div class="form-group">
                    <label>Trạng thái</label>
                    <div class="select-wrapper-modal">
                      <select v-model="form.status" class="modal-select">
                        <option value="ACTIVE">Hiệu lực</option>
                        <option value="EXPIRED">Hết hạn</option>
                      </select>
                      <i class="bi bi-chevron-down select-icon-modal"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="modal-footer">
                <button type="button" class="btn-secondary" @click="showModal = false" :disabled="loading">Hủy</button>
                <button type="submit" class="btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-small"></span>
                  <template v-else>
                    <i class="bi bi-save"></i>
                    Lưu Hợp Đồng
                  </template>
                </button>
              </div>
            </form>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="showDeleteConfirmModal" class="modal-overlay" @click.self="showDeleteConfirmModal = false">
          <div class="modal-container modal-sm">
            <div class="modal-header">
              <div class="modal-title-group">
                <div class="modal-icon modal-icon-danger">
                  <i class="bi bi-exclamation-triangle-fill"></i>
                </div>
                <div>
                  <h3>Xóa Hợp Đồng</h3>
                  <p>Hành động không thể hoàn tác</p>
                </div>
              </div>
              <button class="modal-close" @click="showDeleteConfirmModal = false">
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div class="modal-body delete-body">
              <div class="delete-avatar" style="background: var(--danger-light); color: var(--danger)">
                <i class="bi bi-file-earmark-x"></i>
              </div>
              <p class="delete-name">Hợp đồng #{{ contractIdToDelete }}</p>
              <p class="delete-warning">Bạn có chắc chắn muốn xóa hợp đồng này khỏi hệ thống vĩnh viễn không?</p>
            </div>
            <div class="modal-footer">
              <button class="btn-secondary" @click="showDeleteConfirmModal = false">Hủy</button>
              <button class="btn-danger" :disabled="loading" @click="executeDeleteContract">
                <span v-if="loading" class="spinner-small"></span>
                <template v-else>
                  <i class="bi bi-trash"></i>
                  Đồng ý Xóa
                </template>
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted, watch} from 'vue';
import {useRouter} from 'vue-router';
import {useToast} from '@/utils/toast';
import contractService from "@/services/contract.service";
import positionsService from "@/services/position.service.js";
import { useAuthStore } from '@/stores/auth.store.js';

const router = useRouter();
const toast = useToast();
const auth = useAuthStore();

const canCreate       = computed(() => auth.can('CONTRACT_CREATE'))
const canDelete       = computed(() => auth.can('CONTRACT_DELETE'))
const canStatusUpdate = computed(() => auth.can('CONTRACT_STATUS_UPDATE'))

// --- STATE ---
const contracts = ref([]);
const dbPositions = ref([]);
const loading = ref(false);
const showModal = ref(false);

const showDeleteConfirmModal = ref(false);
const contractIdToDelete = ref(null);

const currentFilter = ref('ALL');
const searchQuery = ref('');
const debouncedSearchQuery = ref('');
const selectedType = ref('ALL');
const selectedPosition = ref('ALL');
const currentPage = ref(1);
const pageSize = 25;
let searchTimer = null;

// 🌟 Lưu giá trị thật (số thô)
const minSalary = ref(null);
const maxSalary = ref(null);

const form = ref({
  contractId: null,
  employeeId: '',
  type: 'YEAR_1',
  startDate: '',
  endDate: '',
  position: '',
  baseSalary: null, // Đổi về null để Form init dễ kiểm soát
  status: 'ACTIVE'
});

// 🌟 Computed Formatting tiền tệ (Thêm dấu chấm tự động khi nhập)
const minSalaryFormatted = computed({
  get: () => minSalary.value ? new Intl.NumberFormat('vi-VN').format(minSalary.value) : '',
  set: (val) => {
    const rawValue = val.toString().replace(/\D/g, ''); // Xóa mọi ký tự không phải số
    minSalary.value = rawValue ? Number(rawValue) : null;
  }
});

const maxSalaryFormatted = computed({
  get: () => maxSalary.value ? new Intl.NumberFormat('vi-VN').format(maxSalary.value) : '',
  set: (val) => {
    const rawValue = val.toString().replace(/\D/g, '');
    maxSalary.value = rawValue ? Number(rawValue) : null;
  }
});

const baseSalaryFormatted = computed({
  get: () => form.value.baseSalary ? new Intl.NumberFormat('vi-VN').format(form.value.baseSalary) : '',
  set: (val) => {
    const rawValue = val.toString().replace(/\D/g, '');
    form.value.baseSalary = rawValue ? Number(rawValue) : null;
  }
});


const positionSearch = ref('');
const showPositionDropdown = ref(false);

// Delay hide dropdown so click event registers
const delayHidePositionDropdown = () => {
  setTimeout(() => showPositionDropdown.value = false, 200)
};

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
const currencyFormatter = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});
const dateFormatter = new Intl.DateTimeFormat('vi-VN', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric'
});
const typeLabels = {'YEAR_1': '1 Năm', 'YEAR_3': '3 Năm', 'INDEFINITE': 'Vô thời hạn'};

const formatCurrency = (value) => currencyFormatter.format(value || 0);
const formatDate = (dateStr) => dateStr ? dateFormatter.format(new Date(dateStr)) : 'Vô thời hạn';
const getTypeLabel = (type) => {
  return typeLabels[type] || type;
};
const getInitials = (name) => name ? name.charAt(0).toUpperCase() : 'U';

const GRADIENTS = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
];

const avatarGradient = (name) => {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return GRADIENTS[Math.abs(hash) % GRADIENTS.length];
};

const normalizeContract = (contract) => {
  const displayName = contract.fullName || contract.employeeId?.toString() || 'U';
  return {
    ...contract,
    typeLabel: getTypeLabel(contract.type),
    startDateLabel: formatDate(contract.startDate),
    endDateLabel: formatDate(contract.endDate),
    salaryLabel: formatCurrency(contract.baseSalary),
    initials: getInitials(displayName),
    avatarBg: avatarGradient(displayName),
    searchText: `${contract.fullName || ''} ${contract.employeeId || ''}`.toLowerCase(),
  };
};

const availablePositions = computed(() => {
  const positions = contracts.value.map(c => c.position).filter(Boolean);
  return [...new Set(positions)];
});

// --- COMPUTED STATS ---
const totalContracts = computed(() => contracts.value.length);
const contractStats = computed(() => {
  let active = 0;
  let expired = 0;
  for (const contract of contracts.value) {
    if (contract.status === 'ACTIVE') active += 1;
    if (contract.status === 'EXPIRED') expired += 1;
  }
  return {active, expired};
});
const activeContracts = computed(() => contractStats.value.active);
const expiredContracts = computed(() => contractStats.value.expired);

// --- LOGIC LỌC ĐA TẦNG ---
const filteredContracts = computed(() => {
  let result = contracts.value;

  if (currentFilter.value !== 'ALL') result = result.filter(c => c.status === currentFilter.value);
  if (selectedType.value !== 'ALL') result = result.filter(c => c.type === selectedType.value);
  if (selectedPosition.value !== 'ALL') result = result.filter(c => c.position === selectedPosition.value);

  // So sánh dựa trên giá trị raw của minSalary và maxSalary
  if (minSalary.value !== null && minSalary.value !== '') result = result.filter(c => c.baseSalary >= Number(minSalary.value));
  if (maxSalary.value !== null && maxSalary.value !== '') result = result.filter(c => c.baseSalary <= Number(maxSalary.value));

  if (debouncedSearchQuery.value.trim() !== '') {
    const query = debouncedSearchQuery.value.toLowerCase().trim();
    result = result.filter(c => c.searchText.includes(query));
  }
  return result;
});

const totalPages = computed(() => Math.max(1, Math.ceil(filteredContracts.value.length / pageSize)));
const pageStart = computed(() => (currentPage.value - 1) * pageSize);
const pageEnd = computed(() => Math.min(pageStart.value + pageSize, filteredContracts.value.length));
const paginatedContracts = computed(() => filteredContracts.value.slice(pageStart.value, pageEnd.value));

const isFilterActive = computed(() => {
  return currentFilter.value !== 'ALL' || selectedType.value !== 'ALL' ||
      selectedPosition.value !== 'ALL' || searchQuery.value !== '' ||
      (minSalary.value !== null && minSalary.value !== '') ||
      (maxSalary.value !== null && maxSalary.value !== '');
});

const clearFilters = () => {
  currentFilter.value = 'ALL';
  searchQuery.value = '';
  debouncedSearchQuery.value = '';
  selectedType.value = 'ALL';
  selectedPosition.value = 'ALL';
  minSalary.value = null;
  maxSalary.value = null;
};

watch(searchQuery, (value) => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    debouncedSearchQuery.value = value;
  }, 180);
});

watch([currentFilter, debouncedSearchQuery, selectedType, selectedPosition, minSalary, maxSalary], () => {
  currentPage.value = 1;
});

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages;
});

// --- AUTO-CALCULATE END DATE ---
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

// --- METHODS CALL API ---
const fetchContracts = async () => {
  loading.value = true;
  try {
    const response = await contractService.getAll();
    contracts.value = (response.data || []).map(normalizeContract);
  } catch (error) {
    toast.error("Lỗi khi tải danh sách hợp đồng");
  } finally {
    loading.value = false;
  }
};

const fetchPositions = async () => {
  try {
    const response = await positionsService.getAll();
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
    baseSalary: null, // Đã chỉnh về null để ô input trống ban đầu
    status: 'ACTIVE'
  };
  showModal.value = true;
};

const handleSubmit = async () => {
  if (!form.value.position.trim()) {
    toast.warning("Vui lòng chọn hoặc nhập vị trí công việc!");
    return;
  }

  // 🌟 Đẩy con số thực tế không có dấu phẩy xuống API
  const payload = {
    type: form.value.type,
    position: form.value.position,
    startDate: form.value.startDate,
    endDate: form.value.endDate || null,
    baseSalary: Number(form.value.baseSalary || 0),
    status: form.value.status,
    employeeId: Number(form.value.employeeId)
  };

  try {
    loading.value = true;
    await contractService.create(payload);
    await fetchContracts();
    toast.success('Tạo hợp đồng thành công!');
    showModal.value = false;
  } catch (error) {
    const errorMsg = error.response?.data || 'Có lỗi xảy ra, vui lòng kiểm tra lại!';
    toast.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

const toggleContractStatus = async (id) => {
  try {
    loading.value = true;
    const response = await contractService.update(id);
    await fetchContracts();
    toast.success(response.data || 'Cập nhật trạng thái thành công');
  } catch (error) {
    const errorMsg = error.response?.data || 'Lỗi cập nhật trạng thái.';
    toast.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

const confirmDeleteContract = (id) => {
  contractIdToDelete.value = id;
  showDeleteConfirmModal.value = true;
};

const executeDeleteContract = async () => {
  const id = contractIdToDelete.value;
  if (!id) return;

  try {
    loading.value = true;
    const response = await contractService.delete(id);
    contracts.value = contracts.value.filter(c => c.contractId !== id);
    toast.success(response.data || 'Đã xóa hợp đồng thành công.');
    showDeleteConfirmModal.value = false;
  } catch (error) {
    const errorMsg = error.response?.data || 'Không thể xóa hợp đồng.';
    toast.error(errorMsg);
  } finally {
    loading.value = false;
    contractIdToDelete.value = null;
  }
};

const viewAnnex = (id) => router.push({name: 'ContractAnnex', params: {id}});

onMounted(() => {
  fetchContracts();
  fetchPositions();
});

onUnmounted(() => {
  if (searchTimer) clearTimeout(searchTimer);
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.contract-management {
  padding: 28px 32px;
  min-height: 100vh;
  background: #f0f4ff;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

/* ── MÀU CSS VARIABLES ── */
.contract-management {
  --danger: #dc2626;
  --danger-light: #fee2e2;
  --success: #059669;
  --success-light: #d1fae5;
  --primary: #4f46e5;
  --primary-light: #ede9fe;
}

/* ── Page Header ── */
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

/* ── Stats Row ── */
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
  border: 1.5px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  flex: 1;
  min-width: 140px;
  transition: all 0.2s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.active-stat {
  border-color: #6366f1;
  background: #fafbff;
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

.active-icon {
  background: #d1fae5;
  color: #059669;
}

.locked-icon {
  background: #fee2e2;
  color: #dc2626;
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

/* ── Filter Card ── */
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

/* 🌟 ĐÃ SỬA: Khung tìm kiếm giãn dài ra bù vào chỗ trống */
.search-wrapper {
  position: relative;
  flex: 1 1 auto; /* Tự động giãn ra lấy hết khoảng trống dư thừa */
  min-width: 220px; /* Không được nhỏ hơn mức này khi thu nhỏ trình duyệt */
  max-width: 550px; /* Giới hạn độ dài tối đa để không bị quá dài trên màn hình to */
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
  transition: all 0.2s;
  font-family: inherit;
  color: #0f172a;
}

.search-input:focus {
  outline: none;
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  z-index: 1;
  border-radius: 5px;
}

.clear-btn:hover {
  color: #475569;
  background: #f1f5f9;
}

.select-wrapper {
  position: relative;
  min-width: 140px;
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
  transition: all 0.2s;
}

.filter-select:focus {
  outline: none;
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.select-icon {
  position: absolute;
  right: 11px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 11px;
  pointer-events: none;
}

/* 🌟 ĐÃ SỬA: Khung lương bị ép ngắn lại 1 nửa */
.salary-range-wrapper {
  display: flex;
  align-items: center;
  background: #fafbff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 0;
  position: relative;
  overflow: hidden;
  transition: all 0.2s;

  flex: 1; /* Không tự động giãn bành trướng nữa */
  min-width: 260px;
}

.salary-range-wrapper:focus-within {
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.salary-input {
  border: none;
  background: transparent;
  padding: 9px 10px;
  font-size: 13px;
  width: 100%;
  text-align: center;
  color: #0f172a;
  outline: none;
  font-family: inherit;
}

.salary-input:first-of-type {
  padding-left: 36px;
}

.salary-separator {
  color: #94a3b8;
  font-weight: 600;
  font-size: 12px;
}

/* ── Buttons ── */
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

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.btn-primary:disabled {
  opacity: 0.55;
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

.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.35);
}

/* ── Table ── */
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

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
  background: #fafbff;
}

.pagination-info,
.page-current {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 600;
}

.pagination-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #c7d2fe;
  color: #4f46e5;
  background: #f8fafc;
}

.page-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
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

.col-num {
  width: 52px;
}

.text-muted {
  color: #94a3b8;
}

.font-medium {
  font-weight: 600;
  color: #475569;
}

/* User Cell */
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

/* Badges */
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

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 12.5px;
  font-weight: 500;
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

/* ── Modal ── */
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
  max-width: 520px;
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

/* Form Group */
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
  top: 50%;
  transform: translateY(-50%);
  color: #c7d2fe;
  font-size: 14px;
  pointer-events: none;
  z-index: 1;
}

.input-wrapper input {
  width: 100%;
  padding: 10px 12px 10px 38px;
  border: 1.5px solid #e8edff;
  border-radius: 11px;
  font-size: 13.5px;
  background: #fafbff;
  transition: all 0.2s;
  font-family: inherit;
  color: #0f172a;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.bg-light {
  background: #f1f5f9 !important;
  color: #64748b !important;
  cursor: not-allowed;
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
  transition: all 0.2s;
  font-family: inherit;
  color: #334155;
  font-weight: 500;
}

.modal-select:focus {
  outline: none;
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

/* Custom Dropdown Modal */
.custom-dropdown-group {
  position: relative;
}

.custom-dropdown-list {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 11px;
  border: 1px solid #e8edff;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  max-height: 180px;
  overflow-y: auto;
  z-index: 100;
  padding: 6px;
  list-style: none;
  margin: 0;
}

.dropdown-item {
  padding: 8px 12px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: #f0f4ff;
  color: #4f46e5;
  font-weight: 500;
}

.dropdown-empty {
  padding: 8px 12px;
  font-size: 12px;
  color: #64748b;
  font-style: italic;
}

/* Delete Modal */
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

.spinner-small {
  display: inline-block;
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
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

.dropdown-fade-enter-active, .dropdown-fade-leave-active {
  transition: all 0.2s ease;
}

.dropdown-fade-enter-from, .dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

@media (max-width: 768px) {
  .contract-management {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: flex-start;
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }

  .search-wrapper, .select-wrapper, .salary-range-wrapper {
    width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .stats-row {
    gap: 10px;
  }

  .pagination-bar {
    align-items: stretch;
    flex-direction: column;
  }

  .pagination-actions {
    justify-content: space-between;
  }
}
</style>
