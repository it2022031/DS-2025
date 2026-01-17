<template>
  <section class="list-properties section bg-secondary py-5">
    <div class="container d-flex flex-column">
      <h2 class="text-center mb-4" style="color: #343a40;">🏠 Rent A Property</h2>

      <!-- Search bar -->
      <div class="search-bar mb-4 text-center">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Search by property name..."
            class="form-control search-input"
        />
      </div>

      <div class="row">
        <!-- Filters section -->
        <div class="col-md-3 mb-4">
          <div class="filters bg-white p-3 rounded">
            <h4>Filters</h4>
            <div class="form-group mb-2">
              <label>Country</label>
              <input type="text" v-model="filters.country" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>City</label>
              <input type="text" v-model="filters.city" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>Square Meters (min)</label>
              <input type="number" v-model.number="filters.squareMeters" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>Postal Code</label>
              <input type="text" v-model="filters.postalCode" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>Price per Day (max €)</label>
              <input type="number" v-model.number="filters.price" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>Check-in Date</label>
              <input type="date" v-model="checkinDate" class="form-control" />
            </div>
            <div class="form-group mb-2">
              <label>Check-out Date</label>
              <input type="date" v-model="checkoutDate" class="form-control" />
            </div>
            <button class="btn btn-primary w-100 mt-2" @click="clearFilters">Clear Filters</button>
          </div>
        </div>

        <!-- Properties list -->
        <div class="col-md-9">
          <div v-if="loading" class="text-center text-white">Loading properties...</div>
          <div v-else-if="error" class="text-center text-danger">Failed to load properties.</div>
          <div v-else-if="filteredProperties.length === 0" class="text-center">No properties found.</div>

          <ul v-else class="approved-properties list-unstyled">
            <li
                v-for="property in filteredProperties"
                :key="property.id"
                class="property-card mb-3 d-flex"
            >
              <router-link
                  :to="{ name: 'PropertyDetails', params: { id: property.id } }"
                  class="d-flex w-100 text-decoration-none text-dark"
                  target="_parent"
              >
                <!-- Show first photo -->
                <img
                    :src="property.imageUrl || '/default-property.jpg'"
                    :alt="property.name"
                    class="property-image"
                />
                <div class="property-info p-3 flex-grow-1">
                  <h3>{{ property.name }}</h3>
                  <p><strong>Owner:</strong> 👤 {{ property.ownerFirstName }} {{ property.ownerLastName }}</p>
                  <p>{{ property.description }}</p>
                  <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
                  <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
                  <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
                  <p><strong>Price per day:</strong> {{ property.price }} €</p>
                </div>
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "AllProperties",
  data() {
    return {
      properties: [],
      loading: false,
      error: false,
      searchQuery: "",
      checkinDate: "",
      checkoutDate: "",
      filters: {
        country: "",
        city: "",
        squareMeters: null,
        postalCode: "",
        price: null,
      },
      occupiedDatesCache: {}, // store occupied dates per property
    };
  },
  computed: {
    filteredProperties() {
      return this.properties.filter((p) => {
        const matchesSearch =
            p.name && p.name.toLowerCase().includes(this.searchQuery.toLowerCase());

        const matchesCountry = this.filters.country
            ? p.country && p.country.toLowerCase().includes(this.filters.country.toLowerCase())
            : true;

        const matchesCity = this.filters.city
            ? p.city && p.city.toLowerCase().includes(this.filters.city.toLowerCase())
            : true;

        const matchesSquare = this.filters.squareMeters
            ? p.squareMeters >= this.filters.squareMeters
            : true;

        const matchesPostal = this.filters.postalCode
            ? p.postalCode && p.postalCode.toString().includes(this.filters.postalCode)
            : true;

        const matchesPrice = this.filters.price ? p.price <= this.filters.price : true;

        const matchesDates =
            this.checkinDate && this.checkoutDate
                ? this.isAvailable(p.id, this.checkinDate, this.checkoutDate)
                : true;

        return (
            matchesSearch &&
            matchesCountry &&
            matchesCity &&
            matchesSquare &&
            matchesPostal &&
            matchesPrice &&
            matchesDates
        );
      });
    },
  },
  methods: {
    async fetchProperties() {
      this.loading = true;
      this.error = false;

      try {
        // IMPORTANT:
        // this.$api already has baseURL="/api" and token interceptor.
        // So here we call endpoints WITHOUT the /api prefix.
        const response = await this.$api.get("/properties/all");

        // Only approved properties
        this.properties = (response.data || []).filter((p) => p.approvalStatus === "APPROVED");

        // Fetch photos for each property
        await Promise.all(
            this.properties.map(async (property) => {
              try {
                const photosRes = await this.$api.get(`/properties/${property.id}/photos`);

                if (photosRes.data && photosRes.data.length > 0) {
                  // Ensure image URL is ALWAYS relative through nginx (/api/...)
                  // If backend returns absolute url, normalize it.
                  const first = photosRes.data[0];
                  const url = first.url || first.baseUrl || "";
                  property.imageUrl = this.normalizePhotoUrl(url, first.id);
                  property.photos = photosRes.data.map((ph) => ({
                    ...ph,
                    url: this.normalizePhotoUrl(ph.url || ph.baseUrl || "", ph.id),
                  }));
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

    // If backend returns absolute URLs, force them to go through our nginx /api proxy.
    normalizePhotoUrl(url, photoId) {
      // If backend already gives "/api/..." or "/properties/photos/..", normalize.
      if (typeof url !== "string") return "/default-property.jpg";

      // If it's already relative and points to api photos, keep it (but avoid /api/api).
      if (url.startsWith("/api/")) return url.replace("/api/api/", "/api/");

      // Some backends return "/properties/photos/{id}" or "/api/properties/photos/{id}"
      if (url.startsWith("/properties/photos/")) return `/api${url}`;
      if (url.startsWith("/api/properties/photos/")) return url.replace("/api/api/", "/api/");

      // If it's absolute (http://localhost:8080/...), strip host and route via /api
      if (url.startsWith("http://") || url.startsWith("https://")) {
        try {
          const u = new URL(url);
          // u.pathname could be "/api/properties/photos/1" or "/properties/photos/1"
          if (u.pathname.startsWith("/api/")) return u.pathname.replace("/api/api/", "/api/");
          return `/api${u.pathname}`;
        } catch {
          // ignore
        }
      }

      // If backend returned empty but we have photoId, build it.
      if (photoId != null) return `/api/properties/photos/${photoId}`;

      return "/default-property.jpg";
    },

    clearFilters() {
      this.searchQuery = "";
      this.checkinDate = "";
      this.checkoutDate = "";
      this.filters = {
        country: "",
        city: "",
        squareMeters: null,
        postalCode: "",
        price: null,
      };
    },

    async fetchOccupiedDates(propertyId) {
      if (this.occupiedDatesCache[propertyId]) return this.occupiedDatesCache[propertyId];

      try {
        const response = await this.$api.get(`/properties/${propertyId}/closed-dates`);

        this.occupiedDatesCache[propertyId] = (response.data || []).map((d) => ({
          startDate: new Date(d.startDate),
          endDate: new Date(d.endDate),
        }));

        return this.occupiedDatesCache[propertyId];
      } catch (err) {
        console.error(`Failed to fetch occupied dates for property ${propertyId}`, err);
        return [];
      }
    },

    isAvailable(propertyId, checkin, checkout) {
      const checkinDate = new Date(checkin);
      const checkoutDate = new Date(checkout);
      const occupied = this.occupiedDatesCache[propertyId];
      if (!occupied) return true; // not yet fetched, assume available

      // Check if any occupied period overlaps
      return !occupied.some((period) => checkinDate <= period.endDate && checkoutDate >= period.startDate);
    },
  },

  async created() {
    await this.fetchProperties();

    // Pre-fetch occupied dates for all properties
    for (let property of this.properties) {
      await this.fetchOccupiedDates(property.id);
    }
  },
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.property-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.property-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.property-image {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 12px 0 0 12px;
  flex-shrink: 0;
}

.property-info h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #2c3e50;
}

.property-info p {
  margin: 5px 0;
  color: #555;
  font-size: 15px;
}

.filters h4 {
  margin-bottom: 15px;
}

.search-input {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.bg-secondary {
  background-color: #343a40;
}

.text-white {
  color: white;
}

.text-danger {
  color: #dc3545;
}

.py-5 {
  padding-top: 3rem;
  padding-bottom: 3rem;
}
</style>
