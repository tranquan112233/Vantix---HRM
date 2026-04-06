<template>
  <div class="task-management">
    <div class="row">
      <div class="col-md-8">

        <div class="page-header">
          <div>
            <h2 class="page-title">Task Inventory</h2>
            <p class="page-desc">Quản lý, theo dõi và đánh giá công việc của nhân viên.</p>
          </div>
        </div>

        <div class="filter-bar">
          <div class="search-wrapper">
            <i>🔍</i>
            <input v-model="searchQuery" type="text" placeholder="Tìm tên nhân viên, công việc...">
            <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">✖</button>
          </div>
          <button class="btn-primary" @click="openCreateModal">
            + New Task
          </button>
        </div>

        <div class="table-card shadow-sm">
          <div class="table-scroll">
            <table>
              <thead>
              <tr>
                <th>Task Info</th>
                <th class="text-center">Diff / Urg</th>
                <th class="text-center">Points</th>
                <th>Status</th>
                <th class="text-right">Actions</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="filteredTasks.length === 0">
                <td colspan="5">
                  <div class="state-center">
                    <div class="empty-icon">🕵️‍♂️</div>
                    <div class="empty-title">Không tìm thấy công việc nào!</div>
                    <div class="empty-sub">Vui lòng tạo công việc mới hoặc thử tìm kiếm khác.</div>
                  </div>
                </td>
              </tr>

              <tr v-for="task in filteredTasks" :key="task.taskId" @click="selectTask(task)" style="cursor: pointer" :style="selectedTask?.taskId === task.taskId ? 'background: #eff6ff;' : ''">
                <td>
                  <div class="employee-cell">
                    <div class="avatar" :style="{ background: stringToColor(task.employeeName || 'A') }">
                      {{ (task.employeeName || getEmployeeName(task.employeeId) || 'N').charAt(0).toUpperCase() }}
                    </div>
                    <div>
                      <div class="employee-name" :class="task.status === 'CANCELLED' ? 'text-decoration-line-through text-muted' : ''">
                        {{ task.taskTitle }}
                        <span v-if="task.fileUrl" title="Có file minh chứng">📎</span>
                        <span v-if="task.status === 'DONE'" class="status-badge done ms-2" style="font-size: 10px; padding: 2px 6px;">MỚI NỘP</span>
                      </div>
                      <div class="employee-username">
                        👤 {{ task.employeeName || getEmployeeName(task.employeeId) }}
                      </div>
                    </div>
                  </div>
                </td>
                <td class="text-center td-num">{{ task.difficultyLevel }} / {{ task.urgencyLevel }}</td>
                <td class="text-center font-weight-bold" style="color: #16a34a;">+{{ task.point }} pts</td>
                <td>
                    <span class="status-badge" :class="statusClass(task.status)">
                      <span class="dot"></span> {{ task.status || 'OPEN' }}
                    </span>
                </td>
                <td class="text-right">
                  <div class="row-actions justify-content-end">
                    <button v-if="task.status !== 'COMPLETED' && task.status !== 'CANCELLED'" class="icon-btn" title="Sửa" @click.stop="openEditModal(task)">
                      ✏️
                    </button>
                    <span v-else-if="task.status === 'COMPLETED'" class="status-badge working">✔ Finalized</span>
                    <span v-else class="status-badge resigned">🚫 Hủy</span>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="table-card p-4 shadow-sm" style="position: sticky; top: 20px;">
          <div class="section-title mb-4">
            👨‍💼 Task Monitoring
          </div>

          <div v-if="selectedTask">
            <div class="p-3 rounded-3 mb-4" :style="selectedTask.status === 'DONE' ? 'background: #fffbeb; border: 1px solid #fde68a;' : 'background: #f8f8f6; border: 1px solid #e8e8e8;'">
              <div class="small text-uppercase fw-bold mb-1" style="color: #6366f1;">Target Task:</div>
              <strong class="fs-5 text-dark d-block mb-3">{{ selectedTask.taskTitle }}</strong>

              <div class="bg-white p-3 rounded-3 shadow-sm border mb-3">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <span class="td-meta fw-bold">Người làm:</span>
                  <span class="employee-name" style="color: #16a34a;">👤 {{ selectedTask.employeeName || getEmployeeName(selectedTask.employeeId) }}</span>
                </div>

                <div class="mb-3" v-if="selectedTask.status !== 'OPEN'">
                  <div class="d-flex justify-content-between small fw-bold mb-2">
                    <span class="td-meta">Tiến độ:</span>
                    <span style="color: #6366f1;">{{ selectedTask.progressPercent || 0 }}%</span>
                  </div>
                  <div style="background: #f0f0f0; height: 8px; border-radius: 4px; overflow: hidden;">
                    <div :style="{ width: (selectedTask.progressPercent || 0) + '%', background: '#16a34a', height: '100%', transition: 'width 0.3s' }"></div>
                  </div>
                </div>

                <div v-if="selectedTask.workDescription" class="mb-3 p-3 rounded-3" style="background: #f8f8f6; border: 1px dashed #ccc;">
                  <span class="td-meta fw-bold d-block mb-1">📝 Lời nhắn từ nhân viên:</span>
                  <span class="td-desc fst-italic">"{{ selectedTask.workDescription }}"</span>
                </div>

                <div v-if="selectedTask.fileUrl" class="mt-3">
                  <button class="btn-primary w-100 justify-content-center" @click="downloadEvidence(selectedTask.fileUrl)">
                    📄 Tải file minh chứng
                  </button>
                </div>

                <div v-else-if="selectedTask.status === 'DONE'" class="alert-error mt-3">
                  <strong>⚠️</strong> Nhân viên nộp bài nhưng KHÔNG đính kèm file!
                </div>
              </div>

              <div v-if="selectedTask.status === 'DONE'" class="d-flex flex-column gap-2">
                <button class="btn-primary justify-content-center" style="background: #16a34a;" @click="handleApprove(selectedTask.taskId)">
                  ✅ PHÊ DUYỆT & CHỐT ĐIỂM
                </button>
                <button class="btn-ghost justify-content-center" style="color: #d97706; border-color: #fcd34d;" @click="handleReopen(selectedTask.taskId)">
                  🔄 YÊU CẦU LÀM LẠI
                </button>
              </div>

              <div v-if="selectedTask.status === 'CANCELLED'" class="alert-error mt-2 justify-content-center">
                🚫 Công việc này đã bị hủy.
              </div>
            </div>

            <div v-if="selectedTask.status === 'OPEN'" class="mb-4">
              <div class="field mb-3">
                <label>Giao cho nhân viên</label>
                <div class="select-wrap">
                  <select v-model="assignData.employeeId">
                    <option value="" disabled>-- Chọn nhân viên --</option>
                    <option v-for="e in employees" :key="e.id || e.employeeId" :value="e.id || e.employeeId">
                      {{ e.fullName || e.name }}
                    </option>
                  </select>
                  <i class="bi-chevron-down">▼</i>
                </div>
              </div>
              <button class="btn-primary w-100 justify-content-center" @click="handleAssign" :disabled="loadingAssign || !assignData.employeeId">
                <span v-if="loadingAssign" class="spin-sm me-2"></span> Giao việc
              </button>
            </div>

            <div v-if="selectedTask.status === 'OPEN' || selectedTask.status === 'IN_PROGRESS'" class="mt-4 pt-3" style="border-top: 1px solid #f0f0f0;">
              <button class="btn-danger w-100 justify-content-center" @click="handleCancel(selectedTask.taskId)">
                🚫 Hủy công việc này
              </button>
            </div>
          </div>

          <div v-else class="state-center">
            <div class="empty-icon">👉</div>
            <div class="empty-title">Chọn task để xem</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <div class="modal-custom animate__animated animate__zoomIn">
          <div class="modal-header-custom">
            <div>
              <h3 class="modal-title">{{ isEditMode ? 'Chỉnh sửa công việc' : 'Tạo công việc mới' }}</h3>
              <p class="modal-subtitle">Điền thông tin chi tiết cho công việc</p>
            </div>
            <button class="btn-close-custom" @click="closeModal">✖</button>
          </div>

          <div class="modal-body-custom">
            <div class="field">
              <label>Tiêu đề công việc <span class="req">*</span></label>
              <div class="input-wrap">
                <input v-model="newTask.taskTitle" placeholder="Nhập tiêu đề..." />
              </div>
            </div>

            <div class="field">
              <label>Mô tả chi tiết</label>
              <textarea v-model="newTask.description" class="textarea-field" rows="3" placeholder="Yêu cầu cụ thể..."></textarea>
            </div>

            <div class="form-row">
              <div class="field">
                <label>Độ khó (1-5)</label>
                <div class="input-wrap">
                  <input type="number" v-model="newTask.difficultyLevel" min="1" max="5" />
                </div>
              </div>
              <div class="field">
                <label>Mức khẩn cấp (1-5)</label>
                <div class="input-wrap">
                  <input type="number" v-model="newTask.urgencyLevel" min="1" max="5" />
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer-custom">
            <button class="btn-ghost" @click="closeModal">Hủy</button>
            <button class="btn-primary" @click="saveTask" :disabled="loading">
              <span v-if="loading" class="spin-sm me-2"></span>
              {{ isEditMode ? 'Lưu thay đổi' : 'Tạo mới' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue"
import taskService from "@/services/taskApi.service"

const tasks = ref([]);
const employees = ref([]);
const selectedTask = ref(null);
const assignData = reactive({ taskId: "", employeeId: "" });
const searchQuery = ref("");

const showModal = ref(false);
const loading = ref(false);
const loadingAssign = ref(false);
const isEditMode = ref(false);
const editingTaskId = ref(null);
const newTask = reactive({ taskTitle: "", description: "", difficultyLevel: 1, urgencyLevel: 1, status: "OPEN" });

// Sinh màu ngẫu nhiên cho Avatar dựa trên tên
const stringToColor = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return `hsl(${hash % 360}, 70%, 60%)`;
};

const loadData = async () => {
  try {
    const [tRes, eRes] = await Promise.all([taskService.getAll(), taskService.getEmployees()]);

    let employeeList = [];
    if (Array.isArray(eRes.data)) employeeList = eRes.data;
    else if (eRes.data && Array.isArray(eRes.data.data)) employeeList = eRes.data.data;
    else if (eRes.data && Array.isArray(eRes.data.content)) employeeList = eRes.data.content;
    else if (eRes.data && Array.isArray(eRes.data.result)) employeeList = eRes.data.result;

    employees.value = employeeList;

    let taskList = [];
    if (Array.isArray(tRes.data)) taskList = tRes.data;
    else if (tRes.data && Array.isArray(tRes.data.data)) taskList = tRes.data.data;
    else if (tRes.data && Array.isArray(tRes.data.content)) taskList = tRes.data.content;
    tasks.value = taskList;

    if (selectedTask.value && tasks.value.length > 0) {
      const found = tasks.value.find(t => t.taskId === selectedTask.value.taskId);
      if (found) selectedTask.value = found;
    }
  } catch (err) { console.error("❌ Lỗi khi load data:", err); }
};

const getEmployeeName = (id) => {
  if (!id || !Array.isArray(employees.value)) return "Chưa có";
  const emp = employees.value.find(e => String(e.id || e.employeeId) === String(id));
  return emp ? (emp.fullName || emp.name) : "N/A";
}

const filteredTasks = computed(() => {
  if (!searchQuery.value) return tasks.value;
  const query = searchQuery.value.toLowerCase();
  return tasks.value.filter(task => {
    const empName = task.employeeName || getEmployeeName(task.employeeId);
    return empName.toLowerCase().includes(query) || task.taskTitle.toLowerCase().includes(query);
  });
});

// Truyền vào 'fileUrl' thay vì 'taskId' anh nhé!
const downloadEvidence = (fileUrl) => {
  if (!fileUrl) {
    alert("⚠️ Công việc này không có file!");
    return;
  }

  // tải trực tiếp từ Cloudinary
  const link = document.createElement("a");
  link.href = fileUrl;
  link.target = "_blank";
  link.rel = "noopener";

  // QUAN TRỌNG: không đặt tên file tay
  link.download = "";

  document.body.appendChild(link);
  link.click();
  link.remove();
};

const selectTask = (task) => {
  selectedTask.value = task;
  assignData.taskId = task.taskId;
  assignData.employeeId = task.employeeId || "";
}

const handleApprove = async (id) => {
  if (!confirm("Duyệt task này? Nhân viên sẽ nhận được điểm thưởng.")) return;
  try {
    await taskService.approve(id);
    alert("✅ Đã chốt thành công!");
    await loadData();
  } catch (e) { alert("Lỗi khi phê duyệt!"); }
}

const handleReopen = async (id) => {
  if (!confirm("⚠️ Bắt nhân viên làm lại task này?")) return;
  try {
    await taskService.reopen(id);
    alert("🔄 Đã trả lại task cho nhân viên làm lại!");
    await loadData();
  } catch (e) { alert("Lỗi khi yêu cầu làm lại!"); }
}

const handleAssign = async () => {
  loadingAssign.value = true;
  try {
    await taskService.assign(assignData);
    alert("Đã giao việc!");
    await loadData();
  } catch (e) { alert("Lỗi giao việc!"); }
  finally { loadingAssign.value = false; }
}

const handleCancel = async (id) => {
  if (!confirm("⚠️ Bạn có chắc chắn muốn HỦY công việc này không?")) return;
  try {
    await taskService.cancel(id);
    alert("🚫 Đã hủy công việc!");
    await loadData();
  } catch (e) { alert("Lỗi khi hủy công việc!"); }
}

const openCreateModal = () => {
  isEditMode.value = false;
  Object.assign(newTask, { taskTitle: "", description: "", difficultyLevel: 1, urgencyLevel: 1, status: "OPEN" });
  showModal.value = true;
}

const openEditModal = (task) => {
  isEditMode.value = true;
  editingTaskId.value = task.taskId;
  Object.assign(newTask, { ...task });
  showModal.value = true;
}

const closeModal = () => { showModal.value = false; }

const saveTask = async () => {
  if (!newTask.taskTitle) return alert("Vui lòng nhập tiêu đề!");

  loading.value = true;
  try {
    // 🔥 THÊM DÒNG NÀY: Ép kiểu về số và tự tính điểm (Ví dụ: Độ khó + Khẩn cấp * 10)
    newTask.difficultyLevel = Number(newTask.difficultyLevel) || 1;
    newTask.urgencyLevel = Number(newTask.urgencyLevel) || 1;
    newTask.point = (newTask.difficultyLevel + newTask.urgencyLevel) * 10;

    if (isEditMode.value) {
      await taskService.update(editingTaskId.value, newTask);
    } else {
      await taskService.create(newTask);
    }

    closeModal();
    await loadData();
  } catch (err) {
    console.error("Lỗi lưu task:", err);
    alert("Lỗi hệ thống khi lưu công việc!");
  } finally {
    loading.value = false;
  }
}

const statusClass = (s) => {
  const m = {
    'OPEN': 'open',
    'IN_PROGRESS': 'in_progress',
    'DONE': 'done',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled'
  };
  return m[s] || 'open';
}

onMounted(loadData)
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

/* Buttons */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #4f52e0;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff;
  color: #444;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-ghost:hover {
  background: #f5f5f5;
  border-color: #ccc;
  color: #6366f1;
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #dc2626;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
  transform: translateY(-1px);
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-wrapper {
  position: relative;
  flex: 1;
  min-width: 220px;
}

.search-wrapper > i {
  position: absolute;
  left: 11px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 13px;
  font-style: normal;
  pointer-events: none;
}

.search-wrapper input {
  width: 100%;
  padding: 8px 34px 8px 34px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
  font-family: inherit;
  color: #111;
  transition: all 0.2s;
}

.search-wrapper input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
  padding: 2px 4px;
  font-size: 14px;
}

.clear-btn:hover { color: #6366f1; }

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
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

.table-scroll { overflow-x: auto; }

table { width: 100%; border-collapse: collapse; }
thead tr { border-bottom: 1px solid #f0f0f0; }
th {
  padding: 11px 16px;
  font-size: 11.5px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-align: left;
  white-space: nowrap;
  background: #fafafa;
}

tbody tr { border-bottom: 1px solid #f5f5f5; transition: background 0.1s; }
tbody tr:last-child { border-bottom: none; }
tbody tr:hover { background: #fafafa; }

td {
  padding: 12px 16px;
  font-size: 13.5px;
  color: #333;
  vertical-align: middle;
}

.td-num { color: #bbb; font-size: 12px; }
.td-meta { color: #888; font-size: 12.5px; }
.td-desc { color: #888; font-size: 13px; max-width: 220px; }

/* Avatar */
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.employee-cell { display: flex; align-items: center; gap: 10px; }
.employee-name { font-size: 13.5px; font-weight: 600; color: #111; }
.employee-username { font-size: 11.5px; color: #999; margin-top: 2px; }

/* Row Actions */
.row-actions { display: flex; gap: 4px; }
.icon-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  color: #888;
  transition: all 0.15s;
  font-size: 13px;
}
.icon-btn:hover { background: #f0f0f0; color: #6366f1; }

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

.dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.status-badge.working { background: #f0fdf4; color: #16a34a; }
.status-badge.resigned { background: #fef2f2; color: #dc2626; }

/* Các trạng thái của Task */
.status-badge.open { background: #f3f4f6; color: #4b5563; }
.status-badge.in_progress { background: #eff6ff; color: #3b82f6; }
.status-badge.done { background: #fffbeb; color: #d97706; }
.status-badge.completed { background: #f0fdf4; color: #16a34a; }
.status-badge.cancelled { background: #fef2f2; color: #dc2626; }

/* Form Fields */
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #333; }
.req { color: #dc2626; }

.input-wrap { position: relative; display: flex; align-items: center; }
.input-wrap input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #111;
  background: #fff;
  transition: all 0.2s;
}
.input-wrap input:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1); }

.textarea-field {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #111;
  background: #fff;
  resize: vertical;
  transition: all 0.2s;
}
.textarea-field:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1); }

.select-wrap { position: relative; }
.select-wrap select {
  width: 100%;
  padding: 9px 30px 9px 11px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13.5px;
  font-family: inherit;
  color: #333;
  background: #fff;
  appearance: none;
  cursor: pointer;
  transition: all 0.2s;
}
.select-wrap select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1); }
.select-wrap .bi-chevron-down {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  color: #aaa; font-size: 11px; font-style: normal; pointer-events: none;
}

.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  font-size: 13px;
  color: #dc2626;
}

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.section-title {
  display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 700;
  color: #111; padding: 8px 0 12px; border-bottom: 1px solid #f0f0f0; margin-bottom: 12px;
}

/* Spinner */
.spin-sm {
  display: inline-block; width: 14px; height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Modal CSS Bổ sung */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  z-index: 9999; backdrop-filter: blur(4px);
}

.modal-container {
  width: 500px;
  max-width: 95vw;
}

.modal-custom {
  background: #fff;
  border: none;
  border-radius: 14px;
  overflow: hidden;
  font-family: 'DM Sans', sans-serif;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-header-custom {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 20px 24px 16px; border-bottom: 1px solid #f0f0f0; background: #fff;
}
.modal-header-custom .modal-title { font-size: 18px; font-weight: 700; color: #111; margin: 0; }
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

@media (max-width: 768px) {
  .task-management { padding: 16px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .filter-bar { flex-direction: column; align-items: stretch; }
  .search-wrapper { min-width: 100%; }
  .form-row { grid-template-columns: 1fr; }
}
</style>