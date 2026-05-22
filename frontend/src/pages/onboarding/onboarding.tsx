import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card, Select, Button, Title, Stack, Group, Text } from '@mantine/core';
import { useMakeUserBuilder, useMakeUserLearner } from '../../api/auth.ts';
import type { AxiosResponse } from 'axios';
import { track } from '@plausible-analytics/tracker';
import { useUserService } from '../../provider/user-provider.tsx';
import { Layout } from '../../components/layout.tsx';
import { useLocation } from 'react-router';

const Onboarding = () => {
  const { t } = useTranslation();
  const userService = useUserService();
  const location = useLocation();

  const hasLearnerRole = userService.roles.includes('learner');
  const isBecomeBuilderRoute = location.pathname === '/become-builder';

  const isUpgradeMode = hasLearnerRole || isBecomeBuilderRoute;

  const [role, setRole] = useState<string | null>(isUpgradeMode ? 'builder' : null);

  const { mutateAsync: makeBuilder, isPending: isBuilderLoading } = useMakeUserBuilder();
  const { mutateAsync: makeLearner, isPending: isLearnerLoading } = useMakeUserLearner();

  const handleSubmit = async () => {
    if (!role) return;

    let result: AxiosResponse | undefined = undefined;

    if (role === 'builder') {
      result = await makeBuilder();
    } else if (role === 'learner') {
      result = await makeLearner();
    }

    if (result?.status === 201) {
      track('successfulRoleOnboarding', { props: { role } });
      window.location.href = '/';
    }
  };

  const isSubmitting = isBuilderLoading || isLearnerLoading;

  const roleOptions = [
    { value: 'builder', label: t('auth.role_builder') },
    { value: 'learner', label: t('auth.role_learner') },
  ];

  return (
    <Layout>
      <Stack align="center" justify="center" mt="xl">
        <Card shadow="sm" padding="xl" radius="md" withBorder w={400}>
          <Stack>
            <Title order={3}>{t('onboarding.title')}</Title>

            {/* 3. Dropdown komplett ausblenden, wenn der User den Builder-Link geklickt hat */}
            {isUpgradeMode ? (
              <Text size="sm" style={{ lineHeight: 1.5 }}>
                Klicke auf den Button unten, um dein Konto auf <strong>Builder</strong> hochzustufen
                und eigene Lerninhalte zu erstellen.
              </Text>
            ) : (
              // Klassisches Dropdown bleibt NUR für neue User auf der Haupt-Route
              <Select
                label={t('onboarding.role_selection_label')}
                placeholder={t('onboarding.role_selection_placeholder')}
                data={roleOptions}
                value={role}
                onChange={setRole}
              />
            )}

            <Group justify="flex-end" mt="md">
              <Button onClick={handleSubmit} loading={isSubmitting} disabled={!role}>
                {isUpgradeMode ? 'Jetzt Builder werden' : t('onboarding.submit')}
              </Button>
            </Group>
          </Stack>
        </Card>
      </Stack>
    </Layout>
  );
};

export default Onboarding;
