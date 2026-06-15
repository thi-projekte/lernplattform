import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card, Select, Button, Title, Stack, Group, Text, Container } from '@mantine/core';
import { useMakeUserBuilder, useMakeUserLearner } from '../../api/auth.ts';
import type { AxiosResponse } from 'axios';
import { track } from '@plausible-analytics/tracker';
import { useUserService } from '../../provider/user-provider.tsx';
import { Layout } from '../../components/layout.tsx';
import { markOnboardingTourPending } from '../../components/onboarding-tour.constants.ts';

const Onboarding = ({ withoutLayout }: { withoutLayout?: boolean }) => {
  const { t } = useTranslation();
  const userService = useUserService();

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
      markOnboardingTourPending();
      window.location.href = '/';
    }
  };

  const isSubmitting = isBuilderLoading || isLearnerLoading;

  const roleOptions = [
    { value: 'learner', label: t('auth.role_learner') },
    { value: 'builder', label: t('auth.role_builder') },
  ];

  const mainContent = (
    <Stack align="center" justify="center" mt="xl" style={{ height: '100%' }}>
      <Card shadow="sm" padding="xl" radius="md" withBorder w={400}>
        <Stack>
          <Title order={3}>{t('onboarding.title')}</Title>

          {isUpgradeMode ? (
            <Text size="sm" style={{ lineHeight: 1.5 }}>
              {t('onboarding.upgradeToBuilderText')}
            </Text>
          ) : (
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
              {t('onboarding.submit')}
            </Button>
          </Group>
        </Stack>
      </Card>
    </Stack>
  );

  if (withoutLayout) {
    return <Container style={{ height: '100vh' }}>{mainContent}</Container>;
  }

  return <Layout>{mainContent}</Layout>;
};

export default Onboarding;
