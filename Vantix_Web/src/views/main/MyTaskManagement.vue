<template>
  <div class="container">
    <div class="header-section">
      <h2>🧑‍💻 My Tasks Workspace</h2>

      <div class="user-selector">
        <label>View tasks as: </label>
        <select v-model="currentEmployeeId" @change="loadTasks">
          <option disabled value="">-- Select an employee --</option>
          <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">
            {{ emp.fullName }} (ID: {{ emp.employeeId }})
          </option>
        </select>
      </div>
    </div>

    <div v-if="!currentEmployeeId" class="empty-state">
      <p>Please select an employee above to view assigned tasks.</p>
    </div>

    <div v-else-if="tasks.length === 0" class="empty-state">
      <p>🎉 Awesome! You have no pending tasks right now.</p>
    </div>

    <div v-else class="task-grid">
      <div v-for="task in tasks" :key="task.taskId" class="task-card">
        <div class="card-header">
          <h4>{{ task.taskTitle }}</h4>
          <span class="badge open" v-if="task.status === 'OPEN'">OPEN</span>
          <span class="badge progress" v-else-if="task.status === 'IN_PROGRESS'">IN PROGRESS</span>
          <span class="badge done" v-else-if="task.status === 'DONE'">DONE</span>
        </div>

        <div class="card-body">
          <p><strong>Difficulty:</strong> Level {{ task.difficultyLevel }}</p>
          <p><strong>Urgency:</strong> Level {{ task.urgencyLevel }}</p>
          <p><strong>Points:</strong> {{ task.point }} pts</p>
        </div>

        <div class="card-footer">
          <button class="btn-report" @click="openReport(task)">📝 Report Progress</button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>Report Progress</h3>

        <div class="form-group">
          <label>What did you do?</label>
          <textarea v-model="report.workDescription" rows="4" placeholder="Describe your work..."></textarea>
        </div>

        <div class="form-group">
          <label>Progress (%)</label>
          <input type="number" v-model="report.progressPercent" min="0" max="100" placeholder="E.g. 50" />
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeModal">Cancel</button>
          <button class="btn-submit" @click="submitReport">Submit Report</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import taskService from "@/services/taskApi.service"
import axios from "axios"

const employees = ref([])
const currentEmployeeId = ref("")
const tasks = ref([])
const showModal = ref(false)

const report = ref({
  taskId: null,
  workDescription: "",
  progressPercent: 0
})

// Lấy danh sách nhân viên để làm dropdown chọn user
const loadEmployees = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/employees")
    employees.value = res.data
  } catch (error) {
    console.error("Error loading employees:", error)
  }
}

// Lấy task theo đúng ID nhân viên đang được chọn
const loadTasks = async () => {
  if (!currentEmployeeId.value) return;
  try {
    const res = await taskService.myTasks(currentEmployeeId.value)
    tasks.value = res.data
  } catch (error) {
    console.error("Error loading tasks:", error)
  }
}

const openReport = (task) => {
  report.value.taskId = task.taskId
  report.value.workDescription = ""
  report.value.progressPercent = 0
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const submitReport = async () => {
  if (!report.value.workDescription) {
    alert("Please enter work description!");
    return;
  }

  try {
    // Gửi kèm ID nhân viên để Backend biết ai đang report
    const payload = {
      taskId: report.value.taskId,
      employeeId: currentEmployeeId.value,
      workDescription: report.value.workDescription,
      progressPercent: report.value.progressPercent
    }

    await taskService.report(payload)
    alert("Report submitted successfully!")
    closeModal()
    loadTasks() // Load lại danh sách
  } catch (error) {
    console.error(error)
    alert("Error submitting report. Check console!")
  }
}

onMounted(() => {
  loadEmployees()
})
</script>

<style scoped>
.container {
  padding: 30px;
  background: #f8f9fa;
  min-height: 100vh;
  font-family: Arial, sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.user-selector select {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 16px;
  margin-left: 10px;
}

.empty-state {
  text-align: center;
  padding: 50px;
  background: white;
  border-radius: 12px;
  color: #6c757d;
  font-size: 18px;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.task-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  transition: transform 0.2s;
}

.task-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.card-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.card-body p {
  margin: 5px 0;
  color: #555;
}

.card-footer {
  margin-top: 20px;
  text-align: right;
}

.badge {
  padding: 5px 10px;
  border-radius: 20px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}
.open { background: #ff9800; }
.progress { background: #2196f3; }
.done { background: #4caf50; }

.btn-report {
  background: #6c5ce7;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.btn-report:hover {
  background: #5b4bc4;
}

/* MODAL */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 12px;
  width: 400px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: bold;
  margin-bottom: 5px;
}

.form-group input, .form-group textarea {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel {
  background: #ccc;
  border: none;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-submit {
  background: #4caf50;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
}
</style>