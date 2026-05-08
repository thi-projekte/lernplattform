import { useMemo } from 'react';
import type {
  EdgeMouseHandler,
  NodeMouseHandler,
  NodeTypes,
  OnConnect,
  OnMoveEnd,
} from '@xyflow/react';
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
  onConnect?: OnConnect;
  onAssociationClick?: (relatedTopicId: string) => void;
  canEditAssociations?: boolean;
  canDeleteAssociations?: boolean;
  allowNodeDragging?: boolean;
  allowCanvasPanning?: boolean;
  allowPanOnScroll?: boolean;
  viewportLocked?: boolean;
  onToggleViewportLock?: () => void;
}

const PersonalTopicsGraph = ({
  topics,
  currentUsername,
  onTopicClick,
  onMoveEnd,
  onConnect,
  onAssociationClick,
  canEditAssociations = false,
  canDeleteAssociations = false,
  allowNodeDragging = false,
  allowCanvasPanning = true,
  allowPanOnScroll = true,
  viewportLocked = false,
  onToggleViewportLock,
}: PersonalTopicsGraphProps) => {
  const { nodes, edges } = useMemo(
    () => buildPersonalTopicsGraph(topics, currentUsername),
    [currentUsername, topics]
  );

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    onTopicClick?.(node.data as GraphTopicNodeData);
  };

  const handleEdgeClick: EdgeMouseHandler = (_event, edge) => {
    if (!canDeleteAssociations || !onAssociationClick) {
      return;
    }

    const relatedTopicId = edge.target.startsWith('personal-topic-')
      ? edge.target.replace('personal-topic-', '')
      : edge.target;

    onAssociationClick(relatedTopicId);
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
      allowPanOnScroll={allowPanOnScroll}
      showControls={false}
      showViewportToolbar
      viewportLocked={viewportLocked}
      onToggleViewportLock={onToggleViewportLock}
      fitViewMaxZoom={0.99}
    />
  );
};

export default PersonalTopicsGraph;
