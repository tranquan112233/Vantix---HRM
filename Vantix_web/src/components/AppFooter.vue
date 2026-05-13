<script setup>
const year = new Date().getFullYear()
</script>

<template>
  <el-footer class="layout-footer">
    <div class="footer-inner">
      <span class="footer-brand">Vantix</span>
      <span class="footer-meta">{{ year }}</span>
    </div>
  </el-footer>
</template>

<style scoped>
.layout-footer {
  height: 44px;
  background: var(--vx-card-bg);
  border-top: 1px solid var(--vx-border);
  padding: 0 24px;
  flex: 0 0 auto;
}

.footer-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
}

.footer-brand {
  color: var(--vx-text);
  font-weight: 600;
}

.footer-meta {
  font-weight: 500;
}

@media (max-width: 720px) {
  .layout-footer {
    padding: 0 12px;
  }
}
</style>
