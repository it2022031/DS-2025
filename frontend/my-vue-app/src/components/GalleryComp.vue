<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
    <PropertyCard
        v-for="property in properties"
        :key="property.id"
        :property="property"
    />
  </div>
</template>

<script setup>
import PropertyCard from './PropertyCard.vue'

const properties = [
  { id: 1, title: 'Διαμέρισμα στο Κολωνάκι', price: '€900', image: 'https://via.placeholder.com/200' },
  { id: 2, title: 'Γκαρσονιέρα στη Θεσσαλονίκη', price: '€450', image: 'https://via.placeholder.com/200' },
  { id: 3, title: 'Μονοκατοικία στην Κρήτη', price: '€1200', image: 'https://via.placeholder.com/200' },
]
</script>
