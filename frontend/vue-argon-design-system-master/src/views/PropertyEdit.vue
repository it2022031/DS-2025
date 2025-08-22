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

        <!-- Existing Photos Section -->
        <div class="mt-4" v-if="photos.length">
          <h6>Existing Photos:</h6>
          <div class="d-flex flex-wrap">
            <div v-for="photo in photos" :key="photo.id" class="me-2 mb-2 position-relative">
              <img :src="photo.url" :alt="photo.filename" width="100" height="100" class="border rounded" />
              <button @click="deletePhoto(photo.id)"
                      class="btn btn-sm btn-danger position-absolute top-0 end-0"
                      style="font-size: 0.7rem;">
                🗑
              </button>
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
      selectedPhotos: [],
      previewPhotos: [],
      photos: [] // array for existing photos
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
      await this.fetchPhotos(); // fetch existing photos
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
      this.selectedPhotos.forEach(file => {
        const reader = new FileReader();
        reader.onload = e => this.previewPhotos.push(e.target.result);
        reader.readAsDataURL(file);
      });
    },

    async uploadPhotos() {
      if (!this.selectedPhotos.length) return;
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");
      const formData = new FormData();
      this.selectedPhotos.forEach(file => formData.append("file", file));

      try {
        await axios.post(
            `http://localhost:8080/api/properties/${this.property.id}/photos`,
            formData,
            {
              headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "multipart/form-data"
              }
            }
        );
        this.selectedPhotos = [];
        this.previewPhotos = [];
        alert("✅ Photos uploaded successfully!");
        await this.fetchPhotos(); // refresh existing photos
      } catch (err) {
        console.error("❌ Error uploading photos:", err);
        alert("Failed to upload photos.");
      }
    },

    async fetchPhotos() {
      const token = localStorage.getItem("token");
      try {
        const response = await axios.get(
            `http://localhost:8080/api/properties/${this.property.id}/photos`,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        // Map each photo to its direct URL endpoint
        this.photos = response.data.map(file => ({
          ...file,
          url: `http://localhost:8080/api/properties/photos/${file.id}`
        }));
      } catch (err) {
        console.error("Error fetching photos:", err);
        this.photos = [];
      }
    },

    async deletePhoto(photoId) {
      if (!confirm("Are you sure you want to delete this photo?")) return;
      const token = localStorage.getItem("token");
      try {
        await axios.delete(
            `http://localhost:8080/api/properties/photos/${photoId}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        alert("Photo deleted ✅");
        await this.fetchPhotos(); // refresh photos after delete
      } catch (err) {
        console.error("Error deleting photo:", err);
        alert("Failed to delete photo.");
      }
    }
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

.position-relative {
  position: relative;
}

.position-absolute {
  position: absolute;
}

.top-0 { top: 0; }
.end-0 { right: 0; }

.border { border: 1px solid #ccc; }
.rounded { border-radius: 4px; }
.me-2 { margin-right: 0.5rem; }
.mb-2 { margin-bottom: 0.5rem; }

.btn-sm { font-size: 0.7rem; padding: 0.2rem 0.4rem; }
</style>
