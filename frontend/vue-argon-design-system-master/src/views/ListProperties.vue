<template>
  <div class="list-properties">
    <h2>Properties List</h2>
    <ul>
      <li v-for="property in properties" :key="property.id">
        <strong>{{ property.name }}</strong> - {{ property.description }}
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "ListProperties",
  data() {
    return {
      properties: [
        { id: 1, name: "Property One", description: "Description for property one" },
        { id: 2, name: "Property Two", description: "Description for property two" }
      ]
    };
  }
};
</script>

<style scoped>
.list-properties {
  padding: 20px;
  font-family: Arial, sans-serif;
}

.list-properties h2 {
  margin-bottom: 10px;
}

.list-properties ul {
  list-style-type: none;
  padding: 0;
}

.list-properties li {
  padding: 8px 0;
  border-bottom: 1px solid #ccc;
}
</style>
