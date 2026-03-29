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
              Update Progress
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
            <div class="mb-4">
              <label class="form-label fw-bold small text-muted text-uppercase">Ghi chú công việc</label>
              <textarea v-model="report.workDescription" class="form-control border-2 shadow-none" rows="3" placeholder="Bạn đã làm được gì..."></textarea>
            </div>

            <div class="mb-4 bg-light p-3 rounded-3">
              <label class="form-label fw-bold small text-muted text-uppercase mb-2 d-block">Minh chứng (Nếu 100%)</label>
              <input type="file" class="form-control" @change="handleFileUpload">
            </div>

            <div class="mb-2">
              <div class="d-flex justify-content-between fw-bold mb-2">
                <span>Tiến độ:</span>
                <span class="text-primary">{{ report.progressPercent }}%</span>
              </div>
              <input type="range" v-model="report.progressPercent" class="form-range" min="0" max="100" step="5">
            </div>

            <div v-if="report.progressPercent == 100" class="alert alert-warning py-2 small mt-3 border-0 text-center">
              ⚠️ Đạt 100% Task sẽ được gửi đi và <b>ẩn khỏi danh sách</b> này.
            </div>
          </div>

          <div class="modal-footer border-0 p-4 pt-0">
            <button class="btn btn-light fw-bold px-4 me-2" @click="closeModal">Đóng</button>
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

const tasks = ref([])
const showModal = ref(false)
const loadingReport = ref(false)
const selectedFile = ref(null)
const currentEmployeeId = getUser()?.employeeId || 6

const report = reactive({ taskId: null, workDescription: "", progressPercent: 0 })

const loadTasks = async () => {
  try {
    const res = await taskService.myTasks(currentEmployeeId)
    // 🔥 LOGIC QUAN TRỌNG:
    // 1. Chỉ hiện Task chưa đạt 100%
    // 2. Chỉ hiện Task chưa ở trạng thái DONE hoặc COMPLETED
    tasks.value = res.data.filter(t => {
      const isUnder100 = parseInt(t.progressPercent || 0) < 100;
      const isNotDone = t.status !== 'DONE' && t.status !== 'COMPLETED';
      return isUnder100 && isNotDone;
    });
  } catch (error) { console.error("Lỗi tải Task:", error) }
}

const openReport = (task) => {
  Object.assign(report, { taskId: task.taskId, workDescription: "", progressPercent: task.progressPercent || 0 })
  selectedFile.value = null
  showModal.value = true
}

const handleFileUpload = (e) => { selectedFile.value = e.target.files[0] }
const closeModal = () => { showModal.value = false }

const submitReport = async () => {
  if (!report.workDescription) return alert("Vui lòng nhập mô tả!");

  const is100 = parseInt(report.progressPercent) === 100;
  if (is100 && !selectedFile.value) return alert("Phải đính kèm file khi hoàn thành 100%!");

  loadingReport.value = true;
  try {
    const fd = new FormData();
    fd.append('taskId', report.taskId);
    fd.append('employeeId', currentEmployeeId);
    fd.append('workDescription', report.workDescription);
    fd.append('progressPercent', report.progressPercent);
    // Nếu 100% thì set DONE để Backend biết mà báo cho Admin
    fd.append('status', is100 ? 'DONE' : 'IN_PROGRESS');
    if (selectedFile.value) fd.append('file', selectedFile.value);

    await taskService.report(fd);

    alert(is100 ? "🎉 Task đã hoàn thành và ẩn!" : "Đã cập nhật tiến độ.");
    closeModal();
    await loadTasks(); // Tải lại và filter sẽ tự động đuổi Task 100% đi
  } catch (error) {
    alert("Lỗi hệ thống khi nộp bài!");
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