import { ActionIcon, Group, Modal, ThemeIcon, Title } from '@mantine/core';
import { createElement } from 'react';
import type { AnyContentElementDto } from '../../../schemas/content-element';
import ContentElementDisplay from '../../topic/content-element-display';
import { useTranslation } from 'react-i18next';
import { useDisclosure } from '@mantine/hooks';
import { IconMaximize } from '@tabler/icons-react';
import { CONTENT_ICONS, DEFAULT_ICON_BY_TYPE } from '../../icon-picker/icons';
import CategoryBadge from '../../category-badge.tsx';

interface ContentSidebarContentProps {
  selectedElement: AnyContentElementDto;
}

const ContentSidebarContent = ({ selectedElement }: ContentSidebarContentProps) => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);
  const iconComponent =
    (selectedElement.icon ? CONTENT_ICONS[selectedElement.icon] : undefined) ??
    CONTENT_ICONS[DEFAULT_ICON_BY_TYPE[selectedElement.type]];

  return (
    <>
      <Group justify="space-between" align="flex-start">
        <Group gap="sm" align="flex-start" wrap="nowrap">
          {iconComponent && (
            <ThemeIcon size={36} radius="md" variant="light" color="teal">
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
