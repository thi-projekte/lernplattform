import { Layout } from '../../components/layout.tsx';
import { useDeleteTopicMutation, useQueryPersonalTopicsPaginated } from '../../api/topic.ts';
import { useMemo, useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import EntityTable from '../../components/entity-table.tsx';
import {
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
import { IconPlusFilled } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';
import LayoutLoader from '../../components/layout-loader.tsx';
import { useFetchMostPopularTopicsWithNeighbors } from '../../api/topic-graph.ts';
import PersonalTopicsGraph from '../../components/graph-view/personal-topics-graph.tsx';
import type { GraphTopicNodeData } from '../../components/graph-view/topic-graph.types.ts';
import { useUserService } from '../../provider/user-provider.tsx';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';

const BuilderModeListPage = () => {
  const [pagination, setPagination] = useState<PaginationState>({ pageSize: 20, pageIndex: 0 });
  const [viewMode, setViewMode] = useState<'list' | 'graph'>('list');
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);
  const { data, isLoading } = useQueryPersonalTopicsPaginated(pagination);
  const userService = useUserService();
  const { data: graphTopics, isLoading: isGraphLoading } = useFetchMostPopularTopicsWithNeighbors(
    undefined,
    true,
    viewMode === 'graph'
  );

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { mutate } = useDeleteTopicMutation();
  const columns = useTopicColumns({
    editAction: true,
    deleteActionHandler: mutate,
    viewAction: true,
  });
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
              gridTemplateColumns: 'minmax(0, 1fr) 320px',
              alignItems: 'start',
            }}
          >
            <Paper withBorder radius="md" p="md" h={760}>
              {graphTopics && (
                <PersonalTopicsGraph
                  topics={graphTopics}
                  currentUsername={userService.account.username}
                  onTopicClick={setSelectedTopicNode}
                />
              )}
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
                        color={
                          userService.account.username?.toLowerCase() ===
                          selectedGraphTopic.creatorId.toLowerCase()
                            ? 'orange'
                            : 'blue'
                        }
                        variant="light"
                      >
                        {userService.account.username?.toLowerCase() ===
                        selectedGraphTopic.creatorId.toLowerCase()
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
