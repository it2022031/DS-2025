<template>
  <footer class="footer">
    <!-- Ο footer καθαρίστηκε πλήρως -->
  </footer>
</template>

<script>
export default {
  name: 'app-footer',
  data() {
    return {
      year: new Date().getFullYear()
    }
  }
};
</script>

<style scoped>
/* Αν θες να κρύψεις και το footer τελείως */
.footer {
  display: none;
}
</style>