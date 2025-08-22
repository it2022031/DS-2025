<template>
  <section class="edit-property section bg-light py-5">
    <div class="container">
      <h2 class="mb-4">Edit Property #{{ property && property.id }}</h2>

      <div v-if="loading">Loading...</div>
      <div v-else-if="error" class="text-danger">Failed to load property.</div>
      <div v-else-if="property">
        <!-- Property Edit Form -->
        <form @submit.prevent="saveProperty">
          <div class="mb-3">
            <label class="form-label">Name</label>
            <input v-model="property.name" type="text" class="form-control" required />
          </div>

          <div class="mb-3">
            <label class="form-label">Description</label>
            <textarea v-model="property.description" class="form-control" rows="4"></textarea>
          </div>

          <div class="row g-3">
            <div class="col-md-4">
              <label class="form-label">City</label>
              <input v-model="property.city" type="text" class="form-control" required />
            </div>
            <div class="col-md-4">
              <label class="form-label">Country</label>
              <input v-model="property.country" type="text" class="form-control" required />
            </div>
            <div class="col-md-4">
              <label class="form-label">Street</label>
              <input v-model="property.street" type="text" class="form-control" />
            </div>
          </div>

          <div class="row g-3 mt-3">
            <div class="col-md-4">
              <label class="form-label">Postal Code</label>
              <input v-model="property.postalCode" type="text" class="form-control" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Square Meters</label>
              <input v-model.number="property.squareMeters" type="number" class="form-control" min="0" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Price Per Day</label>
              <input v-model.number="property.price" type="number" class="form-control" min="0" />
            </div>
            <div class="col-md-4">
              <label class="form-label">Status</label>

              <select v-if="userRole === 'ADMIN'" v-model="property.approvalStatus" class="form-select">
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
              </select>

              <div v-else class="form-control-plaintext">
                {{ property.approvalStatus }}
              </div>
            </div>
          </div>

          <div class="mt-4 d-flex justify-content-end">
            <router-link to="/list-properties" class="btn btn-link me-3">Cancel</router-link>
            <button type="submit" class="btn btn-primary">Save Changes</button>
          </div>
        </form>

        <!-- Photo Upload Section -->
        <div class="mt-4">
          <label class="form-label">Upload Property Photos</label>
          <input type="file" multiple @change="handlePhotoChange" class="form-control mb-2" />
          <button class="btn btn-secondary" @click="uploadPhotos" :disabled="!selectedPhotos.length">
            Upload Photos
          </button>

          <!-- Preview selected images -->
          <div class="mt-3" v-if="previewPhotos.length">
            <h6>Selected Photos:</h6>
            <div class="d-flex flex-wrap">
              <div v-for="(src, index) in previewPhotos" :key="index" class="me-2 mb-2">
                <img :src="src" alt="preview" width="100" height="100" class="border rounded" />
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PropertyEdit',
  data() {
    return {
      property: { approvalStatus: "" },
      loading: false,
      error: false,
      selectedPhotos: [], // raw File objects
      previewPhotos: []   // preview URLs using FileReader
    };
  },
  computed: {
    userRole() {
      return (localStorage.getItem("userRole") || "").toUpperCase();
    },
    userId() {
      return Number(localStorage.getItem("userId"));
    }
  },
  async mounted() {
    this.loading = true;
    const id = this.$route.params.id;
    const token = localStorage.getItem('token');
    try {
      const response = await axios.get(`http://localhost:8080/api/properties/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      this.property = response.data;
    } catch (err) {
      console.error('Error loading property:', err.response ? err.response.status : err.message);
      this.error = true;
    } finally {
      this.loading = false;
    }
  },
  methods: {
    async saveProperty() {
      const token = localStorage.getItem("token");

      const payload = {
        name: this.property.name,
        price: this.property.price,
        description: this.property.description,
        city: this.property.city,
        country: this.property.country,
        street: this.property.street,
        postalCode: this.property.postalCode,
        squareMeters: this.property.squareMeters
      };

      try {
        const response = await axios.patch(
            `http://localhost:8080/api/properties/${this.property.id}`,
            payload,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        console.log("Property updated:", response.data);
        alert("Property updated successfully!");
      } catch (err) {
        console.error("Error saving property:", err);
        alert("Failed to save changes.");
      }
    },

    handlePhotoChange(event) {
      this.selectedPhotos = Array.from(event.target.files);
      this.previewPhotos = [];

      // Generate previews using FileReader (like avatar)
      this.selectedPhotos.forEach(file => {
        const reader = new FileReader();
        reader.onload = e => {
          this.previewPhotos.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
    },

    async uploadPhotos() {
      if (!this.selectedPhotos.length) return;

      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      const formData = new FormData();
      // Use "file" field name for backend
      this.selectedPhotos.forEach(file => formData.append("file", file));

      try {
        const response = await axios.post(
            `http://localhost:8080/api/properties/${this.property.id}/photos`,
            formData,
            {
              headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "multipart/form-data"
              }
            }
        );
        console.log("Photos uploaded successfully:", response.data);
        alert("✅ Photos uploaded successfully!");
        this.selectedPhotos = [];
        this.previewPhotos = [];
      } catch (err) {
        console.error("❌ Error uploading photos:", err);
        alert("Failed to upload photos.");
      }
    },
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}
img {
  object-fit: cover;
}
</style>
