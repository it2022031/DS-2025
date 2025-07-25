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
              <small>Sign up with credentials</small>
            </div>

            <form role="form" @submit.prevent="submitForm">
              <base-input
                  v-model="name"
                  alternative
                  class="mb-1"
                  placeholder="Name"
                  addon-left-icon="ni ni-hat-3"
                  @blur="onBlur('name')"
                  @input="onInput('name')"
              />
              <div v-if="showError('name')" class="text-danger text-sm mb-2">
                {{ errors.name }}
              </div>

              <base-input
                  v-model="surname"
                  alternative
                  class="mb-1"
                  placeholder="Surname"
                  addon-left-icon="ni ni-hat-3"
                  @blur="onBlur('surname')"
                  @input="onInput('surname')"
              />
              <div v-if="showError('surname')" class="text-danger text-sm mb-2">
                {{ errors.surname }}
              </div>

              <base-input
                  v-model="email"
                  alternative
                  class="mb-1"
                  placeholder="Email"
                  addon-left-icon="ni ni-email-83"
                  @blur="onBlur('email')"
                  @input="onInput('email')"
              />
              <div v-if="showError('email')" class="text-danger text-sm mb-2">
                {{ errors.email }}
              </div>

              <base-input
                  v-model="username"
                  alternative
                  class="mb-1"
                  placeholder="Username"
                  addon-left-icon="ni ni-single-02"
                  @blur="onBlur('username')"
                  @input="onInput('username')"
              />
              <div v-if="showError('username')" class="text-danger text-sm mb-2">
                {{ errors.username }}
              </div>

              <base-input
                  v-model="password"
                  alternative
                  type="password"
                  class="mb-1"
                  placeholder="Password"
                  addon-left-icon="ni ni-lock-circle-open"
                  @blur="onBlur('password')"
                  @input="onInput('password')"
              />
              <div v-if="showError('password')" class="text-danger text-sm mb-2">
                {{ errors.password }}
              </div>

              <base-input
                  v-model="idNumber"
                  alternative
                  class="mb-1"
                  placeholder="ID or Passport Number"
                  addon-left-icon="ni ni-credit-card"
                  @blur="onBlur('idNumber')"
                  @input="onInput('idNumber')"
              />
              <div v-if="showError('idNumber')" class="text-danger text-sm mb-2">
                {{ errors.idNumber }}
              </div>

              <base-input
                  v-model="taxNumber"
                  alternative
                  class="mb-1"
                  placeholder="Tax Registration Number (ΑΦΜ)"
                  addon-left-icon="ni ni-badge"
                  @blur="onBlur('taxNumber')"
                  @input="onInput('taxNumber')"
              />
              <div v-if="showError('taxNumber')" class="text-danger text-sm mb-2">
                {{ errors.taxNumber }}
              </div>

              <div class="text-muted font-italic">
                <small>password strength:
                  <span class="text-success font-weight-700">strong</span>
                </small>
              </div>

              <div v-if="errorMessage" class="text-danger text-center mt-2">
                {{ errorMessage }}
              </div>

              <div class="text-center">
                <button type="submit" class="btn btn-primary my-4">
                  <template v-if="!errorMessage && submitted">
                    ✔️
                  </template>
                  Create account
                </button>
              </div>
            </form>

            <div v-if="success" class="text-center my-3">
              <base-button type="success" disabled>
                ✔️ Account created successfully!
              </base-button>
            </div>

          </card>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      name: '',
      surname: '',
      email: '',
      username: '',
      password: '',
      idNumber: '',
      taxNumber: '',
      errors: {},
      touched: {},
      submitted: false,
      errorMessage: '',
      success: false
    };
  },
  methods: {
    onInput(field) {
      this.touched[field] = true;
      this.validateField(field);
    },
    onBlur(field) {
      this.touched[field] = true;
      this.validateField(field);
    },
    showError(field) {
      return this.errors[field] && (this.touched[field] || this.submitted);
    },
    validateField(field) {
      const val = this[field] ? this[field].trim() : '';
      this.errors[field] = '';

      switch (field) {
        case 'name':
        case 'surname':
          if (!val) {
            this.errors[field] = `${field === 'name' ? 'Name' : 'Surname'} is required.`;
          } else if (/\d/.test(val)) {
            this.errors[field] = `${field === 'name' ? 'Name' : 'Surname'} must not contain numbers.`;
          }
          break;
        case 'email':
          if (!val) {
            this.errors[field] = 'Email is required.';
          } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
            this.errors[field] = 'Email format is invalid.';
          }
          break;
        case 'username':
          if (!val) this.errors[field] = 'Username is required.';
          break;
        case 'password':
          if (!val) this.errors[field] = 'Password is required.';
          break;
        case 'taxNumber':
          if (!val) {
            this.errors[field] = 'Tax Number is required.';
          } else if (!/^\d+$/.test(val)) {
            this.errors[field] = 'Tax Number must contain only digits.';
          }
          break;
      }
    },
    validateAllFields() {
      const fields = ['name', 'surname', 'email', 'username', 'password', 'idNumber', 'taxNumber'];
      fields.forEach(field => {
        this.touched[field] = true;
        this.validateField(field);
      });
      const hasErrors = Object.values(this.errors).some(Boolean);
      this.errorMessage = hasErrors ? 'Please fix the errors above before submitting.' : '';
      return !hasErrors;
    },
    async submitForm() {
      this.submitted = true;
      if (this.validateAllFields()) {
        try {
          const response = await axios.post('http://localhost:8080/api/users/analytical', {
            username: this.username,
            password: this.password,
            firstName: this.name,
            lastName: this.surname,
            email: this.email,
            passportNumber: this.idNumber,
            afm: this.taxNumber
          });
          this.success = true;
          this.errorMessage = '';
          alert('Account created successfully for ' + response.data.username);
        } catch (error) {
          this.success = false;
          this.errorMessage = 'Error creating account: ' + (error.response && error.response.data && error.response.data.message ? error.response.data.message : error.message);
        }
      } else {
        this.success = false;
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

.checkmark {
  position: absolute;
  right: 10px;
  top: 10px;
  font-weight: bold;
  font-size: 18px;
}
</style>
