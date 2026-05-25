<script setup>
import { useSettingsStore } from '@/stores/settings'

const settings = useSettingsStore()
</script>

<template>
  <div class="auth-layout">
    <aside class="auth-hero">
      <div class="auth-brand">
        <div class="brand-mark">V</div>
        <h1>Vantix</h1>
        <p>{{ settings.t('login.subtitle') }}</p>
      </div>
      <div class="auth-features">
        <div class="feature-item">
          <el-icon><User /></el-icon>
          <span>{{ settings.t('login.featureEmployees') }}</span>
        </div>
        <div class="feature-item">
          <el-icon><OfficeBuilding /></el-icon>
          <span>{{ settings.t('login.featureDepartments') }}</span>
        </div>
        <div class="feature-item">
          <el-icon><Lock /></el-icon>
          <span>{{ settings.t('login.featureAccess') }}</span>
        </div>
        <div class="feature-item">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ settings.t('login.featureAnalytics') }}</span>
        </div>
      </div>
    </aside>

    <main class="auth-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1fr) 500px;
  background: var(--vx-card-bg);
}

.auth-hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 64px;
  color: #fff;
  background: linear-gradient(135deg, #1e1b4b 0%, #3730a3 46%, #4f46e5 100%);
}

.auth-brand {
  margin-bottom: 48px;
}

.brand-mark {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.16);
  font-size: var(--vx-font-size-3xl);
  font-weight: 800;
  margin-bottom: 20px;
}

.auth-brand h1 {
  font-size: var(--vx-font-size-5xl);
  font-weight: 800;
  margin-bottom: 8px;
}

.auth-brand p {
  font-size: var(--vx-font-size-lg);
  opacity: 0.86;
}

.auth-features {
  display: grid;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: var(--vx-font-size-md);
  opacity: 0.88;
}

.auth-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 56px;
}

@media (max-width: 860px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    display: none;
  }

  .auth-main {
    padding: 32px 20px;
  }
}
</style>
