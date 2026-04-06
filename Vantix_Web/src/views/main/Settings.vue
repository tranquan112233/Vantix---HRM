<template>
  <div class="settings-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Settings</h1>
        <p class="page-desc">System configuration and preferences</p>
      </div>
    </div>

    <div class="settings-layout">
      <!-- Sidebar Nav -->
      <nav class="settings-nav">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['nav-item', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <i :class="'bi ' + tab.icon"></i>
          <span>{{ tab.label }}</span>
        </button>
      </nav>

      <!-- Content -->
      <div class="settings-content">

        <!-- ── System Info ──────────────────────────────────────── -->
        <section v-if="activeTab === 'system'" class="setting-section">
          <div class="section-heading">
            <i class="bi bi-info-circle"></i>
            <span>System Information</span>
          </div>
          <div class="info-grid">
            <div class="info-row" v-for="r in sysInfo" :key="r.label">
              <span class="info-label">{{ r.label }}</span>
              <span class="info-value" :class="r.cls">{{ r.value }}</span>
            </div>
          </div>

          <div class="section-heading mt">
            <i class="bi bi-shield-check"></i>
            <span>API Health</span>
          </div>
          <div class="health-row">
            <span class="health-dot" :class="apiStatus"></span>
            <span class="health-label">Backend API</span>
            <span class="health-status">{{ apiStatus === 'ok' ? 'Connected' : apiStatus === 'checking' ? 'Checking…' : 'Unreachable' }}</span>
            <button class="btn-sm" @click="checkApi">
              <i class="bi bi-arrow-repeat"></i> Check
            </button>
          </div>
        </section>

        <!-- ── Account & Security ──────────────────────────────── -->
        <section v-if="activeTab === 'account'" class="setting-section">
          <div class="section-heading">
            <i class="bi bi-person-circle"></i>
            <span>Account</span>
          </div>
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">Username</span>
              <span class="info-value">{{ auth.user?.username || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Email</span>
              <span class="info-value">{{ auth.user?.email || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Role</span>
              <span class="info-value"><span class="badge-role">{{ auth.user?.roleName || '—' }}</span></span>
            </div>
          </div>

          <div class="action-row">
            <router-link to="/profile" class="btn-primary">
              <i class="bi bi-person-gear"></i> Edit Profile
            </router-link>
          </div>

          <div class="section-heading mt">
            <i class="bi bi-lock"></i>
            <span>Security</span>
          </div>
          <div class="toggle-list">
            <div class="toggle-row">
              <div>
                <div class="toggle-label">Session Auto-Logout</div>
                <div class="toggle-desc">Automatically logout after inactivity</div>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.autoLogout" @change="savePrefs">
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </section>

        <!-- ── Appearance ──────────────────────────────────────── -->
        <section v-if="activeTab === 'appearance'" class="setting-section">
          <div class="section-heading">
            <i class="bi bi-palette"></i>
            <span>Display</span>
          </div>
          <div class="toggle-list">
            <div class="toggle-row">
              <div>
                <div class="toggle-label">Compact Mode</div>
                <div class="toggle-desc">Reduce spacing in tables and lists</div>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.compact" @change="savePrefs">
                <span class="slider"></span>
              </label>
            </div>
            <div class="toggle-row">
              <div>
                <div class="toggle-label">Show Row Numbers</div>
                <div class="toggle-desc">Display row index in all tables</div>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.showRowNumbers" @change="savePrefs">
                <span class="slider"></span>
              </label>
            </div>
          </div>

          <div class="section-heading mt">
            <i class="bi bi-translate"></i>
            <span>Language & Region</span>
          </div>
          <div class="select-row">
            <label class="sr-label">Date Format</label>
            <div class="select-wrap">
              <select v-model="prefs.dateFormat" @change="savePrefs">
                <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                <option value="YYYY-MM-DD">YYYY-MM-DD</option>
              </select>
              <i class="bi bi-chevron-down"></i>
            </div>
          </div>
          <div class="select-row">
            <label class="sr-label">Default Page Size</label>
            <div class="select-wrap">
              <select v-model.number="prefs.defaultPageSize" @change="savePrefs">
                <option :value="10">10 rows</option>
                <option :value="20">20 rows</option>
                <option :value="50">50 rows</option>
              </select>
              <i class="bi bi-chevron-down"></i>
            </div>
          </div>
        </section>

        <!-- ── Notifications ───────────────────────────────────── -->
        <section v-if="activeTab === 'notif'" class="setting-section">
          <div class="section-heading">
            <i class="bi bi-bell"></i>
            <span>Notification Preferences</span>
          </div>
          <div class="toggle-list">
            <div class="toggle-row" v-for="n in notifOptions" :key="n.key">
              <div>
                <div class="toggle-label">{{ n.label }}</div>
                <div class="toggle-desc">{{ n.desc }}</div>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.notif[n.key]" @change="savePrefs">
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </section>

      </div>
    </div>

    <!-- Save toast -->
    <transition name="fade">
      <div v-if="saved" class="save-toast"><i class="bi bi-check-circle-fill"></i> Preferences saved</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store.js'

const auth = useAuthStore()

const activeTab = ref('system')
const apiStatus = ref('checking') // 'ok' | 'error' | 'checking'
const saved = ref(false)

const tabs = [
  { key: 'system',     label: 'System',       icon: 'bi-info-circle' },
  { key: 'account',    label: 'Account',       icon: 'bi-person' },
  { key: 'appearance', label: 'Appearance',    icon: 'bi-palette' },
  { key: 'notif',      label: 'Notifications', icon: 'bi-bell' },
]

const sysInfo = [
  { label: 'Application',    value: 'Vantix HRM',   cls: '' },
  { label: 'Version',        value: 'v1.0.0',        cls: '' },
  { label: 'Environment',    value: 'Development',   cls: 'badge-env' },
  { label: 'Backend',        value: 'Spring Boot 3 · Java 17', cls: '' },
  { label: 'Frontend',       value: 'Vue 3 + Vite',  cls: '' },
  { label: 'Database',       value: 'MySQL',          cls: '' },
]

const notifOptions = [
  { key: 'leaveApproval',  label: 'Leave Approval',   desc: 'When a leave request is approved or rejected' },
  { key: 'contractExpiry', label: 'Contract Expiry',  desc: 'Reminder when a contract is about to expire' },
  { key: 'newEmployee',    label: 'New Employee',      desc: 'When a new employee is added to the system' },
  { key: 'salaryReady',    label: 'Salary Ready',      desc: 'When monthly salary batch is processed' },
]

// ── Preferences (stored in localStorage) ─────────────────────────────────────

const PREF_KEY = 'vantix_prefs'

const prefs = reactive({
  autoLogout: false,
  compact: false,
  showRowNumbers: true,
  dateFormat: 'DD/MM/YYYY',
  defaultPageSize: 10,
  notif: {
    leaveApproval: true,
    contractExpiry: true,
    newEmployee: false,
    salaryReady: true,
  }
})

function loadPrefs() {
  try {
    const raw = localStorage.getItem(PREF_KEY)
    if (raw) Object.assign(prefs, JSON.parse(raw))
  } catch { /* ignore */ }
}

let saveTimer = null
function savePrefs() {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    localStorage.setItem(PREF_KEY, JSON.stringify(prefs))
    saved.value = true
    setTimeout(() => { saved.value = false }, 2500)
  }, 300)
}

// ── API Health Check ──────────────────────────────────────────────────────────

async function checkApi() {
  apiStatus.value = 'checking'
  try {
    await fetch('/api/auth/health').then(r => { if (!r.ok) throw new Error() })
    apiStatus.value = 'ok'
  } catch {
    // Fallback: try any endpoint
    try {
      const token = localStorage.getItem('token')
      const r = await fetch('/api/employees?page=0&size=1', {
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      })
      apiStatus.value = r.status < 500 ? 'ok' : 'error'
    } catch {
      apiStatus.value = 'error'
    }
  }
}

onMounted(() => {
  loadPrefs()
  checkApi()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

* { margin: 0; padding: 0; box-sizing: border-box; }

.settings-page {
  padding: 32px;
  min-height: 100vh;
  background: #f8f8f6;
  font-family: 'DM Sans', sans-serif;
  color: #1a1a1a;
}

/* Header */
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; letter-spacing: -.5px; color: #111; }
.page-desc  { font-size: 13px; color: #888; margin-top: 3px; }

/* Layout */
.settings-layout {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
}
@media (max-width: 700px) { .settings-layout { grid-template-columns: 1fr; } }

/* Nav */
.settings-nav {
  background: #fff;
  border-radius: 14px;
  padding: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  background: transparent;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 500;
  color: #555;
  cursor: pointer;
  transition: all .15s;
  text-align: left;
}
.nav-item i { font-size: 15px; }
.nav-item:hover { background: #f4f4ff; color: #6366f1; }
.nav-item.active { background: #ede9fe; color: #6366f1; font-weight: 700; }

/* Content */
.settings-content {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
}

.setting-section { display: flex; flex-direction: column; gap: 0; }

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  font-weight: 700;
  color: #444;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.section-heading.mt { margin-top: 28px; }

/* Info Grid */
.info-grid { display: flex; flex-direction: column; gap: 0; margin-bottom: 4px; }
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid #fafafa;
}
.info-label { font-size: 13px; color: #888; }
.info-value { font-size: 13px; font-weight: 600; color: #222; }

.badge-env {
  background: #fef3c7;
  color: #d97706;
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12px;
}
.badge-role {
  background: #ede9fe;
  color: #6366f1;
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
}

/* Health Row */
.health-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
  margin-bottom: 4px;
}
.health-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.health-dot.ok       { background: #10b981; }
.health-dot.error    { background: #ef4444; }
.health-dot.checking { background: #f59e0b; animation: pulse .8s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.4} }
.health-label { flex: 1; font-size: 13px; font-weight: 600; color: #333; }
.health-status { font-size: 12.5px; color: #888; }
.btn-sm {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  background: #f4f4f9;
  border: 1px solid #e8e8f0;
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 600;
  color: #555;
  cursor: pointer;
  font-family: inherit;
  transition: all .15s;
}
.btn-sm:hover { background: #ede9fe; color: #6366f1; border-color: #c7d2fe; }

/* Action Row */
.action-row { margin: 14px 0; }
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 9px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  text-decoration: none;
  transition: all .2s;
}
.btn-primary:hover { background: #4f52e0; transform: translateY(-1px); }

/* Toggles */
.toggle-list { display: flex; flex-direction: column; gap: 2px; }
.toggle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
}
.toggle-label { font-size: 13.5px; font-weight: 600; color: #222; }
.toggle-desc  { font-size: 12px; color: #999; margin-top: 2px; }

/* Toggle Switch */
.switch { position: relative; display: inline-block; width: 42px; height: 24px; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute; cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background: #ddd;
  border-radius: 24px;
  transition: .25s;
}
.slider:before {
  content: '';
  position: absolute;
  height: 18px; width: 18px;
  left: 3px; bottom: 3px;
  background: #fff;
  border-radius: 50%;
  transition: .25s;
}
input:checked + .slider { background: #6366f1; }
input:checked + .slider:before { transform: translateX(18px); }

/* Select Row */
.select-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #fafafa;
}
.sr-label { font-size: 13px; color: #666; font-weight: 500; min-width: 140px; }
.select-wrap {
  position: relative;
  flex: 1;
  max-width: 240px;
}
.select-wrap select {
  width: 100%;
  appearance: none;
  padding: 8px 32px 8px 12px;
  border: 1px solid #e8e8f0;
  border-radius: 9px;
  font-size: 13px;
  font-family: inherit;
  background: #fff;
  color: #222;
  font-weight: 500;
  cursor: pointer;
  outline: none;
}
.select-wrap select:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,.1); }
.select-wrap .bi-chevron-down {
  position: absolute;
  right: 10px; top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  color: #aaa;
  pointer-events: none;
}

/* Save Toast */
.save-toast {
  position: fixed;
  bottom: 28px;
  right: 32px;
  background: #111;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 9999;
  box-shadow: 0 4px 20px rgba(0,0,0,.25);
}
.save-toast i { color: #34d399; font-size: 16px; }
.fade-enter-active, .fade-leave-active { transition: opacity .3s, transform .3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
</style>
