import Vue from "vue";
import Router from "vue-router";
import AppHeader from "./layout/AppHeader";
import AppFooter from "./layout/AppFooter";
import Components from "./views/Components.vue";
import Landing from "./views/Landing.vue";
import Login from "./views/Login.vue";
import Register from "./views/Register.vue";
import Profile from "./views/Profile.vue";
import UserList from "./views/ListUsers.vue";
import ListProperties from './views/ListProperties.vue';
import AddProperty from "@/views/AddProperty.vue";
import Rent from './views/Rent.vue';
import ListRentals from "@/views/ListRentals.vue";
import Home from './views/Home.vue';
import About from './views/About.vue';
import RoleRequests from './views/RoleRequests.vue';
import PropertyEdit from "@/views/PropertyEdit.vue";
import RequestRenter from "@/views/RequestRenter.vue";

Vue.use(Router);

export default new Router({
  linkExactActiveClass: "active",
  routes: [
    {
      path: "/",
      name: "home",
      components: {
        header: AppHeader,
        default: Home,
        footer: AppFooter
      }
    },
    {
      path: "/request-renter",
      name: "request-renter",
      components: {
        header: AppHeader,
        default: RequestRenter,
        footer: AppFooter
      }
    },
    {
      path: "/rentals",
      name: "ListRentals",
      components: {
        header: AppHeader,
        default: ListRentals,
        footer: AppFooter
      }
    },
    {
      path: "/list-properties",
      name: "list-properties",
      components: {
        header: AppHeader,
        default: ListProperties,
        footer: AppFooter
      }
    },
    {
      path: "/users",
      name: "users",
      components: {
        header: AppHeader,
        default: UserList,
        footer: AppFooter
      }
    },
    {
      path: '/properties/add',
      name: 'AddProperty',
      components: {
        header: AppHeader,
        default: AddProperty,
        footer: AppFooter
      }
    },
    {
      path: "/properties/:id/edit",
      name: "PropertyEdit",
      components: {
        header: AppHeader,
        default: PropertyEdit,
        footer: AppFooter
      },
      meta: { requiresAuth: true, roles: ["owner", "admin"] }
    },
    {
      path: "/rent",
      name: "rent",
      components: {
        header: AppHeader,
        default: Rent,
        footer: AppFooter
      }
    },
    {
      path: "/login",
      name: "login",
      // αν θες χωρίς header/footer, βάλε μόνο component
      components: {
        header: AppHeader,
        default: Login,
        footer: AppFooter
      }
    },
    {
      path: "/register",
      name: "register",
      components: {
        header: AppHeader,
        default: Register,
        footer: AppFooter
      }
    },
    {
      path: "/admin/role-requests",
      name: "role-requests",
      components: {
        header: AppHeader,
        default: RoleRequests,
        footer: AppFooter
      },
      meta: {
        requiresAuth: true,
        roles: ["admin"]
      }
    },
    {
      path: "/profile",
      name: "profile",
      components: {
        header: AppHeader,
        default: Profile,
        footer: AppFooter
      },
      meta: {
        showInMenu: true,
        label: "Προφίλ",
        align: "right"
      }
    },
    {
      path: "/my-properties",
      name: "my-properties",
      components: {
        header: AppHeader,
        default: () => import("./views/MyProperties.vue"),
        footer: AppFooter
      },
      meta: { requiresAuth: true } // προαιρετικό
    },
    {
      path: "/about",
      name: "about",
      components: {
        header: AppHeader,
        default: About,
        footer: AppFooter
      }
    }

  ]
});
