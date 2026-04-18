<template>
  <div class="task-management mgmt-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">Công việc của tôi</h2>
        <p class="page-desc">Hoàn thành 100% tiến độ để nộp bài và chờ quản trị viên phê duyệt.</p>
      </div>
      <div class="status-badge in_progress" style="font-size: 14px; padding: 6px 16px;">
        Đang thực hiện: {{ tasks.length }}
      </div>
    </div>

    <div v-if="tasks.length === 0" class="table-card mt-4">
      <div class="state-center">
        <i class="bi bi-stars empty-icon"></i>
        <div class="empty-title">Bạn đã hoàn thành sạch sẽ!</div>
        <div class="empty-sub">Không còn công việc nào đang chờ xử lý dưới 100%.</div>
      </div>
    </div>

    <div v-else class="task-grid mt-4">
      <div v-for="task in tasks" :key="task.taskId" class="table-card task-card-custom p-4 d-flex flex-column">

        <div class="d-flex justify-content-between align-items-start mb-3">
          <h5 class="task-title m-0">{{ task.taskTitle }}</h5>
          <span class="status-badge" :class="task.progressPercent >= 80 ? 'working' : (task.progressPercent >= 40 ? 'done' : 'in_progress')">
            {{ task.progressPercent || 0 }}%
          </span>
        </div>

        <p class="td-desc flex-grow-1 description-text">
          {{ task.description || 'Nội dung chi tiết chưa cập nhật.' }}
        </p>

        <div class="progress-bar-container mb-3 mt-auto">
          <div class="progress-fill"
               :style="{
                 width: (task.progressPercent || 0) + '%',
                 background: task.progressPercent >= 80 ? '#16a34a' : (task.progressPercent >= 40 ? '#d97706' : '#6366f1')
               }">
          </div>
        </div>

        <div class="d-flex justify-content-between td-meta fw-bold mb-4">
          <span><i class="bi bi-bar-chart-line me-1"></i>Khó: {{ task.difficultyLevel }}</span>
          <span style="color: #16a34a;">+{{ task.point }} điểm</span>
        </div>

        <button class="btn-ghost w-100 justify-content-center" @click="openReport(task)">
          <i class="bi bi-arrow-repeat"></i> Cập nhật tiến độ
        </button>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container" style="width: 480px;">
        <div class="modal-custom animate__animated animate__zoomIn">

          <div class="modal-header-custom">
            <div>
              <h3 class="modal-title">Cập nhật tiến độ</h3>
              <p class="modal-subtitle">Ghi lại những gì bạn đã làm được</p>
            </div>
            <button class="btn-close-custom" @click="closeModal"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="modal-body-custom">
            <div class="field mb-2">
              <label class="d-flex justify-content-between align-items-center">
                <span>Tiến độ hiện tại:</span>
                <span style="color: #6366f1; font-size: 18px;">{{ report.progressPercent }}%</span>
              </label>
              <input type="range" v-model="report.progressPercent" class="range-slider mt-2" min="0" max="100" step="5">
            </div>

            <div class="field mt-2">
              <label>Mô tả công việc đã làm <span class="req">*</span></label>
              <textarea v-model="report.workDescription" class="textarea-field" rows="3" placeholder="Ví dụ: Đã code xong giao diện UI, fix xong bug..."></textarea>
            </div>

            <div class="field mt-3 p-3 rounded-3" style="background: #f8f8f6; border: 1px dashed #ccc;">
              <label>Minh chứng (Bắt buộc nếu 100%)</label>
              <input type="file" class="file-input mt-2" @change="handleFileUpload">
            </div>

            <div v-if="report.progressPercent == 100" class="alert-warning-custom mt-2">
              <strong><i class="bi bi-exclamation-triangle me-1"></i>Chú ý:</strong> Đạt 100% công việc sẽ được gửi cho quản trị viên duyệt và <b>ẩn khỏi danh sách</b>.
            </div>
          </div>

          <div class="modal-footer-custom">
            <button class="btn-ghost" @click="closeModal">Hủy</button>
            <button class="btn-primary"
                    :style="report.progressPercent == 100 ? 'background: #16a34a;' : ''"
                    @click="submitReport" :disabled="loadingReport">
              <span v-if="loadingReport" class="spin-sm me-2"></span>
              {{ report.progressPercent == 100 ? 'Gửi & Hoàn tất' : 'Lưu tiến độ' }}
            </button>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import taskService from "@/services/taskApi.service"
import { useToast } from "@/utils/toast"
import { useAuthStore } from "@/stores/auth.store"

const toast = useToast()
const auth = useAuthStore()

// --- STATE ---
const tasks = ref([])
const showModal = ref(false)
const loadingReport = ref(false)
const selectedFile = ref(null)

const currentEmployeeId = computed(() => auth.user?.employeeId ?? null)

const report = reactive({
  taskId: null,
  workDescription: "",
  progressPercent: 0
})

// --- LOGIC TẢI DỮ LIỆU ---
const loadTasks = async () => {
  if (!auth.user && auth.token) await auth.fetchMe();
  if (!currentEmployeeId.value) {
    tasks.value = [];
    toast.error("Không tìm thấy mã nhân viên của tài khoản đăng nhập.");
    return;
  }

  try {
    const res = await taskService.myTasks(currentEmployeeId.value)

    let rawData = []
    if (Array.isArray(res.data)) {
      rawData = res.data
    } else if (res.data && Array.isArray(res.data.data)) {
      rawData = res.data.data
    }

    tasks.value = rawData.filter(t => {
      const progress = Number(t.progressPercent || 0);
      const isNotFinalized = t.status !== 'DONE' && t.status !== 'COMPLETED';
      return progress < 100 && isNotFinalized;
    });

  } catch (error) {
    console.error("❌ Lỗi tải Task:", error);
    tasks.value = [];
    toast.error(error, "Không thể tải danh sách công việc.");
  }
}

// --- XỬ LÝ MODAL & FILE ---
const openReport = (task) => {
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
    toast.error("Không xác định được công việc cần cập nhật.");
    return;
  }

  if (!report.workDescription?.trim()) {
    toast.warning("Vui lòng nhập mô tả công việc đã thực hiện.");
    return;
  }

  const is100 = Number(report.progressPercent) === 100;

  if (is100 && !selectedFile.value) {
    toast.warning("Cần đính kèm file minh chứng khi hoàn thành 100%.");
    return;
  }

  loadingReport.value = true;

  try {
    const fd = new FormData();

    fd.append("taskId", report.taskId);
    fd.append("employeeId", currentEmployeeId.value);
    fd.append("workDescription", report.workDescription);
    fd.append("progressPercent", report.progressPercent);
    fd.append("status", is100 ? "DONE" : "IN_PROGRESS");

    // ✅ gửi file trực tiếp cho /tasks/report
    if (selectedFile.value) {
      fd.append("file", selectedFile.value);
    }

    // ✅ CHỈ GỌI 1 API DUY NHẤT
    await taskService.report(fd);

    toast.success(
      is100
        ? "Đã nộp task thành công. Công việc đang chờ duyệt."
        : "Đã lưu tiến độ công việc."
    );

    closeModal();
    await loadTasks();

  } catch (error) {
    console.error("Lỗi nộp bài:", error);
    toast.error(error, "Không thể gửi báo cáo công việc.");
  } finally {
    loadingReport.value = false;
  }
};

onMounted(loadTasks)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.task-management {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #111;
}

.page-desc {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

/* Base Cards */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.state-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 10px;
  color: #888;
}

.empty-icon { font-size: 36px; color: #ccc; }
.empty-title { font-size: 16px; font-weight: 600; color: #333; }
.empty-sub { font-size: 13px; color: #999; }

/* Grid Layout cho Task */
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.task-card-custom {
  transition: transform 0.2s, box-shadow 0.2s;
}

.task-card-custom:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
}

.task-title {
  font-size: 16px;
  font-weight: 700;
  color: #111;
  line-height: 1.4;
  margin-right: 10px;
}

.description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 3em;
}

.td-desc { color: #888; font-size: 13px; }
.td-meta { color: #888; font-size: 12.5px; }

/* Progress Bar Custom */
.progress-bar-container {
  width: 100%;
  height: 8px;
  background: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.4s ease, background-color 0.4s ease;
}

/* Status Badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 600;
}

.status-badge.working { background: #f0fdf4; color: #16a34a; }
.status-badge.in_progress { background: #eff6ff; color: #3b82f6; }
.status-badge.done { background: #fffbeb; color: #d97706; }

/* Buttons */
.btn-primary {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; background: #6366f1; color: #fff;
  border: none; border-radius: 8px; font-size: 13.5px;
  font-weight: 600; cursor: pointer; font-family: inherit; transition: all 0.2s;
}
.btn-primary:hover:not(:disabled) { background: #4f52e0; transform: translateY(-1px); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-ghost {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 14px; background: #fff; color: #444;
  border: 1px solid #e0e0e0; border-radius: 8px; font-size: 13.5px;
  font-weight: 600; cursor: pointer; font-family: inherit; transition: all 0.2s;
}
.btn-ghost:hover { background: #f5f5f5; border-color: #ccc; color: #6366f1; }

/* Modal CSS */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  z-index: 9999; backdrop-filter: blur(4px);
}

.modal-container { max-width: 95vw; }

.modal-custom {
  background: #fff; border: none; border-radius: 14px;
  overflow: hidden; font-family: 'DM Sans', sans-serif;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-header-custom {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 20px 24px 16px; border-bottom: 1px solid #f0f0f0; background: #fff;
}
.modal-title { font-size: 18px; font-weight: 700; color: #111; margin: 0; }
.modal-subtitle { font-size: 13px; color: #888; margin: 4px 0 0; }

.btn-close-custom {
  background: none; border: none; color: #aaa; cursor: pointer;
  font-size: 16px; padding: 2px; display: flex; align-items: center; line-height: 1;
}
.btn-close-custom:hover { color: #dc2626; }

.modal-body-custom {
  padding: 20px 24px; display: flex; flex-direction: column; gap: 16px;
  background: #fff; max-height: 70vh; overflow-y: auto;
}

.modal-footer-custom {
  display: flex; gap: 8px; justify-content: flex-end; padding: 16px 24px;
  border-top: 1px solid #f0f0f0; background: #fff;
}

/* Form Fields */
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #333; }
.req { color: #dc2626; }

.textarea-field {
  width: 100%; padding: 9px 12px; border: 1px solid #e0e0e0;
  border-radius: 8px; font-size: 13.5px; font-family: inherit;
  color: #111; background: #fff; resize: vertical; transition: all 0.2s;
}
.textarea-field:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1); }

.file-input {
  width: 100%; font-size: 13px; color: #555;
}
.file-input::file-selector-button {
  padding: 6px 12px; border-radius: 6px; border: 1px solid #e0e0e0;
  background: #fff; color: #333; font-weight: 600; cursor: pointer;
  transition: all 0.2s; margin-right: 10px; font-family: 'DM Sans', sans-serif;
}
.file-input::file-selector-button:hover { background: #f5f5f5; border-color: #ccc; }

.range-slider {
  width: 100%;
  accent-color: #6366f1;
}

.alert-warning-custom {
  padding: 10px 14px; background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 8px; font-size: 12.5px; color: #d97706;
}

/* Spinner */
.spin-sm {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .task-management { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
}
</style>
