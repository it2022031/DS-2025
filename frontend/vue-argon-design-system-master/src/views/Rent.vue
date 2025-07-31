<template>
  <section class="section bg-light py-5">
    <div class="container">
      <h2 class="mb-5 text-center">📝 Rent Management</h2>

      <form @submit.prevent="createRent" class="mb-5">
        <div class="row g-4">
          <div class="col-md-6">
            <label class="form-label">Select Property</label>
            <v-select
                :options="properties"
                label="name"
                :reduce="property => property.id"
                v-model="form.propertyId"
                placeholder="-- Choose Property --"
                clearable
            />
          </div>

          <div class="col-md-6 position-relative">
            <label class="form-label">Άτομα</label>
            <input
                type="text"
                readonly
                class="form-control"
                :value="totalPeopleDisplay"
                @click="togglePeopleDropdown"
            />
            <div v-if="showPeopleDropdown" class="people-dropdown p-3 border bg-white rounded shadow">
              <div class="mb-3">
                <label>Ενήλικες</label>
                <div class="input-group">
                  <button type="button" class="btn btn-outline-secondary" @click="decrement('adults')">-</button>
                  <input
                      type="number"
                      min="0"
                      class="form-control text-center"
                      v-model.number="form.adults"
                      @input="validateNumber('adults')"
                  />
                  <button type="button" class="btn btn-outline-secondary" @click="increment('adults')">+</button>
                </div>
              </div>
              <div>
                <label>Παιδιά</label>
                <div class="input-group">
                  <button type="button" class="btn btn-outline-secondary" @click="decrement('children')">-</button>
                  <input
                      type="number"
                      min="0"
                      class="form-control text-center"
                      v-model.number="form.children"
                      @input="validateNumber('children')"
                  />
                  <button type="button" class="btn btn-outline-secondary" @click="increment('children')">+</button>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Start Date</label>
            <input
                type="date"
                v-model="form.startDate"
                class="form-control"
                :min="today"
                required
                @change="onStartDateChange"
            />
          </div>
          <div class="col-md-6">
            <label class="form-label">End Date</label>
            <input
                type="date"
                v-model="form.endDate"
                class="form-control"
                :min="minEndDate"
                required
                @change="onEndDateChange"
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
            <small>{{ formatDate(rent.startDate) }} ➜ {{ formatDate(rent.endDate) }}</small>
          </div>
          <div class="d-flex align-items-center">
            <span class="badge bg-primary me-3">{{ rent.id }}</span>
            <span v-if="checkConflict(rent)" class="badge bg-danger">Conflict!</span>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from "axios";
import vSelect from "vue-select";
import "vue-select/dist/vue-select.css";

export default {
  name: "RentManagement",
  components: { vSelect },
  data() {
    return {
      today: new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().split("T")[0],
      form: {
        propertyId: "",
        startDate: "",
        endDate: "",
        adults: 1,
        children: 0,
      },
      showPeopleDropdown: false,
      rents: [],
      properties: [],
      users: [],
      loadingRents: false,
    };
  },
  computed: {
    totalPeopleDisplay() {
      const total = this.form.adults + this.form.children;
      let text = `${total} άτομα`;
      if (this.form.adults > 0) text += ` (${this.form.adults} ενήλικες`;
      if (this.form.children > 0) text += `, ${this.form.children} παιδιά`;
      if (this.form.adults > 0) text += ")";
      return text;
    },
    // Ελάχιστη επιτρεπτή ημερομηνία για το End Date
    minEndDate() {
      return this.form.startDate || this.today;
    },
  },
  methods: {
    togglePeopleDropdown() {
      this.showPeopleDropdown = !this.showPeopleDropdown;
    },
    increment(type) {
      this.form[type]++;
    },
    decrement(type) {
      if (this.form[type] > 0) this.form[type]--;
    },
    validateNumber(type) {
      if (this.form[type] < 0) this.form[type] = 0;
    },
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
    checkConflict(existingRent) {
      if (
          this.form.propertyId === "" ||
          this.form.startDate === "" ||
          this.form.endDate === ""
      ) return false;

      return (
          existingRent.propertyId === this.form.propertyId &&
          (
              (this.form.startDate >= existingRent.startDate && this.form.startDate <= existingRent.endDate) ||
              (this.form.endDate >= existingRent.startDate && this.form.endDate <= existingRent.endDate) ||
              (this.form.startDate <= existingRent.startDate && this.form.endDate >= existingRent.endDate)
          )
      );
    },
    async createRent() {
      const userRole = localStorage.getItem("userRole");
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      if (!userRole || !token) {
        alert("Πρέπει να είστε συνδεδεμένοι για να κάνετε ενοικίαση.");
        setTimeout(() => {
          this.$router.push("/login");
        }, 1500);
        return;
      }

      if (!["user", "owner", "admin"].includes(userRole)) {
        alert("Δεν έχετε δικαίωμα να κάνετε ενοικίαση.");
        return;
      }

      if (!this.form.startDate || !this.form.endDate) {
        alert("Παρακαλώ συμπληρώστε και τις δύο ημερομηνίες.");
        return;
      }

      if (this.form.endDate < this.form.startDate) {
        alert("Η ημερομηνία λήξης δεν μπορεί να είναι πριν από την ημερομηνία έναρξης.");
        return;
      }

      const conflict = this.rents.some(rent => {
        return rent.propertyId === this.form.propertyId &&
            (
                (this.form.startDate >= rent.startDate && this.form.startDate <= rent.endDate) ||
                (this.form.endDate >= rent.startDate && this.form.endDate <= rent.endDate) ||
                (this.form.startDate <= rent.startDate && this.form.endDate >= rent.endDate)
            );
      });

      if (conflict) {
        alert("Το ακίνητο δεν είναι διαθέσιμο για τις επιλεγμένες ημερομηνίες.");
        return;
      }

      try {
        const newRent = {
          property: { id: this.form.propertyId },
          user: { id: userId },
          startDate: this.form.startDate,
          endDate: this.form.endDate,
          adults: this.form.adults,
          children: this.form.children,
        };

        const res = await axios.post("http://localhost:8080/api/rentals/add", newRent, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        this.rents.push(res.data);
        this.resetForm();
        alert("Η ενοικίαση καταχωρήθηκε επιτυχώς!");
      } catch (error) {
        console.error("❌ Error creating rent:", error);
        alert("Αποτυχία κατά την καταχώρηση ενοικίασης. Προσπαθήστε ξανά.");
      }
    },

    resetForm() {
      this.form = {
        propertyId: "",
        startDate: "",
        endDate: "",
        adults: 1,
        children: 0,
      };
      this.showPeopleDropdown = false;
    },

    formatUserName(user) {
      if (user.firstName && user.lastName) return `${user.firstName} ${user.lastName}`;
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
    },

    onStartDateChange() {
      if (this.form.endDate && this.form.endDate < this.form.startDate) {
        this.form.endDate = "";
      }
    },
    onEndDateChange() {
      if (this.form.endDate && this.form.startDate && this.form.endDate < this.form.startDate) {
        this.form.endDate = "";
      }
    },
  },
  mounted() {
    this.fetchProperties();
    this.fetchUsers();
    this.fetchRents();

    document.addEventListener("click", e => {
      const peopleDropdown = this.$el.querySelector(".people-dropdown");
      const input = this.$el.querySelector("input.form-control[readonly]");
      if (
          this.showPeopleDropdown &&
          peopleDropdown &&
          !peopleDropdown.contains(e.target) &&
          e.target !== input
      ) {
        this.showPeopleDropdown = false;
      }
    });
  },
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

/* Στυλ για το custom dropdown ατόμων */
.people-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  width: 100%;
  max-width: 300px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15);
}

.input-group button {
  min-width: 2.5rem;
}
</style>
