<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { systemLogApi } from '@/api'
import { DEFAULT_PAGE_SIZE, applyPage, pageParams } from '@/utils/pagination'
import { useSettingsStore } from '@/stores/settings'
import { ElMessage } from 'element-plus'
import ExportActions from '@/components/ExportActions.vue'

const settings = useSettingsStore()
const loading = ref(false)
const logs = ref([])
const modules = ref([])
const selectedLog = ref(null)
const detailVisible = ref(false)
const filters = reactive({
  keyword: '',
  level: '',
  module: '',
})
const pagination = reactive({ page: 1, size: DEFAULT_PAGE_SIZE, total: 0 })

const levelOptions = [
  { value: 'INFO', label: 'INFO', type: 'info' },
  { value: 'WARN', label: 'WARN', type: 'warning' },
  { value: 'ERROR', label: 'ERROR', type: 'danger' },
]

const locale = computed(() => settings.locale === 'vi' ? 'vi-VN' : 'en-US')

onMounted(() => {
  fetchModules()
  fetchLogs()
})

async function fetchModules() {
  try {
    const { data } = await systemLogApi.modules()
    modules.value = data
  } catch {}
}

async function fetchLogs() {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword,
      ...pageParams(pagination),
    }
    if (filters.level) params.level = filters.level
    if (filters.module) params.module = filters.module

    const { data } = await systemLogApi.list(params)
    applyPage(data, logs, pagination)
  } catch {
    ElMessage.error(settings.t('logs.unavailable'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchLogs()
}

function handleReset() {
  filters.keyword = ''
  filters.level = ''
  filters.module = ''
  pagination.page = 1
  fetchLogs()
}

function handlePageChange(page) {
  pagination.page = page
  fetchLogs()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchLogs()
}

function levelType(level) {
  return levelOptions.find(option => option.value === level)?.type || 'info'
}

function formatDate(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat(locale.value, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

function openDetails(row) {
  selectedLog.value = row
  detailVisible.value = true
}

const exportColumns = computed(() => [
  { label: settings.t('common.createdAt'), format: row => formatDate(row.createdAt) },
  { prop: 'level', label: settings.t('logs.level') },
  { prop: 'module', label: settings.t('logs.module') },
  { prop: 'action', label: settings.t('logs.action') },
  { label: settings.t('logs.actor'), format: row => row.actorUsername || '' },
  { label: settings.t('logs.entity'), format: row => `${row.entityName || ''}${row.entityId ? ' #' + row.entityId : ''}` },
  { prop: 'ipAddress', label: settings.t('logs.ipAddress') },
  { prop: 'description', label: settings.t('common.description') },
])

async function fetchAllLogs() {
  const params = { keyword: filters.keyword, page: 1, size: 10000 }
  if (filters.level) params.level = filters.level
  if (filters.module) params.module = filters.module
  const { data } = await systemLogApi.list(params)
  return Array.isArray(data) ? data : data.content || []
}
</script>

<template>
  <div class="page-card">
    <div class="table-toolbar">
      <div class="log-filters">
        <el-input
          v-model="filters.keyword"
          :placeholder="settings.t('logs.searchPlaceholder')"
          clearable
          class="log-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="filters.level"
          :placeholder="settings.t('logs.level')"
          clearable
          class="log-filter"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option
            v-for="level in levelOptions"
            :key="level.value"
            :label="level.label"
            :value="level.value"
          />
        </el-select>

        <el-select
          v-model="filters.module"
          :placeholder="settings.t('logs.module')"
          clearable
          filterable
          class="log-filter"
          @change="handleSearch"
          @clear="handleSearch"
        >
          <el-option
            v-for="module in modules"
            :key="module"
            :label="module"
            :value="module"
          />
        </el-select>
      </div>

      <div class="log-actions">
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          {{ settings.t('common.reset') }}
        </el-button>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ settings.t('common.search') }}
        </el-button>
        <ExportActions
          filename="system-logs"
          title="System Logs"
          :columns="exportColumns"
          :fetch-all="fetchAllLogs"
        />
      </div>
    </div>

    <el-table :data="logs" v-loading="loading" stripe style="width:100%" :empty-text="settings.t('common.noData')">
      <el-table-column :label="settings.t('common.createdAt')" min-width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column :label="settings.t('logs.level')" width="100">
        <template #default="{ row }">
          <el-tag :type="levelType(row.level)" effect="light" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" :label="settings.t('logs.module')" width="140" />
      <el-table-column prop="action" :label="settings.t('logs.action')" min-width="160" />
      <el-table-column :label="settings.t('logs.actor')" min-width="150">
        <template #default="{ row }">
          {{ row.actorUsername || '-' }}
        </template>
      </el-table-column>
      <el-table-column :label="settings.t('logs.entity')" min-width="160">
        <template #default="{ row }">
          {{ row.entityName || '-' }}<span v-if="row.entityId"> #{{ row.entityId }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" :label="settings.t('logs.ipAddress')" width="130" />
      <el-table-column :label="settings.t('common.actions')" width="110" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openDetails(row)">
            <el-icon><View /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <el-drawer
      v-model="detailVisible"
      :title="settings.t('logs.details')"
      size="420px"
      destroy-on-close
    >
      <el-descriptions v-if="selectedLog" :column="1" border>
        <el-descriptions-item :label="settings.t('common.createdAt')">
          {{ formatDate(selectedLog.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.level')">
          <el-tag :type="levelType(selectedLog.level)" effect="light" size="small">
            {{ selectedLog.level }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.module')">
          {{ selectedLog.module || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.action')">
          {{ selectedLog.action || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.actor')">
          {{ selectedLog.actorUsername || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.entity')">
          {{ selectedLog.entityName || '-' }}<span v-if="selectedLog.entityId"> #{{ selectedLog.entityId }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.ipAddress')">
          {{ selectedLog.ipAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('common.description')">
          {{ selectedLog.description || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="settings.t('logs.userAgent')">
          {{ selectedLog.userAgent || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<style scoped>
.log-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.log-search {
  width: 300px;
}

.log-filter {
  width: 170px;
}

.log-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 760px) {
  .log-search,
  .log-filter {
    width: 100%;
  }

  .log-filters,
  .log-actions {
    width: 100%;
  }
}
</style>
