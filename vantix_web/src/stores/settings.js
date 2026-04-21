import { defineStore } from 'pinia'
import { computed, reactive } from 'vue'
import { translations } from '@/i18n/translations'

const STORAGE_KEY = 'vx_settings'

const defaultSettings = {
  primaryColor: '#4F46E5',
  fontSize: 14,
  fontFamily: 'Inter',
  language: 'en',
}

const themePresets = [
  { name: 'Indigo', color: '#4F46E5' },
  { name: 'Emerald', color: '#059669' },
  { name: 'Rose', color: '#E11D48' },
  { name: 'Amber', color: '#D97706' },
  { name: 'Cyan', color: '#0891B2' },
  { name: 'Slate', color: '#475569' },
]

const fontOptions = [
  { label: 'Inter', value: 'Inter' },
  { label: 'Segoe UI', value: 'Segoe UI' },
  { label: 'Arial', value: 'Arial' },
  { label: 'Tahoma', value: 'Tahoma' },
  { label: 'Georgia', value: 'Georgia' },
]

const languageOptions = [
  { labelKey: 'settings.english', value: 'en' },
  { labelKey: 'settings.vietnamese', value: 'vi' },
]

function readStoredSettings() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

function normalizeHex(color) {
  return /^#[0-9A-Fa-f]{6}$/.test(color) ? color : defaultSettings.primaryColor
}

function hexToRgb(hex) {
  const value = hex.replace('#', '')
  return {
    r: parseInt(value.slice(0, 2), 16),
    g: parseInt(value.slice(2, 4), 16),
    b: parseInt(value.slice(4, 6), 16),
  }
}

function rgbToHex({ r, g, b }) {
  return '#' + [r, g, b]
    .map(value => Math.max(0, Math.min(255, Math.round(value))).toString(16).padStart(2, '0'))
    .join('')
}

function mixHex(color, target, ratio) {
  const source = hexToRgb(color)
  const destination = hexToRgb(target)
  return rgbToHex({
    r: source.r + (destination.r - source.r) * ratio,
    g: source.g + (destination.g - source.g) * ratio,
    b: source.b + (destination.b - source.b) * ratio,
  })
}

function normalizeSettings(value = {}) {
  return {
    primaryColor: normalizeHex(value.primaryColor),
    fontSize: Math.min(Math.max(Number(value.fontSize) || defaultSettings.fontSize, 12), 18),
    fontFamily: fontOptions.some(font => font.value === value.fontFamily)
      ? value.fontFamily
      : defaultSettings.fontFamily,
    language: translations[value.language] ? value.language : defaultSettings.language,
  }
}

export const useSettingsStore = defineStore('settings', () => {
  const settings = reactive(normalizeSettings({
    ...defaultSettings,
    ...readStoredSettings(),
  }))

  const locale = computed(() => settings.language)

  function t(key) {
    return translations[settings.language]?.[key]
      || translations.en[key]
      || key
  }

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
  }

  function applySettings() {
    const root = document.documentElement
    const primary = normalizeHex(settings.primaryColor)
    const primaryLight = mixHex(primary, '#FFFFFF', 0.32)
    const primaryDark = mixHex(primary, '#000000', 0.28)
    const sidebarBg = mixHex(primary, '#111827', 0.72)
    const fontSize = settings.fontSize

    root.style.setProperty('--vx-primary', primary)
    root.style.setProperty('--vx-primary-light', primaryLight)
    root.style.setProperty('--vx-primary-dark', primaryDark)
    root.style.setProperty('--vx-sidebar-bg', sidebarBg)
    root.style.setProperty('--vx-sidebar-active', primary)
    root.style.setProperty('--vx-font-size-base', `${fontSize}px`)
    root.style.setProperty('--vx-font-size-2xs', `${Math.max(fontSize - 4, 10)}px`)
    root.style.setProperty('--vx-font-size-xs', `${Math.max(fontSize - 2, 10)}px`)
    root.style.setProperty('--vx-font-size-sm', `${Math.max(fontSize - 1, 11)}px`)
    root.style.setProperty('--vx-font-size-md', `${fontSize + 1}px`)
    root.style.setProperty('--vx-font-size-lg', `${fontSize + 2}px`)
    root.style.setProperty('--vx-font-size-xl', `${fontSize + 4}px`)
    root.style.setProperty('--vx-font-size-2xl', `${fontSize + 6}px`)
    root.style.setProperty('--vx-font-size-3xl', `${fontSize + 10}px`)
    root.style.setProperty('--vx-font-size-4xl', `${fontSize + 14}px`)
    root.style.setProperty('--vx-font-size-5xl', `${fontSize + 22}px`)
    root.style.setProperty('--vx-font-family', `'${settings.fontFamily}', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`)
    root.style.setProperty('--el-font-family', `'${settings.fontFamily}', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`)
    root.style.setProperty('--el-font-size-extra-small', `${Math.max(fontSize - 2, 10)}px`)
    root.style.setProperty('--el-font-size-small', `${Math.max(fontSize - 1, 11)}px`)
    root.style.setProperty('--el-font-size-base', `${fontSize}px`)
    root.style.setProperty('--el-font-size-medium', `${fontSize + 1}px`)
    root.style.setProperty('--el-font-size-large', `${fontSize + 2}px`)
    root.style.setProperty('--el-font-size-extra-large', `${fontSize + 4}px`)
    root.lang = settings.language
  }

  function saveAndApply() {
    persist()
    applySettings()
  }

  function setPrimaryColor(color) {
    settings.primaryColor = normalizeHex(color)
    saveAndApply()
  }

  function setFontSize(fontSize) {
    settings.fontSize = Math.min(Math.max(Number(fontSize) || defaultSettings.fontSize, 12), 18)
    saveAndApply()
  }

  function setFontFamily(fontFamily) {
    settings.fontFamily = fontOptions.some(font => font.value === fontFamily)
      ? fontFamily
      : defaultSettings.fontFamily
    saveAndApply()
  }

  function setLanguage(language) {
    settings.language = translations[language] ? language : defaultSettings.language
    saveAndApply()
  }

  function resetSettings() {
    Object.assign(settings, defaultSettings)
    saveAndApply()
  }

  return {
    settings,
    locale,
    themePresets,
    fontOptions,
    languageOptions,
    t,
    applySettings,
    setPrimaryColor,
    setFontSize,
    setFontFamily,
    setLanguage,
    resetSettings,
  }
})
