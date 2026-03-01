<template>
  <div class="container-fluid">

    <!-- ===== STATS ===== -->
    <div class="row g-4 mb-4">
      <div class="col-md-3" v-for="card in cards" :key="card.title">
        <div class="card stat-card shadow-sm border-0">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div>
              <p class="text-muted small mb-1">
                {{ card.title }}
              </p>
              <h4 class="fw-bold mb-0">
                {{ card.value }}
              </h4>
            </div>
            <i :class="card.icon + ' stat-icon'"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== CHARTS ===== -->
    <div class="row g-4">

      <!-- LINE -->
      <div class="col-md-6">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <h6 class="fw-bold mb-3">
              Employee Growth
            </h6>
            <canvas ref="lineChart"></canvas>
          </div>
        </div>
      </div>

      <!-- BAR -->
      <div class="col-md-6">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <h6 class="fw-bold mb-3">
              Employees by Department
            </h6>
            <canvas ref="barChart"></canvas>
          </div>
        </div>
      </div>

      <!-- PIE -->
      <div class="col-md-6">
        <div class="card shadow-sm border-0">
          <div class="card-body">
            <h6 class="fw-bold mb-3">
              Employee Status
            </h6>
            <canvas ref="pieChart"></canvas>
          </div>
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import Chart from "chart.js/auto"
import departmentService from "@/services/department.service.js"

const lineChart = ref(null)
const barChart = ref(null)
const pieChart = ref(null)

/* Cards */
const cards = ref([
  { title: "Total Employees", value: 320, icon: "bi bi-people" },
  { title: "Departments", value: 0, icon: "bi bi-building" }, // 👈 sẽ update bằng API
  { title: "Active Employees", value: 280, icon: "bi bi-person-check" },
  { title: "Inactive Employees", value: 40, icon: "bi bi-person-x" }
])

/* ================= FETCH DEPARTMENT COUNT ================= */
async function fetchDepartmentCount() {
  try {
    const { data } = await departmentService.getAll()

    // data là list departments
    const count = data.length

    // update card Departments
    const departmentCard = cards.value.find(
        c => c.title === "Departments"
    )

    if (departmentCard) {
      departmentCard.value = count
    }

  } catch (err) {
    console.error("Fetch department error:", err)
  }
}

onMounted(() => {

  fetchDepartmentCount() // 👈 gọi API

  /* LINE CHART */
  new Chart(lineChart.value, {
    type: "line",
    data: {
      labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
      datasets: [{
        label: "Employees",
        data: [200, 220, 250, 270, 300, 320],
        borderWidth: 2,
        fill: false
      }]
    },
    options: { responsive: true }
  })

  /* BAR CHART */
  new Chart(barChart.value, {
    type: "bar",
    data: {
      labels: ["HR", "IT", "Finance", "Marketing", "Sales", "Admin"],
      datasets: [{
        label: "Employees",
        data: [40, 90, 50, 60, 70, 10],
        borderWidth: 1
      }]
    },
    options: { responsive: true }
  })

  /* PIE CHART */
  new Chart(pieChart.value, {
    type: "pie",
    data: {
      labels: ["Active", "Inactive"],
      datasets: [{
        data: [280, 40]
      }]
    },
    options: { responsive: true }
  })

})
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
  transition: 0.2s;
}
.stat-card:hover {
  transform: translateY(-4px);
}
.stat-icon {
  font-size: 28px;
  opacity: 0.5;
}
</style>