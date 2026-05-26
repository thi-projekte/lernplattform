import { Badge, Group, Paper, Stack, Text, ThemeIcon, Title } from '@mantine/core';
import { IconFlame } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import { useFetchStreaks } from '../api/streak.ts';
import LayoutLoader from '../components/layout-loader.tsx';
import type { StreakDto, StreakType } from '../schemas/streak.ts';

const streakDurationDays = (streak: StreakDto): number => {
  const end = streak.endedAt ? new Date(streak.endedAt) : new Date();
  const start = new Date(streak.startedAt);
  return Math.max(1, Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)));
};

const StreakPage = () => {
  const { t } = useTranslation();
  const { data: streaks, isLoading } = useFetchStreaks();

  if (isLoading) return <LayoutLoader />;

  const activeStreak = streaks?.find((s) => s.isActive);
  const history = streaks?.filter((s) => !s.isActive) ?? [];

  return (
    <Layout>
      <Stack gap="lg">
        <Stack gap={4}>
          <Title order={1}>{t('streak.title')}</Title>
          <Text c="dimmed">{t('streak.subtitle')}</Text>
        </Stack>

        {activeStreak && (
          <Paper withBorder radius="lg" p="lg">
            <Group gap="md" align="center">
              <ThemeIcon size={56} radius="xl" color="orange" variant="light">
                <IconFlame size={32} />
              </ThemeIcon>
              <div>
                <Text size="xs" c="dimmed" fw={500} tt="uppercase">
                  {t('streak.currentStreak')}
                </Text>
                <Group gap={6} align="baseline">
                  <Title order={2} c="orange">
                    {streakDurationDays(activeStreak)}
                  </Title>
                  <Text c="dimmed">{t(`streak.type.${activeStreak.type}`)}</Text>
                </Group>
                <Badge color={activeStreak.isSatisfied ? 'green' : 'orange'} variant="light" mt={4}>
                  {activeStreak.isSatisfied ? t('streak.satisfied') : t('streak.notSatisfied')}
                </Badge>
              </div>
            </Group>
          </Paper>
        )}

        {!activeStreak && (
          <Paper withBorder radius="lg" p="lg">
            <Group gap="md" align="center">
              <ThemeIcon size={48} radius="xl" color="gray" variant="light">
                <IconFlame size={28} />
              </ThemeIcon>
              <Text c="dimmed">{t('streak.noActiveStreak')}</Text>
            </Group>
          </Paper>
        )}

        {history.length > 0 && (
          <Stack gap="sm">
            <Title order={3}>{t('streak.history')}</Title>
            {history.map((streak) => (
              <Paper key={streak.id} withBorder radius="md" p="md">
                <Group justify="space-between" align="center">
                  <Group gap="sm">
                    <ThemeIcon size={36} radius="xl" color="gray" variant="light">
                      <IconFlame size={20} />
                    </ThemeIcon>
                    <div>
                      <Text fw={600} size="sm">
                        {streakDurationDays(streak)} {t(`streak.type.${streak.type as StreakType}`)}
                      </Text>
                      <Text size="xs" c="dimmed">
                        {new Date(streak.startedAt).toLocaleDateString()} –{' '}
                        {streak.endedAt
                          ? new Date(streak.endedAt).toLocaleDateString()
                          : t('streak.present')}
                      </Text>
                    </div>
                  </Group>
                </Group>
              </Paper>
            ))}
          </Stack>
        )}
      </Stack>
    </Layout>
  );
};

export default StreakPage;
