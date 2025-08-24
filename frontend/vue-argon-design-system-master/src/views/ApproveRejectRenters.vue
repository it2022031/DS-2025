<template>
  <section class="list-users section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center mb-4" style="color: #343a40;">👥 Renter Requests</h2>

      <div v-if="loading" class="text-center text-white">Loading renter requests...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load requests.</div>
      <div v-else-if="users.length === 0" class="text-center text-white">No pending requests found.</div>

      <ul v-else class="user-list">
        <li v-for="user in users" :key="user.id" class="user-card">
          <div class="user-info">
            <h3>{{ user.username }}</h3>
            <p><strong>Email: </strong> {{ user.email }}</p>
            <p><strong>Status: </strong> <span style="color: #ffc107;">{{ user.status }}</span></p>

            <div class="user-actions mt-3 d-flex align-items-center"
                 v-if="canModerate(user)">
              <button
                  @click="approveUser(user.id)"
                  class="btn btn-sm btn-success mr-2"
              >
                ✅ Approve
              </button>

              <button
                  @click="rejectUser(user.id)"
                  class="btn btn-sm btn-outline-danger"
              >
                ❌ Reject
              </button>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: "ListRenterRequests",
  data() {
    return {
      users: [],
      loading: false,
      error: false,
    };
  },
  computed: {
    userRole() {
      return (localStorage.getItem('userRole') || '').toUpperCase();
    },
  },
  methods: {
    async fetchUsers() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `http://localhost:8080/api/users/renter-requests`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
        );
        this.users = response.data;
      } catch (err) {
        console.error("Error fetching users:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    async approveUser(userId) {
      if (!confirm("Are you sure you want to approve this renter?")) return;

      try {
        const token = localStorage.getItem("token");
        await axios.post(
            `http://localhost:8080/api/users/${userId}/approve-renter`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );

        await this.fetchUsers();

        alert(`User ${userId} approved as renter ✅`);
      } catch (err) {
        console.error(`Error approving user ${userId}:`, err);
        alert(`Failed to approve user ${userId}`);
      }
    },

    async rejectUser(userId) {
      if (!confirm("Are you sure you want to reject this renter?")) return;

      try {
        const token = localStorage.getItem("token");
        await axios.post(
            `http://localhost:8080/api/users/${userId}/reject-renter`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        );

        await this.fetchUsers();

        alert(`User ${userId} rejected ❌`);
      } catch (err) {
        console.error(`Error rejecting user ${userId}:`, err);
        alert(`Failed to reject user ${userId}`);
      }
    },

    canModerate(user) {
      return this.userRole === "ADMIN";
    }
  },

  mounted() {
    this.fetchUsers();
  },
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.user-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 600px;
}

.user-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.user-info h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #2c3e50;
}

.user-info p {
  margin: 5px 0;
  color: #555;
  font-size: 16px;
}

.user-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  margin-top: 12px;
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
</style>
