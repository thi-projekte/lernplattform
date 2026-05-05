import { type BaseRouteObject, createBrowserRouter } from 'react-router';
import Homepage from './pages/home.tsx';
import BuilderModeListPage from './pages/builder-mode/list.tsx';
import type { ComponentType } from 'react';
import { IconHammer, IconHome, type IconProps } from '@tabler/icons-react';
import CreateTopicPage from './pages/builder-mode/create.tsx';
import EditTopicPage from './pages/builder-mode/edit.tsx';
import TopicDetailsPage from './pages/topic/details.tsx';
import { Role } from './auth.ts';

export interface TypedMyndRoute extends BaseRouteObject {
  isSidebar?: boolean;
  icon?: ComponentType<IconProps>;
  translation?: string;
  roles?: Role[];
}

export const routes: TypedMyndRoute[] = [
  {
    path: '/',
    Component: Homepage,
    isSidebar: true,
    icon: IconHome,
    translation: 'dashboard',
  },
  {
    path: '/builder-mode',
    Component: BuilderModeListPage,
    isSidebar: true,
    icon: IconHammer,
    translation: 'builderMode',
    roles: [Role.Builder],
  },
  {
    path: '/builder-mode/topics/create',
    Component: CreateTopicPage,
    translation: 'createTopic',
    roles: [Role.Builder],
  },
  {
    path: '/builder-mode/topics/:topicId/edit',
    Component: EditTopicPage,
    translation: 'editTopic',
    roles: [Role.Builder],
  },
  {
    path: '/topics/:topicId/details',
    Component: TopicDetailsPage,
    translation: 'topicDetails',
  },
];

export const router = createBrowserRouter(routes);
