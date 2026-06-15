import React from 'react';
import ReactDOM from 'react-dom/client';
import { LegalComponent } from './pages/LegalPage.tsx';
import { theme } from './theme.ts';
import { ModalsProvider } from '@mantine/modals';
import { Notifications } from '@mantine/notifications';
import { MantineProvider } from '@mantine/core';

import './i18n.ts';
import './index.css';
import './app.css';

import '@mantine/core/styles.css';
import '@mantine/dropzone/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/notifications/styles.css';
import '@mantine/spotlight/styles.css';

ReactDOM.createRoot(document.getElementById('legal-root')!).render(
  <React.StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="auto">
      <ModalsProvider>
        <Notifications />
        <LegalComponent />
      </ModalsProvider>
    </MantineProvider>
  </React.StrictMode>
);
