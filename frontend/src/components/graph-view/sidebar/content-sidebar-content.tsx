import { Badge, Title, Modal, ActionIcon, Group } from '@mantine/core';
import type { AnyContentElementDto } from '../../../schemas/content-element';
import ContentElementDisplay from '../../topic/content-element-display';
import { useTranslation } from 'react-i18next';
import { useDisclosure } from '@mantine/hooks';
import { IconMaximize } from '@tabler/icons-react';

interface ContentSidebarContentProps {
  selectedElement: AnyContentElementDto;
}

const ContentSidebarContent = ({ selectedElement }: ContentSidebarContentProps) => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);

  return (
    <>
      <Group justify="space-between" align="flex-start">
        <div>
          <Title order={3} style={{ lineHeight: 1.2 }}>
            {selectedElement.title}
          </Title>
          <Badge color="teal" mt="xs">
            {t(`topic.contentElementType.${selectedElement.type}`)}
          </Badge>
        </div>
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
