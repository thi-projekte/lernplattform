import { useMemo } from 'react';
import type { Node, NodeMouseHandler, NodeTypes, OnConnect, OnMoveEnd } from '@xyflow/react';
import TopicNode from './topic-node.tsx';
import TopicGraphView from './topic-graph.tsx';
import { buildTopicAssociationsGraph } from './topic-graph.utils.ts';
import type { GraphTopicNodeData, TopicAssociationsGraphInput } from './topic-graph.types.ts';

const nodeTypes: NodeTypes = {
  topic: TopicNode,
};

interface TopicAssociationsGraphProps {
  topic?: TopicAssociationsGraphInput;
  onTopicClick?: (topic: GraphTopicNodeData) => void;
  onMoveEnd?: OnMoveEnd;
  onConnect?: OnConnect;
  canEditAssociations?: boolean;
  allowNodeDragging?: boolean;
  showControls?: boolean;
  fitView?: boolean;
  fitViewPadding?: number;
  backgroundColor?: string;
  backgroundGap?: number;
}

const TopicAssociationsGraph = ({
  topic,
  onTopicClick,
  onMoveEnd,
  onConnect,
  canEditAssociations = false,
  allowNodeDragging = false,
  showControls = true,
  fitView = true,
  fitViewPadding = 0.2,
  backgroundColor = '#dee2e6',
  backgroundGap = 16,
}: TopicAssociationsGraphProps) => {
  const { nodes, edges } = useMemo(() => buildTopicAssociationsGraph(topic), [topic]);

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<GraphTopicNodeData>;
    onTopicClick?.(graphNode.data);
  };

  return (
    <TopicGraphView
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      onNodeClick={onTopicClick ? handleNodeClick : undefined}
      onMoveEnd={onMoveEnd}
      onConnect={onConnect}
      canEditAssociations={canEditAssociations}
      allowNodeDragging={allowNodeDragging}
      showControls={showControls}
      fitView={fitView}
      fitViewPadding={fitViewPadding}
      backgroundColor={backgroundColor}
      backgroundGap={backgroundGap}
    />
  );
};

export default TopicAssociationsGraph;
