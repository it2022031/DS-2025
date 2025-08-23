<!--<template>-->
<!--  <section class="list-rentals section bg-secondary py-5">-->
<!--    <div class="container">-->
<!--      <h2 class="text-center text-white mb-4">📖 Rentals List</h2>-->

<!--      <div v-if="loading" class="text-center text-white">Loading rentals...</div>-->
<!--      <div v-else-if="error" class="text-center text-danger">Failed to load rentals.</div>-->
<!--      <div v-else-if="rentals.length === 0" class="text-center text-white">No rentals found.</div>-->

<!--      <ul v-else class="rental-list">-->
<!--        <li v-for="rental in rentals" :key="rental.id" class="rental-card">-->
<!--          <div class="rental-info">-->
<!--            <h3>Booking ID: {{ rental.id }}</h3>-->
<!--&lt;!&ndash;            <p><strong>Property's Name:</strong> {{  }}</p>&ndash;&gt;-->
<!--&lt;!&ndash;            <p><strong>Owner's Name:</strong> {{ }}</p>&ndash;&gt;-->
<!--            <p><strong>Start Date:</strong> {{ formatDate(rental.startDate) }}</p>-->
<!--            <p><strong>End Date:</strong> {{ formatDate(rental.endDate) }}</p>-->
<!--            <p><strong>Approval Status (by Owner):</strong>-->
<!--              <span :class="{ approved: rental.status, pending: !rental.status }">-->
<!--                {{ rental.status ? 'Approved' : 'Pending' }}-->
<!--              </span>-->
<!--            </p>-->
<!--          </div>-->

<!--        </li>-->
<!--      </ul>-->

<!--&lt;!&ndash;      <div class="text-center mt-4">&ndash;&gt;-->
<!--&lt;!&ndash;        <button class="btn btn-light" @click="$router.back()">← Back</button>&ndash;&gt;-->
<!--&lt;!&ndash;      </div>&ndash;&gt;-->
<!--    </div>-->
<!--  </section>-->
<!--</template>-->


<!--<script>-->
<!--import axios from 'axios';-->

<!--export default {-->
<!--  name: 'ListRentals',-->
<!--  data() {-->
<!--    return {-->
<!--      rentals: [],-->
<!--      loading: false,-->
<!--      error: false,-->
<!--      baseURL: 'http://localhost:8080',-->
<!--      userId: localStorage.getItem('userId') // ✅ read directly here-->
<!--    };-->
<!--  },-->
<!--  methods: {-->
<!--    formatDate(dateStr) {-->
<!--      if (!dateStr) return 'N/A';-->
<!--      const options = { year: 'numeric', month: 'short', day: 'numeric' };-->
<!--      return new Date(dateStr).toLocaleDateString(undefined, options);-->
<!--    },-->
<!--    async fetchRentals() {-->
<!--      this.loading = true;-->
<!--      this.error = false;-->
<!--      try {-->
<!--        const token = localStorage.getItem('token');-->
<!--        const response = await axios.get(-->
<!--            `${this.baseURL}/api/users/${this.userId}/rentals`,-->
<!--            { headers: { Authorization: `Bearer ${token}` } }-->
<!--        );-->
<!--        this.rentals = response.data;-->
<!--      } catch (err) {-->
<!--        console.error('Error fetching rentals:', err);-->
<!--        this.error = true;-->
<!--      } finally {-->
<!--        this.loading = false;-->
<!--      }-->
<!--    }-->
<!--  },-->
<!--  async mounted() {-->
<!--    await this.fetchRentals();-->
<!--  }-->
<!--};-->
<!--</script>-->


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
            <h3>Booking ID: {{ rental.rentalId }}</h3>
            <p><strong>Property's Name:</strong> {{ rental.propertyName}}</p>
            <p><strong>Owner's Name:</strong> {{ rental.ownerName}}</p>
            <p><strong>Check-In Date:</strong> {{ formatDate(rental.startDate) }}</p>
            <p><strong>Check-Out Date:</strong> {{ formatDate(rental.endDate) }}</p>
            <p><strong>Total Price:</strong> {{ rental.totalPrice}}<strong> €</strong></p>
            <p>
              <strong>Approval Status (by Owner):</strong>
              <span :class="statusClass(rental.status)">
    {{ rental.status }}
  </span>
            </p>

          </div>
        </li>
      </ul>
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
      baseURL: 'http://localhost:8080',
      userId: localStorage.getItem('userId')
    };
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(dateStr).toLocaleDateString(undefined, options);
    },
    statusClass(status) {
      switch(status.toLowerCase()) {
        case 'approved': return 'approved';
        case 'pending': return 'pending';
        case 'rejected': return 'rejected';
        default: return '';
      }
    },
    async fetchRentals() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem('token');

        const response = await axios.get(
            `${this.baseURL}/api/rentals/by-renter/${this.userId}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        this.rentals = response.data; // assume each item has propertyName and ownerName
      } catch (err) {
        console.error('Error fetching rentals:', err);
        this.error = true;
      } finally {
        this.loading = false;
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
