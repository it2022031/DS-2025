<template>
  <section class="section bg-light py-5">
    <div class="container">
      <h2 class="mb-4 text-center">🏠 Τα ακίνητά μου</h2>

      <div v-if="loading" class="text-center">Φόρτωση...</div>
      <div v-else-if="error" class="text-center text-danger">{{ error }}</div>
      <div v-else-if="properties.length === 0" class="text-center text-muted">
        Δεν έχετε προσθέσει ακίνητα ακόμα.
      </div>

      <div v-else class="row">
        <div class="col-md-6 col-lg-4 mb-4" v-for="p in properties" :key="p.id">
          <div class="card h-100 shadow-sm">
            <div class="card-body d-flex flex-column">
              <h5 class="card-title mb-2">{{ p.name }}</h5>
              <p class="card-text text-muted mb-2">{{ p.description }}</p>
              <p class="mb-1"><strong>Τοποθεσία:</strong> {{ p.city }}, {{ p.country }}</p>
              <p class="mb-1"><strong>Διεύθυνση:</strong> {{ p.street }} {{ p.postalCode }}</p>
              <p class="mb-3"><strong>Τ.Μ.:</strong> {{ p.squareMeters }}</p>

              <router-link
                  class="btn btn-sm btn-outline-primary mt-auto"
                  :to="`/properties/${p.id}/edit`"
              >
                ✏️ Επεξεργασία
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="text-center mt-4">
        <router-link to="/properties/add" class="btn btn-success">
          ➕ Προσθήκη νέου ακινήτου
        </router-link>
      </div>
    </div>
  </section>
</template>

<script>
import api from "@/api";

export default {
  name: "MyProperties",
  data() {
    return {
      properties: [],
      loading: false,
      error: null
    };
  },
  methods: {
    async loadMyProperties() {
      this.loading = true;
      this.error = null;

      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      if (!token || !userId) {
        this.error = "Δεν είστε συνδεδεμένοι.";
        this.$router.push("/login");
        return;
      }

      try {
        const res = await api.get("/users/" + userId + "/properties");
        this.properties = res.data || [];
      } catch (e) {
        console.error(e);
        if (e && e.response && e.response.status === 401) {
          this.error = "Η συνεδρία σας έληξε. Παρακαλώ συνδεθείτε ξανά.";
          this.$router.push("/login");
        } else {
          this.error = "Αποτυχία φόρτωσης ακινήτων.";
        }
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {
    this.loadMyProperties();
  }
};
</script>

<style scoped>
.card { border-radius: 12px; }
</style>
