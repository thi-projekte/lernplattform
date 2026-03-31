import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import keycloak from "./keycloak.ts";

keycloak.init({
    onLoad: 'login-required',
    // Needs to enabled in order to work with vite HMR
    checkLoginIframe: false
}).then((authenticated) => {
    if (authenticated) {
        createRoot(document.getElementById('root')!).render(<App />);
    }
}).catch(err => {
    console.error("Keycloak init failed", err);
});