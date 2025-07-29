<template>
  <section class="section bg-light py-5">
    <div class="container">
      <h2 class="mb-5 text-center">📝 Rent Management</h2>

      <!-- Φόρμα δημιουργίας ενοικίασης -->
      <form @submit.prevent="createRent" class="mb-5">
        <div class="row g-4">
          <div class="col-md-6">
            <label class="form-label">Select Property</label>
            <select v-model="form.propertyId" class="form-select form-select-lg" required>
              <option disabled value="">-- Choose Property --</option>
              <option v-for="p in properties" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Select User</label>
            <select v-model="form.userId" class="form-select form-select-lg" required>
              <option disabled value="">-- Choose User --</option>
              <option v-for="u in users" :key="u.id" :value="u.id">
                {{ formatUserName(u) }}
              </option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">Start Date</label>
            <input
                type="date"
                v-model="form.startDate"
                class="form-control"
                :min="today"
                required
            />
          </div>
          <div class="col-md-6">
            <label class="form-label">End Date</label>
            <input
                type="date"
                v-model="form.endDate"
                class="form-control"
                :min="form.startDate || today"
                required
            />
          </div>
        </div>
        <div class="mt-4 text-center">
          <button type="submit" class="btn btn-success px-5 py-2">Rent</button>
        </div>
      </form>

      <!-- Λίστα Ενοικιάσεων -->
      <h4 class="mb-3">📋 Existing Rents</h4>
      <div v-if="loadingRents">Loading rents...</div>
      <div v-else-if="rents.length === 0" class="text-muted">No rents found.</div>
      <ul class="list-group">
        <li
            v-for="rent in rents"
            :key="rent.id"
            class="list-group-item d-flex justify-content-between align-items-center"
        >
          <div>
            <strong>{{ getUserName(rent.userId) }}</strong> rented
            <strong>{{ getPropertyName(rent.propertyId) }}</strong><br />
            <small>{{ rent.startDate }} ➜ {{ rent.endDate }}</small>
          </div>
          <span class="badge bg-primary">{{ rent.id }}</span>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "RentManagement",
  data() {
    return {
      today: new Date().toISOString().split("T")[0],
      form: {
        propertyId: "",
        userId: "",
        startDate: "",
        endDate: ""
      },
      rents: [],
      properties: [],
      users: [],
      loadingRents: false
    };
  },
  methods: {
    async fetchProperties() {
      try {
        const res = await axios.get("http://localhost:8080/api/properties/all");
        this.properties = res.data;
      } catch (error) {
        console.error("❌ Error fetching properties:", error);
      }
    },
    async fetchUsers() {
      try {
        const res = await axios.get("http://localhost:8080/api/users");
        this.users = res.data;
      } catch (error) {
        console.error("❌ Error fetching users:", error);
      }
    },
    async fetchRents() {
      this.loadingRents = true;
      try {
        const res = await axios.get("http://localhost:8080/api/rentals/all");
        this.rents = res.data;
      } catch (error) {
        console.error("❌ Error fetching rents:", error);
      } finally {
        this.loadingRents = false;
      }
    },
    async createRent() {
      try {
        const newRent = {
          property: { id: this.form.propertyId },
          user: { id: this.form.userId },
          startDate: this.form.startDate,
          endDate: this.form.endDate
        };

        const res = await axios.post("http://localhost:8080/api/rentals/add", newRent);
        this.rents.push(res.data);
        this.resetForm();
      } catch (error) {
        console.error("❌ Error creating rent:", error);
        alert("Failed to create rent. Please try again.");
      }
    },
    resetForm() {
      this.form = {
        propertyId: "",
        userId: "",
        startDate: "",
        endDate: ""
      };
    },
    formatUserName(user) {
      if (user.firstName && user.lastName)
        return `${user.firstName} ${user.lastName}`;
      if (user.username) return user.username;
      if (user.name) return user.name;
      return "Unknown User";
    },
    getUserName(userId) {
      const user = this.users.find(u => u.id === userId);
      return user ? this.formatUserName(user) : "Unknown User";
    },
    getPropertyName(propertyId) {
      const prop = this.properties.find(p => p.id === propertyId);
      return prop ? prop.name : "Unknown Property";
    }
  },
  mounted() {
    this.fetchProperties();
    this.fetchUsers();
    this.fetchRents();
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.list-group-item {
  border-radius: 8px;
  margin-bottom: 10px;
}

form select,
form input {
  border-radius: 8px;
}
</style>
