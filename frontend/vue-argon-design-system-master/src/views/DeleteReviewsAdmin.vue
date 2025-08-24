<template>
  <section class="list-reviews section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center mb-4" style="color: #343a40;">📝 All Reviews</h2>

      <div v-if="loading" class="text-center text-white">Loading reviews...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load reviews.</div>
      <div v-else>
        <!-- Search bar -->
        <div class="search-bar">
          <input
              v-model="searchQuery"
              type="search"
              class="form-control"
              placeholder="Search by id, renterId, rating, content, propertyId, date…"
          />
          <button
              v-if="searchQuery"
              class="btn btn-light"
              @click="searchQuery = ''"
              title="Clear"
          >
            ✕
          </button>
        </div>

        <!-- Counter -->
        <div class="result-info" v-if="reviews.length > 0">
          <small class="text-white-50">
            {{ filteredReviews.length }} / {{ reviews.length }} results
          </small>
        </div>

        <!-- Empty states -->
        <div v-if="filteredReviews.length === 0" class="text-center text-white mt-3">
          {{ searchQuery ? 'No results for your search.' : 'No reviews found.' }}
        </div>

        <!-- List -->
        <ul v-else class="review-list">
          <li v-for="review in filteredReviews" :key="review.id" class="review-card">
            <div class="review-info">
              <p><strong>Review ID:</strong> {{ review.id }}</p>
              <p><strong>Renter ID:</strong> {{ review.renterId }}</p>
              <p><strong>Content:</strong> {{ review.content }}</p>
              <p><strong>Rating:</strong> {{ review.rating }} ⭐</p>
              <!-- <p><strong>Property ID:</strong> {{ review.propertyId }}</p> -->
              <p><strong>Created At:</strong> {{ formatDateTime(review.createdAt) }}</p>

              <div class="review-actions mt-2" v-if="isAdmin">
                <button class="btn btn-danger" @click="deleteReview(review.id)">
                  🗑️ Delete Review
                </button>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: "ListReviewsAdmin",
  data() {
    return {
      reviews: [],
      loading: false,
      error: false,
      baseURL: 'http://localhost:8080',
      searchQuery: ""
    };
  },
  computed: {
    isAdmin() {
      return (localStorage.getItem('userRole') || '').toUpperCase() === 'ADMIN';
    },
    filteredReviews() {
      const q = this.searchQuery.trim().toLowerCase();
      if (!q) return this.reviews;

      return this.reviews.filter(r => {
        const fields = [
          r.id,
          r.renterId,
          r.propertyId,
          r.rating,
          r.content,
          this.formatDateTime(r.createdAt)
        ]
            .filter(v => v !== null && v !== undefined)
            .map(v => String(v).toLowerCase());

        return fields.some(f => f.includes(q));
      });
    }
  },
  methods: {
    formatDateTime(dateStr) {
      if (!dateStr) return 'N/A';
      const options = {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
      };
      return new Date(dateStr).toLocaleString(undefined, options);
    },

    async fetchReviews() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `${this.baseURL}/api/properties/reviews/all`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.reviews = response.data;
      } catch (err) {
        console.error("Error fetching reviews:", err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    async deleteReview(reviewId) {
      if (!confirm("Are you sure you want to delete this review?")) return;

      try {
        const token = localStorage.getItem("token");
        await axios.delete(`${this.baseURL}/api/properties/reviews/${reviewId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.reviews = this.reviews.filter(r => r.id !== reviewId);
        alert("Review deleted successfully.");
      } catch (err) {
        console.error("Error deleting review:", err);
        alert("Failed to delete review. Try again.");
      }
    }
  },
  mounted() {
    this.fetchReviews();
  }
};
</script>

<style scoped>
.section { min-height: 100vh; }

.search-bar {
  display: flex;
  gap: .5rem;
  max-width: 640px;
  margin: 0 auto 1rem;
}
.search-bar .form-control {
  flex: 1;
}
.result-info {
  text-align: center;
  margin-bottom: .5rem;
}

.review-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 900px;
}

.review-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.review-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.review-info p { margin: 5px 0; color: #555; font-size: 16px; }

.review-actions { display: flex; gap: 8px; margin-top: 10px; }
.btn-danger { background-color: #dc3545; border: none; color: #fff; cursor: pointer; padding: .5rem 1rem; border-radius: .3rem; }
.btn-danger:hover { background-color: #c82333; }

.text-center { text-align: center; }
.text-white { color: white; }
.text-danger { color: #dc3545; }
.bg-secondary { background-color: #343a40; }
.py-5 { padding-top: 3rem; padding-bottom: 3rem; }
</style>
