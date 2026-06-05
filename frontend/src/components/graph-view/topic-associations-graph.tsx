import { useMemo } from 'react';
import type {
  Edge,
  EdgeMouseHandler,
  Node,
  NodeMouseHandler,
  OnNodeDrag,
  NodeTypes,
  OnConnect,
  OnMoveEnd,
} from '@xyflow/react';
import GenericTopicNode from './generic-topic-node.tsx';
import TopicGraphView from './topic-graph.tsx';
import { buildTopicAssociationsGraph } from './topic-graph.utils.ts';
import type {
  GraphTopicNodeData,
  TopicAssociationsGraphInput,
  TopicGraphNodePositions,
} from './topic-graph.types.ts';

const nodeTypes: NodeTypes = {
  topic: GenericTopicNode,
};

interface TopicAssociationsGraphProps {
  topic?: TopicAssociationsGraphInput;
  currentUsername?: string;
  selectedTopicId?: string;
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
  fitViewMaxZoom?: number;
  backgroundColor?: string;
  backgroundGap?: number;
  nodePositions?: TopicGraphNodePositions;
}

const TopicAssociationsGraph = ({
  topic,
  currentUsername,
  selectedTopicId,
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
  fitViewMaxZoom,
  backgroundColor = '#d1d5db',
  backgroundGap = 16,
  nodePositions,
}: TopicAssociationsGraphProps) => {
  const { nodes: builtNodes, edges } = useMemo(
    () => buildTopicAssociationsGraph(topic, nodePositions, currentUsername),
    [currentUsername, nodePositions, topic]
  );

  const nodes = useMemo(() => {
    if (!selectedTopicId) {
      return builtNodes;
    }

    return builtNodes.map((node) =>
      node.id === selectedTopicId ? { ...node, selected: true } : node
    );
  }, [builtNodes, selectedTopicId]);

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<GraphTopicNodeData>;
    onTopicClick?.({
      ...graphNode.data,
      graphNodeId: graphNode.id,
    });
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

  const handleNodeDragStop: OnNodeDrag = (_event, node) => {
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
      fitViewMaxZoom={fitViewMaxZoom}
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
