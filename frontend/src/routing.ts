import { type BaseRouteObject, createBrowserRouter } from 'react-router';
import Homepage from './pages/home.tsx';
import BuilderModeListPage from './pages/builder-mode/list.tsx';
import type { ComponentType } from 'react';
// Icon für den neuen Eintrag importieren
import { IconHammer, IconHome, IconUser, type IconProps } from '@tabler/icons-react';
import CreateTopicPage from './pages/builder-mode/create.tsx';
import EditTopicPage from './pages/builder-mode/edit.tsx';
import TopicDetailsPage from './pages/topic/details.tsx';
import AccountPage from './pages/account.tsx';
import OnboardingPage from './pages/onboarding/onboarding.tsx';
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
  // --- NEUER EINTRAG FÜR DAS ONBOARDING IN DER SIDEBAR ---
  {
    path: '/become-builder',
    Component: OnboardingPage,
    isSidebar: true,
    icon: IconUser,
    translation: 'becomeBuilder', // Den Key 'routes.becomeBuilder' musst du noch in deine locales (z.B. de.json) aufnehmen!
  },
  // --------------------------------------------------------
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
  {
    path: '/account',
    Component: AccountPage,
    translation: 'account',
  },
];

export const router = createBrowserRouter(routes);
