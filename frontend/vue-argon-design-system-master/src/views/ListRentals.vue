<template>
  <section class="list-rentals section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">📖 Rentals List</h2>

      <div v-if="loading" class="text-center text-white">Loading rentals...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load rentals.</div>
      <div v-else-if="rentals.length === 0" class="text-center text-white">No rentals found.</div>

      <ul v-else class="rental-list">
        <li v-for="rental in rentals" :key="rental.id" class="rental-card">
          <div class="rental-info">
            <h3>Rental ID: {{ rental.id }}</h3>
            <p><strong>Property ID:</strong> {{ rental.propertyId }}</p>
            <p><strong>Renter ID:</strong> {{ rental.userId }}</p>
            <p><strong>Check-In Date:</strong> {{ formatDate(rental.startDate) }}</p>
            <p><strong>Check-Out Date:</strong> {{ formatDate(rental.endDate) }}</p>
            <p><strong>Approval Status (by You):</strong>
              <span :class="{
                approved: rental.approvalStatus === 'APPROVED',
                pending: rental.approvalStatus === 'PENDING',
                rejected: rental.approvalStatus === 'REJECTED'
              }">
                {{ rental.approvalStatus }}
              </span>
            </p>
          </div>

          <!-- Action buttons only for PENDING -->
          <div class="rental-actions mt-3" v-if="rental.approvalStatus === 'PENDING'">
            <button class="btn btn-success btn-sm mr-2"
                    @click="approveRental(rental.id)">
              ✅ Approve
            </button>
            <button class="btn btn-danger btn-sm"
                    @click="rejectRental(rental.id)">
              ❌ Reject
            </button>
          </div>
        </li>
      </ul>

<!--      <div class="text-center mt-4">-->
<!--        <button class="btn btn-light" @click="$router.back()">← Back</button>-->
<!--      </div>-->
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ListRentals',
  data() {
    return {
      rentals: [],
      loading: false,
      error: false,
      baseURL: 'http://localhost:8080'
    };
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(dateStr).toLocaleDateString(undefined, options);
    },

    async fetchRentals() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`${this.baseURL}/api/rentals/owner`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        // ✅ Show ALL rentals
        this.rentals = response.data;
      } catch (err) {
        console.error('Error fetching rentals:', err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    async approveRental(rentalId) {
      try {
        const token = localStorage.getItem('token');
        await axios.post(`${this.baseURL}/api/rentals/${rentalId}/approve`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert(`Rental ${rentalId} approved ✅`);
        await this.fetchRentals();
      } catch (err) {
        console.error(`Error approving rental ${rentalId}:`, err);
        alert(`Failed to approve rental ${rentalId}`);
      }
    },

    async rejectRental(rentalId) {
      try {
        const token = localStorage.getItem('token');
        await axios.post(`${this.baseURL}/api/rentals/${rentalId}/reject`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert(`Rental ${rentalId} rejected ❌`);
        await this.fetchRentals();
      } catch (err) {
        console.error(`Error rejecting rental ${rentalId}:`, err);
        alert(`Failed to reject rental ${rentalId}`);
      }
    }
  },

  async mounted() {
    await this.fetchRentals();
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

.rental-actions {
  display: flex;
  justify-content: flex-start;
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

.btn {
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 0.3rem;
  font-size: 1rem;
}

.btn-light {
  background-color: #f8f9fa;
  border: 1px solid #ced4da;
  color: #212529;
  transition: background-color 0.2s ease;
}

.btn-light:hover {
  background-color: #e2e6ea;
}

.btn-success {
  background-color: #28a745;
  border: none;
  color: white;
}

.btn-danger {
  background-color: #dc3545;
  border: none;
  color: white;
}
</style>
