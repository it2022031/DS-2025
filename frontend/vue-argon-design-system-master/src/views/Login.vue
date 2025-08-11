<template>
  <section class="section section-shaped section-lg my-0">
    <div class="shape shape-style-1 bg-gradient-default">
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
                <a href="#" class="text-light"><small>Forgot password?</small></a>
              </div>
              <div class="col-6 text-right">
                <router-link to="/register" class="text-light"><small>Create new account</small></router-link>
              </div>
            </template>

            <template v-else>
              <div class="col-12 text-center">
                <button class="btn btn-danger" @click="logout">Logout</button>
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

const DEFAULT_AVATAR = "https://cdn-icons-png.flaticon.com/512/147/147144.png";

export default {
  name: "Login",
  data() {
    return {
      username: "",
      password: "",
      rememberMe: false,
      loading: false,
      error: null,
      isLoggedIn: false
    };
  },
  created() {
    // ΜΗΝ χρησιμοποιείς μεταβλητή 'user' εδώ – απλός έλεγχος token
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
  methods: {
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

        const token = loginResp.data && loginResp.data.token;
        if (!token) throw new Error("Token missing from response");

        const ttl = this.rememberMe ? 7 * 24 * 3600 * 1000 : 3600 * 1000;
        const expiry = Date.now() + ttl;
        localStorage.setItem("token", token);
        localStorage.setItem("token_expiry", String(expiry));
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`;

        // 2) Προφίλ
        const meResp = await api.get("/auth/me");
        const user = meResp.data || {};

        // 3) Ρόλοι
        const roles = Array.isArray(user.roles) ? user.roles : [];
        localStorage.setItem("userRoles", JSON.stringify(roles));
        if (roles.length > 0) {
          localStorage.setItem("userRole", roles[0]); // optional
        }

        // 4) Στοιχεία χρήστη
        localStorage.setItem(
            "username",
            user.username || `${user.firstName || ""} ${user.lastName || ""}`.trim()
        );
        if (user.id != null) localStorage.setItem("userId", String(user.id));

        // 5) Avatar (ΜΗΝ προσπαθείς να διαβάσεις user.avatar – φέρε blob)
        let avatarUrl = DEFAULT_AVATAR;
        try {
          const uid = localStorage.getItem("userId");
          if (uid) {
            const blobRes = await api.get(`/users/${uid}/photo`, {
              responseType: "blob"
            });
            if (blobRes && blobRes.data && blobRes.data.size > 0) {
              avatarUrl = URL.createObjectURL(blobRes.data);
            }
          }
        } catch (e) {
          // αν δεν υπάρχει φωτο, κράτα το default
        }
        localStorage.setItem("userAvatar", avatarUrl);
        eventBus.$emit("avatar-updated");

        this.isLoggedIn = true;
        alert(`Welcome, ${localStorage.getItem("username")}!`);
        eventBus.$emit("login-status-changed", true);
        this.$router.push("/");
      } catch (err) {
        console.error(err);
        this.error =
            (err.response && err.response.data && (err.response.data.error || err.response.data.message)) ||
            err.message ||
            "Login failed.";
      } finally {
        this.loading = false;
      }
    },
    logout() {
      localStorage.clear();
      if (api && api.defaults && api.defaults.headers && api.defaults.headers.common) {
        delete api.defaults.headers.common.Authorization;
      }
      this.isLoggedIn = false;
      // αποφυγή NavigationDuplicated
      if (this.$route.path !== '/login') {
        this.$router.push('/login').catch(() => {});
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
