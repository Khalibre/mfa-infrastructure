import react from "@vitejs/plugin-react-swc";
import { defineConfig } from "vite";
import { checker } from "vite-plugin-checker";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react(), checker({ typescript: true })],
    server: {
        origin: "http://localhost:5173",
        port: 5173,
    },
    base: "",
    resolve: {
        // @keycloak/keycloak-account-ui and @keycloak/keycloak-ui-shared ship their
        // own copies of i18next/react-i18next. If more than one copy ends up in the
        // bundle, only the one wired up in src/i18n.ts receives the instance and the
        // others fall back to an identity `t`, rendering raw message keys.
        dedupe: [
            "i18next",
            "react-i18next",
            "i18next-http-backend",
            "i18next-fetch-backend",
            "react-router",
            "react-router-dom",
        ],
    },
    build: {
        manifest: true,
        sourcemap: true,
        rollupOptions: {
            input: "src/main.tsx",
            external: ["react", "react/jsx-runtime", "react-dom"],
            treeshake: false,
        },
    },
});
