<template>
  <footer class="mt-auto">
    <div class="container-fluid py-3 px-4 text-start">
      <div class="text-muted small">
        © {{ year }} <strong>Vantix HRM</strong>. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>
