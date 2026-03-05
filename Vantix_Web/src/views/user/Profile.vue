<template>
  <div class="container py-5">
    <h3 class="fw-bold text-center mb-4">
      👤 My Profile
    </h3>

    <div class="row g-4">
      <div class="col-lg-4">
        <div class="card profile-card text-center p-4">

          <div class="avatar-wrapper mx-auto" @click="openFilePicker">
            <img
                :src="avatarUrl"
                @error="setDefaultAvatar"
                class="avatar-lg"
            />
            <div class="avatar-overlay">
              📷 Change
            </div>
          </div>

          <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="d-none"
              @change="uploadAvatar"
          />

          <h5 class="mt-3 mb-1 fw-semibold">
            {{ profile.fullName }}
          </h5>

          <p class="text-muted mb-2">
            {{ profile.position || 'Chưa cập nhật' }}
          </p>

          <span class="badge bg-primary mb-3">
            {{ profile.department || 'Chưa cập nhật' }}
          </span>

          <hr />

          <div class="small text-muted">
            <div>{{ profile.email }}</div>
            <div>{{ profile.phone }}</div>
          </div>

        </div>
      </div>

      <div class="col-lg-8">
        <div class="card profile-form p-4">
          <h5 class="fw-bold mb-4">Edit Information</h5>

          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary"></div>
            <p class="mt-2">Loading profile...</p>
          </div>

          <form v-else @submit.prevent="updateProfile">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Username</label>
                <input class="form-control" v-model="profile.username" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Email (Không thể đổi)</label>
                <input class="form-control" v-model="profile.email" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Full Name</label>
                <input class="form-control" v-model="profile.fullName" required />
              </div>

              <div class="col-md-6">
                <label class="form-label">Phone</label>
                <input class="form-control" v-model="profile.phone" />
              </div>

              <div class="col-12">
                <label class="form-label">Address</label>
                <input class="form-control" v-model="profile.address" />
              </div>

              <div class="col-md-6">
                <label class="form-label">Birth Date</label>
                <input type="date" class="form-control" v-model="profile.birthDate" />
              </div>

              <div class="col-md-6">
                <label class="form-label">Gender</label>
                <select class="form-select" v-model="profile.gender">
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div class="col-md-6">
                <label class="form-label">Department</label>
                <input class="form-control" :value="profile.department" disabled />
              </div>

              <div class="col-md-6">
                <label class="form-label">Position</label>
                <input class="form-control" :value="profile.position" disabled />
              </div>
            </div>

            <div class="text-end mt-4 d-flex justify-content-end align-items-center">
              <span v-if="success" class="text-success me-3 fw-semibold">
                ✔ Updated successfully!
              </span>
              <button type="submit" class="btn btn-primary px-4">
                💾 Update Profile
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
import ProfileService from '@/services/profileservice.service.js'

// State
const loading = ref(true)
const success = ref(false)
const fileInput = ref(null)

const defaultAvatar = 'https://via.placeholder.com/150'
const avatarUrl = ref(defaultAvatar)

// Tạo biến lưu ID để dùng khi upload ảnh hoặc update thông tin
const currentEmployeeId = ref(null)

// Object chứa dữ liệu form
const profile = reactive({
  username: '', email: '', fullName: '', phone: '',
  address: '', birthDate: '', gender: 'OTHER',
  department: '', position: '', avatarUrl: ''
})

// 1. Load dữ liệu từ Backend bằng Token (không cần truyền ID)
const loadProfile = async () => {
  try {
    loading.value = true

    // Gọi API /me
    const res = await ProfileService.getMyProfile()

    // Đổ dữ liệu từ API vào object profile
    Object.assign(profile, res.data)

    // Gán ID lấy được vào biến để dùng cho các hàm bên dưới
    currentEmployeeId.value = res.data.employeeId

    // Xử lý ảnh đại diện
    avatarUrl.value = res.data.avatarUrl || defaultAvatar
  } catch (error) {
    console.error("Lỗi lấy thông tin:", error)
    alert('Không thể tải hồ sơ. Vui lòng đăng nhập lại!')
  } finally {
    loading.value = false
  }
}

// 2. Mở hộp thoại chọn file
const openFilePicker = () => {
  fileInput.value.click()
}

// 3. Upload file ảnh
const uploadAvatar = async (event) => {
  const file = event.target.files[0]
  if (!file || !currentEmployeeId.value) return

  try {
    // Truyền currentEmployeeId.value vào API
    const res = await ProfileService.uploadAvatar(currentEmployeeId.value, file)
    avatarUrl.value = res.data // Cập nhật ảnh ngay lập tức
    alert('Cập nhật ảnh đại diện thành công!')
  } catch (error) {
    console.error("Lỗi upload ảnh:", error)
    alert('Không thể tải ảnh lên.')
  } finally {
    event.target.value = '' // Reset input file
  }
}

// Xử lý lỗi load ảnh (nếu link die)
const setDefaultAvatar = () => {
  avatarUrl.value = defaultAvatar
}

// 4. Lưu thông tin Profile
const updateProfile = async () => {
  if (!currentEmployeeId.value) return;

  try {
    // Truyền currentEmployeeId.value vào API thay vì để trống
    await ProfileService.updateContactInfo(currentEmployeeId.value, profile)

    success.value = true
    setTimeout(() => {
      success.value = false
    }, 3000) // Tắt thông báo thành công sau 3s

  } catch (error) {
    console.error("Lỗi lưu thông tin:", error)
    alert('Cập nhật thất bại.')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-card, .profile-form {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
  background-color: #fff;
}

/* Avatar Styles */
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid #f8f9fa;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  margin-bottom: 1rem;
}

.avatar-lg {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 0.85rem;
  padding: 8px 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.badge {
  font-size: 0.85rem;
  padding: 0.5em 0.75em;
}
</style>