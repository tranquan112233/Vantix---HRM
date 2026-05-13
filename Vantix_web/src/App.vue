<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import en from 'element-plus/es/locale/lang/en'
import vi from 'element-plus/es/locale/lang/vi'
import { useSettingsStore } from '@/stores/settings'

const settings = useSettingsStore()
const route = useRoute()
const elementLocale = computed(() => settings.locale === 'vi' ? vi : en)
const browserTitle = computed(() => {
  const titleKey = route.meta.titleKey || route.meta.title || 'menu.dashboard'
  return `${settings.t(titleKey)} - Vantix`
})

watch(browserTitle, (title) => {
  document.title = title
}, { immediate: true })
</script>

<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>
