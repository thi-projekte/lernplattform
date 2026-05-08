import { useMemo } from 'react';
import type { NodeMouseHandler, NodeTypes, OnMoveEnd } from '@xyflow/react';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import type { GraphTopicNodeData } from './topic-graph.types.ts';
import TopicNode from './topic-node.tsx';
import TopicGraphView from './topic-graph.tsx';
import { buildPersonalTopicsGraph } from './topic-graph.utils.ts';

const nodeTypes: NodeTypes = {
  topic: TopicNode,
};

interface PersonalTopicsGraphProps {
  topics: GraphTopicDto[];
  currentUsername?: string;
  onTopicClick?: (topic: GraphTopicNodeData) => void;
  onMoveEnd?: OnMoveEnd;
}

const PersonalTopicsGraph = ({
  topics,
  currentUsername,
  onTopicClick,
  onMoveEnd,
}: PersonalTopicsGraphProps) => {
  const { nodes, edges } = useMemo(
    () => buildPersonalTopicsGraph(topics, currentUsername),
    [currentUsername, topics]
  );

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    onTopicClick?.(node.data as GraphTopicNodeData);
  };

  return (
    <TopicGraphView
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      onNodeClick={onTopicClick ? handleNodeClick : undefined}
      onMoveEnd={onMoveEnd}
      allowCanvasPanning
      allowPanOnScroll
      showControls={false}
      showViewportToolbar
      fitViewMaxZoom={0.99}
    />
  );
};

export default PersonalTopicsGraph;
