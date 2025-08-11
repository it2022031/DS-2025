<template>
  <section class="section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">Users List</h2>

      <!-- Search box -->
      <div class="mb-4">
        <input
            v-model="searchQuery"
            type="text"
            placeholder="🔍 Αναζήτηση με ID, Username ή Ονοματεπώνυμο..."
            class="form-control"
        />
      </div>

      <div v-if="loading" class="text-center text-white">Loading users...</div>
      <div v-else-if="error" class="text-center text-danger">{{ errorMessage }}</div>
      <div v-else-if="filteredUsers.length === 0" class="text-center text-white">No matching users found.</div>

      <div v-else>
        <ul class="user-list">
          <li
              v-for="user in filteredUsers"
              :key="user.id"
              class="user-card clickable"
              @click="toggleUserSelection(user)"
          >
            <div><strong>{{ user.username }}</strong></div>
            <span class="badge">{{ user.id }}</span>
          </li>
        </ul>

        <!-- Edit form -->
        <div v-if="selectedUser" class="user-edit-form bg-dark text-white p-4 rounded shadow mt-4">
          <h4>Προβολή Χρήστη</h4>

          <div class="form-group">
            <label>Όνομα</label>
            <input type="text" v-model="selectedUser.firstName" class="form-control" :disabled="!editMode" />
          </div>

          <div class="form-group">
            <label>Επίθετο</label>
            <input type="text" v-model="selectedUser.lastName" class="form-control" :disabled="!editMode" />
          </div>

          <div class="form-group">
            <label>Email</label>
            <input type="email" v-model="selectedUser.email" class="form-control" :disabled="!editMode" />
          </div>

          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="selectedUser.username" class="form-control" :disabled="!editMode" />
          </div>

          <div class="form-group">
            <label>ΑΦΜ</label>
            <input type="text" v-model="selectedUser.afm" class="form-control" :disabled="!editMode" />
          </div>

          <div class="form-group">
            <label>Αρ. Διαβατηρίου</label>
            <input type="text" v-model="selectedUser.passportNumber" class="form-control" :disabled="!editMode" />
          </div>

          <div v-if="!editMode">
            <button class="btn btn-primary" @click="editMode = true">✏️ Επεξεργασία</button>
            <button class="btn btn-secondary ml-2" @click="selectedUser = null">Κλείσιμο</button>
          </div>
          <div v-else>
            <button class="btn btn-success" @click="saveProfile" :disabled="saving">💾 Αποθήκευση</button>
            <button class="btn btn-secondary ml-2" @click="cancelEdit">Ακύρωση</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import api from "@/api";

export default {
  name: "UserList",
  data() {
    return {
      users: [],
      searchQuery: "",
      loading: false,
      error: false,
      errorMessage: "",
      selectedUser: null,
      originalUser: null,
      editMode: false,
      saving: false
    };
  },
  computed: {
    roles() {
      try {
        var raw = localStorage.getItem("userRoles") || "[]";
        var parsed = JSON.parse(raw);
        return parsed.map(function (r) {
          var value = (typeof r === "string") ? r : (r && r.role) ? r.role : "";
          return value.toString().toUpperCase();
        });
      } catch (e) {
        return [];
      }
    },
    isAdmin() {
      return this.roles.indexOf("ADMIN") !== -1;
    },
    filteredUsers() {
      if (!this.searchQuery.trim()) {
        return this.users;
      }
      const q = this.searchQuery.trim().toLowerCase();
      return this.users.filter(u => {
        const fullName = ((u.firstName || "") + " " + (u.lastName || "")).toLowerCase();
        return (
            String(u.id).includes(q) ||
            (u.username && u.username.toLowerCase().includes(q)) ||
            fullName.includes(q)
        );
      });
    }
  },
  methods: {
    async fetchUsers() {
      this.loading = true;
      this.error = false;
      this.errorMessage = "";
      try {
        const res = await api.get("/users");
        this.users = res.data;
      } catch (err) {
        console.error("Error fetching users:", err);
        this.error = true;
        const status = (err && err.response && err.response.status) ? err.response.status : 0;
        if (status === 401) {
          this.errorMessage = "Δεν είστε συνδεδεμένοι.";
          this.$router.push("/login");
        } else if (status === 403) {
          this.errorMessage = "Δεν έχετε δικαίωμα πρόσβασης (ADMIN only).";
        } else {
          this.errorMessage = "Failed to load users.";
        }
      } finally {
        this.loading = false;
      }
    },
    toggleUserSelection(user) {
      if (this.selectedUser && this.selectedUser.id === user.id) {
        this.selectedUser = null;
        this.editMode = false;
      } else {
        this.selectedUser = { ...user };
        this.originalUser = { ...user };
        this.editMode = false;
      }
    },
    cancelEdit() {
      this.selectedUser = { ...this.originalUser };
      this.editMode = false;
    },
    async saveProfile() {
      const updates = {
        firstName: this.selectedUser.firstName,
        lastName: this.selectedUser.lastName,
        username: this.selectedUser.username,
        email: this.selectedUser.email,
        passportNumber: this.selectedUser.passportNumber,
        afm: this.selectedUser.afm
      };
      this.saving = true;
      try {
        await api.patch(`/users/${this.selectedUser.id}`, updates);
        await this.fetchUsers();
        this.editMode = false;
        this.selectedUser = null;
      } catch (err) {
        console.error("Error updating profile:", err);
        alert("Σφάλμα κατά την ενημέρωση.");
      } finally {
        this.saving = false;
      }
    }
  },
  mounted() {
    if (!this.isAdmin) {
      this.$router.push("/");
      return;
    }
    this.fetchUsers();
  }
};
</script>

<style scoped>
.section { min-height: 100vh; }
.user-list { padding: 0; list-style: none; }
.user-card {
  background-color: #1e1e2f; color: white; padding: 16px 24px; margin-bottom: 15px;
  border-radius: 12px; display: flex; justify-content: space-between; align-items: center;
  transition: box-shadow 0.2s ease, transform 0.2s ease; box-shadow: 0 2px 6px rgba(0,0,0,.2);
}
.user-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.15); transform: translateY(-2px); }
.badge { background-color: #007bff; color: white; padding: 6px 12px; border-radius: 20px; font-size: 14px; }
.user-edit-form .form-group label { font-weight: bold; color: #ccc; }
.user-edit-form input { margin-bottom: 10px; }
.clickable { cursor: pointer; }
.form-control { border-radius: 8px; }
@media (max-width: 576px) {
  .user-card { flex-direction: column; align-items: flex-start; gap: 10px; }
  .badge { align-self: flex-end; }
}
</style>
