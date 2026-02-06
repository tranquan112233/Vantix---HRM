<template>
  <AppHeader />

  <div class="profile-page">
    <div class="profile-card">

      <!-- Avatar -->
      <div class="avatar-section">
        <img
            :src="form.AvatarURL || defaultAvatar"
            class="avatar"
        />
        <input type="file" @change="onAvatarChange" />
      </div>

      <!-- Info -->
      <div class="info-section">
        <h2>Thông tin cá nhân</h2>

        <div class="form-group">
          <label>Mã nhân viên</label>
          <input type="text" v-model="form.EmployeeCode" disabled />
        </div>

        <div class="form-group">
          <label>Họ và tên</label>
          <input type="text" v-model="form.Fullname" />
        </div>

        <div class="form-group">
          <label>Giới tính</label>
          <select v-model="form.Gender">
            <option value="Male">Nam</option>
            <option value="Female">Nữ</option>
            <option value="Other">Khác</option>
          </select>
        </div>

        <div class="form-group">
          <label>Email</label>
          <input type="email" v-model="form.Email" />
        </div>

        <div class="form-group">
          <label>Số điện thoại</label>
          <input type="text" v-model="form.Phone" />
        </div>

        <div class="form-group">
          <label>Địa chỉ</label>
          <textarea v-model="form.Address"></textarea>
        </div>

        <button class="btn-save" @click="saveProfile">
          💾 Lưu thay đổi
        </button>
      </div>
    </div>
  </div>

  <AppFooter />
</template>

<script>
import AppHeader from "../AppHeader.vue"
import AppFooter from "../AppFooter.vue"

export default {
  name: "EmployeeProfile",
  components: { AppHeader, AppFooter },
  data() {
    return {
      defaultAvatar: "https://cdn-icons-png.flaticon.com/512/149/149071.png",
      form: {
        EmployeeCode: "EMP001",
        Fullname: "Lê Quốc Anh",
        Gender: "Male",
        Address: "Đà Nẵng",
        Email: "emp01@vantix.com",
        Phone: "0923456789",
        AvatarURL: ""
      }
    }
  },
  methods: {
    onAvatarChange(e) {
      const file = e.target.files[0]
      if (!file) return
      this.form.AvatarURL = URL.createObjectURL(file)
    },
    saveProfile() {
      console.log("SAVE PROFILE:", this.form)
      alert("Cập nhật thông tin thành công!")
      // TODO: call API PUT /employee/profile
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd, #f8fbff);
  display: flex;
  justify-content: center;
  align-items: center;
}

.profile-card {
  background: #fff;
  width: 900px;
  display: grid;
  grid-template-columns: 280px 1fr;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  overflow: hidden;
}

/* Avatar */
.avatar-section {
  background: #e3f2fd;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.avatar {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #2196f3;
  margin-bottom: 12px;
}

/* Info */
.info-section {
  padding: 30px;
}

.info-section h2 {
  margin-bottom: 20px;
  color: #0d47a1;
}

.form-group {
  margin-bottom: 14px;
}

label {
  display: block;
  font-size: 13px;
  margin-bottom: 6px;
  color: #455a64;
}

input, textarea, select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #cfd8dc;
  font-size: 14px;
}

textarea {
  resize: none;
}

input:disabled {
  background: #f5f5f5;
}

.btn-save {
  margin-top: 16px;
  padding: 12px;
  width: 100%;
  border: none;
  border-radius: 10px;
  background: #2196f3;
  color: white;
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-save:hover {
  background: #1976d2;
}
</style>
