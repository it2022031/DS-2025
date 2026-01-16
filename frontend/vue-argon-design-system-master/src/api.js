// src/api.js
import axios from "axios";

/**
 * API base URL strategy:
 * - In production behind nginx: use "/api" (relative) so it keeps the same origin (http://127.0.0.1:8090)
 * - In dev: you can set VUE_APP_API_BASE_URL in .env / .env.production
 */
const API_BASE =
    (process.env.VUE_APP_API_BASE_URL && process.env.VUE_APP_API_BASE_URL.trim()) ||
    "/api";

const api = axios.create({
    baseURL: API_BASE,
    headers: {
        "Content-Type": "application/json",
    },
    // If you use cookies/sessions, enable this. For Bearer token only, not required.
    // withCredentials: true,
});

// Interceptor: attach Bearer token if valid
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        const expiry = localStorage.getItem("token_expiry");

        if (token && expiry && Date.now() < Number(expiry)) {
            config.headers["Authorization"] = `Bearer ${token}`;
        } else {
            localStorage.removeItem("token");
            localStorage.removeItem("token_expiry");
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
