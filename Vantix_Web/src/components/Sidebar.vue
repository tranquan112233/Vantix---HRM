<template>
  <!-- ================= SIDEBAR WRAPPER ================= -->
  <aside
      class="sidebar"
      :class="{
        collapsed: collapsed && !isHovering,
        expanded: collapsed && isHovering
      }"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
  >

    <!-- ================= LOGO ================= -->
    <div class="logo">
      <i class="bi bi-bar-chart logo-icon"></i>

      <!-- Chỉ hiện chữ khi sidebar không collapsed -->
      <span v-if="!collapsed || isHovering" class="logo-text">
        VANTIX
      </span>
    </div>

    <!-- ================= MENU ================= -->
    <ul class="menu">

      <!-- Loop từng section -->
      <template v-for="(items, section) in groupedMenu" :key="section">

        <!-- ===== SECTION TITLE ===== -->
        <li
            v-if="!collapsed || isHovering"
            class="menu-title"
        >
          {{ section }}
        </li>

        <!-- ===== MENU ITEMS ===== -->
        <li v-for="item in items" :key="item.key">

          <!-- ================= ITEM KHÔNG CÓ SUB ================= -->
          <router-link
              v-if="!item.children"
              :to="item.to"
              custom
              v-slot="{ navigate }"
          >
            <div
                class="menu-item"
                :class="{ active: isExactActive(item.to) }"
                @click="navigate"
            >
              <i :class="item.icon"></i>

              <span v-if="!collapsed || isHovering">
                {{ item.label }}
              </span>
            </div>
          </router-link>

          <!-- ================= ITEM CÓ SUB ================= -->
          <template v-else>

            <!-- ===== PARENT ===== -->
            <div
                class="menu-item has-sub"
                :class="{ active: isParentActive(item) }"
                @click="toggle(item.key)"
            >
              <i :class="item.icon"></i>

              <span v-if="!collapsed || isHovering">
                {{ item.label }}
              </span>

              <i
                  v-if="!collapsed || isHovering"
                  class="bi bi-chevron-down arrow"
                  :class="{ open: open[item.key] }"
              />
            </div>

            <!-- ===== CHILDREN ===== -->
            <transition name="slide">
              <ul
                  v-show="open[item.key] && (!collapsed || isHovering)"
                  class="submenu"
              >
                <li
                    v-for="child in item.children"
                    :key="child.to"
                >
                  <router-link
                      :to="child.to"
                      custom
                      v-slot="{ navigate }"
                  >
                    <div
                        class="submenu-item"
                        :class="{ active: isExactActive(child.to) }"
                        @click="navigate"
                    >
                      {{ child.label }}
                    </div>
                  </router-link>
                </li>
              </ul>
            </transition>

          </template>

        </li>

      </template>

    </ul>

  </aside>
</template>

<script setup>
import { reactive, computed, watch, ref } from "vue"
import { useRoute } from "vue-router"
import { menuItems } from "@/config/menu.config"
import { getUser } from "@/utils/jwtDecode"

const user = getUser()
const userRoles = ref(user?.roles || [])
const userPermissions = ref(user?.permissions || [])

// Phân loại Role
const isAdmin = userRoles.value.includes('ADMIN') || userRoles.value.includes('ROLE_ADMIN')
const isHR = userRoles.value.includes('HR') || userRoles.value.includes('ROLE_HR')

const props = defineProps({ collapsed: Boolean })
const route = useRoute()
const isHovering = ref(false)

const handleMouseEnter = () => { if (props.collapsed) isHovering.value = true }
const handleMouseLeave = () => { isHovering.value = false }

// 🔥 HÀM CHECK QUYỀN ĐÃ FIX: SOI THEO KEY, KHÔNG SOI THEO SECTION
const hasDynamicPermission = (item) => {
  if (isAdmin) return true;

  if (item.children && item.children.length > 0) {
    return item.children.some(child => hasDynamicPermission(child));
  }

  const reqPerm = item.key;
  const employeeZone = ['dashboard', 'attendances', 'my-leaves', 'shifts', 'profile'];
  const hrZone = ['departments', 'positions', 'employees', 'contracts', 'contract-annexes', 'leave-approvals', 'leave-types', 'salaries', 'notifications'];

  if (employeeZone.includes(reqPerm)) return true;
  if (hrZone.includes(reqPerm) && isHR) return true;

  return userPermissions.value.includes(reqPerm);
}

const groupedMenu = computed(() => {
  const groups = {}
  const filteredMenu = menuItems
      .filter(item => hasDynamicPermission(item))
      .map(item => {
        if (item.children) {
          const filteredChildren = item.children.filter(child => hasDynamicPermission(child))
          if (filteredChildren.length === 0) return null
          return { ...item, children: filteredChildren }
        }
        return item
      })
      .filter(Boolean)

  filteredMenu.forEach(item => {
    if (!groups[item.section]) groups[item.section] = []
    groups[item.section].push(item)
  })
  return groups
})

const open = reactive({})
watch(groupedMenu, (newMenu) => {
  Object.values(newMenu).flat().forEach(item => { if (item.children) open[item.key] = false })
}, { immediate: true })

const isExactActive = (path) => route.path === path || route.path.startsWith(path + "/")
const isParentActive = (item) => item.children?.some(child => isExactActive(child.to))

watch(route, () => {
  menuItems.forEach(item => { if (item.children && isParentActive(item)) open[item.key] = true })
}, { immediate: true })

const toggle = (key) => { open[key] = !open[key] }
</script>

<style scoped>

/* ================= SIDEBAR ================= */
.sidebar {
  width: 260px;
  min-width: 260px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  padding: 14px;
  overflow-y: auto;
  transition: width 0.25s ease;
}

/* Collapse width */
.sidebar.collapsed {
  width: 65px;
  min-width: 65px;
}

/* Hover expand */
.sidebar.expanded {
  width: 260px;
  min-width: 260px;
}

/* ================= LOGO ================= */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
}

.logo-icon {
  font-size: 26px;
  color: #0d6efd;
}

.logo-text {
  font-weight: 600;
  font-size: 16px;
}

/* Center logo when collapsed */
.sidebar.collapsed .logo {
  justify-content: center;
}

/* ================= MENU ================= */
.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-title {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  color: #94a3b8;
  margin: 14px 0 6px 4px;
  letter-spacing: 1px;
}

/* ================= MENU ITEM ================= */
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  color: #334155;
  margin-bottom: 3px;
  transition: 0.2s;
}

.menu-item:hover {
  background: #f1f5f9;
}

.menu-item.active {
  background: #0d6efd;
  color: white;
}

/* ================= SUBMENU ================= */
.submenu {
  padding-left: 28px;
  margin-top: 4px;
}

.submenu-item {
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  color: #475569;
  transition: 0.2s;
}

.submenu-item:hover {
  background: #f1f5f9;
}

.submenu-item.active {
  background: #0d6efd;
  color: white;
}

/* ================= ARROW ================= */
.arrow {
  margin-left: auto;
  font-size: 11px;
  transition: transform 0.2s;
}

.arrow.open {
  transform: rotate(180deg);
}

/* ================= ANIMATION ================= */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

</style>