<!--<script setup lang="ts">-->

<!--</script>-->

<!--<template>-->

<!--</template>-->

<!--<style scoped>-->

<!--</style>-->

<template>
  <div>
    <HeaderComp />
    <main class="p-4">
      <h2 class="text-2xl font-bold mb-4">Προτεινόμενα Ακίνητα</h2>
      <GalleryComp />
    </main>
    <footer class="bg-gray-200 p-4 text-center">
      © 2025 My Rent Platform
    </footer>
  </div>
</template>

<script setup>
import HeaderComp from '../components/HeaderComp.vue'
import GalleryComp from '../components/GalleryComp.vue'
</script>
