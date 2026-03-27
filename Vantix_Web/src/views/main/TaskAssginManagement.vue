npm i<template>
  <div class="container">

    <div class="card">

      <h2>👨‍💼 Assign Task</h2>

      <!-- SELECT TASK -->
      <div class="form-group">
        <label>Task</label>
        <select v-model="data.taskId">
          <option disabled value="">Select Task</option>
          <option v-for="t in tasks" :key="t.taskId" :value="t.taskId">
            {{ t.taskTitle }} ({{ t.point }} pts)
          </option>
        </select>
      </div>

      <!-- SELECT EMPLOYEE -->
      <div class="form-group">
        <label>Employee</label>
        <select v-model="data.employeeId">
          <option disabled value="">Select Employee</option>
          <option v-for="e in employees" :key="e.employeeId" :value="e.employeeId">
            {{ e.fullName }}
          </option>
        </select>
      </div>

      <!-- BUTTON -->
      <button @click="assign" :disabled="loading">
        {{ loading ? "Assigning..." : "Assign Task" }}
      </button>

      <!-- SUCCESS -->
      <p v-if="success" class="success">✅ Assigned successfully!</p>

    </div>

  </div>
</template>

<script setup>

import { ref, onMounted } from "vue"
import taskService from "@/services/taskApi.service"
import axios from "axios"

const tasks = ref([])
const employees = ref([])
const loading = ref(false)
const success = ref(false)

const data = ref({
  taskId: "",
  employeeId: ""
})

const assign = async () => {

  if (!data.value.taskId || !data.value.employeeId) {
    alert("Please select task and employee!")
    return
  }

  try {
    loading.value = true

    await taskService.assign(data.value)

    success.value = true

    // reset form
    data.value.taskId = ""
    data.value.employeeId = ""

  } catch (e) {
    console.error(e)
    alert("Assign failed!")
  } finally {
    loading.value = false
  }

}

onMounted(async () => {

  try {

    const t = await taskService.getAll()
    tasks.value = t.data

    const e = await axios.get("http://localhost:8080/api/employees")
    employees.value = e.data

  } catch (err) {
    console.error(err)
  }

})

</script>

<style scoped>

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f4f6f9;
}

/* CARD */
.card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

/* TITLE */
h2 {
  text-align: center;
  margin-bottom: 20px;
}

/* FORM */
.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

select {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ccc;
  margin-top: 5px;
  font-size: 14px;
}

/* BUTTON */
button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: #28a745;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}

button:hover {
  background: #218838;
}

button:disabled {
  background: gray;
  cursor: not-allowed;
}

/* SUCCESS */
.success {
  text-align: center;
  margin-top: 10px;
  color: green;
  font-weight: bold;
}

</style>