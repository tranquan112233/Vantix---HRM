<template>
  <div class="forbidden">
    <h1>403</h1>
    <p>Bạn không có quyền truy cập trang này</p>
    <router-link to="/dashboard">
      Quay về Dashboard
    </router-link>
  </div>
</template>

<style scoped>
.forbidden {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 15px;
}

h1 {
  font-size: 80px;
  color: #ef4444;
}
</style>