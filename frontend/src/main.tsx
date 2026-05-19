import './sentry.ts';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import keycloak from './keycloak.ts';
import { type KcContext, KcPage } from './keycloak-theme/kc.gen.tsx';
import { init } from '@plausible-analytics/tracker';
import * as Sentry from '@sentry/react';

init({
  domain: import.meta.env.VITE_ANALYTICS_URI,
  endpoint: `${import.meta.env.VITE_ANALYTICS_INSTANCE}/api/event`,
  //captureOnLocalhost: true,
});



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
        createRoot(document.getElementById('root')!, {
          // Callback called when an error is thrown and not caught by an ErrorBoundary.
          onUncaughtError: Sentry.reactErrorHandler((error, errorInfo) => {
            console.warn('Uncaught error', error, errorInfo.componentStack);
          }),
          // Callback called when React catches an error in an ErrorBoundary.
          onCaughtError: Sentry.reactErrorHandler(),
          // Callback called when React automatically recovers from errors.
          onRecoverableError: Sentry.reactErrorHandler(),
        }).render(<App />);
      }
    })
    .catch((err) => {
      console.error('Keycloak init failed', err);
    });
}
