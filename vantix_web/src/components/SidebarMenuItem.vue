<script setup>
import { useSettingsStore } from '@/stores/settings'

defineOptions({ name: 'SidebarMenuItem' })

defineProps({
  item: {
    type: Object,
    required: true,
  },
})

const settings = useSettingsStore()

function itemIndex(item) {
  return item.index || item.path || item.name || item.label
}

function itemLabel(item) {
  return settings.t(item.labelKey || item.label)
}
</script>

<template>
  <el-menu-item-group
    v-if="item.type === 'group'"
    :title="itemLabel(item)"
    class="sidebar-menu-group"
  >
    <SidebarMenuItem
      v-for="child in item.children"
      :key="child.path || child.name"
      :item="child"
    />
  </el-menu-item-group>

  <el-sub-menu v-else-if="item.children?.length" :index="itemIndex(item)">
    <template #title>
      <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
      <span>{{ itemLabel(item) }}</span>
    </template>

    <SidebarMenuItem
      v-for="child in item.children"
      :key="child.path || child.name"
      :item="child"
    />
  </el-sub-menu>

  <el-menu-item v-else :index="item.path">
    <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
    <template #title>{{ itemLabel(item) }}</template>
  </el-menu-item>
</template>
