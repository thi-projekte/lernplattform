import { ActionIcon, AppShell, Burger, Group, Image, NavLink } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconUser } from '@tabler/icons-react';
import { type FC, type ReactNode } from 'react';
import LanguagePicker from './language-picker.tsx';
import { useTranslation } from 'react-i18next';

import logo from '../assets/logo.png';
import keycloak from '../keycloak.ts';
import { routes } from '../routing.ts';
import { useLocation, useNavigate } from 'react-router';
import { isGranted } from '../auth.ts';

interface LayoutProps {
  children: ReactNode;
}

export const Layout: FC<LayoutProps> = ({ children }) => {
  const [opened, { toggle }] = useDisclosure();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const sidebarRoutes = routes.filter((r) => r.isSidebar && r.path);

  const longestActiveTarget = sidebarRoutes
    .filter((route) => pathname.indexOf(route.path ?? '') > -1)
    .reduce((longest, current) => {
      return (current.path?.length || 0) > (longest.path?.length || 0) ? current : longest;
    }, {});

  const isActive = (path: string) => longestActiveTarget.path === path;

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={{ width: 300, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group>
            <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
            <Image src={logo} alt="MYnd Logo" h={45} w="auto" fit="contain" />
          </Group>

          <Group>
            <LanguagePicker />
            <ActionIcon variant="default" size="xl" onClick={() => keycloak.accountManagement()}>
              <IconUser size={32} stroke={1.5} />
            </ActionIcon>
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        {sidebarRoutes.filter((r) => r.roles ? isGranted(r.roles) : true).map((route) => (
          <NavLink
            label={route.translation ? t(`routes.${route.translation}`) : undefined}
            leftSection={route.icon ? <route.icon size={32} stroke={1.5} /> : undefined}
            active={isActive(route.path ?? '')}
            onClick={() => navigate(route.path ?? '')}
            key={route.path}
          />
        ))}
      </AppShell.Navbar>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
};
