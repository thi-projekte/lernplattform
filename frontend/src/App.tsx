import { UserProvider } from './provider/user-provider.tsx';
import { RouterProvider } from 'react-router/dom';
import { router } from './routing.ts';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { theme } from './theme.ts';
import { TopicSpotlight } from './components/topic-spotlight.tsx';

import './i18n.ts';
import './index.css';
import './app.css';

import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/notifications/styles.css';
import '@mantine/spotlight/styles.css';
import { ModalsProvider } from '@mantine/modals';
import { Notifications } from '@mantine/notifications';
import RegistrationProvider from './provider/registration-provider.tsx';
import { SubscriptionProvider } from './provider/subscription-provider.tsx';
import type { KeycloakResourceAccess } from 'keycloak-js';
import keycloak from './keycloak.ts';
import { track } from '@plausible-analytics/tracker';

const queryClient = new QueryClient();

function App() {
  const resourceAccess: KeycloakResourceAccess = keycloak.tokenParsed
    ?.resource_access as KeycloakResourceAccess;
  const roles = resourceAccess['mynd']?.roles ?? [];
  for (const role of roles) {
    if (role) {
      track('role', { props: { role: role } });
    }
  }
  return (
    <MantineProvider theme={theme} defaultColorScheme="auto">
      <ModalsProvider>
        <QueryClientProvider client={queryClient}>
          <UserProvider>
            <Notifications />
            <RegistrationProvider>
              <SubscriptionProvider>
                <RouterProvider router={router} />
              </SubscriptionProvider>
            </RegistrationProvider>
          </UserProvider>
        </QueryClientProvider>
      </ModalsProvider>
    </MantineProvider>
  );
}

export default App;
