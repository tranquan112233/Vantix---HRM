<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'

const auth = useAuthStore()
const settings = useSettingsStore()
const loading = ref(false)
const savingEmail = ref(false)
const savingPassword = ref(false)
const profile = ref(null)
const emailFormRef = ref(null)
const passwordFormRef = ref(null)

const emailForm = reactive({
  email: '',
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const emailRules = computed(() => ({
  email: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
    { type: 'email', message: settings.t('common.emailInvalid'), trigger: 'blur' },
  ],
}))

const passwordRules = computed(() => ({
  currentPassword: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
    { min: 6, max: 100, message: settings.t('profile.passwordLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: settings.t('common.required'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(settings.t('profile.passwordMismatch')))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
}))

const account = computed(() => profile.value?.account || null)
const employee = computed(() => profile.value?.employee || null)

onMounted(fetchProfile)

async function fetchProfile() {
  loading.value = true
  try {
    const { data } = await profileApi.get()
    profile.value = data
    emailForm.email = data.account?.email || ''
  } catch {
    ElMessage.error(settings.t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function saveEmail() {
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingEmail.value = true
  try {
    const { data } = await profileApi.updateEmail({ email: emailForm.email })
    profile.value = data
    await auth.refreshCurrentUser()
    ElMessage.success(settings.t('profile.emailUpdated'))
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    savingEmail.value = false
  }
}

async function changePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingPassword.value = true
  try {
    await profileApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    })
    passwordFormRef.value.clearValidate()
    ElMessage.success(settings.t('profile.passwordUpdated'))
  } catch (e) {
    ElMessage.error(e.response?.data?.message || settings.t('common.somethingWrong'))
  } finally {
    savingPassword.value = false
  }
}

function displayValue(value) {
  return value || '-'
}

function statusLabel(status) {
  const map = {
    PROBATION: 'status.probation',
    ACTIVE: 'status.active',
    UNPAID_LEAVE: 'status.unpaidLeave',
    RESIGNED: 'status.resigned',
    TERMINATED: 'status.terminated',
    LOCKED: 'status.locked',
  }

  return status ? settings.t(map[status] || status) : '-'
}

function genderLabel(gender) {
  const map = {
    MALE: 'gender.male',
    FEMALE: 'gender.female',
    OTHER: 'gender.other',
  }

  return gender ? settings.t(map[gender] || gender) : '-'
}
</script>

<template>
  <div v-loading="loading" class="profile-page">
    <div class="profile-grid">
      <section class="page-card account-card">
        <div class="account-hero">
          <el-avatar :size="64" class="profile-avatar">
            {{ account?.username?.charAt(0)?.toUpperCase() || '?' }}
          </el-avatar>
          <div class="account-copy">
            <h2>{{ displayValue(account?.username) }}</h2>
            <p>{{ displayValue(account?.email) }}</p>
            <el-tag effect="light" size="small">{{ displayValue(account?.roleName) }}</el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item :label="settings.t('user.username')">
            {{ displayValue(account?.username) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('user.email')">
            {{ displayValue(account?.email) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('user.role')">
            {{ displayValue(account?.roleName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.status')">
            {{ statusLabel(account?.status) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('profile.lastLogin')">
            {{ displayValue(account?.lastLogin) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="page-card employee-card">
        <div class="section-heading">
          <el-icon><Postcard /></el-icon>
          <span>{{ settings.t('profile.employeeProfile') }}</span>
        </div>

        <el-empty
          v-if="!employee"
          :description="settings.t('profile.noEmployee')"
          :image-size="80"
        />

        <el-descriptions v-else :column="1" border>
          <el-descriptions-item :label="settings.t('employee.employeeCode')">
            {{ displayValue(employee.employeeCode) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.fullName')">
            {{ displayValue(employee.fullName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.gender')">
            {{ genderLabel(employee.gender) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.phoneNumber')">
            {{ displayValue(employee.phoneNumber) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.personalEmail')">
            {{ displayValue(employee.personalEmail) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.department')">
            {{ displayValue(employee.departmentName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.position')">
            {{ displayValue(employee.positionName) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.hireDate')">
            {{ displayValue(employee.joinDate) }}
          </el-descriptions-item>
          <el-descriptions-item :label="settings.t('employee.status')">
            {{ statusLabel(employee.status) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </div>

    <div class="profile-grid forms-grid">
      <section class="page-card">
        <div class="section-heading">
          <el-icon><Message /></el-icon>
          <span>{{ settings.t('profile.changeEmail') }}</span>
        </div>

        <el-form
          ref="emailFormRef"
          :model="emailForm"
          :rules="emailRules"
          label-position="top"
          class="profile-form"
        >
          <el-form-item :label="settings.t('user.email')" prop="email">
            <el-input v-model="emailForm.email" autocomplete="email" />
          </el-form-item>
          <el-button type="primary" :loading="savingEmail" @click="saveEmail">
            <el-icon><Check /></el-icon>
            {{ settings.t('common.save') }}
          </el-button>
        </el-form>
      </section>

      <section class="page-card">
        <div class="section-heading">
          <el-icon><Lock /></el-icon>
          <span>{{ settings.t('profile.changePassword') }}</span>
        </div>

        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-position="top"
          class="profile-form"
        >
          <el-form-item :label="settings.t('profile.currentPassword')" prop="currentPassword">
            <el-input v-model="passwordForm.currentPassword" type="password" show-password autocomplete="current-password" />
          </el-form-item>
          <el-form-item :label="settings.t('profile.newPassword')" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item :label="settings.t('profile.confirmPassword')" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-button type="primary" :loading="savingPassword" @click="changePassword">
            <el-icon><Check /></el-icon>
            {{ settings.t('profile.updatePassword') }}
          </el-button>
        </el-form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 320px;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 20px;
}

.account-card,
.employee-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.account-hero {
  display: flex;
  align-items: center;
  gap: 14px;
}

.profile-avatar {
  background: var(--vx-primary);
  color: #FFFFFF;
  font-size: var(--vx-font-size-3xl);
  font-weight: 700;
  flex-shrink: 0;
}

.account-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.account-copy h2 {
  color: var(--vx-text);
  font-size: var(--vx-font-size-2xl);
  font-weight: 700;
  line-height: 1.2;
}

.account-copy p {
  color: var(--vx-text-secondary);
  word-break: break-all;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vx-text);
  font-weight: 700;
}

.profile-form {
  margin-top: 18px;
  max-width: 520px;
}

.forms-grid {
  align-items: start;
}

@media (max-width: 980px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
