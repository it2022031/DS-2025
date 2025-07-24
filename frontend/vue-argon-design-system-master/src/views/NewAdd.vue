<template>
  <section class="section section-shaped section-lg my-0">
    <div class="shape shape-style-1 bg-gradient-default">
      <span></span><span></span><span></span><span></span>
      <span></span><span></span><span></span><span></span>
    </div>

    <div class="container pt-lg-md">
      <div class="row justify-content-center">
        <div class="col-lg-5">
          <card type="secondary" shadow header-classes="bg-white pb-5" body-classes="px-lg-5 py-lg-5" class="border-0">
            <template>
              <div class="text-center text-muted mb-4">
                <small>Add New Property</small>
              </div>

              <form @submit.prevent="submitForm">
                <base-input
                    v-model="address"
                    alternative
                    class="mb-3"
                    placeholder="Address"
                    addon-left-icon="ni ni-pin-3"
                    @blur="onBlur('address')"
                    @input="onInput('address')"
                />
                <div v-if="showError('address')" class="text-danger text-sm mb-2">
                  {{ errors.address }}
                </div>

                <base-input
                    v-model="price"
                    alternative
                    class="mb-3"
                    placeholder="Price (€)"
                    type="number"
                    addon-left-icon="ni ni-money-coins"
                    @blur="onBlur('price')"
                    @input="onInput('price')"
                />
                <div v-if="showError('price')" class="text-danger text-sm mb-2">
                  {{ errors.price }}
                </div>

                <div class="mb-3">
                  <label class="form-control-label text-muted">Select Owner</label>
                  <select v-model="ownerId" class="form-control" @blur="onBlur('ownerId')">
                    <option disabled value="">-- Select User --</option>
                    <option v-for="user in users" :key="user.id" :value="user.id">
                      {{ user.username }}
                    </option>
                  </select>
                  <div v-if="showError('ownerId')" class="text-danger text-sm mt-2">
                    {{ errors.ownerId }}
                  </div>
                </div>

                <div v-if="errorMessage" class="text-danger text-center mt-2">
                  {{ errorMessage }}
                </div>

                <div class="text-center">
                  <base-button type="primary" class="my-4">
                    Add Property
                  </base-button>
                </div>
              </form>

              <div v-if="success" class="text-center my-3">
                <base-button type="success" disabled>
                  ✔️ Property added successfully!
                </base-button>
              </div>
            </template>
          </card>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  data() {
    return {
      address: '',
      price: '',
      ownerId: '',
      users: [],
      errors: {},
      touched: {},
      submitted: false,
      errorMessage: '',
      success: false
    };
  },
  mounted() {
    this.fetchUsers();
  },
  methods: {
    fetchUsers() {
      fetch('http://localhost:8080/users') // adjust this path if needed
          .then(res => res.json())
          .then(data => {
            this.users = data;
          })
          .catch(err => {
            console.error('Failed to load users:', err);
          });
    },
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
      const val = this[field] ? this[field].toString().trim() : '';
      this.errors[field] = '';

      if (!val) {
        this.errors[field] = `${field.charAt(0).toUpperCase() + field.slice(1)} is required.`;
        return;
      }

      if (field === 'price' && (isNaN(parseFloat(val)) || parseFloat(val) <= 0)) {
        this.errors[field] = 'Price must be a positive number.';
      }
    },
    validateAll() {
      ['address', 'price', 'ownerId'].forEach(f => this.validateField(f));
      return Object.values(this.errors).every(e => !e);
    },
    submitForm() {
      this.submitted = true;
      if (!this.validateAll()) {
        this.errorMessage = 'Please fix the errors above.';
        this.success = false;
        return;
      }

      const payload = {
        address: this.address,
        price: parseFloat(this.price),
        ownerId: this.ownerId
      };

      fetch('http://localhost:8080/properties', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
          .then(res => {
            if (!res.ok) throw new Error('Error while adding property.');
            return res.json();
          })
          .then(() => {
            this.success = true;
            this.errorMessage = '';
            this.clearForm();
          })
          .catch(err => {
            console.error(err);
            this.success = false;
            this.errorMessage = 'Failed to add property.';
          });
    },
    clearForm() {
      this.address = '';
      this.price = '';
      this.ownerId = '';
      this.touched = {};
      this.errors = {};
      this.submitted = false;
    }
  }
};
</script>

<style scoped>
select.form-control {
  border-radius: 8px;
}
</style>
