import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import Argon from "./plugins/argon-kit";
import api from "./api";
import "./registerServiceWorker"; // αρκεί να υπάρχει το αρχείο

Vue.config.productionTip = false;
Vue.use(Argon);

// διαθέσιμο σε όλα τα components ως this.$api
Vue.prototype.$api = api;

new Vue({
    router,
    render: h => h(App)
}).$mount("#app");
