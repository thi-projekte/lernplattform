import { type BaseRouteObject, createBrowserRouter } from 'react-router';
import Homepage from './pages/home.tsx';
import BuilderModeListPage from './pages/builder-mode/list.tsx';
import type { ComponentType } from 'react';
import { IconHammer, IconHome, type IconProps } from '@tabler/icons-react';
import CreateTopicPage from './pages/builder-mode/create.tsx';
import EditTopicPage from './pages/builder-mode/edit.tsx';
import TopicDetailsPage from './pages/topic/details.tsx';

export interface TypedMyndRoute extends BaseRouteObject {
  isSidebar?: boolean;
  icon?: ComponentType<IconProps>;
  translation?: string;
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
  },
  {
    path: '/builder-mode/topics/create',
    Component: CreateTopicPage,
    translation: 'createTopic',
  },
  {
    path: '/builder-mode/topics/:topicId/edit',
    Component: EditTopicPage,
    translation: 'editTopic',
  },
  {
    path: '/topics/:topicId/details',
    Component: TopicDetailsPage,
    translation: 'topicDetails'
  }
];

export const router = createBrowserRouter(routes);
