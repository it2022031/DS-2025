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
      <form @submit.prevent="saveProfile">
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

        <button type="submit" class="btn btn-primary btn-block mt-3">
          Αποθήκευση
        </button>
        <hr />
        <button @click.prevent="logout" class="btn btn-link btn-block text-danger">
          Αποσύνδεση
        </button>
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
        name: "",
        email: "",
        phone: "",
        profession: "",
        location: "",
        avatar: ""
      },
      showDropdown: false,
      defaultAvatar:
          "https://cdn-icons-png.flaticon.com/512/147/147144.png" // αν δεν υπάρχει avatar
    };
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    closeOnOutsideClick(event) {
      if (
          !this.$el.contains(event.target) &&
          this.showDropdown
      ) {
        this.showDropdown = false;
      }
    },
    logout() {
      localStorage.removeItem("token");
      this.$router.push("/login");
    },
    fetchUser() {
      // Παίρνουμε το token από localStorage και κάνουμε request για τα στοιχεία του χρήστη
      const token = localStorage.getItem("token");
      if (!token) {
        this.$router.push("/login");
        return;
      }
      axios
          .get("http://localhost:8080/api/users/me", {
            headers: { Authorization: `Bearer ${token}` }
          })
          .then(res => {
            this.user = res.data;
          })
          .catch(err => {
            console.error("Error fetching user data:", err);
          });
    },
    saveProfile() {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Δεν είστε συνδεδεμένοι.");
        this.$router.push("/login");
        return;
      }
      // Αποθήκευση αλλαγών προφίλ
      axios
          .put("http://localhost:8080/api/users/me", this.user, {
            headers: { Authorization: `Bearer ${token}` }
          })
          .then(() => {
            alert("Το προφίλ ενημερώθηκε!");
            this.showDropdown = false;
          })
          .catch(err => {
            console.error("Error updating profile:", err);
            alert("Σφάλμα κατά την ενημέρωση.");
          });
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    onAvatarChange(event) {
      const file = event.target.files[0];
      if (!file) return;

      // Απλό preview της εικόνας
      const reader = new FileReader();
      reader.onload = e => {
        this.user.avatar = e.target.result;
      };
      reader.readAsDataURL(file);

      // Εδώ θα έστελνες την εικόνα στο server, πχ με FormData και axios.post
      // πχ: this.uploadAvatar(file)
    }
  },
  mounted() {
    this.fetchUser();
    document.addEventListener("click", this.closeOnOutsideClick);
  },
  beforeDestroy() {
    document.removeEventListener("click", this.closeOnOutsideClick);
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
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
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
