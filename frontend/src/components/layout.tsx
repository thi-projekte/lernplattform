import {
  AppShell,
  Avatar,
  Box,
  Burger,
  Button,
  Group,
  Image,
  NavLink,
  useMantineTheme,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconChevronLeft, IconUser } from '@tabler/icons-react';
import { useQueryProfilePicture } from '../api/profile-picture.ts';
import { type FC, type ReactNode, useMemo, useState } from 'react';
import LanguagePicker from './language-picker.tsx';
import { useTranslation } from 'react-i18next';

import { routes, type TypedMyndRoute } from '../routing.ts';
import { useLocation, useMatches, useNavigate } from 'react-router';
import { isGranted } from '../auth.ts';
import AccessDenied from './access-denied.tsx';
import { useUserService } from '../provider/user-provider.tsx';

interface LayoutProps {
  children: ReactNode;
}

export const Layout: FC<LayoutProps> = ({ children }) => {
  const [opened, { toggle }] = useDisclosure();
  const [desktopExpanded, setDesktopExpanded] = useState(false);
  const { t } = useTranslation();
  const userService = useUserService();
  const { data: profilePicture } = useQueryProfilePicture(userService.account.username);
  const theme = useMantineTheme();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const matches = useMatches();

    // Prüfen, ob der aktuelle Nutzer bereits die Builder-Rolle hat
    const isBuilder = userService.roles.includes('builder');

    // Sidebar-Routen filtern
    const sidebarRoutes = routes.filter((r) => {
        // Wenn es die Become-Builder-Route ist und der Nutzer schon Builder ist -> Link ausblenden!
        if (r.path === '/become-builder' && isBuilder) {
            return false;
        }
        return r.isSidebar && r.path;
    });
  const userDisplayName =
    [userService.account.firstName, userService.account.lastName].filter(Boolean).join(' ') ||
    userService.account.username ||
    userService.account.email ||
    t('journey.genericUser');
  const userRole = userService.roles.includes('builder')
    ? t('auth.role_builder')
    : userService.roles.includes('learner')
      ? t('auth.role_learner')
      : undefined;

  const isActive = (path: string) => {
    if (path === '/') return pathname === '/';
    return pathname === path || pathname.startsWith(path + '/');
  };

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
      <AppShell.Header
        style={{
          background: theme.other.layoutHeaderBg,
          borderBottom: `1px solid ${theme.other.layoutBorder}`,
        }}
      >
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
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar
        p="md"
        onMouseEnter={() => setDesktopExpanded(true)}
        onMouseLeave={() => setDesktopExpanded(false)}
        style={{
          background: theme.other.layoutNavbarBg,
          borderRight: `1px solid ${theme.other.layoutBorder}`,
          transition: 'width 150ms ease',
          overflowX: 'hidden',
          display: 'flex',
          flexDirection: 'column',
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

        <Box mt="auto">
          <NavLink
            onClick={() => navigate('/account')}
            active={pathname === '/account'}
            title={t('layout.openAccount')}
            aria-label={t('layout.openAccount')}
            label={desktopExpanded ? userDisplayName : undefined}
            description={desktopExpanded ? userRole : undefined}
            leftSection={
              <Box
                style={{
                  width: 44,
                  height: 56,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                {profilePicture?.url ? (
                  <Avatar src={profilePicture.url} size={40} radius="50%" />
                ) : (
                  <IconUser size={32} stroke={1.5} />
                )}
              </Box>
            }
            styles={{
              root: {
                height: 56,
                borderRadius: 12,
                paddingInline: desktopExpanded ? undefined : 0,
                overflow: 'hidden',
                transition: 'background-color 150ms ease, color 150ms ease',
              },
              label: {
                fontWeight: 700,
              },
              body: {
                display: desktopExpanded ? undefined : 'none',
                minWidth: 0,
              },
              section: {
                marginInlineEnd: desktopExpanded ? undefined : 0,
                width: desktopExpanded ? 44 : '100%',
                minWidth: desktopExpanded ? 44 : '100%',
                justifyContent: 'center',
              },
            }}
          />
        </Box>
      </AppShell.Navbar>

      <AppShell.Main>
        <Box
          px="md"
          py="md"
          style={{
            minHeight: 'calc(100vh - 104px)',
            background: theme.other.layoutMainBg,
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
              {t('common.back')}
            </Button>
          )}
          {isCurrentRouteGranted ? children : <AccessDenied />}
        </Box>
      </AppShell.Main>
    </AppShell>
  );
};
