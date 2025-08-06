<template>
  <header class="header-global">
    <base-nav ref="nav" class="navbar-main" transparent effect="light" expand>
      <!-- Λογότυπο -->
      <router-link slot="brand" class="navbar-brand mr-lg-5" to="/">
        <img src="img/brand/white.png" alt="logo" />
      </router-link>

      <!-- Mobile menu header -->
      <div class="row" slot="content-header" slot-scope="{ closeMenu }">
        <div class="col-6 collapse-brand">
          <a href="#">
            <img src="img/brand/blue.png" />
          </a>
        </div>
        <div class="col-6 collapse-close">
          <close-button @click="closeMenu"></close-button>
        </div>
      </div>

      <!-- Αριστερό μενού -->
      <ul class="navbar-nav align-items-lg-center">
        <li class="nav-item">
          <router-link to="/" class="nav-link">Home</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/Rent" class="nav-link">Ενοικίαση</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/properties/add" class="nav-link">Νέα Αγγελία</router-link>
        </li>
        <li class="nav-item">
          <router-link v-if="(userRole || '').toLowerCase() === 'owner' || (userRole || '').toLowerCase() === 'admin'" to="/list-properties" class="nav-link"> list-properties</router-link>
        </li>
        <li class="nav-item">
          <router-link v-if="(userRole || '').toLowerCase() === 'admin'" to="/users"  class="nav-link">list-users</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/Rentals" class="nav-link">list-rentals</router-link>
        </li>
      </ul>

      <!-- Δεξιό μενού -->
      <ul class="navbar-nav align-items-lg-center ml-lg-auto">
        <li v-if="!isLoggedIn" class="nav-item">
          <router-link to="/login" class="btn btn-outline-light btn-sm mr-2">Login</router-link>
        </li>
        <li v-if="!isLoggedIn" class="nav-item">
          <router-link to="/register" class="btn btn-light btn-sm">Register</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/about" class="nav-link">About</router-link>
        </li>
      </ul>

      <!-- Εικονίδιο χρήστη + dropdown -->
      <div v-if="isLoggedIn" class="d-flex align-items-center pl-3 position-relative" style="cursor: pointer;" @click.stop="toggleDropdown">
        <div class="rounded-circle bg-white d-flex justify-content-center align-items-center"
             style="width: 40px; height: 40px;">
          <i class="ni ni-single-02 text-dark" style="font-size: 22px;"></i>
        </div>

        <!-- Dropdown μενού -->
        <div v-show="showDropdown" class="dropdown-menu dropdown-menu-right show custom-dropdown">
          <router-link to="/profile" class="dropdown-item">Προφίλ</router-link>
          <div class="dropdown-divider"></div>
          <a class="dropdown-item" @click.prevent="logout">Αποσύνδεση</a>
        </div>
      </div>
    </base-nav>
  </header>
</template>

<script>
import BaseNav from "@/components/BaseNav";
import BaseDropdown from "@/components/BaseDropdown";
import CloseButton from "@/components/CloseButton";
import { eventBus } from '@/eventBus';

export default {
  name: "AppHeader",
  components: {
    BaseNav,
    CloseButton,
    BaseDropdown
  },
  data() {
    return {
      showDropdown: false,
      userRole: localStorage.getItem('userRole') || null,
      isLoggedIn: !!localStorage.getItem('token'),
    };
  },
  created() {
    // Ακούμε το event και ενημερώνουμε το isLoggedIn
    eventBus.$on('login-status-changed', (status) => {
      this.isLoggedIn = status;
      this.userRole = localStorage.getItem('userRole');
    });
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    closeOnOutsideClick(event) {
      const dropdown = this.$el.querySelector(".custom-dropdown");
      if (dropdown && !dropdown.contains(event.target)) {
        this.showDropdown = false;
      }
    },
    logout() {
      localStorage.removeItem("token");
      localStorage.removeItem("userRole");
      eventBus.$emit('login-status-changed', false);
      this.isLoggedIn = false;
      this.$router.push("/login");
    }
  },
  mounted() {
    document.addEventListener("click", this.closeOnOutsideClick);
  },
  beforeDestroy() {
    document.removeEventListener("click", this.closeOnOutsideClick);
    eventBus.$off('login-status-changed');
  },
  watch: {
    $route() {
      this.showDropdown = false;
      if (this.$refs.nav) {
        this.$refs.nav.closeMenu();
      }
    }
  }
};
</script>

<style scoped>
.header-global {
  background-color: #343a40;
  color: white;
  min-height: 100px;
}

.navbar-main {
  padding-right: 1rem;
}

.custom-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  min-width: 150px;
  z-index: 1000;
  padding: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.2s ease-in-out;
}

.custom-dropdown .dropdown-item {
  padding: 8px 12px;
  color: #333;
  text-decoration: none;
}

.custom-dropdown .dropdown-item:hover {
  background-color: #f8f9fa;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>