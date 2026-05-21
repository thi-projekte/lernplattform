import {
  Box,
  Button,
  Collapse,
  Divider,
  Group,
  Paper,
  Progress,
  Text,
  TextInput,
  ThemeIcon,
  Title,
  Stack,
  Tooltip,
  UnstyledButton,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import {
  IconCheck,
  IconChevronDown,
  IconChevronUp,
  IconEdit,
  IconRobot,
  IconSend,
} from '@tabler/icons-react';
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

  const [mynaOpen, { toggle: toggleMyna }] = useDisclosure(false);

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
    <Stack gap="sm">
      <div>
        <Title order={3} mb={6}>
          {selectedElement.title}
        </Title>
        <Group gap={6} c="dimmed">
          <Text size="xs">{selectedElement.creatorFullName}</Text>
          {selectedElement.estimatedLearningDuration && (
            <>
              <Text size="xs">·</Text>
              <Text size="xs">
                {selectedElement.estimatedLearningDuration}{' '}
                {t('topic.fields.estimatedLearningDurationSuffix')}
              </Text>
            </>
          )}
        </Group>
        {selectedElement.categories && selectedElement.categories.length > 0 && (
          <Group gap={6} mt={8}>
            {selectedElement.categories.map((cat) => (
              <CategoryBadge key={cat.id} title={cat.title} color={cat.color ?? '8b5cf6'} />
            ))}
          </Group>
        )}
      </div>

      {selectedElement.teaser && (
        <Text size="sm" c="dimmed" style={{ lineHeight: 1.6 }}>
          {selectedElement.teaser}
        </Text>
      )}

      <Divider />

      <Group gap="xs" wrap="wrap">
        {isOwner && (
          <Button
            leftSection={<IconEdit size={15} />}
            variant="light"
            color="blue"
            size="sm"
            onClick={() => navigate(`/builder-mode/topics/${selectedElement.id}/edit`)}
          >
            {t('common.edit')}
          </Button>
        )}

        {isCompleted ? (
          <Button size="sm" color="green" variant="light" disabled leftSection={<IconCheck size={15} />}>
            {t('topic.actions.completed')}
          </Button>
        ) : isStarted ? (
          <Tooltip
            label={t('topic.actions.completeBlockedHint')}
            disabled={progressPercent >= 100}
            withArrow
          >
            <Button
              size="sm"
              color="green"
              loading={isCompleting}
              onClick={() => {
                if (topicId) {
                  track('topicLearnCompletedManually', { props: { topicId } });
                  completeTopic(topicId);
                }
              }}
            >
              {t('topic.actions.complete')}
            </Button>
          </Tooltip>
        ) : (
          <Button
            size="sm"
            color="blue"
            loading={isStarting}
            onClick={() => {
              if (topicId) {
                track('topicLearnStarted', { props: { topicId } });
                startTopic(topicId);
              }
            }}
          >
            {t('topic.actions.start')}
          </Button>
        )}
      </Group>

      {learnProgress && (
        <Paper withBorder radius="md" p="sm" bg="gray.0">
          <Group justify="space-between" mb={6}>
            <Text size="xs" c="dimmed" fw={500}>
              {t('topic.progress.label')}
            </Text>
            <Text size="xs" fw={700} c={isCompleted ? 'green.7' : 'blue.7'}>
              {Math.round(progressPercent)}%
            </Text>
          </Group>
          <Progress value={progressPercent} color={isCompleted ? 'green' : 'blue'} size="sm" radius="xl" />
          {totalContentElements > 0 && (
            <Text size="xs" c="dimmed" mt={6}>
              {completedCurrentIds.length} / {totalContentElements}{' '}
              {t('topic.progress.elementsCompleted')}
            </Text>
          )}
        </Paper>
      )}

      <Divider />

      <UnstyledButton onClick={toggleMyna} style={{ borderRadius: 8 }}>
        <Group justify="space-between" align="center" p={4}>
          <Group gap="sm">
            <ThemeIcon color="blue" radius="xl" variant="light" size="md">
              <IconRobot size={15} />
            </ThemeIcon>
            <div>
              <Text fw={600} size="sm">
                Myna
              </Text>
              <Text size="xs" c="dimmed">
                {t('topic.myna.subtitle')}
              </Text>
            </div>
          </Group>
          {mynaOpen ? (
            <IconChevronUp size={16} color="gray" />
          ) : (
            <IconChevronDown size={16} color="gray" />
          )}
        </Group>
      </UnstyledButton>

      <Collapse in={mynaOpen}>
        <Stack gap="xs">
          <Box
            style={(theme) => ({
              background: theme.colors.gray[0],
              border: `1px solid ${theme.colors.gray[2]}`,
              borderRadius: theme.radius.md,
              minHeight: 110,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            })}
            p="md"
          >
            <Stack align="center" gap={6}>
              <ThemeIcon color="blue" variant="light" size="xl" radius="xl">
                <IconRobot size={22} />
              </ThemeIcon>
              <Text size="xs" c="dimmed" ta="center">
                {t('topic.myna.comingSoon')}
              </Text>
            </Stack>
          </Box>
          <Group gap="xs" wrap="nowrap">
            <TextInput
              placeholder={t('topic.myna.inputPlaceholder')}
              disabled
              style={{ flex: 1 }}
              size="sm"
              radius="md"
            />
            <Button size="sm" variant="light" color="blue" disabled px="sm" radius="md">
              <IconSend size={15} />
            </Button>
          </Group>
        </Stack>
      </Collapse>
    </Stack>
  );
};

export default TopicSidebarContent;
