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

            <template>
              <div class="text-center text-muted mb-4">
                <small>Sign in with your credentials</small>
              </div>
              <form @submit.prevent="handleSignIn" role="form">
                <base-input alternative
                            class="mb-3"
                            placeholder="Username"
                            addon-left-icon="ni ni-email-83"
                            v-model="name">
                </base-input>

                <base-input alternative
                            type="password"
                            placeholder="Password"
                            addon-left-icon="ni ni-lock-circle-open"
                            v-model="password">
                </base-input>

                <base-checkbox>
                  Remember me
                </base-checkbox>

                <div class="text-center">
                  <base-button type="primary" class="my-4" native-type="submit">Sign In</base-button>
                </div>
              </form>
            </template>
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
      name: '',
      password: ''
    };
  },
  methods: {
    async handleSignIn() {
      if (!this.name.trim() || !this.password.trim()) {
        alert("Please enter both username and password.");
        return;
      }

      // Ψεύτικος έλεγχος χρήστη admin
      if (this.name === 'admin' && this.password === 'admin123') {
        const fakeUser = {
          id: 1,
          name: 'Admin User',
          role: 'admin'
        };
        const fakeToken = 'fake-jwt-token';

        localStorage.setItem("token", fakeToken);
        localStorage.setItem("userRole", fakeUser.role);
        localStorage.setItem("username", fakeUser.name);
        localStorage.setItem("userId", fakeUser.id);

        alert(`Welcome, ${fakeUser.name}!`);
        this.$router.push("/");
        return;
      }

      // Ψεύτικος έλεγχος χρήστη user
      if (this.name === 'user' && this.password === 'user123') {
        const fakeUser = {
          id: 2,
          name: 'Regular User',
          role: 'user'
        };
        const fakeToken = 'fake-jwt-token-user';

        localStorage.setItem("token", fakeToken);
        localStorage.setItem("userRole", fakeUser.role);
        localStorage.setItem("username", fakeUser.name);
        localStorage.setItem("userId", fakeUser.id);

        alert(`Welcome, ${fakeUser.name}!`);
        this.$router.push("/");
        return;
      }

      // Κανονικό API login fallback
      try {
        const response = await axios.post('http://localhost:8080/api/auth/login', {
          name: this.name,
          password: this.password
        });

        const { token, user } = response.data;

        localStorage.setItem("token", token);
        localStorage.setItem("userRole", user.role);
        localStorage.setItem("username", user.name);
        localStorage.setItem("userId", user.id);

        alert(`Welcome, ${user.name}!`);
        this.$router.push("/");

      } catch (error) {
        console.error('Login error:', error);
        alert("Login failed. Please check your credentials.");
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
