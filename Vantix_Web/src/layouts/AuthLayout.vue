<template>
  <div class="auth-shell">

    <!-- LEFT: branding -->
    <div class="auth-left">
      <div class="brand">
        <div class="brand-logo">
          <img src="/favicon.svg" alt="Vantix" />
        </div>
        <span class="brand-name">Vantix HRM</span>
      </div>

      <div class="panel-body">
        <h2 class="panel-headline">Quản lý đội ngũ<br/>hiệu quả hơn.</h2>
        <p class="panel-sub">Một nền tảng cho nhân sự, lương<br/>và dữ liệu vận hành.</p>
        <div class="feature-list">
          <div v-for="f in features" :key="f.label" class="feature-item">
            <div class="feature-icon"><i :class="f.icon"></i></div>
            <span>{{ f.label }}</span>
          </div>
        </div>
      </div>

      <!-- Decorative circles -->
      <div class="deco deco-1"></div>
      <div class="deco deco-2"></div>
    </div>

    <!-- RIGHT: router view với transition -->
    <div class="auth-right">
      <RouterView v-slot="{ Component }">
        <Transition name="fade-up" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </div>

  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'

const features = [
  { icon: 'bi bi-people',        label: 'Quản lý nhân viên'        },
  { icon: 'bi bi-bar-chart',     label: 'Lương và phân tích'       },
  { icon: 'bi bi-shield-check',  label: 'Phân quyền theo vai trò'  },
]
</script>

<style scoped>
.auth-shell {
  display: flex;
  min-height: 100vh;
  background: var(--background-color);
}

/* ── Left panel ──────────────────────────────────────────────────────────── */
.auth-left {
  position: relative;
  width: 420px;
  flex-shrink: 0;
  background: linear-gradient(145deg, var(--primary-color) 0%, var(--primary-color-dark) 55%, var(--primary-color-darker) 100%);
  display: flex;
  flex-direction: column;
  padding: 40px 44px;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}
.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color-light);
  letter-spacing: -.3px;
}

.brand-logo img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  display: block;
}

.brand-logo {
  padding: 6px;
  border-radius: 10px;
  background: var(--white-with-opacity-12);
}

.panel-body {
  margin: auto 0;
  position: relative;
  z-index: 1;
}
.panel-headline {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-color-light);
  line-height: 1.2;
  margin: 0 0 12px;
  letter-spacing: -.5px;
}
.panel-sub {
  font-size: 14px;
  color: var(--text-color-lightest);
  margin: 0 0 36px;
  line-height: 1.6;
}

.feature-list { display: flex; flex-direction: column; gap: 14px; }
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-color-lighter);
  font-size: 13.5px;
  font-weight: 500;
}
.feature-icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--white-with-opacity-15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.deco-1 { width: 300px; height: 300px; bottom: -80px; right: -80px; background: var(--white-with-opacity-6); }
.deco-2 { width: 180px; height: 180px; top: 60px;    right: -40px; background: var(--white-with-opacity-4); }

/* ── Right panel ─────────────────────────────────────────────────────────── */
.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

/* ── Transition ──────────────────────────────────────────────────────────── */
.fade-up-enter-active, .fade-up-leave-active { transition: all .2s ease; }
.fade-up-enter-from { opacity: 0; transform: translateY(10px); }
.fade-up-leave-to   { opacity: 0; transform: translateY(-6px); }

/* ── Responsive ──────────────────────────────────────────────────────────── */
@media (max-width: 768px) {
  .auth-left  { width: 100%; padding: 28px 24px; }
  .panel-body { margin: 24px 0; }
  .panel-headline { font-size: 22px; }
  .auth-right { padding: 32px 16px; }
  .auth-shell { flex-direction: column; }
}
</style>
