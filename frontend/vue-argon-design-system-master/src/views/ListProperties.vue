<template>
  <section class="list-properties section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">🏠 Properties List</h2>

      <div v-if="loading" class="text-center text-white">Loading properties...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load properties.</div>
      <div v-else-if="properties.length === 0" class="text-center text-white">No properties found.</div>

      <ul v-else class="property-list">
        <li v-for="property in properties" :key="property.id" class="property-card">
          <!-- Αν δεν έχεις image url, βάζουμε ένα placeholder -->
          <img
              :src="property.imageUrl || '/default-property.jpg'"
              :alt="property.name"
              class="property-image"
          />
          <div class="property-info">
            <h3>{{ property.name }}</h3>
            <p><strong>Owner:</strong> 👤{{ property.username || 'N/A' }}</p>
            <p>{{ property.description }}</p>
            <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
            <p><strong>Status:</strong> {{ property.approvalStatus }}</p>
            <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
            <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
            <div class="mt-3">
              <router-link
                  v-if="canEdit(property)"
                  :to="`/properties/${property.id}/edit`"
                  class="btn btn-sm btn-outline-light"
              >
                Edit
              </router-link>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: "ListProperties",
  data() {
    return {
      properties: [],
      loading: false,
      error: false,
    };
  },
  computed: {
    userRole() {
      return (localStorage.getItem('userRole') || '').toUpperCase();
    },
    userId() {
      return Number(localStorage.getItem('userId'));
    },
  },
  methods: {
    async fetchProperties() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `http://localhost:8080/api/users/${this.userId}/properties`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
        );
        // backend επιστρέφει array από PropertyDto με ownerName και ownerId
        this.properties = response.data;
      } catch (err) {
        console.error("Error fetching properties:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
    canEdit(property) {
      return (
          this.userRole === "ADMIN" ||
          (this.userRole === "OWNER" && property.ownerId === this.userId)
      );
    },
  },
  mounted() {
    this.fetchProperties();
  },
};
</script>


<style scoped>
.section {
  min-height: 100vh;
}

.property-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 900px;
}

.property-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.property-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.property-image {
  width: 250px;
  height: 160px;
  object-fit: cover;
  border-radius: 12px 0 0 12px;
  flex-shrink: 0;
}

.property-info {
  padding: 20px;
  flex-grow: 1;
  position: relative;
}

.property-info h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #2c3e50;
}

.property-info p {
  margin: 5px 0;
  color: #555;
  font-size: 16px;
}

.property-info .btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
}

.text-center {
  text-align: center;
}

.text-white {
  color: white;
}

.text-danger {
  color: #dc3545;
}

.bg-secondary {
  background-color: #343a40;
}

.py-5 {
  padding-top: 3rem;
  padding-bottom: 3rem;
}
</style>
