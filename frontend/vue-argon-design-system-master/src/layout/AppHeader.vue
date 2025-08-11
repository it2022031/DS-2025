<template>
  <header class="header-global">
    <base-nav ref="nav" class="navbar-main" transparent effect="light" expand>
      <router-link slot="brand" class="navbar-brand mr-lg-5" to="/">
        <img src="img/brand/white.png" alt="logo" />
      </router-link>

      <div class="row" slot="content-header" slot-scope="{ closeMenu }">
        <div class="col-6 collapse-brand">
          <a href="#"><img src="img/brand/blue.png" /></a>
        </div>
        <div class="col-6 collapse-close">
          <close-button @click="closeMenu"></close-button>
        </div>
      </div>

      <!-- Αριστερό μενού -->
      <ul class="navbar-nav align-items-lg-center">
        <li class="nav-item"><router-link to="/" class="nav-link">Home</router-link></li>

        <li v-if="isLoggedIn" class="nav-item">
          <router-link to="/rent" class="nav-link">Ενοικίαση</router-link>
        </li>

        <li v-if="isLoggedIn && hasRole('OWNER','ADMIN')" class="nav-item">
          <router-link to="/properties/add" class="nav-link">Νέα Αγγελία</router-link>
        </li>

        <li v-if="isLoggedIn && hasRole('OWNER','ADMIN')" class="nav-item">
          <router-link to="/list-properties" class="nav-link">Λίστα Ακινήτων</router-link>
        </li>

        <li v-if="isLoggedIn && hasRole('ADMIN')" class="nav-item">
          <router-link to="/users" class="nav-link">Λίστα Χρηστών</router-link>
        </li>

        <li v-if="isLoggedIn" class="nav-item">
          <router-link to="/rentals" class="nav-link">Λίστα Ενοικιάσεων</router-link>
        </li>

        <li v-if="isLoggedIn" class="nav-item">
          <router-link to="/my-properties" class="nav-link">Τα ακίνητά μου</router-link>
        </li>
      </ul>

      <!-- Δεξιά -->
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

        <!-- Avatar dropdown -->
        <li v-if="isLoggedIn" class="nav-item dropdown d-flex align-items-center">
          <img :src="avatarUrl" alt="avatar" class="rounded-circle"
               style="width:36px;height:36px;object-fit:cover;border:2px solid #fff;cursor:pointer;"
               @click.stop="toggleDropdown" />
          <div v-show="showDropdown" class="dropdown-menu dropdown-menu-right show custom-dropdown">
            <router-link to="/profile" class="dropdown-item">Προφίλ</router-link>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" @click.prevent="logout">Αποσύνδεση</a>
          </div>
        </li>
      </ul>
    </base-nav>
  </header>
</template>

<script>
import BaseNav from "@/components/BaseNav";
import CloseButton from "@/components/CloseButton";
import BaseDropdown from "@/components/BaseDropdown";
import { eventBus } from "@/eventBus";

const DEFAULT_AVATAR = "https://cdn-icons-png.flaticon.com/512/147/147144.png";

export default {
  name: "AppHeader",
  components: { BaseNav, CloseButton, BaseDropdown },
  data() {
    return {
      showDropdown: false,
      roles: [],
      isLoggedIn: !!localStorage.getItem("token"),
      avatarUrl: localStorage.getItem("userAvatar") || DEFAULT_AVATAR
    };
  },
  created() {
    this.loadRoles();
    eventBus.$on("login-status-changed", (status) => {
      this.isLoggedIn = !!status;
      this.loadRoles();
      this.refreshAvatar();
    });
    eventBus.$on("avatar-updated", this.refreshAvatar);
  },
  methods: {
    loadRoles() {
      try {
        const stored = JSON.parse(localStorage.getItem("userRoles") || "[]");
        this.roles = (Array.isArray(stored) ? stored : [])
            .map(r => {
              const raw = typeof r === "string" ? r : (r.role || "");
              return raw.replace(/^ROLE_/, "").toUpperCase();
            })
            .filter(Boolean);
      } catch {
        this.roles = [];
      }
    },
    hasRole(...required) {
      return this.roles.some(r => required.includes(r));
    },
    refreshAvatar() {
      this.avatarUrl = localStorage.getItem("userAvatar") || DEFAULT_AVATAR;
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    logout() {
      localStorage.clear();
      this.isLoggedIn = false;
      this.refreshAvatar();
      eventBus.$emit("login-status-changed", false);
      if (this.$route.path !== "/login") {
        this.$router.push("/login").catch(() => {});
      }
    }
  },
  watch: {
    $route() {
      this.showDropdown = false;
      if (this.$refs.nav && typeof this.$refs.nav.closeMenu === 'function') {
        this.$refs.nav.closeMenu();
      }

    }
  }
};
</script>

<style scoped>
.header-global { background-color: #5866e9; color: white; }
.navbar-main { padding: 0.5rem 1rem; }

/* dropdown */
.custom-dropdown {
  position: absolute; top: 100%; right: 0;
  background: white; border: 1px solid #ddd; border-radius: 8px;
  min-width: 150px; z-index: 1000; padding: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>
