import { Layout } from '../../components/layout.tsx';
import { useDeleteTopicMutation, useQueryPersonalTopicsPaginated } from '../../api/topic.ts';
import { useCallback, useMemo, useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import EntityTable from '../../components/entity-table.tsx';
import {
  ActionIcon,
  Badge,
  Button,
  Card,
  Flex,
  Group,
  Paper,
  SegmentedControl,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { useTranslation } from 'react-i18next';
import type { OnConnect } from '@xyflow/react';
import { IconPlusFilled, IconTrash } from '@tabler/icons-react';
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
import type { ListTopicDto } from '../../schemas/topic.ts';

const BuilderModeListPage = () => {
  const [pagination, setPagination] = useState<PaginationState>({ pageSize: 20, pageIndex: 0 });
  const [viewMode, setViewMode] = useState<'list' | 'graph'>('list');
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);
  const [searchSuggestions, setSearchSuggestions] = useState<ListTopicDto[]>([]);
  const [isViewportLocked, setIsViewportLocked] = useState(false);
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
  const personalGraphTopics = useMemo<GraphTopicDto[]>(
    () =>
      graphTopics
        ? [
            ...graphTopics,
            ...searchSuggestions
              .filter((suggestion) => !graphTopics.some((topic) => topic.id === suggestion.id))
              .map((suggestion) => ({
                id: suggestion.id,
                title: suggestion.title,
                categories: suggestion.categories,
                creatorId: suggestion.creatorId,
                creatorFullName: suggestion.creatorFullName,
                associatedTopics: [],
              })),
          ]
        : [],
    [graphTopics, searchSuggestions]
  );
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
  const existingTopicIds = useMemo(
    () => personalGraphTopics.map((topic) => topic.id),
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
      await createAssociation({ owningTopicId, foreignTopicId });
      await refetchGraphTopics();
    },
    [createAssociation, refetchGraphTopics]
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
        await handleAssociationCreate(selectedGraphTopic.id, newTopic.id);
      }
    },
    [handleAssociationCreate, selectedGraphTopic, selectedGraphTopicIsOwned]
  );

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
        <Group justify="space-between" align="center">
          <Text c="dimmed" size="sm">
            {t('topic.personalGraph.graphDescription')}
          </Text>
          <SegmentedControl
            value={viewMode}
            onChange={(value) => setViewMode(value as 'list' | 'graph')}
            data={[
              { label: t('topic.graph.modeList'), value: 'list' },
              { label: t('topic.graph.modeGraph'), value: 'graph' },
            ]}
          />
        </Group>

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
              gridTemplateColumns: '240px minmax(0, 1fr) 320px',
              alignItems: 'start',
            }}
          >
            <Paper withBorder radius="md" p="sm" h={760}>
              <Stack gap="md" h="100%">
                <div>
                  <Group justify="space-between" align="center" wrap="nowrap">
                    <Title order={4}>{t('topic.graph.graphModeRailTitle')}</Title>
                    <Badge variant="light" color="gray">
                      {personalGraphTopics.length}
                    </Badge>
                  </Group>
                  <Text size="xs" c="dimmed">
                    {t('topic.graph.graphModeRailDescription')}
                  </Text>
                </div>
                <TopicSearchbar
                  existingIds={existingTopicIds}
                  onAdd={handleSuggestionAdd}
                  onSuggestionsChange={handleSuggestionsChange}
                />
                <Text size="xs" c="dimmed">
                  {selectedGraphTopic && selectedGraphTopicIsOwned
                    ? t('topic.graph.graphModeSearchHint')
                    : t('topic.personalGraph.searchHintSelectOwned')}
                </Text>
                <Stack gap="xs" style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
                  {searchSuggestions.map((suggestion) => (
                    <Paper
                      key={suggestion.id}
                      withBorder
                      radius="md"
                      p="sm"
                      style={{ background: 'rgba(255, 255, 255, 0.85)' }}
                    >
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
                    </Paper>
                  ))}
                </Stack>
              </Stack>
            </Paper>

            <Paper withBorder radius="md" p="md" h={760}>
              <PersonalTopicsGraph
                topics={personalGraphTopics}
                currentUsername={userService.account.username}
                onTopicClick={setSelectedTopicNode}
                onConnect={handleConnect}
                canEditAssociations={!isCreatingAssociation}
                canDeleteAssociations={false}
                allowNodeDragging={!isViewportLocked}
                allowCanvasPanning={!isViewportLocked}
                allowPanOnScroll={!isViewportLocked}
                viewportLocked={isViewportLocked}
                onToggleViewportLock={() => setIsViewportLocked((current) => !current)}
              />
            </Paper>

            <Card withBorder radius="md" p="md">
              <Stack gap="md">
                <div>
                  <Title order={2}>{t('topic.personalGraph.selectedTopic')}</Title>
                  <Text c="dimmed" mt={4}>
                    {t('topic.personalGraph.emptySelection')}
                  </Text>
                </div>

                {selectedGraphTopic ? (
                  <Card withBorder radius="md" p="md">
                    <Stack gap="md">
                      <div>
                        <Title order={3}>{selectedGraphTopic.title}</Title>
                      </div>

                      <Group gap={8}>
                        {selectedGraphTopic.categories.map((category) => (
                          <Badge key={category.id} color={category.color} variant="light">
                            {category.title}
                          </Badge>
                        ))}
                      </Group>

                      <Badge
                        w="fit-content"
                        color={selectedGraphTopicIsOwned ? 'orange' : 'blue'}
                        variant="light"
                      >
                        {selectedGraphTopicIsOwned
                          ? t('topic.personalGraph.ownedTopic')
                          : t('topic.personalGraph.foreignTopic')}
                      </Badge>

                      <Text c="dimmed">{selectedGraphTopic.creatorFullName}</Text>
                    </Stack>
                  </Card>
                ) : null}
              </Stack>
            </Card>
          </div>
        )}
      </Stack>
    </Layout>
  );
};

export default BuilderModeListPage;
