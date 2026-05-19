import { Group, Paper, SegmentedControl, Stack, Text, Title, useMantineTheme } from '@mantine/core';
import type { Node, NodeMouseHandler } from '@xyflow/react';
import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  useFetchDirectNeighborQueries,
  useFetchMostPopularTopicsWithNeighbors,
} from '../api/topic-graph.ts';
import TopicGraphView from '../components/graph-view/topic-graph.tsx';
import type { TopicGraphNodePositions } from '../components/graph-view/topic-graph.types.ts';
import GenericTopicNode from '../components/graph-view/generic-topic-node.tsx';
import type {
  SkillTreeNodeData,
  SkillTreeOrientation,
} from '../components/graph-view/skill-tree.types.ts';
import { buildSkillTreeGraph } from '../components/graph-view/topic-graph.utils.ts';
import { Layout } from '../components/layout.tsx';
import LayoutLoader from '../components/layout-loader.tsx';
import { useUserService } from '../provider/user-provider.tsx';
import type { GraphTopicDto } from '../schemas/topic-graph.ts';

const nodeTypes = {
  skillTreeTopic: GenericTopicNode,
};

const cachedDagrePositions: Record<SkillTreeOrientation, TopicGraphNodePositions> = {
  vertical: {},
  horizontal: {},
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
  const theme = useMantineTheme();
  const userProfile = useUserService();
  const { data, isLoading } = useFetchMostPopularTopicsWithNeighbors();

  const [orientation, setOrientation] = useState<SkillTreeOrientation>('vertical');
  const [expandedTopicIds, setExpandedTopicIds] = useState<string[]>([]);
  const [nodePositionsByOrientation, setNodePositionsByOrientation] = useState<
    Record<SkillTreeOrientation, TopicGraphNodePositions>
  >({
    vertical: {},
    horizontal: {},
  });
  const [isViewportLocked, setIsViewportLocked] = useState(false);
  const directNeighborQueries = useFetchDirectNeighborQueries(expandedTopicIds);

  const graphTopics = useMemo(() => {
    return directNeighborQueries.reduce(
      (current, query) => mergeGraphTopics(current, query.data ?? []),
      data ?? []
    );
  }, [data, directNeighborQueries]);

  const { nodes: layoutNodes, edges } = useMemo(
    () => buildSkillTreeGraph(graphTopics, orientation, userProfile.account.username),
    [graphTopics, orientation, userProfile.account.username]
  );

  const currentNodePositions = useMemo(
    () => nodePositionsByOrientation[orientation] ?? {},
    [nodePositionsByOrientation, orientation]
  );

  const nodes = useMemo(() => {
    const cached = cachedDagrePositions[orientation];
    return layoutNodes.map((node) => {
      const userPosition = currentNodePositions[node.id];
      if (userPosition) {
        return { ...node, position: userPosition };
      }
      if (cached[node.id]) {
        return { ...node, position: cached[node.id] };
      }
      cached[node.id] = node.position;
      return node;
    });
  }, [currentNodePositions, layoutNodes, orientation]);

  const onNodeClick: NodeMouseHandler = async (_event, node) => {
    const graphNode = node as Node<SkillTreeNodeData>;
    const topic = graphNode.data.payload;

    setNodePositionsByOrientation((current) => {
      const currentOrientationPositions = current[orientation] ?? {};
      const nextOrientationPositions: TopicGraphNodePositions = {
        ...currentOrientationPositions,
      };
      let changed = false;

      nodes.forEach((currentNode) => {
        const previous = currentOrientationPositions[currentNode.id];
        if (
          !previous ||
          previous.x !== currentNode.position.x ||
          previous.y !== currentNode.position.y
        ) {
          nextOrientationPositions[currentNode.id] = currentNode.position;
          changed = true;
        }
      });

      if (!changed) {
        return current;
      }

      return {
        ...current,
        [orientation]: nextOrientationPositions,
      };
    });
    setExpandedTopicIds((current) =>
      current.includes(topic.id) ? current : [...current, topic.id]
    );
  };

  const handleNodePositionChange = useCallback(
    (nodeId: string, position: { x: number; y: number }) => {
      setNodePositionsByOrientation((current) => {
        const currentOrientationPositions = current[orientation] ?? {};
        const previous = currentOrientationPositions[nodeId];
        if (previous && previous.x === position.x && previous.y === position.y) {
          return current;
        }

        return {
          ...current,
          [orientation]: {
            ...currentOrientationPositions,
            [nodeId]: position,
          },
        };
      });
    },
    [orientation]
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

        <Paper withBorder radius="lg" p="lg"  style={{ overflow: 'hidden' }}>
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
                position: 'relative',
                height: 640,
                borderRadius: 16,
                overflow: 'hidden',
                border: `1px solid ${theme.other.layoutBorder}`,
                background: theme.other.graphBg,
              }}
            >
              <TopicGraphView
                key={orientation}
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onNodeClick={onNodeClick}
                onNodeDragStop={(_event, node) => handleNodePositionChange(node.id, node.position)}
                allowCanvasPanning={!isViewportLocked}
                allowPanOnScroll={!isViewportLocked}
                allowNodeDragging={!isViewportLocked}
                showControls={false}
                showViewportToolbar
                viewportLocked={isViewportLocked}
                onToggleViewportLock={() => setIsViewportLocked((current) => !current)}
                fitView
                fitViewPadding={orientation === 'horizontal' ? 0.22 : 0.3}
                backgroundColor={theme.other.graphDots}
                backgroundGap={20}
              />
            </div>
          </Stack>
        </Paper>
      </Stack>
    </Layout>
  );
};

export default HomePage;
