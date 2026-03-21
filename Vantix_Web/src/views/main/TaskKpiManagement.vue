<template>
  <div class="container">

    <h3>KPI Ranking</h3>

    <table class="table table-striped">
      <thead>
      <tr>
        <th>Employee</th>
        <th>KPI Point</th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="k in kpis" :key="k.employee">
        <td>{{ k.employee }}</td>
        <td>{{ k.kpiPoint }}</td>
      </tr>
      </tbody>

    </table>

  </div>
</template>

<script setup>

import { ref, onMounted } from "vue"
import axios from "axios"

const kpis = ref([])

onMounted(async () => {
  const res = await axios.get("http://localhost:8080/api/kpi")
  kpis.value = res.data
})

</script>