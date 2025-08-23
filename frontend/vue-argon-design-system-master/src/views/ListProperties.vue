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
          <!-- Display first photo or placeholder -->
          <img
              :src="property.photos && property.photos.length ? property.photos[0].url : '/default-property.jpg'"
              :alt="property.name"
              class="property-image"
          />
          <div class="property-info">
            <h3>{{ property.name }}</h3>
            <p>{{ property.description }}</p>
            <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
            <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
            <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
            <p><strong>Price Per Day:</strong> {{ property.price }} €</p>
            <p>
              <strong>Approval Status (by Admin):</strong>
              <span :class="statusClass(property.approvalStatus)">
                {{ property.approvalStatus }}
              </span>
            </p>

            <!-- Render all photos as thumbnails -->
            <div class="mt-2" v-if="property.photos && property.photos.length">
              <h6 class="text-white">Photos:</h6>
              <div class="d-flex flex-wrap">
                <img
                    v-for="photo in property.photos"
                    :key="photo.id"
                    :src="photo.url"
                    :alt="photo.filename"
                    class="border rounded me-2 mb-2"
                    width="60"
                    height="60"
                />
              </div>
            </div>

            <div
                class="property-actions mt-3 d-flex align-items-center"
                v-if="canEdit(property) || canDelete(property)"
            >
              <router-link
                  v-if="canEdit(property)"
                  :to="`/properties/${property.id}/edit`"
                  class="btn btn-sm btn-outline-light mr-2"
              >
                ✏️ Edit
              </router-link>

              <button
                  v-if="canDelete(property)"
                  @click="deleteProperty(property.id)"
                  class="btn btn-sm btn-outline-danger"
              >
                🗑 Delete
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
      searchQuery: "",       // 🔍 new
      selectedStatus: "ALL", // ⬇️ new
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
    statusClass(status) {
      if (!status) return "";
      switch (status.toLowerCase()) {
        case "approved":
          return "approved";
        case "pending":
          return "pending";
        case "rejected":
          return "rejected";
        default:
          return "";
      }
    },
    async fetchProperties() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `http://localhost:8080/api/users/${this.userId}/properties`,
            {
              headers: { Authorization: `Bearer ${token}` },
            }
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
                property.photos = photosRes.data.map((photo) => ({
                  ...photo,
                }));
              } catch (err) {
                console.error(`Error fetching photos for property ${property.id}:`, err);
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

    async deleteProperty(propertyId) {
      if (!confirm("Are you sure you want to delete this property?")) return;

      try {
        const token = localStorage.getItem("token");
        await axios.delete(`http://localhost:8080/api/properties/${propertyId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        this.properties = this.properties.filter((p) => p.id !== propertyId);

        alert(`Property ${propertyId} deleted 🗑`);
      } catch (err) {
        console.error(`Error deleting property ${propertyId}:`, err);
        alert(`Failed to delete property ${propertyId}`);
      }
    },

    canEdit(property) {
      return (
          this.userRole === "ADMIN" ||
          this.userRole === "USER" ||
          this.userRole === "RENTER"
      );
    },
    canDelete(property) {
      return (
          this.userRole === "ADMIN" ||
          this.userRole === "USER" ||
          this.userRole === "RENTER"
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

.property-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  margin-top: 12px;
}

.approved {
  color: #28a745;
  font-weight: bold;
}

.pending {
  color: #ffc107;
  font-weight: bold;
}

.rejected {
  color: #dc3545;
  font-weight: bold;
}
</style>
