<script setup>
import { ElMessage } from 'element-plus'
import { useSettingsStore } from '@/stores/settings'
import {
  copyToClipboard,
  exportCsv,
  exportPdf,
  exportXls,
  printTable,
} from '@/utils/export'

const props = defineProps({
  filename: { type: String, default: 'export' },
  title: { type: String, default: '' },
  columns: { type: Array, required: true },
  rows: { type: Array, default: () => [] },
  // Optional async loader that returns all rows (e.g. to bypass pagination)
  fetchAll: { type: Function, default: null },
  size: { type: String, default: 'default' },
})

const settings = useSettingsStore()

async function resolveRows() {
  if (typeof props.fetchAll === 'function') {
    const data = await props.fetchAll()
    return Array.isArray(data) ? data : []
  }
  return props.rows || []
}

async function run(action) {
  try {
    const rows = await resolveRows()
    if (!rows.length && action !== 'print' && action !== 'pdf') {
      ElMessage.warning(settings.t('export.empty'))
      return
    }
    const payload = { filename: props.filename, title: props.title, columns: props.columns, rows }
    switch (action) {
      case 'copy':
        await copyToClipboard(payload)
        ElMessage.success(settings.t('export.copied'))
        break
      case 'csv':
        exportCsv(payload)
        break
      case 'excel':
        exportXls(payload)
        break
      case 'pdf':
        exportPdf(payload)
        break
      case 'print':
        printTable(payload)
        break
    }
  } catch (e) {
    ElMessage.error(e?.message || settings.t('common.somethingWrong'))
  }
}
</script>

<template>
  <el-dropdown trigger="click" @command="run">
    <el-button :size="size">
      <el-icon><Download /></el-icon>
      {{ settings.t('export.label') }}
      <el-icon style="margin-left:4px"><ArrowDown /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="copy">
          <el-icon><DocumentCopy /></el-icon>
          {{ settings.t('export.copy') }}
        </el-dropdown-item>
        <el-dropdown-item command="csv">
          <el-icon><Document /></el-icon>
          CSV
        </el-dropdown-item>
        <el-dropdown-item command="excel">
          <el-icon><Grid /></el-icon>
          Excel
        </el-dropdown-item>
        <el-dropdown-item command="pdf">
          <el-icon><Tickets /></el-icon>
          PDF
        </el-dropdown-item>
        <el-dropdown-item command="print" divided>
          <el-icon><Printer /></el-icon>
          {{ settings.t('export.print') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
