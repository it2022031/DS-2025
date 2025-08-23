<template>
  <section class="list-rentals section bg-secondary py-5">
    <div class="container">
      <h2 class="text-center text-white mb-4">📖 Rentals List</h2>

      <div v-if="loading" class="text-center text-white">Loading rentals...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load rentals.</div>
      <div v-else-if="rentals.length === 0" class="text-center text-white">No rentals found.</div>

      <ul v-else class="rental-list">
        <li v-for="rental in rentals" :key="rental.rentalId" class="rental-card">
          <div class="rental-info">
            <h3>Booking ID: {{ rental.rentalId }}</h3>
            <p><strong>Property's Name:</strong> {{ rental.propertyName }}</p>
            <p><strong>Owner's Name:</strong> {{ rental.ownerName }}</p>
            <p><strong>Check-In Date:</strong> {{ formatDate(rental.startDate) }}</p>
            <p><strong>Check-Out Date:</strong> {{ formatDate(rental.endDate) }}</p>
            <p><strong>Total Price:</strong> {{ rental.totalPrice }}<strong> €</strong></p>
            <p>
              <strong>Approval Status (by Owner):</strong>
              <span :class="statusClass(rental.status)">
                {{ rental.status }}
              </span>
            </p>
          </div>

          <!-- Review form: only if eligible and no review exists -->
          <div v-if="canLeaveReview(rental) && !submittedReviews[rental.rentalId]" class="review-section mt-3">
            <h4 class="mb-2">Leave a Review</h4>

            <textarea
                v-model="reviews[rental.rentalId].content"
                placeholder="Write your review here..."
                rows="3"
                class="form-control mb-2"
            ></textarea>

            <select v-model="reviews[rental.rentalId].rating" class="form-control mb-2">
              <option disabled value="">Select rating</option>
              <option v-for="n in 5" :key="n" :value="n">{{ n }} ⭐</option>
            </select>

            <button
                class="btn btn-success"
                @click="submitReview(rental)"
                :disabled="submitting[rental.rentalId]"
            >
              {{ submitting[rental.rentalId] ? 'Submitting...' : 'Submit Review' }}
            </button>

            <p v-if="success[rental.rentalId]" class="text-success mt-2">
              ✅ Review submitted successfully!
            </p>
            <p v-if="reviewError[rental.rentalId]" class="text-danger mt-2">
              ❌ Failed to submit review. Try again.
            </p>
          </div>

          <!-- Show submitted review -->
          <div v-if="submittedReviews[rental.rentalId]" class="submitted-review mt-3">
            <h4 class="mb-2">Your Review</h4>

            <!-- Edit mode -->
            <div v-if="editing[rental.rentalId]">
              <textarea
                  v-model="editReviews[rental.rentalId].content"
                  rows="3"
                  class="form-control mb-2"
              ></textarea>

              <select v-model="editReviews[rental.rentalId].rating" class="form-control mb-2">
                <option disabled value="">Select rating</option>
                <option v-for="n in 5" :key="n" :value="n">{{ n }} ⭐</option>
              </select>

              <button
                  class="btn btn-success mr-2"
                  @click="updateReview(rental)"
                  :disabled="submittingEdit[rental.rentalId]"
              >
                {{ submittingEdit[rental.rentalId] ? 'Saving...' : 'Save Changes' }}
              </button>
              <button class="btn btn-light" @click="cancelEdit(rental)">Cancel</button>
            </div>

            <!-- View mode -->
            <div v-else>
              <p><strong>Rating:</strong> {{ submittedReviews[rental.rentalId].rating }} ⭐</p>
              <p><strong>Content:</strong> {{ submittedReviews[rental.rentalId].content }}</p>
              <p class="text-muted">
                <em>Submitted at: {{ formatDateTime(submittedReviews[rental.rentalId].createdAt) }}</em>
              </p>

              <button class="btn btn-light mr-2" @click="startEdit(rental)">✏️ Edit Review</button>
              <button class="btn btn-danger" @click="deleteReview(rental)">🗑️ Delete Review</button>
            </div>

          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ListRentals',
  data() {
    return {
      rentals: [],
      loading: false,
      error: false,
      baseURL: 'http://localhost:8080',
      userId: localStorage.getItem('userId'),

      reviews: {},            // form state for new reviews
      submittedReviews: {},   // saved reviews per rental
      editReviews: {},        // form state for editing
      editing: {},            // track if rental is in edit mode

      submitting: {},         // submitting new review
      submittingEdit: {},     // submitting edit
      success: {},
      reviewError: {}
    };
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      return new Date(dateStr).toLocaleDateString(undefined, options);
    },
    formatDateTime(dateStr) {
      if (!dateStr) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(dateStr).toLocaleString(undefined, options);
    },
    statusClass(status) {
      if (!status) return '';
      switch (status.toLowerCase()) {
        case 'approved': return 'approved';
        case 'pending': return 'pending';
        case 'rejected': return 'rejected';
        default: return '';
      }
    },
    canLeaveReview(rental) {
      return (
          rental.status &&
          rental.status.toLowerCase() === 'approved' &&
          new Date(rental.endDate) < new Date()
      );
    },
    async fetchRentals() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem('token');

        // Fetch rentals
        const rentalsResponse = await axios.get(
            `${this.baseURL}/api/rentals/by-renter/${this.userId}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.rentals = rentalsResponse.data;

        // Fetch renter's submitted reviews
        const reviewsResponse = await axios.get(
            `${this.baseURL}/api/properties/user/${this.userId}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        const renterReviews = reviewsResponse.data;

        // Initialize states and map reviews
        this.rentals.forEach(rental => {
          const review = renterReviews.find(r => r.rentalId === rental.rentalId);
          if (review) {
            this.$set(this.submittedReviews, rental.rentalId, review);
            this.$set(this.editReviews, rental.rentalId, {
              content: review.content,
              rating: review.rating
            });
          } else {
            this.$set(this.reviews, rental.rentalId, { content: '', rating: '' });
          }

          this.$set(this.submitting, rental.rentalId, false);
          this.$set(this.submittingEdit, rental.rentalId, false);
          this.$set(this.success, rental.rentalId, false);
          this.$set(this.reviewError, rental.rentalId, false);
          this.$set(this.editing, rental.rentalId, false);
        });
      } catch (err) {
        console.error('Error fetching rentals or reviews:', err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },
    async submitReview(rental) {
      const { content, rating } = this.reviews[rental.rentalId];
      if (!content || !rating) {
        alert('Please write a review and select a rating.');
        return;
      }

      this.submitting[rental.rentalId] = true;
      this.success[rental.rentalId] = false;
      this.reviewError[rental.rentalId] = false;

      try {
        const token = localStorage.getItem('token');
        const response = await axios.post(
            `${this.baseURL}/api/properties/${rental.propertyId}/reviews`,
            { content, rating, rentalId: rental.rentalId },
            { headers: { Authorization: `Bearer ${token}` } }
        );

        this.submittedReviews[rental.rentalId] = response.data;
        this.success[rental.rentalId] = true;
        this.reviews[rental.rentalId] = { content: '', rating: '' };
      } catch (err) {
        console.error('Error submitting review:', err);
        this.reviewError[rental.rentalId] = true;
      } finally {
        this.submitting[rental.rentalId] = false;
      }
    },
    startEdit(rental) {
      const review = this.submittedReviews[rental.rentalId];
      this.editReviews[rental.rentalId] = {
        content: review.content,
        rating: review.rating
      };
      this.editing[rental.rentalId] = true;
    },
    cancelEdit(rental) {
      this.editing[rental.rentalId] = false;
    },
    async deleteReview(rental) {
      const reviewId = this.submittedReviews[rental.rentalId].id;
      if (!confirm('Are you sure you want to delete this review?')) return;

      try {
        const token = localStorage.getItem('token');
        await axios.delete(`${this.baseURL}/api/properties/reviews/${reviewId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        // Remove locally so form reappears
        this.$set(this.submittedReviews, rental.rentalId, null);
        this.$set(this.reviews, rental.rentalId, { content: '', rating: '' });
        alert('Review deleted successfully.');
      } catch (err) {
        console.error('Error deleting review:', err);
        alert('Failed to delete review. Try again.');
      }
    },
    async updateReview(rental) {
      const { content, rating } = this.editReviews[rental.rentalId];
      if (!content || !rating) {
        alert('Please write a review and select a rating.');
        return;
      }

      this.submittingEdit[rental.rentalId] = true;
      try {
        const token = localStorage.getItem('token');
        const reviewId = this.submittedReviews[rental.rentalId].id;

        const response = await axios.patch(
            `${this.baseURL}/api/properties/reviews/${reviewId}`,
            { content, rating },
            { headers: { Authorization: `Bearer ${token}` } }
        );

        this.submittedReviews[rental.rentalId] = response.data;
        this.editing[rental.rentalId] = false;
      } catch (err) {
        console.error('Error updating review:', err);
        alert('Failed to update review. Try again.');
      } finally {
        this.submittingEdit[rental.rentalId] = false;
      }
    }
  },
  async mounted() {
    await this.fetchRentals();
  }
};
</script>

<style scoped>
.section {
  min-height: 100vh;
}

.rental-list {
  list-style: none;
  padding: 0;
  margin: 0 auto;
  max-width: 900px;
}

.rental-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.rental-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.rental-info h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #2c3e50;
}

.rental-info p {
  margin: 5px 0;
  color: #555;
  font-size: 16px;
}

.review-section,
.submitted-review {
  margin-top: 15px;
  padding: 15px;
  border-top: 1px solid #eee;
  background: #f9f9f9;
  border-radius: 8px;
}

textarea.form-control,
select.form-control {
  width: 100%;
  padding: 10px;
  font-size: 15px;
  border: 1px solid #ccc;
  border-radius: 8px;
}

textarea.form-control:focus,
select.form-control:focus {
  outline: none;
  border-color: #28a745;
  box-shadow: 0 0 5px rgba(40, 167, 69, 0.3);
}

.approved {
  color: #28a745;
  font-weight: bold;
}

.pending {
  color: #ffc107;
  font-weight: bold;
}

.rejected {
  color: #dc3545;
  font-weight: bold;
}

.text-success {
  color: #28a745 !important;
}

.text-danger {
  color: #dc3545 !important;
}
</style>
