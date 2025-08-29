<template>
  <div class="home-page">
    <!-- Featured property banner -->
    <section class="hero-banner">
      <img src="/img/home_featured_photo.jpg" alt="Featured Property" class="hero-img" />
      <div class="hero-overlay">
        <h1 class="hero-title">All You Need About Real Estate</h1>
        <p class="hero-subtitle">Rent unique properties, or register your own</p>
      </div>
    </section>

    <!-- Awards section -->
    <section class="awards text-center">
      <h2 class="fw-bold">Why to trust us</h2>
      <p class="lead mb-4">Recognized by top organizations in the real estate industry</p>

      <div class="awards-grid">
        <div class="award-item">
          <img src="/img/design-awards-2025.svg" alt="Award 1" />
          <p>Best Real Estate Platform 2025</p>
        </div>
      </div>
    </section>

  </div>
</template>

<script>
export default {
  name: "Home",
};
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: white;
}

/* Banner with image */
.hero-banner {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 0 0 20px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.4), rgba(0,0,0,0.1));
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 2rem;
  color: #fff;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
}

/* Intro section */
.intro {
  max-width: 800px;
  margin: 3rem auto;
  padding: 0 1rem;
}

.intro h2 {
  color: #343a40;
  margin-bottom: 1rem;
}

.intro p {
  color: #6c757d;
}

.awards {
  max-width: 900px;
  margin: 3rem auto;
  padding: 0 1rem;
}

.awards h2 {
  color: #343a40;
  margin-bottom: 0.5rem;
}

.awards p.lead {
  color: #6c757d;
  margin-bottom: 2rem;
}

.awards-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2rem;
}

.award-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 150px;
}

.award-item img {
  width: 120px;
  height: auto;
  margin-bottom: 0.5rem;
}

.award-item p {
  font-size: 0.9rem;
  color: #495057;
}

</style>
