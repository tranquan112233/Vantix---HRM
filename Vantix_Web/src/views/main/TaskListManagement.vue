<template>
  <div class="admin-workspace p-4">
    <div class="row">
      <div class="col-md-8">
        <div class="card border-0 shadow-sm p-4 h-100 rounded-4">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold m-0 text-dark">📋 Task Inventory</h2>
            <button class="btn btn-primary fw-bold px-4 rounded-pill shadow-sm" @click="openCreateModal">+ New Task</button>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle">
              <thead class="table-light">
              <tr>
                <th>Task Title</th>
                <th class="text-center">D/U</th>
                <th class="text-center">Points</th>
                <th>Status</th>
                <th class="text-end">Actions</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="task in tasks" :key="task.taskId" @click="selectTask(task)" class="task-row"
                  :class="{ 'table-active-custom': selectedTask?.taskId === task.taskId }">
                <td class="fw-bold" :class="task.status === 'DONE' ? 'text-danger' : 'text-dark'">
                  {{ task.taskTitle }}
                  <span v-if="task.status === 'DONE'" class="badge bg-danger ms-2">MỚI NỘP</span>
                </td>
                <td class="text-center small">{{ task.difficultyLevel }}/{{ task.urgencyLevel }}</td>
                <td class="text-center text-success fw-bold">{{ task.point }} pts</td>
                <td><span :class="statusClass(task.status)" class="badge px-3 py-1 rounded-pill">{{ task.status || 'OPEN' }}</span></td>
                <td class="text-end">
                  <button v-if="task.status !== 'COMPLETED'" class="btn btn-sm btn-outline-warning rounded-pill" @click.stop="openEditModal(task)">Sửa</button>
                  <span v-else class="text-success small fw-bold">✔ Finalized</span>
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
                <div class="mb-3">
                  <div class="small text-muted mb-1 fw-bold">Người làm:</div>
                  <div class="fw-bold">👤 {{ selectedTask.employeeName || getEmployeeName(selectedTask.employeeId) }}</div>
                </div>
                <div v-if="selectedTask.fileUrl">
                  <button @click="openEvidence(selectedTask.fileUrl)" class="btn btn-sm btn-primary w-100 fw-bold rounded-pill">
                    📄 Xem file minh chứng
                  </button>
                </div>
              </div>

              <div v-if="selectedTask.status === 'DONE'">
                <button class="btn btn-success w-100 fw-bold py-2 rounded-3 shadow" @click="handleApprove(selectedTask.taskId)">
                  ✅ PHÊ DUYỆT & CHỐT ĐIỂM
                </button>
              </div>
            </div>

            <div v-if="selectedTask.status === 'OPEN'" class="mb-4">
              <label class="form-label fw-bold small text-muted">Giao cho nhân viên</label>
              <select v-model="assignData.employeeId" class="form-select rounded-3 mb-3">
                <option value="" disabled>-- Chọn --</option>
                <option v-for="e in employees" :key="e.employeeId" :value="e.employeeId">{{ e.fullName }}</option>
              </select>
              <button class="btn btn-success w-100 fw-bold" @click="handleAssign" :disabled="!assignData.employeeId">Giao việc</button>
            </div>
          </div>
          <div v-else class="p-5 text-center text-muted border border-dashed rounded-4">Chọn task để xem</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import taskService from "@/services/taskApi.service"
import axios from "axios"

const tasks = ref([]); const employees = ref([]); const selectedTask = ref(null);
const assignData = reactive({ taskId: "", employeeId: "" });

const loadData = async () => {
  try {
    const [tRes, eRes] = await Promise.all([taskService.getAll(), axios.get("http://localhost:8080/api/employees")]);
    tasks.value = tRes.data; employees.value = eRes.data;
    if (selectedTask.value) selectedTask.value = tasks.value.find(t => t.taskId === selectedTask.value.taskId);
  } catch (err) { console.error(err) }
}

const getEmployeeName = (id) => {
  const emp = employees.value.find(e => String(e.employeeId) === String(id));
  return emp ? emp.fullName : "Chưa có";
}

const openEvidence = (url) => { window.open(url, '_blank', 'noopener,noreferrer'); }

const selectTask = (task) => { selectedTask.value = task; assignData.taskId = task.taskId; assignData.employeeId = task.employeeId || ""; }

const handleApprove = async (id) => {
  if (!confirm("Duyệt task này?")) return;
  try {
    const task = tasks.value.find(t => t.taskId === id);
    task.status = 'COMPLETED'; // Chốt sổ
    await taskService.update(id, task);
    alert("✅ Đã chốt thành công!");
    await loadData();
  } catch (e) { alert("Lỗi!"); }
}

const handleAssign = async () => {
  try { await taskService.assign(assignData); alert("Đã giao!"); await loadData(); } catch (e) { alert("Lỗi!"); }
}

const statusClass = (s) => {
  const m = { 'OPEN': 'bg-secondary', 'IN_PROGRESS': 'bg-primary', 'DONE': 'bg-danger', 'COMPLETED': 'bg-success' };
  return m[s] || 'bg-dark';
}

onMounted(loadData)
</script>

<style scoped>
.task-row { cursor: pointer; border-left: 5px solid transparent; transition: 0.2s; }
.table-active-custom { background-color: #eef2ff !important; border-left-color: #4f46e5 !important; }
.bg-warning-subtle { background-color: #fff3cd !important; border-color: #ffecb5 !important; }
</style>