import { UserProvider } from './provider/user-provider.tsx';
import { RouterProvider } from 'react-router/dom';
import { router } from './routing.ts';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';

const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <UserProvider>
        <RouterProvider router={router} />
      </UserProvider>
    </QueryClientProvider>
  );
}

export default App;
