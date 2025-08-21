<template>
  <section class="container py-4">
    <h1>Property Details</h1>
    <p>Το ID του ακινήτου είναι: <strong>{{ idToShow }}</strong></p>
  </section>
</template>

<script>
export default {
  name: "PropertyDetails",
  props: {
    id: { type: [String, Number], required: false }
  },
  computed: {
    idToShow() {
      // ΜΗ χρήση ?? ή optional chaining για συμβατότητα με παλαιότερο ESLint
      if (this.id !== undefined && this.id !== null && this.id !== "") {
        return this.id;
      }
      if (this.$route && this.$route.params && this.$route.params.id !== undefined) {
        return this.$route.params.id;
      }
      return "(χωρίς id)";
    }
  }
};
</script>

<style scoped>
.container { max-width: 600px; }
</style>