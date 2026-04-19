import { Button, Flex, Text } from '@mantine/core';
import { IconPlusFilled } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import type { Topic } from '../../schemas/topic.ts';
import { useDisclosure } from '@mantine/hooks';
import CreateContentElementModal from './modal/create-content-element.tsx';
import { useState } from 'react';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';

interface ContentElementsDndProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const ContentElementsDnd = ({topic, setTopic}: ContentElementsDndProps) => {

  const {t} = useTranslation();

  const [opened, {close, open}] = useDisclosure(false);

  const [newElements, setNewElements] = useState<AnyContentElementDto[]>([]);

  const addContentElement = (contentElement: AnyContentElementDto) => {
      setNewElements((prev) => [...prev, contentElement]);
      setTopic({
        ...topic,
        contentElementIds: [...(topic.contentElementIds ?? []), contentElement.id]
      });
  }

  return (
    <>
      <Flex justify="flex-end" w="100%" mt={12}>
        <Button variant="filled" onClick={open}>
          <IconPlusFilled />
          &nbsp;{t('topic.actions.createContentElement')}
        </Button>
      </Flex>
      <CreateContentElementModal opened={opened} onClose={close} onAddContentElement={addContentElement} />

      {newElements.map((el) => <Text>{JSON.stringify(el)}</Text>)}
    </>
  );
}

export default ContentElementsDnd;