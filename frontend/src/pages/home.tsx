import { Badge, Group, Paper, SegmentedControl, Stack, Text, Title } from '@mantine/core';
import { Layout } from '../components/layout.tsx';
import { useTranslation } from 'react-i18next';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useUserService } from '../provider/user-provider.tsx';
import {
  fetchDirectNeighbors,
  useFetchMostPopularTopicsWithNeighbors,
} from '../api/topic-graph.ts';
import LayoutLoader from '../components/layout-loader.tsx';
import TopicGraphView from '../components/graph-view/topic-graph.tsx';
import SkillTreeNodeComponent from '../components/graph-view/skill-tree-node.tsx';
import type {
  SkillTreeNodeData,
  SkillTreeOrientation,
} from '../components/graph-view/skill-tree.types.ts';
import { buildSkillTreeGraph } from '../components/graph-view/topic-graph.utils.ts';
import type { GraphTopicDto } from '../schemas/topic-graph.ts';
import type { Node, NodeMouseHandler } from '@xyflow/react';
import type { TopicGraphNodePositions } from '../components/graph-view/topic-graph.types.ts';

const nodeTypes = {
  skillTreeTopic: SkillTreeNodeComponent,
};

const mergeGraphTopics = (current: GraphTopicDto[], incoming: GraphTopicDto[]) => {
  const merged = new Map<string, GraphTopicDto>();

  current.forEach((topic) => merged.set(topic.id, topic));
  incoming.forEach((topic) => {
    const existing = merged.get(topic.id);

    if (!existing) {
      merged.set(topic.id, topic);
      return;
    }

    merged.set(topic.id, {
      ...existing,
      ...topic,
      associatedTopics: [...new Set([...existing.associatedTopics, ...topic.associatedTopics])],
    });
  });

  return [...merged.values()];
};

const HomePage = () => {
  const { t } = useTranslation();
  const userProfile = useUserService();
  const { data, isLoading } = useFetchMostPopularTopicsWithNeighbors();

  const [orientation, setOrientation] = useState<SkillTreeOrientation>('vertical');
  const [selectedTopicId, setSelectedTopicId] = useState<string | null>(null);
  const [loadedTopics, setLoadedTopics] = useState<GraphTopicDto[]>([]);
  const [isExpandingNode, setIsExpandingNode] = useState(false);
  const [nodePositions, setNodePositions] = useState<TopicGraphNodePositions>({});
  const [isViewportLocked, setIsViewportLocked] = useState(false);

  const graphTopics = useMemo(
    () => (loadedTopics.length > 0 ? loadedTopics : (data ?? [])),
    [data, loadedTopics]
  );

  const selectedTopic = useMemo(
    () => graphTopics.find((topic) => topic.id === selectedTopicId) ?? graphTopics[0] ?? null,
    [graphTopics, selectedTopicId]
  );

  const { nodes: layoutNodes, edges } = useMemo(
    () => buildSkillTreeGraph(graphTopics, orientation),
    [graphTopics, orientation]
  );

  useEffect(() => {
    setNodePositions({});
  }, [orientation]);

  useEffect(() => {
    setNodePositions((current) => {
      const nextEntries = layoutNodes.map((node) => [
        node.id,
        current[node.id] ?? node.position,
      ] as const);

      const next = Object.fromEntries(nextEntries);

      const sameLength = Object.keys(current).length === nextEntries.length;
      const sameValues = sameLength
        && nextEntries.every(([id, position]) => {
          const previous = current[id];
          return previous && previous.x === position.x && previous.y === position.y;
        });

      return sameValues ? current : next;
    });
  }, [layoutNodes]);

  const nodes = useMemo(
    () =>
      layoutNodes.map((node) => ({
        ...node,
        position: nodePositions[node.id] ?? node.position,
      })),
    [layoutNodes, nodePositions]
  );

  const onNodeClick: NodeMouseHandler = async (_event, node) => {
    const graphNode = node as Node<SkillTreeNodeData>;
    const topic = graphNode.data.payload;

    setSelectedTopicId(topic.id);
    setIsExpandingNode(true);

    try {
      const neighbors = await fetchDirectNeighbors(topic.id);
      setLoadedTopics((current) =>
        mergeGraphTopics(mergeGraphTopics(current, graphTopics), neighbors)
      );
    } finally {
      setIsExpandingNode(false);
    }
  };

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

  if (isLoading) {
    return <LayoutLoader />;
  }

  return (
    <Layout>
      <Stack gap="lg">
        <Stack gap={4}>
          <Title order={1}>{t('journey.title')}</Title>
          <Text c="dimmed">
            {t('journey.subtitle', {
              name: userProfile.account.username ?? t('journey.genericUser'),
            })}
          </Text>
        </Stack>

        <Paper withBorder radius="lg" p="lg">
          <Stack gap="md">
            <Group justify="space-between" align="center">
              <Text size="sm" c="dimmed">
                {t('journey.tipText')}
              </Text>
              <SegmentedControl
                value={orientation}
                onChange={(value) => setOrientation(value as SkillTreeOrientation)}
                data={[
                  { label: t('journey.orientation.vertical'), value: 'vertical' },
                  { label: t('journey.orientation.horizontal'), value: 'horizontal' },
                ]}
              />
            </Group>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'minmax(0, 1fr) 300px',
                gap: 20,
                minHeight: 640,
              }}
            >
              <div
                style={{
                  position: 'relative',
                  minHeight: 640,
                  borderRadius: 16,
                  overflow: 'hidden',
                  background: '#fcfcfd',
                  border: '1px solid #e9ecef',
                }}
              >
                <TopicGraphView
                  key={orientation}
                  nodes={nodes}
                  edges={edges}
                  nodeTypes={nodeTypes}
                  onNodeClick={onNodeClick}
                  onNodeDragStop={(_event, node) =>
                    handleNodePositionChange(node.id, node.position)
                  }
                  allowCanvasPanning={!isViewportLocked}
                  allowPanOnScroll={!isViewportLocked}
                  allowNodeDragging={!isViewportLocked}
                  showControls={false}
                  showViewportToolbar
                  viewportLocked={isViewportLocked}
                  onToggleViewportLock={() => setIsViewportLocked((current) => !current)}
                  fitView
                  fitViewPadding={orientation === 'horizontal' ? 0.22 : 0.3}
                  backgroundColor="#e5e7eb"
                  backgroundGap={20}
                />
              </div>

              <Paper withBorder radius="lg" p="md" style={{ alignSelf: 'start' }}>
                <Stack gap="md">
                  <div>
                    <Title order={3}>{t('journey.selectedTopic')}</Title>
                    <Text size="sm" c="dimmed">
                      {t('common.clickOnANodeToSeeContent')}
                    </Text>
                  </div>

                  {selectedTopic && (
                    <Paper withBorder radius="md" p="md">
                      <Stack gap="xs">
                        <Title order={4}>{selectedTopic.title}</Title>
                        {selectedTopic.categories.length > 0 && (
                          <Group gap="xs">
                            {selectedTopic.categories.map((category) => (
                              <Badge key={category.id} color={`#${category.color}`} radius="xl">
                                {category.title}
                              </Badge>
                            ))}
                          </Group>
                        )}
                        <Text size="sm" c="dimmed">
                          {selectedTopic.creatorFullName || t('journey.unknownCreator')}
                        </Text>
                        {isExpandingNode && (
                          <Text size="sm" c="blue">
                            {t('journey.loadingNeighbors')}
                          </Text>
                        )}
                      </Stack>
                    </Paper>
                  )}
                </Stack>
              </Paper>
            </div>
          </Stack>
        </Paper>
      </Stack>
    </Layout>
  );
};

export default HomePage;
