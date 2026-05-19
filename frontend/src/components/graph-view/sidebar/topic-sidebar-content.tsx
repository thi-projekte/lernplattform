import {
  Button,
  Divider,
  Group,
  Progress,
  ThemeIcon,
  Text,
  Title,
  Stack,
  Tooltip,
} from '@mantine/core';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import { IconCheck, IconEdit, IconMessageCircle, IconRobot } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useUserService } from '../../../provider/user-provider';
import CategoryBadge from '../../category-badge.tsx';
import {
  useCompleteTopicManuallyMutation,
  useStartTopicMutation,
} from '../../../api/learn-progress.ts';
import { track } from '@plausible-analytics/tracker';

interface TopicSidebarContentProps {
  selectedElement: Topic;
}

const TopicSidebarContent = ({ selectedElement }: TopicSidebarContentProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const userService = useUserService();
  const currentUsername = userService.account.username?.toLowerCase();
  const isOwner =
    !!selectedElement.creatorId &&
    !!currentUsername &&
    selectedElement.creatorId.toLowerCase() === currentUsername;

  const { mutate: startTopic, isPending: isStarting } = useStartTopicMutation();
  const { mutate: completeTopic, isPending: isCompleting } = useCompleteTopicManuallyMutation();

  const learnProgress = selectedElement.learnProgress;
  const topicId = selectedElement.id;

  const contentElementIds = new Set(selectedElement.contentElements?.map((el) => el.id) ?? []);
  const completedCurrentIds = (learnProgress?.completedContentElementIds ?? []).filter((id) =>
    contentElementIds.has(id)
  );
  const totalContentElements = contentElementIds.size;
  const isManuallyCompleted = learnProgress?.status === 'COMPLETED_MANUALLY';
  const isAutoCompleted =
    totalContentElements > 0 && completedCurrentIds.length >= totalContentElements;
  const isCompleted = isManuallyCompleted || isAutoCompleted;
  const isStarted = !!learnProgress && !isCompleted;
  const progressPercent = isCompleted
    ? 100
    : totalContentElements > 0
      ? (completedCurrentIds.length / totalContentElements) * 100
      : 0;

  return (
    <>
      <Title order={3}>{selectedElement.title}</Title>
      <Text size="sm" c="dimmed">
        {t('topic.fields.author')}: {selectedElement.creatorFullName}
      </Text>
      <Group gap={6}>
        {selectedElement.categories?.map((cat) => (
          <CategoryBadge key={cat.id} title={cat.title} color={cat.color ?? '8b5cf6'} />
        ))}
      </Group>
      <Text size="sm" c="dimmed">
        {t('topic.fields.estimatedLearningDuration')}: {selectedElement.estimatedLearningDuration}{' '}
        {t('topic.fields.estimatedLearningDurationSuffix')}
      </Text>
      <Text size="sm">{selectedElement.teaser}</Text>

      {isOwner && (
        <Button
          leftSection={<IconEdit size={16} />}
          variant="light"
          color="blue"
          fullWidth
          mt="xl"
          onClick={() => navigate(`/builder-mode/topics/${selectedElement.id}/edit`)}
        >
          {t('common.edit')}
        </Button>
      )}

      {isCompleted ? (
        <Button
          color="green"
          variant="light"
          fullWidth
          mt="xl"
          disabled
          leftSection={<IconCheck size={16} />}
        >
          {t('topic.actions.completed')}
        </Button>
      ) : isStarted ? (
        <Tooltip
          label={t('topic.actions.completeBlockedHint')}
          disabled={progressPercent >= 100}
          withArrow
        >
          <Button
            color="green"
            fullWidth
            mt="xl"
            loading={isCompleting}
            onClick={() => topicId && completeTopic(topicId) && track('topicLearnCompletedManually', {props: {topicId: topicId ?? ''}})}
          >
            {t('topic.actions.complete')}
          </Button>
        </Tooltip>
      ) : (
        <Button
          color="blue"
          fullWidth
          mt="xl"
          loading={isStarting}
          onClick={() => topicId && startTopic(topicId) && track('topicLearnStarted', {props: {topicId: topicId ?? ''}})}
        >
          {t('topic.actions.start')}
        </Button>
      )}

      {learnProgress && (
        <Stack gap={4} mt="md">
          <Group justify="space-between">
            <Text size="xs" c="dimmed">
              {t('topic.progress.label')}
            </Text>
            <Text size="xs" fw={600}>
              {Math.round(progressPercent)}%
            </Text>
          </Group>
          <Progress value={progressPercent} color={isCompleted ? 'green' : 'blue'} />
        </Stack>
      )}

      <Divider mt="lg" />

      <Group gap="sm" mt="md">
        <ThemeIcon color="blue" radius="xl" variant="light">
          <IconRobot size={16} />
        </ThemeIcon>
        <div>
          <Text fw={600}>Myna</Text>
          <Text size="xs" c="dimmed">
            {t('topic.myna.subtitle')}
          </Text>
        </div>
      </Group>
      <Button
        variant="outline"
        color="blue"
        fullWidth
        mt="sm"
        leftSection={<IconMessageCircle size={16} />}
      >
        {t('topic.myna.askButton')}
      </Button>
    </>
  );
};

export default TopicSidebarContent;
