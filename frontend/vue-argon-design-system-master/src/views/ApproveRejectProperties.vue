<template>
  <section class="list-properties section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">🏠 Properties List</h2>

      <div v-if="loading" class="text-center text-white">Loading properties...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load properties.</div>
      <div v-else-if="properties.length === 0" class="text-center text-white">No properties found.</div>

      <ul v-else class="property-list">
        <li v-for="property in properties" :key="property.id" class="property-card">
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
            <p><strong>Price:</strong> {{ property.price }} €</p>

            <div class="property-actions mt-3 d-flex align-items-center"
                 v-if="canModerate(property)">
              <button
                  @click="approveProperty(property.id)"
                  class="btn btn-sm btn-success mr-2"
              >
                ✅ Approve
              </button>

              <button
                  @click="rejectProperty(property.id)"
                  class="btn btn-sm btn-outline-danger"
              >
                ❌ Reject
              </button>
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
            `http://localhost:8080/api/properties/all`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
        );
        this.properties = response.data.filter(p => p.approvalStatus === "PENDING");
      } catch (err) {
        console.error("Error fetching properties:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    async approveProperty(propertyId) {
      try {
        const token = localStorage.getItem("token");
        await axios.post(
            `http://localhost:8080/api/properties/${propertyId}/approve`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );

        // instead of updating locally, refresh list from backend
        await this.fetchProperties();

        alert(`Property ${propertyId} approved ✅`);
      } catch (err) {
        console.error(`Error approving property ${propertyId}:`, err);
        alert(`Failed to approve property ${propertyId}`);
      }
    },

    async rejectProperty(propertyId) {
      try {
        const token = localStorage.getItem("token");
        await axios.post(
            `http://localhost:8080/api/properties/${propertyId}/reject`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );

        // refresh list from backend
        await this.fetchProperties();

        alert(`Property ${propertyId} rejected ❌`);
      } catch (err) {
        console.error(`Error rejecting property ${propertyId}:`, err);
        alert(`Failed to reject property ${propertyId}`);
      }
    },


    canModerate(property) {
      // Only ADMIN can approve/reject
      return this.userRole === "ADMIN";
    }
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

.property-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  margin-top: 12px;
}
</style>
