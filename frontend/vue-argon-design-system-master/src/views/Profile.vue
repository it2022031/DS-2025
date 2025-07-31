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
        email: "",
        phone: "",
        profession: "",
        location: "",
        avatar: "",
        role: ""
      },
      showDropdown: false,
      requestSent: false,
      defaultAvatar:
          "https://cdn-icons-png.flaticon.com/512/147/147144.png"
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
          .get("http://localhost:8080/api/users/me", {
            headers: { Authorization: `Bearer ${token}` }
          })
          .then(res => {
            this.user = res.data;
          })
          .catch(err => {
            console.error("Error fetching user data:", err);
            this.$router.push("/login");
          });
    },
    saveProfile() {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Δεν είστε συνδεδεμένοι.");
        this.$router.push("/login");
        return;
      }

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

      const reader = new FileReader();
      reader.onload = e => {
        this.user.avatar = e.target.result;
      };
      reader.readAsDataURL(file);

      // TODO: Αν θες να στείλεις το αρχείο στον server, βάλε axios.post με FormData εδώ
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
