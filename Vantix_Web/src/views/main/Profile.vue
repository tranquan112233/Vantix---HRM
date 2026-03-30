<template>
  <div class="profile-container">
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Đang tải hồ sơ...</p>
    </div>

    <div v-else-if="profile" class="profile-layout">
      <div class="profile-card sidebar-card">
        <div class="avatar-section">
          <div class="avatar-large">{{ getInitials(profile.fullName) }}</div>
          <h2 class="profile-name">{{ profile.fullName }}</h2>
          <span class="job-title">{{ profile.positionName }}</span>
          <span :class="['status-badge', profile.workStatus === 'WORKING' ? 'working' : 'resigned']">
            {{ profile.workStatus === 'WORKING' ? 'Đang làm việc' : 'Đã nghỉ việc' }}
          </span>
        </div>
      </div>

      <div class="profile-card details-card">
        <h3 class="section-title">👤 Thông tin cá nhân</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">Họ và tên</span>
            <span class="value">{{ profile.fullName }}</span>
          </div>
          <div class="info-item">
            <span class="label">Email liên hệ</span>
            <span class="value">{{ profile.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">Số điện thoại</span>
            <span class="value">{{ profile.phone || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Giới tính</span>
            <span class="value">{{ translateGender(profile.gender) }}</span>
          </div>
          <div class="info-item">
            <span class="label">Ngày sinh</span>
            <span class="value">{{ formatDate(profile.birthDate) }}</span>
          </div>
          <div class="info-item full-width">
            <span class="label">Địa chỉ</span>
            <span class="value">{{ profile.address || 'Chưa cập nhật' }}</span>
          </div>
        </div>

        <hr class="divider" />

        <h3 class="section-title">🏢 Thông tin công việc</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">Mã nhân viên</span>
            <span class="value fw-bold">EMP-{{ profile.employeeId }}</span>
          </div>
          <div class="info-item">
            <span class="label">Phòng ban</span>
            <span class="value">{{ profile.departmentName }}</span>
          </div>
          <div class="info-item">
            <span class="label">Chức vụ</span>
            <span class="value">{{ profile.positionName }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import EmployeeService from "@/services/employee.service.js";

const profile = ref(null)
const loading = ref(true)

const fetchProfile = async () => {
  try {
    loading.value = true
    const response = await EmployeeService.getMyProfile()
    profile.value = response.data
  } catch (error) {
    console.error('Lỗi lấy hồ sơ:', error)
    alert('Không thể tải hồ sơ cá nhân!')
  } finally {
    loading.value = false
  }
}

// Hàm lấy chữ cái đầu làm Avatar
const getInitials = (name) => {
  if (!name) return 'U'
  const parts = name.trim().split(' ')
  return parts[parts.length - 1].charAt(0).toUpperCase()
}

// Hàm dịch giới tính
const translateGender = (gender) => {
  if (gender === 'MALE') return 'Nam'
  if (gender === 'FEMALE') return 'Nữ'
  return 'Khác'
}

// Format ngày tháng (YYYY-MM-DD -> DD/MM/YYYY)
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa cập nhật'
  const [year, month, day] = dateString.split('-')
  return `${day}/${month}/${year}`
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 15px;
  font-family: 'Inter', sans-serif;
  color: #111827;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 40vh;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.profile-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03);
  border: 1px solid #e5e7eb;
}

/* Sidebar */
.sidebar-card {
  flex: 1;
  min-width: 280px;
  padding: 40px 20px;
  text-align: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5, #818cf8);
  color: white;
  font-size: 3rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.3);
}

.profile-name { margin: 0 0 8px 0; font-size: 1.5rem; font-weight: 700; }
.job-title { color: #6b7280; margin-bottom: 16px; font-weight: 500; }

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}
.status-badge.working { background-color: #d1fae5; color: #065f46; }
.status-badge.resigned { background-color: #fee2e2; color: #991b1b; }

/* Details Card */
.details-card {
  flex: 2;
  padding: 30px 40px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.full-width { grid-column: span 2; }

.label { font-size: 0.85rem; color: #6b7280; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
.value { font-size: 1rem; color: #111827; font-weight: 500; }
.fw-bold { font-weight: 700; color: #4f46e5; }

.divider { border: 0; border-top: 1px solid #e5e7eb; margin: 30px 0; }

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; }
  .sidebar-card, .details-card { width: 100%; box-sizing: border-box; }
  .info-grid { grid-template-columns: 1fr; }
  .full-width { grid-column: span 1; }
}
</style>