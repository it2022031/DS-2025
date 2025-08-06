<template>
  <div class="text-center py-5">
    <h1>🏠 Καλώς ήρθες!</h1>
    <p>Επέλεξε τι θέλεις να κάνεις:</p>

    <!-- ΟΡΑΤΟ σε όλους -->
    <router-link to="/rentals" class="btn btn-info m-2">📃 Rentals List</router-link>
    <router-link to="/rent" class="btn btn-warning m-2">📅 Rent a Property</router-link>
    <router-link v-if="(userRole || '').toLowerCase() === 'owner' || (userRole || '').toLowerCase() === 'admin'" to="/properties/add" class="btn btn-success m-2">➕ Add Property</router-link>
    <router-link v-if="(userRole || '').toLowerCase() === 'admin'" to="/users"  class="btn btn-danger m-2"> 👥 List Users</router-link>
    <router-link to="/about" class="btn btn-warning m-2">ℹ️ About us</router-link>

  </div>
</template>

<script>
export default {
  name: 'Home',
  computed: {
    userRole() {
      return localStorage.getItem("userRole");
    },
  }
};
</script>
