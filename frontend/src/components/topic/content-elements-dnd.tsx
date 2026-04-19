import { Button, Flex, Paper, Stack, Text, ThemeIcon } from '@mantine/core';
import { IconGripVertical, IconPlusFilled } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import type { Topic } from '../../schemas/topic.ts';
import { useDisclosure } from '@mantine/hooks';
import CreateContentElementModal from './modal/create-content-element.tsx';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import { DragDropContext, Draggable, Droppable, type OnDragEndResponder } from '@hello-pangea/dnd';
import { useMemo } from 'react';

interface ContentElementsDndProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const ContentElementsDnd = ({topic, setTopic}: ContentElementsDndProps) => {

  const {t} = useTranslation();

  const [opened, {close, open}] = useDisclosure(false);

  const addContentElement = (contentElement: AnyContentElementDto) => {
      setTopic({
        ...topic,
        contentElements: [...(topic.contentElements ?? []), contentElement]
      });
  }

  const sortedContentElements = useMemo<AnyContentElementDto[]>(() => {
    return (topic.contentElements ?? [])
      .sort((a, b) => (a.rank ?? 0) - (b.rank ?? 0))
  }, [topic.contentElements])

  const onDragEnd: OnDragEndResponder<string> = (result) => {
    if (!result.destination) return;

    const items: AnyContentElementDto[] = Array.from(sortedContentElements);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);

    const updatedItems = items.map((item, index) => ({
      ...item,
      rank: index,
    }));

    setTopic({
      ...topic,
      contentElements: updatedItems,
    });
  };

  return (
    <>
      <Flex justify="flex-end" w="100%" mt={12} mb={12}>
        <Button variant="filled" onClick={open}>
          <IconPlusFilled />
          &nbsp;{t('topic.actions.createContentElement')}
        </Button>
      </Flex>
      <CreateContentElementModal
        opened={opened}
        onClose={close}
        onAddContentElement={addContentElement}
      />

      <DragDropContext onDragEnd={onDragEnd}>
        <Droppable droppableId="column">
          {(provided) => (
            <Stack {...provided.droppableProps} ref={provided.innerRef} mb="xl">
              {sortedContentElements.map((item, index) => (
                <Draggable key={item.id} draggableId={item.id} index={index}>
                  {(provided, snapshot) => (
                    <Paper
                      withBorder
                      p="sm"
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      shadow={snapshot.isDragging ? 'md' : 'xs'}
                      style={{
                        ...provided.draggableProps.style,
                        display: 'flex',
                        alignItems: 'center',
                      }}
                    >
                      <div {...provided.dragHandleProps} style={{ marginRight: '10px' }}>
                        <ThemeIcon variant="subtle" color="gray">
                          <IconGripVertical size={18} />
                        </ThemeIcon>
                      </div>
                      <Stack>
                        <Text fw={500}>{item.title}</Text>
                      </Stack>
                    </Paper>
                  )}
                </Draggable>
              ))}
            </Stack>
          )}
        </Droppable>
      </DragDropContext>
    </>
  );
}

export default ContentElementsDnd;