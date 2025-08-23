<template>
  <section class="list-properties section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center mb-4" style="color: #343a40;">🏠 Properties List</h2>

      <!-- 🔍 Search + Filter -->
      <div class="filters mb-4 d-flex justify-content-center gap-3 flex-wrap">
        <!-- Search bar -->
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Search by property name..."
            class="form-control search-input"
        />

        <!-- Dropdown filter -->
        <select v-model="selectedStatus" class="form-control filter-select">
          <option value="ALL">All</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      <div v-if="loading" class="text-center text-white">Loading properties...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load properties.</div>
      <div v-else-if="filteredProperties.length === 0" class="text-center text-white">No properties found.</div>

      <ul v-else class="property-list">
        <li v-for="property in filteredProperties" :key="property.id" class="property-card">
          <img
              :src="property.imageUrl || '/default-property.jpg'"
              :alt="property.name"
              class="property-image"
          />
          <div class="property-info">
            <h3>{{ property.name }}</h3>
            <p><strong>Owner:</strong> 👤{{ property.ownerFirstName}} {{ property.ownerLastName}}</p>
            <p>{{ property.description }}</p>
            <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
            <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
            <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
            <p><strong>Price:</strong> {{ property.price }} €</p>
            <!-- 🟢 Status with color -->
            <p>
              <strong>Status:</strong>
              <span :class="['status-badge', getStatusClass(property.approvalStatus)]">
                {{ property.approvalStatus }}
              </span>
            </p>

            <div class="property-actions mt-3 d-flex align-items-center" v-if="canModerate(property)">
              <button
                  v-if="property.approvalStatus === 'PENDING' || property.approvalStatus === 'REJECTED'"
                  @click="approveProperty(property.id)"
                  class="btn btn-sm btn-success mr-2"
              >
                ✅ Approve
              </button>

              <button
                  v-if="property.approvalStatus === 'PENDING' || property.approvalStatus === 'APPROVED'"
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
import axios from "axios";

export default {
  name: "ListProperties",
  data() {
    return {
      properties: [],
      loading: false,
      error: false,
      searchQuery: "",
      selectedStatus: "ALL", // <-- new state
    };
  },
  computed: {
    userRole() {
      return (localStorage.getItem("userRole") || "").toUpperCase();
    },
    userId() {
      return Number(localStorage.getItem("userId"));
    },
    filteredProperties() {
      return this.properties.filter((p) => {
        const matchesSearch =
            p.name && p.name.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesStatus =
            this.selectedStatus === "ALL" || p.approvalStatus === this.selectedStatus;
        return matchesSearch && matchesStatus;
      });
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
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.properties = response.data;

        // fetch photos for each property
        await Promise.all(
            this.properties.map(async (property) => {
              try {
                const photosRes = await axios.get(
                    `http://localhost:8080/api/properties/${property.id}/photos`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (photosRes.data && photosRes.data.length > 0) {
                  property.imageUrl = photosRes.data[0].url;
                  property.photos = photosRes.data;
                } else {
                  property.imageUrl = "/default-property.jpg";
                  property.photos = [];
                }
              } catch (err) {
                console.error(`Error fetching photos for property ${property.id}:`, err);
                property.imageUrl = "/default-property.jpg";
                property.photos = [];
              }
            })
        );
      } catch (err) {
        console.error("Error fetching properties:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    getStatusClass(status) {
      switch (status) {
        case "APPROVED":
          return "status-approved";
        case "PENDING":
          return "status-pending";
        case "REJECTED":
          return "status-rejected";
        default:
          return "";
      }
    },

    async approveProperty(propertyId) {
      const token = localStorage.getItem("token");
      try {
        await axios.post(
            `http://localhost:8080/api/properties/${propertyId}/approve`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );
        await this.fetchProperties();
        alert(`Property ${propertyId} approved ✅`);
      } catch (err) {
        console.error(`Error approving property ${propertyId}:`, err);
        alert(`Failed to approve property ${propertyId}`);
      }
    },

    async rejectProperty(propertyId) {
      const token = localStorage.getItem("token");
      try {
        await axios.post(
            `http://localhost:8080/api/properties/${propertyId}/reject`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );
        await this.fetchProperties();
        alert(`Property ${propertyId} rejected ❌`);
      } catch (err) {
        console.error(`Error rejecting property ${propertyId}:`, err);
        alert(`Failed to reject property ${propertyId}`);
      }
    },

    canModerate(property) {
      return this.userRole === "ADMIN";
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

.filters {
  max-width: 900px;
  margin: 0 auto 20px auto;
}

.search-input {
  width: 250px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.filter-select {
  width: 180px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
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

/* ✅ Status styles */
.status-badge {
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 6px;
}
.status-approved {
  color: #28a745;
}
.status-pending {
  color: #ffc107;
}
.status-rejected {
  color: #dc3545;
}
</style>
