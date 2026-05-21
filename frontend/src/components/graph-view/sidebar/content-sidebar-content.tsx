import { ActionIcon, Button, Group, Modal, ThemeIcon, Title, Tooltip } from '@mantine/core';
import { createElement } from 'react';
import type { AnyContentElementDto } from '../../../schemas/content-element';
import ContentElementDisplay from '../../topic/content-element-display';
import { useTranslation } from 'react-i18next';
import { useDisclosure } from '@mantine/hooks';
import { IconCheck, IconMaximize } from '@tabler/icons-react';
import { CONTENT_ICONS, DEFAULT_ICON_BY_TYPE } from '../../icon-picker/icons';
import CategoryBadge from '../../category-badge.tsx';
import { useCompleteContentElementMutation } from '../../../api/learn-progress.ts';
import type { TopicLearnProgressDto } from '../../../schemas/learn-progress.ts';
import { track } from '@plausible-analytics/tracker';

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
      ) : (
        <Tooltip label={t('topic.actions.start')} disabled={topicStarted} withArrow>
          <Button
            color="blue"
            size="sm"
            disabled={!canMarkCompleted}
            loading={isPending}
            onClick={() => {
              completeContentElement(selectedElement.id);
              track('contentElementCompleted', {
                props: { contentElementId: selectedElement.id },
              });
            }}
            leftSection={<IconCheck size={14} />}
            style={{ alignSelf: 'flex-start' }}
          >
            {t('topic.actions.markContentElementCompleted')}
          </Button>
        </Tooltip>
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
