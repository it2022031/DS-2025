<template>
  <section class="list-rentals section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center mb-4" style="color: #343a40;">📖 Rentals List</h2>

      <!-- 🔍 Search + Filter -->
      <div class="filters mb-4 d-flex justify-content-center gap-3 flex-wrap">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Search by property name..."
            class="form-control search-input"
        />

        <select v-model="selectedStatus" class="form-control filter-select">
          <option value="ALL">All</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>

        <!-- 🚀 Delete All Rejected Button (Admin only) -->
        <button
            @click="deleteAllRejected"
            class="btn btn-danger"
            :disabled="!hasRejectedRentals"
        >
          🗑️ Delete All Rejected
        </button>
      </div>

      <div v-if="loading" class="text-center text-white">Loading rentals...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load rentals.</div>
      <div v-else-if="filteredRentals.length === 0" class="text-center text-white">No rentals found.</div>

      <ul v-else class="rental-list">
        <li v-for="rental in filteredRentals" :key="rental.id" class="rental-card">
          <div class="rental-info">
            <h3>Rental ID: {{ rental.id }}</h3>
            <p><strong>Property's Name:</strong> {{ rental.propertyName }}</p>
            <p><strong>Renter's Name:</strong> {{ rental.renterFirstName }} {{ rental.renterLastName }}</p>
            <p><strong>Check-In Date:</strong> {{ formatDate(rental.startDate) }}</p>
            <p><strong>Check-Out Date:</strong> {{ formatDate(rental.endDate) }}</p>
            <p><strong>Total Price:</strong> {{ rental.TotalPrice }} €</p>
            <p>
              <strong>Approval Status (by Owner):</strong>
              <span :class="{
                approved: rental.approvalStatus === 'APPROVED',
                pending: rental.approvalStatus === 'PENDING',
                rejected: rental.approvalStatus === 'REJECTED'
              }">
                {{ rental.approvalStatus }}
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
      searchQuery: "",
      selectedStatus: "ALL",
    };
  },
  computed: {
    filteredRentals() {
      return this.rentals.filter(r => {
        const matchesSearch =
            r.propertyName.toLowerCase().includes(this.searchQuery.toLowerCase());
        const matchesStatus =
            this.selectedStatus === "ALL" || r.approvalStatus === this.selectedStatus;
        return matchesSearch && matchesStatus;
      });
    },
    hasRejectedRentals() {
      return this.rentals.some(p => p.approvalStatus === "REJECTED");
    }
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
        const response = await axios.get(`${this.baseURL}/api/rentals/all`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.rentals = response.data; // already has correct fields
      } catch (err) {
        console.error('Error fetching rentals:', err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
    async deleteAllRejected() {
      if (!confirm("Are you sure you want to delete ALL rejected rentals?")) {
        return;
      }
      const token = localStorage.getItem("token");
      try {
        await axios.delete(
            `http://localhost:8080/api/rentals/status/rejected`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        await this.fetchRentals(); // refresh the list
        alert("All rejected rentals deleted 🗑️");
      } catch (err) {
        console.error("Error deleting rejected rentals:", err);
        alert("Failed to delete rejected rentals ❌");
      }
    },
  },
  mounted() {
    this.fetchRentals();
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

.filters {
  max-width: 900px;
  margin: 0 auto 20px auto;
}

.search-input {
  width: 250px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.filter-select {
  width: 180px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
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

.btn-danger {
  background-color: #dc3545;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: bold;
  transition: background 0.2s ease;
}

.btn-danger:hover {
  background-color: #b02a37;
}

.btn-danger:disabled {
  background-color: #e6aeb3;
  cursor: not-allowed;
  opacity: 0.7;
}

</style>
