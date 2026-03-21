<template>
  <div class="container">

    <div class="header">
      <h2>📋 Task Management</h2>

      <router-link to="/tasks/create">
        <button class="btn-create">+ Create Task</button>
      </router-link>
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

  </div>
</template>

<script setup>

import { ref, onMounted } from "vue"
import taskService from "@/services/taskApi.service"

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

/* BUTTON */
.btn-create {
  padding: 10px 15px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
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

/* HOVER */
.table tr:hover {
  background: #f9fafb;
}

/* TASK TITLE */
.task-title {
  font-weight: 600;
}

/* POINT */
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

.open {
  background: orange;
}

.progress {
  background: #007bff;
}

.done {
  background: green;
}

.cancel {
  background: gray;
}

</style>