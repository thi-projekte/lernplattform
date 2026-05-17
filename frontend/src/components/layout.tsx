import { ActionIcon, AppShell, Box, Burger, Button, Group, Image, NavLink } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconChevronLeft, IconUser } from '@tabler/icons-react';
import { type FC, type ReactNode, useMemo, useState } from 'react';
import LanguagePicker from './language-picker.tsx';
import { useTranslation } from 'react-i18next';

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
      header={{ height: 104 }}
      navbar={{
        width: desktopNavbarWidth,
        breakpoint: 'sm',
        collapsed: { mobile: !opened },
      }}
      padding={0}
    >
      <AppShell.Header style={{ backgroundColor: 'var(--mantine-color-brandGray-5)' }}>
        <Group h="100%" justify="space-between" wrap="nowrap">
          <Box
            visibleFrom="sm"
            style={{
              width: 80,
              minWidth: 80,
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              paddingLeft: 83,
              overflow: 'visible',
              transition: 'width 150ms ease',
            }}
          >
            <Image src="/mynd-logo.png" alt="MYnd Logo" w={250} h="auto" fit="contain" />
          </Box>

          <Group hiddenFrom="sm" gap="sm" h="100%" px="md">
            <Burger opened={opened} onClick={toggle} size="sm" />
            <Image src="/mynd-logo.png" alt="MYnd Logo" h={58} w="auto" fit="contain" />
          </Group>
          <Group px="md">
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
          backgroundColor: 'var(--mantine-color-brandGray-4)',
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
                styles={{
                  root: {
                    borderRadius: 12,
                    paddingInline: desktopExpanded ? undefined : 0,
                  },
                  body: {
                    display: desktopExpanded ? undefined : 'none',
                  },
                  section: {
                    marginInlineEnd: desktopExpanded ? undefined : 0,
                    width: desktopExpanded ? undefined : '100%',
                    justifyContent: 'center',
                  },
                }}
              />
            );
          })}
      </AppShell.Navbar>

      <AppShell.Main>
        <Box
          px="md"
          py="md"
          bg="brandGray.2"
          style={{
            minHeight: 'calc(100vh - 104px)',
          }}
        >
          {pathname !== '/' && (
            <Button
              variant="subtle"
              color="gray"
              leftSection={<IconChevronLeft size={16} stroke={2} />}
              onClick={() => navigate(-1)}
              mb="md"
              px="xs"
              size="sm"
              styles={{
                root: { color: 'var(--mantine-color-dimmed)' },
                label: { fontWeight: 400 },
              }}
            >
              Zurück
            </Button>
          )}
          {isCurrentRouteGranted ? children : <AccessDenied />}
        </Box>
      </AppShell.Main>
    </AppShell>
  );
};
