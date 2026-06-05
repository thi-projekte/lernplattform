import {
  Alert,
  Badge,
  Button,
  Card,
  Divider,
  Group,
  Progress,
  Stack,
  Text,
  ThemeIcon,
  Title,
} from '@mantine/core';
import { IconAlertCircle, IconCheck, IconTrophy, IconX } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import {
  useFetchCurrentChallenge,
  useFetchChallengeHistory,
  useClaimRewardMutation,
} from '../api/challenge.ts';
import { notifications } from '@mantine/notifications';

const ChallengePage = () => {
  const { t } = useTranslation();
  const { data: challenge, isLoading, isError } = useFetchCurrentChallenge();
  const { data: history } = useFetchChallengeHistory();
  const { mutate: claim, isPending } = useClaimRewardMutation();

  const handleClaim = (id: string) => {
    claim(id, {
      onSuccess: () =>
        notifications.show({
          color: 'green',
          title: t('common.success'),
          message: t('challenge.rewardClaimed'),
        }),
    });
  };

  if (isLoading) return null;

  if (isError)
    return (
      <Alert icon={<IconAlertCircle size={16} />} color="red" m="md">
        {t('common.serverError')}
      </Alert>
    );

  const progress = challenge
    ? Math.min((challenge.currentCount / challenge.targetCount) * 100, 100)
    : 0;

  return (
    <Layout>
      <Stack p="md" maw={600}>
        <Title order={2}>{t('challenge.title')}</Title>
        <Text c="dimmed">{t('challenge.subtitle')}</Text>

        {challenge && (
          <Card shadow="sm" padding="xl" radius="md" withBorder>
            <Stack gap="md">
              <Group justify="space-between">
                <Text fw={600}>{t('challenge.weekly')}</Text>
                {challenge.completed && (
                  <Badge
                    color={challenge.rewardClaimed ? 'gray' : 'green'}
                    leftSection={<IconTrophy size={12} />}
                  >
                    {challenge.rewardClaimed ? t('challenge.claimed') : t('challenge.completed')}
                  </Badge>
                )}
              </Group>

              <Text size="sm" c="dimmed">
                {t('challenge.description', { target: challenge.targetCount })}
              </Text>

              <Progress value={progress} size="lg" radius="xl" />

              <Text size="sm" ta="right">
                {challenge.currentCount} / {challenge.targetCount} {t('challenge.nodes')}
              </Text>

              <Text size="xs" c="dimmed">
                {t('challenge.reward', { count: 2 })}
              </Text>

              {challenge.completed && !challenge.rewardClaimed && (
                <Button
                  leftSection={<IconTrophy size={16} />}
                  color="green"
                  loading={isPending}
                  onClick={() => handleClaim(challenge?.id)}
                >
                  {t('challenge.claimButton')}
                </Button>
              )}
            </Stack>
          </Card>
        )}

        <Divider my="md" label={t('challenge.history')} labelPosition="left" />

        {history && history.length === 0 && (
          <Text size="sm" c="dimmed">
            {t('challenge.noHistory')}
          </Text>
        )}

        {history?.map((c) => (
          <Card key={c.id} shadow="sm" padding="md" radius="md" withBorder>
            <Group justify="space-between">
              <Stack gap={2}>
                <Text size="sm" fw={600}>
                  {t('challenge.weekly')}
                </Text>
                <Text size="xs" c="dimmed">
                  {c.startDate} – {c.endDate}
                </Text>
              </Stack>
              <Group gap="xs">
                <Text size="sm">
                  {c.currentCount} / {c.targetCount}
                </Text>
                <ThemeIcon
                  size="sm"
                  radius="xl"
                  color={c.completed ? 'green' : 'red'}
                  variant="light"
                >
                  {c.completed ? <IconCheck size={12} /> : <IconX size={12} />}
                </ThemeIcon>
                {!c.rewardClaimed && (
                  <Button
                    leftSection={<IconTrophy size={16} />}
                    color="green"
                    loading={isPending}
                    onClick={() => handleClaim(c.id)}
                  >
                    {t('challenge.claimButton')}
                  </Button>
                )}
              </Group>
            </Group>
          </Card>
        ))}
      </Stack>
    </Layout>
  );
};

export default ChallengePage;
