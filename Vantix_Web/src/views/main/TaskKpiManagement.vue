<template>
  <div class="container mt-4 pb-5">
    <div class="text-center mb-4">
      <h1 class="fw-bold text-primary display-5 mb-2">🏆 BẢNG VÀNG KPI</h1>
      <p class="text-muted fs-5">Vinh danh những nhân viên xuất sắc nhất</p>
    </div>

    <div class="d-flex justify-content-center mb-5">
      <div class="d-flex align-items-center bg-white p-2 rounded-pill shadow-sm border" style="transition: 0.3s;">
        <span class="fw-bold text-muted ms-3 me-2">📅 Chọn tháng KPI:</span>
        <select v-model="selectedMonth" @change="loadRanking" class="form-select border-0 shadow-none fw-bold text-primary bg-transparent" style="width: 180px; cursor: pointer;">
          <option value="">🌟 Cả năm (All)</option>
          <option v-for="m in 12" :key="m" :value="m">Tháng {{ m }}</option>
        </select>
      </div>
    </div>

    <div class="card border-0 shadow-lg rounded-4 overflow-hidden">
      <div class="card-body p-0">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-primary text-white text-uppercase">
          <tr class="text-center">
            <th class="py-3" style="width: 15%">Xếp hạng</th>
            <th class="text-start py-3" style="width: 40%">Nhân viên</th>
            <th class="py-3">Task Hoàn Thành</th>
            <th class="py-3">Điểm Thưởng (Pts)</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="rankingData.length === 0">
            <td colspan="4" class="text-center py-5">
              <div class="fs-1 mb-2">📭</div>
              <h5 class="fw-bold text-muted">Chưa có dữ liệu KPI cho tháng này!</h5>
              <p class="text-secondary small m-0">Hãy phê duyệt các công việc để ghi nhận điểm cho nhân viên.</p>
            </td>
          </tr>

          <tr v-for="(user, index) in rankingData" :key="user.employeeId"
              class="ranking-row" :class="getRowClass(index)">

            <td class="text-center fs-3">
              <span v-if="index === 0" class="animate__animated animate__tada animate__infinite">🥇</span>
              <span v-else-if="index === 1">🥈</span>
              <span v-else-if="index === 2">🥉</span>
              <span v-else class="fw-bold text-muted fs-5">#{{ index + 1 }}</span>
            </td>

            <td class="text-start">
              <div class="d-flex align-items-center">
                <div class="avatar-circle me-3 fw-bold text-white shadow-sm" :class="getAvatarClass(index)">
                  {{ getInitials(user.employeeName) }}
                </div>
                <span class="fw-bold fs-5" :class="index < 3 ? 'text-dark' : 'text-secondary'">
                    {{ user.employeeName }}
                  </span>
              </div>
            </td>

            <td class="text-center fw-bold text-secondary fs-5">
              {{ user.completedTasks }}
            </td>

            <td class="text-center fw-bold fs-4" :class="index === 0 ? 'text-danger' : 'text-success'">
              {{ user.totalPoints }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import taskService from "@/services/taskApi.service";
import axios from "axios";

// Data xếp hạng
const rankingData = ref([]);

// Lấy tháng hiện tại làm mặc định (Ví dụ tháng 3)
const selectedMonth = ref(new Date().getMonth() + 1);

// Hàm tải dữ liệu
const loadRanking = async () => {
  try {
    // Gọi API lấy Nhân viên và Bảng xếp hạng cùng lúc (Có truyền selectedMonth để lọc)
    const [empRes, rankRes] = await Promise.all([
      axios.get("http://localhost:8080/api/employees"),
      taskService.getRanking(selectedMonth.value)
    ]);

    const employees = empRes.data;
    const ranks = rankRes.data;

    // Map tên nhân viên vào data xếp hạng
    rankingData.value = ranks.map(r => {
      const emp = employees.find(e => String(e.employeeId) === String(r.employeeId));
      return {
        ...r,
        employeeName: emp ? emp.fullName : "Nhân viên Ẩn danh"
      };
    });

  } catch (error) {
    console.error("Lỗi tải bảng xếp hạng:", error);
  }
};

// --- CÁC HÀM TRANG TRÍ GIAO DIỆN ---

// Lấy 2 chữ cái đầu của Tên (VD: Nguyễn Văn A -> NA)
const getInitials = (name) => {
  if (!name) return "NV";
  const parts = name.trim().split(" ");
  return parts.length > 1 ? parts[0][0] + parts[parts.length - 1][0] : name.substring(0, 2).toUpperCase();
};

// Đổi màu nền cho Top 1, 2, 3
const getRowClass = (index) => {
  if (index === 0) return 'table-warning top-1'; // Vàng nhạt
  if (index === 1) return 'table-light top-2';
  if (index === 2) return 'table-light top-3';
  return '';
};

// Đổi màu Avatar cho Top 1, 2, 3
const getAvatarClass = (index) => {
  if (index === 0) return 'bg-warning'; // Vàng
  if (index === 1) return 'bg-secondary'; // Bạc
  if (index === 2) return 'bg-danger'; // Đồng
  return 'bg-primary'; // Các hạng khác màu xanh
};

// Chạy hàm khi mở trang
onMounted(loadRanking);
</script>

<style scoped>
/* Trang trí Avatar tròn */
.avatar-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  text-transform: uppercase;
}

/* Hiệu ứng hover từng dòng */
.ranking-row {
  transition: all 0.3s ease;
  cursor: default;
}
.ranking-row:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  z-index: 10;
  position: relative;
}

/* Làm nổi bật Top 1 */
.top-1 { background-color: #fff9e6 !important; }
.top-1 td { font-weight: 800 !important; }

/* Làm đẹp thanh tiêu đề bảng */
thead th {
  background-color: #4f46e5;
  border-bottom: none;
  letter-spacing: 0.5px;
}

select:focus {
  outline: none;
  box-shadow: none;
}
</style>