<template>
  <div class="mt-4 container-fluid d-flex justify-content-center align-items-center">
    <div class="row g-4 justify-content-center home-grid">

      <div
          v-for="item in features"
          :key="item.title"
          class="col-6 col-md-4"
      >
        <div
            class="home-card text-center p-4 h-100"
            @click="go(item.route)"
        >
          <i :class="`bi ${item.icon} fs-1 mb-3`"></i>
          <div class="fw-semibold">{{ item.title }}</div>
        </div>
      </div>

    </div>
  </div>
</template>


<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const features = [
  {
    title: 'Hồ sơ',
    icon: 'bi-person-circle',
    route: '/profile'
  },
  {
    title: 'Chấm công',
    icon: 'bi-clock-history',
    route: '/attendance'
  },
  {
    title: 'Lương',
    icon: 'bi-cash-stack',
    route: '/salary'
  },
  {
    title: 'Nghỉ phép',
    icon: 'bi-calendar-check',
    route: '/leave'
  },
  {
    title: 'Thông báo',
    icon: 'bi-bell',
    route: '/notifications'
  },
  {
    title: 'Hỗ trợ',
    icon: 'bi-headset',
    route: '/support'
  }
]

const go = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-grid {
  max-width: 100%;
}

/* Card chính */
.home-card {
  cursor: pointer;
  background: #ffffff;
  border-radius: 16px;
  min-height: 150px;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  transition: all 0.25s ease;
}

/* Hover effect */
.home-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 35px rgba(0, 0, 0, 0.12);
}

/* Icon */
.home-card i {
  color: #0d6efd; /* xanh Bootstrap */
}

/* Text */
.home-card div {
  font-size: 1.05rem;
}

/* Mobile tinh chỉnh */
@media (max-width: 576px) {
  .home-card {
    min-height: 120px;
  }
}
</style>

