<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { payrollSettingApi } from '@/api'
import { useSettingsStore } from '@/stores/settings'
import { isCancelError, showApiError } from '@/utils/errors'

const settings = useSettingsStore()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  employeeSocialInsuranceRate: 0.08,
  employeeHealthInsuranceRate: 0.015,
  employeeUnemploymentInsuranceRate: 0.01,
  employerSocialInsuranceRate: 0.175,
  employerHealthInsuranceRate: 0.03,
  employerUnemploymentInsuranceRate: 0.01,
  governmentBaseSalary: 2340000,
  minRegionalSalary: 4960000,
  personalDeduction: 11000000,
  dependentDeduction: 4400000,
  mealAllowanceExempt: 730000,
  overtimeWeekdayMultiplier: 1.5,
  overtimeWeekendMultiplier: 2,
  overtimeHolidayMultiplier: 3,
  overtimeNightMultiplier: 1.3,
})

const employeeInsuranceTotal = computed(() =>
  form.employeeSocialInsuranceRate + form.employeeHealthInsuranceRate + form.employeeUnemploymentInsuranceRate,
)

const employerInsuranceTotal = computed(() =>
  form.employerSocialInsuranceRate + form.employerHealthInsuranceRate + form.employerUnemploymentInsuranceRate,
)

onMounted(fetchSettings)

async function fetchSettings() {
  loading.value = true
  try {
    const res = await payrollSettingApi.get()
    Object.assign(form, normalize(res.data))
  } catch (e) {
    showApiError(e, settings, 'common.loadFailed')
  } finally {
    loading.value = false
  }
}

async function saveSettings() {
  saving.value = true
  try {
    const res = await payrollSettingApi.update({ ...form })
    Object.assign(form, normalize(res.data))
    ElMessage.success(settings.t('settings.saved'))
  } catch (e) {
    showApiError(e, settings)
  } finally {
    saving.value = false
  }
}

async function resetSettings() {
  try {
    await ElMessageBox.confirm(
      settings.t('payrollSettings.resetConfirm'),
      settings.t('common.confirm'),
      { type: 'warning' },
    )
    const res = await payrollSettingApi.reset()
    Object.assign(form, normalize(res.data))
    ElMessage.success(settings.t('settings.resetDone'))
  } catch (e) {
    if (!isCancelError(e)) showApiError(e, settings)
  }
}

function normalize(data) {
  return Object.fromEntries(Object.entries(data || {})
    .filter(([key]) => key in form)
    .map(([key, value]) => [key, Number(value)]))
}

function percent(value) {
  return `${(Number(value || 0) * 100).toFixed(2)}%`
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}
</script>

<template>
  <div class="payroll-settings page-card" v-loading="loading">
    <div class="settings-header">
      <div>
        <h2>{{ settings.t('payrollSettings.title') }}</h2>
        <p>{{ settings.t('payrollSettings.subtitle') }}</p>
      </div>
      <div class="settings-actions">
        <el-button @click="resetSettings">
          <el-icon><RefreshLeft /></el-icon>
          {{ settings.t('common.reset') }}
        </el-button>
        <el-button type="primary" :loading="saving" @click="saveSettings">
          <el-icon><Check /></el-icon>
          {{ settings.t('common.save') }}
        </el-button>
      </div>
    </div>

    <div class="summary-strip">
      <div class="summary-item">
        <span>{{ settings.t('payrollSettings.employeeInsuranceTotal') }}</span>
        <strong>{{ percent(employeeInsuranceTotal) }}</strong>
      </div>
      <div class="summary-item">
        <span>{{ settings.t('payrollSettings.employerInsuranceTotal') }}</span>
        <strong>{{ percent(employerInsuranceTotal) }}</strong>
      </div>
      <div class="summary-item">
        <span>{{ settings.t('payrollSettings.personalDeduction') }}</span>
        <strong>{{ formatMoney(form.personalDeduction) }}</strong>
      </div>
      <div class="summary-item">
        <span>{{ settings.t('payrollSettings.mealAllowanceExempt') }}</span>
        <strong>{{ formatMoney(form.mealAllowanceExempt) }}</strong>
      </div>
    </div>

    <el-form label-position="top" class="settings-form">
      <section class="form-section">
        <div class="form-section-header">
          <el-icon class="form-section-icon"><User /></el-icon>
          <div>
            <h4 class="form-section-title">{{ settings.t('payrollSettings.employeeInsurance') }}</h4>
            <p class="form-section-subtitle">{{ settings.t('payrollSettings.rateHint') }}</p>
          </div>
        </div>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.socialInsurance')">
              <el-input-number v-model="form.employeeSocialInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.healthInsurance')">
              <el-input-number v-model="form.employeeHealthInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.unemploymentInsurance')">
              <el-input-number v-model="form.employeeUnemploymentInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </section>

      <section class="form-section">
        <div class="form-section-header">
          <el-icon class="form-section-icon"><OfficeBuilding /></el-icon>
          <div>
            <h4 class="form-section-title">{{ settings.t('payrollSettings.employerInsurance') }}</h4>
            <p class="form-section-subtitle">{{ settings.t('payrollSettings.employerCostHint') }}</p>
          </div>
        </div>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.socialInsurance')">
              <el-input-number v-model="form.employerSocialInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.healthInsurance')">
              <el-input-number v-model="form.employerHealthInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.unemploymentInsurance')">
              <el-input-number v-model="form.employerUnemploymentInsuranceRate" :min="0" :max="1" :step="0.001" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </section>

      <section class="form-section">
        <div class="form-section-header">
          <el-icon class="form-section-icon"><Money /></el-icon>
          <div>
            <h4 class="form-section-title">{{ settings.t('payrollSettings.taxAndCap') }}</h4>
            <p class="form-section-subtitle">{{ settings.t('payrollSettings.amountHint') }}</p>
          </div>
        </div>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.governmentBaseSalary')">
              <el-input-number v-model="form.governmentBaseSalary" :min="0" :step="100000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.minRegionalSalary')">
              <el-input-number v-model="form.minRegionalSalary" :min="0" :step="100000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.personalDeduction')">
              <el-input-number v-model="form.personalDeduction" :min="0" :step="100000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.dependentDeduction')">
              <el-input-number v-model="form.dependentDeduction" :min="0" :step="100000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item :label="settings.t('payrollSettings.mealAllowanceExempt')">
              <el-input-number v-model="form.mealAllowanceExempt" :min="0" :step="10000" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </section>

      <section class="form-section">
        <div class="form-section-header">
          <el-icon class="form-section-icon"><Clock /></el-icon>
          <div>
            <h4 class="form-section-title">{{ settings.t('payrollSettings.overtime') }}</h4>
            <p class="form-section-subtitle">{{ settings.t('payrollSettings.multiplierHint') }}</p>
          </div>
        </div>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="6">
            <el-form-item :label="settings.t('payroll.otWeekday')">
              <el-input-number v-model="form.overtimeWeekdayMultiplier" :min="0" :step="0.1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="6">
            <el-form-item :label="settings.t('payroll.otWeekend')">
              <el-input-number v-model="form.overtimeWeekendMultiplier" :min="0" :step="0.1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="6">
            <el-form-item :label="settings.t('payroll.otHoliday')">
              <el-input-number v-model="form.overtimeHolidayMultiplier" :min="0" :step="0.1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="6">
            <el-form-item :label="settings.t('payroll.otNight')">
              <el-input-number v-model="form.overtimeNightMultiplier" :min="0" :step="0.1" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
      </section>
    </el-form>
  </div>
</template>

<style scoped>
.payroll-settings {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.settings-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.settings-header h2 {
  font-size: var(--vx-font-size-2xl);
  color: var(--vx-text);
  margin: 0 0 4px;
}

.settings-header p {
  color: var(--vx-text-secondary);
  line-height: 1.5;
}

.settings-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 12px;
}

.summary-item {
  border: 1px solid var(--vx-border);
  border-radius: 8px;
  padding: 14px;
  background: var(--vx-bg);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-item span {
  color: var(--vx-text-secondary);
  font-size: var(--vx-font-size-xs);
}

.summary-item strong {
  color: var(--vx-text);
  font-size: var(--vx-font-size-lg);
}

.settings-form :deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 900px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>
