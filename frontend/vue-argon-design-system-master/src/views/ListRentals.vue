<template>
  <section class="list-rentals section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">📅 Rentals List</h2>

      <div v-if="loading" class="text-center text-white">Loading rentals...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load rentals.</div>
      <div v-else-if="rentals.length === 0" class="text-center text-white">No rentals found.</div>
      <ul v-else class="rental-list">
        <li v-for="rental in rentals" :key="rental.id" class="rental-card">
          <div class="rental-info">
            <h3>Rental ID: {{ rental.id }}</h3>
            <p><strong>Property ID:</strong> {{ rental.propertyId }}</p>
            <p><strong>Start Date:</strong> {{ formatDate(rental.startDate) }}</p>
            <p><strong>End Date:</strong> {{ formatDate(rental.endDate) }}</p>
            <p><strong>Payment:</strong> €{{ rental.payment.toFixed(2) }}</p>
            <p><strong>Status:</strong> <span :class="{'approved': rental.status, 'pending': !rental.status}">
              {{ rental.status ? 'Approved' : 'Pending' }}
            </span></p>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: "ListRentals",
  data() {
    return {
      rentals: [],
      loading: false,
      error: false,
    };
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(dateStr).toLocaleDateString(undefined, options);
    }
  },
  async mounted() {
    this.loading = true;
    this.error = false;
    try {
      const response = await axios.get('http://localhost:8080/api/rentals/all');
      this.rentals = response.data;
    } catch (err) {
      console.error('Error fetching rentals:', err);
      this.error = true;
    } finally {
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.rental-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 900px;
}

.rental-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.rental-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.rental-info h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #2c3e50;
}

.rental-info p {
  margin: 5px 0;
  color: #555;
  font-size: 16px;
}

.approved {
  color: #28a745; /* πράσινο */
  font-weight: bold;
}

.pending {
  color: #ffc107; /* πορτοκαλί */
  font-weight: bold;
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
</style>
