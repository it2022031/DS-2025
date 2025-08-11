import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import Argon from "./plugins/argon-kit";
import api from "./api"; // το καινούριο instance
import './registerServiceWorker';

Vue.config.productionTip = false;
Vue.use(Argon);

// Κάνουμε διαθέσιμο το api instance σε όλα τα components ως this.$api
Vue.prototype.$api = api;

new Vue({
  router,
  render: h => h(App)
}).$mount("#app");
