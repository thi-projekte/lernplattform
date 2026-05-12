import type { Topic } from '../../schemas/topic.ts';
import {
  ActionIcon,
  Badge,
  Box,
  Button,
  Group,
  Paper,
  SegmentedControl,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { OnMoveEnd, Viewport } from '@xyflow/react';
import { IconTrash } from '@tabler/icons-react';
import TopicSearchbar from './topic-searchbar.tsx';
import EntityTable from '../entity-table.tsx';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';
import TopicAssociationsGraph from '../graph-view/topic-associations-graph.tsx';
import type {
  GraphTopicNodeData,
  TopicGraphNodePositions,
} from '../graph-view/topic-graph.types.ts';
import type { ListTopicDto } from '../../schemas/topic.ts';
import { useUserService } from '../../provider/user-provider.tsx';

interface AssociatedTopicsStepProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const AssociatedTopicsStep = ({ topic, setTopic }: AssociatedTopicsStepProps) => {
  const { t } = useTranslation();
  const userService = useUserService();
  const [editorMode, setEditorMode] = useState<'list' | 'graph'>('graph');
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);
  const [lastViewport, setLastViewport] = useState<Viewport | null>(null);
  const [searchSuggestions, setSearchSuggestions] = useState<ListTopicDto[]>([]);
  const [nodePositions, setNodePositions] = useState<TopicGraphNodePositions>({});
  const [isViewportLocked, setIsViewportLocked] = useState(false);

  const removeTopic = (topicId: string) => {
    setTopic({
      ...topic,
      relatedTopics: (topic.relatedTopics ?? []).filter((ass) => ass.id !== topicId),
    });

    setSelectedTopicNode((current) => (current && current.payload.id === topicId ? null : current));
  };

  const createAssociation = (topicId: string) => {
    const topicToAdd = searchSuggestions.find((topic) => topic.id === topicId);

    if (!topicToAdd) {
      return;
    }

    setTopic({
      ...topic,
      relatedTopics: [...(topic.relatedTopics ?? []), topicToAdd],
    });

    setSelectedTopicNode({
      kind: 'topic',
      title: topicToAdd.title,
      creatorFullName: topicToAdd.creatorFullName,
      payload: topicToAdd,
    });
  };

  const hideIsolatedTopic = (topicId: string) => {
    setSearchSuggestions((current) => current.filter((topic) => topic.id !== topicId));
    setNodePositions((current) => {
      const next = { ...current };
      delete next[`isolated-topic-${topicId}`];
      return next;
    });
    setSelectedTopicNode((current) => (current && current.payload.id === topicId ? null : current));
  };

  const columns = useTopicColumns({ deleteActionHandler: removeTopic });
  const existingIds = useMemo(
    () => (topic.relatedTopics ?? []).map((t) => t.id),
    [topic.relatedTopics]
  );
  const selectedTopicId =
    selectedTopicNode && typeof selectedTopicNode.payload.id === 'string'
      ? selectedTopicNode.payload.id
      : null;
  const selectedCategories =
    selectedTopicNode && 'categories' in selectedTopicNode.payload
      ? selectedTopicNode.payload.categories
      : undefined;
  const isolatedTopicCount = searchSuggestions.length;

  const handleMoveEnd: OnMoveEnd = (_event, viewport) => {
    setLastViewport(viewport);
  };

  const handleSuggestionsChange = useCallback((topics: ListTopicDto[], searchTerm: string) => {
    const nextSuggestions = searchTerm.trim() ? topics.slice(0, 4) : [];

    setSearchSuggestions((current) => {
      if (
        current.length === nextSuggestions.length &&
        current.every((topic, index) => topic.id === nextSuggestions[index]?.id)
      ) {
        return current;
      }

      return nextSuggestions;
    });
  }, []);

  const handleNodePositionChange = useCallback(
    (nodeId: string, position: { x: number; y: number }) => {
      setNodePositions((current) => {
        const previous = current[nodeId];
        if (previous && previous.x === position.x && previous.y === position.y) {
          return current;
        }

        return {
          ...current,
          [nodeId]: position,
        };
      });
    },
    []
  );

  return (
    <Stack>
      <Group justify="center">
        <SegmentedControl
          value={editorMode}
          onChange={(value) => setEditorMode(value as 'list' | 'graph')}
          data={[
            { label: t('topic.graph.modeList'), value: 'list' },
            { label: t('topic.graph.modeGraph'), value: 'graph' },
          ]}
        />
      </Group>

      {editorMode === 'list' ? (
        <Paper withBorder radius="md" p="sm" mih={760}>
          <Stack gap="sm" h="100%">
            <div>
              <Title order={4}>{t('topic.graph.relatedTopicsTableTitle')}</Title>
              <Text size="sm" c="dimmed">
                {t('topic.graph.relatedTopicsTableDescription')}
              </Text>
            </div>
            <TopicSearchbar
              onAdd={(newTopic) =>
                setTopic({ ...topic, relatedTopics: [...(topic.relatedTopics ?? []), newTopic] })
              }
              existingIds={existingIds}
              onSuggestionsChange={handleSuggestionsChange}
            />
            <div style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
              <EntityTable data={topic.relatedTopics ?? []} columns={columns} hidePagination />
            </div>
          </Stack>
        </Paper>
      ) : (
        <div
          style={{
            display: 'grid',
            gap: '1rem',
            gridTemplateColumns: '220px minmax(0, 1fr) 200px',
            alignItems: 'start',
          }}
        >
          <div>
            <Paper withBorder radius="md" p="sm" h={760}>
              <Stack gap="md" h="100%">
                <div>
                  <Group justify="space-between" align="center" wrap="nowrap">
                    <Title order={4}>{t('topic.graph.graphModeRailTitle')}</Title>
                    <Badge variant="light" color="gray">
                      {(topic.relatedTopics ?? []).length}
                    </Badge>
                  </Group>
                  <Text size="xs" c="dimmed">
                    {t('topic.graph.graphModeRailDescription')}
                  </Text>
                </div>
                <TopicSearchbar
                  existingIds={existingIds}
                  onSuggestionsChange={handleSuggestionsChange}
                />
                <Text size="xs" c="dimmed">
                  {t('topic.graph.graphModeSearchHint')}
                </Text>
                {isolatedTopicCount > 0 && (
                  <Paper withBorder radius="md" p="xs">
                    <Stack gap={8}>
                      <Group justify="space-between" align="center" wrap="nowrap">
                        <Text fw={600} size="sm" c="blue.7">
                          {t('topic.graph.foundTopicsTitle')}
                        </Text>
                        <Badge color="blue" variant="light">
                          {isolatedTopicCount}
                        </Badge>
                      </Group>
                      <Text size="xs" c="dimmed">
                        {t('topic.graph.foundTopicsDescription')}
                      </Text>
                      <Stack gap={6}>
                        {searchSuggestions.map((suggestion) => (
                          <Paper key={suggestion.id} withBorder radius="md" p="xs">
                            <Text fw={600} size="xs" truncate>
                              {suggestion.title}
                            </Text>
                            {suggestion.creatorFullName && (
                              <Text size="xs" c="dimmed" truncate>
                                {suggestion.creatorFullName}
                              </Text>
                            )}
                          </Paper>
                        ))}
                      </Stack>
                    </Stack>
                  </Paper>
                )}
                <Stack gap="xs" style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
                  {(topic.relatedTopics ?? []).map((relatedTopic) => (
                    <Paper
                      key={relatedTopic.id}
                      withBorder
                      radius="md"
                      p="sm"
                      style={{
                        cursor: 'pointer',
                        borderColor: selectedTopicId === relatedTopic.id ? '#228be6' : undefined,
                        background:
                          selectedTopicId === relatedTopic.id
                            ? 'rgba(34, 139, 230, 0.06)'
                            : undefined,
                      }}
                      onClick={() =>
                        setSelectedTopicNode({
                          kind: 'topic',
                          title: relatedTopic.title,
                          creatorFullName: relatedTopic.creatorFullName,
                          payload: relatedTopic,
                        })
                      }
                    >
                      <Group justify="space-between" align="flex-start" wrap="nowrap">
                        <div style={{ minWidth: 0 }}>
                          <Text fw={600} size="sm" truncate>
                            {relatedTopic.title}
                          </Text>
                          {relatedTopic.creatorFullName && (
                            <Text size="xs" c="dimmed" truncate>
                              {relatedTopic.creatorFullName}
                            </Text>
                          )}
                          {relatedTopic.categories.length > 0 && (
                            <Group gap={6} mt={8}>
                              {relatedTopic.categories.slice(0, 1).map((category) => (
                                <Badge key={category.id} color={category.color} variant="light">
                                  {category.title}
                                </Badge>
                              ))}
                            </Group>
                          )}
                        </div>
                        <ActionIcon
                          variant="light"
                          color="red"
                          onClick={(event) => {
                            event.stopPropagation();
                            removeTopic(relatedTopic.id);
                          }}
                        >
                          <IconTrash size={16} />
                        </ActionIcon>
                      </Group>
                    </Paper>
                  ))}
                </Stack>
              </Stack>
            </Paper>
          </div>
          <div>
            <Paper withBorder radius="md" p="md" h={760}>
              <Stack gap="md" h="100%">
                <Group justify="space-between" align="flex-start" wrap="nowrap">
                  <div style={{ minWidth: 0 }}>
                    <Title order={2}>{t('topic.graph.workspaceTitle')}</Title>
                    <Text size="xs" c="dimmed" maw={420}>
                      {t('topic.graph.workspaceDescription')}
                    </Text>
                  </div>
                  <Group gap={6} wrap="wrap" justify="flex-end" maw={250}>
                    <Badge color="red">{t('topic.graph.legendRoot')}</Badge>
                    <Badge color="orange">{t('topic.graph.legendAssociated')}</Badge>
                    <Badge color="blue">{t('topic.graph.legendIsolated')}</Badge>
                  </Group>
                </Group>
                <Box
                  style={{
                    flex: 1,
                    minHeight: 0,
                    overflow: 'hidden',
                    border: '1px dashed #ced4da',
                    borderRadius: 12,
                    background: '#f1f3f5e0',
                  }}
                >
                  <TopicAssociationsGraph
                    topic={{
                      id: topic.id,
                      title: topic.title,
                      categories: topic.categories,
                      relatedTopics: topic.relatedTopics,
                      isolatedTopics: searchSuggestions,
                    }}
                    currentUsername={userService.account.username}
                    onTopicClick={setSelectedTopicNode}
                    onAssociationClick={removeTopic}
                    onMoveEnd={handleMoveEnd}
                    onNodePositionChange={handleNodePositionChange}
                    canEditAssociations
                    canDeleteAssociations
                    allowNodeDragging={!isViewportLocked}
                    allowCanvasPanning={!isViewportLocked}
                    allowPanOnScroll={!isViewportLocked}
                    showControls={false}
                    showViewportToolbar
                    viewportLocked={isViewportLocked}
                    onToggleViewportLock={() => setIsViewportLocked((current) => !current)}
                    fitViewMaxZoom={0.99}
                    nodePositions={nodePositions}
                  />
                </Box>
              </Stack>
            </Paper>
          </div>
          <div>
            <Paper withBorder radius="md" p="sm" h={760}>
              <Stack gap="md" h="100%">
                <div>
                  <Title order={4}>{t('topic.graph.inspectorTitle')}</Title>
                </div>
                {selectedTopicNode ? (
                  <Stack gap="xs">
                    <Title order={4}>{selectedTopicNode.title}</Title>
                    {selectedTopicNode.creatorFullName && (
                      <Text size="sm" c="dimmed">
                        {selectedTopicNode.creatorFullName}
                      </Text>
                    )}
                    {selectedCategories && selectedCategories.length > 0 && (
                      <Group gap="xs">
                        {selectedCategories.map((category) => (
                          <Badge key={category.title} color={`#${category.color}`}>
                            {category.title}
                          </Badge>
                        ))}
                      </Group>
                    )}
                    <Text size="xs" c="dimmed">
                      {selectedTopicNode.isRoot
                        ? t('topic.graph.currentTopic')
                        : selectedTopicNode.isIsolated
                          ? t('topic.graph.isolatedTopic')
                          : t('topic.graph.associatedTopic')}
                    </Text>
                    {!selectedTopicNode.isRoot &&
                      !selectedTopicNode.isIsolated &&
                      (() => {
                        const selectedTopicId = selectedTopicNode.payload.id;
                        if (typeof selectedTopicId !== 'string') {
                          return null;
                        }

                        return (
                          <Button
                            variant="light"
                            color="red"
                            size="xs"
                            onClick={() => removeTopic(selectedTopicId)}
                          >
                            {t('topic.graph.removeAssociation')}
                          </Button>
                        );
                      })()}
                    {selectedTopicNode.isIsolated &&
                      (() => {
                        const selectedTopicId = selectedTopicNode.payload.id;
                        if (typeof selectedTopicId !== 'string') {
                          return null;
                        }

                        return (
                          <Stack gap="xs">
                            <Button
                              variant="light"
                              color="blue"
                              size="xs"
                              onClick={() => createAssociation(selectedTopicId)}
                            >
                              {t('topic.graph.addAssociation')}
                            </Button>
                            <Button
                              variant="subtle"
                              color="gray"
                              size="xs"
                              onClick={() => hideIsolatedTopic(selectedTopicId)}
                              fullWidth
                              styles={{
                                label: {
                                  whiteSpace: 'normal',
                                  textAlign: 'center',
                                },
                              }}
                            >
                              {t('topic.graph.hideIsolatedTopic')}
                            </Button>
                          </Stack>
                        );
                      })()}
                  </Stack>
                ) : (
                  <Stack gap="xs">
                    <Text size="xs" c="dimmed">
                      {t('topic.graph.clickTopicNodeToSeeDetails')}
                    </Text>
                    <Text size="xs" c="dimmed">
                      {t('topic.graph.clickAssociationToRemove')}
                    </Text>
                    <Text size="xs" c="dimmed">
                      {t('topic.graph.clickIsolatedTopicToAssociate')}
                    </Text>
                  </Stack>
                )}
                <div style={{ marginTop: 'auto' }}>
                  {lastViewport && (
                    <Text size="sm" c="dimmed">
                      {t('topic.graph.viewportStatus', {
                        x: Math.round(lastViewport.x),
                        y: Math.round(lastViewport.y),
                        zoom: lastViewport.zoom.toFixed(2),
                      })}
                    </Text>
                  )}
                </div>
              </Stack>
            </Paper>
          </div>
        </div>
      )}
    </Stack>
  );
};

export default AssociatedTopicsStep;
