import type { Edge } from '@xyflow/react';
import type { ListTopicDto, Topic } from '../../schemas/topic';
import type {
  TopicAssociationsGraphInput,
  TopicGraphNode,
  TopicGraphNodePositions,
} from './topic-graph.types';

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
