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

        <!-- Ενοικίαση: μόνο logged in -->
        <li v-if="isLoggedIn && (hasRole('RENTER'))" class="nav-item">
          <router-link to="/approved-properties" class="nav-link">Ενοικίαση</router-link>
        </li>

        <li v-if="isLoggedIn && (!hasRole('ADMIN') && !hasRole('RENTER'))" class="nav-item">
          <router-link to="/rent" class="nav-link">Ενοικίαση</router-link>
        </li>

        <!-- Νέα Αγγελία: OWNER ή ADMIN -->
        <li v-if="isLoggedIn && hasRole('USER') && (!hasRole('ADMIN'))" class="nav-item">
          <router-link to="/properties/add" class="nav-link">Νέα Αγγελία</router-link>
        </li>

        <!-- Λίστα Ακινήτων: OWNER ή ADMIN -->
        <li v-if="isLoggedIn && hasRole('USER', 'RENTER') && (!hasRole('ADMIN'))" class="nav-item">
          <router-link to="/list-properties" class="nav-link">Λίστα Ακινήτων</router-link>
        </li>

        <!-- Λίστα Χρηστών: μόνο ADMIN -->
        <li v-if="isLoggedIn && hasRole('ADMIN')" class="nav-item">
          <router-link to="/users" class="nav-link">Λίστα Χρηστών</router-link>
        </li>

        <!-- Λίστα Ενοικιάσεων: μόνο logged in -->
        <li v-if="isLoggedIn && hasRole('USER') && (!hasRole('ADMIN'))" class="nav-item">
          <router-link to="/rentals" class="nav-link">Λίστα Ενοικιάσεων</router-link>
        </li>

        <!-- Λίστα ΚΡΑΤΗΣΕΩΝ: μόνο logged in -->
        <li v-if="isLoggedIn && hasRole('RENTER') && (!hasRole('ADMIN'))" class="nav-item">
          <router-link to="/list-bookings" class="nav-link">Λίστα Κρατήσεων</router-link>
        </li>

        <!-- Λίστα ΚΡΑΤΗΣΕΩΝ: μόνο logged in -->
        <li v-if="isLoggedIn && hasRole('ADMIN')" class="nav-item">
          <router-link to="/approve-reject-properties" class="nav-link">Λίστα Pending Properties</router-link>
        </li>

        <!-- Λίστα ΚΡΑΤΗΣΕΩΝ: μόνο logged in -->
        <li v-if="isLoggedIn && hasRole('ADMIN')" class="nav-item">
          <router-link to="/approve-reject-renters" class="nav-link">Λίστα Approve To Be Renters</router-link>
        </li>

        <li v-if="isLoggedIn && hasRole('ADMIN')" class="nav-item">
          <router-link to="/delete-reviews-admin" class="nav-link">See All Reviews</router-link>
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
      roles: [],
      isLoggedIn: !!localStorage.getItem('token')
    };
  },
  created() {
    this.loadRoles();
    eventBus.$on('login-status-changed', (status) => {
      this.isLoggedIn = status;
      this.loadRoles();
    });
  },
  methods: {
    loadRoles() {
      try {
        const stored = JSON.parse(localStorage.getItem("userRoles") || "[]");
        this.roles = Array.isArray(stored)
            ? stored.map(r => (typeof r === "string" ? r.toUpperCase() : (r.role || "").toUpperCase()))
            : [];
      } catch {
        this.roles = [];
      }
    },
    hasRole(...requiredRoles) {
      return this.roles.some(r => requiredRoles.includes(r));
    },
    toggleDropdown() { this.showDropdown = !this.showDropdown; },
    logout() {
      localStorage.clear();
      eventBus.$emit('login-status-changed', false);
      this.isLoggedIn = false;

      if (this.$route.path !== "/login") {
        this.$router.push("/login");
      }
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