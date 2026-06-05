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
  type OnNodeDrag,
  type OnConnect,
  type OnMoveEnd,
  type NodeTypes,
  type EdgeTypes,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useCallback, useEffect, useRef } from 'react';
import ViewportToolbar from './viewport-toolbar.tsx';
import { type ForceLayoutHandle } from './topic-graph.utils.ts';
import ForceLayoutController from './force-layout-controller.tsx';

export type GraphLayoutMode = 'tree' | 'force';

interface TopicGraphViewProps {
  nodes: Node[];
  edges: Edge[];
  nodeTypes: NodeTypes;
  edgeTypes?: EdgeTypes;
  onNodeClick?: NodeMouseHandler;
  onNodeDragStop?: OnNodeDrag;
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
  layoutMode?: GraphLayoutMode;
  onChangeLayoutMode?: (mode: GraphLayoutMode) => void;
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
  layoutMode = 'tree',
  onChangeLayoutMode,
  fitView = true,
  fitViewPadding = 0.2,
  fitViewMaxZoom,
  backgroundColor = '#d9e7f3',
  backgroundGap = 16,
}: TopicGraphViewProps) => {
  const [internalNodes, setInternalNodes, onNodesChange] = useNodesState(nodes);
  const [internalEdges, setInternalEdges, onEdgesChange] = useEdgesState(edges);
  const forceHandleRef = useRef<ForceLayoutHandle | null>(null);

  useEffect(() => {
    setInternalNodes(nodes);
  }, [nodes, setInternalNodes]);

  useEffect(() => {
    setInternalEdges(edges);
  }, [edges, setInternalEdges]);

  const handleForceReady = useCallback((handle: ForceLayoutHandle | null) => {
    forceHandleRef.current = handle;
  }, []);

  const handleNodeDragStart: OnNodeDrag = (_event, node) => {
    forceHandleRef.current?.beginDrag(node.id, node.position);
  };

  const handleNodeDrag: OnNodeDrag = (_event, node) => {
    forceHandleRef.current?.drag(node.id, node.position);
  };

  const handleNodeDragStop: OnNodeDrag = (event, node, nodes) => {
    forceHandleRef.current?.endDrag(node.id);
    onNodeDragStop?.(event, node, nodes);
  };

  return (
    <ReactFlow
      nodes={internalNodes}
      edges={internalEdges}
      nodeTypes={nodeTypes}
      edgeTypes={edgeTypes}
      onNodeClick={onNodeClick}
      onNodeDragStart={layoutMode === 'force' ? handleNodeDragStart : undefined}
      onNodeDrag={layoutMode === 'force' ? handleNodeDrag : undefined}
      onNodeDragStop={layoutMode === 'force' ? handleNodeDragStop : onNodeDragStop}
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
      panOnDrag={allowCanvasPanning}
      panOnScroll={allowPanOnScroll}
      panOnScrollMode={PanOnScrollMode.Free}
      zoomOnScroll={!allowPanOnScroll}
      selectionOnDrag={false}
      elementsSelectable
      elevateEdgesOnSelect
      zoomActivationKeyCode={null}
    >
      {layoutMode === 'force' && (
        <ForceLayoutController
          baseNodes={nodes}
          edges={edges}
          onHandleReady={handleForceReady}
        />
      )}
      {showViewportToolbar && (
        <ViewportToolbar
          fitViewPadding={fitViewPadding}
          fitViewMaxZoom={fitViewMaxZoom}
          viewportLocked={viewportLocked}
          onToggleViewportLock={onToggleViewportLock}
          layoutMode={layoutMode}
          onChangeLayoutMode={onChangeLayoutMode}
        />
      )}
      <Background color={backgroundColor} gap={backgroundGap} />
    </ReactFlow>
  );
};

export default TopicGraphView;
