import { Paper, SegmentedControl, Stack, Title } from '@mantine/core';
import { Layout } from '../components/layout.tsx';
import { useTranslation } from 'react-i18next';
import { useCallback, useMemo, useState } from 'react';
import { useUserService } from '../provider/user-provider.tsx';
import {
  useFetchDirectNeighborQueries,
  useFetchMostPopularTopicsWithNeighbors,
} from '../api/topic-graph.ts';
import LayoutLoader from '../components/layout-loader.tsx';
import TopicGraphView from '../components/graph-view/topic-graph.tsx';
import GenericTopicNode from '../components/graph-view/generic-topic-node.tsx';
import type {
  SkillTreeNodeData,
  SkillTreeOrientation,
} from '../components/graph-view/skill-tree.types.ts';
import { buildSkillTreeGraph } from '../components/graph-view/topic-graph.utils.ts';
import type { GraphTopicDto } from '../schemas/topic-graph.ts';
import type { Node, NodeMouseHandler } from '@xyflow/react';
import type { TopicGraphNodePositions } from '../components/graph-view/topic-graph.types.ts';

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
        <Title order={1}>{t('journey.title')}</Title>

        <Paper withBorder radius="lg" p="xs" style={{ overflow: 'hidden' }}>
          <div
            style={{
              position: 'relative',
              height: 640,
              borderRadius: 12,
              overflow: 'hidden',
              background: '#f8fafc',
            }}
          >
            <SegmentedControl
              value={orientation}
              onChange={(value) => setOrientation(value as SkillTreeOrientation)}
              data={[
                { label: t('journey.orientation.vertical'), value: 'vertical' },
                { label: t('journey.orientation.horizontal'), value: 'horizontal' },
              ]}
              style={{
                position: 'absolute',
                top: 16,
                right: 16,
                zIndex: 2,
                boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)',
              }}
            />
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
              backgroundColor="#d1d5db"
              backgroundGap={20}
            />
          </div>
        </Paper>
      </Stack>
    </Layout>
  );
};

export default HomePage;
