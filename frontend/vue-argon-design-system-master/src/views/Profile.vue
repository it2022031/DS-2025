<template>
  <div class="profile-page text-center mt-5">
    <!-- Εμφάνιση avatar και ονόματος -->
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
      {{ user.name || 'Unknown User' }}
      <i class="ni ni-bold-down ml-2"></i>
    </h3>

    <!-- Dropdown με στοιχεία προφίλ και φόρμα ενημέρωσης -->
    <div v-show="showDropdown" class="custom-dropdown mt-2 text-left">
      <!-- ✅ Λίστα Properties -->
      <div class="mt-4 text-left">
        <h5>Τα Ακίνητά Μου</h5>
        <div v-if="userProperties.length === 0" class="text-muted">Δεν έχετε προσθέσει ακίνητα.</div>
        <div v-else>
          <div v-for="p in userProperties" :key="p.id" class="property-item p-3 border rounded mb-3">
            <div v-if="editingProperty === p.id">
              <input v-model="propertyForm.name" class="form-control mb-2" placeholder="Όνομα" />
              <textarea v-model="propertyForm.description" class="form-control mb-2" placeholder="Περιγραφή"></textarea>
              <input v-model="propertyForm.city" class="form-control mb-2" placeholder="Πόλη" />
              <input v-model="propertyForm.country" class="form-control mb-2" placeholder="Χώρα" />
              <button @click="savePropertyEdit" class="btn btn-sm btn-success mr-2">Αποθήκευση</button>
              <button @click="cancelEdit" class="btn btn-sm btn-secondary">Άκυρο</button>
            </div>
            <div v-else>
              <h6>{{ p.name }}</h6>
              <p class="mb-1"><strong>Περιγραφή:</strong> {{ p.description }}</p>
              <p class="mb-1"><strong>Τοποθεσία:</strong> {{ p.city }}, {{ p.country }}</p>
              <button @click="startEdit(p)" class="btn btn-sm btn-outline-primary">✏️ Επεξεργασία</button>
            </div>
          </div>
        </div>
      </div>
      <form @submit.prevent="saveProfile">
        <!-- Οπτική επιβεβαίωση αποθήκευσης -->
        <div v-if="saveSuccess" class="text-success text-center mb-3">
          ✅ Το προφίλ ενημερώθηκε με επιτυχία!
        </div>

        <div class="form-group">
          <label>Όνομα</label>
          <input type="text" v-model="user.name" class="form-control" />
        </div>

        <div class="form-group">
          <label>Email</label>
          <input type="email" v-model="user.email" class="form-control" />
        </div>

        <div class="form-group">
          <label>Τηλέφωνο</label>
          <input type="text" v-model="user.phone" class="form-control" />
        </div>

        <div class="form-group">
          <label>Επάγγελμα</label>
          <input type="text" v-model="user.profession" class="form-control" />
        </div>

        <div class="form-group">
          <label>Τοποθεσία</label>
          <input type="text" v-model="user.location" class="form-control" />
        </div>

        <div class="form-group">
          <label>Επίθετο</label>
          <input type="text" v-model="user.surname" class="form-control" />
        </div>

        <div class="form-group">
          <label>Username</label>
          <input type="text" v-model="user.username" class="form-control" />
        </div>

        <div class="form-group">
          <label>Αριθμός Διαβατηρίου</label>
          <input type="text" v-model="user.idNumber" class="form-control" />
        </div>

        <div class="form-group">
          <label>ΑΦΜ</label>
          <input type="text" v-model="user.taxNumber" class="form-control" />
        </div>

        <button type="submit" class="btn btn-primary btn-block mt-3" :disabled="saving">
          <span v-if="saving">Αποθήκευση...</span>
          <span v-else>Αποθήκευση</span>
        </button>

        <button class="btn btn-secondary btn-block mt-2" @click.prevent="resetProfile" :disabled="saving">
          Ακύρωση αλλαγών
        </button>

        <hr />
        <button @click.prevent="logout" class="btn btn-link btn-block text-danger">
          Αποσύνδεση
        </button>

        <!-- ✅ Αίτημα για ρόλο OWNER -->
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
import axios from "axios";

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
        phone: "",
        profession: "",
        location: "",
        idNumber: "",  // Passport Number
        taxNumber: "", // ΑΦΜ
        avatar: "",
      },
      originalUser: {},       // ✅ κρατάει backup για "Ακύρωση"
      saveSuccess: false,     // ✅ δείχνει μήνυμα επιτυχίας
      saving: false,           // ✅ δείχνει loading κατάσταση

      showDropdown: false,
      requestSent: false,
      defaultAvatar:
          "https://cdn-icons-png.flaticon.com/512/147/147144.png",
      userProperties: [],
      loadingProps: false,
      editingProperty: null,
      propertyForm: {}
    };
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    closeOnOutsideClick(event) {
      if (!this.$el.contains(event.target) && this.showDropdown) {
        this.showDropdown = false;
      }
    },
    logout() {
      localStorage.removeItem("token");
      localStorage.removeItem("token_expiry");
      localStorage.removeItem("userRole");
      localStorage.removeItem("userId");
      localStorage.removeItem("username");
      this.$router.push("/login");
    },
    fetchUser() {
      const token = localStorage.getItem("token");
      if (!token) {
        this.$router.push("/login");
        return;
      }

      axios
          .get("http://localhost:8080/api/auth/me", {
            headers: { Authorization: `Bearer ${token}` }
          })
          .then(res => {
            const data = res.data;
            this.user.id = data.id;
            this.user.username = data.username;
            this.user.email = data.email;
            this.user.name = data.firstName || '';    // firstName από backend -> name στο frontend
            this.user.surname = data.lastName || '';  // lastName από backend -> surname στο frontend
            this.user.role = data.role || '';

            this.user.phone = data.phone || '';
            this.user.profession = data.profession || '';
            this.user.location = data.location || '';
            this.user.idNumber = data.passportNumber || '';
            this.user.taxNumber = data.afm || '';

            this.originalUser = { ...this.user }; // ✅ κρατάει backup για "Ακύρωση αλλαγών"
          })
          .catch(err => {
            console.error("Error fetching user data:", err);
            this.$router.push("/login");
          });
    },
    async fetchUserProperties() {
      const token = localStorage.getItem("token");
      if (!token) return;
      try {
        const res = await axios.get("http://localhost:8080/api/properties/my", {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.userProperties = res.data;
      } catch (err) {
        console.error("❌ Error fetching properties:", err);
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
        await axios.put(`http://localhost:8080/api/properties/${this.propertyForm.id}`, this.propertyForm, {
          headers: { Authorization: `Bearer ${token}` }
        });
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

    saveProfile() {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Δεν είστε συνδεδεμένοι.");
        this.$router.push("/login");
        return;
      }

      this.saving = true; // 🔄 δείχνει loading

      axios
          .put("http://localhost:8080/api/users/me", this.user, {
            headers: { Authorization: `Bearer ${token}` }
          })
          .then(() => {
            this.saveSuccess = true; // ✅ μήνυμα επιτυχίας
            this.originalUser = { ...this.user }; // 🔁 ανανέωση backup
            setTimeout(() => this.saveSuccess = false, 3000); // ⏱️ κρύψε μετά από 3 δευτ
            this.showDropdown = false;
          })
          .catch(err => {
            console.error("Error updating profile:", err);
            alert("Σφάλμα κατά την ενημέρωση.");
          })
          .finally(() => {
            this.saving = false; // ✅ stop loading
          });
    },
    resetProfile() {
      this.user = { ...this.originalUser };
    },

    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    onAvatarChange(event) {
      const file = event.target.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = e => {
        this.user.avatar = e.target.result;
      };
      reader.readAsDataURL(file);

      const token = localStorage.getItem("token");
      if (!token) return;

      const formData = new FormData();
      formData.append('avatar', file);

      axios.post('http://localhost:8080/api/users/me/avatar', formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      }).then(() => {
        alert('Avatar ενημερώθηκε!');
      }).catch(() => {
        alert('Σφάλμα κατά την ενημέρωση του avatar.');
      });
    },
    async requestOwnerRole() {
      const token = localStorage.getItem("token");

      if (!token) {
        alert("Δεν είστε συνδεδεμένοι.");
        this.$router.push("/login");
        return;
      }

      try {
        await axios.post("http://localhost:8080/api/role-requests", {
          userId: this.user.id,
          requestedRole: "owner"
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });

        this.requestSent = true;
      } catch (error) {
        console.error("❌ Error requesting role change:", error);
        alert("Σφάλμα κατά την αποστολή αιτήματος.");
      }
    }
  },
  mounted() {
    const token = localStorage.getItem("token");
    const expiry = localStorage.getItem("token_expiry"); // πρέπει να αποθηκεύεις πότε λήγει το token

    if (!token || !expiry || new Date().getTime() > Number(expiry)) {
      localStorage.clear();
      this.$router.push("/login");
      return;
    }

    this.fetchUser();
    this.fetchUserProperties();
    document.addEventListener("mousedown", this.closeOnOutsideClick);
  },
  beforeUnmount() {
    document.removeEventListener("mousedown", this.closeOnOutsideClick);
  }
};
</script>


<style scoped>
.profile-name {
  cursor: pointer;
  font-weight: bold;
  font-size: 1.5rem;
  user-select: none;
}

.custom-dropdown {
  display: inline-block;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  max-width: 350px;
  animation: fadeIn 0.2s ease-in-out;
  text-align: left;
}

.avatar-container {
  display: flex;
  justify-content: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 3px solid #007bff;
}

.form-group {
  margin-bottom: 1rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
