<template>
  <aside
      class="sidebar"
      :class="{ 'sidebar--collapsed': isCollapsed, 'sidebar--hover': isCollapsed && hovering }"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
  >
    <!-- Logo -->
    <div class="sidebar-logo">
      <div class="logo-icon">
        <img src="/favicon.svg" alt="Vantix" />
      </div>
      <span class="logo-text">VANTIX</span>
    </div>

    <!-- Menu -->
    <nav class="sidebar-nav">
      <template v-for="section in filteredSections" :key="section.section">

        <!-- Section title -->
        <div class="section-label">{{ section.section }}</div>

        <template v-for="item in section.items" :key="item.label">

          <!-- Item đơn -->
          <RouterLink
              v-if="!item.children"
              :to="item.to"
              class="menu-item"
              :class="{ 'menu-item--active': isActive(item.to) }"
              :title="isCollapsed && !hovering ? item.label : ''"
          >
            <span class="item-icon"><i :class="`bi ${item.icon}`"></i></span>
            <span class="item-label">{{ item.label }}</span>
          </RouterLink>

          <!-- Group -->
          <div v-else class="menu-group">
            <div
                class="menu-item menu-item--group"
                :class="{ 'menu-item--active': isGroupActive(item) }"
                :title="isCollapsed && !hovering ? item.label : ''"
                @click="toggleGroup(item.label)"
            >
              <span class="item-icon"><i :class="`bi ${item.icon}`"></i></span>
              <span class="item-label">{{ item.label }}</span>
              <i class="bi bi-chevron-down item-arrow" :class="{ 'item-arrow--open': open[item.label] }"></i>
            </div>

            <div class="submenu" :class="{ 'submenu--open': open[item.label] }">
              <div class="submenu-inner">
                <RouterLink
                    v-for="child in item.children"
                    :key="child.to"
                    :to="child.to"
                    class="submenu-item"
                    :class="{ 'submenu-item--active': isActive(child.to) }"
                >
                  <span class="sub-icon"><i :class="`bi ${child.icon}`"></i></span>
                  {{ child.label }}
                </RouterLink>
              </div>
            </div>
          </div>

        </template>
      </template>
    </nav>
  </aside>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { RouterLink, useRoute }           from 'vue-router'
import { useAuthStore }                   from '@/stores/auth.store'
import { menuSections }                   from '@/config/menu.config.js'

const props = defineProps({ collapsed: Boolean })

const route   = useRoute()
const auth    = useAuthStore()
const hovering = ref(false)

const isCollapsed = computed(() => props.collapsed && !hovering.value)

// ── Lọc menu theo permission ──────────────────────────────────────────────
const filteredSections = computed(() =>
    menuSections
        .map(s => ({
          ...s,
          items: s.items
              .map(item => {
                if (item.children) {
                  const visible = item.children.filter(c => !c.permission || auth.can(c.permission))
                  return visible.length ? { ...item, children: visible } : null
                }
                return !item.permission || auth.can(item.permission) ? item : null
              })
              .filter(Boolean)
        }))
        .filter(s => s.items.length > 0)
)

// ── Active state ──────────────────────────────────────────────────────────
const isActive      = path  => route.path === path || route.path.startsWith(path + '/')
const isGroupActive = item  => item.children?.some(c => isActive(c.to))

// ── Submenu open/close ────────────────────────────────────────────────────
const open = reactive({})
const toggleGroup = label => { open[label] = !open[label] }

// Tự mở group chứa route hiện tại
watch(
    () => route.path,
    () => {
      menuSections.forEach(s =>
          s.items.forEach(item => {
            if (item.children && isGroupActive(item)) open[item.label] = true
          })
      )
    },
    { immediate: true }
)
</script>

<style scoped>
.sidebar {
  --w:       228px;
  --w-col:   56px;

  width: var(--w);
  height: 100vh;
  flex-shrink: 0;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  box-shadow: 2px 0 16px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width .22s cubic-bezier(.4,0,.2,1);
}
.sidebar--collapsed                         { width: var(--w-col); }
.sidebar--collapsed.sidebar--hover          { width: var(--w); box-shadow: 6px 0 20px rgba(0,0,0,0.3); }

/* ── Logo ──────────────────────────────────────────────────────────────── */
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-bottom: 1px solid var(--sidebar-border);
  flex-shrink: 0;
  overflow: hidden;
}
.logo-icon {
  width: 28px; height: 28px;
  background: var(--primary-color);
  border-radius: 7px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #fff;
  flex-shrink: 0;
}
.logo-text {
  font-size: 12px; font-weight: 700;
  letter-spacing: 1.5px; color: var(--sidebar-text-hi);
  white-space: nowrap;
}
.sidebar--collapsed:not(.sidebar--hover) .logo-text { display: none; }

/* ── Nav ───────────────────────────────────────────────────────────────── */
.sidebar-nav {
  flex: 1;
  overflow-y: auto; overflow-x: hidden;
  padding: 8px;
  scrollbar-width: thin;
  scrollbar-color: var(--sidebar-border) transparent;
}

.section-label {
  font-size: 10px; font-weight: 600;
  letter-spacing: .8px;
  color: var(--sidebar-text-dim);
  padding: 14px 8px 5px;
  white-space: nowrap;
  text-transform: uppercase;
}
.sidebar--collapsed:not(.sidebar--hover) .section-label {
  height: 1px; background: var(--sidebar-border);
  margin: 8px 6px; padding: 0;
  font-size: 0; overflow: hidden;
}

/* ── Menu item ─────────────────────────────────────────────────────────── */
.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  margin: 1px 0;
  border-radius: 7px;
  font-size: 13px;
  color: var(--sidebar-text);
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
  text-decoration: none;
  transition: background var(--transition), color var(--transition);
}
.menu-item:hover                { background: var(--sidebar-hover); color: var(--sidebar-text-hi); }
.menu-item--active              { background: var(--sidebar-accent-bg); color: var(--primary-color); font-weight: 500; }
.menu-item--active .item-icon   { color: var(--primary-color); }

.sidebar--collapsed:not(.sidebar--hover) .menu-item {
  justify-content: center;
  padding: 7px 0;
}

.item-icon {
  width: 26px; height: 26px;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; color: var(--sidebar-text-dim);
  flex-shrink: 0;
  transition: color var(--transition);
}
.menu-item:hover .item-icon { color: var(--sidebar-text-hi); }

.item-label { overflow: hidden; flex: 1; }
.sidebar--collapsed:not(.sidebar--hover) .item-label { display: none; }

.item-arrow {
  font-size: 10px; color: var(--sidebar-text-dim);
  transition: transform .2s; flex-shrink: 0;
}
.item-arrow--open                                           { transform: rotate(180deg); }
.sidebar--collapsed:not(.sidebar--hover) .item-arrow       { display: none; }

/* ── Submenu ────────────────────────────────────────────────────────────── */
.submenu {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows .2s ease;
}
.submenu--open                                        { grid-template-rows: 1fr; }
.sidebar--collapsed:not(.sidebar--hover) .submenu    { display: none; }
.submenu-inner                                         { overflow: hidden; }

.submenu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  margin: 1px 0;
  border-radius: 7px;
  font-size: 12.5px;
  color: var(--sidebar-text-dim);
  text-decoration: none;
  white-space: nowrap;
  transition: background var(--transition), color var(--transition);
}
.submenu-item:hover           { background: var(--sidebar-hover); color: var(--sidebar-text-hi); }
.submenu-item--active         { color: var(--primary-color); font-weight: 500; }
.submenu-item--active .sub-icon { color: var(--primary-color); }

.sub-icon {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; opacity: .7;
  flex-shrink: 0;
}
.submenu-item:hover .sub-icon   { opacity: .9; }
.submenu-item--active .sub-icon { opacity: 1; }
</style>
