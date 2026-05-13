import { useMemo } from 'react';
import type {
  EdgeMouseHandler,
  EdgeTypes,
  NodeMouseHandler,
  NodeTypes,
  OnConnect,
  OnMoveEnd,
} from '@xyflow/react';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import type { GraphTopicNodeData } from './topic-graph.types.ts';
import GenericTopicNode from './generic-topic-node.tsx';
import LockedAssociationEdge from './locked-association-edge.tsx';
import TopicGraphView from './topic-graph.tsx';
import { buildPersonalTopicsGraph } from './topic-graph.utils.ts';

const nodeTypes: NodeTypes = {
  topic: GenericTopicNode,
};

const edgeTypes: EdgeTypes = {
  protected: LockedAssociationEdge,
};

interface PersonalTopicsGraphProps {
  topics: GraphTopicDto[];
  currentUsername?: string;
  selectedTopicId?: string;
  lockedAssociationEdgeIds?: Set<string>;
  lockedAssociationTooltip?: string;
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
  selectedTopicId,
  lockedAssociationEdgeIds,
  lockedAssociationTooltip,
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
  const { nodes, edges } = useMemo(() => {
    const built = buildPersonalTopicsGraph(topics, currentUsername);
    if (!lockedAssociationEdgeIds || lockedAssociationEdgeIds.size === 0) {
      return built;
    }

    const decoratedEdges = built.edges.map((edge) =>
      lockedAssociationEdgeIds.has(edge.id)
        ? { ...edge, type: 'protected', data: { tooltipLabel: lockedAssociationTooltip } }
        : edge
    );

    return { nodes: built.nodes, edges: decoratedEdges };
  }, [currentUsername, lockedAssociationEdgeIds, lockedAssociationTooltip, topics]);

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    onTopicClick?.(node.data as GraphTopicNodeData);
  };

  const handleEdgeClick: EdgeMouseHandler = (_event, edge) => {
    if (!canDeleteAssociations || !onAssociationClick || !selectedTopicId) {
      return;
    }

    if (lockedAssociationEdgeIds?.has(edge.id)) {
      return;
    }

    const relatedTopicId = edge.source === selectedTopicId ? edge.target : edge.source;
    if (!relatedTopicId || relatedTopicId === selectedTopicId) {
      return;
    }

    onAssociationClick(relatedTopicId);
  };

  return (
    <TopicGraphView
      nodes={nodes}
      edges={edges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
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
      backgroundColor="#d1d5db"
    />
  );
};

export default PersonalTopicsGraph;
