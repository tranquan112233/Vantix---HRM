<template>
  <div class="profile-page mgmt-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">Hồ sơ của tôi</h2>
        <p class="page-desc">Thông tin cá nhân và vị trí hiện tại của tài khoản đang đăng nhập.</p>
      </div>
    </div>

    <div v-if="loading" class="state-center table-card">
      <div class="spin-lg mb-3"></div>
      <div class="empty-title">Đang tải hồ sơ</div>
      <div class="empty-sub">Hệ thống đang lấy thông tin cá nhân của bạn.</div>
    </div>

    <div v-else-if="errorMessage" class="state-center table-card">
      <i class="bi bi-exclamation-octagon empty-icon"></i>
      <div class="empty-title">Không thể tải hồ sơ</div>
      <div class="empty-sub">{{ errorMessage }}</div>
      <button class="btn-primary mt-2" @click="fetchProfile">
        <i class="bi bi-arrow-repeat"></i> Tải lại
      </button>
    </div>

    <div v-else-if="profile" class="profile-layout">
      <aside class="sidebar-card profile-sidebar">
        <div class="avatar-section">
          <div class="profile-avatar" :style="{ background: `linear-gradient(135deg, ${stringToColor(profile.fullName)}, #6f86ff)` }">
            {{ getInitials(profile.fullName) }}
          </div>
          <h3 class="profile-name">{{ profile.fullName }}</h3>
          <p class="profile-position">{{ profile.positionName || 'Chưa cập nhật chức vụ' }}</p>
          <p class="profile-dept">
            <i class="bi bi-building"></i>
            {{ profile.departmentName || 'Chưa cập nhật phòng ban' }}
          </p>
          <span :class="['status-badge', profile.workStatus === 'WORKING' ? 'working' : 'resigned']">
            {{ profile.workStatus === 'WORKING' ? 'Đang làm việc' : 'Đã nghỉ việc' }}
          </span>
        </div>

        <div class="sidebar-divider"></div>

        <div class="sidebar-meta">
          <div class="meta-row">
            <span class="meta-label"><i class="bi bi-envelope"></i> Email</span>
            <span class="meta-value">{{ profile.email || 'Chưa cập nhật' }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label"><i class="bi bi-telephone"></i> Điện thoại</span>
            <span class="meta-value">{{ profile.phone || 'Chưa cập nhật' }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label"><i class="bi bi-geo-alt"></i> Địa chỉ</span>
            <span class="meta-value">{{ profile.address || 'Chưa cập nhật' }}</span>
          </div>
        </div>
      </aside>

      <div class="profile-details">
        <section class="info-card">
          <div class="info-card-header">
            <i class="bi bi-person-badge"></i>
            <span>Thông tin cá nhân</span>
          </div>
          <div class="info-grid">
            <div class="info-cell">
              <span class="info-label">Họ và tên</span>
              <span class="info-value">{{ profile.fullName }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Giới tính</span>
              <span class="info-value">{{ translateGender(profile.gender) }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Ngày sinh</span>
              <span class="info-value">{{ formatDate(profile.birthDate) }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Email liên hệ</span>
              <span class="info-value">{{ profile.email || 'Chưa cập nhật' }}</span>
            </div>
            <div class="info-cell span-2">
              <span class="info-label">Địa chỉ</span>
              <span class="info-value">{{ profile.address || 'Chưa cập nhật' }}</span>
            </div>
          </div>
        </section>

        <section class="info-card">
          <div class="info-card-header">
            <i class="bi bi-briefcase"></i>
            <span>Thông tin công việc</span>
          </div>
          <div class="info-grid">
            <div class="info-cell">
              <span class="info-label">Mã nhân viên</span>
              <span class="info-value">EMP-{{ profile.employeeId }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Trạng thái</span>
              <span class="info-value">
                {{ profile.workStatus === 'WORKING' ? 'Đang làm việc' : 'Đã nghỉ việc' }}
              </span>
            </div>
            <div class="info-cell">
              <span class="info-label">Phòng ban</span>
              <span class="info-value">{{ profile.departmentName || 'Chưa cập nhật' }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Chức vụ</span>
              <span class="info-value">{{ profile.positionName || 'Chưa cập nhật' }}</span>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import EmployeeService from "@/services/employee.service.js";
import { useToast } from "@/utils/toast";

const profile = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const toast = useToast()

const fetchProfile = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    const response = await EmployeeService.getMyProfile()
    profile.value = response.data
  } catch (error) {
    console.error('Lỗi lấy hồ sơ:', error)
    profile.value = null
    errorMessage.value = 'Hồ sơ cá nhân hiện chưa tải được. Vui lòng thử lại sau.'
    toast.error(error, 'Không thể tải hồ sơ cá nhân.')
  } finally {
    loading.value = false
  }
}

const getInitials = (name) => {
  if (!name) return 'N'
  const parts = name.trim().split(' ')
  return parts[parts.length - 1].charAt(0).toUpperCase()
}

const stringToColor = (str = 'Người dùng') => {
  let hash = 0
  for (let i = 0; i < str.length; i += 1) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return `hsl(${hash % 360}, 72%, 56%)`
}

const translateGender = (gender) => {
  if (gender === 'MALE') return 'Nam'
  if (gender === 'FEMALE') return 'Nữ'
  return 'Khác'
}

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa cập nhật'
  const [year, month, day] = dateString.split('-')
  return `${day}/${month}/${year}`
}

onMounted(fetchProfile)
</script>

<style scoped>
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
