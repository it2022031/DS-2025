<template>
  <div class="home-page py-5">
    <!-- Hero section -->
    <section class="hero text-center mb-5">
      <h1 class="display-4 fw-bold">🏠 Καλώς ήρθες στην πλατφόρμα μας!</h1>
      <p class="lead mt-3">Βρες το ιδανικό ακίνητο ή διαχειρίσου τα δικά σου εύκολα και γρήγορα.</p>
    </section>

    <!-- Actions section -->
    <section class="actions container">
      <div class="row justify-content-center g-4">
        <!-- Rentals List -->
        <div class="col-md-4">
          <router-link to="/rentals" class="card text-decoration-none text-dark h-100 shadow-sm hover-card">
            <div class="card-body text-center">
              <div class="icon mb-3">📃</div>
              <h5 class="card-title">Rentals List</h5>
              <p class="card-text">Δες όλα τα διαθέσιμα ακίνητα και τις λεπτομέρειές τους.</p>
            </div>
          </router-link>
        </div>

        <!-- Rent a Property -->
        <div class="col-md-4">
          <router-link to="/approved-properties" class="card text-decoration-none text-dark h-100 shadow-sm hover-card">
            <div class="card-body text-center">
              <div class="icon mb-3">📅</div>
              <h5 class="card-title">Rent a Property</h5>
              <p class="card-text">Κλείσε εύκολα τις ημερομηνίες που σε ενδιαφέρουν.</p>
            </div>
          </router-link>
        </div>

        <!-- Add Property (owners/admins only) -->
        <div v-if="isOwnerOrAdmin" class="col-md-4">
          <router-link to="/properties/add" class="card text-decoration-none text-dark h-100 shadow-sm hover-card">
            <div class="card-body text-center">
              <div class="icon mb-3">➕</div>
              <h5 class="card-title">Add Property</h5>
              <p class="card-text">Δημιούργησε ένα νέο ακίνητο και διαχειρίσου το εύκολα.</p>
            </div>
          </router-link>
        </div>

        <!-- List Users (admins only) -->
        <div v-if="isAdmin" class="col-md-4">
          <router-link to="/users" class="card text-decoration-none text-dark h-100 shadow-sm hover-card">
            <div class="card-body text-center">
              <div class="icon mb-3">👥</div>
              <h5 class="card-title">List Users</h5>
              <p class="card-text">Δες και διαχειρίσου όλους τους χρήστες της πλατφόρμας.</p>
            </div>
          </router-link>
        </div>

        <!-- About us -->
        <div class="col-md-4">
          <router-link to="/about" class="card text-decoration-none text-dark h-100 shadow-sm hover-card">
            <div class="card-body text-center">
              <div class="icon mb-3">ℹ️</div>
              <h5 class="card-title">About Us</h5>
              <p class="card-text">Μάθε περισσότερα για την πλατφόρμα και τους στόχους μας.</p>
            </div>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: "Home",
  computed: {
    userRole() {
      return (localStorage.getItem("userRole") || "").toLowerCase();
    },
    isOwnerOrAdmin() {
      return this.userRole === "owner" || this.userRole === "admin";
    },
    isAdmin() {
      return this.userRole === "admin";
    }
  }
};
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.hero {
  background-color: #fff;
  padding: 50px 20px;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
}

.actions .card {
  border-radius: 15px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.actions .card:hover,
.actions .hover-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}

.actions .icon {
  font-size: 2.5rem;
}

.actions .card-title {
  font-weight: 600;
  margin-bottom: 0.5rem;
}
</style>
