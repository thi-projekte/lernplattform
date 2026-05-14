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
  lockedAssociationEdgeId?: string | null;
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
  lockedAssociationEdgeId,
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
  const built = useMemo(
    () => buildPersonalTopicsGraph(topics, currentUsername),
    [currentUsername, topics]
  );

  const nodes = useMemo(() => {
    if (!selectedTopicId) return built.nodes;
    return built.nodes.map((node) =>
      node.id === selectedTopicId ? { ...node, selected: true } : node
    );
  }, [built.nodes, selectedTopicId]);

  const edges = useMemo(() => {
    if (!lockedAssociationEdgeId) return built.edges;
    return built.edges.map((edge) =>
      edge.id === lockedAssociationEdgeId
        ? { ...edge, type: 'protected', data: { tooltipLabel: lockedAssociationTooltip } }
        : edge
    );
  }, [built.edges, lockedAssociationEdgeId, lockedAssociationTooltip]);

  const handleNodeClick: NodeMouseHandler = (_event, node) => {
    onTopicClick?.(node.data as GraphTopicNodeData);
  };

  const handleEdgeClick: EdgeMouseHandler = (_event, edge) => {
    if (!canDeleteAssociations || !onAssociationClick || !selectedTopicId) {
      return;
    }

    if (lockedAssociationEdgeId && edge.id === lockedAssociationEdgeId) {
      return;
    }

    if (edge.source !== selectedTopicId && edge.target !== selectedTopicId) {
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
