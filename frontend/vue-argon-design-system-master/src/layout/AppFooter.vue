<template>
  <footer class="footer">
    <div class="footer-content">
      <p>© {{ year }} Rental App. All rights reserved. Group-15</p>
      <p>
        <a href="#">Privacy Policy</a> |
        <a href="#">Terms of Service</a>
      </p>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
  data() {
    return {
      year: new Date().getFullYear()
    }
  }
};
</script>

<style scoped>
.footer {
  background-color: #343a40;
  color: #f5f6fa;
  text-align: center;
  padding: 2rem 1rem;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
}

.footer a {
  color: #f5f6fa;
  text-decoration: none;
  margin: 0 0.5rem;
  transition: color 0.3s;
}

.footer a:hover {
  color: #adb5bd;
}

.footer-content p {
  margin: 0.5rem 0;
}
</style>
