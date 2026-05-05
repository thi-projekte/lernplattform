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
import type { GraphTopicNodeData, TopicAssociationsGraphInput } from './topic-graph.types.ts';

const nodeTypes: NodeTypes = {
  topic: TopicNode,
};

interface TopicAssociationsGraphProps {
  topic?: TopicAssociationsGraphInput;
  onTopicClick?: (topic: GraphTopicNodeData) => void;
  onAssociationCreate?: (topicId: string) => void;
  onAssociationClick?: (relatedTopicId: string) => void;
  onMoveEnd?: OnMoveEnd;
  onConnect?: OnConnect;
  canEditAssociations?: boolean;
  canDeleteAssociations?: boolean;
  allowNodeDragging?: boolean;
  allowCanvasPanning?: boolean;
  showControls?: boolean;
  fitView?: boolean;
  fitViewPadding?: number;
  backgroundColor?: string;
  backgroundGap?: number;
}

const TopicAssociationsGraph = ({
  topic,
  onTopicClick,
  onAssociationCreate,
  onAssociationClick,
  onMoveEnd,
  onConnect,
  canEditAssociations = false,
  canDeleteAssociations = false,
  allowNodeDragging = false,
  allowCanvasPanning = true,
  showControls = true,
  fitView = true,
  fitViewPadding = 0.2,
  backgroundColor = '#dee2e6',
  backgroundGap = 16,
}: TopicAssociationsGraphProps) => {
  const { nodes, edges } = useMemo(() => buildTopicAssociationsGraph(topic), [topic]);

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<GraphTopicNodeData>;

    if (canEditAssociations && graphNode.data.isIsolated && onAssociationCreate) {
      const topicId = graphNode.data.payload.id;
      if (typeof topicId === 'string') {
        onAssociationCreate(topicId);
      }
      return;
    }

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

  return (
    <TopicGraphView
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      onNodeClick={onTopicClick ? handleNodeClick : undefined}
      onEdgeClick={canDeleteAssociations ? handleEdgeClick : undefined}
      onMoveEnd={onMoveEnd}
      onConnect={onConnect}
      canEditAssociations={canEditAssociations}
      allowNodeDragging={allowNodeDragging}
      allowCanvasPanning={allowCanvasPanning}
      showControls={showControls}
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
