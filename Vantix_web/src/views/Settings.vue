<script setup>
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { ElMessage } from 'element-plus'

const settingsStore = useSettingsStore()
const settings = settingsStore.settings

const fontSizeLabel = computed(() => {
  if (settings.fontSize <= 13) return settingsStore.t('settings.compact')
  if (settings.fontSize >= 16) return settingsStore.t('settings.comfortable')
  return settingsStore.t('settings.normal')
})

function handlePrimaryColor(color) {
  settingsStore.setPrimaryColor(color)
  ElMessage.success(settingsStore.t('settings.saved'))
}

function handleFontSize(size) {
  settingsStore.setFontSize(size)
}

function handleFontFamily(fontFamily) {
  settingsStore.setFontFamily(fontFamily)
  ElMessage.success(settingsStore.t('settings.saved'))
}

function handleLanguage(language) {
  settingsStore.setLanguage(language)
  ElMessage.success(settingsStore.t('settings.saved'))
}

function handleReset() {
  settingsStore.resetSettings()
  ElMessage.success(settingsStore.t('settings.resetDone'))
}
</script>

<template>
  <div class="settings-page">
    <div class="page-card settings-panel">
      <div class="settings-section">
        <div class="section-heading">
          <el-icon><Brush /></el-icon>
          <span>{{ settingsStore.t('settings.appearance') }}</span>
        </div>

        <el-form label-position="top" class="settings-form">
          <el-form-item :label="settingsStore.t('settings.primaryColor')">
            <div class="color-row">
              <button
                v-for="preset in settingsStore.themePresets"
                :key="preset.color"
                type="button"
                class="color-swatch"
                :class="{ active: settings.primaryColor === preset.color }"
                :style="{ background: preset.color }"
                :title="preset.name"
                @click="handlePrimaryColor(preset.color)"
              />
              <el-color-picker
                :model-value="settings.primaryColor"
                @change="handlePrimaryColor"
              />
            </div>
          </el-form-item>

          <el-form-item :label="settingsStore.t('settings.fontSize')">
            <div class="font-size-control">
              <el-slider
                :model-value="settings.fontSize"
                :min="12"
                :max="18"
                :step="1"
                show-stops
                @input="handleFontSize"
                @change="handleFontSize"
              />
              <span class="font-size-value">{{ settings.fontSize }}px · {{ fontSizeLabel }}</span>
            </div>
          </el-form-item>

          <el-form-item :label="settingsStore.t('settings.fontFamily')">
            <el-select
              :model-value="settings.fontFamily"
              style="width: 260px"
              @change="handleFontFamily"
            >
              <el-option
                v-for="font in settingsStore.fontOptions"
                :key="font.value"
                :label="font.label"
                :value="font.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item :label="settingsStore.t('settings.language')">
            <el-radio-group
              :model-value="settings.language"
              @change="handleLanguage"
            >
              <el-radio-button
                v-for="language in settingsStore.languageOptions"
                :key="language.value"
                :value="language.value"
              >
                {{ settingsStore.t(language.labelKey) }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div class="settings-actions">
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            {{ settingsStore.t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="page-card preview-panel">
      <div class="section-heading">
        <el-icon><View /></el-icon>
        <span>{{ settingsStore.t('settings.preview') }}</span>
      </div>

      <div class="preview-box">
        <div class="preview-icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="preview-copy">
          <h3>{{ settingsStore.t('settings.previewTitle') }}</h3>
          <p>{{ settingsStore.t('settings.previewText') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.settings-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vx-text);
  font-weight: 700;
}

.settings-form {
  max-width: 620px;
}

.color-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.color-swatch {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.5);
}

.color-swatch.active {
  border-color: var(--vx-text);
}

.font-size-control {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 120px;
  gap: 16px;
  align-items: center;
  width: 100%;
}

.font-size-value {
  color: var(--vx-text-secondary);
  font-weight: 600;
}

.settings-actions {
  display: flex;
  justify-content: flex-start;
}

.preview-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-box {
  display: flex;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  background: var(--vx-bg);
}

.preview-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: var(--vx-primary);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.preview-copy h3 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
  margin-bottom: 4px;
}

.preview-copy p {
  color: var(--vx-text-secondary);
  line-height: 1.5;
}

@media (max-width: 980px) {
  .settings-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 620px) {
  .font-size-control {
    grid-template-columns: 1fr;
  }
}
</style>
