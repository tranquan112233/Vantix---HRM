<template>
  <div class="leave-management-wrapper p-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="fw-bold mb-1"><i class="bi bi-tags-fill text-primary me-2"></i>Danh mục Nghỉ phép</h3>
      </div>
      <button class="btn btn-primary px-4 shadow-sm" @click="openModal('add')">
        <i class="bi bi-plus-circle me-2"></i>Thêm loại nghỉ mới
      </button>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="modern-table w-100">
          <thead>
          <tr>
            <th class="text-center" style="width: 80px">ID</th>
            <th>Tên loại nghỉ phép</th>
            <th class="text-center">Hưởng lương (is_paid)</th>
            <th class="text-end">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="type in leaveTypes" :key="type.leaveTypeId" class="table-row">
            <td class="text-center text-muted">#{{ type.leaveTypeId }}</td>
            <td class="fw-bold text-primary">{{ type.typeName }}</td>
            <td class="text-center">
              <span v-if="type.isPaid" class="badge bg-success"><i class="bi bi-check-circle"></i> Có</span>
              <span v-else class="badge bg-danger"><i class="bi bi-x-circle"></i> Không</span>
            </td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-secondary me-2" @click="openModal('edit', type)">
                <i class="bi bi-pencil-square"></i>
              </button>
              <button class="btn btn-sm btn-outline-danger" @click="deleteType(type.leaveTypeId)">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>
          <tr v-if="leaveTypes.length === 0">
            <td colspan="4" class="text-center py-5 text-muted">Chưa có dữ liệu.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="custom-modal-overlay">
      <div class="custom-modal-content card border-0 shadow-lg">
        <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
          <h5 class="mb-0 fw-bold">{{ modalMode === 'add' ? '✨ Thêm Mới' : '✏️ Cập Nhật' }}</h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>
        <div class="card-body p-4">
          <form @submit.prevent="saveLeaveType">
            <div class="mb-4">
              <label class="form-label fw-bold">Tên loại nghỉ <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="formData.typeName" required>
            </div>

            <div class="mb-4 form-check form-switch">
              <input class="form-check-input" type="checkbox" id="isPaid" v-model="formData.isPaid">
              <label class="form-check-label fw-bold" for="isPaid">Loại nghỉ này CÓ hưởng lương</label>
            </div>

            <div class="d-flex justify-content-end gap-2">
              <button type="button" class="btn btn-light border" @click="closeModal">Hủy bỏ</button>
              <button type="submit" class="btn btn-primary px-4">
                <i class="bi bi-save me-1"></i> Lưu lại
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import leaveTypeService from '@/services/leavetypeservice.service.js'

const leaveTypes = ref([])
const showModal = ref(false)
const modalMode = ref('add')
let editingId = null

const formData = reactive({
  typeName: '',
  isPaid: true
})

// 1. LẤY DỮ LIỆU
const fetchLeaveTypes = async () => {
  try {
    const response = await leaveTypeService.getAll() // 🔥 Dùng service cực gọn
    leaveTypes.value = response.data
  } catch (error) {
    console.error('Lỗi API GET:', error)
  }
}

onMounted(() => fetchLeaveTypes())

// 2. XỬ LÝ MODAL (Giữ nguyên không đổi)
const openModal = (mode, typeData = null) => {
  modalMode.value = mode
  if (mode === 'edit' && typeData) {
    editingId = typeData.leaveTypeId
    Object.assign(formData, { typeName: typeData.typeName, isPaid: typeData.isPaid })
  } else {
    editingId = null
    Object.assign(formData, { typeName: '', isPaid: true })
  }
  showModal.value = true
}

const closeModal = () => showModal.value = false

// 3. THÊM / CẬP NHẬT
const saveLeaveType = async () => {
  try {
    if (modalMode.value === 'add') {
      await leaveTypeService.create(formData) // 🔥 Dùng service
    } else {
      await leaveTypeService.update(editingId, formData) // 🔥 Dùng service
    }
    closeModal()
    fetchLeaveTypes()
  } catch (error) {
    console.error('Lỗi khi lưu:', error)
    alert(error.response?.data || 'Có lỗi xảy ra!')
  }
}

// 4. XÓA
const deleteType = async (id) => {
  if(confirm('Bạn có chắc chắn muốn xóa không?')) {
    try {
      await leaveTypeService.delete(id) // 🔥 Dùng service
      fetchLeaveTypes()
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
    }
  }
}
</script>

<style scoped>
/* Table hiện đại */
.modern-table { border-collapse: collapse; text-align: left; }
.modern-table th { background-color: #f8fafc; padding: 14px 16px; font-size: 0.85rem; text-transform: uppercase; color: #64748b; font-weight: 600; border-bottom: 2px solid #e2e8f0; }
.modern-table td { padding: 16px; font-size: 0.95rem; border-bottom: 1px solid #e2e8f0; vertical-align: middle; }
.table-row:hover { background-color: #f1f5f9; transition: 0.2s; }

/* Modal Tự Chế */
.custom-modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100vw; height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
  backdrop-filter: blur(2px);
}
.custom-modal-content {
  width: 100%;
  max-width: 500px;
  animation: slideDown 0.3s ease-out;
}
@keyframes slideDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>