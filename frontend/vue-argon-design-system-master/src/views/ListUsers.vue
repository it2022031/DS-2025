<template>
  <section class="section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">Users List</h2>
      <div v-if="loading" class="text-center text-white">Loading users...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load users.</div>
      <div v-else>
        <ul class="list-group">
          <li v-for="user in users" :key="user.id" class="list-group-item d-flex justify-content-between align-items-center">
            {{ user.name }}
            <span class="badge badge-primary badge-pill">{{ user.id }}</span>
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
.list-group-item {
  border-radius: 8px;
  margin-bottom: 10px;
}
</style>
