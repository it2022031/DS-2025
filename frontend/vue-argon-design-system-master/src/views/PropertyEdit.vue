<template>
  <section class="edit-property section bg-light py-5">
    <div class="container">
      <h2 class="mb-4">Edit Property #{{ property && property.id }}</h2>

      <div v-if="loading">Loading...</div>
      <div v-else-if="error" class="text-danger">Failed to load property.</div>
      <div v-else-if="property">
        <!-- Προβολή φόρμας μόνο αν υπάρχει property -->
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
              <label class="form-label">Status</label>
              <select v-model="property.approvalStatus" class="form-select">
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
              </select>
            </div>
          </div>

          <div class="mt-4 d-flex justify-content-end">
            <router-link to="/list-properties" class="btn btn-link me-3">Cancel</router-link>
            <button type="submit" class="btn btn-primary">Save Changes</button>
          </div>
        </form>
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
      property: null,
      loading: false,
      error: false,
    };
  },
  async mounted() {
    this.loading = true;
    const id = this.$route.params.id;
    const token = localStorage.getItem('token');  // Παίρνουμε το token
    try {
      const response = await axios.get(`http://localhost:8080/api/properties/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`  // Προσθέτουμε το header
        }
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
      if (!this.property || !this.property.id) return;
      const token = localStorage.getItem('token');  // Παίρνουμε το token ξανά
      try {
        await axios.patch(
            `http://localhost:8080/api/properties/${this.property.id}`,
            this.property,
            {
              headers: {
                Authorization: `Bearer ${token}`
              }
            }
        );
        this.$router.push('/list-properties');
      } catch (err) {
        console.error('Error saving property:', err);
        alert('Failed to save changes.');
      }
    }
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}
</style>
