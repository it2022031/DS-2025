import Vue from "vue";
import Router from "vue-router";
import AppHeader from "./layout/AppHeader";
import AppFooter from "./layout/AppFooter";
import Components from "./views/Components.vue";
import Landing from "./views/Landing.vue";
import Login from "./views/Login.vue";
import Register from "./views/Register.vue";
import Profile from "./views/Profile.vue";
import UserList from "./views/ListUsers.vue"; // ή ./components/UserList.vue ανάλογα πού το έβαλες
import ListProperties from './views/ListProperties.vue';
import AddProperty from "@/views/AddProperty.vue";
import Rent from './views/Rent.vue';

Vue.use(Router);

export default new Router({
  linkExactActiveClass: "active",
  routes: [
    {
      path: "/",
      name: "components",
      components: {
        header: AppHeader,
        // default: Components,
        // footer: AppFooter
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
      component: AddProperty
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

    //   {
  //     path: "/landing",
  //     name: "landing",
  //     components: {
  //       header: AppHeader,
  //       default: Landing,
  //       footer: AppFooter
  //     }
  //   },
    {
      path: "/login",
      name: "login",
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



  //   {
  //     path: "/profile",
  //     name: "profile",
  //     components: {
  //       header: AppHeader,
  //       default: Profile,
  //       footer: AppFooter
  //     }
  //   }
  // ],
  // scrollBehavior: to => {
  //   if (to.hash) {
  //     return { selector: to.hash };
  //   } else {
  //     return { x: 0, y: 0 };
  //   }
  // }

]

});
