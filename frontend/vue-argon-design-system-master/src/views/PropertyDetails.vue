<template>
  <section class="container py-4">
    <h1>Property Details</h1>
    <p>Το ID του ακινήτου είναι: <strong>{{ idToShow }}</strong></p>
    <p>checkInDate: <strong>{{ checkInDateToShow }}</strong></p>

    <div class="col-md-9">
      <div v-if="loading" class="text-center text-white">Loading property...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load property.</div>
      <div v-else-if="!property" class="text-center text-white">Property not found.</div>

      <div v-else class="property-card d-flex">
        <img
            :src="property.imageUrl || '/default-property.jpg'"
            :alt="property.name"
            class="property-image"
        />
        <div class="property-info p-3 flex-grow-1">
          <h3>{{ property.name }}</h3>
          <p><strong>Owner:</strong> 👤 {{ property.username || 'N/A' }}</p>
          <p>{{ property.description }}</p>
          <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
          <p><strong>Status:</strong> {{ property.approvalStatus }}</p>
          <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
          <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
          <p><strong>Price per day:</strong> {{ property.price }} €</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "PropertyDetails",
  props: {
    id: { type: [String, Number], required: false },
    checkInDate: { type: [String, Date], required: false }
  },
  data() {
    return {
      property: null,
      loading: false,
      error: false,
    };
  },
  computed: {
    idToShow() {
      if (this.id !== undefined && this.id !== null && this.id !== "") {
        return this.id;
      }
      if (this.$route && this.$route.params && this.$route.params.id !== undefined) {
        return this.$route.params.id;
      }
      return "(χωρίς id)";
    },
    checkInDateToShow() {
      if (this.checkInDate && this.checkInDate !== "") {
        return this.checkInDate;
      }
      if (this.$route && this.$route.params && this.$route.params.checkInDate !== undefined) {
        return this.$route.params.checkInDate;
      }
      return "NO Date";
    }
  },
  methods: {
    async fetchProperty() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `http://localhost:8080/api/properties/${this.idToShow}`,
            {
              headers: { Authorization: `Bearer ${token}` },
            }
        );
        // only show if approved
        if (response.data.approvalStatus === "APPROVED") {
          this.property = response.data;
        }
      } catch (err) {
        console.error(err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
  },
  mounted() {
    this.fetchProperty();
  },
};
</script>

<style scoped>
.container {
  max-width: 600px;
}
.property-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}
.property-image {
  width: 200px;
  height: 200px;
  object-fit: cover;
}
</style>
