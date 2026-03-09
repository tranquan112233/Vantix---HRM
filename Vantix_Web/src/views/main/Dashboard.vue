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

import departmentService from "@/services/department.service"
import employeeService from "@/services/employee.service"

const lineChart = ref(null)
const barChart = ref(null)
const pieChart = ref(null)

const departments = ref([])
const employees = ref([])

/* ===== CARDS ===== */

const cards = ref([
  { title: "Total Employees", value: 0, icon: "bi bi-people" },
  { title: "Departments", value: 0, icon: "bi bi-building" },
  { title: "Active Employees", value: 0, icon: "bi bi-person-check" },
  { title: "Inactive Employees", value: 0, icon: "bi bi-person-x" }
])

/* ===== FETCH DATA ===== */

async function fetchData() {
  try {

    const deptRes = await departmentService.getAll()
    const empRes = await employeeService.getAll()

    departments.value = deptRes.data
    employees.value = empRes.data

    updateCards()
    createCharts()

  } catch (err) {
    console.error("Dashboard load error:", err)
  }
}

/* ===== UPDATE CARDS ===== */

function updateCards() {

  const totalEmployees = employees.value.length

  const activeEmployees = employees.value.filter(
      e => e.workStatus === "WORKING"
  ).length

  const inactiveEmployees = employees.value.filter(
      e => e.workStatus === "RESIGNED"
  ).length

  cards.value.find(c => c.title === "Total Employees").value = totalEmployees
  cards.value.find(c => c.title === "Departments").value = departments.value.length
  cards.value.find(c => c.title === "Active Employees").value = activeEmployees
  cards.value.find(c => c.title === "Inactive Employees").value = inactiveEmployees
}

/* ===== CREATE CHARTS ===== */

function createCharts() {

  /* ===== BAR CHART (EMPLOYEE BY DEPARTMENT) ===== */

  const departmentNames = departments.value.map(d => d.departmentName)

  const employeeCounts = departments.value.map(dep => {
    return employees.value.filter(
        e => e.departmentId === dep.departmentId
    ).length
  })

  new Chart(barChart.value, {
    type: "bar",
    data: {
      labels: departmentNames,
      datasets: [{
        label: "Employees",
        data: employeeCounts,
        borderWidth: 1
      }]
    },
    options: { responsive: true }
  })


  /* ===== PIE CHART ===== */

  const working = employees.value.filter(e => e.workStatus === "WORKING").length
  const resigned = employees.value.filter(e => e.workStatus === "RESIGNED").length

  new Chart(pieChart.value, {
    type: "pie",
    data: {
      labels: ["Working", "Resigned"],
      datasets: [{
        data: [working, resigned]
      }]
    },
    options: { responsive: true }
  })


  /* ===== LINE CHART ===== */

  new Chart(lineChart.value, {
    type: "line",
    data: {
      labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
      datasets: [{
        label: "Employees",
        data: [10, 20, 30, 50, 70, employees.value.length],
        borderWidth: 2,
        fill: false
      }]
    },
    options: { responsive: true }
  })

}

/* ===== MOUNT ===== */

onMounted(() => {
  fetchData()
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