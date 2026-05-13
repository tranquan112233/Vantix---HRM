<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { notificationApi, userApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { notificationMessage, notificationTitle } from '@/utils/notifications'
import { useSettingsStore } from '@/stores/settings'
import { useAuthStore } from '@/stores/auth'

const settings = useSettingsStore()
const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const notifications = ref([])
const activeTab = ref('all')
const filterType = ref(null)
const sendDialogVisible = ref(false)
const formRef = ref(null)
const users = ref([])
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })
const allTotal = ref(0)
const unreadTotal = ref(0)

const defaultForm = { userIds: [], sendToAll: false, type: 'GENERAL', title: '', message: '', targetUrl: '' }
const form = reactive({ ...defaultForm })

const validateRecipients = (_rule, _value, callback) => {
  if (form.sendToAll) return callback()
  if (!form.userIds || form.userIds.length === 0) {
    return callback(new Error(settings.t('notification.selectRecipientRequired')))
  }
  callback()
}

const rules = computed(() => ({
  userIds: [{ validator: validateRecipients, trigger: 'change' }],
  title: [{ required: true, message: settings.t('notification.enterTitle'), trigger: 'blur' }],
  message: [{ required: true, message: settings.t('notification.enterMessage'), trigger: 'blur' }],
}))

const typeOptions = [
  { value: 'SYSTEM', labelKey: 'notification.typeSystem', color: '#EF4444' },
  { value: 'ATTENDANCE', labelKey: 'notification.typeAttendance', color: '#3B82F6' },
  { value: 'LEAVE', labelKey: 'notification.typeLeave', color: '#F59E0B' },
  { value: 'PAYROLL', labelKey: 'notification.typePayroll', color: '#10B981' },
  { value: 'TASK', labelKey: 'notification.typeTask', color: '#A855F7' },
  { value: 'GENERAL', labelKey: 'notification.typeGeneral', color: '#6366F1' },
]

const unreadCount = computed(() => unreadTotal.value)
const canSend = computed(() => auth.hasPermission('NOTIFICATION_SEND'))

onMounted(fetchData)

watch([activeTab, filterType], () => {
  pagination.page = 1
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([
      notificationApi.list(pagedNotificationParams()),
      notificationApi.unreadCount(),
    ])
    applyPage(listRes.data, notifications, pagination)
    if (activeTab.value === 'all') {
      allTotal.value = pagination.total
    }
    unreadTotal.value = countRes.data.count
  } catch { ElMessage.error(settings.t('notification.loadFailed')) }
  finally { loading.value = false }
}

function pagedNotificationParams() {
  const params = pageParams(pagination)
  if (activeTab.value === 'unread') {
    params.status = 'UNREAD'
  } else if (activeTab.value === 'read') {
    params.status = 'READ'
  }
  if (filterType.value) {
    params.type = filterType.value
  }
  return params
}

function handleResetFilters() {
  activeTab.value = 'all'
  filterType.value = null
  pagination.page = 1
  fetchData()
}

function handlePageChange(page) {
  pagination.page = page
  fetchData()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchData()
}

async function handleMarkRead(n) {
  if (n.status !== 'READ') {
    try {
      await notificationApi.markAsRead(n.id)
      n.status = 'READ'
      n.readAt = new Date().toISOString()
      fetchData()
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
    ElMessage.success(settings.t('notification.allMarkedRead'))
    fetchData()
  } catch {}
}

async function handleDelete(n) {
  try {
    await ElMessageBox.confirm(settings.t('notification.deleteConfirm'), settings.t('common.confirm'), { type: 'warning' })
    await notificationApi.delete(n.id)
    ElMessage.success(settings.t('common.deleted'))
    fetchData()
  } catch {}
}

function openSend() {
  if (!canSend.value) return
  Object.assign(form, { ...defaultForm })
  sendDialogVisible.value = true
  if (!users.value.length) {
    userApi.list()
      .then(res => { users.value = Array.isArray(res.data) ? res.data : res.data.content || [] })
      .catch(() => { users.value = [] })
  }
}

async function handleSend() {
  if (!canSend.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = {
      type: form.type,
      title: form.title,
      message: form.message,
      targetUrl: form.targetUrl,
    }
    if (form.sendToAll) {
      payload.sendToAll = true
    } else {
      payload.userIds = form.userIds
    }
    const res = await notificationApi.create(payload)
    const count = Array.isArray(res.data) ? res.data.length : 1
    ElMessage.success(`${settings.t('notification.sent')} (${count})`)
    sendDialogVisible.value = false
    fetchData()
  } catch (e) { ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong')) }
}

function typeInfo(type) {
  return typeOptions.find(t => t.value === type) || typeOptions[4]
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
  <div class="page-card">
    <div class="table-toolbar">
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <el-radio-group v-model="activeTab" size="default">
          <el-radio-button value="all">
            {{ settings.t('notification.all') }} ({{ allTotal }})
          </el-radio-button>
          <el-radio-button value="unread">
            {{ settings.t('notification.unread') }} ({{ unreadCount }})
          </el-radio-button>
          <el-radio-button value="read">
            {{ settings.t('notification.read') }}
          </el-radio-button>
        </el-radio-group>
        <el-select
          v-model="filterType"
          clearable
          :placeholder="settings.t('notification.type')"
          style="width:180px"
        >
          <el-option
            v-for="t in typeOptions"
            :key="t.value"
            :label="settings.t(t.labelKey)"
            :value="t.value"
          />
        </el-select>
        <el-button @click="handleResetFilters">
          <el-icon><RefreshLeft /></el-icon> {{ settings.t('common.reset') }}
        </el-button>
      </div>
      <div style="display:flex;gap:8px">
        <el-button @click="handleMarkAllRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon> {{ settings.t('notification.markAllRead') }}
        </el-button>
        <el-button v-if="canSend" type="primary" @click="openSend">
          <el-icon><Promotion /></el-icon> {{ settings.t('notification.send') }}
        </el-button>
      </div>
    </div>

    <!-- Notification list -->
    <div v-loading="loading" class="notification-list">
      <div
        v-for="n in notifications"
        :key="n.id"
        class="notification-item"
        :class="{ unread: n.status === 'UNREAD' }"
        @click="handleMarkRead(n)"
      >
        <div class="notif-dot" v-if="n.status === 'UNREAD'" />
        <div class="notif-icon" :style="{ background: typeInfo(n.type).color + '15', color: typeInfo(n.type).color }">
          <el-icon :size="18">
            <Bell v-if="n.type === 'GENERAL'" />
            <Monitor v-else-if="n.type === 'SYSTEM'" />
            <Clock v-else-if="n.type === 'ATTENDANCE'" />
            <Calendar v-else-if="n.type === 'LEAVE'" />
            <Wallet v-else-if="n.type === 'PAYROLL'" />
            <Bell v-else />
          </el-icon>
        </div>
        <div class="notif-content">
          <div class="notif-header">
            <span class="notif-title">{{ notificationTitle(n, settings) }}</span>
            <el-tag :color="typeInfo(n.type).color + '20'" :style="{ color: typeInfo(n.type).color, border: 'none' }" size="small">
              {{ settings.t(typeInfo(n.type).labelKey) }}
            </el-tag>
          </div>
          <p class="notif-message">{{ notificationMessage(n, settings) }}</p>
          <span class="notif-time">{{ timeAgo(n.createdAt) }}</span>
        </div>
        <el-button
          text
          type="danger"
          size="small"
          class="notif-delete"
          @click.stop="handleDelete(n)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>

      <el-empty v-if="!loading && !notifications.length" :description="settings.t('layout.noNotifications')" :image-size="100" />
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- Send notification dialog -->
    <el-dialog
      v-model="sendDialogVisible"
      :title="settings.t('notification.send')"
      width="680px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><UserFilled /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('notification.recipient') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('notification.selectRecipient') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('notification.recipient')" prop="userIds">
            <el-select
              v-model="form.userIds"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              :disabled="form.sendToAll"
              style="width:100%"
              :placeholder="settings.t('notification.selectRecipient')"
            >
              <el-option v-for="u in users" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
            </el-select>
            <el-checkbox v-model="form.sendToAll" style="margin-top:6px">
              {{ settings.t('notification.sendToAll') }}
            </el-checkbox>
          </el-form-item>
          <el-form-item :label="settings.t('notification.type')">
            <el-select v-model="form.type" style="width:100%">
              <el-option v-for="t in typeOptions" :key="t.value" :label="settings.t(t.labelKey)" :value="t.value" />
            </el-select>
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('notification.title') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('notification.message') }} &middot; {{ settings.t('notification.targetLink') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('notification.title')" prop="title">
            <el-input v-model="form.title" maxlength="150" show-word-limit />
          </el-form-item>
          <el-form-item :label="settings.t('notification.message')" prop="message">
            <el-input v-model="form.message" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item :label="settings.t('notification.targetLink')">
            <el-input v-model="form.targetUrl" placeholder="/employees" />
          </el-form-item>
        </section>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSend">{{ settings.t('notification.send') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.notification-list {
  min-height: 200px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border-bottom: 1px solid var(--vx-border);
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}
.notification-item:hover {
  background: #F8FAFC;
}
.notification-item:last-child {
  border-bottom: none;
}
.notification-item.unread {
  background: #F0F4FF;
}
.notification-item.unread:hover {
  background: #E8EDFF;
}

.notif-dot {
  position: absolute;
  left: 6px;
  top: 24px;
  width: 8px;
  height: 8px;
  background: var(--vx-primary);
  border-radius: 50%;
}

.notif-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-content {
  flex: 1;
  min-width: 0;
}
.notif-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.notif-title {
  font-size: var(--vx-font-size-base);
  font-weight: 600;
  color: var(--vx-text);
}
.notif-message {
  font-size: var(--vx-font-size-sm);
  color: var(--vx-text-secondary);
  line-height: 1.5;
  margin-bottom: 4px;
}
.notif-time {
  font-size: var(--vx-font-size-xs);
  color: #94A3B8;
}

.notif-delete {
  opacity: 0;
  transition: opacity 0.15s;
}
.notification-item:hover .notif-delete {
  opacity: 1;
}
</style>
