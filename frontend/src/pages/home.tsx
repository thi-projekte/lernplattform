import {
  Button,
  Group,
  Paper,
  SegmentedControl,
  Stack,
  Text,
  Title,
  useMantineTheme,
} from '@mantine/core';
import type { Node, NodeMouseHandler } from '@xyflow/react';
import { IconEye } from '@tabler/icons-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
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

const POSITIONS_STORAGE_KEY = 'skillTree.nodePositions';

const loadSavedPositions = (): Record<SkillTreeOrientation, TopicGraphNodePositions> => {
  try {
    const saved = localStorage.getItem(POSITIONS_STORAGE_KEY);
    if (saved) return JSON.parse(saved) as Record<SkillTreeOrientation, TopicGraphNodePositions>;
  } catch {
    return { vertical: {}, horizontal: {} };
  }
  return { vertical: {}, horizontal: {} };
};

const cachedDagrePositions: Record<SkillTreeOrientation, TopicGraphNodePositions> = {
  vertical: {},
  horizontal: {},
};
let cachedExpandedTopicIds: string[] = [];
let cachedSelectedTopicId: string | null = null;
let cachedHiddenTopicIds: string[] = [];

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
  const [expandedTopicIds, setExpandedTopicIds] = useState<string[]>(cachedExpandedTopicIds);
  const [selectedTopicId, setSelectedTopicId] = useState<string | null>(cachedSelectedTopicId);

  useEffect(() => {
    cachedExpandedTopicIds = expandedTopicIds;
  }, [expandedTopicIds]);
  useEffect(() => {
    cachedSelectedTopicId = selectedTopicId;
  }, [selectedTopicId]);
  const [hiddenTopicIds, setHiddenTopicIds] = useState<string[]>(cachedHiddenTopicIds);
  useEffect(() => {
    cachedHiddenTopicIds = hiddenTopicIds;
  }, [hiddenTopicIds]);
  const [nodePositionsByOrientation, setNodePositionsByOrientation] =
    useState<Record<SkillTreeOrientation, TopicGraphNodePositions>>(loadSavedPositions);
  useEffect(() => {
    localStorage.setItem(POSITIONS_STORAGE_KEY, JSON.stringify(nodePositionsByOrientation));
  }, [nodePositionsByOrientation]);
  const [isViewportLocked, setIsViewportLocked] = useState(false);
  const [lastExpandedNode, setLastExpandedNode] = useState<{
    id: string;
    position: { x: number; y: number };
  } | null>(null);
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

  const visibleEdges = useMemo(
    () =>
      edges.filter((e) => !hiddenTopicIds.includes(e.source) && !hiddenTopicIds.includes(e.target)),
    [edges, hiddenTopicIds]
  );

  const nodes = useMemo(() => {
    const cached = cachedDagrePositions[orientation];
    const filteredNodes =
      hiddenTopicIds.length > 0
        ? layoutNodes.filter((n) => !hiddenTopicIds.includes(n.data.payload.id))
        : layoutNodes;

    // Identify nodes that have no position yet (arriving after an expansion click).
    // We only treat nodes as "new" when there are already pinned nodes on the canvas
    // (i.e. not during the initial load), so the initial dagre layout is used as-is.
    const hasPinnedNodes =
      Object.keys(currentNodePositions).length > 0 || Object.keys(cached).length > 0;

    const newNodes = hasPinnedNodes
      ? filteredNodes.filter((n) => !cached[n.id] && !currentNodePositions[n.id])
      : [];

    // Place new nodes near the expanded node, avoiding overlap with existing nodes.
    // Each node gets its desired spread position; if occupied, it shifts down (or
    // right in horizontal mode) one step at a time until a free slot is found.
    const NODE_SLOT_W = 300;
    const NODE_SLOT_H = 160;
    const newNodePositions = new Map<string, { x: number; y: number }>();
    if (newNodes.length > 0 && lastExpandedNode) {
      const parentPos =
        currentNodePositions[lastExpandedNode.id] ??
        cached[lastExpandedNode.id] ??
        lastExpandedNode.position;
      const isVertical = orientation !== 'horizontal';

      const occupied: { x: number; y: number }[] = filteredNodes
        .filter((n) => cached[n.id] || currentNodePositions[n.id])
        .map((n) => (currentNodePositions[n.id] ?? cached[n.id])!);

      newNodes.forEach((node, index) => {
        const spread = index - (newNodes.length - 1) / 2;
        const baseX = isVertical ? parentPos.x + spread * NODE_SLOT_W : parentPos.x + NODE_SLOT_W;
        const baseY = isVertical ? parentPos.y + NODE_SLOT_H : parentPos.y + spread * NODE_SLOT_H;

        let placed = false;
        for (let attempt = 0; attempt < 20 && !placed; attempt++) {
          const candidate = {
            x: isVertical ? baseX : baseX + attempt * NODE_SLOT_W,
            y: isVertical ? baseY + attempt * NODE_SLOT_H : baseY,
          };
          const free = !occupied.some(
            (o) =>
              Math.abs(o.x - candidate.x) < NODE_SLOT_W && Math.abs(o.y - candidate.y) < NODE_SLOT_H
          );
          if (free) {
            occupied.push(candidate);
            newNodePositions.set(node.id, candidate);
            placed = true;
          }
        }
        if (!placed) {
          newNodePositions.set(node.id, { x: baseX, y: baseY });
        }
      });
    }

    return filteredNodes.map((node) => {
      const userPosition = currentNodePositions[node.id];
      const selected = node.data.payload.id === selectedTopicId;
      const topicId = node.data.payload.id;
      const isExpanded = expandedTopicIds.includes(topicId);
      const onExpand = () => {
        const pos = userPosition ?? cached[node.id] ?? node.position;
        setLastExpandedNode({ id: node.id, position: pos });
        setExpandedTopicIds((current) =>
          current.includes(topicId) ? current : [...current, topicId]
        );
        setSelectedTopicId(null);
      };

      const onHide = () => {
        setHiddenTopicIds((current) => [...current, topicId]);
        setSelectedTopicId(null);
      };
      const enrichData = (base: typeof node.data) => ({ ...base, onExpand, isExpanded, onHide });

      if (userPosition)
        return { ...node, position: userPosition, selected, data: enrichData(node.data) };
      if (cached[node.id])
        return { ...node, position: cached[node.id], selected, data: enrichData(node.data) };

      const newPos = newNodePositions.get(node.id) ?? node.position;
      cached[node.id] = newPos;
      return { ...node, position: newPos, selected, data: enrichData(node.data) };
    });
  }, [
    currentNodePositions,
    expandedTopicIds,
    hiddenTopicIds,
    lastExpandedNode,
    layoutNodes,
    orientation,
    selectedTopicId,
  ]);

  const onNodeClick: NodeMouseHandler = async (_event, node) => {
    const graphNode = node as Node<SkillTreeNodeData>;
    const topic = graphNode.data.payload;
    setSelectedTopicId(topic.id);
    setExpandedTopicIds((current) =>
      current.includes(topic.id) ? current : [...current, topic.id]
    );

    setLastExpandedNode({ id: graphNode.id, position: node.position });

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

        <Paper withBorder radius="lg" p="lg" style={{ overflow: 'hidden' }}>
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
                edges={visibleEdges}
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
              {hiddenTopicIds.length > 0 && (
                <div
                  style={{
                    position: 'absolute',
                    bottom: 12,
                    left: '50%',
                    transform: 'translateX(-50%)',
                    zIndex: 10,
                  }}
                >
                  <Button
                    size="xs"
                    variant="light"
                    color="gray"
                    leftSection={<IconEye size={14} />}
                    onClick={() => setHiddenTopicIds([])}
                  >
                    {t('journey.showHidden', { count: hiddenTopicIds.length })}
                  </Button>
                </div>
              )}
            </div>
          </Stack>
        </Paper>
      </Stack>
    </Layout>
  );
};

export default HomePage;
