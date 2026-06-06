import { ActionIcon, Button, Group, Modal, Stack, ThemeIcon, Title, Tooltip } from '@mantine/core';
import { createElement } from 'react';
import type { AnyContentElementDto } from '../../../schemas/content-element';
import ContentElementDisplay from '../../topic/content-element-display';
import { useTranslation } from 'react-i18next';
import { useDisclosure } from '@mantine/hooks';
import { IconCheck, IconMaximize, IconRefresh } from '@tabler/icons-react';
import { CONTENT_ICONS, DEFAULT_ICON_BY_TYPE } from '../../icon-picker/icons';
import CategoryBadge from '../../category-badge.tsx';
import {
  useCompleteContentElementMutation,
  useResetContentElementMutation,
} from '../../../api/learn-progress.ts';
import type { TopicLearnProgressDto } from '../../../schemas/learn-progress.ts';
import { track } from '@plausible-analytics/tracker';
import { useSubscription } from '../../../provider/subscription-provider.tsx';
import { useNavigate } from 'react-router';

interface ContentSidebarContentProps {
  selectedElement: AnyContentElementDto;
  topicLearnProgress?: TopicLearnProgressDto | null;
}

const ContentSidebarContent = ({
  selectedElement,
  topicLearnProgress,
}: ContentSidebarContentProps) => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);
  const iconComponent =
    (selectedElement.icon ? CONTENT_ICONS[selectedElement.icon] : undefined) ??
    CONTENT_ICONS[DEFAULT_ICON_BY_TYPE[selectedElement.type]];

  const { mutate: completeContentElement, isPending } = useCompleteContentElementMutation();
  const { mutate: resetContentElement, isPending: isResetting } = useResetContentElementMutation();
  const { canLearnTopics } = useSubscription();
  const navigate = useNavigate();

  const topicStarted = !!topicLearnProgress;
  const isElementCompleted = !!topicLearnProgress?.completedContentElementIds.includes(
    selectedElement.id
  );
  const canMarkCompleted = topicStarted && !isElementCompleted;

  return (
    <>
      <Group justify="space-between" align="flex-start">
        <Group gap="sm" align="flex-start" wrap="nowrap">
          {iconComponent && (
            <ThemeIcon size={36} radius="md" variant="light" color="blue">
              {createElement(iconComponent, { size: 22 })}
            </ThemeIcon>
          )}
          <div>
            <Title order={3} style={{ lineHeight: 1.2 }}>
              {selectedElement.title}
            </Title>
            <CategoryBadge
              mt="xs"
              title={t(`topic.contentElementType.${selectedElement.type}`)}
              color="#0ca678"
            />
          </div>
        </Group>
        <ActionIcon variant="light" onClick={open} size="lg">
          <IconMaximize size={20} />
        </ActionIcon>
      </Group>

      <ContentElementDisplay contentElement={selectedElement} />

      {isElementCompleted ? (
        <Stack gap="xs">
          <Button
            color="green"
            variant="light"
            size="sm"
            disabled
            leftSection={<IconCheck size={14} />}
            style={{ alignSelf: 'flex-start' }}
          >
            {t('topic.actions.contentElementCompleted')}
          </Button>
          <Button
            variant="subtle"
            color="gray"
            size="xs"
            fullWidth
            leftSection={<IconRefresh size={14} />}
            loading={isResetting}
            onClick={() => resetContentElement(selectedElement.id)}
          >
            {t('topic.actions.resetProgress')}
          </Button>
        </Stack>
      ) : (
        <Stack gap="xs" style={{ alignSelf: 'flex-start' }}>
          <Tooltip
            label={!canLearnTopics ? t('subscription.upgradeToLearn') : t('topic.actions.start')}
            disabled={canLearnTopics && topicStarted}
            withArrow
          >
            <Button
              color="blue"
              size="sm"
              disabled={!canLearnTopics || !canMarkCompleted}
              loading={isPending}
              onClick={() => {
                completeContentElement(selectedElement.id);
                track('contentElementCompleted', {
                  props: { contentElementId: selectedElement.id },
                });
              }}
              leftSection={<IconCheck size={14} />}
            >
              {t('topic.actions.markContentElementCompleted')}
            </Button>
          </Tooltip>
          {!canLearnTopics && (
            <Button
              size="sm"
              color="yellow"
              variant="light"
              onClick={() => navigate('/subscription')}
            >
              {t('subscription.goPremium')}
            </Button>
          )}
        </Stack>
      )}

      <Modal
        opened={opened}
        onClose={close}
        size="xl"
        title={<Title order={3}>{selectedElement.title}</Title>}
      >
        <ContentElementDisplay contentElement={selectedElement} />
      </Modal>
    </>
  );
};

export default ContentSidebarContent;
