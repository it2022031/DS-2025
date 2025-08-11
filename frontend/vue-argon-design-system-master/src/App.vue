<template>
  <div id="app">
    <router-view name="header" />
    <main class="page-main">
      <fade-transition origin="center" mode="out-in" :duration="250">
        <router-view/>
      </fade-transition>
    </main>
    <router-view name="footer" />
  </div>
</template>

<script>
import { FadeTransition } from "vue2-transitions";

export default {
  components: { FadeTransition }
};
</script>

<style>
/* Δώσε χώρο κάτω από το navbar */
.page-main {
  padding-top: 72px; /* προσαρμόζεις ανάλογα το ύψος του header σου */
}

/* Optional: φτιάξε λίγο και το body αν το nav είναι fixed */
body {
  /* αν εξακολουθεί να καλύπτεται, χρησιμοποίησε αυτό αντί/μαζί με το .page-main */
  /* padding-top: 72px; */
}
</style>
