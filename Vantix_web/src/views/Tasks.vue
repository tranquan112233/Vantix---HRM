<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, taskApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const VIEW_STORAGE_KEY = 'vx_tasks_view_mode'

const auth = useAuthStore()
const settings = useSettingsStore()
const loading = ref(false)
const saving = ref(false)
const tasks = ref([])
const employees = ref([])
const keyword = ref('')
const viewMode = ref(localStorage.getItem(VIEW_STORAGE_KEY) || 'kanban')
const filterStatus = ref('')
const filterAssigneeId = ref(null)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailTask = ref(null)
const formRef = ref(null)
const editingId = ref(null)
const fileList = ref([])
const draggingTask = ref(null)

const defaultForm = {
  title: '',
  description: '',
  status: 'TODO',
  assigneeId: null,
  dueDate: '',
}

const page = ref(1)
const pageSize = ref(10)
const pagedTasks = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return tasks.value.slice(start, start + pageSize.value)
})
function handleSizeChange(size) { pageSize.value = size; page.value = 1 }

const form = reactive({ ...defaultForm })

const statusOptions = computed(() => [
  { value: 'TODO', label: settings.t('task.todo'), type: 'info' },
  { value: 'DOING', label: settings.t('task.doing'), type: 'warning' },
  { value: 'DONE', label: settings.t('task.done'), type: 'success' },
])

const viewOptions = computed(() => [
  { label: settings.t('task.kanban'), value: 'kanban' },
  { label: settings.t('task.list'), value: 'list' },
])

const rules = computed(() => ({
  title: [{ required: true, message: settings.t('common.required'), trigger: 'blur' }],
}))

const canCreate = computed(() => auth.canAccess(['TASK_CREATE']))
const canUpdate = computed(() => auth.canAccess(['TASK_UPDATE']))
const canDelete = computed(() => auth.canAccess(['TASK_DELETE']))
const canLoadEmployees = computed(() => auth.hasPermission('EMPLOYEE_VIEW'))

const groupedTasks = computed(() => {
  const grouped = { TODO: [], DOING: [], DONE: [] }
  tasks.value.forEach(task => {
    if (!grouped[task.status]) {
      grouped[task.status] = []
    }
    grouped[task.status].push(task)
  })
  return grouped
})

const currentAttachments = computed(() => {
  if (!editingId.value) return []
  return tasks.value.find(task => task.id === editingId.value)?.attachments || []
})

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const taskRes = await taskApi.list(taskParams())
    tasks.value = taskRes.data
    page.value = 1
    loadEmployees()
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function loadEmployees() {
  if (!canLoadEmployees.value) {
    employees.value = []
    return
  }

  try {
    const employeeRes = await employeeApi.list()
    employees.value = Array.isArray(employeeRes.data) ? employeeRes.data : employeeRes.data.items || employeeRes.data.content || []
  } catch {
    employees.value = []
  }
}

function taskParams() {
  const params = {}
  if (keyword.value) params.keyword = keyword.value
  if (filterStatus.value) params.status = filterStatus.value
  if (filterAssigneeId.value) params.assigneeId = filterAssigneeId.value
  return params
}

function handleSearch() {
  fetchData()
}

function resetFilters() {
  keyword.value = ''
  filterStatus.value = ''
  filterAssigneeId.value = null
  fetchData()
}

function setViewMode(value) {
  viewMode.value = value
  localStorage.setItem(VIEW_STORAGE_KEY, value)
}

function openCreate() {
  editingId.value = null
  Object.assign(form, defaultForm)
  fileList.value = []
  dialogVisible.value = true
}

function openDetail(task) {
  detailTask.value = task
  detailVisible.value = true
}

function openEdit(task) {
  editingId.value = task.id
  Object.assign(form, {
    title: task.title,
    description: task.description || '',
    status: task.status || 'TODO',
    assigneeId: task.assigneeId || null,
    dueDate: task.dueDate || '',
  })
  fileList.value = []
  dialogVisible.value = true
}

async function saveTask() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = { ...form }
    const response = editingId.value
      ? await taskApi.update(editingId.value, payload)
      : await taskApi.create(payload)

    const files = selectedFiles()
    if (files.length) {
      await taskApi.uploadAttachments(response.data.id, files)
    }

    dialogVisible.value = false
    ElMessage.success(editingId.value ? settings.t('common.updated') : settings.t('common.created'))
    await fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    saving.value = false
  }
}

function selectedFiles() {
  return fileList.value
    .map(item => item.raw)
    .filter(Boolean)
}

function beforeUpload() {
  return false
}

function handleFileChange(_file, files) {
  fileList.value = files
}

function handleFileRemove(_file, files) {
  fileList.value = files
}

function handleDragStart(task) {
  if (!canUpdate.value) return
  draggingTask.value = task
}

function handleDragEnd() {
  draggingTask.value = null
}

async function handleDrop(status) {
  if (!draggingTask.value || draggingTask.value.status === status) {
    draggingTask.value = null
    return
  }

  await updateTaskStatus(draggingTask.value, status)
  draggingTask.value = null
}

async function updateTaskStatus(task, status) {
  if (!canUpdate.value) return

  const oldStatus = task.status
  task.status = status
  try {
    const { data } = await taskApi.updateStatus(task.id, status)
    Object.assign(task, data)
  } catch (e) {
    task.status = oldStatus
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  }
}

async function deleteTask(task) {
  try {
    await ElMessageBox.confirm(`${settings.t('task.deleteConfirm')} "${task.title}"?`, settings.t('common.confirm'), { type: 'warning' })
    await taskApi.delete(task.id)
    ElMessage.success(settings.t('common.deleted'))
    await fetchData()
  } catch {}
}

async function deleteAttachment(attachment) {
  if (!editingId.value) return

  try {
    await ElMessageBox.confirm(`${settings.t('task.deleteAttachmentConfirm')} "${attachment.originalFileName}"?`, settings.t('common.confirm'), { type: 'warning' })
    await taskApi.deleteAttachment(editingId.value, attachment.id)
    ElMessage.success(settings.t('common.deleted'))
    await fetchData()
  } catch {}
}

async function downloadAttachment(attachment) {
  try {
    const response = await taskApi.downloadAttachment(attachment.id)
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileNameFromDisposition(response.headers['content-disposition']) || attachment.originalFileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error(settings.t('task.downloadFailed'))
  }
}

function fileNameFromDisposition(disposition) {
  if (!disposition) return ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return match?.[1] || ''
}

function statusMeta(status) {
  return statusOptions.value.find(option => option.value === status) || statusOptions.value[0]
}

function assigneeLabel(task) {
  return task.assigneeName || settings.t('task.unassigned')
}

function attachmentCount(task) {
  return task.attachments?.length || 0
}

function formatFileSize(size) {
  if (!size) return '0 KB'
  if (size < 1024 * 1024) return `${Math.ceil(size / 1024)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<template>
  <div class="tasks-page">
    <div class="page-card task-panel">
      <div class="task-toolbar">
        <div class="task-filters">
          <el-input
            v-model="keyword"
            :placeholder="settings.t('task.search')"
            clearable
            class="task-search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>

          <el-select
            v-model="filterStatus"
            clearable
            class="filter-select"
            :placeholder="settings.t('task.status')"
            @change="handleSearch"
            @clear="handleSearch"
          >
            <el-option
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>

          <el-select
            v-model="filterAssigneeId"
            clearable
            filterable
            class="filter-select"
            :placeholder="settings.t('task.assignee')"
            @change="handleSearch"
            @clear="handleSearch"
          >
            <el-option
              v-for="employee in employees"
              :key="employee.id"
              :label="`${employee.fullName} (${employee.employeeCode})`"
              :value="employee.id"
            />
          </el-select>

          <el-button @click="resetFilters">
            <el-icon><RefreshLeft /></el-icon>
            {{ settings.t('common.reset') }}
          </el-button>
        </div>

        <div class="task-actions">
          <el-radio-group :model-value="viewMode" @change="setViewMode">
            <el-radio-button
              v-for="option in viewOptions"
              :key="option.value"
              :value="option.value"
            >
              <el-icon>
                <Grid v-if="option.value === 'kanban'" />
                <List v-else />
              </el-icon>
              {{ option.label }}
            </el-radio-button>
          </el-radio-group>

          <el-button v-if="canCreate" type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon>
            {{ settings.t('task.add') }}
          </el-button>
        </div>
      </div>

      <div v-if="viewMode === 'kanban'" v-loading="loading" class="kanban-board">
        <section
          v-for="column in statusOptions"
          :key="column.value"
          class="kanban-column"
          @dragover.prevent
          @drop="handleDrop(column.value)"
        >
          <header class="kanban-column-header">
            <div>
              <h3>{{ column.label }}</h3>
              <span>{{ groupedTasks[column.value]?.length || 0 }}</span>
            </div>
          </header>

          <div class="kanban-list">
            <article
              v-for="task in groupedTasks[column.value]"
              :key="task.id"
              class="task-card"
              :draggable="canUpdate"
              @dragstart="handleDragStart(task)"
              @dragend="handleDragEnd"
              @click="openDetail(task)"
            >
              <div class="task-card-header">
                <h4>{{ task.title }}</h4>
                <el-dropdown trigger="click" @click.stop>
                  <el-button text circle size="small" @click.stop>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="openDetail(task)">
                        <el-icon><View /></el-icon>
                        {{ settings.t('task.viewDetails') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="canUpdate" @click="openEdit(task)">
                        <el-icon><Edit /></el-icon>
                        {{ settings.t('task.edit') }}
                      </el-dropdown-item>
                      <template v-if="canUpdate">
                        <el-dropdown-item
                          v-for="status in statusOptions"
                          :key="status.value"
                          @click="updateTaskStatus(task, status.value)"
                        >
                          <el-icon><Check /></el-icon>
                          {{ status.label }}
                        </el-dropdown-item>
                      </template>
                      <el-dropdown-item v-if="canDelete" divided @click="deleteTask(task)">
                        <el-icon><Delete /></el-icon>
                        {{ settings.t('common.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <p v-if="task.description" class="task-description">{{ task.description }}</p>

              <div class="task-meta">
                <span>
                  <el-icon><User /></el-icon>
                  {{ assigneeLabel(task) }}
                </span>
                <span v-if="task.dueDate">
                  <el-icon><Calendar /></el-icon>
                  {{ task.dueDate }}
                </span>
              </div>

              <div class="task-card-footer">
                <el-tag :type="statusMeta(task.status).type" effect="light" size="small">
                  {{ statusMeta(task.status).label }}
                </el-tag>
                <span class="attachment-count">
                  <el-icon><Paperclip /></el-icon>
                  {{ attachmentCount(task) }}
                </span>
              </div>
            </article>

            <el-empty
              v-if="!groupedTasks[column.value]?.length"
              :description="settings.t('common.noData')"
              :image-size="56"
            />
          </div>
        </section>
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="pagedTasks"
        stripe
        style="width:100%"
        :empty-text="settings.t('common.noData')"
      >
        <el-table-column prop="title" :label="settings.t('task.title')" min-width="220" />
        <el-table-column :label="settings.t('task.status')" width="130">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" effect="light" size="small">
              {{ statusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('task.assignee')" min-width="190">
          <template #default="{ row }">{{ assigneeLabel(row) }}</template>
        </el-table-column>
        <el-table-column prop="departmentName" :label="settings.t('employee.department')" min-width="150" />
        <el-table-column prop="dueDate" :label="settings.t('task.dueDate')" width="130" />
        <el-table-column :label="settings.t('task.attachments')" width="120">
          <template #default="{ row }">
            <span class="attachment-count">
              <el-icon><Paperclip /></el-icon>
              {{ attachmentCount(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="settings.t('common.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="info" size="small" @click="openDetail(row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button v-if="canUpdate" text type="primary" size="small" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button v-if="canDelete" text type="danger" size="small" @click="deleteTask(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="viewMode === 'list'" class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="tasks.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      :title="settings.t('task.details')"
      width="720px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <div v-if="detailTask" class="task-detail">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div style="flex:1">
              <h4 class="form-section-title">{{ detailTask.title }}</h4>
              <p class="form-section-subtitle">
                <el-tag :type="statusMeta(detailTask.status).type" effect="light" size="small">
                  {{ statusMeta(detailTask.status).label }}
                </el-tag>
              </p>
            </div>
          </div>
          <div class="task-detail-meta">
            <div>
              <span class="task-detail-label">{{ settings.t('task.assignee') }}</span>
              <span>{{ assigneeLabel(detailTask) }}</span>
            </div>
            <div v-if="detailTask.dueDate">
              <span class="task-detail-label">{{ settings.t('task.dueDate') }}</span>
              <span>{{ detailTask.dueDate }}</span>
            </div>
          </div>
          <div class="task-detail-section">
            <div class="task-detail-label">{{ settings.t('task.description') }}</div>
            <p v-if="detailTask.description" class="task-detail-description">{{ detailTask.description }}</p>
            <p v-else class="task-detail-empty">{{ settings.t('task.noDescription') }}</p>
          </div>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Paperclip /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('task.attachments') }}</h4>
              <p class="form-section-subtitle">
                {{ detailTask.attachments?.length || 0 }}
              </p>
            </div>
          </div>
          <div v-if="detailTask.attachments?.length" class="attachment-list">
            <div
              v-for="attachment in detailTask.attachments"
              :key="attachment.id"
              class="attachment-item"
            >
              <div>
                <span>{{ attachment.originalFileName }}</span>
                <small>{{ formatFileSize(attachment.fileSize) }}</small>
              </div>
              <div class="attachment-actions">
                <el-button text type="primary" size="small" @click="downloadAttachment(attachment)">
                  <el-icon><Download /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <p v-else class="task-detail-empty">{{ settings.t('task.noAttachments') }}</p>
        </section>
      </div>

      <template #footer>
        <el-button v-if="canUpdate && detailTask" type="primary" @click="detailVisible = false; openEdit(detailTask)">
          <el-icon><Edit /></el-icon>
          {{ settings.t('task.edit') }}
        </el-button>
        <el-button @click="detailVisible = false">{{ settings.t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="settings.t(editingId ? 'task.edit' : 'task.add')"
      width="760px"
      align-center
      destroy-on-close
      class="vx-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Document /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('common.basicInfo') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('task.title') }} &middot; {{ settings.t('task.description') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('task.title')" prop="title">
            <el-input v-model="form.title" />
          </el-form-item>
          <el-form-item :label="settings.t('task.description')">
            <el-input v-model="form.description" type="textarea" :rows="4" />
          </el-form-item>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Operation /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('task.status') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('task.assignee') }} &middot; {{ settings.t('task.dueDate') }}</p>
            </div>
          </div>
          <el-row :gutter="18">
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('task.status')">
                <el-select v-model="form.status" style="width:100%">
                  <el-option
                    v-for="status in statusOptions"
                    :key="status.value"
                    :label="status.label"
                    :value="status.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('task.assignee')">
                <el-select v-model="form.assigneeId" clearable filterable style="width:100%">
                  <el-option
                    v-for="employee in employees"
                    :key="employee.id"
                    :label="`${employee.fullName} (${employee.employeeCode})`"
                    :value="employee.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item :label="settings.t('task.dueDate')">
                <el-date-picker
                  v-model="form.dueDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  style="width:100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="form-section">
          <div class="form-section-header">
            <el-icon class="form-section-icon"><Upload /></el-icon>
            <div>
              <h4 class="form-section-title">{{ settings.t('task.attachments') }}</h4>
              <p class="form-section-subtitle">{{ settings.t('task.uploadHint') }}</p>
            </div>
          </div>
          <el-form-item :label="settings.t('task.upload')">
            <el-upload
              v-model:file-list="fileList"
              drag
              multiple
              :auto-upload="false"
              :before-upload="beforeUpload"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="el-upload__text">{{ settings.t('task.uploadHint') }}</div>
            </el-upload>
          </el-form-item>

          <div v-if="editingId && currentAttachments.length" class="attachment-list">
            <div class="attachment-title">{{ settings.t('task.currentAttachments') }}</div>
            <div
              v-for="attachment in currentAttachments"
              :key="attachment.id"
              class="attachment-item"
            >
              <div>
                <span>{{ attachment.originalFileName }}</span>
                <small>{{ formatFileSize(attachment.fileSize) }}</small>
              </div>
              <div class="attachment-actions">
                <el-button text type="primary" size="small" @click="downloadAttachment(attachment)">
                  <el-icon><Download /></el-icon>
                </el-button>
                <el-button v-if="canUpdate" text type="danger" size="small" @click="deleteAttachment(attachment)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </section>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ settings.t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveTask">
          {{ settings.t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.tasks-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.task-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.task-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.task-filters,
.task-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.task-search {
  width: 280px;
}

.filter-select {
  width: 210px;
}

.kanban-board {
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 1fr));
  gap: 16px;
  min-height: 360px;
}

.kanban-column {
  min-width: 0;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-bg);
  display: flex;
  flex-direction: column;
}

.kanban-column-header {
  padding: 14px;
  border-bottom: 1px solid var(--vx-border);
}

.kanban-column-header > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.kanban-column-header h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-md);
  font-weight: 700;
}

.kanban-column-header span {
  min-width: 28px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: var(--vx-card-bg);
  color: var(--vx-text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--vx-font-size-xs);
  font-weight: 700;
}

.kanban-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 280px;
}

.task-card {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-card-bg);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: grab;
}

.task-card:active {
  cursor: grabbing;
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.task-card h4 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-base);
  font-weight: 700;
  line-height: 1.35;
}

.task-description {
  color: var(--vx-text-secondary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
}

.task-meta span,
.attachment-count {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.task-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.attachment-count {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.upload-icon {
  font-size: var(--vx-font-size-3xl);
  color: var(--vx-primary);
}

.attachment-list {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  overflow: hidden;
}

.attachment-title {
  padding: 10px 12px;
  background: var(--vx-bg);
  color: var(--vx-text);
  font-weight: 700;
}

.attachment-item {
  min-height: 48px;
  padding: 9px 12px;
  border-top: 1px solid var(--vx-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.attachment-item > div:first-child {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.attachment-item span {
  color: var(--vx-text);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-item small {
  color: var(--vx-text-secondary);
}

.attachment-actions {
  flex: 0 0 auto;
}

.task-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.task-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.task-detail-header h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  font-weight: 700;
  line-height: 1.35;
}

.task-detail-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-bg);
}

.task-detail-meta > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--vx-text);
  font-size: var(--vx-font-size-sm);
}

.task-detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-detail-label {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.task-detail-description {
  color: var(--vx-text);
  line-height: 1.55;
  white-space: pre-wrap;
}

.task-detail-empty {
  color: var(--vx-text-secondary);
  font-style: italic;
}

@media (max-width: 1100px) {
  .kanban-board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .task-search,
  .filter-select {
    width: 100%;
  }

  .task-filters,
  .task-actions {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
