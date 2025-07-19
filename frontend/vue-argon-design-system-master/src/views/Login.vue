<!--<template>-->
<!--    <section class="section section-shaped section-lg my-0">-->
<!--        <div class="shape shape-style-1 bg-gradient-default">-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--            <span></span>-->
<!--        </div>-->
<!--        <div class="container pt-lg-md">-->
<!--            <div class="row justify-content-center">-->
<!--                <div class="col-lg-5">-->
<!--                    <card type="secondary" shadow-->
<!--                          header-classes="bg-white pb-5"-->
<!--                          body-classes="px-lg-5 py-lg-5"-->
<!--                          class="border-0">-->
<!--                        <template>-->
<!--                            <div class="text-muted text-center mb-3">-->
<!--                                <small>Sign in with</small>-->
<!--                            </div>-->
<!--                            <div class="btn-wrapper text-center">-->
<!--                                <base-button type="neutral">-->
<!--                                    <img slot="icon" src="img/icons/common/github.svg">-->
<!--                                    Github-->
<!--                                </base-button>-->

<!--                                <base-button type="neutral">-->
<!--                                    <img slot="icon" src="img/icons/common/google.svg">-->
<!--                                    Google-->
<!--                                </base-button>-->
<!--                            </div>-->
<!--                        </template>-->
<!--                        <template>-->
<!--                            <div class="text-center text-muted mb-4">-->
<!--                                <small>Or sign in with credentials</small>-->
<!--                            </div>-->
<!--                            <form role="form">-->
<!--                                <base-input alternative-->
<!--                                            class="mb-3"-->
<!--                                            placeholder="Email"-->
<!--                                            addon-left-icon="ni ni-email-83">-->
<!--                                </base-input>-->
<!--                                <base-input alternative-->
<!--                                            type="password"-->
<!--                                            placeholder="Password"-->
<!--                                            addon-left-icon="ni ni-lock-circle-open">-->
<!--                                </base-input>-->
<!--                                <base-checkbox>-->
<!--                                    Remember me-->
<!--                                </base-checkbox>-->
<!--                                <div class="text-center">-->
<!--                                    <base-button type="primary" class="my-4">Sign In</base-button>-->
<!--                                </div>-->
<!--                            </form>-->
<!--                        </template>-->
<!--                    </card>-->
<!--                    <div class="row mt-3">-->
<!--                        <div class="col-6">-->
<!--                            <a href="#" class="text-light">-->
<!--                                <small>Forgot password?</small>-->
<!--                            </a>-->
<!--                        </div>-->
<!--                        <div class="col-6 text-right">-->
<!--                            <a href="#" class="text-light">-->
<!--                                <small>Create new account</small>-->
<!--                            </a>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                </div>-->
<!--            </div>-->
<!--        </div>-->
<!--    </section>-->
<!--</template>-->
<!--<script>-->
<!--export default {};-->
<!--</script>-->
<!--<style>-->
<!--</style>-->



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
<!--            <template>-->
<!--              <div class="text-muted text-center mb-3">-->
<!--                <small>Sign in with</small>-->
<!--              </div>-->
<!--&lt;!&ndash;              <div class="btn-wrapper text-center">&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                <base-button type="neutral">&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                  <img slot="icon" src="img/icons/common/github.svg">&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                  Github&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                </base-button>&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                <base-button type="neutral">&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                  <img slot="icon" src="img/icons/common/google.svg">&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                  Google&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;&lt;!&ndash;                </base-button>&ndash;&gt;&ndash;&gt;-->
<!--&lt;!&ndash;              </div>&ndash;&gt;-->
<!--            </template>-->

            <template>
              <div class="text-center text-muted mb-4">
                <small>Or sign in with credentials</small>
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
                  <base-button type="primary" class="my-4" @click="handleSignIn">Sign In</base-button>
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
      if (!this.name.trim()) {
        alert("Please enter a name before signing in.");
        return;
      }

      try {
        const response = await axios.post('http://localhost:8080/api/users', {
          name: this.name
        });
        console.log('User created:', response.data);
        alert(`Welcome, ${response.data.name}!`);
      } catch (error) {
        console.error('Error posting user:', error);
        alert("An error occurred while signing in.");
      }
    }
  }
};
</script>

<style scoped>
/* Simple visual polish */
input {
  border-radius: 8px !important;
}

.base-button {
  border-radius: 25px !important;
  padding: 10px 20px;
}
</style>
