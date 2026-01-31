<template>
  <nav
      class="navbar px-4 shadow-sm"
      style="background: linear-gradient(135deg, #1e88e5, #42a5f5);"
  >
    <!-- Logo -->
    <span class="navbar-brand text-white fw-semibold">
      Vantix Co.
    </span>

    <!-- User dropdown -->
    <div class="dropdown">
      <button
          class="btn btn-primary bg-white text-primary border-0 dropdown-toggle fw-medium"
          data-bs-toggle="dropdown"
      >
        👤 {{ user.fullName }}
      </button>

      <ul class="dropdown-menu dropdown-menu-end border-0 shadow">
        <li>
          <a class="dropdown-item" href="#">Thông tin cá nhân</a>
        </li>

        <li>
          <a class="dropdown-item" href="#">Đổi mật khẩu</a>
        </li>

        <!-- CHỈ ADMIN MỚI HIỆN -->
        <li v-if="isAdmin">
          <a class="dropdown-item fw-semibold text-primary" href="/admin">
            Admin Panel
          </a>
        </li>

        <li><hr class="dropdown-divider"></li>

        <li>
          <a class="dropdown-item text-danger" href="#">
            Đăng xuất
          </a>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script>
export default {
  name: "AppHeader",
  data() {
    return {
      user: {
        fullName: "Nguyễn Văn A",
        role: "Admin" // đổi thành "Staff" để test
      }
    };
  },
  computed: {
    isAdmin() {
      return this.user.role === "Admin";
    }
  }
};
</script>
