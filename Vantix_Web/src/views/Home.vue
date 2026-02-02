<template>
<!--  <AppHeader />-->
  <div class="home">
    <div class="card-grid">
      <div
          class="card"
          v-for="item in features"
          :key="item.title"
          @click="go(item.route)"
      >
        <div class="icon">{{ item.icon }}</div>
        <h3>{{ item.title }}</h3>
        <p>{{ item.desc }}</p>
      </div>
    </div>
  </div>
  <AppFooter />
</template>

<script>
import AppHeader from "../components/AppHeader.vue";
import AppFooter from "../components/AppFooter.vue";

export default {
  name: 'EmployeeHome',
  components: {AppFooter, AppHeader},
  data() {
    return {
      features: [
        {
          title: 'Thông tin cá nhân',
          desc: 'Xem & cập nhật hồ sơ của bạn',
          icon: '👤',
          route: '/profile'
        },
        {
          title: 'Chấm công',
          desc: 'Xem lịch sử ca làm việc',
          icon: '⏱️',
          route: '/attendance'
        },
        {
          title: 'Nghỉ phép',
          desc: 'Gửi & theo dõi đơn nghỉ phép',
          icon: '📄',
          route: '/leave'
        },
        {
          title: 'Lương',
          desc: 'Xem bảng lương cá nhân',
          icon: '💰',
          route: '/salary'
        }
      ]
    }
  },
  methods: {
    go(route) {
      this.$router.push(route)
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e3f2fd, #f8fbff);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
  width: 70%;
  max-width: 900px;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
  cursor: pointer;

  border: 1px solid #e0f0ff;
  box-shadow: 0 8px 20px rgba(0, 102, 204, 0.08);

  transition: all 0.25s ease;
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 14px 32px rgba(0, 102, 204, 0.18);
  border-color: #2196f3;
}

.icon {
  font-size: 42px;
  margin-bottom: 14px;
  color: #2196f3;
}

h3 {
  margin: 10px 0 6px;
  font-size: 18px;
  color: #0d47a1;
}

p {
  font-size: 14px;
  color: #546e7a;
}
</style>

