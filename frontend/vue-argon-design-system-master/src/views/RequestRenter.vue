<template>
  <section class="section bg-light py-5 d-flex align-items-center">
    <div class="container">
      <div class="card request-card mx-auto p-5 shadow-sm" style="max-width: 500px;">
        <h2 class="mb-3 text-center">Request Renter Role</h2>
        <p class="text-center text-muted mb-4">
          If you want to rent properties, you need to request the Renter role.
        </p>

        <div v-if="error" class="text-danger mb-3 text-center">{{ error }}</div>

        <div v-if="renterRequestStatus === 'PENDING'" class="text-warning mb-3 text-center">
          Your request to become a renter is pending.
        </div>

        <div v-else class="text-center">
          <button class="btn btn-primary rounded-btn" @click="sendRequest">
            Send Request
          </button>
        </div>
      </div>
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

.request-card {
  border-radius: 1rem;
  background: #fff;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.request-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.12);
}

.rounded-btn {
  border-radius: 25px;
  padding: 0.6rem 1.8rem;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.rounded-btn:hover {
  transform: translateY(-2px);
}

</style>
