<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
  pageTitle: {
    type: String,
    default: '',
  },
  canViewNotifications: {
    type: Boolean,
    default: false,
  },
  unreadCount: {
    type: Number,
    default: 0,
  },
  recentNotifications: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['toggle-sidebar', 'mark-read', 'mark-all-read'])

const auth = useAuthStore()
const settings = useSettingsStore()
const route = useRoute()
const router = useRouter()

const showCurrentBreadcrumb = computed(() => route.path !== '/')
const notifPopoverVisible = ref(false)

function goToNotifications() {
  notifPopoverVisible.value = false
  router.push('/notifications')
}

function handleInboxItemClick(n) {
  notifPopoverVisible.value = false
  emit('mark-read', n)
}

function handleUserCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }

  if (command === 'logout') {
    auth.logout()
  }
}

function timeAgo(dateStr) {
  if (!dateStr) return ''

  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return settings.t('layout.justNow')
  if (mins < 60) return `${mins} ${settings.t('time.minutesAgo')}`

  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours} ${settings.t('time.hoursAgo')}`

  const days = Math.floor(hours / 24)
  return `${days} ${settings.t('time.daysAgo')}`
}
</script>

<template>
  <el-header class="layout-header">
    <div class="header-left">
      <el-tooltip
        :content="collapsed ? settings.t('layout.expandSidebar') : settings.t('layout.collapseSidebar')"
        placement="bottom"
      >
        <el-button
          text
          circle
          class="sidebar-toggle"
          :aria-label="collapsed ? settings.t('layout.expandSidebar') : settings.t('layout.collapseSidebar')"
          @click="emit('toggle-sidebar')"
        >
          <el-icon :size="20">
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
        </el-button>
      </el-tooltip>

      <div class="header-title-block">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <el-breadcrumb separator=">" class="page-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">
            {{ settings.t('layout.home') }}
          </el-breadcrumb-item>
          <el-breadcrumb-item v-if="showCurrentBreadcrumb">
            {{ pageTitle }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <div class="header-right">
      <el-popover
        v-if="canViewNotifications"
        v-model:visible="notifPopoverVisible"
        placement="bottom-end"
        :width="380"
        trigger="click"
        popper-class="notif-popover"
      >
        <template #reference>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
            <el-button text circle class="bell-btn">
              <el-icon :size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>
        </template>

        <div class="inbox-header">
          <span class="inbox-title">{{ settings.t('layout.notifications') }}</span>
          <el-button
            v-if="unreadCount > 0"
            text
            size="small"
            type="primary"
            @click="emit('mark-all-read')"
          >
            {{ settings.t('layout.markAllRead') }}
          </el-button>
        </div>

        <div class="inbox-list">
          <div
            v-for="n in recentNotifications"
            :key="n.id"
            class="inbox-item"
            :class="{ unread: n.status === 'UNREAD', 'has-link': !!n.targetUrl }"
            @click="handleInboxItemClick(n)"
          >
            <div v-if="n.status === 'UNREAD'" class="inbox-dot" />
            <div class="inbox-body">
              <p class="inbox-item-title">{{ n.title }}</p>
              <p class="inbox-item-msg">{{ n.message }}</p>
              <span class="inbox-item-time">{{ timeAgo(n.createdAt) }}</span>
            </div>
          </div>
          <el-empty
            v-if="!recentNotifications.length"
            :description="settings.t('layout.noNotifications')"
            :image-size="60"
          />
        </div>

        <div class="inbox-footer">
          <el-button text type="primary" @click="goToNotifications">
            {{ settings.t('layout.viewAllNotifications') }}
          </el-button>
        </div>
      </el-popover>

      <el-dropdown trigger="click" @command="handleUserCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ auth.username?.charAt(0)?.toUpperCase() || '?' }}
          </el-avatar>
          <div class="user-meta">
            <span class="user-name">{{ auth.username }}</span>
            <span class="user-email">{{ auth.email }}</span>
          </div>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              {{ settings.t('layout.profile') }}
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              {{ settings.t('layout.logout') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<style scoped>
.layout-header {
  height: 72px;
  background: var(--vx-card-bg);
  border-bottom: 1px solid var(--vx-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  gap: 16px;
}

.header-left {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-toggle {
  width: 38px;
  height: 38px;
  color: var(--vx-text-secondary);
  flex: 0 0 auto;
}

.sidebar-toggle:hover {
  color: var(--vx-primary);
  background: var(--vx-primary-light);
}

.header-title-block {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.page-title {
  margin: 0;
  color: var(--vx-text);
  font-size: var(--vx-font-size-xl);
  font-weight: 600;
  line-height: 1.2;
}

.page-breadcrumb {
  font-size: var(--vx-font-size-xs);
  line-height: 1;
}

.page-breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--vx-text-secondary);
  font-weight: 500;
}

.page-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--vx-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.bell-btn {
  color: var(--vx-text-secondary);
}

.bell-btn:hover {
  color: var(--vx-primary);
}

.notif-badge :deep(.el-badge__content) {
  font-size: var(--vx-font-size-2xs);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}

.user-info:hover {
  background: #F1F5F9;
}

.user-avatar {
  background: var(--vx-primary);
  color: white;
  font-weight: 600;
}

.user-name {
  font-size: var(--vx-font-size-base);
  font-weight: 500;
  color: var(--vx-text);
  line-height: 1.2;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-email {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-2xs);
  line-height: 1.2;
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .layout-header {
    height: 68px;
    padding: 0 12px;
  }

  .header-left {
    gap: 8px;
  }

  .sidebar-toggle {
    width: 34px;
    height: 34px;
  }

  .user-info {
    padding: 6px 8px;
  }

  .user-meta {
    display: none;
  }
}
</style>

<style>
.notif-popover {
  padding: 0 !important;
}

.inbox-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--vx-border);
}

.inbox-title {
  font-size: var(--vx-font-size-md);
  font-weight: 600;
}

.inbox-list {
  max-height: 360px;
  overflow-y: auto;
}

.inbox-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.12s;
  position: relative;
}

.inbox-item:hover {
  background: #F8FAFC;
}

.inbox-item.unread {
  background: #F0F4FF;
}

.inbox-item.unread:hover {
  background: #E8EDFF;
}

.inbox-dot {
  position: absolute;
  left: 6px;
  top: 20px;
  width: 6px;
  height: 6px;
  background: var(--vx-primary);
  border-radius: 50%;
}

.inbox-body {
  flex: 1;
  min-width: 0;
}

.inbox-item-title {
  font-size: var(--vx-font-size-sm);
  font-weight: 600;
  color: var(--vx-text);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.inbox-item-msg {
  font-size: var(--vx-font-size-xs);
  color: var(--vx-text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.inbox-item-time {
  font-size: var(--vx-font-size-2xs);
  color: #94A3B8;
  margin-top: 4px;
  display: block;
}

.inbox-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid var(--vx-border);
}
</style>
