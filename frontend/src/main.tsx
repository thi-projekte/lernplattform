import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import keycloak from './keycloak.ts';
import { type KcContext, KcPage } from './keycloak-theme/kc.gen.tsx';

/* eslint-disable @typescript-eslint/no-explicit-any */
const kcContext = (window as any).kcContext as KcContext | undefined;

if (kcContext) {
  createRoot(document.getElementById('root')!).render(<KcPage kcContext={kcContext} />);
} else {
  keycloak
    .init({
      onLoad: 'login-required',
      // Needs to enabled in order to work with vite HMR
      checkLoginIframe: false,
    })
    .then((authenticated) => {
      if (authenticated) {
        createRoot(document.getElementById('root')!).render(<App />);
      }
    })
    .catch((err) => {
      console.error('Keycloak init failed', err);
    });
}
