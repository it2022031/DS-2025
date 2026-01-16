import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import Argon from "./plugins/argon-kit";
import api from "./api";
// import "./registerServiceWorker"; // αρκεί να υπάρχει το αρχείο

Vue.config.productionTip = false;
Vue.use(Argon);

// διαθέσιμο σε όλα τα components ως this.$api
Vue.prototype.$api = api;

// 🔹 Logout only on first app open (per browser session)
if (!sessionStorage.getItem("appStarted")) {
    localStorage.removeItem("token");
    localStorage.removeItem("token_expiry");
    localStorage.removeItem("userRoles");
    localStorage.removeItem("userRole");
    localStorage.removeItem("username");
    localStorage.removeItem("userId");

    sessionStorage.setItem("appStarted", "true");
}

new Vue({
    router,
    render: h => h(App)
}).$mount("#app");
