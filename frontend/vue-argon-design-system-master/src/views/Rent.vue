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
              <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }}</option>
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
      <div v-if="rents.length === 0" class="text-muted">No rents found.</div>
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
export default {
  name: "Rent",
  data() {
    return {
      today: new Date().toISOString().split("T")[0], // YYYY-MM-DD
      form: {
        propertyId: "",
        userId: "",
        startDate: "",
        endDate: ""
      },
      rents: [
        {
          id: 1,
          userId: 1,
          propertyId: 2,
          startDate: "2024-09-01",
          endDate: "2024-09-10"
        }
      ],
      properties: [
        { id: 1, name: "Seaside Villa" },
        { id: 2, name: "Mountain Retreat" }
      ],
      users: [
        { id: 1, name: "John Doe" },
        { id: 2, name: "Jane Smith" }
      ]
    };
  },
  methods: {
    createRent() {
      const newRent = {
        id: this.rents.length + 1,
        userId: this.form.userId,
        propertyId: this.form.propertyId,
        startDate: this.form.startDate,
        endDate: this.form.endDate
      };
      this.rents.push(newRent);
      this.resetForm();
    },
    resetForm() {
      this.form = {
        propertyId: "",
        userId: "",
        startDate: "",
        endDate: ""
      };
    },
    getUserName(userId) {
      const user = this.users.find(u => u.id === userId);
      return user ? user.name : "Unknown User";
    },
    getPropertyName(propertyId) {
      const prop = this.properties.find(p => p.id === propertyId);
      return prop ? prop.name : "Unknown Property";
    }
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
