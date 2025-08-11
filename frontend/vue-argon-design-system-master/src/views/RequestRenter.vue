<template>
  <section class="section bg-light py-5">
    <div class="container text-center">
      <h2>Request Renter Role</h2>
      <p>If you want to rent properties, you need to request the Renter role.</p>
      <div v-if="error" class="text-danger mb-3">{{ error }}</div>
      <button class="btn btn-primary" @click="sendRequest">Send Request</button>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "RequestRenter",
  data() {
    return {
      error: ""
    };
  },
  methods: {
    async sendRequest() {
      this.error = "";
      try {
        const token = localStorage.getItem("token");
        await axios.post(
            "http://localhost:8080/api/users/become-renter",
            {},
            {
              headers: { Authorization: `Bearer ${token}` }
            }
        );
        alert("Your request to become a renter has been sent!");
      } catch (_t) {
        console.error(_t);
        if (_t.response && _t.response.data && _t.response.data.message) {
          this.error = _t.response.data.message;
        } else {
          this.error = "Failed to send request.";
        }
      }
    }
  }
};
</script>

<style scoped>
section {
  min-height: 60vh;
}
</style>
