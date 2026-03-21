<template>
  <div class="container">

    <h2>My Tasks</h2>

    <div class="task-grid">

      <div v-for="task in tasks" :key="task.taskId" class="task-card">

        <h4>{{ task.taskTitle }}</h4>

        <p>Difficulty: {{ task.difficultyLevel }}</p>

        <!-- STATUS -->
        <span class="badge open" v-if="task.status === 'OPEN'">OPEN</span>
        <span class="badge progress" v-if="task.status === 'IN_PROGRESS'">IN PROGRESS</span>
        <span class="badge done" v-if="task.status === 'DONE'">DONE</span>

        <!-- PROGRESS BAR -->
        <div class="progress">
          <div class="progress-bar" :style="{ width: task.progress + '%' }"></div>
        </div>
        <small>{{ task.progress || 0 }}%</small>

        <button @click="openReport(task)">Report</button>

      </div>

    </div>

    <!-- MODAL -->
    <div v-if="showModal" class="modal">

      <div class="modal-content">

        <h3>Report Task</h3>

        <textarea v-model="report.workDescription" placeholder="Work..."></textarea>

        <input type="number" v-model="report.progressPercent" placeholder="Progress %" />

        <div class="modal-actions">
          <button @click="submitReport">Submit</button>
          <button @click="showModal=false">Cancel</button>
        </div>

      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import taskService from "@/services/taskApi.service"

const tasks = ref([])
const showModal = ref(false)

const report = ref({
  taskId: null,
  workDescription: "",
  progressPercent: 0
})

const openReport = (task) => {
  report.value.taskId = task.taskId
  showModal.value = true
}

const submitReport = async () => {
  await taskService.report(report.value)
  alert("Report success")
  showModal.value = false
  loadTasks()
}

const loadTasks = async () => {
  const res = await taskService.myTasks(3) // 🔥 nhớ truyền employeeId
  tasks.value = res.data
}

onMounted(loadTasks)
</script>

<style scoped>
.container { padding:20px; background:#f5f7fa; }

.task-grid {
  display:grid;
  grid-template-columns: repeat(auto-fill,minmax(250px,1fr));
  gap:20px;
}

.task-card {
  background:white;
  padding:20px;
  border-radius:12px;
  box-shadow:0 5px 15px rgba(0,0,0,0.1);
}

.badge { padding:5px 10px; border-radius:6px; color:white; font-size:12px; }
.open { background:orange; }
.progress { background:#007bff; }
.done { background:green; }

.progress {
  background:#eee;
  height:8px;
  border-radius:10px;
  margin:10px 0;
}

.progress-bar {
  height:100%;
  background:#4caf50;
  border-radius:10px;
}

button {
  width:100%;
  margin-top:10px;
  padding:8px;
  border:none;
  background:#4caf50;
  color:white;
  border-radius:8px;
}

.modal {
  position:fixed;
  top:0; left:0;
  width:100%; height:100%;
  background:rgba(0,0,0,0.5);
  display:flex;
  justify-content:center;
  align-items:center;
}

.modal-content {
  background:white;
  padding:20px;
  border-radius:12px;
  width:300px;
}

.modal-actions {
  display:flex;
  gap:10px;
}
</style>