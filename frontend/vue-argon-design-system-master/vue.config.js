const webpack = require("webpack");

module.exports = {
    // ⬇️ κλείνει το lint στο save
    lintOnSave: false,

    /**
     * ✅ DEV SERVER PROXY
     * Τοπικά:
     *  Front: http://localhost:8081
     *  Backend: http://localhost:8080
     *
     * Ό,τι πάει σε /api → προωθείται στο backend
     */
    devServer: {
        port: 8081,
        proxy: {
            "/api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                secure: false,
                logLevel: "debug"
            }
        }
    },

    configureWebpack: {
        plugins: [
            // ⬇️ αυτό που είχες (ΔΕΝ το πειράζουμε)
            new webpack.optimize.LimitChunkCountPlugin({
                maxChunks: 6
            })
        ]
    },

    pwa: {
        name: "Vue Argon Design",
        themeColor: "#172b4d",
        msTileColor: "#172b4d",
        appleMobileWebAppCapable: "yes",
        appleMobileWebAppStatusBarStyle: "#172b4d"
    },

    css: {
        // Enable CSS source maps.
        sourceMap: process.env.NODE_ENV !== "production"
    }
};
