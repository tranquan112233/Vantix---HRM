<template>
  <div class="container mt-4 pb-5">
    <div class="header-section mb-4 d-flex justify-content-between align-items-center">
      <div>
        <h2 class="fw-bold m-0 text-dark">🧑‍💻 My Tasks</h2>
        <p class="text-muted m-0">Hoàn thành 100% để nộp bài và ẩn công việc.</p>
      </div>
      <div class="badge bg-primary px-3 py-2 fs-6 shadow-sm rounded-pill">
        Đang thực hiện: {{ tasks.length }}
      </div>
    </div>

    <div v-if="tasks.length === 0" class="text-center p-5 border-0 rounded-4 bg-light shadow-sm">
      <div class="fs-1 mb-3">✅</div>
      <h4 class="fw-bold text-success">Bạn đã hoàn thành sạch sẽ!</h4>
      <p class="text-muted">Không còn công việc nào dưới 100%.</p>
    </div>

    <div v-else class="row">
      <div v-for="task in tasks" :key="task.taskId" class="col-md-4 mb-4">
        <div class="card h-100 shadow-sm border-0 task-card rounded-4 overflow-hidden">
          <div class="card-body d-flex flex-column p-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <h5 class="fw-bold text-primary mb-0">{{ task.taskTitle }}</h5>
              <span class="badge rounded-pill bg-info text-white">
                {{ task.progressPercent || 0 }}%
              </span>
            </div>

            <p class="text-muted small description-text flex-grow-1">
              {{ task.description || 'Nội dung chi tiết chưa cập nhật.' }}
            </p>

            <div class="progress mb-3" style="height: 8px; border-radius: 10px;">
              <div class="progress-bar bg-success progress-bar-striped progress-bar-animated"
                   :style="{ width: (task.progressPercent || 0) + '%' }"></div>
            </div>

            <div class="d-flex justify-content-between text-muted small mb-3">
              <span>⭐ Khó: {{ task.difficultyLevel }}</span>
              <span class="fw-bold text-success">+{{ task.point }} pts</span>
            </div>

            <button class="btn btn-outline-primary w-100 fw-bold rounded-3" @click="openReport(task)">
              Cập nhật tiến độ
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-dialog modal-dialog-centered" style="width: 480px;">
        <div class="modal-content border-0 rounded-4 shadow-lg">
          <div class="modal-header border-0 bg-light px-4">
            <h5 class="fw-bold m-0 text-primary">Cập nhật Task</h5>
            <button type="button" class="btn-close shadow-none" @click="closeModal"></button>
          </div>

          <div class="modal-body p-4">
            <div class="mb-2">
              <div class="d-flex justify-content-between fw-bold mb-2">
                <span>Tiến độ hiện tại:</span>
                <span class="text-primary fs-5">{{ report.progressPercent }}%</span>
              </div>
              <input type="range" v-model="report.progressPercent" class="form-range" min="0" max="100" step="5">
            </div>

            <div class="mb-3 mt-4">
              <label class="form-label fw-bold small text-muted text-uppercase d-block mb-2">
                Mô tả công việc đã làm <span class="text-danger">*</span>
              </label>
              <textarea v-model="report.workDescription" class="form-control rounded-3" rows="3" placeholder="Ví dụ: Đã code xong giao diện UI, fix xong bug..."></textarea>
            </div>

            <div class="mb-4 bg-light p-3 rounded-3 mt-3">
              <label class="form-label fw-bold small text-muted text-uppercase mb-2 d-block">Minh chứng (Bắt buộc nếu 100%)</label>
              <input type="file" class="form-control" @change="handleFileUpload">
            </div>

            <div v-if="report.progressPercent == 100" class="alert alert-warning py-2 small mt-3 border-0 text-center">
              ⚠️ Đạt 100% Task sẽ được gửi cho Admin và <b>ẩn khỏi danh sách</b>.
            </div>
          </div>

          <div class="modal-footer border-0 p-4 pt-0">
            <button class="btn btn-light fw-bold px-4 me-2" @click="closeModal">Hủy</button>
            <button class="btn px-4 fw-bold shadow-sm"
                    :class="report.progressPercent == 100 ? 'btn-success' : 'btn-primary'"
                    @click="submitReport" :disabled="loadingReport">
              <span v-if="loadingReport" class="spinner-border spinner-border-sm me-1"></span>
              {{ report.progressPercent == 100 ? 'Gửi & Hoàn tất' : 'Lưu tiến độ' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import taskService from "@/services/taskApi.service"
import { getUser } from "@/utils/jwtDecode"

// --- STATE ---
const tasks = ref([])
const showModal = ref(false)
const loadingReport = ref(false)
const selectedFile = ref(null)

const user = getUser()
const currentEmployeeId = user?.id || user?.employeeId || user?.sub || 6

const report = reactive({
  taskId: null,
  workDescription: "",
  progressPercent: 0
})

// --- LOGIC TẢI DỮ LIỆU ---
const loadTasks = async () => {
  try {
    console.log("🔍 Đang tải task cho NV ID:", currentEmployeeId);
    const res = await taskService.myTasks(currentEmployeeId)

    let rawData = []
    if (Array.isArray(res.data)) {
      rawData = res.data
    } else if (res.data && Array.isArray(res.data.data)) {
      rawData = res.data.data
    }

    console.log("📦 Dữ liệu thô từ server:", rawData);

    tasks.value = rawData.filter(t => {
      const progress = Number(t.progressPercent || 0);
      const isNotFinalized = t.status !== 'DONE' && t.status !== 'COMPLETED';
      return progress < 100 && isNotFinalized;
    });

  } catch (error) {
    console.error("❌ Lỗi tải Task:", error);
    tasks.value = [];
  }
}

// --- XỬ LÝ MODAL & FILE ---
const openReport = (task) => {
  console.log("🔍 Task đang chọn:", task);

  Object.assign(report, {
    taskId: task.id || task.taskId,
    workDescription: "",
    progressPercent: Number(task.progressPercent || 0)
  })
  selectedFile.value = null
  showModal.value = true
}

const handleFileUpload = (e) => {
  const file = e.target.files[0];
  selectedFile.value = file || null;
}

const closeModal = () => { showModal.value = false }

const submitReport = async () => {
  if (!report.taskId) {
    return alert("❌ Lỗi hệ thống: Không lấy được ID công việc! Vui lòng F5 lại trang.");
  }

  if (!report.workDescription || !report.workDescription.trim()) {
    return alert("Vui lòng nhập mô tả công việc đã làm!");
  }

  const is100 = Number(report.progressPercent) === 100;

  if (is100 && !selectedFile.value) {
    return alert("⚠️ Bạn phải đính kèm file minh chứng khi hoàn thành 100%!");
  }

  loadingReport.value = true;
  try {
    const fd = new FormData();
    fd.append('taskId', report.taskId);
    fd.append('employeeId', currentEmployeeId);
    fd.append('workDescription', report.workDescription);
    fd.append('progressPercent', report.progressPercent);
    fd.append('status', is100 ? 'DONE' : 'IN_PROGRESS');

    if (selectedFile.value) {
      fd.append('file', selectedFile.value);
    }

    await taskService.report(fd);

    alert(is100 ? "🎉 Nộp bài thành công! Task đang chờ Admin duyệt." : "✅ Đã lưu tiến độ.");
    closeModal();
    await loadTasks();
  } catch (error) {
    console.error("Lỗi nộp bài:", error);
    alert(error.response?.data?.message || error.response?.data || "Lỗi nộp bài (400 Bad Request)!");
  } finally {
    loadingReport.value = false;
  }
}

onMounted(loadTasks)
</script>

<style scoped>
.task-card { border: 1px solid #eee !important; transition: 0.3s; }
.task-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important; }
.description-text { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 3em; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 2000; backdrop-filter: blur(4px); }
</style>