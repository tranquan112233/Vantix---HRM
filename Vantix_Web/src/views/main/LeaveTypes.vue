<template>
  <div class="leave-types-page mgmt-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Loại nghỉ phép</h1>
        <p class="page-desc">Quản lý danh mục loại nghỉ phép và quy tắc hưởng lương.</p>
      </div>
      <div class="header-actions">
        <button v-if="canCreate" class="btn-primary" @click="openModal('add')">
          <i class="bi bi-plus-lg"></i> Thêm loại nghỉ
        </button>
      </div>
    </div>

    <div v-if="loading" class="state-center table-card">
      <div class="spin-lg mb-3"></div>
      <div class="empty-title">Đang tải danh mục</div>
      <div class="empty-sub">Hệ thống đang lấy dữ liệu loại nghỉ phép hiện có.</div>
    </div>

    <div v-else class="table-card">
      <div v-if="leaveTypes.length > 0" class="table-scroll">
        <table>
          <thead>
            <tr>
              <th style="width: 100px">ID</th>
              <th>Loại nghỉ</th>
              <th class="text-center">Hưởng lương</th>
              <th class="text-right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="type in leaveTypes" :key="type.leaveTypeId">
              <td class="td-meta">#{{ type.leaveTypeId }}</td>
              <td>
                <div class="type-name-cell">
                  <div class="title-icon small-icon">
                    <i class="bi bi-tags"></i>
                  </div>
                  <div>
                    <div class="employee-name">{{ type.typeName }}</div>
                    <div class="employee-username">Mã loại nghỉ #{{ type.leaveTypeId }}</div>
                  </div>
                </div>
              </td>
              <td class="text-center">
                <span :class="['status-badge', type.isPaid ? 'completed' : 'open']">
                  {{ type.isPaid ? 'Có lương' : 'Không lương' }}
                </span>
              </td>
              <td class="text-right">
                <div class="row-actions justify-content-end">
                  <button
                    v-if="canEdit"
                    class="icon-btn"
                    type="button"
                    title="Chỉnh sửa"
                    @click="openModal('edit', type)"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button
                    v-if="canDelete"
                    class="icon-btn danger"
                    type="button"
                    title="Xóa"
                    @click="deleteType(type)"
                  >
                    <i class="bi bi-trash3"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="state-center">
        <i class="bi bi-tags empty-icon"></i>
        <div class="empty-title">Chưa có loại nghỉ phép</div>
        <div class="empty-sub">Hãy thêm loại nghỉ mới để nhân viên có thể gửi đơn đúng nhóm.</div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <div class="modal-custom">
          <div class="modal-header-custom">
            <div>
              <h3 class="modal-title">
                {{ modalMode === 'add' ? 'Thêm loại nghỉ phép' : 'Cập nhật loại nghỉ phép' }}
              </h3>
              <p class="modal-subtitle">Tên loại nghỉ cần ngắn gọn, dễ hiểu và thống nhất.</p>
            </div>
            <button class="btn-close-custom" type="button" @click="closeModal">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

          <form class="modal-body-custom" @submit.prevent="saveLeaveType">
            <div class="field">
              <label>Tên loại nghỉ <span class="req">*</span></label>
              <input
                v-model="formData.typeName"
                type="text"
                class="field-input name-input"
                placeholder="Ví dụ: Nghỉ phép năm, Nghỉ ốm..."
              />
            </div>

            <label class="paid-toggle">
              <input v-model="formData.isPaid" type="checkbox" />
              <span class="paid-toggle-box">
                <i class="bi" :class="formData.isPaid ? 'bi-check-circle-fill' : 'bi-dash-circle'"></i>
                {{ formData.isPaid ? 'Loại nghỉ này có hưởng lương' : 'Loại nghỉ này không hưởng lương' }}
              </span>
            </label>

            <div class="modal-footer-custom modal-footer-inline">
              <button type="button" class="btn-ghost" @click="closeModal">Hủy</button>
              <button type="submit" class="btn-primary" :disabled="submitting">
                <span v-if="submitting" class="spin-sm me-2"></span>
                {{ modalMode === 'add' ? 'Tạo loại nghỉ' : 'Lưu thay đổi' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import leaveTypeService from '@/services/leavetypeservice.service.js'
import { useAuthStore } from '@/stores/auth.store.js'
import { confirmDialog } from '@/composables/useConfirmDialog'
import { useToast } from '@/utils/toast'

const auth = useAuthStore()
const toast = useToast()

const canCreate = computed(() => auth.can('LEAVE_TYPE_CREATE'))
const canEdit = computed(() => auth.can('LEAVE_TYPE_UPDATE'))
const canDelete = computed(() => auth.can('LEAVE_TYPE_DELETE'))

const leaveTypes = ref([])
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const modalMode = ref('add')
let editingId = null

const formData = reactive({
  typeName: '',
  isPaid: true
})

const resetForm = () => {
  editingId = null
  Object.assign(formData, { typeName: '', isPaid: true })
}

const fetchLeaveTypes = async () => {
  try {
    loading.value = true
    const response = await leaveTypeService.getAll()
    leaveTypes.value = Array.isArray(response.data) ? response.data : response.data?.data || []
  } catch (error) {
    console.error('Lỗi API GET:', error)
    leaveTypes.value = []
    toast.error(error, 'Không thể tải danh mục loại nghỉ.')
  } finally {
    loading.value = false
  }
}

onMounted(fetchLeaveTypes)

const openModal = (mode, typeData = null) => {
  modalMode.value = mode

  if (mode === 'edit' && typeData) {
    editingId = typeData.leaveTypeId
    Object.assign(formData, {
      typeName: typeData.typeName,
      isPaid: Boolean(typeData.isPaid)
    })
  } else {
    resetForm()
  }

  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const saveLeaveType = async () => {
  if (!formData.typeName.trim()) {
    toast.warning('Vui lòng nhập tên loại nghỉ.')
    return
  }

  try {
    submitting.value = true
    const payload = {
      typeName: formData.typeName.trim(),
      isPaid: Boolean(formData.isPaid)
    }

    if (modalMode.value === 'add') {
      await leaveTypeService.create(payload)
      toast.success('Đã thêm loại nghỉ phép mới.')
    } else {
      await leaveTypeService.update(editingId, payload)
      toast.success('Đã cập nhật loại nghỉ phép.')
    }

    closeModal()
    await fetchLeaveTypes()
  } catch (error) {
    console.error('Lỗi khi lưu:', error)
    toast.error(error, 'Không thể lưu loại nghỉ phép.')
  } finally {
    submitting.value = false
  }
}

const deleteType = async (type) => {
  const confirmed = await confirmDialog({
    title: 'Xóa loại nghỉ phép',
    message: `Bạn có chắc chắn muốn xóa "${type.typeName}"?`,
    confirmText: 'Xóa loại nghỉ',
    cancelText: 'Hủy',
    variant: 'danger',
    icon: 'bi bi-trash3'
  })

  if (!confirmed) return

  try {
    await leaveTypeService.delete(type.leaveTypeId)
    toast.success('Đã xóa loại nghỉ phép.')
    await fetchLeaveTypes()
  } catch (error) {
    console.error('Lỗi khi xóa:', error)
    toast.error(error, 'Không thể xóa loại nghỉ phép.')
  }
}
</script>

<style scoped>
.leave-types-page {
  gap: 24px;
}

.type-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.small-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 16px;
}

.name-input {
  padding-left: 14px;
}

.paid-toggle {
  display: block;
  cursor: pointer;
}

.paid-toggle input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.paid-toggle-box {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid #e4e7ec;
  border-radius: 14px;
  background: #f8fafc;
  color: #344054;
  font-size: 14px;
  font-weight: 600;
}

.paid-toggle-box i {
  color: #465fff;
  font-size: 16px;
}

.modal-footer-inline {
  padding: 0 !important;
  border-top: 0 !important;
}

.spin-lg {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
