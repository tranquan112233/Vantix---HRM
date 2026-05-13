<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import { notificationApi } from '@/api'
import { notificationMessage, notificationTitle } from '@/utils/notifications'
import {
  canAccessPermissions,
  filterMenuByPermissions,
  findFirstAccessiblePath,
  menuConfig,
} from '@/config/menu.config'
import AppFooter from '@/components/AppFooter.vue'
import AppHeader from '@/components/AppHeader.vue'
import SidebarMenuItem from '@/components/SidebarMenuItem.vue'

const auth = useAuthStore()
const settings = useSettingsStore()
const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)

const pageTitle = computed(() => settings.t(route.meta.titleKey || route.meta.title || 'menu.dashboard'))
const visibleMenuItems = computed(() => filterMenuByPermissions(undefined, auth.permissions))
const defaultOpeneds = computed(() => collectOpenMenuIndexes(visibleMenuItems.value, route.path))
const canViewNotifications = computed(() => auth.canAccess(['NOTIFICATION_VIEW']))

// Inbox notifications
const unreadCount = ref(0)
const recentNotifications = ref([])
let pollTimer = null
let permissionTimer = null
let permissionRefreshPromise = null
let socketReconnectTimer = null

const PERMISSION_REFRESH_INTERVAL = 5000
const NOTIFICATION_POLL_INTERVAL = 30000
const NOTIFICATION_SOCKET_RECONNECT_DELAY = 5000
let notificationSocket = null

function itemIndex(item) {
  return item.index || item.path || item.name || item.label
}

function containsActivePath(item, path) {
  if (item.path === path) {
    return true
  }

  return item.children?.some(child => containsActivePath(child, path)) || false
}

function collectOpenMenuIndexes(items, path) {
  return items.flatMap(item => {
    if (!item.children?.length) {
      return []
    }

    const childOpeneds = collectOpenMenuIndexes(item.children, path)
    if (item.type === 'group' || !containsActivePath(item, path)) {
      return childOpeneds
    }

    return [itemIndex(item), ...childOpeneds]
  })
}

async function fetchNotifications() {
  if (!canViewNotifications.value) {
    unreadCount.value = 0
    recentNotifications.value = []
    return
  }

  try {
    const [countRes, listRes] = await Promise.all([
      notificationApi.unreadCount(),
      notificationApi.list(),
    ])
    unreadCount.value = countRes.data.count
    recentNotifications.value = listRes.data.slice(0, 5)
  } catch {}
}

function handleNotificationMessage(event) {
  fetchNotifications()

  if (!event?.data) return
  try {
    const envelope = JSON.parse(event.data)
    const notification = envelope?.type === 'notification' ? envelope.payload : null
    if (!notification?.title && !notification?.titleKey) return

    ElNotification({
      title: notificationTitle(notification, settings),
      message: notificationMessage(notification, settings),
      type: 'info',
      duration: 4500,
      position: 'bottom-right',
    })
  } catch {
    // Ignore malformed socket payloads and keep polling as fallback.
  }
}


async function handleMarkRead(n) {
  if (n.status !== 'READ') {
    try {
      await notificationApi.markAsRead(n.id)
      n.status = 'READ'
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {}
  }
  navigateToNotification(n)
}

function navigateToNotification(n) {
  const url = n?.targetUrl?.trim()
  if (!url) return
  if (/^https?:\/\//i.test(url)) {
    window.open(url, '_blank', 'noopener')
    return
  }
  const path = url.startsWith('/') ? url : `/${url}`
  if (path !== route.fullPath) {
    router.push(path).catch(() => {})
  }
}

async function handleMarkAllRead() {
  try {
    await notificationApi.markAllAsRead()
    recentNotifications.value.forEach(n => { n.status = 'READ' })
    unreadCount.value = 0
  } catch {}
}

function startNotificationPolling() {
  if (pollTimer || !canViewNotifications.value) return
  fetchNotifications()
  pollTimer = setInterval(fetchNotifications, NOTIFICATION_POLL_INTERVAL)
}

function stopNotificationPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  unreadCount.value = 0
  recentNotifications.value = []
}

function startNotificationSocket() {
  if (notificationSocket || !canViewNotifications.value || !auth.token) return

  if (socketReconnectTimer) {
    clearTimeout(socketReconnectTimer)
    socketReconnectTimer = null
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  notificationSocket = new WebSocket(
    `${protocol}//${window.location.host}/ws/notifications?access_token=${encodeURIComponent(auth.token)}`
  )
  notificationSocket.onmessage = handleNotificationMessage
  notificationSocket.onclose = () => {
    notificationSocket = null
    if (canViewNotifications.value && auth.token) {
      socketReconnectTimer = setTimeout(startNotificationSocket, NOTIFICATION_SOCKET_RECONNECT_DELAY)
    }
  }
  notificationSocket.onerror = () => notificationSocket?.close()
}

function stopNotificationSocket() {
  if (socketReconnectTimer) {
    clearTimeout(socketReconnectTimer)
    socketReconnectTimer = null
  }

  if (!notificationSocket) return

  const socket = notificationSocket
  notificationSocket = null
  socket.onclose = null
  socket.onmessage = null
  socket.onerror = null
  socket.close()
}

function redirectIfCurrentRouteDenied() {
  const allowed = canAccessPermissions(
    route.meta.permissions || [],
    auth.permissions,
    route.meta.permissionMode || 'all'
  )

  if (!allowed) {
    router.replace(findFirstAccessiblePath(menuConfig, auth.permissions))
  }
}

async function refreshPermissions() {
  if (permissionRefreshPromise) {
    return permissionRefreshPromise
  }

  permissionRefreshPromise = auth.refreshCurrentUser()
    .then(() => redirectIfCurrentRouteDenied())
    .catch(() => {})
    .finally(() => {
      permissionRefreshPromise = null
    })

  return permissionRefreshPromise
}

function handleWindowFocus() {
  refreshPermissions()
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    refreshPermissions()
  }
}

watch(canViewNotifications, (allowed) => {
  if (allowed) {
    startNotificationPolling()
    startNotificationSocket()
  } else {
    stopNotificationPolling()
    stopNotificationSocket()
  }
}, { immediate: true })

watch(
  () => auth.token,
  () => {
    stopNotificationSocket()
    if (canViewNotifications.value) {
      startNotificationSocket()
    }
  }
)

watch(
  () => [route.fullPath, auth.permissions.join('|')],
  () => redirectIfCurrentRouteDenied()
)

onMounted(() => {
  refreshPermissions()
  permissionTimer = setInterval(refreshPermissions, PERMISSION_REFRESH_INTERVAL)
  window.addEventListener('focus', handleWindowFocus)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopNotificationSocket()
  stopNotificationPolling()
  if (permissionTimer) clearInterval(permissionTimer)
  window.removeEventListener('focus', handleWindowFocus)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '60px' : '220px'" class="layout-sidebar" :class="{ 'is-collapsed': isCollapsed }">
      <div class="sidebar-logo">
        <div class="logo-icon">V</div>
        <transition name="fade">
          <span v-if="!isCollapsed" class="logo-text">Vantix</span>
        </transition>
      </div>

      <el-menu
        :default-active="route.path"
        router
        :collapse="isCollapsed"
        :collapse-transition="false"
        :default-openeds="defaultOpeneds"
        :unique-opened="false"
        background-color="transparent"
        text-color="#C7D2FE"
        active-text-color="#FFFFFF"
        popper-class="sidebar-menu-popper"
        class="sidebar-menu"
      >
        <SidebarMenuItem
          v-for="item in visibleMenuItems"
          :key="item.path || item.name"
          :item="item"
        />
      </el-menu>

    </el-aside>

    <!-- Main -->
    <el-container direction="vertical">
      <AppHeader
        :collapsed="isCollapsed"
        :page-title="pageTitle"
        :can-view-notifications="canViewNotifications"
        :unread-count="unreadCount"
        :recent-notifications="recentNotifications"
        @toggle-sidebar="isCollapsed = !isCollapsed"
        @mark-read="handleMarkRead"
        @mark-all-read="handleMarkAllRead"
      />

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <AppFooter />
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.layout-sidebar {
  background: var(--vx-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-right: none;
  contain: layout paint;
}

.sidebar-logo {
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.layout-sidebar.is-collapsed .sidebar-logo {
  padding: 0;
  justify-content: center;
}
.logo-icon {
  width: 30px;
  height: 30px;
  background: var(--vx-primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: var(--vx-font-size-lg);
  flex-shrink: 0;
}
.logo-text {
  color: white;
  font-size: var(--vx-font-size-xl);
  font-weight: 700;
  letter-spacing: 0;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  padding: 6px;
  overflow-y: auto;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #C7D2FE;
  --el-menu-active-color: #FFFFFF;
  --el-menu-hover-bg-color: rgba(255,255,255,0.06);
  --el-menu-hover-text-color: #FFFFFF;
}
.sidebar-menu :deep(.el-menu-item-group__title) {
  height: auto;
  line-height: 1;
  padding: 14px 12px 6px !important;
  color: rgba(199, 210, 254, 0.72);
  font-size: var(--vx-font-size-2xs);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.sidebar-menu.el-menu--collapse :deep(.el-menu-item-group__title) {
  display: none;
}
.sidebar-menu :deep(.el-sub-menu__title),
.sidebar-menu :deep(.el-menu-item) {
  border-radius: 7px;
  margin-bottom: 2px;
  height: 38px;
  line-height: 38px;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.sidebar-menu :deep(.el-sub-menu__title),
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-menu-item .el-menu-tooltip__trigger) {
  display: flex;
  align-items: center;
}
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #C7D2FE !important;
}
.sidebar-menu :deep(.el-sub-menu__title:hover),
.sidebar-menu :deep(.el-menu-item:hover:not(.is-active)) {
  background: rgba(255,255,255,0.06) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--vx-sidebar-active) !important;
  color: #FFFFFF !important;
}
.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #FFFFFF !important;
}
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(255,255,255,0.04) !important;
  background: color-mix(in srgb, var(--vx-sidebar-active) 14%, transparent) !important;
  border-radius: 8px;
  margin: 2px 0 5px;
  padding: 3px;
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  height: 36px;
  line-height: 36px;
  padding-left: 40px !important;
}
.sidebar-menu.el-menu--collapse {
  width: 100%;
}
.sidebar-menu.el-menu--collapse :deep(.el-menu-item),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title),
.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-menu-tooltip__trigger) {
  width: 48px;
  min-width: 48px;
  height: 38px;
  padding: 0 !important;
  justify-content: center;
}
.sidebar-menu.el-menu--collapse :deep(.el-icon) {
  width: 20px;
  height: 20px;
  margin: 0 !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__icon-arrow) {
  display: none !important;
}
.sidebar-menu.el-menu--collapse :deep(.el-menu-item-group > ul) {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu),
.sidebar-menu.el-menu--collapse :deep(.el-menu-item) {
  display: flex;
  justify-content: center;
}

/* Main */
.layout-main {
  background: var(--vx-bg);
  padding: 24px;
  overflow-y: auto;
}
</style>

<style>
.sidebar-menu-popper {
  border: none !important;
  background: var(--vx-sidebar-bg) !important;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.24) !important;
}

.sidebar-menu-popper .el-menu--popup {
  min-width: 210px;
  padding: 8px;
  border: none;
  border-radius: 8px;
  background: var(--vx-sidebar-bg) !important;
}

.sidebar-menu-popper .el-menu {
  border: none !important;
  background: var(--vx-sidebar-bg) !important;
}

.sidebar-menu-popper .el-menu-item,
.sidebar-menu-popper .el-sub-menu__title {
  height: 42px;
  line-height: 42px;
  margin-bottom: 2px;
  border-radius: 8px;
  color: #C7D2FE !important;
  background: transparent !important;
}

.sidebar-menu-popper .el-menu-item:hover:not(.is-active),
.sidebar-menu-popper .el-sub-menu__title:hover {
  color: #FFFFFF !important;
  background: rgba(255,255,255,0.08) !important;
}

.sidebar-menu-popper .el-menu-item.is-active,
.sidebar-menu-popper .el-sub-menu.is-active > .el-sub-menu__title {
  color: #FFFFFF !important;
  background: var(--vx-sidebar-active) !important;
}
</style>
