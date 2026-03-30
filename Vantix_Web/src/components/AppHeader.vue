<template>
  <header class="app-header">
    <button class="icon-btn" title="Toggle sidebar" @click="$emit('toggle-sidebar')">
      <i class="bi bi-list fs-5"></i>
    </button>
    <div class="header-right">
      <div class="dropdown">
        <button class="user-btn dropdown-toggle" data-bs-toggle="dropdown">
          <div class="avatar">{{ firstLetter }}</div>
          <div class="user-info">
            <span class="user-name">{{ username }}</span>
            <span class="user-email">{{ email }}</span>
          </div>
          <i class="bi bi-chevron-down caret"></i>
        </button>
        <div class="dropdown-menu dropdown-menu-end user-panel shadow p-2">
          <RouterLink to="/profile" class="drop-item">
            <i class="bi bi-person-circle text-muted"></i> Profile
          </RouterLink>
          <RouterLink v-if="isAdmin" to="/notifications" class="drop-item">
            <i class="bi bi-bell text-muted"></i> Quản lý thông báo
          </RouterLink>
          <hr class="dropdown-divider my-1">
          <button class="drop-item text-danger w-100" @click="logout">
            <i class="bi bi-box-arrow-right"></i> Logout
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

defineEmits(['toggle-sidebar'])
const router = useRouter()
const auth   = useAuthStore()
const username    = computed(() => auth.user?.username ?? 'User')
const email       = computed(() => auth.user?.email    ?? '')
const firstLetter = computed(() => username.value.charAt(0).toUpperCase())
const isAdmin     = computed(() => auth.userRole === 'ADMIN')
const logout = () => { auth.logout(); router.push('/auth/login') }
</script>

<style scoped>
.app-header { height: 56px; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; background: var(--bg-white); border-bottom: 1px solid var(--border); box-shadow: var(--shadow-sm); flex-shrink: 0; }
.header-right { display: flex; align-items: center; gap: 4px; }
.icon-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; border: none; background: transparent; border-radius: var(--radius-sm); color: var(--text-muted); cursor: pointer; font-size: 16px; transition: background var(--transition), color var(--transition); }
.icon-btn:hover { background: var(--bg); color: var(--text); }
.user-btn { display: flex; align-items: center; gap: 8px; border: none; background: transparent; border-radius: var(--radius-sm); padding: 4px 8px 4px 4px; cursor: pointer; max-width: 220px; transition: background var(--transition); }
.user-btn::after { display: none; }
.user-btn:hover { background: var(--bg); }
.avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--primary); color: #fff; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.user-info { display: flex; flex-direction: column; text-align: left; line-height: 1.3; max-width: 110px; min-width: 0; }
.user-name  { font-size: 12.5px; font-weight: 600; color: var(--text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-email { font-size: 11px; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.caret { font-size: 10px; color: var(--text-dim); transition: transform .18s; }
.user-btn.show .caret { transform: rotate(180deg); }
.user-panel { width: 200px; border-radius: var(--radius) !important; border: 1px solid var(--border) !important; }
.drop-item { display: flex; align-items: center; gap: 8px; padding: 7px 9px; font-size: 13px; color: var(--text); text-decoration: none; background: transparent; border: none; cursor: pointer; border-radius: var(--radius-sm); transition: background var(--transition); width: 100%; }
.drop-item:hover { background: var(--bg); }
.drop-item.text-danger:hover { background: #fef2f2; }
@media (max-width: 480px) { .user-info { display: none; } }
</style>