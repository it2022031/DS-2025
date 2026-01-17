<template>
  <div class="profile-page text-center mt-5">
    <div class="avatar-container mb-3">
      <img
          :src="user.avatar || defaultAvatar"
          alt="Avatar"
          class="avatar clickable"
          @click="triggerFileInput"
      />
      <input type="file" ref="fileInput" @change="onAvatarChange" hidden />
    </div>

    <h3 @click.stop="toggleDropdown" class="profile-name clickable">
      {{ user.name || "Unknown User" }}
      <i class="ni ni-bold-down ml-2"></i>
    </h3>

    <div v-show="showDropdown" class="custom-dropdown mt-2 text-left">
      <!-- Properties Section -->
      <div class="mt-4 text-left">
        <h5>My Profile</h5>

        <div v-if="userProperties.length === 0" class="text-muted">
          <!-- empty -->
        </div>

        <div v-else>
          <div v-for="p in userProperties" :key="p.id" class="property-item p-3 border rounded mb-3">
            <div v-if="editingProperty === p.id">
              <input v-model="propertyForm.name" class="form-control mb-2" placeholder="Όνομα" />
              <textarea
                  v-model="propertyForm.description"
                  class="form-control mb-2"
                  placeholder="Περιγραφή"
              ></textarea>
              <input v-model="propertyForm.city" class="form-control mb-2" placeholder="Πόλη" />
              <input v-model="propertyForm.country" class="form-control mb-2" placeholder="Χώρα" />

              <button @click="savePropertyEdit" class="btn btn-sm btn-success mr-2">
                Αποθήκευση
              </button>
              <button @click="cancelEdit" class="btn btn-sm btn-secondary">Άκυρο</button>
            </div>

            <div v-else>
              <h6>{{ p.name }}</h6>
              <p class="mb-1"><strong>Περιγραφή:</strong> {{ p.description }}</p>
              <p class="mb-1"><strong>Τοποθεσία:</strong> {{ p.city }}, {{ p.country }}</p>
              <button @click="startEdit(p)" class="btn btn-sm btn-outline-primary">
                ✏️ Επεξεργασία
              </button>
            </div>
          </div>
        </div>
      </div>

      <form @submit.prevent="saveProfile">
        <div v-if="saveSuccess" class="text-success text-center mb-3">
          ✅ Το προφίλ ενημερώθηκε με επιτυχία!
        </div>

        <div class="form-group">
          <label>First Name</label>
          <input type="text" v-model="user.name" class="form-control" />
        </div>

        <div class="form-group">
          <label>Email</label>
          <input type="email" v-model="user.email" class="form-control" />
        </div>

        <div class="form-group">
          <label>Last Name</label>
          <input type="text" v-model="user.surname" class="form-control" />
        </div>

        <div class="form-group">
          <label>Username</label>
          <input type="text" v-model="user.username" class="form-control" />
        </div>

        <div class="form-group">
          <label>Passport Number</label>
          <input type="text" v-model="user.idNumber" class="form-control" />
        </div>

        <div class="form-group">
          <label>ΑΦΜ</label>
          <input type="text" v-model="user.taxNumber" class="form-control" />
        </div>

        <button type="submit" class="btn btn-primary btn-block mt-3" :disabled="saving">
          <span v-if="saving">Αποθήκευση...</span>
          <span v-else>Save Changes</span>
        </button>

        <button class="btn btn-secondary btn-block mt-2" @click.prevent="resetProfile" :disabled="saving">
          Cancel
        </button>

        <hr />

        <button @click.prevent="logout" class="btn btn-link btn-block text-danger">
          Log Out
        </button>

        <div v-if="user.role === 'user' && !requestSent" class="mt-3 text-center">
          <button @click="requestOwnerRole" class="btn btn-outline-primary">
            🔄 Ζήτησε να γίνεις Ιδιοκτήτης
          </button>
        </div>

        <div v-if="requestSent" class="text-success mt-2 text-center">
          ✅ Το αίτημά σου στάλθηκε στον διαχειριστή.
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProfilePage",
  data() {
    return {
      user: {
        id: null,
        name: "",
        surname: "",
        username: "",
        email: "",
        idNumber: "",
        taxNumber: "",
        avatar: "",
        role: "",
      },
      originalUser: {},
      saveSuccess: false,
      saving: false,
      showDropdown: false,
      requestSent: false,
      defaultAvatar: "https://cdn-icons-png.flaticon.com/512/147/147144.png",
      userProperties: [],
      editingProperty: null,
      propertyForm: {},
    };
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },

    closeOnOutsideClick(e) {
      const dropdown = this.$el.querySelector(".custom-dropdown");
      const name = this.$el.querySelector(".profile-name");
      if (!dropdown || !name) return;

      // αν κλικάρει έξω από dropdown + name, κλείσε
      if (!dropdown.contains(e.target) && !name.contains(e.target)) {
        this.showDropdown = false;
      }
    },

    logout() {
      localStorage.clear();
      this.$router.push("/login");
    },

    async fetchUser() {
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      try {
        const res = await this.$api.get("/auth/me");
        const d = res.data;

        this.user = {
          id: d.id,
          username: d.username,
          email: d.email,
          name: d.firstName || "",
          surname: d.lastName || "",
          role: d.role || "",
          idNumber: d.passportNumber || "",
          taxNumber: d.afm || "",
          avatar: "", // blob will be loaded
        };

        this.originalUser = { ...this.user };

        if (this.user.id) {
          await this.loadAvatar();
        }
      } catch (e) {
        this.$router.push("/login");
      }
    },

    // ✅ Robust: try /properties/my, fallback to /users/{id}/properties
    async fetchUserProperties() {
      const token = localStorage.getItem("token");
      if (!token) return;

      try {
        const res = await this.$api.get("/properties/my");
        this.userProperties = res.data || [];
        return;
      } catch (err1) {
        // fallback
      }

      try {
        if (!this.user.id) return;
        const res2 = await this.$api.get(`/users/${this.user.id}/properties`);
        this.userProperties = res2.data || [];
      } catch (err2) {
        console.error("❌ Error fetching properties:", err2);
        this.userProperties = [];
      }
    },

    startEdit(property) {
      this.editingProperty = property.id;
      this.propertyForm = { ...property };
    },

    async savePropertyEdit() {
      const token = localStorage.getItem("token");
      if (!token) return;

      try {
        // backend expects PATCH usually; keeping PUT if your API expects it.
        await this.$api.put(`/properties/${this.propertyForm.id}`, this.propertyForm);
        this.editingProperty = null;
        await this.fetchUserProperties();
        alert("Το ακίνητο ενημερώθηκε!");
      } catch (err) {
        console.error("❌ Error updating property:", err);
        alert("Σφάλμα κατά την αποθήκευση.");
      }
    },

    cancelEdit() {
      this.editingProperty = null;
      this.propertyForm = {};
    },

    async saveProfile() {
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      this.saving = true;

      const updates = {
        firstName: this.user.name,
        lastName: this.user.surname,
        username: this.user.username,
        email: this.user.email,
        passportNumber: this.user.idNumber,
        afm: this.user.taxNumber,
      };

      try {
        await this.$api.patch("/users/me", updates);

        this.saveSuccess = true;
        await this.fetchUser();

        setTimeout(() => (this.saveSuccess = false), 3000);
        this.showDropdown = false;
      } catch (err) {
        console.error("Error updating profile:", err);
        alert("Σφάλμα κατά την ενημέρωση.");
      } finally {
        this.saving = false;
      }
    },

    resetProfile() {
      this.user = { ...this.originalUser };
    },

    triggerFileInput() {
      this.$refs.fileInput.click();
    },

    async onAvatarChange(event) {
      const file = event.target.files && event.target.files[0];
      if (!file) return;

      // Preview immediately (local)
      const reader = new FileReader();
      reader.onload = (e) => {
        this.user.avatar = e.target.result;
      };
      reader.readAsDataURL(file);

      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      try {
        const formData = new FormData();
        formData.append("file", file);

        await this.$api.post(`/users/${this.user.id}/upload-photo`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });

        alert("✅ Avatar ενημερώθηκε!");
        await this.loadAvatar();
      } catch (err) {
        console.error("❌ Error uploading avatar:", err);
        alert("Σφάλμα κατά την ενημέρωση του avatar.");
      } finally {
        event.target.value = "";
      }
    },

    async loadAvatar() {
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      try {
        const response = await this.$api.get(`/users/${this.user.id}/photo`, {
          responseType: "blob",
        });
        this.user.avatar = URL.createObjectURL(response.data);
      } catch (err) {
        console.error("❌ Error loading avatar:", err);
        this.user.avatar = this.defaultAvatar;
      }
    },

    async requestOwnerRole() {
      const token = localStorage.getItem("token");
      if (!token) return this.$router.push("/login");

      try {
        await this.$api.post("/role-requests", {
          userId: this.user.id,
          requestedRole: "owner",
        });
        this.requestSent = true;
      } catch (error) {
        console.error("❌ Error requesting role change:", error);
        alert("Σφάλμα κατά την αποστολή αιτήματος.");
      }
    },
  },

  async mounted() {
    const token = localStorage.getItem("token");
    const expiry = localStorage.getItem("token_expiry");
    if (!token || !expiry || new Date().getTime() > Number(expiry)) {
      localStorage.clear();
      this.$router.push("/login");
      return;
    }

    await this.fetchUser();
    await this.fetchUserProperties();

    document.addEventListener("mousedown", this.closeOnOutsideClick);
  },

  beforeUnmount() {
    document.removeEventListener("mousedown", this.closeOnOutsideClick);
  },
};
</script>

<style scoped>
.profile-name {
  cursor: pointer;
  font-weight: bold;
  font-size: 1.5rem;
  user-select: none;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 3px solid #ccc;
  transition: border-color 0.3s;
}

.avatar:hover {
  border-color: #007bff;
}

.custom-dropdown {
  position: absolute;
  background: white;
  border-radius: 10px;
  padding: 20px;
  max-width: 320px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  z-index: 9999;
  left: 50%;
  transform: translateX(-50%);
}

.clickable {
  cursor: pointer;
  user-select: none;
}
</style>
