// src/api.js
import axios from "axios";

// Φτιάχνουμε instance
const api = axios.create({
    baseURL: "http://localhost:8080/api", // βάλε εδώ το backend URL σου
    headers: {
        "Content-Type": "application/json",
    },
});

// Interceptor για να βάζει token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        const expiry = localStorage.getItem("token_expiry");

        if (token && expiry && Date.now() < Number(expiry)) {
            config.headers["Authorization"] = `Bearer ${token}`;
        } else {
            // Αν δεν υπάρχει ή έχει λήξει, καθάρισμα storage
            localStorage.removeItem("token");
            localStorage.removeItem("token_expiry");
        }

        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
