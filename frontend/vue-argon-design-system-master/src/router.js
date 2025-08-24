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
import AllProperties from "@/views/AllProperties.vue";
import PropertyDetails from "@/views/PropertyDetails.vue";
import ListBookings from "@/views/ListBookings.vue";
import ApproveRejectProperties from "@/views/ApproveRejectProperties.vue";
import ApproveRejectRenters from "@/views/ApproveRejectRenters.vue";
import DeleteReviewsAdmin from "@/views/DeleteReviewsAdmin.vue";
import DeleteRentalsAdmin from "@/views/DeleteRentalsAdmin.vue";

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
      path: "/about",
      name: "about",
      components: {
        header: AppHeader,
        default: About,
        footer: AppFooter
      }
    },
      {
          path: '/approved-properties',
          name: 'AllProperties',
          components: {
              header: AppHeader,
              default: AllProperties,
              footer: AppFooter
          }
      },
      {
          path: "/properties/:id",
          name: "PropertyDetails",
          components: {
              header: AppHeader,
              default: PropertyDetails,
              footer: AppFooter
          },
          props: true, // this allows you to receive "id" as a prop in PropertyDetails
      },
      {
          path: '/list-bookings',
          name: 'ListBookings',
          components: {
              header: AppHeader,
              default: ListBookings,
              footer: AppFooter
          }
      },
      {
          path: '/approve-reject-properties',
          name: 'ApproveRejectProperties',
          components: {
              header: AppHeader,
              default: ApproveRejectProperties,
              footer: AppFooter
          }
      },
      {
          path: '/approve-reject-renters',
          name: 'ApproveRejectRenters',
          components: {
              header: AppHeader,
              default: ApproveRejectRenters,
              footer: AppFooter
          }
      },
      {
          path: '/delete-reviews-admin',
          name: 'DeleteReviewsAdmin',
          components: {
              header: AppHeader,
              default: DeleteReviewsAdmin,
              footer: AppFooter
          }
      },
      {
          path: '/delete-rentals-admin',
          name: 'DeleteRentalsAdmin',
          components: {
              header: AppHeader,
              default: DeleteRentalsAdmin,
              footer: AppFooter
          }
      },
  ]
});
