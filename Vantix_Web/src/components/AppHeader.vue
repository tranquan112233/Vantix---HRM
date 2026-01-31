<template>
  <nav class="navbar navbar-expand-lg navbar-dark app-header">
    <div class="container-fluid">
      <a class="navbar-brand" href="#">Vantix HRM</a>

      <div class="navbar-nav">
        <router-link class="nav-link" to="/roles">Vai trò</router-link>
        <router-link class="nav-link" to="/departments">Phòng ban</router-link>
        <router-link class="nav-link" to="/users">Nhân viên</router-link>
      </div>
    </div>
  </nav>
</template>

<script>
  export default {
    name: "AppHeader"
  }
</script>
