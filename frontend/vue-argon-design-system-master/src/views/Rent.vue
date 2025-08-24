<template>
  <section class="section bg-light py-5">
    <div class="container">
      <h2 class="mb-5 text-center">📝 Rent Management</h2>

      <!-- Φόρμα δημιουργίας ενοικίασης -->
      <form @submit.prevent="createRent" class="mb-5">
        <div class="row g-4">
          <div class="col-12">
            <label class="form-label">Select Property</label>
            <v-select
                v-model="form.propertyId"
                :options="filteredProperties"
                :filterable="true"
                label="name"
                :reduce="p => p.id"
                placeholder="-- Choose Property --"
                class="form-select"
                required
            />
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
          <div class="col-md-6">
            <label class="form-label">Min Price</label>
            <input
                type="number"
                v-model.number="form.priceMin"
                class="form-control"
                min="0"
                placeholder="Ελάχιστη Τιμή"
                required
            />
          </div>
          <div class="col-md-6">
            <label class="form-label">Max Price</label>
            <input
                type="number"
                v-model.number="form.priceMax"
                class="form-control"
                :min="form.priceMin"
                placeholder="Μέγιστη Τιμή"
                required
            />
          </div>
        </div>
        <div class="mt-4 text-center">
          <button type="submit" class="btn btn-success px-5 py-2">Rent</button>
        </div>
      </form>

      <!-- Λίστα Ενοικιάσεων μόνο για admin -->
      <div v-if="isAdmin">
        <h4 class="mb-3">📋 Existing Rents</h4>
        <div v-if="loadingRents">Loading rents...</div>
        <div v-else-if="rents.length === 0" class="text-muted">No rents found.</div>
        <ul class="list-group">
          <li
              v-for="rent in rents"
              :key="rent.id"
              class="list-group-item d-flex justify-content-between align-items-start flex-column"
          >
            <div class="w-100 d-flex justify-content-between">
              <div>
                <strong>#{{ rent.id }}</strong>
                <div><strong>Property:</strong> {{ getPropertyName(rent.property && rent.property.id) }}</div>
                <div><strong>User:</strong> {{ getUserName(rent.user && rent.user.id) }}</div>
                <div><strong>Dates:</strong> {{ rent.bookedFrom }} ➜ {{ rent.bookedTo }}</div>
                <div><strong>Price Range:</strong> {{ rent.priceMin }}€ - {{ rent.priceMax }}€</div>
              </div>
            </div>
          </li>
        </ul>
      </div>

      <!-- Λίστα Ενοικιάσεων του τρέχοντος χρήστη -->
      <div v-else>
        <h4 class="mb-3">📋 Your Rents</h4>
        <div v-if="loadingRents">Loading rents...</div>
        <div v-else-if="userRents.length === 0" class="text-muted">You have no rents.</div>
        <ul class="list-group">
          <li
              v-for="rent in userRents"
              :key="rent.id"
              class="list-group-item d-flex justify-content-between align-items-center"
          >
            <div>
              <strong>#{{ rent.id }}</strong> {{ getPropertyName(rent.propertyId) }}<br />
              <small>{{ rent.startDate }} ➜ {{ rent.endDate }}</small><br />
              <small>{{ rent.priceMin }}€ - {{ rent.priceMax }}€</small>
            </div>
            <div>
              <button class="btn btn-sm btn-outline-primary me-2" @click="startEdit(rent)">Edit</button>
              <button class="btn btn-sm btn-outline-danger" @click="cancelRent(rent.id)">Cancel</button>
            </div>
          </li>
        </ul>
      </div>

      <!-- Modal Επεξεργασίας -->
      <div v-if="editingRent" class="modal-backdrop">
        <div class="modal-dialog">
          <div class="modal-content p-4">
            <h5>Edit Rent #{{ editingRent.id }}</h5>
            <form @submit.prevent="updateRent">
              <div class="mb-3">
                <label>Start Date</label>
                <input type="date" v-model="editForm.startDate" class="form-control" :min="today" required />
              </div>
              <div class="mb-3">
                <label>End Date</label>
                <input type="date" v-model="editForm.endDate" class="form-control" :min="editForm.startDate" required />
              </div>
              <div class="mb-3">
                <label>Min Price</label>
                <input
                    type="number"
                    v-model.number="editForm.priceMin"
                    class="form-control"
                    min="0"
                    required
                />
              </div>
              <div class="mb-3">
                <label>Max Price</label>
                <input
                    type="number"
                    v-model.number="editForm.priceMax"
                    class="form-control"
                    :min="editForm.priceMin"
                    required
                />
              </div>
              <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-secondary me-2" @click="editingRent = null">Close</button>
                <button type="submit" class="btn btn-primary">Save</button>
              </div>
            </form>
          </div>
        </div>
      </div>

    </div>
  </section>
</template>

<script>
import axios from "axios";
// import vSelect from "vue-select";
// import "vue-select/dist/vue-select.css";
import vSelect from 'vue-select';
import 'vue-select/dist/vue-select.css';
export default {
  name: "RentManagement",
  components: { vSelect },
  data() {
    return {
      today: new Date().toISOString().split("T")[0],
      form: { propertyId: "", startDate: "", endDate: "", priceMin: 0, priceMax: 0 },
      editForm: { startDate: "", endDate: "", priceMin: 0, priceMax: 0 },
      rents: [], properties: [], users: [], loadingRents: false,
      editingRent: null
    };
  },
  computed: {
    userRole() { return localStorage.getItem('userRole'); },
    isAdmin() { return this.userRole === 'admin'; },
    userId() { return +localStorage.getItem('userId'); },
    userRents() { return this.rents.filter(r => r.userId === this.userId); },
    filteredProperties() { return this.properties; }
  },
  methods: {
    async fetchProperties() {
      const token = localStorage.getItem('token');
      try { const res = await axios.get("http://localhost:8080/api/properties/all", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }); this.properties = res.data; } catch (e) { console.error(e); } },
    async fetchUsers() { try { const res = await axios.get("http://localhost:8080/api/users"); this.users = res.data; } catch (e) { console.error(e); } },
    async fetchRents() {
      this.loadingRents = true;
      try {
        const token = localStorage.getItem('token');
        const res = await axios.get("http://localhost:8080/api/bookings", {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }); this.rents = res.data; } catch (e) { console.error(e); } finally { this.loadingRents = false; }
    },
    normalize(d) { return d.split('T')[0]; },
    hasConflict(start, end, propId, excludeId=null) {
      return this.rents.some(r => r.propertyId===propId && r.id!==excludeId && (
          (start>=this.normalize(r.startDate) && start<=this.normalize(r.endDate)) ||
          (end>=this.normalize(r.startDate) && end<=this.normalize(r.endDate)) ||
          (start<=this.normalize(r.startDate) && end>=this.normalize(r.endDate))
      ));
    },
    async createRent() {
      if (this.hasConflict(this.form.startDate, this.form.endDate, this.form.propertyId)) {
        return alert('Conflict with existing rent!');
      }

      try {
        const newRent = {
          property: { id: this.form.propertyId },
          bookedFrom: this.form.startDate,
          bookedTo: this.form.endDate,
          priceMin: this.form.priceMin,
          priceMax: this.form.priceMax,
          status: true
        };

        const token = localStorage.getItem('token');

        const res = await axios.post("http://localhost:8080/api/rentals", newRent, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.rents.push(res.data);
        this.resetForm();
      } catch (e) {
        console.error(e);
        alert('Failed to create rent.');
      }
    },

    startEdit(rent) {
      this.editingRent = rent;
      this.editForm.startDate = this.normalize(rent.startDate);
      this.editForm.endDate = this.normalize(rent.endDate);
      this.editForm.priceMin = rent.priceMin;
      this.editForm.priceMax = rent.priceMax;
    },
    async updateRent() {
      if (this.hasConflict(this.editForm.startDate,this.editForm.endDate,this.editingRent.propertyId,this.editingRent.id)) { return alert('Conflict on update!'); }
      try {
        const payload = {
          id: this.editingRent.id,
          bookedFrom: this.editForm.startDate,
          bookedTo: this.editForm.endDate,
          priceMin: this.editForm.priceMin,
          priceMax: this.editForm.priceMax,
          status: true,
          property: this.editingRent.property,
          user: this.editingRent.user
        };
        await axios.put(`http://localhost:8080/api/bookings/${payload.id}`, payload);
        Object.assign(this.editingRent, payload);
        this.editingRent = null;
      } catch(e) { console.error(e); alert('Failed to update rent.'); }
    },
    async cancelRent(id) { try { await axios.delete(`http://localhost:8080/api/bookings/${id}`); this.rents = this.rents.filter(r=>r.id!==id); } catch(e){console.error(e); alert('Cancel failed');} },
    getUserName(id){ const u=this.users.find(x=>x.id===id); return u?this.formatUserName(u):'Unknown'; },
    getPropertyName(id){ const p=this.properties.find(x=>x.id===id); return p?p.name:'Unknown'; },
    formatUserName(u){ if(u.firstName&&u.lastName)return`${u.firstName} ${u.lastName}`; if(u.username)return u.username; if(u.name)return u.name; return 'Unknown';},
    resetForm(){ this.form={propertyId:'',startDate:'',endDate:'',priceMin:0,priceMax:0}; }
  },
  mounted() {
    let storedRolesRaw = localStorage.getItem("userRoles");
    let storedRoles = [];

    try {
      storedRoles = storedRolesRaw ? JSON.parse(storedRolesRaw) : [];
    } catch (e) {
      console.error("Failed to parse roles from localStorage:", e);
    }

    const roleUpper = storedRoles
        .map(r => (typeof r === "string" ? r : r.role || "").toString().toUpperCase())
        .filter(Boolean);

    console.log("Roles array:", roleUpper);

    // Αν ακόμα δεν έχει φορτωθεί τίποτα, μην κάνεις redirect αμέσως
    if (roleUpper.length === 0) {
      console.warn("No roles found yet — skipping redirect until roles are set.");
      return;
    }

    if (!roleUpper.includes("RENTER") && !roleUpper.includes("ADMIN")) {
      if (this.$route.path !== "/request-renter") {
        this.$router.push("/request-renter").catch(() => {});
      }
      return;
    }

    console.log("Access granted to Rent page");
    this.fetchProperties();
    this.fetchUsers();
    this.fetchRents();
  }






};
</script>

<style scoped>
.section{min-height:100vh;}
.list-group-item{border-radius:8px;margin-bottom:10px;}
form .form-select, form input, .v-select{border-radius:8px;width:100%;}
.modal-backdrop{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;}
.modal-dialog{background:#fff;border-radius:8px;}
</style>
