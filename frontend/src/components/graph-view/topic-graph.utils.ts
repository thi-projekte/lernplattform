import type { Edge } from '@xyflow/react';
import type { ListTopicDto, Topic } from '../../schemas/topic';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import type {
  TopicAssociationsGraphInput,
  TopicGraphNode,
  TopicGraphNodePositions,
} from './topic-graph.types';
import type { SkillTreeNode, SkillTreeOrientation } from './skill-tree.types.ts';

// The edge handles are derived from node angles so arrows stay attached to the
// most natural side of each node. Be careful when changing this mapping,
// because it affects edge routing and label readability across graph variants.
const getHandleForAngle = (angle: number) => {
  const a = ((angle % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
  if (a >= (7 * Math.PI) / 4 || a < Math.PI / 4) return 'right';
  if (a >= Math.PI / 4 && a < (3 * Math.PI) / 4) return 'bottom';
  if (a >= (3 * Math.PI) / 4 && a < (5 * Math.PI) / 4) return 'left';
  return 'top';
};

const getOppositeHandle = (handle: string) => {
  switch (handle) {
    case 'right':
      return 'left';
    case 'left':
      return 'right';
    case 'top':
      return 'bottom';
    case 'bottom':
      return 'top';
    default:
      return 'left';
  }
};

const getAssociationAngles = (count: number) => {
  if (count <= 0) return [];
  if (count === 1) return [-Math.PI / 2];
  if (count === 2) return [(-5 * Math.PI) / 6, -Math.PI / 6];

  const startAngle = (-5 * Math.PI) / 6;
  const endAngle = -Math.PI / 6;
  const step = (endAngle - startAngle) / (count - 1);

  return Array.from({ length: count }, (_, index) => startAngle + index * step);
};

const getIsolatedAngles = (count: number) => {
  if (count <= 0) return [];
  if (count === 1) return [Math.PI / 2];

  const startAngle = Math.PI / 4;
  const endAngle = (3 * Math.PI) / 4;
  const step = (endAngle - startAngle) / (count - 1);

  return Array.from({ length: count }, (_, index) => startAngle + index * step);
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
  nodePositions: TopicGraphNodePositions = {}
): { nodes: TopicGraphNode[]; edges: Edge[] } => {
  const nodes: TopicGraphNode[] = [];
  const edges: Edge[] = [];

  if (!topic) {
    return { nodes, edges };
  }

  const rootId = topic.id ? `topic-${topic.id}` : 'topic-root';
  const rootTitle = topic.title?.trim() || 'Untitled topic';
  const rootPosition = { x: 400, y: 320 };

  nodes.push({
    id: rootId,
    type: 'topic',
    position: nodePositions[rootId] ?? rootPosition,
    data: {
      kind: 'topic',
      title: rootTitle,
      creatorFullName: topic.creatorFullName,
      isRoot: true,
      payload: topic as unknown as Topic | ListTopicDto,
    },
  });

  const relatedTopics = topic.relatedTopics ?? [];
  const isolatedTopics = (topic.isolatedTopics ?? []).filter(
    (isolatedTopic) => !relatedTopics.some((relatedTopic) => relatedTopic.id === isolatedTopic.id)
  );
  const radius = 250;
  const angles = getAssociationAngles(relatedTopics.length);

  relatedTopics.forEach((relatedTopic, index) => {
    const angle = angles[index] ?? -Math.PI / 2;
    const x = rootPosition.x + radius * Math.cos(angle);
    const y = rootPosition.y + radius * Math.sin(angle);
    const nodeId = `related-topic-${relatedTopic.id}`;

    nodes.push({
      id: nodeId,
      type: 'topic',
      position: nodePositions[nodeId] ?? { x, y },
      data: {
        kind: 'topic',
        title: relatedTopic.title,
        creatorFullName: relatedTopic.creatorFullName,
        payload: relatedTopic,
      },
    });

    const sourceHandle = getHandleForAngle(angle);
    const targetHandle = getOppositeHandle(sourceHandle);

    edges.push({
      id: `edge-topic-${relatedTopic.id}`,
      source: rootId,
      target: nodeId,
      sourceHandle,
      targetHandle,
      animated: true,
      style: { stroke: '#adb5bd', strokeWidth: 2 },
    });
  });

  const isolatedRadius = 180;
  const isolatedAngles = getIsolatedAngles(isolatedTopics.length);

  isolatedTopics.forEach((isolatedTopic, index) => {
    const angle = isolatedAngles[index] ?? Math.PI / 2;
    const x = rootPosition.x + isolatedRadius * Math.cos(angle);
    const y = rootPosition.y + isolatedRadius * Math.sin(angle);

    const nodeId = `isolated-topic-${isolatedTopic.id}`;

    nodes.push({
      id: nodeId,
      type: 'topic',
      position: nodePositions[nodeId] ?? { x, y },
      data: {
        kind: 'topic',
        title: isolatedTopic.title,
        creatorFullName: isolatedTopic.creatorFullName,
        isIsolated: true,
        payload: isolatedTopic,
      },
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
): { nodes: SkillTreeNode[]; edges: Edge[] } => {
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
  const groupedByLevel = new Map<number, GraphTopicDto[]>();

  [...topics]
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
    siblingDistance: 220,
    origin: { x: 520, y: 140 },
  };
  const horizontalLayoutConfig = {
    levelDistance: 320,
    branchDistance: 180,
    intraBranchDistance: 90,
    origin: { x: 180, y: 320 },
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

  const nodes: SkillTreeNode[] = [];
  groupedByLevel.forEach((levelTopics, level) => {
    const totalWidth =
      orientation === 'vertical'
        ? (levelTopics.length - 1) * verticalLayoutConfig.siblingDistance
        : 0;

    levelTopics.forEach((topic, index) => {
      const stackOffset =
        level *
        (orientation === 'vertical'
          ? verticalLayoutConfig.levelDistance
          : horizontalLayoutConfig.levelDistance);
      let position;

      if (orientation === 'vertical') {
        const inlineOffset = index * verticalLayoutConfig.siblingDistance - totalWidth / 2;
        position = {
          x: verticalLayoutConfig.origin.x + inlineOffset,
          y: verticalLayoutConfig.origin.y + stackOffset,
        };
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

  const edges: Edge[] = [];
  const seenEdges = new Set<string>();
  const nodePositions = new Map(nodes.map((node) => [node.id, node.position]));

  topics.forEach((topic) => {
    topic.associatedTopics.forEach((associatedTopicId) => {
      if (!topics.some((candidate) => candidate.id === associatedTopicId)) return;

      const edgeKey = [topic.id, associatedTopicId].sort().join(':');
      if (seenEdges.has(edgeKey)) return;
      seenEdges.add(edgeKey);

      const sourcePosition = nodePositions.get(topic.id);
      const targetPosition = nodePositions.get(associatedTopicId);

      const angle =
        sourcePosition && targetPosition
          ? Math.atan2(targetPosition.y - sourcePosition.y, targetPosition.x - sourcePosition.x)
          : 0;
      const sourceHandle = getHandleForAngle(angle);
      const targetHandle = getOppositeHandle(sourceHandle);

      edges.push({
        id: `skill-edge-${edgeKey}`,
        source: topic.id,
        target: associatedTopicId,
        sourceHandle,
        targetHandle,
        type: 'straight',
        style: {
          stroke: '#94a3b8',
          strokeWidth: 2.25,
          strokeDasharray: '8 6',
        },
      });
    });
  });

  return { nodes, edges };
};
