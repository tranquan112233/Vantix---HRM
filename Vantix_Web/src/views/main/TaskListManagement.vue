<template>
  <div class="container">

    <div class="header">
      <h2>📋 Task Management</h2>
      <button class="btn-create" @click="openModal">+ Create Task</button>
    </div>

    <div class="table-wrapper">
      <table class="table">
        <thead>
        <tr>
          <th>Task</th>
          <th>Difficulty</th>
          <th>Urgency</th>
          <th>Point</th>
          <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="task in tasks" :key="task.taskId">
          <td class="task-title">{{ task.taskTitle }}</td>
          <td>{{ task.difficultyLevel }}</td>
          <td>{{ task.urgencyLevel }}</td>
          <td class="point">{{ task.point }}</td>
          <td>
            <span class="badge open" v-if="task.status === 'OPEN'">OPEN</span>
            <span class="badge progress" v-else-if="task.status === 'IN_PROGRESS'">IN PROGRESS</span>
            <span class="badge done" v-else-if="task.status === 'DONE'">DONE</span>
            <span class="badge cancel" v-else>CANCELLED</span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>Create New Task</h3>
          <button class="btn-close" @click="closeModal">×</button>
        </div>

        <div class="form-group">
          <label>Task Title</label>
          <input v-model="newTask.taskTitle" placeholder="Enter task title" />
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea v-model="newTask.description" placeholder="Enter description"></textarea>
        </div>

        <div class="form-group">
          <label>Difficulty</label>
          <select v-model="newTask.difficultyLevel">
            <option :value="1">1 - Easy</option>
            <option :value="2">2</option>
            <option :value="3">3 - Medium</option>
            <option :value="4">4</option>
            <option :value="5">5 - Hard</option>
          </select>
        </div>

        <div class="form-group">
          <label>Urgency</label>
          <select v-model="newTask.urgencyLevel">
            <option :value="1">1 - Low</option>
            <option :value="2">2</option>
            <option :value="3">3 - Normal</option>
            <option :value="4">4</option>
            <option :value="5">5 - High</option>
          </select>
        </div>

        <button class="btn-submit" @click="saveTask" :disabled="loading">
          {{ loading ? "Creating..." : "Create Task" }}
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import taskService from "@/services/taskApi.service"

// --- QUẢN LÝ DANH SÁCH TASK ---
const tasks = ref([])

const loadTasks = async () => {
  try {
    const res = await taskService.getAll()
    tasks.value = res.data
  } catch (error) {
    console.error(error)
  }
}

onMounted(loadTasks)

// --- QUẢN LÝ FORM CREATE TASK ---
const showModal = ref(false)
const loading = ref(false)

const newTask = reactive({
  taskTitle: "",
  description: "",
  difficultyLevel: 1,
  urgencyLevel: 1
})

const openModal = () => {
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  // Reset form sau khi đóng
  newTask.taskTitle = ""
  newTask.description = ""
  newTask.difficultyLevel = 1
  newTask.urgencyLevel = 1
}

const saveTask = async () => {
  if (!newTask.taskTitle) {
    alert("Task title is required!")
    return
  }

  try {
    loading.value = true
    await taskService.create(newTask)

    // Tải lại danh sách task mới nhất
    await loadTasks()

    // Đóng modal và reset form
    closeModal()
  } catch (err) {
    console.error(err)
    alert("Error creating task")
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
  background: #f4f6f9;
  min-height: 100vh;
}

/* HEADER */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* BUTTON CREATE */
.btn-create {
  padding: 10px 15px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
  font-weight: bold;
}

.btn-create:hover {
  background: #0056b3;
}

/* TABLE */
.table-wrapper {
  background: white;
  margin-top: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th {
  background: #f1f3f5;
  padding: 12px;
  text-align: left;
}

.table td {
  padding: 12px;
  border-top: 1px solid #eee;
}

.table tr:hover {
  background: #f9fafb;
}

.task-title {
  font-weight: 600;
}

.point {
  font-weight: bold;
  color: #28a745;
}

/* BADGE */
.badge {
  padding: 5px 10px;
  border-radius: 6px;
  color: white;
  font-size: 12px;
}
.open { background: orange; }
.progress { background: #007bff; }
.done { background: green; }
.cancel { background: gray; }

/* ========================================= */
/* MODAL STYLES */
/* ========================================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-card {
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #888;
}

.btn-close:hover {
  color: #333;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 500;
  margin-bottom: 5px;
  font-size: 14px;
}

input, textarea, select {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 14px;
}

input:focus, textarea:focus, select:focus {
  border-color: #007bff;
  outline: none;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #28a745;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
  margin-top: 10px;
}

.btn-submit:hover {
  background: #218838;
}

.btn-submit:disabled {
  background: gray;
  cursor: not-allowed;
}
</style>