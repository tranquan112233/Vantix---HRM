<template>
  <div class="leave-types-page">

    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <div class="title-icon">🏷️</div>
        <div>
          <h1 class="page-title">Danh mục Loại nghỉ phép</h1>
          <p class="page-description">Quản lý các loại nghỉ phép trong hệ thống</p>
        </div>
      </div>
      <button v-if="canCreate" class="btn-primary-grad" @click="openModal('add')">
        <span>＋</span> Thêm loại mới
      </button>
    </div>

    <!-- Table Card -->
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width: 80px">ID</th>
            <th>Tên loại nghỉ phép</th>
            <th class="text-center">Hưởng lương</th>
            <th class="text-right">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="type in leaveTypes" :key="type.leaveTypeId" class="table-row">
            <td class="id-cell">#{{ type.leaveTypeId }}</td>
            <td class="name-cell">{{ type.typeName }}</td>
            <td class="text-center">
              <span v-if="type.isPaid" class="status-badge paid">✓ Có lương</span>
              <span v-else class="status-badge unpaid">✗ Không lương</span>
            </td>
            <td class="text-right">
              <div class="action-group">
                <button v-if="canEdit" class="btn-icon edit" @click="openModal('edit', type)" title="Chỉnh sửa">
                  ✏️
                </button>
                <button v-if="canDelete" class="btn-icon delete" @click="deleteType(type.leaveTypeId)" title="Xóa">
                  🗑️
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="leaveTypes.length === 0">
            <td colspan="4" class="empty-cell">
              <div class="empty-state">
                <div class="empty-icon">🏷️</div>
                <p>Chưa có loại nghỉ phép nào.</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ modalMode === 'add' ? '✨ Thêm Mới' : '✏️ Cập Nhật' }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveLeaveType">
          <div class="form-group">
            <label>Tên loại nghỉ <span class="required">*</span></label>
            <input type="text" class="form-input" v-model="formData.typeName" placeholder="VD: Nghỉ ốm, Nghỉ phép năm..." required />
          </div>
          <div class="form-group">
            <label class="toggle-label">
              <div class="toggle-wrapper">
                <input type="checkbox" v-model="formData.isPaid" class="toggle-checkbox" />
                <span class="toggle-track"></span>
              </div>
              <span>Loại nghỉ này <strong>có hưởng lương</strong></span>
            </label>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn-ghost" @click="closeModal">Hủy bỏ</button>
            <button type="submit" class="btn-primary-grad">
              💾 Lưu lại
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import leaveTypeService from '@/services/leavetypeservice.service.js'
import { useAuthStore } from '@/stores/auth.store.js'

const auth = useAuthStore()
const canCreate = computed(() => auth.can('LEAVE_TYPE_CREATE'))
const canEdit   = computed(() => auth.can('LEAVE_TYPE_UPDATE'))
const canDelete = computed(() => auth.can('LEAVE_TYPE_DELETE'))

const leaveTypes = ref([])
const showModal = ref(false)
const modalMode = ref('add')
let editingId = null

const formData = reactive({
  typeName: '',
  isPaid: true
})

const fetchLeaveTypes = async () => {
  try {
    const response = await leaveTypeService.getAll()
    leaveTypes.value = response.data
  } catch (error) {
    console.error('Lỗi API GET:', error)
  }
}

onMounted(() => fetchLeaveTypes())

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

const saveLeaveType = async () => {
  try {
    if (modalMode.value === 'add') {
      await leaveTypeService.create(formData)
    } else {
      await leaveTypeService.update(editingId, formData)
    }
    closeModal()
    fetchLeaveTypes()
  } catch (error) {
    console.error('Lỗi khi lưu:', error)
    alert(error.response?.data || 'Có lỗi xảy ra!')
  }
}

const deleteType = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa không?')) {
    try {
      await leaveTypeService.delete(id)
      fetchLeaveTypes()
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

* { box-sizing: border-box; margin: 0; padding: 0; }

.leave-types-page {
  padding: 28px 32px;
  min-height: 100vh;
  background: #f0f4ff;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

/* ── Header ── */
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
  font-size: 22px;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  flex-shrink: 0;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.page-description {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

/* ── Buttons ── */
.btn-primary-grad {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
  transition: all 0.2s;
}

.btn-primary-grad:hover {
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.btn-ghost {
  padding: 10px 20px;
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-ghost:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-icon {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-icon.edit { background: #ede9fe; }
.btn-icon.edit:hover { background: #ddd6fe; transform: scale(1.1); }
.btn-icon.delete { background: #fee2e2; }
.btn-icon.delete:hover { background: #fecaca; transform: scale(1.1); }

/* ── Table Card ── */
.table-card {
  background: white;
  border-radius: 16px;
  border: 1.5px solid #e8edff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
}

.data-table th {
  background: #fafbff;
  padding: 13px 18px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  border-bottom: 1.5px solid #e8edff;
}

.data-table td {
  padding: 14px 18px;
  font-size: 14px;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.table-row:last-child td { border-bottom: none; }

.table-row:hover { background: #fafbff; }

.text-center { text-align: center; }
.text-right { text-align: right; }

.id-cell { color: #94a3b8; font-size: 13px; font-weight: 600; }
.name-cell { font-weight: 600; color: #1e293b; }

.action-group { display: flex; justify-content: flex-end; gap: 6px; }

/* ── Status Badges ── */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.paid { background: #d1fae5; color: #065f46; }
.status-badge.unpaid { background: #fee2e2; color: #991b1b; }

/* ── Empty State ── */
.empty-cell { padding: 48px 0 !important; }
.empty-state { text-align: center; color: #94a3b8; }
.empty-icon { font-size: 36px; margin-bottom: 10px; }
.empty-state p { font-size: 14px; }

/* ── Modal ── */
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-box {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  animation: modalIn 0.25s ease-out;
}

@keyframes modalIn {
  from { transform: translateY(-16px) scale(0.97); opacity: 0; }
  to   { transform: translateY(0) scale(1); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-header h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.close-btn {
  background: #f1f5f9;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.close-btn:hover { background: #e2e8f0; color: #dc2626; }

.modal-body {
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group { display: flex; flex-direction: column; gap: 6px; }

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.required { color: #dc2626; margin-left: 2px; }

.form-input {
  padding: 10px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  color: #1e293b;
  background: #fafbff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #6366f1;
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

/* Toggle Switch */
.toggle-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-size: 14px;
  color: #334155;
  user-select: none;
}

.toggle-wrapper { position: relative; display: inline-flex; align-items: center; }
.toggle-checkbox { position: absolute; opacity: 0; width: 0; height: 0; }

.toggle-track {
  width: 42px;
  height: 24px;
  background: #e2e8f0;
  border-radius: 12px;
  position: relative;
  transition: background 0.2s;
  display: block;
}

.toggle-track::after {
  content: '';
  position: absolute;
  top: 3px; left: 3px;
  width: 18px; height: 18px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  transition: transform 0.2s;
}

.toggle-checkbox:checked + .toggle-track { background: #6366f1; }
.toggle-checkbox:checked + .toggle-track::after { transform: translateX(18px); }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 4px;
}
</style>
