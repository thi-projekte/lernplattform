import { useMemo } from 'react';
import type {
  Edge,
  EdgeMouseHandler,
  Node,
  NodeMouseHandler,
  NodeTypes,
  OnConnect,
  OnMoveEnd,
} from '@xyflow/react';
import TopicNode from './topic-node.tsx';
import TopicGraphView from './topic-graph.tsx';
import { buildTopicAssociationsGraph } from './topic-graph.utils.ts';
import type {
  GraphTopicNodeData,
  TopicAssociationsGraphInput,
  TopicGraphNodePositions,
} from './topic-graph.types.ts';

const nodeTypes: NodeTypes = {
  topic: TopicNode,
};

interface TopicAssociationsGraphProps {
  topic?: TopicAssociationsGraphInput;
  onTopicClick?: (topic: GraphTopicNodeData) => void;
  onAssociationClick?: (relatedTopicId: string) => void;
  onMoveEnd?: OnMoveEnd;
  onConnect?: OnConnect;
  onNodePositionChange?: (nodeId: string, position: { x: number; y: number }) => void;
  canEditAssociations?: boolean;
  canDeleteAssociations?: boolean;
  allowNodeDragging?: boolean;
  allowCanvasPanning?: boolean;
  allowPanOnScroll?: boolean;
  showControls?: boolean;
  showViewportToolbar?: boolean;
  viewportLocked?: boolean;
  onToggleViewportLock?: () => void;
  fitView?: boolean;
  fitViewPadding?: number;
  backgroundColor?: string;
  backgroundGap?: number;
  nodePositions?: TopicGraphNodePositions;
}

const TopicAssociationsGraph = ({
  topic,
  onTopicClick,
  onAssociationClick,
  onMoveEnd,
  onConnect,
  onNodePositionChange,
  canEditAssociations = false,
  canDeleteAssociations = false,
  allowNodeDragging = false,
  allowCanvasPanning = true,
  allowPanOnScroll = false,
  showControls = true,
  showViewportToolbar = false,
  viewportLocked = false,
  onToggleViewportLock,
  fitView = true,
  fitViewPadding = 0.2,
  backgroundColor = '#dee2e6',
  backgroundGap = 16,
  nodePositions,
}: TopicAssociationsGraphProps) => {
  const { nodes, edges } = useMemo(
    () => buildTopicAssociationsGraph(topic, nodePositions),
    [nodePositions, topic]
  );

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<GraphTopicNodeData>;
    onTopicClick?.(graphNode.data);
  };

  const handleEdgeClick: EdgeMouseHandler = (_event, edge) => {
    if (!canDeleteAssociations || !onAssociationClick) {
      return;
    }

    const relatedTopicId = getRelatedTopicId(edge);
    if (relatedTopicId) {
      onAssociationClick(relatedTopicId);
    }
  };

  const handleNodeDragStop: NodeMouseHandler = (_event, node) => {
    onNodePositionChange?.(node.id, node.position);
  };

  return (
    <TopicGraphView
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      onNodeClick={onTopicClick ? handleNodeClick : undefined}
      onNodeDragStop={allowNodeDragging ? handleNodeDragStop : undefined}
      onEdgeClick={canDeleteAssociations ? handleEdgeClick : undefined}
      onMoveEnd={onMoveEnd}
      onConnect={onConnect}
      canEditAssociations={canEditAssociations}
      allowNodeDragging={allowNodeDragging}
      allowCanvasPanning={allowCanvasPanning}
      allowPanOnScroll={allowPanOnScroll}
      showControls={showControls}
      showViewportToolbar={showViewportToolbar}
      viewportLocked={viewportLocked}
      onToggleViewportLock={onToggleViewportLock}
      fitView={fitView}
      fitViewPadding={fitViewPadding}
      backgroundColor={backgroundColor}
      backgroundGap={backgroundGap}
    />
  );
};

export default TopicAssociationsGraph;

const getRelatedTopicId = (edge: Edge) => {
  const relatedNodeId = edge.target.startsWith('related-topic-') ? edge.target : edge.source;

  return relatedNodeId.startsWith('related-topic-')
    ? relatedNodeId.replace('related-topic-', '')
    : null;
};
