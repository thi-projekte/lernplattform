import { ActionIcon, AppShell, Box, Burger, Group, Image, NavLink } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconUser } from '@tabler/icons-react';
import { type FC, type ReactNode, useMemo, useState } from 'react';
import LanguagePicker from './language-picker.tsx';
import { useTranslation } from 'react-i18next';

import logo from '../assets/logo.png';
import keycloak from '../keycloak.ts';
import { routes, type TypedMyndRoute } from '../routing.ts';
import { useLocation, useMatches, useNavigate } from 'react-router';
import { isGranted } from '../auth.ts';
import AccessDenied from './access-denied.tsx';

interface LayoutProps {
  children: ReactNode;
}

export const Layout: FC<LayoutProps> = ({ children }) => {
  const [opened, { toggle }] = useDisclosure();
  const [desktopExpanded, setDesktopExpanded] = useState(false);
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const matches = useMatches();

  const sidebarRoutes = routes.filter((r) => r.isSidebar && r.path);

  const longestActiveTarget = sidebarRoutes
    .filter((route) => pathname.indexOf(route.path ?? '') > -1)
    .reduce((longest, current) => {
      return (current.path?.length || 0) > (longest.path?.length || 0) ? current : longest;
    }, {});

  const isActive = (path: string) => longestActiveTarget.path === path;

  const matchingRoute = useMemo<TypedMyndRoute | null>(() => {
    if (matches.length > 0) {
      return routes[parseInt(matches[matches.length - 1].id, 10)];
    }
    return null;
  }, [matches]);

  const isCurrentRouteGranted = useMemo(
    () => (matchingRoute?.roles ? isGranted(matchingRoute.roles) : true),
    [matchingRoute]
  );

  const desktopNavbarWidth = desktopExpanded ? 280 : 76;

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={{
        width: desktopNavbarWidth,
        breakpoint: 'sm',
        collapsed: { mobile: !opened },
      }}
      padding={0}
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

      <AppShell.Navbar
        p="md"
        onMouseEnter={() => setDesktopExpanded(true)}
        onMouseLeave={() => setDesktopExpanded(false)}
        style={{
          transition: 'width 150ms ease',
          overflowX: 'hidden',
        }}
      >
        {sidebarRoutes
          .filter((r) => (r.roles ? isGranted(r.roles) : true))
          .map((route) => {
            const routeLabel = route.translation ? t(`routes.${route.translation}`) : undefined;

            return (
              <NavLink
                label={desktopExpanded ? routeLabel : undefined}
                leftSection={route.icon ? <route.icon size={32} stroke={1.5} /> : undefined}
                active={isActive(route.path ?? '')}
                onClick={() => navigate(route.path ?? '')}
                key={route.path}
                title={routeLabel}
                aria-label={routeLabel}
                style={{
                  borderRadius: 12,
                }}
              />
            );
          })}
      </AppShell.Navbar>

      <AppShell.Main>
        <Box px="md" py="md">
          {isCurrentRouteGranted ? children : <AccessDenied />}
        </Box>
      </AppShell.Main>
    </AppShell>
  );
};
