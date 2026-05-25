import type { Edge } from '@xyflow/react';
import dagre from 'dagre';
import type { ListTopicDto, Topic } from '../../schemas/topic';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import type {
  TopicAssociationsGraphInput,
  TopicGraphNode,
  TopicGraphNodePositions,
} from './topic-graph.types';
import type { SkillTreeNode, SkillTreeOrientation } from './skill-tree.types.ts';

// Approximate dimensions of the rendered topic card. Used to reserve space
// during layout so cards don't visually overlap.
const SKILL_NODE_WIDTH = 280;
const SKILL_NODE_HEIGHT = 120;
const TOPIC_NODE_HORIZONTAL_GAP = 48;
const TOPIC_NODE_VERTICAL_GAP = 56;

// Wraps a set of nodes + edges with dagre to compute non-overlapping positions.
// React Flow expects top-left corner coordinates; dagre returns node centers,
// so we offset by half the node size.
interface DagreConfig {
  nodeWidth?: number;
  nodeHeight?: number;
  nodesep?: number;
  ranksep?: number;
}

const applyDagreLayout = <T extends { id: string; position: { x: number; y: number } }>(
  nodes: T[],
  edges: Edge[],
  orientation: SkillTreeOrientation,
  config: DagreConfig = {}
): T[] => {
  if (nodes.length === 0) return nodes;

  const {
    nodeWidth = SKILL_NODE_WIDTH,
    nodeHeight = SKILL_NODE_HEIGHT,
    nodesep = 40,
    ranksep = 80,
  } = config;

  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  dagreGraph.setGraph({
    rankdir: orientation === 'horizontal' ? 'LR' : 'TB',
    nodesep,
    ranksep,
  });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  edges.forEach((edge) => {
    if (dagreGraph.hasNode(edge.source) && dagreGraph.hasNode(edge.target)) {
      dagreGraph.setEdge(edge.source, edge.target);
    }
  });

  dagre.layout(dagreGraph);

  return nodes.map((node) => {
    const layouted = dagreGraph.node(node.id);
    if (!layouted) return node;
    return {
      ...node,
      position: {
        x: layouted.x - nodeWidth / 2,
        y: layouted.y - nodeHeight / 2,
      },
    };
  });
};

const overlapsExistingTopicNode = (
  candidate: { x: number; y: number },
  occupiedPositions: { x: number; y: number }[]
) =>
  occupiedPositions.some(
    (position) =>
      Math.abs(position.x - candidate.x) < SKILL_NODE_WIDTH + TOPIC_NODE_HORIZONTAL_GAP &&
      Math.abs(position.y - candidate.y) < SKILL_NODE_HEIGHT + TOPIC_NODE_VERTICAL_GAP
  );

const getNextIsolatedTopicPosition = (
  occupiedPositions: { x: number; y: number }[],
  startX: number,
  startY: number
) => {
  let row = 0;
  let column = 0;

  while (row < 32) {
    const candidate = {
      x: startX + column * (SKILL_NODE_WIDTH + TOPIC_NODE_HORIZONTAL_GAP),
      y: startY + row * (SKILL_NODE_HEIGHT + TOPIC_NODE_VERTICAL_GAP),
    };

    if (!overlapsExistingTopicNode(candidate, occupiedPositions)) {
      return candidate;
    }

    column += 1;
    if (column === 2) {
      column = 0;
      row += 1;
    }
  }

  return { x: startX, y: startY };
};

export const buildTopicDetailsGraph = (
  topic?: Omit<Topic, 'relatedTopics'>
): { nodes: TopicGraphNode[]; edges: Edge[] } => {
  const nodes: TopicGraphNode[] = [];
  const edges: Edge[] = [];

  if (!topic) {
    return { nodes, edges };
  }

  nodes.push({
    id: 'topic-root',
    type: 'topic',
    position: { x: 400, y: 300 },
    data: {
      kind: 'topic',
      title: topic.title,
      isRoot: true,
      payload: topic,
    },
  });

  const radius = 300;

  const sortedContents = [...(topic.contentElements || [])].sort((a, b) => {
    const rankA = a.rank ?? Infinity;
    const rankB = b.rank ?? Infinity;
    return rankA - rankB;
  });

  sortedContents.forEach((content, index) => {
    const id = `content-${index}`;
    const angleStep = Math.PI / 6;
    const angle = index * angleStep - Math.PI / 2;
    const x = 400 + radius * Math.cos(angle);
    const y = 300 + radius * Math.sin(angle);

    nodes.push({
      id,
      type: 'content',
      position: { x, y },
      data: {
        kind: 'content',
        title: content.title,
        payload: content,
      },
    });
  });

  return { nodes, edges };
};

export const buildTopicAssociationsGraph = (
  topic?: TopicAssociationsGraphInput,
  nodePositions: TopicGraphNodePositions = {},
  currentUsername?: string
): { nodes: TopicGraphNode[]; edges: Edge[] } => {
  const nodes: TopicGraphNode[] = [];
  const edges: Edge[] = [];

  if (!topic) {
    return { nodes, edges };
  }

  const currentUsernameNormalized = currentUsername?.trim().toLowerCase();
  const isOwnedByCurrent = (creatorId?: string) =>
    !!currentUsernameNormalized && creatorId?.toLowerCase() === currentUsernameNormalized;

  const rootId = topic.id ? `topic-${topic.id}` : 'topic-root';
  const rootTitle = topic.title?.trim() || 'Untitled topic';

  nodes.push({
    id: rootId,
    type: 'topic',
    position: { x: 0, y: 0 },
    data: {
      kind: 'topic',
      title: rootTitle,
      creatorFullName: topic.creatorFullName,
      isRoot: true,
      isOwned: true,
      payload: topic as unknown as Topic | ListTopicDto,
    },
  });

  const relatedTopics = topic.relatedTopics ?? [];
  const isolatedTopics = (topic.isolatedTopics ?? []).filter(
    (isolatedTopic) => !relatedTopics.some((relatedTopic) => relatedTopic.id === isolatedTopic.id)
  );

  relatedTopics.forEach((relatedTopic) => {
    const nodeId = `related-topic-${relatedTopic.id}`;

    nodes.push({
      id: nodeId,
      type: 'topic',
      position: { x: 0, y: 0 },
      data: {
        kind: 'topic',
        title: relatedTopic.title,
        creatorFullName: relatedTopic.creatorFullName,
        isOwned: isOwnedByCurrent(relatedTopic.creatorId),
        payload: relatedTopic,
      },
    });

    edges.push({
      id: `edge-topic-${relatedTopic.id}`,
      source: rootId,
      target: nodeId,
      sourceHandle: 'bottom',
      targetHandle: 'top',
      style: { stroke: '#adb5bd', strokeWidth: 0.8 },
    });
  });

  const layoutedNodes = applyDagreLayout(nodes, edges, 'vertical');

  const finalNodes = layoutedNodes.map((node) => ({
    ...node,
    position: nodePositions[node.id] ?? node.position,
  }));

  const occupiedPositions = finalNodes.map((node) => node.position);
  const maxConnectedX = finalNodes.length
    ? Math.max(...finalNodes.map((node) => node.position.x))
    : 0;
  const minConnectedY = finalNodes.length
    ? Math.min(...finalNodes.map((node) => node.position.y))
    : 0;
  const isolatedStartX = maxConnectedX + SKILL_NODE_WIDTH + 120;
  const isolatedStartY = minConnectedY;

  isolatedTopics.forEach((isolatedTopic) => {
    const nodeId = `isolated-topic-${isolatedTopic.id}`;
    const position =
      nodePositions[nodeId] ??
      getNextIsolatedTopicPosition(occupiedPositions, isolatedStartX, isolatedStartY);

    occupiedPositions.push(position);

    finalNodes.push({
      id: nodeId,
      type: 'topic',
      position,
      data: {
        kind: 'topic',
        title: isolatedTopic.title,
        creatorFullName: isolatedTopic.creatorFullName,
        isIsolated: true,
        isOwned: isOwnedByCurrent(isolatedTopic.creatorId),
        payload: isolatedTopic,
      },
    });
  });

  return { nodes: finalNodes, edges };
};

export const buildPersonalTopicsGraph = (topics: GraphTopicDto[], currentUsername?: string) => {
  const nodes: TopicGraphNode[] = [];
  const edges: Edge[] = [];

  if (topics.length === 0) {
    return { nodes, edges };
  }

  const currentUsernameNormalized = currentUsername?.trim().toLowerCase();
  const adjacency = new Map<string, Set<string>>();
  const topicsById = new Map(topics.map((topic) => [topic.id, topic]));

  const compareTopics = (a: GraphTopicDto, b: GraphTopicDto) => {
    const aIsOwned =
      currentUsernameNormalized !== undefined &&
      a.creatorId.toLowerCase() === currentUsernameNormalized;
    const bIsOwned =
      currentUsernameNormalized !== undefined &&
      b.creatorId.toLowerCase() === currentUsernameNormalized;

    if (aIsOwned !== bIsOwned) {
      return aIsOwned ? -1 : 1;
    }

    const degreeDiff = (adjacency.get(b.id)?.size ?? 0) - (adjacency.get(a.id)?.size ?? 0);
    if (degreeDiff !== 0) {
      return degreeDiff;
    }

    const titleDiff = a.title.localeCompare(b.title);
    if (titleDiff !== 0) {
      return titleDiff;
    }

    return a.id.localeCompare(b.id);
  };

  topics.forEach((topic) => {
    adjacency.set(topic.id, new Set());
  });

  topics.forEach((topic) => {
    topic.associatedTopics.forEach((associatedTopicId) => {
      if (!topicsById.has(associatedTopicId)) return;
      adjacency.get(topic.id)?.add(associatedTopicId);
      adjacency.get(associatedTopicId)?.add(topic.id);
    });
  });

  const sortedTopics = [...topics].sort(compareTopics);
  const visited = new Set<string>();
  const components: GraphTopicDto[][] = [];

  sortedTopics.forEach((topic) => {
    if (visited.has(topic.id)) return;

    const component: GraphTopicDto[] = [];
    const queue = [topic.id];
    visited.add(topic.id);

    while (queue.length > 0) {
      const currentId = queue.shift()!;
      const currentTopic = topicsById.get(currentId);
      if (!currentTopic) continue;

      component.push(currentTopic);

      [...(adjacency.get(currentId) ?? new Set())]
        .sort((leftId, rightId) => compareTopics(topicsById.get(leftId)!, topicsById.get(rightId)!))
        .forEach((neighborId) => {
          if (visited.has(neighborId)) return;
          visited.add(neighborId);
          queue.push(neighborId);
        });
    }

    components.push(component);
  });

  components.sort((left, right) => compareTopics(left[0], right[0]));
  let componentOffsetX = 240;

  components.forEach((component) => {
    const componentById = new Map(component.map((topic) => [topic.id, topic]));
    const rootTopic = [...component].sort(compareTopics)[0];

    const levels = new Map<string, number>([[rootTopic.id, 0]]);
    const bfsVisited = new Set<string>([rootTopic.id]);
    const queue = [rootTopic.id];

    while (queue.length > 0) {
      const currentId = queue.shift()!;
      const currentLevel = levels.get(currentId) ?? 0;

      [...(adjacency.get(currentId) ?? new Set())]
        .sort((leftId, rightId) => compareTopics(topicsById.get(leftId)!, topicsById.get(rightId)!))
        .forEach((neighborId) => {
          if (!componentById.has(neighborId) || bfsVisited.has(neighborId)) return;
          bfsVisited.add(neighborId);
          levels.set(neighborId, currentLevel + 1);
          queue.push(neighborId);
        });
    }

    component.filter((topic) => !levels.has(topic.id)).forEach((topic) => levels.set(topic.id, 0));

    const topicsByLevel = new Map<number, GraphTopicDto[]>();
    component.forEach((topic) => {
      const level = levels.get(topic.id) ?? 0;
      const existing = topicsByLevel.get(level) ?? [];
      existing.push(topic);
      topicsByLevel.set(level, existing);
    });

    const sortedLevels = [...topicsByLevel.keys()].sort((a, b) => a - b);
    const widestLevel = Math.max(
      ...sortedLevels.map((level) => topicsByLevel.get(level)?.length ?? 0)
    );
    const componentWidth = Math.max(widestLevel, 1) * 250;
    const centerX = componentOffsetX + componentWidth / 2;

    sortedLevels.forEach((level) => {
      const levelTopics = [...(topicsByLevel.get(level) ?? [])].sort(compareTopics);

      levelTopics.forEach((topic, index) => {
        const x = centerX + (index - (levelTopics.length - 1) / 2) * 250;
        const y = 140 + level * 220;

        nodes.push({
          id: topic.id,
          type: 'topic',
          position: { x, y },
          data: {
            kind: 'topic',
            title: topic.title,
            creatorFullName: topic.creatorFullName,
            isOwned:
              currentUsernameNormalized !== undefined &&
              topic.creatorId.toLowerCase() === currentUsernameNormalized,
            payload: topic,
          },
        });
      });
    });

    componentOffsetX += componentWidth + 220;
  });

  const seenEdges = new Set<string>();

  topics.forEach((topic) => {
    topic.associatedTopics.forEach((associatedTopicId) => {
      if (!topicsById.has(associatedTopicId)) return;

      const edgeKey = [topic.id, associatedTopicId].sort().join(':');
      if (seenEdges.has(edgeKey)) return;
      seenEdges.add(edgeKey);

      edges.push({
        id: `personal-topic-edge-${edgeKey}`,
        source: topic.id,
        target: associatedTopicId,
        type: 'straight',
        style: {
          stroke: '#7a90a8',
          strokeWidth: 0.7,
        },
      });
    });
  });

  return { nodes, edges };
};

const buildGraphAdjacency = (topics: GraphTopicDto[]) => {
  const topicIds = new Set(topics.map((topic) => topic.id));
  const adjacency = new Map<string, Set<string>>();

  topics.forEach((topic) => {
    if (!adjacency.has(topic.id)) {
      adjacency.set(topic.id, new Set());
    }

    topic.associatedTopics.forEach((associatedTopicId) => {
      if (!topicIds.has(associatedTopicId)) return;

      if (!adjacency.has(associatedTopicId)) {
        adjacency.set(associatedTopicId, new Set());
      }

      adjacency.get(topic.id)?.add(associatedTopicId);
      adjacency.get(associatedTopicId)?.add(topic.id);
    });
  });

  return adjacency;
};

const determineSkillTreeRootId = (topics: GraphTopicDto[], adjacency: Map<string, Set<string>>) => {
  return [...topics]
    .sort((a, b) => {
      const degreeDiff = (adjacency.get(b.id)?.size ?? 0) - (adjacency.get(a.id)?.size ?? 0);
      if (degreeDiff !== 0) return degreeDiff;

      return a.title.localeCompare(b.title);
    })
    .at(0)?.id;
};

const getSkillTreeLevels = (
  rootId: string,
  topics: GraphTopicDto[],
  adjacency: Map<string, Set<string>>
) => {
  const queue: Array<{ id: string; level: number }> = [{ id: rootId, level: 0 }];
  const levels = new Map<string, number>();
  const parents = new Map<string, string | null>([[rootId, null]]);
  const visited = new Set<string>([rootId]);

  while (queue.length > 0) {
    const current = queue.shift();
    if (!current) continue;

    levels.set(current.id, current.level);

    const neighbors = [...(adjacency.get(current.id) ?? [])].sort();
    neighbors.forEach((neighborId) => {
      if (visited.has(neighborId)) return;
      visited.add(neighborId);
      parents.set(neighborId, current.id);
      queue.push({ id: neighborId, level: current.level + 1 });
    });
  }

  const fallbackLevel = (Math.max(...levels.values(), 0) || 0) + 1;
  topics.forEach((topic) => {
    if (!levels.has(topic.id)) {
      levels.set(topic.id, fallbackLevel);
      parents.set(topic.id, null);
    }
  });

  return { levels, parents };
};

const getRootBranchId = (
  topicId: string,
  rootId: string,
  parents: Map<string, string | null>
): string => {
  if (topicId === rootId) return rootId;

  let currentId = topicId;
  let parentId = parents.get(currentId) ?? null;

  if (!parentId) {
    return `disconnected:${topicId}`;
  }

  while (parentId && parentId !== rootId) {
    currentId = parentId;
    parentId = parents.get(currentId) ?? null;
  }

  return parentId === rootId ? currentId : `disconnected:${topicId}`;
};

export const buildSkillTreeGraph = (
  topics: GraphTopicDto[] = [],
  orientation: SkillTreeOrientation = 'vertical',
  currentUsername?: string
) => {
  if (topics.length === 0) {
    return { nodes: [], edges: [] };
  }

  const adjacency = buildGraphAdjacency(topics);
  const rootId = determineSkillTreeRootId(topics, adjacency);

  if (!rootId) {
    return { nodes: [], edges: [] };
  }

  const { levels, parents } = getSkillTreeLevels(rootId, topics, adjacency);
  const rootBranchByTopicId = new Map(
    topics.map((topic) => [topic.id, getRootBranchId(topic.id, rootId, parents)])
  );
  const connectedTopics = topics.filter(
    (topic) => topic.id === rootId || (parents.get(topic.id) ?? null) !== null
  );
  const disconnectedTopics = topics
    .filter((topic) => !connectedTopics.some((connectedTopic) => connectedTopic.id === topic.id))
    .sort((a, b) => a.title.localeCompare(b.title));
  const groupedByLevel = new Map<number, GraphTopicDto[]>();

  [...connectedTopics]
    .sort((a, b) => {
      const levelDiff = (levels.get(a.id) ?? 0) - (levels.get(b.id) ?? 0);
      if (levelDiff !== 0) return levelDiff;
      const branchA = rootBranchByTopicId.get(a.id) ?? a.id;
      const branchB = rootBranchByTopicId.get(b.id) ?? b.id;
      const branchDiff = branchA.localeCompare(branchB);
      if (branchDiff !== 0) return branchDiff;
      return a.title.localeCompare(b.title);
    })
    .forEach((topic) => {
      const level = levels.get(topic.id) ?? 0;
      const current = groupedByLevel.get(level) ?? [];
      current.push(topic);
      groupedByLevel.set(level, current);
    });

  const verticalLayoutConfig = {
    levelDistance: 240,
    branchDistance: 320,
    siblingDistance: 180,
    origin: { x: 520, y: 140 },
    disconnectedColumnOffset: 320,
    disconnectedRowDistance: 180,
  };
  const horizontalLayoutConfig = {
    levelDistance: 320,
    branchDistance: 180,
    intraBranchDistance: 90,
    origin: { x: 180, y: 320 },
    disconnectedLevelOffset: 260,
    disconnectedNodeDistance: 180,
  };

  const horizontalBranchOrder = Array.from(
    new Set(
      [...topics]
        .sort((a, b) => {
          const branchA = rootBranchByTopicId.get(a.id) ?? a.id;
          const branchB = rootBranchByTopicId.get(b.id) ?? b.id;
          if (branchA !== branchB) return branchA.localeCompare(branchB);
          return a.title.localeCompare(b.title);
        })
        .map((topic) => rootBranchByTopicId.get(topic.id) ?? topic.id)
        .filter((branchId) => branchId !== rootId)
    )
  );
  const horizontalBranchIndex = new Map(
    horizontalBranchOrder.map((branchId, index) => [branchId, index])
  );
  const verticalBranchOrder = Array.from(
    new Set(
      [...connectedTopics]
        .sort((a, b) => {
          const branchA = rootBranchByTopicId.get(a.id) ?? a.id;
          const branchB = rootBranchByTopicId.get(b.id) ?? b.id;
          if (branchA !== branchB) return branchA.localeCompare(branchB);
          return a.title.localeCompare(b.title);
        })
        .map((topic) => rootBranchByTopicId.get(topic.id) ?? topic.id)
        .filter((branchId) => branchId !== rootId)
    )
  );
  const verticalBranchIndex = new Map(
    verticalBranchOrder.map((branchId, index) => [branchId, index])
  );

  const nodes: SkillTreeNode[] = [];
  groupedByLevel.forEach((levelTopics, level) => {
    levelTopics.forEach((topic, index) => {
      const stackOffset =
        level *
        (orientation === 'vertical'
          ? verticalLayoutConfig.levelDistance
          : horizontalLayoutConfig.levelDistance);
      let position;

      if (orientation === 'vertical') {
        const branchId = rootBranchByTopicId.get(topic.id) ?? topic.id;

        if (topic.id === rootId) {
          position = {
            x: verticalLayoutConfig.origin.x,
            y: verticalLayoutConfig.origin.y,
          };
        } else {
          const branchIndex = verticalBranchIndex.get(branchId) ?? index;
          const centeredBranchOffset =
            (branchIndex - (verticalBranchOrder.length - 1) / 2) *
            verticalLayoutConfig.branchDistance;
          const levelBranchTopics = levelTopics.filter(
            (candidate) => (rootBranchByTopicId.get(candidate.id) ?? candidate.id) === branchId
          );
          const branchTopicIndex = levelBranchTopics.findIndex(
            (candidate) => candidate.id === topic.id
          );
          const centeredSiblingOffset =
            (branchTopicIndex - (levelBranchTopics.length - 1) / 2) *
            verticalLayoutConfig.siblingDistance;

          position = {
            x: verticalLayoutConfig.origin.x + centeredBranchOffset + centeredSiblingOffset,
            y: verticalLayoutConfig.origin.y + stackOffset,
          };
        }
      } else {
        const branchId = rootBranchByTopicId.get(topic.id) ?? topic.id;

        if (topic.id === rootId) {
          position = {
            x: horizontalLayoutConfig.origin.x,
            y: horizontalLayoutConfig.origin.y,
          };
        } else {
          const branchIndex = horizontalBranchIndex.get(branchId) ?? index;
          const centeredBranchOffset =
            (branchIndex - (horizontalBranchOrder.length - 1) / 2) *
            horizontalLayoutConfig.branchDistance;

          const levelBranchTopics = levelTopics.filter(
            (candidate) => (rootBranchByTopicId.get(candidate.id) ?? candidate.id) === branchId
          );
          const branchTopicIndex = levelBranchTopics.findIndex(
            (candidate) => candidate.id === topic.id
          );
          const centeredSiblingOffset =
            (branchTopicIndex - (levelBranchTopics.length - 1) / 2) *
            horizontalLayoutConfig.intraBranchDistance;

          position = {
            x: horizontalLayoutConfig.origin.x + stackOffset,
            y: horizontalLayoutConfig.origin.y + centeredBranchOffset + centeredSiblingOffset,
          };
        }
      }

      if (!position) {
        position = {
          x: verticalLayoutConfig.origin.x,
          y: verticalLayoutConfig.origin.y,
        };
      }

      nodes.push({
        id: topic.id,
        type: 'skillTreeTopic',
        position,
        data: {
          kind: 'skill-topic',
          title: topic.title,
          categories: topic.categories,
          creatorId: topic.creatorId,
          creatorFullName: topic.creatorFullName,
          isOwned:
            currentUsername !== undefined &&
            currentUsername.trim().length > 0 &&
            topic.creatorId.toLowerCase() === currentUsername.toLowerCase(),
          role:
            topic.id === rootId
              ? 'root'
              : (adjacency.get(topic.id)?.size ?? 0) > 0
                ? 'connected'
                : 'disconnected',
          payload: topic,
        },
      });
    });
  });

  if (disconnectedTopics.length > 0) {
    disconnectedTopics.forEach((topic, index) => {
      const position =
        orientation === 'vertical'
          ? {
              x: verticalLayoutConfig.origin.x + verticalLayoutConfig.disconnectedColumnOffset,
              y:
                verticalLayoutConfig.origin.y +
                index * verticalLayoutConfig.disconnectedRowDistance,
            }
          : {
              x:
                horizontalLayoutConfig.origin.x +
                index * horizontalLayoutConfig.disconnectedNodeDistance,
              y: horizontalLayoutConfig.origin.y + horizontalLayoutConfig.disconnectedLevelOffset,
            };

      nodes.push({
        id: topic.id,
        type: 'skillTreeTopic',
        position,
        data: {
          kind: 'skill-topic',
          title: topic.title,
          categories: topic.categories,
          creatorId: topic.creatorId,
          creatorFullName: topic.creatorFullName,
          isOwned:
            currentUsername !== undefined &&
            currentUsername.trim().length > 0 &&
            topic.creatorId.toLowerCase() === currentUsername.toLowerCase(),
          role: 'disconnected',
          payload: topic,
        },
      });
    });
  }

  const edges: Edge[] = [];
  const seenEdges = new Set<string>();

  topics.forEach((topic) => {
    topic.associatedTopics.forEach((associatedTopicId) => {
      if (!topics.some((candidate) => candidate.id === associatedTopicId)) return;

      const edgeKey = [topic.id, associatedTopicId].sort().join(':');
      if (seenEdges.has(edgeKey)) return;
      seenEdges.add(edgeKey);

      edges.push({
        id: `skill-edge-${edgeKey}`,
        source: topic.id,
        target: associatedTopicId,
        type: 'straight',
        style: {
          stroke: '#94a3b8',
          strokeWidth: 1.4,
          opacity: 0.65,
        },
      });
    });
  });

  const layoutedNodes = applyDagreLayout(nodes, edges, orientation, {
    nodeWidth: 280,
    nodeHeight: 140,
    nodesep: 60,
    ranksep: 100,
  });
  return { nodes: layoutedNodes, edges };
};
