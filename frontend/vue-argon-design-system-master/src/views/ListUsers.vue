<template>
  <section class="section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">Users List</h2>

      <div v-if="loading" class="text-center text-white">Loading users...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load users.</div>
      <div v-else-if="users.length === 0" class="text-center text-white">No users found.</div>
      <div v-else>
        <ul class="user-list">
          <li v-for="user in users" :key="user.id" class="user-card">
            <div><strong>{{ user.name }}</strong></div>
            <span class="badge">{{ user.id }}</span>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UserList',
  data() {
    return {
      users: [],
      loading: false,
      error: false
    };
  },
  async mounted() {
    this.loading = true;
    try {
      const response = await axios.get('http://localhost:8080/api/users');
      this.users = response.data;
    } catch (err) {
      console.error('Error fetching users:', err);
      this.error = true;
    } finally {
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.user-list {
  padding: 0;
  list-style: none;
}

.user-card {
  background: white;
  padding: 16px 24px;
  margin-bottom: 15px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
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
