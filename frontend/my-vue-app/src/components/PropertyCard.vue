<template>
  <div class="border rounded shadow hover:shadow-lg transition p-2">
    <img :src="property.image" :alt="property.title" class="w-full h-48 object-cover rounded" />
    <h3 class="text-lg font-semibold mt-2">{{ property.title }}</h3>
    <p class="text-gray-700">{{ property.price }}</p>
  </div>
</template>

<!-- PropertyCard.vue -->

<script setup>
// eslint-disable-next-line no-undef
defineProps({
  property: Object
})
</script>

