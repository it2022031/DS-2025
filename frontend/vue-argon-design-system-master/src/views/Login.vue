<template>
  <section class="section section-shaped section-lg my-0">
    <div class="shape shape-style-1 bg-gradient-default" >
      <span></span><span></span><span></span><span></span>
      <span></span><span></span><span></span><span></span>
    </div>

    <div class="container pt-lg-md">
      <div class="row justify-content-center">
        <div class="col-lg-5">
          <card type="secondary" shadow
                header-classes="bg-white pb-5"
                body-classes="px-lg-5 py-lg-5"
                class="border-0">

            <div class="text-center text-muted mb-4">
              <small>Sign in with your credentials</small>
            </div>

            <!-- ✅ Activation feedback -->
            <div v-if="activationMessage" class="small text-center mb-3"
                 :class="activationType === 'success' ? 'text-success' : 'text-danger'">
              {{ activationMessage }}
            </div>

            <form @submit.prevent="handleSignIn" role="form">
              <base-input alternative
                          class="mb-3"
                          placeholder="Username"
                          addon-left-icon="ni ni-email-83"
                          v-model="username">
              </base-input>

              <base-input alternative
                          type="password"
                          placeholder="Password"
                          addon-left-icon="ni ni-lock-circle-open"
                          v-model="password">
              </base-input>

              <div class="mb-3">
                <label>
                  <input type="checkbox" v-model="rememberMe" /> Remember me
                </label>
              </div>

              <div class="text-center" v-if="!isLoggedIn">
                <base-button type="primary" class="my-4" native-type="submit" :disabled="loading">
                  {{ loading ? 'Signing in...' : 'Login' }}
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
                <router-link to="/register" class="text-light"><small>Create new account</small></router-link>
                <br><br><br><br><br><br><br><br>
              </div>
            </template>

            <template v-else>
              <div class="col-12 text-center">
                <button class="btn btn-primary my-4" @click="handleSignIn">LogIn</button>
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

      // ✅ activation feedback
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
    // ✅ Read activation flags from redirect: /#/login?activated=true|false&reason=...
    const q = (this.$route && this.$route.query) ? this.$route.query : {};

    if (q.activated === "true") {
      this.activationType = "success";
      this.activationMessage = "✅ Ο λογαριασμός ενεργοποιήθηκε! Μπορείς τώρα να συνδεθείς.";
    } else if (q.activated === "false") {
      this.activationType = "error";
      const reason = q.reason || "unknown";
      if (reason === "expired") {
        this.activationMessage = "⏳ Το link ενεργοποίησης έληξε. Κάνε ξανά εγγραφή για νέο link.";
      } else if (reason === "invalid") {
        this.activationMessage = "❌ Μη έγκυρο link ενεργοποίησης (ίσως χρησιμοποιήθηκε ήδη).";
      } else {
        this.activationMessage = "❌ Αποτυχία ενεργοποίησης.";
      }
    }
  },
  methods: {
    async handleSignIn() {
      this.error = null;
      if (!this.username.trim() || !this.password) {
        this.error = "Please enter both username and password.";
        return;
      }

      this.loading = true;
      try {
        // 1. Login
        const loginResp = await api.post("/auth/login", {
          username: this.username,
          password: this.password,
        });

        const token = loginResp.data.token;
        if (!token) throw new Error("Token missing from response");

        const ttl = this.rememberMe ? 7 * 24 * 3600 * 1000 : 3600 * 1000;
        localStorage.setItem("token", token);
        localStorage.setItem("token_expiry", Date.now() + ttl);
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        // 2. Παίρνουμε στοιχεία χρήστη
        const profileResp = await api.get("/auth/me");
        const user = profileResp.data;

        // 3. Παίρνουμε ρόλους από το whoami
        const rolesResp = await api.get("/users/whoami");
        const roles = rolesResp.data.map(r => r.replace("ROLE_", "").toUpperCase());

        // 4. Σώζουμε στο localStorage
        localStorage.setItem("userRoles", JSON.stringify(roles));
        if (roles.length > 0) {
          localStorage.setItem("userRole", roles[0]);
        }
        localStorage.setItem("username", user.username || `${user.firstName || ""} ${user.lastName || ""}`.trim());
        localStorage.setItem("userId", user.id);

        this.isLoggedIn = true;
        eventBus.$emit("login-status-changed", true);
        this.$router.push("/");
      } catch (err) {
        console.error(err);
        this.error =
            (err.response && err.response.data && err.response.data.error) ||
            err.message || "Login failed.";
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
