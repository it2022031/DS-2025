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

              <div class="text-center">
                <base-button type="primary" class="my-4" native-type="submit" :disabled="loading">
                  {{ loading ? 'Signing in...' : 'Sign In' }}
                </base-button>
              </div>

              <div v-if="error" class="text-danger small text-center">
                {{ error }}
              </div>
            </form>

          </card>

          <div class="row mt-3">
            <div class="col-6">
              <a href="#" class="text-light"><small>Forgot password?</small></a>
            </div>
            <div class="col-6 text-right">
              <a href="#" class="text-light"><small>Create new account</small></a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'SignInForm',
  data() {
    return {
      username: '',
      password: '',
      rememberMe: false,
      loading: false,
      error: null,
    };
  },
  created() {
    const token = localStorage.getItem('token');
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
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
        // login
        const loginResp = await axios.post('http://localhost:8080/api/auth/login', {
          username: this.username,
          password: this.password
        });

        const token = loginResp.data.token;
        if (!token) {
          throw new Error("Token missing from response");
        }

        // set header globally
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        localStorage.setItem("token", token);

        // fetch profile
        const profileResp = await axios.get('http://localhost:8080/api/users/me');
        const user = profileResp.data;

        // persist user info
        localStorage.setItem("username", user.username || `${user.firstName || ''} ${user.lastName || ''}`.trim());
        localStorage.setItem("userRole", user.role);
        localStorage.setItem("userId", user.id);

        alert(`Welcome, ${localStorage.getItem("username")}!`);
        this.$router.push("/");

      } catch (err) {
        console.error(err);
        if (err.response && err.response.data) {
          if (typeof err.response.data === 'string') {
            this.error = err.response.data;
          } else if (err.response.data.error) {
            this.error = err.response.data.error;
          } else {
            this.error = JSON.stringify(err.response.data);
          }
        } else if (err.message) {
          this.error = err.message;
        } else {
          this.error = "Login failed.";
        }
      } finally {
        this.loading = false;
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
