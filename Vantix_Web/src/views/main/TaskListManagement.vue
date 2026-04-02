<template>
  <div class="admin-workspace p-4">
    <div class="row">
      <div class="col-md-8">
        <div class="card border-0 shadow-sm p-4 h-100 rounded-4">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold m-0 text-dark">📋 Task Inventory</h2>
            <div class="d-flex gap-2 align-items-center">
              <div class="position-relative">
                <input v-model="searchQuery" type="text" class="form-control rounded-pill px-4 shadow-sm border-0 bg-light"
                       placeholder="🔍 Tìm tên nhân viên..." style="width: 250px;">
                <span v-if="searchQuery" class="position-absolute top-50 end-0 translate-middle-y me-3 text-muted"
                      style="cursor: pointer;" @click="searchQuery = ''">✖</span>
              </div>
              <button class="btn btn-primary fw-bold px-4 rounded-pill shadow-sm" @click="openCreateModal">+ New Task</button>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table table-hover align-middle">
              <thead class="table-light">
              <tr>
                <th>Task Info</th>
                <th class="text-center">D/U</th>
                <th class="text-center">Points</th>
                <th>Status</th>
                <th class="text-end">Actions</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="filteredTasks.length === 0">
                <td colspan="5" class="text-center py-5 text-muted fw-bold">
                  Không tìm thấy công việc nào! 🕵️‍♂️
                </td>
              </tr>

              <tr v-for="task in filteredTasks" :key="task.taskId" @click="selectTask(task)" class="task-row"
                  :class="{ 'table-active-custom': selectedTask?.taskId === task.taskId }">
                <td>
                  <div class="fw-bold" :class="task.status === 'DONE' ? 'text-danger' : (task.status === 'CANCELLED' ? 'text-muted text-decoration-line-through' : 'text-dark')">
                    {{ task.taskTitle }}

                    <span v-if="task.fileUrl" class="ms-1 fs-6" title="Có file minh chứng">📎</span>

                    <span v-if="task.status === 'DONE'" class="badge bg-danger ms-2 animate__animated animate__flash animate__infinite">MỚI NỘP</span>
                  </div>
                  <div class="small mt-1 text-secondary">
                    👤 {{ task.employeeName || getEmployeeName(task.employeeId) }}
                  </div>
                </td>

                <td class="text-center small">{{ task.difficultyLevel }}/{{ task.urgencyLevel }}</td>
                <td class="text-center text-success fw-bold">{{ task.point }} pts</td>
                <td><span :class="statusClass(task.status)" class="badge px-3 py-1 rounded-pill">{{ task.status || 'OPEN' }}</span></td>
                <td class="text-end">
                  <button v-if="task.status !== 'COMPLETED' && task.status !== 'CANCELLED'" class="btn btn-sm btn-outline-warning rounded-pill fw-bold px-3" @click.stop="openEditModal(task)">Sửa</button>
                  <span v-else-if="task.status === 'COMPLETED'" class="text-success small fw-bold">✔ Finalized</span>
                  <span v-else class="text-secondary small fw-bold">🚫 Đã hủy</span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card border-0 shadow-sm p-4 sticky-assign-card rounded-4">
          <h4 class="fw-bold mb-4">👨‍💼 Task Monitoring</h4>
          <div v-if="selectedTask">
            <div class="selected-box p-3 rounded-4 border shadow-sm mb-4"
                 :class="selectedTask.status === 'DONE' ? 'bg-warning-subtle border-warning' : 'bg-light'">
              <div class="small text-uppercase fw-bold mb-1 text-primary">Target Task:</div>
              <strong class="fs-5 text-dark d-block mb-3">{{ selectedTask.taskTitle }}</strong>

              <div class="bg-white p-3 rounded-3 shadow-sm border mb-3">
                <div class="mb-3 d-flex justify-content-between align-items-center">
                  <span class="small text-muted fw-bold">Người làm:</span>
                  <span class="fw-bold text-success">👤 {{ selectedTask.employeeName || getEmployeeName(selectedTask.employeeId) }}</span>
                </div>

                <div class="mb-3" v-if="selectedTask.status !== 'OPEN'">
                  <div class="d-flex justify-content-between small fw-bold mb-1">
                    <span class="text-muted">Tiến độ:</span>
                    <span class="text-primary">{{ selectedTask.progressPercent || 0 }}%</span>
                  </div>
                  <div class="progress" style="height: 6px;">
                    <div class="progress-bar bg-success" :style="{ width: (selectedTask.progressPercent || 0) + '%' }"></div>
                  </div>
                </div>

                <div v-if="selectedTask.fileUrl" class="mt-3">
                  <button @click="openEvidence(selectedTask.fileUrl)" class="btn btn-sm btn-primary w-100 fw-bold rounded-pill shadow-sm">
                    📄 Xem file minh chứng
                  </button>
                </div>
                <div v-else-if="selectedTask.status === 'DONE'" class="alert alert-danger py-2 mt-3 mb-0 text-center small border-0 shadow-sm">
                  <strong>⚠️ CHÚ Ý:</strong> Nhân viên nộp bài nhưng <b>KHÔNG</b> đính kèm file! Hãy yêu cầu làm lại.
                </div>
              </div>

              <div v-if="selectedTask.status === 'DONE'">
                <div class="d-flex flex-column gap-2">
                  <button class="btn btn-success w-100 fw-bold py-2 rounded-3 shadow animate__animated animate__pulse animate__infinite" @click="handleApprove(selectedTask.taskId)">
                    ✅ PHÊ DUYỆT & CHỐT ĐIỂM
                  </button>
                  <button class="btn btn-warning w-100 fw-bold py-2 rounded-3 shadow text-dark" @click="handleReopen(selectedTask.taskId)">
                    🔄 YÊU CẦU LÀM LẠI
                  </button>
                </div>
              </div>

              <div v-if="selectedTask.status === 'CANCELLED'" class="alert alert-dark border-0 text-center fw-bold py-2 mt-2 mb-0">
                🚫 Công việc này đã bị hủy.
              </div>
            </div>

            <div v-if="selectedTask.status === 'OPEN'" class="mb-4">
              <label class="form-label fw-bold small text-muted">Giao cho nhân viên</label>
              <select v-model="assignData.employeeId" class="form-select rounded-3 mb-3">
                <option value="" disabled>-- Chọn --</option>
                <option v-for="e in employees" :key="e.id" :value="e.id">{{ e.fullName }}</option>
              </select>
              <button class="btn btn-success w-100 fw-bold" @click="handleAssign" :disabled="loadingAssign || !assignData.employeeId">
                <span v-if="loadingAssign" class="spinner-border spinner-border-sm me-1"></span>
                Giao việc
              </button>
            </div>

            <div v-if="selectedTask.status === 'OPEN' || selectedTask.status === 'IN_PROGRESS'" class="mt-4 pt-3 border-top">
              <button class="btn btn-outline-danger w-100 fw-bold rounded-3" @click="handleCancel(selectedTask.taskId)">
                🚫 Hủy công việc này
              </button>
            </div>
          </div>
          <div v-else class="p-5 text-center text-muted border border-dashed rounded-4">Chọn task để xem chi tiết</div>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card shadow-lg animate__animated animate__zoomIn">
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
          <h3 class="fw-bold m-0 text-primary">{{ isEditMode ? 'Edit Task' : 'New Task' }}</h3>
          <button class="btn-close shadow-none" @click="closeModal"></button>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">Task Title</label>
          <input v-model="newTask.taskTitle" class="form-control rounded-3" placeholder="Tên công việc..." />
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">Description</label>
          <textarea v-model="newTask.description" class="form-control rounded-3" rows="3" placeholder="Mô tả..."></textarea>
        </div>
        <div class="row mb-4">
          <div class="col-6">
            <label class="form-label fw-bold small">Difficulty (1-5)</label>
            <input type="number" v-model="newTask.difficultyLevel" class="form-control rounded-3" min="1" max="5" />
          </div>
          <div class="col-6">
            <label class="form-label fw-bold small">Urgency (1-5)</label>
            <input type="number" v-model="newTask.urgencyLevel" class="form-control rounded-3" min="1" max="5" />
          </div>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-light w-50 fw-bold rounded-3" @click="closeModal">Hủy</button>
          <button class="btn btn-primary w-50 fw-bold rounded-3" @click="saveTask" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
            {{ isEditMode ? 'Lưu thay đổi' : 'Tạo Task' }}
          </button>
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

const loadData = async () => {
  try {
    const [tRes, eRes] = await Promise.all([
      taskService.getAll(),
      taskService.getEmployees()
    ]);

    let employeeList = [];
    if (Array.isArray(eRes.data)) {
      employeeList = eRes.data;
    } else if (eRes.data && typeof eRes.data === 'object') {
      employeeList = eRes.data.data || eRes.data.content || eRes.data.result || eRes.data.items || [];
    }

    employees.value = employeeList;
    tasks.value = Array.isArray(tRes.data) ? tRes.data : (tRes.data?.data || tRes.data?.content || []);

    if (selectedTask.value && tasks.value.length > 0) {
      const found = tasks.value.find(t => t.taskId === selectedTask.value.taskId);
      if (found) selectedTask.value = found;
    }
  } catch (err) {
    console.error("!!! LỖI NGHIÊM TRỌNG KHI LOAD DATA:", err);
    tasks.value = [];
    employees.value = [];
  }
};

const getEmployeeName = (id) => {
  if (!id || !Array.isArray(employees.value)) return "Chưa có";
  const emp = employees.value.find(e => String(e.id) === String(id));
  return emp ? emp.fullName : "N/A";
}

const filteredTasks = computed(() => {
  if (!searchQuery.value) return tasks.value;
  const query = searchQuery.value.toLowerCase();
  return tasks.value.filter(task => {
    const empName = task.employeeName || getEmployeeName(task.employeeId);
    return empName.toLowerCase().includes(query) || task.taskTitle.toLowerCase().includes(query);
  });
});

// 🔥 HÀM MỞ FILE CHO ADMIN
const openEvidence = (fileName) => {
  if (!fileName) return alert("Không tìm thấy file!");

  const baseUrl = "http://localhost:8080/uploads/";
  const fullUrl = fileName.startsWith('http') ? fileName : `${baseUrl}${fileName}`;

  window.open(fullUrl, '_blank', 'noopener,noreferrer');
}

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
  if (!confirm("⚠️ Bắt nhân viên làm lại task này? Task sẽ được mở lại trên màn hình của họ.")) return;
  try {
    await taskService.reopen(id);
    alert("🔄 Đã trả lại task cho nhân viên làm lại!");
    await loadData();
  } catch (e) {
    alert("Lỗi khi yêu cầu làm lại!");
  }
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
  if (!confirm("⚠️ Bạn có chắc chắn muốn HỦY công việc này không? Nhân viên sẽ không thể làm tiếp.")) return;
  try {
    await taskService.cancel(id);
    alert("🚫 Đã hủy công việc thành công!");
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
  if (newTask.difficultyLevel < 1 || newTask.difficultyLevel > 5 || newTask.urgencyLevel < 1 || newTask.urgencyLevel > 5) {
    return alert("Mức độ Khó và Khẩn cấp phải từ 1 đến 5!");
  }

  loading.value = true;
  try {
    if (isEditMode.value) await taskService.update(editingTaskId.value, newTask);
    else await taskService.create(newTask);
    closeModal();
    await loadData();
  } catch (err) { alert("Lỗi hệ thống!") }
  finally { loading.value = false; }
}

const statusClass = (s) => {
  const m = {
    'OPEN': 'bg-secondary',
    'IN_PROGRESS': 'bg-primary',
    'DONE': 'bg-danger',
    'COMPLETED': 'bg-success',
    'CANCELLED': 'bg-dark'
  };
  return m[s] || 'bg-light text-dark';
}

onMounted(loadData)
</script>

<style scoped>
.task-row { cursor: pointer; border-left: 5px solid transparent; transition: 0.2s; }
.task-row:hover { background-color: #f8f9fa !important; }
.table-active-custom { background-color: #eef2ff !important; border-left-color: #4f46e5 !important; }
.bg-warning-subtle { background-color: #fff3cd !important; border-color: #ffecb5 !important; }
.border-dashed { border-style: dashed !important; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.4); display: flex; justify-content: center; align-items: center; z-index: 9999; backdrop-filter: blur(4px); }
.modal-card { background: white; padding: 30px; border-radius: 20px; width: 450px; }
</style>