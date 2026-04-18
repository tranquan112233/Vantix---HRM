<template>
  <div class="kpi-dashboard mgmt-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">Xếp hạng KPI</h2>
        <p class="page-desc">Theo dõi điểm số và số công việc hoàn thành của nhân viên theo từng tháng.</p>
      </div>
      <div class="filter-box">
        <span class="fw-bold text-muted small me-2">Tháng:</span>
        <div class="select-wrap">
          <select v-model="selectedMonth" @change="fetchData">
            <option value="">Toàn thời gian</option>
            <option v-for="m in 12" :key="m" :value="m">Tháng {{ m }}</option>
          </select>
          <i class="bi bi-chevron-down"></i>
        </div>
      </div>
    </div>

    <div v-if="loading" class="state-center table-card mt-4">
      <div class="spin-lg mb-3"></div>
      <div class="fw-bold text-muted">Đang tính toán điểm số...</div>
    </div>

    <div v-else-if="errorMessage" class="state-center table-card mt-4">
      <i class="bi bi-exclamation-octagon empty-icon"></i>
      <div class="empty-title">Không thể tải bảng xếp hạng</div>
      <div class="empty-sub">{{ errorMessage }}</div>
      <button class="btn-primary mt-2" @click="fetchData">
        <i class="bi bi-arrow-repeat"></i> Tải lại
      </button>
    </div>

    <div v-else-if="rankingData.length === 0" class="state-center table-card mt-4">
      <i class="bi bi-bar-chart empty-icon"></i>
      <div class="empty-title">Chưa có dữ liệu thi đua</div>
      <div class="empty-sub">Chưa có ai hoàn thành công việc nào trong thời gian này.</div>
    </div>

    <div v-else class="mt-4">
      <div class="podium-container mb-5">
        <div v-if="rankingData[1]" class="podium-item top-2 animate__animated animate__fadeInUp">
          <div class="medal">🥈</div>
          <div class="avatar-lg shadow" :style="{ background: stringToColor(rankingData[1].employeeName) }">
            {{ rankingData[1].employeeName.charAt(0).toUpperCase() }}
          </div>
          <h5 class="fw-bold mt-3 mb-1">{{ rankingData[1].employeeName }}</h5>
          <div class="points-badge silver">{{ rankingData[1].totalPoints }} điểm</div>
          <div class="task-count mt-2">✔ {{ rankingData[1].completedTasks }} công việc</div>
          <div class="podium-base base-2">#2</div>
        </div>

        <div v-if="rankingData[0]" class="podium-item top-1 animate__animated animate__fadeInUp" style="animation-delay: 0.1s;">
          <div class="medal-crown">👑</div>
          <div class="avatar-xl shadow-lg border-gold" :style="{ background: stringToColor(rankingData[0].employeeName) }">
            {{ rankingData[0].employeeName.charAt(0).toUpperCase() }}
          </div>
          <h4 class="fw-bold mt-3 mb-1 text-gold">{{ rankingData[0].employeeName }}</h4>
          <div class="points-badge gold">{{ rankingData[0].totalPoints }} điểm</div>
          <div class="task-count mt-2">✔ {{ rankingData[0].completedTasks }} công việc</div>
          <div class="podium-base base-1 shadow-lg">#1</div>
        </div>

        <div v-if="rankingData[2]" class="podium-item top-3 animate__animated animate__fadeInUp" style="animation-delay: 0.2s;">
          <div class="medal">🥉</div>
          <div class="avatar-lg shadow" :style="{ background: stringToColor(rankingData[2].employeeName) }">
            {{ rankingData[2].employeeName.charAt(0).toUpperCase() }}
          </div>
          <h5 class="fw-bold mt-3 mb-1">{{ rankingData[2].employeeName }}</h5>
          <div class="points-badge bronze">{{ rankingData[2].totalPoints }} điểm</div>
          <div class="task-count mt-2">✔ {{ rankingData[2].completedTasks }} công việc</div>
          <div class="podium-base base-3">#3</div>
        </div>
      </div>

      <div v-if="rankingData.length > 3" class="table-card shadow-sm animate__animated animate__fadeIn">
        <div class="p-3 border-bottom bg-light">
          <h6 class="fw-bold m-0 text-muted">BẢNG XẾP HẠNG CHUNG</h6>
        </div>
        <table>
          <thead>
          <tr>
            <th class="text-center" style="width: 80px;">Hạng</th>
            <th>Nhân viên</th>
            <th class="text-center">Số công việc đã xong</th>
            <th class="text-right">Tổng Điểm</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(user, index) in rankingData.slice(3)" :key="user.employeeId">
            <td class="text-center fw-bold text-muted">#{{ index + 4 }}</td>
            <td>
              <div class="employee-cell">
                <div class="avatar" :style="{ background: stringToColor(user.employeeName) }">
                  {{ user.employeeName.charAt(0).toUpperCase() }}
                </div>
                <span class="employee-name">{{ user.employeeName }}</span>
              </div>
            </td>
            <td class="text-center">
              <span class="status-badge completed">✔ {{ user.completedTasks }}</span>
            </td>
            <td class="text-right fw-bold" style="color: #6366f1;">
              {{ user.totalPoints }} điểm
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
import { useToast } from "@/utils/toast";

const rankingData = ref([]);
const selectedMonth = ref("");
const loading = ref(false);
const errorMessage = ref("");
const toast = useToast();

// Hàm tạo màu ngẫu nhiên cho Avatar
const stringToColor = (str) => {
  if (!str) return '#ccc';
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return `hsl(${hash % 360}, 70%, 60%)`;
};

const fetchData = async () => {
  loading.value = true;
  errorMessage.value = "";
  try {
    // Gọi song song API lấy Bảng xếp hạng và API lấy danh sách NV để map tên
    const [rankRes, empRes] = await Promise.all([
      taskService.getRanking(selectedMonth.value),
      taskService.getEmployees()
    ]);

    // Bóc dữ liệu Nhân viên
    let employeeList = [];
    if (Array.isArray(empRes.data)) employeeList = empRes.data;
    else if (empRes.data && Array.isArray(empRes.data.data)) employeeList = empRes.data.data;
    else if (empRes.data && Array.isArray(empRes.data.content)) employeeList = empRes.data.content;

    // Bóc dữ liệu Ranking
    let rawRanking = Array.isArray(rankRes.data) ? rankRes.data : (rankRes.data?.data || []);

    // Map tên nhân viên vào mảng Ranking
    rankingData.value = rawRanking.map(rank => {
      const emp = employeeList.find(e => String(e.id || e.employeeId) === String(rank.employeeId));
      return {
        ...rank,
        employeeName: emp ? (emp.fullName || emp.name) : "Nhân viên vô danh"
      };
    });
  } catch (error) {
    console.error("❌ Lỗi lấy bảng xếp hạng:", error);
    rankingData.value = [];
    errorMessage.value = "Dữ liệu xếp hạng hiện chưa tải được. Vui lòng thử lại sau ít phút.";
    toast.error(error, "Không thể tải bảng xếp hạng KPI.");
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&display=swap');

.kpi-dashboard {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
}

/* Header & Filters */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title { font-size: 24px; font-weight: 700; color: #111; margin: 0; }
.page-desc { font-size: 13px; color: #888; margin-top: 4px; }

.filter-box { display: flex; align-items: center; }
.select-wrap { position: relative; }
.select-wrap select {
  padding: 8px 30px 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  color: #333;
  background: #fff;
  appearance: none;
  cursor: pointer;
  outline: none;
}
.select-wrap i {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  color: #aaa; font-size: 11px; font-style: normal; pointer-events: none;
}

/* Base Styles */
.table-card {
  background: #fff; border-radius: 12px; border: 1px solid #e8e8e8; overflow: hidden;
}

.state-center {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 20px; text-align: center;
}
.empty-icon { font-size: 40px; }
.empty-title { font-size: 16px; font-weight: 700; color: #333; margin-top: 10px; }
.empty-sub { font-size: 13px; color: #888; }

.spin-lg {
  width: 30px; height: 30px; border: 3px solid #f3f3f3; border-top: 3px solid #6366f1;
  border-radius: 50%; animation: spin 1s linear infinite;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* PODIUM (BỤC VINH QUANG) */
.podium-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 16px;
  margin-top: 40px;
  height: 350px;
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 180px;
  position: relative;
}

.medal, .medal-crown { font-size: 32px; margin-bottom: -15px; z-index: 2; text-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.medal-crown { font-size: 45px; margin-bottom: -20px; }

.avatar-lg {
  width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; color: white; font-size: 32px; font-weight: bold;
  border: 4px solid #fff; z-index: 1;
}

.avatar-xl {
  width: 100px; height: 100px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; color: white; font-size: 40px; font-weight: bold;
  border: 5px solid #fbbf24; z-index: 1;
}

.text-gold { color: #d97706; }

.points-badge {
  padding: 4px 12px; border-radius: 20px; font-weight: 700; font-size: 14px;
}
.points-badge.gold { background: #fef3c7; color: #d97706; border: 1px solid #fde68a; }
.points-badge.silver { background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }
.points-badge.bronze { background: #ffedd5; color: #c2410c; border: 1px solid #fed7aa; }

.task-count { font-size: 12px; color: #64748b; font-weight: 600; }

.podium-base {
  width: 100%; border-top-left-radius: 12px; border-top-right-radius: 12px;
  display: flex; justify-content: center; align-items: flex-start;
  padding-top: 15px; font-size: 24px; font-weight: bold; color: white;
  margin-top: 16px;
}

.base-1 { height: 160px; background: linear-gradient(135deg, #fbbf24, #d97706); }
.base-2 { height: 120px; background: linear-gradient(135deg, #cbd5e1, #94a3b8); }
.base-3 { height: 90px; background: linear-gradient(135deg, #fdba74, #ea580c); }

/* Table List (Hạng 4+) */
table { width: 100%; border-collapse: collapse; }
th {
  padding: 12px 16px; font-size: 12px; font-weight: 600; color: #888;
  text-transform: uppercase; border-bottom: 1px solid #eee; text-align: left;
}
td { padding: 12px 16px; vertical-align: middle; border-bottom: 1px solid #f5f5f5; }
tr:hover td { background: #fafafa; }

.employee-cell { display: flex; align-items: center; gap: 10px; }
.employee-name { font-weight: 600; color: #333; font-size: 14px; }
.avatar {
  width: 32px; height: 32px; border-radius: 8px; display: flex;
  align-items: center; justify-content: center; color: #fff; font-weight: bold; font-size: 14px;
}

.status-badge {
  display: inline-flex; align-items: center; padding: 4px 10px;
  border-radius: 20px; font-size: 12px; font-weight: 600;
}
.status-badge.completed { background: #f0fdf4; color: #16a34a; }

@media (max-width: 768px) {
  .podium-container { height: auto; align-items: center; flex-direction: column; gap: 30px; margin-top: 20px; }
  .podium-item { width: 100%; }
  .podium-base { border-radius: 12px; height: 60px; padding-top: 0; align-items: center; }
}
</style>
