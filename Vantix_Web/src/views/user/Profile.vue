<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card shadow">
          <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Hồ Sơ Của Tôi</h4>
          </div>
          <div class="card-body" v-if="profile">
            <div class="row">
              <div class="col-md-4 text-center border-end">
                <img :src="'http://localhost:8080' + profile.avatarUrl"
                     alt="Avatar"
                     class="rounded-circle img-fluid mb-3"
                     style="width: 150px; height: 150px; object-fit: cover; border: 3px solid #ddd;">

                <input type="file" ref="fileInput" @change="handleFileUpload" accept="image/*" class="d-none">
                <button class="btn btn-sm btn-outline-secondary" @click="triggerFileInput">
                  Đổi ảnh đại diện
                </button>
              </div>

              <div class="col-md-8">
                <div class="row mb-3">
                  <div class="col-sm-4 fw-bold">Họ và tên:</div>
                  <div class="col-sm-8">{{ profile.fullName }}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-sm-4 fw-bold">Tên đăng nhập:</div>
                  <div class="col-sm-8">{{ profile.username }}</div>
                </div>
                <div class="row mb-3">
                  <div class="col-sm-4 fw-bold">Phòng ban:</div>
                  <div class="col-sm-8">{{ profile.departmentName || 'Chưa phân bổ' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import ProfileService from '@/services/profileservice.service.js';
import apiClient from '@/services/axios.js'; // Dùng cấu hình axios chung

const profile = ref(null);
const fileInput = ref(null);

// Load Profile
const loadProfile = async () => {
  try {
    const response = await ProfileService.getMyProfile();
    profile.value = response.data;
  } catch (error) {
    console.error("Lỗi tải profile:", error);
  }
};

onMounted(() => {
  loadProfile();
});

// Mở cửa sổ chọn file
const triggerFileInput = () => {
  fileInput.value.click();
};

// Xử lý khi user chọn ảnh xong
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    // Gọi API upload (cấu hình apiClient tự đính token rồi)
    const response = await apiClient.post('/profile/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    alert("Cập nhật ảnh thành công!");

    // Tạo chuỗi ngẫu nhiên để tránh trình duyệt cache ảnh cũ
    const randomString = new Date().getTime();
    profile.value.avatarUrl = response.data.url + "?t=" + randomString;

  } catch (error) {
    console.error("Lỗi upload ảnh:", error);
    alert("Cập nhật ảnh thất bại!");
  }
};
</script>