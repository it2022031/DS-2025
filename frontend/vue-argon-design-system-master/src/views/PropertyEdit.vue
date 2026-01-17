<template>
  <section class="edit-property section bg-light py-5">
    <div class="container">
      <div class="card edit-card shadow-sm p-4">
        <h2 class="mb-4 text-center">Edit Property #{{ property && property.id }}</h2>

        <div v-if="loading" class="text-center">Loading...</div>
        <div v-else-if="error" class="text-danger text-center">Failed to load property.</div>

        <div v-else-if="property">
          <form @submit.prevent="saveProperty">
            <div class="mb-3">
              <label class="form-label">Name</label>
              <input v-model="property.name" type="text" class="form-control rounded-input" required />
            </div>

            <div class="mb-3">
              <label class="form-label">Description</label>
              <textarea v-model="property.description" class="form-control rounded-input" rows="4"></textarea>
            </div>

            <div class="row g-3">
              <div class="col-md-4">
                <label class="form-label">City</label>
                <input v-model="property.city" type="text" class="form-control rounded-input" required />
              </div>
              <div class="col-md-4">
                <label class="form-label">Country</label>
                <input v-model="property.country" type="text" class="form-control rounded-input" required />
              </div>
              <div class="col-md-4">
                <label class="form-label">Street</label>
                <input v-model="property.street" type="text" class="form-control rounded-input" />
              </div>
            </div>

            <div class="row g-3 mt-3">
              <div class="col-md-4">
                <label class="form-label">Postal Code</label>
                <input v-model="property.postalCode" type="text" class="form-control rounded-input" />
              </div>
              <div class="col-md-4">
                <label class="form-label">Square Meters</label>
                <input v-model.number="property.squareMeters" type="number" class="form-control rounded-input" min="0" />
              </div>
              <div class="col-md-4">
                <label class="form-label">Price Per Day</label>
                <input v-model.number="property.price" type="number" class="form-control rounded-input" min="0" />
              </div>
              <div class="col-md-4 mt-2">
                <label class="form-label">Approval Status</label>
                <select v-if="userRole === 'ADMIN'" v-model="property.approvalStatus" class="form-select rounded-input">
                  <option value="PENDING">Pending</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                </select>
                <div v-else class="form-control-plaintext">{{ property.approvalStatus }}</div>
              </div>
            </div>

            <div class="mt-4">
              <label class="form-label">Upload Property Media</label>

              <input
                  type="file"
                  multiple
                  accept="image/*"
                  @change="handlePhotoChange"
                  class="form-control rounded-input mb-2"
              />

              <button
                  type="button"
                  class="btn btn-outline-primary rounded-btn"
                  @click="uploadPhotos"
                  :disabled="!selectedPhotos.length"
              >
                Upload Photos
              </button>
            </div>

            <!-- Existing Photos -->
            <div class="mt-4" v-if="photos.length">
              <h6>Existing Photos:</h6>
              <div class="d-flex flex-wrap">
                <div
                    v-for="(photo, idx) in photos"
                    :key="photo.id"
                    class="photo-card position-relative me-2 mb-2 shadow-sm rounded"
                >
                  <img :src="photo.url" :alt="photo.filename" width="100" height="100" class="rounded" />
                  <span v-if="photo.id === coverId || (!property.coverPhotoId && idx === 0)" class="cover-badge">
                    Cover
                  </span>

                  <div class="photo-actions">
                    <button type="button" @click="triggerEditPhoto(photo.id)" class="btn btn-sm btn-warning" title="Replace photo">
                      ✏️
                    </button>
                    <button type="button" @click="deletePhoto(photo.id)" class="btn btn-sm btn-danger" title="Delete photo">
                      🗑
                    </button>
                  </div>

                  <div v-if="replacingId === photo.id" class="overlay">Updating…</div>

                  <input
                      type="file"
                      :ref="`editInput_${photo.id}`"
                      accept="image/*"
                      class="d-none"
                      @change="handleEditPhotoChange($event, photo.id)"
                  />
                </div>
              </div>
            </div>

            <!-- PDF -->
            <div class="mt-4">
              <label class="form-label">Property Document (PDF)</label>

              <div v-if="!document">
                <input
                    type="file"
                    accept="application/pdf"
                    class="form-control rounded-input mb-2"
                    @change="handlePdfChange"
                />

                <button
                    type="button"
                    class="btn btn-primary rounded-btn"
                    @click="uploadPdf"
                    :disabled="!selectedPdf"
                >
                  Upload PDF
                </button>
              </div>

              <div v-else class="mt-2">
                <div class="d-flex align-items-center gap-3 mb-2">
                  <div class="form-control rounded-input bg-light">
                    📄 {{ document.filename }}
                  </div>

                  <button type="button" class="btn btn-outline-primary rounded-btn" @click="viewPdf">
                    View PDF
                  </button>
                </div>

                <div class="d-flex align-items-center gap-2">
                  <input
                      type="file"
                      accept="application/pdf"
                      class="form-control rounded-input"
                      @change="handlePdfChange"
                  />

                  <button
                      type="button"
                      class="btn btn-warning rounded-btn"
                      :disabled="!selectedPdf"
                      @click="uploadPdf"
                  >
                    Replace PDF
                  </button>
                </div>
              </div>
            </div>

            <div class="mt-4 d-flex justify-content-end">
              <router-link to="/list-properties" class="btn btn-light me-3 rounded-btn">
                Cancel
              </router-link>
              <button type="submit" class="btn btn-primary rounded-btn">Save Changes</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: "PropertyEdit",
  data() {
    return {
      property: { approvalStatus: "" },
      loading: false,
      error: false,

      selectedPhotos: [],
      previewPhotos: [],

      photos: [],
      replacingId: null,

      selectedPdf: null,
      document: null, // { filename, url }
    };
  },
  computed: {
    userRole() {
      return (localStorage.getItem("userRole") || "").toUpperCase();
    },
    userId() {
      return Number(localStorage.getItem("userId"));
    },
    coverId() {
      return this.property.coverPhotoId || (this.photos[0] && this.photos[0].id) || null;
    },
  },
  async mounted() {
    this.loading = true;
    const id = this.$route.params.id;

    try {
      const response = await this.$api.get(`/properties/${id}`);
      this.property = response.data;

      await this.fetchPhotos();
      await this.fetchDocument();
    } catch (err) {
      console.error("Error loading property:", err);
      this.error = true;
    } finally {
      this.loading = false;
    }
  },
  methods: {
    // keep the correct port/origin always (e.g. :8090)
    photoUrl(photoId) {
      return `${window.location.origin}/api/properties/photos/${photoId}`;
    },
    documentUrl() {
      return `${window.location.origin}/api/properties/${this.property.id}/document`;
    },

    async saveProperty() {
      const payload = {
        name: this.property.name,
        price: this.property.price,
        description: this.property.description,
        city: this.property.city,
        country: this.property.country,
        street: this.property.street,
        postalCode: this.property.postalCode,
        squareMeters: this.property.squareMeters,
      };

      try {
        const response = await this.$api.patch(`/properties/${this.property.id}`, payload);
        this.property = response.data;
        alert("Property updated successfully!");
      } catch (err) {
        console.error("Error saving property:", err);
        alert("Failed to save changes.");
      }
    },

    handlePhotoChange(event) {
      this.selectedPhotos = Array.from(event.target.files || []);
      this.previewPhotos = [];
      this.selectedPhotos.forEach((file) => {
        const reader = new FileReader();
        reader.onload = (e) => this.previewPhotos.push(e.target.result);
        reader.readAsDataURL(file);
      });
    },

    async uploadPhotos() {
      if (!this.selectedPhotos.length) return;

      const formData = new FormData();
      this.selectedPhotos.forEach((file) => formData.append("file", file));

      try {
        await this.$api.post(`/properties/${this.property.id}/photos`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });

        this.selectedPhotos = [];
        this.previewPhotos = [];
        alert("✅ Photos uploaded successfully!");
        await this.fetchPhotos();
      } catch (err) {
        console.error("❌ Error uploading photos:", err);
        alert("Failed to upload photos.");
      }
    },

    async fetchPhotos() {
      try {
        const response = await this.$api.get(`/properties/${this.property.id}/photos`);
        this.photos = (response.data || []).map((file) => ({
          ...file,
          baseUrl: this.photoUrl(file.id),
          url: this.photoUrl(file.id),
        }));
      } catch (err) {
        console.error("Error fetching photos:", err);
        this.photos = [];
      }
    },

    async deletePhoto(photoId) {
      if (!confirm("Are you sure you want to delete this photo?")) return;

      try {
        await this.$api.delete(`/properties/photos/${photoId}`);
        alert("Photo deleted ✅");
        await this.fetchPhotos();
      } catch (err) {
        console.error("Error deleting photo:", err);
        alert("Failed to delete photo.");
      }
    },

    triggerEditPhoto(photoId) {
      const ref = this.$refs[`editInput_${photoId}`];
      const el = Array.isArray(ref) ? ref[0] : ref;
      if (el) el.click();
    },

    async handleEditPhotoChange(event, photoId) {
      const file = event.target.files && event.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append("file", file);

      this.replacingId = photoId;

      try {
        await this.$api.patch(`/properties/photos/${photoId}`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });

        // cache bust on this specific image
        const idx = this.photos.findIndex((p) => p.id === photoId);
        if (idx !== -1) {
          const base = this.photos[idx].baseUrl || this.photos[idx].url || this.photoUrl(photoId);
          this.$set(this.photos, idx, {
            ...this.photos[idx],
            baseUrl: base,
            url: `${base}?t=${Date.now()}`,
          });
        } else {
          await this.fetchPhotos();
        }

        event.target.value = "";
      } catch (err) {
        console.error("❌ Error replacing photo:", err);
        alert("Αποτυχία αντικατάστασης φωτογραφίας.");
      } finally {
        this.replacingId = null;
      }
    },

    handlePdfChange(event) {
      const file = event.target.files && event.target.files[0];
      if (!file) return;

      if (file.type !== "application/pdf") {
        alert("Please select a PDF file.");
        event.target.value = "";
        return;
      }
      this.selectedPdf = file;
    },

    async uploadPdf() {
      if (!this.selectedPdf) return;

      const formData = new FormData();
      formData.append("file", this.selectedPdf);

      try {
        const response = await this.$api.post(`/properties/${this.property.id}/document`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });

        const fileKey = response.data?.fileKey || "";
        const filename = fileKey ? fileKey.split("_").slice(1).join("_") : "Document Uploaded";

        this.document = {
          filename,
          url: this.documentUrl(),
        };

        this.selectedPdf = null;
      } catch (err) {
        console.error("Failed to upload PDF:", err);
        alert("Failed to upload PDF.");
      }
    },

    async fetchDocument() {
      try {
        // if exists, backend returns blob
        await this.$api.get(`/properties/${this.property.id}/document`, {
          responseType: "blob",
        });

        this.document = {
          filename: this.document?.filename || "Document Already Uploaded",
          url: this.documentUrl(),
        };
      } catch (err) {
        // 404 -> no document
        this.document = null;
      }
    },

    async viewPdf() {
      try {
        const response = await this.$api.get(`/properties/${this.property.id}/document`, {
          responseType: "blob",
        });

        const blob = new Blob([response.data], { type: "application/pdf" });
        const url = window.URL.createObjectURL(blob);
        window.open(url, "_blank");

        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
      } catch (err) {
        console.error("Error opening PDF:", err);
        alert("Unable to open PDF.");
      }
    },
  },
};
</script>

<style scoped>
.section { min-height: 100vh; }
img { object-fit: cover; }

.position-relative { position: relative; }
.border { border: 1px solid #ccc; }
.rounded { border-radius: 4px; }
.me-2 { margin-right: 0.5rem; }
.mb-2 { margin-bottom: 0.5rem; }
.btn-sm { font-size: 0.7rem; padding: 0.2rem 0.4rem; }

.photo-card { width: 100px; height: 100px; }
.photo-actions {
  position: absolute;
  top: 6px;
  right: 6px;
  display: flex;
  gap: 6px;
}

.cover-badge {
  position: absolute;
  left: 6px;
  bottom: 6px;
  background: #198754;
  color: #fff;
  font-size: 0.65rem;
  padding: 2px 6px;
  border-radius: 999px;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  border-radius: 4px;
}

.d-none { display: none; }

.edit-card {
  background: #fff;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.edit-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.12);
}

.rounded-input {
  border-radius: 0.75rem;
  padding: 0.5rem 1rem;
  border: 1px solid #ced4da;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.rounded-input:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13,110,253,0.15);
}

.rounded-btn {
  border-radius: 25px;
  padding: 0.6rem 1.5rem;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.rounded-btn:hover {
  transform: translateY(-2px);
}

.photo-card {
  width: 100px;
  height: 100px;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  transition: transform 0.2s ease;
}

.photo-card:hover {
  transform: scale(1.05);
}
</style>
