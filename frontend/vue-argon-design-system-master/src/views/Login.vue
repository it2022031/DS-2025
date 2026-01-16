<template>
  <section class="section section-shaped section-lg my-0">
    <div class="shape shape-style-1 bg-gradient-default">
      <span></span><span></span><span></span><span></span>
      <span></span><span></span><span></span><span></span>
    </div>

    <div class="container pt-lg-md">
      <div class="row justify-content-center">
        <div class="col-lg-5">
          <card
              type="secondary"
              shadow
              header-classes="bg-white pb-5"
              body-classes="px-lg-5 py-lg-5"
              class="border-0"
          >
            <div class="text-center text-muted mb-4">
              <small>Sign in with your credentials</small>
            </div>

            <!-- ✅ Activation feedback (πιο εμφανές) -->
            <div
                v-if="activationMessage"
                class="alert py-2 small text-center mb-3"
                :class="activationType === 'success' ? 'alert-success' : 'alert-danger'"
            >
              {{ activationMessage }}
            </div>

            <form @submit.prevent="handleSignIn" role="form">
              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Username"
                  addon-left-icon="ni ni-email-83"
                  v-model="username"
              />

              <base-input
                  alternative
                  type="password"
                  placeholder="Password"
                  addon-left-icon="ni ni-lock-circle-open"
                  v-model="password"
              />

              <div class="mb-3">
                <label>
                  <input type="checkbox" v-model="rememberMe" /> Remember me
                </label>
              </div>

              <div class="text-center" v-if="!isLoggedIn">
                <base-button
                    type="primary"
                    class="my-4"
                    native-type="submit"
                    :disabled="loading"
                >
                  {{ loading ? "Signing in..." : "Login" }}
                </base-button>
              </div>

              <div v-if="error" class="text-danger small text-center">
                {{ error }}
              </div>
            </form>
          </card>

          <div class="row mt-3">
            <template v-if="!isLoggedIn">
              <div class="col-6">
                <a href="#" class="text-light"><small></small></a>
              </div>
              <div class="col-6 text-right">
                <router-link to="/register" class="text-light">
                  <small>Create new account</small>
                </router-link>
                <br /><br /><br /><br /><br /><br /><br /><br />
              </div>
            </template>

            <template v-else>
              <div class="col-12 text-center">
                <button class="btn btn-primary my-4" @click="handleSignIn">
                  LogIn
                </button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import api from "@/api";
import { eventBus } from "@/eventBus";

export default {
  name: "Login",
  data() {
    return {
      username: "",
      password: "",
      rememberMe: false,
      loading: false,
      error: null,
      isLoggedIn: false,

      // activation feedback
      activationMessage: "",
      activationType: "" // "success" | "error"
    };
  },

  created() {
    const token = localStorage.getItem("token");
    const expiry = localStorage.getItem("token_expiry");

    if (token && expiry && Date.now() < Number(expiry)) {
      api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      this.isLoggedIn = true;
    } else {
      localStorage.clear();
      this.isLoggedIn = false;
    }
  },

  mounted() {
    this.applyActivationFromUrl();
  },

  watch: {
    // Αν ξαναέρθεις στο ίδιο component με άλλο query/hash
    $route() {
      this.applyActivationFromUrl();
    }
  },

  methods: {
    /**
     * Γιατί το κάνουμε έτσι:
     * Σε Vue2 + hash mode το URL είναι π.χ.:
     *   http://localhost:8081/#/login?activated=false&reason=invalid
     *
     * Κάποιες φορές το vue-router ΔΕΝ γεμίζει σωστά το this.$route.query
     * επειδή το query είναι "μέσα" στο hash.
     *
     * Άρα:
     * 1) Προσπαθούμε πρώτα από this.$route.query
     * 2) Αν είναι άδειο, κάνουμε parse από window.location.hash με URLSearchParams
     */
    getActivationQuery() {
      // 1) Προτίμησε vue-router query αν υπάρχει
      const q1 = this.$route && this.$route.query ? this.$route.query : {};
      if (q1 && (q1.activated !== undefined || q1.reason !== undefined)) return q1;

      // 2) Fallback: parse από hash "#/login?activated=false&reason=invalid"
      const hash = window.location.hash || "";
      const idx = hash.indexOf("?");
      if (idx === -1) return {};

      const qs = hash.substring(idx + 1);
      const params = new URLSearchParams(qs);

      const activated = params.get("activated");
      const reason = params.get("reason");

      const out = {};
      if (activated !== null) out.activated = activated;
      if (reason !== null) out.reason = reason;
      return out;
    },

    applyActivationFromUrl() {
      const q = this.getActivationQuery();

      // reset κάθε φορά
      this.activationMessage = "";
      this.activationType = "";

      if (q.activated === "true") {
        this.activationType = "success";
        this.activationMessage =
            "✅ Ο λογαριασμός ενεργοποιήθηκε! Μπορείς τώρα να συνδεθείς.";
      } else if (q.activated === "false") {
        this.activationType = "error";
        const reason = q.reason || "unknown";

        if (reason === "expired") {
          this.activationMessage =
              "⏳ Το link ενεργοποίησης έληξε. Ζήτησε νέο link ενεργοποίησης.";
        } else if (reason === "invalid") {
          this.activationMessage =
              "❌ Μη έγκυρο link ενεργοποίησης (ίσως χρησιμοποιήθηκε ήδη).";
        } else {
          this.activationMessage = "❌ Αποτυχία ενεργοποίησης.";
        }
      }

      // (Προαιρετικό) καθάρισε τα query params από το URL για να μην ξαναδείχνει σε refresh
      // Αν το θες, ξεσχόλιασε:
      /*
      if (this.activationMessage) {
        this.$router.replace({ path: this.$route.path, query: {} }).catch(() => {});
      }
      */
    },

    async handleSignIn() {
      this.error = null;
      if (!this.username.trim() || !this.password) {
        this.error = "Please enter both username and password.";
        return;
      }

      this.loading = true;
      try {
        // 1) Login
        const loginResp = await api.post("/auth/login", {
          username: this.username,
          password: this.password
        });

        const token = loginResp.data.token;
        if (!token) throw new Error("Token missing from response");

        const ttl = this.rememberMe ? 7 * 24 * 3600 * 1000 : 3600 * 1000;
        localStorage.setItem("token", token);
        localStorage.setItem("token_expiry", Date.now() + ttl);
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        // 2) Profile
        const profileResp = await api.get("/auth/me");
        const user = profileResp.data;

        // 3) Roles
        const rolesResp = await api.get("/users/whoami");
        const roles = rolesResp.data.map(r => r.replace("ROLE_", "").toUpperCase());

        // 4) localStorage
        localStorage.setItem("userRoles", JSON.stringify(roles));
        if (roles.length > 0) {
          localStorage.setItem("userRole", roles[0]);
        }
        localStorage.setItem(
            "username",
            user.username || `${user.firstName || ""} ${user.lastName || ""}`.trim()
        );
        localStorage.setItem("userId", user.id);

        this.isLoggedIn = true;
        eventBus.$emit("login-status-changed", true);
        this.$router.push("/");
      } catch (err) {
        console.error(err);
        this.error =
            (err.response && err.response.data && err.response.data.error) ||
            err.message ||
            "Login failed.";
      } finally {
        this.loading = false;
      }
    },

    logout() {
      localStorage.clear();
      delete api.defaults.headers.common["Authorization"];
      this.isLoggedIn = false;
      eventBus.$emit("login-status-changed", false);
      if (this.$route.path !== "/login") {
        this.$router.push("/login").catch(() => {});
      }
    }
  }
};
</script>

<style scoped>
input {
  border-radius: 8px !important;
}
.base-button {
  border-radius: 25px !important;
  padding: 10px 20px;
}
</style>
