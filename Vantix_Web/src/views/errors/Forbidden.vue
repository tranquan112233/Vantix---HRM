<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="forbidden-page mgmt-page">
    <div class="table-card forbidden-card">
      <div class="forbidden-icon">
        <i class="bi bi-shield-lock"></i>
      </div>
      <h1 class="page-title">403</h1>
      <p class="page-desc">
        Bạn không có quyền truy cập trang này hoặc quyền hiện tại chưa được cấp.
      </p>
      <div class="forbidden-actions">
        <RouterLink to="/" class="btn-primary">
          <i class="bi bi-house-door"></i> Về trang chủ
        </RouterLink>
        <button type="button" class="btn-ghost" @click="$router.back()">
          <i class="bi bi-arrow-left"></i> Quay lại
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forbidden-page {
  display: flex;
  align-items: center;
  justify-content: center;
}

.forbidden-card {
  width: min(100%, 520px);
  padding: 40px 32px;
  text-align: center;
}

.forbidden-icon {
  width: 72px;
  height: 72px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  border-radius: 22px;
  background: #eef4ff;
  color: #465fff;
  font-size: 30px;
}

.forbidden-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 24px;
  flex-wrap: wrap;
}
</style>
