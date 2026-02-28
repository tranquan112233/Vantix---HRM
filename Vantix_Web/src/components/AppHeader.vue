<template>
  <!-- ================= NAVBAR ================= -->
  <nav class="navbar shadow-sm px-3">
    <div class="container-fluid d-flex justify-content-between align-items-center">

      <!-- ================= LEFT SIDE ================= -->
      <div class="d-flex align-items-center">

        <!-- Toggle Sidebar Button -->
        <button
            class="btn toggle-btn"
            @click="$emit('toggle-sidebar')"
        >
          <i class="bi bi-list fs-4"></i>
        </button>

      </div>

      <!-- ================= RIGHT SIDE ================= -->
      <div class="d-flex align-items-center gap-3">

        <!-- =========================================
             NOTIFICATION DROPDOWN
        ========================================== -->
        <div class="dropdown">

          <!-- Notification Button -->
          <button
              class="btn notification-btn"
              data-bs-toggle="dropdown"
          >
            <i class="bi bi-bell notification-icon"></i>

            <!-- Badge -->
            <span
                v-if="hasNotifications"
                class="badge bg-danger notification-badge"
            >
              {{ notificationCount }}
            </span>
          </button>

          <!-- Dropdown -->
          <ul class="dropdown-menu dropdown-menu-end shadow notification-dropdown border-0">

            <!-- Header -->
            <li class="notification-header d-flex justify-content-between align-items-center">
              <span class="fw-semibold">Notifications</span>
              <span class="badge bg-primary rounded-pill">
                {{ notificationCount }}
              </span>
            </li>

            <li><hr class="dropdown-divider m-0"></li>

            <!-- Empty State -->
            <li
                v-if="!hasNotifications"
                class="text-center py-3 text-muted small"
            >
              No notifications
            </li>

            <!-- Notification List -->
            <li v-else>
              <ul class="notification-scroll list-unstyled m-0">

                <li
                    v-for="(item, index) in notifications"
                    :key="index"
                    class="dropdown-item notification-item"
                >
                  <div class="d-flex align-items-start gap-2">
                    <div class="notification-dot"></div>
                    <div>
                      <div class="notification-text">
                        {{ item.text }}
                      </div>
                      <div class="notification-time">
                        {{ item.time }}
                      </div>
                    </div>
                  </div>
                </li>

              </ul>
            </li>

            <li><hr class="dropdown-divider m-0"></li>

            <!-- View All -->
            <li>
              <router-link
                  to="/notifications"
                  class="dropdown-item text-center view-all-btn"
              >
                View All Notifications
              </router-link>
            </li>

          </ul>
        </div>

        <!-- =========================================
             USER PROFILE DROPDOWN
        ========================================== -->
        <div class="dropdown">

          <!-- Profile Button -->
          <button
              class="btn d-flex align-items-center profile-btn"
              data-bs-toggle="dropdown"
          >
            <!-- Avatar -->
            <div class="avatar-sm me-2">
              {{ firstLetter }}
            </div>

            <!-- Username -->
            <span class="fw-semibold me-1">
              {{ username }}
            </span>

            <!-- Custom Arrow -->
            <i class="bi bi-chevron-down arrow-icon"></i>
          </button>

          <!-- Dropdown -->
          <ul class="dropdown-menu dropdown-menu-end shadow profile-dropdown border-0">

            <!-- User Info -->
            <li class="px-3 py-2 border-bottom mb-1">
              <div class="fw-semibold">{{ username }}</div>
              <div class="text-muted small">{{ email }}</div>
            </li>

            <!-- Admin Panel (Only if ADMIN role) -->
            <li v-if="isAdmin">
              <router-link to="/admin" class="dropdown-item profile-item">
                <i class="bi bi-speedometer2 me-2"></i>
                Admin Panel
              </router-link>
            </li>

            <!-- Profile -->
            <li>
              <router-link to="/profile" class="dropdown-item profile-item">
                <i class="bi bi-person me-2"></i>
                Profile
              </router-link>
            </li>

            <li><hr class="dropdown-divider"></li>

            <!-- Logout -->
            <li>
              <button
                  class="dropdown-item profile-item text-danger"
                  @click="logout"
              >
                <i class="bi bi-box-arrow-right me-2"></i>
                Logout
              </button>
            </li>

          </ul>
        </div>

      </div>
    </div>
  </nav>
</template>

<script setup>
/* ================= IMPORTS ================= */
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import AuthService from "@/services/auth.service"
import { getUser } from "@/utils/jwtDecode"

/* ================= ROUTER ================= */
const router = useRouter()

/* ================= USER DATA ================= */
const user = computed(() => getUser())

const username = computed(() =>
    user.value?.username || "User"
)

const email = computed(() =>
    user.value?.sub || ""
)

const firstLetter = computed(() =>
    username.value.charAt(0).toUpperCase()
)

const isAdmin = computed(() =>
    user.value?.role?.includes("ADMIN")
)

/* ================= NOTIFICATIONS ================= */
const notifications = ref([
  { text: "New employee added", time: "2 minutes ago" },
  { text: "System update available", time: "1 hour ago" },
  { text: "Password changed successfully", time: "Yesterday" }
])

const notificationCount = computed(() =>
    notifications.value.length
)

const hasNotifications = computed(() =>
    notificationCount.value > 0
)

/* ================= LOGOUT ================= */
const logout = () => {
  AuthService.logout()
  router.push("/auth/login")
}
</script>

<style scoped>

/* ================= NAVBAR ================= */
.navbar {
  background: #ffffff;
  height: 60px;
}

/* ================= TOGGLE BUTTON ================= */
.toggle-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-btn:hover {
  background: #f3f4f6;
}

/* ================= NOTIFICATION ================= */
.notification-btn {
  position: relative;
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon {
  font-size: 20px;
  color: #374151;
}

.notification-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-dropdown {
  width: 320px;
  border-radius: 12px;
  overflow: hidden;
}

.notification-header {
  padding: 12px 16px;
  background: #f9fafb;
  font-size: 14px;
}

.notification-scroll {
  max-height: 260px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
}

.notification-item:hover {
  background: #f3f4f6;
}

.notification-dot {
  width: 8px;
  height: 8px;
  background: #0d6efd;
  border-radius: 50%;
  margin-top: 6px;
}

.notification-text {
  font-size: 14px;
  font-weight: 500;
}

.notification-time {
  font-size: 12px;
  color: #6b7280;
}

.view-all-btn {
  font-weight: 500;
  color: #0d6efd;
}

/* ================= PROFILE ================= */
.profile-btn {
  height: 40px;
  padding: 0 12px;
  background: transparent;
  border: none;
}

.profile-dropdown {
  width: 260px;
  border-radius: 10px;
  padding: 6px;
}

.profile-item {
  padding: 8px 12px;
  border-radius: 6px;
  color: #374151;
}

.profile-item:hover {
  background: #f3f4f6;
}

.avatar-sm {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #0d6efd;
  color: white;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 13px;
  transition: transform 0.2s ease;
}

.show .arrow-icon {
  transform: rotate(180deg);
}

</style>