import { type BaseRouteObject, createBrowserRouter } from 'react-router';
import Homepage from './pages/home.tsx';

const routes: BaseRouteObject[] = [
  {
    path: '/',
    Component: Homepage,
  },
];

export const router = createBrowserRouter(routes);
