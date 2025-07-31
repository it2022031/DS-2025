<template>
  <section class="section bg-light py-5">
    <div class="container">
      <h2 class="mb-4 text-center">🛡️ Αιτήματα Ρόλων Χρηστών</h2>

      <div v-if="loading">Φόρτωση...</div>
      <div v-else-if="requests.length === 0" class="text-muted">
        Δεν υπάρχουν εκκρεμή αιτήματα.
      </div>

      <ul class="list-group">
        <li
            v-for="request in requests"
            :key="request.id"
            class="list-group-item d-flex justify-content-between align-items-center"
        >
          <div>
            <strong>{{ request.user.name }}</strong> ζητά να γίνει
            <strong>{{ request.requestedRole }}</strong>
          </div>
          <div>
            <button class="btn btn-success btn-sm me-2" @click="approveRequest(request.id)">
              ✔️ Έγκριση
            </button>
            <button class="btn btn-danger btn-sm" @click="rejectRequest(request.id)">
              ❌ Απόρριψη
            </button>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "RoleRequests",
  data() {
    return {
      requests: [],
      loading: false
    };
  },
  methods: {
    async fetchRequests() {
      this.loading = true;
      const token = localStorage.getItem("token");

      try {
        const res = await axios.get("http://localhost:8080/api/role-requests", {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.requests = res.data;
      } catch (error) {
        console.error("❌ Error fetching role requests:", error);
        alert("Σφάλμα κατά τη φόρτωση αιτημάτων.");
      } finally {
        this.loading = false;
      }
    },
    async approveRequest(id) {
      const token = localStorage.getItem("token");

      try {
        await axios.post(`http://localhost:8080/api/role-requests/${id}/approve`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.requests = this.requests.filter(r => r.id !== id);
        alert("✅ Το αίτημα εγκρίθηκε.");
      } catch (error) {
        console.error("❌ Error approving request:", error);
        alert("Αποτυχία στην έγκριση.");
      }
    },
    async rejectRequest(id) {
      const token = localStorage.getItem("token");

      try {
        await axios.post(`http://localhost:8080/api/role-requests/${id}/reject`, {}, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.requests = this.requests.filter(r => r.id !== id);
        alert("❌ Το αίτημα απορρίφθηκε.");
      } catch (error) {
        console.error("❌ Error rejecting request:", error);
        alert("Αποτυχία στην απόρριψη.");
      }
    }
  },
  mounted() {
    this.fetchRequests();
  }
};
</script>

<style scoped>
.list-group-item {
  border-radius: 8px;
  margin-bottom: 10px;
}
</style>
