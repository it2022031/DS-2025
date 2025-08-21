<!--<template>-->
<!--  <section class="container py-4">-->
<!--    <h1>Property Details</h1>-->
<!--    <p>Το ID του ακινήτου είναι: <strong>{{ idToShow }}</strong></p>-->
<!--    <p>checkInDate: <strong>{{ checkInDateToShow }}</strong></p>-->

<!--    <div class="col-md-9">-->
<!--      <div v-if="loading" class="text-center text-white">Loading property...</div>-->
<!--      <div v-else-if="error" class="text-center text-danger">Failed to load property.</div>-->
<!--      <div v-else-if="!property" class="text-center text-white">Property not found.</div>-->

<!--      <div v-else class="property-card d-flex">-->
<!--        <img-->
<!--            :src="property.imageUrl || '/default-property.jpg'"-->
<!--            :alt="property.name"-->
<!--            class="property-image"-->
<!--        />-->
<!--        <div class="property-info p-3 flex-grow-1">-->
<!--          <h3>{{ property.name }}</h3>-->
<!--          <p><strong>Owner:</strong> 👤 {{ property.username || 'N/A' }}</p>-->
<!--          <p>{{ property.description }}</p>-->
<!--          <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>-->
<!--          <p><strong>Status:</strong> {{ property.approvalStatus }}</p>-->
<!--          <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>-->
<!--          <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>-->
<!--          <p><strong>Price per day:</strong> {{ property.price }} €</p>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->
<!--  </section>-->
<!--</template>-->

<!--<script>-->
<!--import axios from "axios";-->

<!--export default {-->
<!--  name: "PropertyDetails",-->
<!--  props: {-->
<!--    id: { type: [String, Number], required: false },-->
<!--    checkInDate: { type: [String, Date], required: false }-->
<!--  },-->
<!--  data() {-->
<!--    return {-->
<!--      property: null,-->
<!--      loading: false,-->
<!--      error: false,-->
<!--    };-->
<!--  },-->
<!--  computed: {-->
<!--    idToShow() {-->
<!--      if (this.id !== undefined && this.id !== null && this.id !== "") {-->
<!--        return this.id;-->
<!--      }-->
<!--      if (this.$route && this.$route.params && this.$route.params.id !== undefined) {-->
<!--        return this.$route.params.id;-->
<!--      }-->
<!--      return "(χωρίς id)";-->
<!--    },-->
<!--    checkInDateToShow() {-->
<!--      if (this.checkInDate && this.checkInDate !== "") {-->
<!--        return this.checkInDate;-->
<!--      }-->
<!--      if (this.$route && this.$route.params && this.$route.params.checkInDate !== undefined) {-->
<!--        return this.$route.params.checkInDate;-->
<!--      }-->
<!--      return "NO Date";-->
<!--    }-->
<!--  },-->
<!--  methods: {-->
<!--    async fetchProperty() {-->
<!--      this.loading = true;-->
<!--      this.error = false;-->
<!--      try {-->
<!--        const token = localStorage.getItem("token");-->
<!--        const response = await axios.get(-->
<!--            `http://localhost:8080/api/properties/${this.idToShow}`,-->
<!--            {-->
<!--              headers: { Authorization: `Bearer ${token}` },-->
<!--            }-->
<!--        );-->
<!--        // only show if approved-->
<!--        if (response.data.approvalStatus === "APPROVED") {-->
<!--          this.property = response.data;-->
<!--        }-->
<!--      } catch (err) {-->
<!--        console.error(err);-->
<!--        this.error = true;-->
<!--      } finally {-->
<!--        this.loading = false;-->
<!--      }-->
<!--    },-->
<!--  },-->
<!--  mounted() {-->
<!--    this.fetchProperty();-->
<!--  },-->
<!--};-->
<!--</script>-->

<!--<style scoped>-->
<!--.container {-->
<!--  max-width: 600px;-->
<!--}-->
<!--.property-card {-->
<!--  border: 1px solid #ddd;-->
<!--  border-radius: 8px;-->
<!--  overflow: hidden;-->
<!--}-->
<!--.property-image {-->
<!--  width: 200px;-->
<!--  height: 200px;-->
<!--  object-fit: cover;-->
<!--}-->
<!--</style>-->


<template>
  <section class="property-details section bg-light py-5">
    <div class="container">
      <div v-if="loading" class="text-center">Loading property...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load property.</div>
      <div v-else-if="!property" class="text-center">Property not found.</div>
      <div v-else class="row">
        <!-- Image -->
        <div class="col-md-6">
          <img
              :src="property.imageUrl || '/default-property.jpg'"
              :alt="property.name"
              class="img-fluid rounded shadow"
          />
        </div>

        <!-- Info -->
        <div class="col-md-6">
          <h2>{{ property.name }}</h2>
          <p><strong>Owner:</strong> 👤{{ property.username || 'N/A' }}</p>
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
      occupiedDates: []
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

        // fetch occupied dates
        const datesRes = await axios.get(
            `http://localhost:8080/api/properties/${this.property.id}/closed-dates`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.occupiedDates = datesRes.data.map(d => ({
          startDate: new Date(d.startDate),
          endDate: new Date(d.endDate)
        }));
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
          startDate: this.checkinDate,   // match backend naming
          endDate: this.checkoutDate     // match backend naming
        };

        const response = await axios.post(
            "http://localhost:8080/api/rentals/add",
            rentalData,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        alert("✅ Rental created successfully!");
        console.log("Rental created:", response.data);

        // Refresh occupied dates so user sees the update
        await this.fetchProperty();
      } catch (err) {
        console.error("Failed to create rental:", err);
        alert("❌ Failed to create rental. Please try again.");
      }
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
</style>
