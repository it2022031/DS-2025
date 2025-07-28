<template>
  <div class="list-properties">
    <h2>🏠 Properties List</h2>
    <ul>
      <li v-for="property in properties" :key="property.id" class="property-card">
        <img :src="property.image" :alt="property.name" class="property-image" />
        <div class="property-info">
          <h3>{{ property.name }}</h3>
          <p>{{ property.description }}</p>
        </div>
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
        {
          id: 1,
          name: "Property One",
          description: "Description for property one",
          image: "https://static9.depositphotos.com/1086013/1149/i/450/depositphotos_11497591-stock-photo-superb-backyard.jpg"
        },
        {
          id: 2,
          name: "Property Two",
          description: "Description for property two",
          image: "https://hitech-house.com/application/files/thumbnails/portofolio/9217/0275/7057/23943JD_Photo001_1699021994.webp"
        }
      ]
    };
  }
};
</script>

<style scoped>
.list-properties {
  padding: 30px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f7fa;
  max-width: 900px;
  margin: auto;
}

.list-properties h2 {
  margin-bottom: 30px;
  font-size: 32px;
  text-align: center;
  color: #2c3e50;
}

.list-properties ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.property-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.property-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.property-image {
  width: 100%;
  height: auto;
  object-fit: cover;
  display: block;
}

.property-info {
  padding: 20px;
}

.property-info h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #2c3e50;
}

.property-info p {
  margin: 0;
  color: #555;
  font-size: 16px;
}
</style>
