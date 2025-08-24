<template>
  <section class="property-details section bg-light py-5">
    <div class="container">
      <div v-if="loading" class="text-center">Loading property...</div>
      <div v-else-if="error" class="text-center text-danger">Failed to load property.</div>
      <div v-else-if="!property" class="text-center">Property not found.</div>
      <div v-else class="row">
        <!-- Images -->
        <div class="col-md-6">
          <img :src="mainPhotoUrl" :alt="property.name" class="img-fluid rounded shadow mb-3" />
          <div v-if="property.photos && property.photos.length > 1" class="d-flex flex-wrap">
            <img
                v-for="photo in property.photos"
                :key="photo.id"
                :src="photo.url"
                :alt="photo.filename"
                class="border rounded me-2 mb-2 thumbnail"
                @click="setMainPhoto(photo.url)"
            />
          </div>
        </div>

        <!-- Info -->
        <div class="col-md-6">
          <h2>{{ property.name }}</h2>
          <p><strong>Owner:</strong> 👤 {{ property.ownerFirstName }} {{ property.ownerLastName }}</p>
          <p>{{ property.description }}</p>
          <p><strong>Location:</strong> {{ property.city }}, {{ property.country }}</p>
          <p><strong>Size:</strong> {{ property.squareMeters }} m²</p>
          <p><strong>Address:</strong> {{ property.street }}, {{ property.postalCode }}</p>
          <p><strong>Price per day:</strong> {{ property.price }} €</p>

          <!-- Date range picker -->
          <div class="form-group mb-2">
            <label class="form-label">Select dates</label>
            <flat-pickr
                v-model="dateRange"
                :config="flatpickrConfig"
                class="form-control"
                placeholder="Choose check-in and check-out"
            />
            <small class="text-muted d-block mt-1">
              Οι γκρι ημερομηνίες στο ημερολόγιο είναι κλειστές/μη διαθέσιμες.
            </small>
          </div>

          <!-- Derived dates -->
          <div class="row">
            <div class="col">
              <div class="form-group mb-2">
                <label>Check-in Date</label>
                <input type="date" class="form-control" :value="checkinDate" readonly />
              </div>
            </div>
            <div class="col">
              <div class="form-group mb-2">
                <label>Check-out Date</label>
                <input type="date" class="form-control" :value="checkoutDate" readonly />
              </div>
            </div>
          </div>

          <!-- Availability -->
          <div v-if="checkinDate && checkoutDate" class="mt-2">
            <span v-if="isAvailable(property.id, checkinDate, checkoutDate)" class="text-success">
              ✅ Available for your dates
            </span>
            <span v-else class="text-danger">❌ Not available for your dates</span>
          </div>

          <!-- Total -->
          <div v-if="nights > 0" class="mt-2 total-box">
            <div><strong>Nights:</strong> {{ nights }}</div>
            <div><strong>Price/night:</strong> {{ property.price }} €</div>
            <div class="h5 m-0"><strong>Total:</strong> {{ totalPrice }} €</div>
          </div>

          <!-- Action -->
          <button
              class="btn btn-primary mt-3"
              :disabled="nights <= 0 || !isAvailable(property.id, checkinDate, checkoutDate)"
              @click="bookProperty"
          >
            Book Now<span v-if="totalPrice"> – {{ totalPrice }} €</span>
          </button>

          <!-- Reviews -->
          <div class="reviews mt-4">
            <h4>Reviews</h4>
            <div v-if="reviews.length === 0">No reviews yet.</div>
            <div v-else>
              <div v-for="review in reviews" :key="review.id" class="card mb-2">
                <div class="card-body">
                  <p class="card-text">{{ review.content }}</p>
                  <small class="text-muted">Rating: {{ review.rating }}/5</small><br />
                  <small class="text-muted">By renter #{{ review.renterId }} on {{ formatDate(review.createdAt) }}</small>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </section>
</template>

<script>
import axios from "axios";
import FlatPickr from "vue-flatpickr-component";
import "flatpickr/dist/flatpickr.css";

export default {
  name: "PropertyDetails",
  components: { flatPickr: FlatPickr },
  data() {
    return {
      property: null,
      loading: false,
      error: false,
      // dates
      dateRange: null,
      checkinDate: "",
      checkoutDate: "",
      // κρατάμε Y-M-D strings για να αποφύγουμε timezone bugs
      occupiedRanges: [], // [{ startYMD:'2025-08-11', endYMD:'2025-08-12' }, ...]
      // ui
      mainPhotoUrl: "/default-property.jpg",
      reviews: [],
      baseURL: "http://localhost:8080",
      flatpickrConfig: {
        mode: "range",
        dateFormat: "Y-m-d",
        minDate: "today",   // μπλοκάρει παρελθόν
        disable: [],        // γεμίζει από occupiedRanges
        onChange: (selectedDates) => {
          if (selectedDates.length === 2) {
            this.checkinDate = this.formatYMD(selectedDates[0]);
            this.checkoutDate = this.formatYMD(selectedDates[1]);
          } else if (selectedDates.length === 1) {
            this.checkinDate = this.formatYMD(selectedDates[0]);
            this.checkoutDate = "";
          } else {
            this.checkinDate = "";
            this.checkoutDate = "";
          }
        }
      }
    };
  },
  computed: {
    nights() {
      if (!this.checkinDate || !this.checkoutDate) return 0;
      const start = this.toStartUTC(this.checkinDate);
      const end = this.toStartUTC(this.checkoutDate);
      const diff = (end - start) / (1000 * 60 * 60 * 24);
      return diff > 0 ? diff : 0;
    },
    totalPrice() {
      const price = Number(this.property?.price || 0);
      return this.nights > 0 ? this.nights * price : 0;
    }
  },
  methods: {
    // ---------- helpers ----------
    formatYMD(d) {
      const y = d.getUTCFullYear();
      const m = String(d.getUTCMonth() + 1).padStart(2, "0");
      const day = String(d.getUTCDate()).padStart(2, "0");
      return `${y}-${m}-${day}`;
    },
    todayYMD() {
      const now = new Date();
      const y = now.getUTCFullYear();
      const m = String(now.getUTCMonth() + 1).padStart(2, "0");
      const d = String(now.getUTCDate()).padStart(2, "0");
      return `${y}-${m}-${d}`;
    },
    toStartUTC(ymd) {
      // ymd: 'YYYY-MM-DD' -> Date at 00:00:00 UTC
      const [y, m, d] = ymd.split("-").map(n => parseInt(n, 10));
      return new Date(Date.UTC(y, m - 1, d));
    },
    addDaysUTC(date, days) {
      const t = new Date(date.getTime());
      t.setUTCDate(t.getUTCDate() + days);
      return t;
    },

    // ---------- api ----------
    async fetchProperty() {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get(
            `${this.baseURL}/api/properties/${this.$route.params.id}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.property = response.data;

        // Occupied (κρατάμε strings)
        const datesRes = await axios.get(
            `${this.baseURL}/api/properties/${this.property.id}/closed-dates`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        this.occupiedRanges = datesRes.data.map(d => ({
          startYMD: d.startDate, // 'YYYY-MM-DD'
          endYMD: d.endDate
        }));

        // Δίνουμε στο flatpickr απευθείας strings
        this.flatpickrConfig = {
          ...this.flatpickrConfig,
          disable: this.occupiedRanges.map(p => ({ from: p.startYMD, to: p.endYMD }))
        };

        // Photos
        try {
          const photosRes = await axios.get(
              `${this.baseURL}/api/properties/${this.property.id}/photos`,
              { headers: { Authorization: `Bearer ${token}` } }
          );
          this.property.photos = photosRes.data.map(photo => ({
            ...photo,
            url: `${this.baseURL}/api/properties/photos/${photo.id}`
          }));
          if (this.property.photos && this.property.photos.length > 0) {
            this.mainPhotoUrl = this.property.photos[0].url;
          }
        } catch (err) {
          console.error("Error fetching property photos:", err);
          this.property.photos = [];
        }

        // Reviews
        try {
          const reviewsRes = await axios.get(
              `${this.baseURL}/api/properties/${this.property.id}/reviews`,
              { headers: { Authorization: `Bearer ${token}` } }
          );
          this.reviews = reviewsRes.data;
        } catch (err) {
          console.error("Error fetching reviews:", err);
          this.reviews = [];
        }

      } catch (err) {
        console.error(err);
        this.error = true;
      } finally {
        this.loading = false;
      }
    },

    // ---------- logic ----------
    isAvailable(propertyId, checkin, checkout) {
      if (!checkin || !checkout) return false;

      // no past/invalid ranges
      const today = this.toStartUTC(this.todayYMD());
      const inDate = this.toStartUTC(checkin);
      const outDate = this.toStartUTC(checkout);
      if (inDate < today || outDate <= inDate) return false;

      // Treat checkout as exclusive -> compare [in, out) με [start, end+1)
      return !this.occupiedRanges.some(r => {
        const start = this.toStartUTC(r.startYMD);
        const endExclusive = this.addDaysUTC(this.toStartUTC(r.endYMD), 1);
        return inDate < endExclusive && outDate > start; // overlap?
      });
    },

    async bookProperty() {
      const today = this.toStartUTC(this.todayYMD());
      const inDate = this.toStartUTC(this.checkinDate || "");
      if (!this.checkinDate || !this.checkoutDate || inDate < today) {
        alert("Δεν επιτρέπονται παρελθοντικές ημερομηνίες.");
        return;
      }
      if (this.nights <= 0 || !this.isAvailable(this.property.id, this.checkinDate, this.checkoutDate)) {
        alert("Παρακαλώ επίλεξε έγκυρες διαθέσιμες ημερομηνίες.");
        return;
      }

      try {
        const token = localStorage.getItem("token");
        const rentalData = {
          propertyId: this.property.id,
          startDate: this.checkinDate,
          endDate: this.checkoutDate
        };
        const response = await axios.post(
            `${this.baseURL}/api/rentals/add`,
            rentalData,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        alert("✅ Rental created successfully!");
        console.log("Rental created:", response.data);
        await this.fetchProperty();
      } catch (err) {
        console.error("Failed to create rental:", err);
        alert("❌ Failed to create rental. Please try again.");
      }
    },

    setMainPhoto(url) {
      this.mainPhotoUrl = url;
    },

    formatDate(dateStr) {
      const options = { year: "numeric", month: "long", day: "numeric" };
      return new Date(dateStr).toLocaleDateString("el-GR", options);
    }
  },
  created() {
    this.fetchProperty();
  }
};
</script>

<style scoped>
.section { min-height: 100vh; }

img { max-height: 350px; object-fit: cover; }

.thumbnail {
  width: 70px; height: 70px; object-fit: cover;
  cursor: pointer; transition: transform 0.2s ease;
}
.thumbnail:hover { transform: scale(1.05); }

.reviews .card { background-color: #f8f9fa; }

.total-box {
  background: #fff; border: 1px solid #e9ecef;
  border-radius: 0.5rem; padding: 0.6rem 0.8rem;
}
</style>
