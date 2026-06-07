import { type BaseRouteObject, createBrowserRouter } from 'react-router';
import Homepage from './pages/home.tsx';
import BuilderModeListPage from './pages/builder-mode/list.tsx';
import LegalPage from './pages/LegalPage.tsx';
import type { ComponentType } from 'react';
import {
  IconFlame,
  IconHammer,
  IconHome,
  IconMail,
  IconReportMoney,
  IconTree,
  type IconProps,
} from '@tabler/icons-react';
import CreateTopicPage from './pages/builder-mode/create.tsx';
import EditTopicPage from './pages/builder-mode/edit.tsx';
import TopicDetailsPage from './pages/topic/details.tsx';
import AccountPage from './pages/account.tsx';
import ManageInvitationsPage from './pages/invitations/manage.tsx';
import AcceptInviteRoute from './pages/invitations/accept-route.tsx';
import OnboardingPage from './pages/onboarding/onboarding.tsx';
import SubscriptionPage from './pages/subscription.tsx';
import StreakPage from './pages/streak.tsx';
import ChallengePage from './pages/challenge.tsx';
import { IconTrophy } from '@tabler/icons-react';
import AdminCategoriesPage from './pages/admin/categories.tsx';
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
    path: '/challenges',
    Component: ChallengePage,
    isSidebar: true,
    icon: IconTrophy,
    translation: 'challenges',
  },
  {
    path: '/become-builder',
    Component: OnboardingPage,
    isSidebar: true,
    icon: IconHammer,
    translation: 'becomeBuilder',
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
  {
    path: '/account',
    Component: AccountPage,
    translation: 'account',
  },
  {
    path: '/streaks',
    Component: StreakPage,
    isSidebar: true,
    icon: IconFlame,
    translation: 'streaks',
  },
  {
    path: '/invitations',
    Component: ManageInvitationsPage,
    isSidebar: true,
    icon: IconMail,
    translation: 'invitations',
  },
  {
    path: '/acceptInvite',
    Component: AcceptInviteRoute,
  },
  {
    path: '/subscription',
    Component: SubscriptionPage,
    translation: 'subscription',
    isSidebar: true,
    icon: IconReportMoney,
  },
  {
    path: '/legal',
    Component: LegalPage,
    translation: 'legal',
  },
  {
    path: '/admin/categories',
    Component: AdminCategoriesPage,
    isSidebar: true,
    icon: IconTree,
    translation: 'adminCategories',
    roles: [Role.Admin],
  },
];

export const router = createBrowserRouter(routes);
