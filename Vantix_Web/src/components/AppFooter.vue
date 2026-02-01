<template>
  <footer class="text-center text-muted py-3 mt-auto">
    <div class="container">
      <small>
        © {{ year }} <strong>Vantix Co.</strong> — All rights reserved
      </small>
    </div>
  </footer>
</template>

<script>
export default {
  name: "AppFooter",
  computed: {
    year() {
      return new Date().getFullYear();
    }
  }
};
</script>
