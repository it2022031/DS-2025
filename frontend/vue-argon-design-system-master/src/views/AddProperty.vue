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
            <form @submit.prevent="handleAddProperty" role="form">
              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Property Name"
                  v-model="name"
                  addon-left-icon="ni ni-building">
              </base-input>

              <base-input
                  alternative
                  class="mb-3"
                  placeholder="Property Description"
                  v-model="description"
                  addon-left-icon="ni ni-align-left-2">
              </base-input>

              <div class="text-center">
                <base-button
                    type="primary"
                    class="my-4"
                    @click="handleAddProperty">Add Property</base-button>
              </div>
            </form>

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
      description: ''
    };
  },
  methods: {
    async handleAddProperty() {
      if (!this.name.trim() || !this.description.trim()) {
        alert("Please enter both name and description.");
        return;
      }

      try {
        const response = await axios.post('http://localhost:8080/api/properties', {
          name: this.name,
          description: this.description
        });
        alert(`Property "${response.data.name}" added successfully!`);
        this.name = '';
        this.description = '';
      } catch (error) {
        console.error('Error adding property:', error);
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
</style>
