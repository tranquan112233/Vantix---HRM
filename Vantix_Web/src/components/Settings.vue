<template>
  <div class="settings-page p-4">
    <h3 class="fw-bold mb-4"><i class="bi bi-gear-wide-connected me-2"></i>Cài đặt hệ thống</h3>

    <div class="row g-4">
      <div class="col-md-8 col-lg-6">
        <div class="card border-0 shadow-sm p-4">
          <h5 class="mb-3 text-primary"><i class="bi bi-type me-2"></i>Tùy chỉnh kích thước chữ</h5>
          <p class="text-muted small mb-4">
            Kéo thanh trượt bên dưới để thay đổi kích thước chữ cho toàn bộ hệ thống, giúp bạn dễ đọc và làm việc thoải mái hơn.
          </p>

          <div class="d-flex align-items-center gap-3">
            <span style="font-size: 14px" class="fw-bold text-secondary">A</span>
            <input
                type="range"
                class="form-range"
                min="14"
                max="22"
                step="1"
                v-model="fontSize"
            >
            <span style="font-size: 22px" class="fw-bold text-dark">A</span>
          </div>

          <p class="text-muted mt-3 mb-0">
            Kích thước hiện tại: <strong class="text-primary">{{ fontSize }}px</strong>
          </p>

          <div class="p-3 border rounded mt-4 bg-light">
            <p :style="{ fontSize: fontSize + 'px' }" class="mb-0 text-dark">
              Vantix HRM: Đây là văn bản mẫu để bác xem thử kích thước chữ.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const fontSize = ref(16) // Kích thước mặc định

// 1. Hàm đổi Kích thước chữ TOÀN CỤC
const updateGlobalFontSize = (size) => {
  document.documentElement.style.fontSize = size + 'px'
  localStorage.setItem('user-font-size', size)
}

// 2. Theo dõi biến fontSize, kéo đến đâu chữ to đến đó
watch(fontSize, (newSize) => {
  updateGlobalFontSize(newSize)
})

// 3. Tự động load lại size chữ cũ khi mở trình duyệt
onMounted(() => {
  const savedSize = localStorage.getItem('user-font-size') || 16
  fontSize.value = parseInt(savedSize)
  updateGlobalFontSize(fontSize.value)
})
</script>

<style scoped>
</style>