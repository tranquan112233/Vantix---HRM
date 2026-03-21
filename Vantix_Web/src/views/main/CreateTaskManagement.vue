<template>
  <div class="container">
    <div class="card">
      <h2>Create Task</h2>

      <div class="form-group">
        <label>Task Title</label>
        <input v-model="task.taskTitle" placeholder="Enter task title" />
      </div>

      <div class="form-group">
        <label>Description</label>
        <textarea v-model="task.description" placeholder="Enter description"></textarea>
      </div>

      <div class="form-group">
        <label>Difficulty</label>
        <select v-model="task.difficultyLevel">
          <option :value="1">1 - Easy</option>
          <option :value="2">2</option>
          <option :value="3">3 - Medium</option>
          <option :value="4">4</option>
          <option :value="5">5 - Hard</option>
        </select>
      </div>

      <div class="form-group">
        <label>Urgency</label>
        <select v-model="task.urgencyLevel">
          <option :value="1">1 - Low</option>
          <option :value="2">2</option>
          <option :value="3">3 - Normal</option>
          <option :value="4">4</option>
          <option :value="5">5 - High</option>
        </select>
      </div>

      <button @click="saveTask" :disabled="loading">
        {{ loading ? "Creating..." : "Create Task" }}
      </button>

      <p v-if="success" class="success">✅ Task created successfully!</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import taskService from "@/services/taskApi.service"

const loading = ref(false)
const success = ref(false)

const task = reactive({
  taskTitle: "",
  description: "",
  difficultyLevel: 1,
  urgencyLevel: 1
})

const saveTask = async () => {

  if (!task.taskTitle) {
    alert("Task title is required!")
    return
  }

  try {
    loading.value = true

    await taskService.create(task)

    success.value = true

    // reset form
    task.taskTitle = ""
    task.description = ""
    task.difficultyLevel = 1
    task.urgencyLevel = 1

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
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f4f6f9;
}

.card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

input, textarea, select {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ccc;
  margin-top: 5px;
  font-size: 14px;
}

input:focus, textarea:focus, select:focus {
  border-color: #4CAF50;
  outline: none;
}

button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: #4CAF50;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}

button:hover {
  background: #45a049;
}

button:disabled {
  background: gray;
  cursor: not-allowed;
}

.success {
  text-align: center;
  margin-top: 10px;
  color: green;
  font-weight: bold;
}
</style>