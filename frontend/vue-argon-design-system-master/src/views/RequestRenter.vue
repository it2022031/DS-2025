<template>
  <section class="section bg-light py-5">
    <div class="container text-center">
      <h2>Request Renter Role</h2>
      <p>If you want to rent properties, you need to request the Renter role.</p>

      <div v-if="error" class="text-danger mb-3">{{ error }}</div>

      <div v-if="renterRequestStatus === 'PENDING'" class="text-warning mb-3">
        Your request to become a renter is pending.
      </div>

      <button
          v-else
          class="btn btn-primary"
          @click="sendRequest"
      >
        Send Request
      </button>
    </div>
  </section>
</template>

<script>
import axios from "axios";

export default {
  name: "RequestRenter",
  data() {
    return {
      error: "",
      renterRequestStatus: null
    };
  },
  async created() {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get("http://localhost:8080/api/auth/me", {
        headers: { Authorization: `Bearer ${token}` }
      });
      this.renterRequestStatus = response.data.renterRequestStatus;
    } catch (_t) {
      console.error(_t);
      this.error = "Failed to load your request status.";
    }
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
        this.renterRequestStatus = "PENDING"; // update status locally
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
