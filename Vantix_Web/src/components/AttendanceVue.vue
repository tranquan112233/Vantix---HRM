<script setup>
import { ref } from 'vue';
import axios from 'axios';

// Khai báo state
const userID = ref(null);
const message = ref('');
const isError = ref(false);
const loading = ref(false);

const handleCheckIn = async () => {
  if (!userID.value) {
    message.value = "Vui lòng nhập ID nhân viên!";
    isError.value = true;
    return;
  }

  loading.value = true;
  message.value = '';

  try {
    // Gọi API đến Controller bạn đã viết
    // Lưu ý: Port mặc định của Spring Boot là 8080
    const response = await axios.post(`http://localhost:8080/api/attendance/create`,
        userID.value, // Gửi userID dưới dạng raw body như code Java bạn viết
        { headers: { 'Content-Type': 'application/json' } }
    );

    message.value = `Chấm công thành công! Giờ vào: ${response.data.checkIn}`;
    isError.value = false;
  } catch (error) {
    isError.value = true;
    message.value = error.response?.data || "Có lỗi xảy ra khi chấm công.";
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="attendance-container">
    <div class="card">
      <h1>HỆ THỐNG CHẤM CÔNG</h1>
      <p class="subtitle">Vantix HRM System</p>

      <div class="input-group">
        <label for="id">Mã số nhân viên (ID):</label>
        <input
            type="number"
            id="id"
            v-model="userID"
            placeholder="Ví dụ: 1"
            @keyup.enter="handleCheckIn"
        />
      </div>

      <button @click="handleCheckIn" :disabled="loading">
        {{ loading ? 'Đang xử lý...' : 'XÁC NHẬN CHẤM CÔNG' }}
      </button>

      <div v-if="message" :class="['alert', isError ? 'error' : 'success']">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.attendance-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

h1 { color: #2c3e50; margin-bottom: 0.5rem; }
.subtitle { color: #7f8c8d; margin-bottom: 2rem; }

.input-group {
  text-align: left;
  margin-bottom: 1.5rem;
}

label { display: block; margin-bottom: 0.5rem; font-weight: bold; }

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 14px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover { background-color: #2980b9; }
button:disabled { background-color: #bdc3c7; }

.alert {
  margin-top: 1.5rem;
  padding: 10px;
  border-radius: 6px;
}
.success { background-color: #d4edda; color: #155724; }
.error { background-color: #f8d7da; color: #721c24; }
</style>