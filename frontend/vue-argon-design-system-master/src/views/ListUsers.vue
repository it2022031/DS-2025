<template>
  <section class="section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center mb-4" style="color: #343a40;">Users List</h2>

      <!-- 🔍 Search bar -->
      <div class="search-bar mb-4 text-center">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Search by username..."
            class="form-control search-input"
        />
      </div>

      <div v-if="loading" class="text-center text-white">Loading users...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load users.</div>
      <div v-else-if="filteredUsers.length === 0" class="text-center text-white">No users found.</div>

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

        <!-- Edit form for selected user -->
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

          <div class="form-group">
            <label>Ρόλοι</label>

            <!-- USER always checked -->
            <div class="form-check">
              <input type="checkbox" checked disabled class="form-check-input" id="role-user" />
              <label class="form-check-label" for="role-user">USER</label>
            </div>

            <!-- Other roles -->
            <div class="form-check">
              <input
                  type="checkbox"
                  value="RENTER"
                  v-model="selectedUser.roles"
                  class="form-check-input"
                  id="role-renter"
                  :disabled="!editMode"
              />
              <label class="form-check-label" for="role-renter">RENTER</label>
            </div>

            <div class="form-check">
              <input
                  type="checkbox"
                  value="ADMIN"
                  v-model="selectedUser.roles"
                  class="form-check-input"
                  id="role-admin"
                  :disabled="!editMode"
              />
              <label class="form-check-label" for="role-admin">ADMIN</label>
            </div>
          </div>

          <div v-if="!editMode">
            <button class="btn btn-primary" @click="editMode = true">✏️ Επεξεργασία</button>
            <button class="btn btn-danger ml-2" @click="deleteUser(selectedUser.id)">🗑️ Διαγραφή</button>
            <button class="btn btn-secondary ml-2" @click="selectedUser = null">Κλείσιμο</button>
          </div>

          <div v-else>
            <button class="btn btn-success" @click="saveProfile">💾 Αποθήκευση</button>
            <button class="btn btn-secondary ml-2" @click="cancelEdit">Ακύρωση</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "UserList",
  data() {
    return {
      users: [],
      loading: false,
      error: false,
      selectedUser: null,
      originalUser: null,
      editMode: false,
      saving: false,
      saveSuccess: false,
      searchQuery: ""
    };
  },
  computed: {
    filteredUsers() {
      return this.users.filter(u =>
          u.username && u.username.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    }
  },
  methods: {
    async fetchUsers() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const res = await axios.get("http://localhost:8080/api/users", {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.users = res.data;
      } catch (err) {
        console.error("Error fetching users:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    toggleUserSelection(user) {
      if (this.selectedUser && this.selectedUser.id === user.id) {
        this.selectedUser = null;
        this.editMode = false;
      } else {
        // Deep copy user object
        this.selectedUser = JSON.parse(JSON.stringify(user));

        // Ensure USER is always included
        if (!this.selectedUser.roles.includes("USER")) {
          this.selectedUser.roles.push("USER");
        }

        this.originalUser = JSON.parse(JSON.stringify(this.selectedUser));
        this.editMode = false;
      }
    },

    cancelEdit() {
      this.selectedUser = { ...this.originalUser };
      this.editMode = false;
    },

    async saveProfile() {
      if (!this.selectedUser) return;
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      this.saving = true;
      const userId = this.selectedUser.id;

      const roleUpdates = {
        set: [...new Set(this.selectedUser.roles)] // keep roles as-is: ["USER", "ADMIN", ...]
      };

      const userUpdates = {
        firstName: this.selectedUser.firstName,
        lastName: this.selectedUser.lastName,
        username: this.selectedUser.username,
        email: this.selectedUser.email,
        passportNumber: this.selectedUser.passportNumber,
        afm: this.selectedUser.afm
      };

      try {
        // 1️⃣ Update roles
        await axios.patch(`http://localhost:8080/api/users/${userId}/roles`, roleUpdates, {
          headers: { Authorization: `Bearer ${token}` }
        });

        // 2️⃣ Update user info
        await axios.patch(`http://localhost:8080/api/users/${userId}`, userUpdates, {
          headers: { Authorization: `Bearer ${token}` }
        });

        // Refresh users list
        await this.fetchUsers();

        this.saveSuccess = true;
        alert("Τα στοιχεία του χρήστη ενημερώθηκαν!.");
        setTimeout(() => (this.saveSuccess = false), 3000);
        this.editMode = false;
        this.selectedUser = null;

      } catch (err) {
        console.error("Error updating profile:", err);
        alert("Σφάλμα κατά την ενημέρωση.");
      } finally {
        this.saving = false;
      }
    },

    async deleteUser(userId) {
      if (!confirm("Είσαι σίγουρος ότι θέλεις να διαγράψεις αυτόν τον χρήστη;")) return;

      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      try {
        await axios.delete(`http://localhost:8080/api/users/${userId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        alert("Ο χρήστης διαγράφηκε με επιτυχία.");
        this.selectedUser = null;
        this.fetchUsers();
      } catch (err) {
        console.error("Error deleting user:", err);
        alert("Σφάλμα κατά τη διαγραφή του χρήστη.");
      }
    }
  },

  mounted() {
    this.fetchUsers();
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.search-bar {
  max-width: 400px;
  margin: 0 auto 20px auto;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.user-list {
  padding: 0;
  list-style: none;
}

.user-card {
  background-color: #1e1e2f;
  color: white;
  padding: 16px 24px;
  margin-bottom: 15px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.badge {
  background-color: #007bff;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
}

.user-edit-form .form-group label {
  font-weight: bold;
  color: #ccc;
}

.user-edit-form input {
  margin-bottom: 10px;
}

.clickable {
  cursor: pointer;
}

@media (max-width: 576px) {
  .user-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .badge {
    align-self: flex-end;
  }
}
</style>
