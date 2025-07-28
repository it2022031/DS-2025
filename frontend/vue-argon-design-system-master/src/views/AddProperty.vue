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
              <small>Add a new property</small>
            </div>

            <form @submit.prevent="submitForm" role="form">
              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Property Name"
                  v-model="name"
                  addon-left-icon="ni ni-building" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Description"
                  v-model="description"
                  addon-left-icon="ni ni-align-left-2" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Country"
                  v-model="country"
                  addon-left-icon="ni ni-world-2" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="City"
                  v-model="city"
                  addon-left-icon="ni ni-square-pin" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Street"
                  v-model="street"
                  addon-left-icon="ni ni-map-big" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Postal Code"
                  v-model="postalCode"
                  addon-left-icon="ni ni-key-25" />

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Square Meters"
                  type="number"
                  v-model.number="squareMeters"
                  addon-left-icon="ni ni-ruler-pencil" />

              <div class="text-center">
                <button type="submit" class="btn btn-primary my-4">
                  <template v-if="success">✔️</template>
                  Add Property
                </button>
              </div>
            </form>

            <div v-if="success" class="text-center my-3">
              <base-button type="success" disabled>
                ✔️ Property added successfully!
              </base-button>
            </div>

            <!-- Πίσω κουμπί -->
            <div class="text-center mt-3">
              <button class="btn btn-light" @click="$router.back()">← Back</button>
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
  name: 'AddProperty',
  data() {
    return {
      name: '',
      description: '',
      country: '',
      city: '',
      street: '',
      postalCode: '',
      squareMeters: null,
      success: false
    };
  },
  methods: {
    async submitForm() {
      if (
          !this.name.trim() ||
          !this.description.trim() ||
          !this.country.trim() ||
          !this.city.trim() ||
          !this.street.trim() ||
          !this.postalCode.trim() ||
          !this.squareMeters
      ) {
        alert("Please fill in all fields.");
        return;
      }

      try {
        const response = await axios.post("http://localhost:8080/api/properties/add", {
          name: this.name,
          description: this.description,
          country: this.country,
          city: this.city,
          street: this.street,
          postalCode: this.postalCode,
          squareMeters: this.squareMeters
        });

        this.success = true;
        alert(`Property "${response.data.name}" added successfully!`);

        this.name = '';
        this.description = '';
        this.country = '';
        this.city = '';
        this.street = '';
        this.postalCode = '';
        this.squareMeters = null;

      } catch (error) {
        console.error("Error adding property:", error);
        alert("An error occurred while adding the property.");
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

.btn-light {
  background-color: #f8f9fa;
  border: 1px solid #ced4da;
  color: #212529;
  transition: background-color 0.2s ease;
}

.btn-light:hover {
  background-color: #e2e6ea;
}
</style>
