import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card, Select, Button, Title, Stack, Group } from '@mantine/core';
import { useMakeUserBuilder, useMakeUserLearner } from '../../api/auth.ts';
import type { AxiosResponse } from 'axios';
import { track } from '@plausible-analytics/tracker';

const Onboarding = () => {
  const { t } = useTranslation();
  const [role, setRole] = useState<string | null>(null);

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
      track('successfulRoleOnboarding', {props: {role}});
      window.location.reload();
    }
  };

  const isSubmitting = isBuilderLoading || isLearnerLoading;

  return (
    <Stack align="center" justify="center" h="100vh" bg="gray.0">
      <Card shadow="sm" padding="xl" radius="md" withBorder w={400}>
        <Stack>
          <Title order={3}>{t('onboarding.title')}</Title>
          <Select
            label={t('onboarding.role_selection_label')}
            placeholder={t('onboarding.role_selection_placeholder')}
            data={[
              { value: 'builder', label: t('auth.role_builder') },
              { value: 'learner', label: t('auth.role_learner') },
            ]}
            value={role}
            onChange={setRole}
          />

          <Group justify="flex-end" mt="md">
            <Button onClick={handleSubmit} loading={isSubmitting} disabled={!role}>
              {t('onboarding.submit')}
            </Button>
          </Group>
        </Stack>
      </Card>
    </Stack>
  );
};

export default Onboarding;
