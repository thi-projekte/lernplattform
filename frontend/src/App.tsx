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

const queryClient = new QueryClient();

function App() {
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
