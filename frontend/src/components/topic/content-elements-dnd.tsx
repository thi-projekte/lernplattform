import { ActionIcon, Button, Flex, Paper, Stack, ThemeIcon, Title, Tooltip } from '@mantine/core';
import { IconGripVertical, IconPlusFilled, IconTrash } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import type { Topic } from '../../schemas/topic.ts';
import { useDisclosure } from '@mantine/hooks';
import CreateContentElementModal from './modal/create-content-element.tsx';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import { DragDropContext, Draggable, Droppable, type OnDragEndResponder } from '@hello-pangea/dnd';
import { type Dispatch, type SetStateAction, useMemo } from 'react';
import ContentElementDisplay from './content-element-display.tsx';
import { notifications } from '@mantine/notifications';
import { useDeleteContentElementMutation } from '../../api/topic.ts';

interface ContentElementsDndProps {
  topic: Partial<Topic>;
  setTopic: Dispatch<SetStateAction<Partial<Topic>>>;
}

const ContentElementsDnd = ({ topic, setTopic }: ContentElementsDndProps) => {
  const { t } = useTranslation();

  const [opened, { close, open }] = useDisclosure(false);
  const { mutateAsync: deleteContentElement, isPending: isDeletingContentElement } =
    useDeleteContentElementMutation();

  const addContentElement = (contentElement: AnyContentElementDto) => {
    setTopic((prev) => ({
      ...prev,
      contentElements: [...(topic.contentElements ?? []), contentElement],
    }));
  };

  const sortedContentElements = useMemo<AnyContentElementDto[]>(() => {
    return (topic.contentElements ?? []).sort((a, b) => (a.rank ?? 0) - (b.rank ?? 0));
  }, [topic.contentElements]);

  const removeContentElement = async (contentElementId: string) => {
    try {
      await deleteContentElement(contentElementId);

      const updatedItems = sortedContentElements
        .filter((item) => item.id !== contentElementId)
        .map((item, index) => ({
          ...item,
          rank: index,
        }));

      setTopic({
        ...topic,
        contentElements: updatedItems,
      });
    } catch {
      notifications.show({
        message: t('common.serverError'),
        color: 'red',
      });
    }
  };

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

  const contentElementCount = topic.contentElements?.length ?? 0;
  const isLimitReached = contentElementCount >= 12;

  return (
    <>
      <Flex justify="flex-end" w="100%" mt={12} mb={12}>
        <Tooltip
          label={t('topic.contentElements.limitReached')}
          disabled={!isLimitReached}
          withArrow
        >
          <Button variant="filled" onClick={open} disabled={isLimitReached}>
            <IconPlusFilled />
            &nbsp;{t('topic.actions.createContentElement')}
          </Button>
        </Tooltip>
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
                        alignItems: 'stretch',
                        width: '100%',
                      }}
                    >
                      <div {...provided.dragHandleProps} style={{ marginRight: '10px' }}>
                        <ThemeIcon variant="subtle" color="gray">
                          <IconGripVertical size={18} />
                        </ThemeIcon>
                      </div>
                      <Stack style={{ flex: 1 }}>
                        <Title order={3}>{item.title}</Title>
                        <ContentElementDisplay contentElement={item} />
                      </Stack>
                      <Flex align="flex-end" style={{ marginLeft: '12px', paddingBottom: '4px' }}>
                        <ActionIcon
                          variant="default"
                          aria-label={t('common.delete')}
                          onClick={() => removeContentElement(item.id)}
                          disabled={isDeletingContentElement}
                        >
                          <IconTrash size={18} />
                        </ActionIcon>
                      </Flex>
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
};

export default ContentElementsDnd;
