import {
  Button,
  Group,
  Progress,
  Text,
  Title,
  Divider,
  Stack,
  ThemeIcon,
  Tooltip,
} from '@mantine/core';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import { IconCheck, IconEdit, IconRobot } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useUserService } from '../../../provider/user-provider';
import CategoryBadge from '../../category-badge.tsx';
import { useCompleteTopicMutation, useStartTopicMutation } from '../../../api/learn-progress.ts';

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
  const { mutate: completeTopic, isPending: isCompleting } = useCompleteTopicMutation();

  const learnProgress = selectedElement.learnProgress;
  const topicId = selectedElement.id;
  const isStarted = !!learnProgress && !learnProgress.completed;
  const isCompleted = !!learnProgress?.completed;
  const progressPercent = (learnProgress?.percentageCompleted ?? 0) * 100;

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
            disabled={progressPercent < 100}
            onClick={() => topicId && completeTopic(topicId)}
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
          onClick={() => topicId && startTopic(topicId)}
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

      <Divider mt="xl" />

      <Stack gap="sm" mt="md">
        <Group>
          <ThemeIcon color="blue" radius="xl">
            <IconRobot size={16} />
          </ThemeIcon>
          <Text fw={500}>Myna</Text>
          <Text size="xs" c="dimmed">
            {t('topic.myna.subtitle')}
          </Text>
        </Group>
        <Text size="sm" c="dimmed">
          {t('topic.myna.description')}
        </Text>
        <Button variant="outline" color="blue" fullWidth>
          {t('topic.myna.askButton')}
        </Button>
      </Stack>
    </>
  );
};

export default TopicSidebarContent;
