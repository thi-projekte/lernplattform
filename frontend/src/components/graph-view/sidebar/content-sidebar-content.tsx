import { ActionIcon, Badge, Group, Modal, ThemeIcon, Title } from '@mantine/core';
import type { AnyContentElementDto } from '../../../schemas/content-element';
import ContentElementDisplay from '../../topic/content-element-display';
import { useTranslation } from 'react-i18next';
import { useDisclosure } from '@mantine/hooks';
import { IconMaximize } from '@tabler/icons-react';
import { DEFAULT_ICON_BY_TYPE, resolveIcon } from '../../icon-picker/icons';

interface ContentSidebarContentProps {
  selectedElement: AnyContentElementDto;
}

const ContentSidebarContent = ({ selectedElement }: ContentSidebarContentProps) => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);
  const Icon =
    resolveIcon(selectedElement.icon) ?? resolveIcon(DEFAULT_ICON_BY_TYPE[selectedElement.type]);

  return (
    <>
      <Group justify="space-between" align="flex-start">
        <Group gap="sm" align="flex-start" wrap="nowrap">
          {Icon && (
            <ThemeIcon size={36} radius="md" variant="light" color="teal">
              <Icon size={22} />
            </ThemeIcon>
          )}
          <div>
            <Title order={3} style={{ lineHeight: 1.2 }}>
              {selectedElement.title}
            </Title>
            <Badge color="teal" mt="xs">
              {t(`topic.contentElementType.${selectedElement.type}`)}
            </Badge>
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
