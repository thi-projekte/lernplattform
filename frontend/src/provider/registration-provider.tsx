import { type ReactNode } from 'react';
import type { KeycloakResourceAccess } from 'keycloak-js';
import keycloak from '../keycloak.ts';
import Onboarding from '../pages/onboarding/onboarding.tsx';
import { Alert, Button, Stack, Text, Title } from '@mantine/core';
import { IconMailCheck, IconAlertCircle } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';

const NoAccessScreen = () => {
  const { t } = useTranslation();
  const params = new URLSearchParams(window.location.search);
  const hasInviteParams = params.get('id') && params.get('redemptionSecret');

  return (
    <Stack align="center" justify="center" h="100vh" bg="gray.0">
      <Alert
        icon={<IconAlertCircle size={20} />}
        color="red"
        variant="light"
        title={t('invitation.noAccess.title')}
        maw={420}
      >
        <Text size="sm" mt={4}>
          {t('invitation.noAccess.description')}
        </Text>
        {hasInviteParams && (
          <Button
            mt="sm"
            size="sm"
            leftSection={<IconMailCheck size={16} />}
            onClick={() =>
              (window.location.href = `/acceptInvite?${window.location.search.slice(1)}`)
            }
          >
            {t('invitation.noAccess.acceptButton')}
          </Button>
        )}
      </Alert>
      <Title order={5} c="dimmed">
        {t('invitation.noAccess.contactHint')}
      </Title>
    </Stack>
  );
};

const RegistrationProvider = ({ children }: { children: ReactNode }) => {
  const resourceAccess: KeycloakResourceAccess = keycloak.tokenParsed
    ?.resource_access as KeycloakResourceAccess;
  const roles: string[] = resourceAccess['mynd']?.roles ?? [];

  const isAuthorized = roles.includes('authorizedUser');
  const hasAppRole = roles.some((r) => r === 'builder' || r === 'learner');

  // Let /acceptInvite through so users without a role can redeem invitations
  if (window.location.pathname === '/acceptInvite') {
    return <>{children}</>;
  }

  // Users with no roles at all and no authorization need an invitation
  if (!isAuthorized && !hasAppRole) {
    return <NoAccessScreen />;
  }

  // Authorized but hasn't selected a role yet → onboarding
  if (!hasAppRole) {
    return <Onboarding withoutLayout />;
  }

  return <>{children}</>;
};

export default RegistrationProvider;
