import { UserProvider } from './provider/user-provider.tsx';
import { RouterProvider } from 'react-router/dom';
import { router } from './routing.ts';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { theme } from './theme.ts';

import './i18n.ts';

import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/notifications/styles.css';
import { ModalsProvider } from '@mantine/modals';
import { Notifications } from '@mantine/notifications';
import RegistrationProvider from './provider/registration-provider.tsx';
import type { KeycloakResourceAccess } from 'keycloak-js';
import keycloak from './keycloak.ts';
import { track } from '@plausible-analytics/tracker';

const queryClient = new QueryClient();

function App() {


  const resourceAccess: KeycloakResourceAccess = keycloak.tokenParsed
    ?.resource_access as KeycloakResourceAccess;
  const roles = resourceAccess['mynd']?.roles;
  for (const role of roles) {
    track('role', {props: {role: role}});
  }

  return (
    <MantineProvider theme={theme}>
      <ModalsProvider>
        <QueryClientProvider client={queryClient}>
          <UserProvider>
            <Notifications />
            <RegistrationProvider>
              <RouterProvider router={router} />
            </RegistrationProvider>
          </UserProvider>
        </QueryClientProvider>
      </ModalsProvider>
    </MantineProvider>
  );
}

export default App;
