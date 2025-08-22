<template>
  <section class="property-details section bg-light py-5">
    <div class="container">
      <div v-if="loading" class="text-center">Loading property...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load property.</div>
      <div v-else-if="!property" class="text-center">Property not found.</div>
      <div v-else class="row">
        <!-- Images -->
        <div class="col-md-6">
          <!-- Main photo -->
          <img
              :src="mainPhotoUrl"
              :alt="property.name"
              class="img-fluid rounded shadow mb-3"
          />

          <!-- Thumbnails -->
          <div v-if="property.photos && property.photos.length > 1" class="d-flex flex-wrap">
            <img
                v-for="photo in property.photos"
                :key="photo.id"
                :src="photo.url"
                :alt="photo.filename"
                class="border rounded me-2 mb-2 thumbnail"
                @click="setMainPhoto(photo.url)"
            />
          </div>
        </div>

        <!-- Info -->
        <div class="col-md-6">
          <h2>{{ property.name }}</h2>
          <p><strong>Owner:</strong> 👤 {{ property.ownerFirstName }} {{ property.ownerLastName }}</p>
          <p>{{ property.description }}</p>
          <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
          <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
          <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
          <p><strong>Price per day:</strong> {{ property.price }} €</p>

          <!-- Date pickers -->
          <div class="form-group mb-2">
            <label>Check-in Date</label>
            <input type="date" v-model="checkinDate" class="form-control" />
          </div>
          <div class="form-group mb-2">
            <label>Check-out Date</label>
            <input type="date" v-model="checkoutDate" class="form-control" />
          </div>

          <!-- Availability -->
          <div v-if="checkinDate && checkoutDate" class="mt-2">
            <span
                v-if="isAvailable(property.id, checkinDate, checkoutDate)"
                class="text-success"
            >✅ Available for your dates</span>
            <span
                v-else
                class="text-danger"
            >❌ Not available for your dates</span>
          </div>

          <!-- Action -->
          <button
              class="btn btn-primary mt-3"
              :disabled="!checkinDate || !checkoutDate || !isAvailable(property.id, checkinDate, checkoutDate)"
              @click="bookProperty"
          >
            Book Now
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "PropertyDetails",
  data() {
    return {
      property: null,
      loading: false,
      error: false,
      checkinDate: "",
      checkoutDate: "",
      occupiedDates: [],
      mainPhotoUrl: "/default-property.jpg", // default main photo
    };
  },
  methods: {
    async fetchProperty() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `http://localhost:8080/api/properties/${this.$route.params.id}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.property = response.data;

        // Fetch occupied dates
        const datesRes = await axios.get(
            `http://localhost:8080/api/properties/${this.property.id}/closed-dates`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.occupiedDates = datesRes.data.map(d => ({
          startDate: new Date(d.startDate),
          endDate: new Date(d.endDate)
        }));

        // Fetch photos
        try {
          const photosRes = await axios.get(
              `http://localhost:8080/api/properties/${this.property.id}/photos`,
              { headers: { Authorization: `Bearer ${token}` } }
          );
          this.property.photos = photosRes.data.map(photo => ({
            ...photo,
            url: photo.url || "/default-property.jpg",
          }));

          // set main photo
          if (this.property.photos && this.property.photos.length > 0) {
            this.mainPhotoUrl = this.property.photos[0].url;
          }
        } catch (err) {
          console.error("Error fetching property photos:", err);
          this.property.photos = [];
        }
      } catch (err) {
        console.error(err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
    isAvailable(propertyId, checkin, checkout) {
      if (!this.occupiedDates.length) return true;
      const inDate = new Date(checkin);
      const outDate = new Date(checkout);

      return !this.occupiedDates.some(period => {
        return inDate <= period.endDate && outDate >= period.startDate;
      });
    },
    async bookProperty() {
      if (!this.checkinDate || !this.checkoutDate) {
        alert("Please select both check-in and check-out dates");
        return;
      }

      try {
        const token = localStorage.getItem("token");

        const rentalData = {
          propertyId: this.property.id,
          startDate: this.checkinDate,
          endDate: this.checkoutDate
        };

        const response = await axios.post(
            "http://localhost:8080/api/rentals/add",
            rentalData,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        alert("✅ Rental created successfully!");
        console.log("Rental created:", response.data);

        // Refresh occupied dates
        await this.fetchProperty();
      } catch (err) {
        console.error("Failed to create rental:", err);
        alert("❌ Failed to create rental. Please try again.");
      }
    },
    setMainPhoto(url) {
      this.mainPhotoUrl = url;
    }
  },
  created() {
    this.fetchProperty();
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

img {
  max-height: 350px;
  object-fit: cover;
}

.thumbnail {
  width: 70px;
  height: 70px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.thumbnail:hover {
  transform: scale(1.05);
}
</style>
