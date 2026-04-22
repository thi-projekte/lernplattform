import { UserProvider } from './provider/user-provider.tsx';
import { RouterProvider } from 'react-router/dom';
import { router } from './routing.ts';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';

import './i18n.ts';

import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/notifications/styles.css';
import { ModalsProvider } from '@mantine/modals';
import { Notifications } from '@mantine/notifications';

const queryClient = new QueryClient();

function App() {
  return (
    <MantineProvider>
      <ModalsProvider>
        <QueryClientProvider client={queryClient}>
          <UserProvider>
            <Notifications />
            <RouterProvider router={router} />
          </UserProvider>
        </QueryClientProvider>
      </ModalsProvider>
    </MantineProvider>
  );
}

export default App;
