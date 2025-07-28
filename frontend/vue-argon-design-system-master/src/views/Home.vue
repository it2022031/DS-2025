<template>
  <div class="text-center py-5">
    <h1>🏠 Καλώς ήρθες στην εφαρμογή!</h1>
    <p>Επέλεξε τι θέλεις να κάνεις:</p>

    <router-link to="/profile" class="btn btn-primary m-2">👤 Profile</router-link>
    <router-link to="/properties/add" class="btn btn-success m-2">➕ New Ad</router-link>
    <router-link to="/list-properties" class="btn btn-info m-2">📃 Rentals List</router-link>
    <router-link to="/rentals" class="btn btn-warning m-2">📅 Rent a Property</router-link>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.btn {
  font-size: 1.2rem;
  padding: 10px 20px;
  border-radius: 30px;
}
</style>
