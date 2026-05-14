import {
  Background,
  ReactFlow,
  PanOnScrollMode,
  useEdgesState,
  useNodesState,
  type Edge,
  type EdgeMouseHandler,
  type Node,
  type NodeMouseHandler,
  type OnConnect,
  type OnMoveEnd,
  type NodeTypes,
  type EdgeTypes,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useEffect } from 'react';
import ViewportToolbar from './viewport-toolbar.tsx';

interface TopicGraphViewProps {
  nodes: Node[];
  edges: Edge[];
  nodeTypes: NodeTypes;
  edgeTypes?: EdgeTypes;
  onNodeClick?: NodeMouseHandler;
  onNodeDragStop?: NodeMouseHandler;
  onEdgeClick?: EdgeMouseHandler;
  onMoveEnd?: OnMoveEnd;
  onConnect?: OnConnect;
  canEditAssociations?: boolean;
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
}

const TopicGraphView = ({
  nodes,
  edges,
  nodeTypes,
  edgeTypes,
  onNodeClick,
  onNodeDragStop,
  onEdgeClick,
  onMoveEnd,
  onConnect,
  canEditAssociations = false,
  allowNodeDragging = false,
  allowCanvasPanning = true,
  allowPanOnScroll = false,
  showViewportToolbar = false,
  viewportLocked = false,
  onToggleViewportLock,
  fitView = true,
  fitViewPadding = 0.2,
  fitViewMaxZoom,
  backgroundColor = '#d9e7f3',
  backgroundGap = 16,
}: TopicGraphViewProps) => {
  const [internalNodes, setInternalNodes, onNodesChange] = useNodesState(nodes);
  const [internalEdges, setInternalEdges, onEdgesChange] = useEdgesState(edges);

  useEffect(() => {
    setInternalNodes(nodes);
  }, [nodes, setInternalNodes]);

  useEffect(() => {
    setInternalEdges(edges);
  }, [edges, setInternalEdges]);

  return (
    <ReactFlow
      nodes={internalNodes}
      edges={internalEdges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      onNodeClick={onNodeClick}
      onNodeDragStop={onNodeDragStop}
      onEdgeClick={onEdgeClick}
      onMoveEnd={onMoveEnd}
      onNodesChange={onNodesChange}
      onEdgesChange={onEdgesChange}
      onConnect={canEditAssociations ? onConnect : undefined}
      fitView={fitView}
      fitViewOptions={{ padding: fitViewPadding, maxZoom: fitViewMaxZoom }}
      nodesDraggable={canEditAssociations || allowNodeDragging}
      nodesConnectable={canEditAssociations}
      nodeDragThreshold={6}
      connectOnClick={false}
      panOnDrag={allowCanvasPanning ? (allowNodeDragging ? [1, 2] : true) : false}
      panOnScroll={allowPanOnScroll}
      panOnScrollMode={PanOnScrollMode.Free}
      zoomOnScroll={!allowPanOnScroll}
      selectionOnDrag={false}
      elementsSelectable
    >
      {showViewportToolbar && (
        <ViewportToolbar
          fitViewPadding={fitViewPadding}
          fitViewMaxZoom={fitViewMaxZoom}
          viewportLocked={viewportLocked}
          onToggleViewportLock={onToggleViewportLock}
        />
      )}
      <Background color={backgroundColor} gap={backgroundGap} />
    </ReactFlow>
  );
};

export default TopicGraphView;
