import { Layout } from '../../components/layout.tsx';
import {
  useDeleteTopicMutation,
  useEditTopicMutation,
  useQueryPersonalTopicsPaginated,
  useQueryTopic,
} from '../../api/topic.ts';
import { useCallback, useMemo, useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import { useQueryClient } from '@tanstack/react-query';
import EntityTable from '../../components/entity-table.tsx';
import {
  ActionIcon,
  Badge,
  Button,
  Flex,
  Group,
  Paper,
  SegmentedControl,
  Stack,
  Text,
  Title,
  Tooltip,
} from '@mantine/core';
import { useTranslation } from 'react-i18next';
import type { OnConnect } from '@xyflow/react';
import { IconLink, IconPlusFilled, IconTrash } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';
import LayoutLoader from '../../components/layout-loader.tsx';
import { useFetchMostPopularTopicsWithNeighbors } from '../../api/topic-graph.ts';
import PersonalTopicsGraph from '../../components/graph-view/personal-topics-graph.tsx';
import type { GraphTopicNodeData } from '../../components/graph-view/topic-graph.types.ts';
import { useUserService } from '../../provider/user-provider.tsx';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import { useCreateAssociation } from '../../api/association.ts';
import TopicSearchbar from '../../components/topic/topic-searchbar.tsx';
import type { ListTopicDto, Topic } from '../../schemas/topic.ts';

const BuilderModeListPage = () => {
  const [pagination, setPagination] = useState<PaginationState>({ pageSize: 20, pageIndex: 0 });
  const [viewMode, setViewMode] = useState<'list' | 'graph'>('list');
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);
  const [searchSuggestions, setSearchSuggestions] = useState<ListTopicDto[]>([]);
  const [isViewportLocked, setIsViewportLocked] = useState(false);
  const queryClient = useQueryClient();
  const { data, isLoading } = useQueryPersonalTopicsPaginated(pagination);
  const userService = useUserService();
  const {
    data: graphTopics,
    isLoading: isGraphLoading,
    refetch: refetchGraphTopics,
  } = useFetchMostPopularTopicsWithNeighbors(undefined, true, viewMode === 'graph');

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { mutate } = useDeleteTopicMutation();
  const { mutateAsync: createAssociation, isPending: isCreatingAssociation } =
    useCreateAssociation();
  const columns = useTopicColumns({
    editAction: true,
    deleteActionHandler: mutate,
    viewAction: true,
  });
  const currentUsername = userService.account.username?.toLowerCase();
  const selectedGraphTopic = useMemo<GraphTopicDto | null>(() => {
    const payload = selectedTopicNode?.payload;
    if (
      !payload ||
      !('creatorId' in payload) ||
      !('associatedTopics' in payload) ||
      !Array.isArray(payload.categories)
    ) {
      return null;
    }

    return payload as GraphTopicDto;
  }, [selectedTopicNode]);
  const selectedGraphTopicIsOwned =
    !!selectedGraphTopic &&
    !!currentUsername &&
    selectedGraphTopic.creatorId.toLowerCase() === currentUsername;
  const personalGraphTopics = useMemo<GraphTopicDto[]>(() => graphTopics ?? [], [graphTopics]);
  const { mutateAsync: editTopic } = useEditTopicMutation(selectedGraphTopic?.id ?? '');
  const { data: selectedOwnedTopicData } = useQueryTopic(
    selectedGraphTopic?.id ?? '',
    true,
    selectedGraphTopicIsOwned
  );
  const selectedOwnedTopicDetails = selectedOwnedTopicData as Topic | undefined;
  const blockedSearchTopicIds = useMemo(
    () => (selectedGraphTopic ? [selectedGraphTopic.id] : []),
    [selectedGraphTopic]
  );
  const lockedAssociationEdgeId = useMemo(() => {
    if (
      !selectedGraphTopic ||
      !selectedGraphTopicIsOwned ||
      !selectedOwnedTopicDetails ||
      selectedOwnedTopicDetails.relatedTopics.length !== 1
    ) {
      return null;
    }
    const lastRelatedId = selectedOwnedTopicDetails.relatedTopics[0]?.id;
    if (!lastRelatedId) return null;
    const edgeKey = [selectedGraphTopic.id, lastRelatedId].sort().join(':');
    return `personal-topic-edge-${edgeKey}`;
  }, [selectedGraphTopic, selectedGraphTopicIsOwned, selectedOwnedTopicDetails]);
  const areTopicsAlreadyAssociated = useCallback(
    (owningTopicId: string, foreignTopicId: string) => {
      const owningTopic = personalGraphTopics.find((topic) => topic.id === owningTopicId);

      return (
        owningTopic?.associatedTopics.includes(foreignTopicId) ||
        personalGraphTopics.some(
          (topic) => topic.id === foreignTopicId && topic.associatedTopics.includes(owningTopicId)
        ) ||
        false
      );
    },
    [personalGraphTopics]
  );

  const handleSuggestionsChange = useCallback((topics: ListTopicDto[], searchTerm: string) => {
    const nextSuggestions = searchTerm.trim() ? topics.slice(0, 6) : [];
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

  const handleAssociationCreate = useCallback(
    async (owningTopicId: string, foreignTopicId: string) => {
      if (owningTopicId === foreignTopicId) {
        return;
      }

      if (areTopicsAlreadyAssociated(owningTopicId, foreignTopicId)) {
        return;
      }

      await createAssociation({ owningTopicId, foreignTopicId });
      setSearchSuggestions((current) => current.filter((topic) => topic.id !== foreignTopicId));
      await queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
      await refetchGraphTopics();
    },
    [areTopicsAlreadyAssociated, createAssociation, queryClient, refetchGraphTopics]
  );

  const handleConnect: OnConnect = useCallback(
    async (connection) => {
      if (!connection.source || !connection.target) {
        return;
      }

      const sourceTopic = personalGraphTopics.find((topic) => topic.id === connection.source);
      const targetTopic = personalGraphTopics.find((topic) => topic.id === connection.target);

      if (!sourceTopic || !targetTopic || !currentUsername) {
        return;
      }

      const sourceIsOwned = sourceTopic.creatorId.toLowerCase() === currentUsername;
      const targetIsOwned = targetTopic.creatorId.toLowerCase() === currentUsername;

      if (sourceIsOwned === targetIsOwned) {
        return;
      }

      const owningTopicId = sourceIsOwned ? sourceTopic.id : targetTopic.id;
      const foreignTopicId = sourceIsOwned ? targetTopic.id : sourceTopic.id;

      await handleAssociationCreate(owningTopicId, foreignTopicId);
    },
    [currentUsername, handleAssociationCreate, personalGraphTopics]
  );

  const handleSuggestionAdd = useCallback(
    async (newTopic: ListTopicDto) => {
      setSearchSuggestions((current) => {
        if (current.some((topic) => topic.id === newTopic.id)) {
          return current;
        }

        return [newTopic, ...current];
      });

      if (selectedGraphTopic && selectedGraphTopicIsOwned) {
        if (areTopicsAlreadyAssociated(selectedGraphTopic.id, newTopic.id)) {
          return;
        }

        await handleAssociationCreate(selectedGraphTopic.id, newTopic.id);
      }
    },
    [
      areTopicsAlreadyAssociated,
      handleAssociationCreate,
      selectedGraphTopic,
      selectedGraphTopicIsOwned,
    ]
  );

  const handleAssociationDelete = useCallback(
    async (relatedTopicId: string) => {
      if (!selectedGraphTopic || !selectedGraphTopicIsOwned || !selectedOwnedTopicDetails) {
        return;
      }

      const nextRelatedTopics = selectedOwnedTopicDetails.relatedTopics.filter(
        (relatedTopic: ListTopicDto) => relatedTopic.id !== relatedTopicId
      );

      if (nextRelatedTopics.length === 0) {
        return;
      }

      await editTopic({
        ...selectedOwnedTopicDetails,
        relatedTopics: nextRelatedTopics,
      });
      await queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
      await refetchGraphTopics();
    },
    [
      editTopic,
      queryClient,
      refetchGraphTopics,
      selectedGraphTopic,
      selectedGraphTopicIsOwned,
      selectedOwnedTopicDetails,
    ]
  );

  const handleTopicDelete = useCallback(() => {
    if (!selectedGraphTopic || !selectedGraphTopicIsOwned) return;
    mutate(selectedGraphTopic.id, {
      onSuccess: () => {
        setSelectedTopicNode(null);
        void refetchGraphTopics();
      },
    });
  }, [mutate, refetchGraphTopics, selectedGraphTopic, selectedGraphTopicIsOwned]);

  if (isLoading || (viewMode === 'graph' && isGraphLoading)) {
    return <LayoutLoader />;
  }

  return (
    <Layout>
      <Title>{t('topic.headings.personalTopics')}</Title>
      <Flex justify="flex-end" w="100%" mt={12}>
        <Flex justify="center" w={190}>
          <Button variant="filled" onClick={() => navigate('/builder-mode/topics/create')}>
            <IconPlusFilled />
            &nbsp;{t('topic.actions.create')}
          </Button>
        </Flex>
      </Flex>
      <Stack gap="md" mt={12}>
        <Stack gap="xs" align="center">
          <Text c="dimmed" size="sm" ta="center">
            {t('topic.personalGraph.graphDescription')}
          </Text>
          <Group justify="center" w="100%">
            <SegmentedControl
              value={viewMode}
              onChange={(value) => setViewMode(value as 'list' | 'graph')}
              data={[
                { label: t('topic.graph.modeList'), value: 'list' },
                { label: t('topic.graph.modeGraph'), value: 'graph' },
              ]}
            />
          </Group>
        </Stack>

        {viewMode === 'list' ? (
          data && (
            <EntityTable
              data={data.results}
              columns={columns}
              pageCount={data.totalPages}
              pagination={pagination}
              isFetching={isLoading}
              setPagination={setPagination}
            />
          )
        ) : (
          <div
            style={{
              display: 'grid',
              gap: '1rem',
              gridTemplateColumns: '240px minmax(0, 1fr)',
              alignItems: 'start',
            }}
          >
            <Paper withBorder radius="md" p="sm" h={760}>
              <Stack gap="md" h="100%" style={{ minHeight: 0 }}>
                <div>
                  <Title order={4}>{t('topic.graph.graphModeRailTitle')}</Title>
                  <Text size="xs" c="dimmed">
                    {t('topic.graph.graphModeRailDescription')}
                  </Text>
                </div>

                {selectedGraphTopic && (
                  <Paper withBorder radius="md" p="sm">
                    <Stack gap="xs">
                      <Text fw={600}>{selectedGraphTopic.title}</Text>
                      {selectedGraphTopic.categories.length > 0 && (
                        <Group gap={6}>
                          {selectedGraphTopic.categories.map((category) => (
                            <Badge
                              key={category.id}
                              color={category.color}
                              variant="light"
                              size="sm"
                            >
                              {category.title}
                            </Badge>
                          ))}
                        </Group>
                      )}
                      <Badge
                        w="fit-content"
                        color={selectedGraphTopicIsOwned ? 'orange' : 'blue'}
                        variant="light"
                        size="sm"
                      >
                        {selectedGraphTopicIsOwned
                          ? t('topic.personalGraph.ownedTopic')
                          : t('topic.personalGraph.foreignTopic')}
                      </Badge>
                      {selectedGraphTopic.creatorFullName && (
                        <Text size="xs" c="dimmed">
                          {selectedGraphTopic.creatorFullName}
                        </Text>
                      )}
                      {selectedGraphTopicIsOwned && (
                        <Button
                          variant="light"
                          color="red"
                          size="xs"
                          mt="xs"
                          fullWidth
                          leftSection={<IconTrash size={14} />}
                          onClick={handleTopicDelete}
                          styles={{
                            label: { whiteSpace: 'normal', lineHeight: 1.2 },
                            root: { height: 'auto', minHeight: 28, paddingBlock: 6 },
                          }}
                        >
                          {t('topic.personalGraph.removeOwnTopic')}
                        </Button>
                      )}
                    </Stack>
                  </Paper>
                )}

                <TopicSearchbar
                  existingIds={blockedSearchTopicIds}
                  onAdd={handleSuggestionAdd}
                  onSuggestionsChange={handleSuggestionsChange}
                />

                {!selectedGraphTopic && (
                  <Text size="xs" c="dimmed">
                    {t('topic.personalGraph.searchHintSelectOwned')}
                  </Text>
                )}

                <Stack gap="xs" style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
                  {searchSuggestions.map((suggestion) => {
                    const canLink = !!(selectedGraphTopic && selectedGraphTopicIsOwned);
                    const alreadyLinked = !!(
                      canLink &&
                      selectedGraphTopic &&
                      areTopicsAlreadyAssociated(selectedGraphTopic.id, suggestion.id)
                    );
                    return (
                      <Paper key={suggestion.id} withBorder radius="md" p="sm">
                        <Group justify="space-between" align="flex-start" wrap="nowrap">
                          <div style={{ minWidth: 0 }}>
                            <Text fw={600} size="sm" truncate>
                              {suggestion.title}
                            </Text>
                            {suggestion.creatorFullName && (
                              <Text size="xs" c="dimmed" truncate>
                                {suggestion.creatorFullName}
                              </Text>
                            )}
                            {suggestion.categories.length > 0 && (
                              <Group gap={6} mt={8}>
                                {suggestion.categories.slice(0, 1).map((category) => (
                                  <Badge key={category.id} color={category.color} variant="light">
                                    {category.title}
                                  </Badge>
                                ))}
                              </Group>
                            )}
                          </div>
                          <Group gap="xs" wrap="nowrap">
                            {canLink && (
                              <Tooltip
                                label={
                                  alreadyLinked
                                    ? t('topic.personalGraph.alreadyLinked')
                                    : t('topic.personalGraph.linkToSelectedTopic')
                                }
                                withArrow
                              >
                                <ActionIcon
                                  variant="light"
                                  aria-label={t('topic.personalGraph.linkToSelectedTopic')}
                                  disabled={isCreatingAssociation || alreadyLinked}
                                  onClick={() =>
                                    void handleAssociationCreate(
                                      selectedGraphTopic.id,
                                      suggestion.id
                                    )
                                  }
                                >
                                  <IconLink size={16} />
                                </ActionIcon>
                              </Tooltip>
                            )}
                            <ActionIcon
                              variant="light"
                              color="red"
                              onClick={() =>
                                setSearchSuggestions((current) =>
                                  current.filter((topic) => topic.id !== suggestion.id)
                                )
                              }
                            >
                              <IconTrash size={16} />
                            </ActionIcon>
                          </Group>
                        </Group>
                      </Paper>
                    );
                  })}
                </Stack>
              </Stack>
            </Paper>

            <Paper withBorder radius="md" p="md" h={760} style={{ background: '#f1f3f5e0' }}>
              <PersonalTopicsGraph
                topics={personalGraphTopics}
                currentUsername={userService.account.username}
                selectedTopicId={selectedGraphTopic?.id}
                lockedAssociationEdgeId={lockedAssociationEdgeId}
                lockedAssociationTooltip={t('topic.personalGraph.keepAtLeastOneAssociation')}
                onTopicClick={setSelectedTopicNode}
                onConnect={handleConnect}
                onAssociationClick={handleAssociationDelete}
                canEditAssociations={!isCreatingAssociation}
                canDeleteAssociations={selectedGraphTopicIsOwned}
                allowNodeDragging={!isViewportLocked}
                allowCanvasPanning={!isViewportLocked}
                allowPanOnScroll={!isViewportLocked}
                viewportLocked={isViewportLocked}
                onToggleViewportLock={() => setIsViewportLocked((current) => !current)}
              />
            </Paper>
          </div>
        )}
      </Stack>
    </Layout>
  );
};

export default BuilderModeListPage;
