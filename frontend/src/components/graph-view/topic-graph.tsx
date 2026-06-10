import {
  Background,
  ReactFlow,
  PanOnScrollMode,
  useEdgesState,
  useNodesState,
  useReactFlow,
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

// Mounts only while the tree layout is active. On mount (i.e. right after the
// user toggles to tree mode) it waits one frame for ReactFlow to commit the
// restored tree positions, then re-fits the viewport so the graph is centred
// instead of leaving the camera wherever force mode left it.
const TreeLayoutFit = ({ padding, maxZoom }: { padding: number; maxZoom?: number }) => {
  const { fitView } = useReactFlow();
  useEffect(() => {
    const timeout = window.setTimeout(() => {
      fitView({ padding, maxZoom: maxZoom ?? 1.2, duration: 400 });
    }, 80);
    return () => window.clearTimeout(timeout);
  }, [fitView, padding, maxZoom]);
  return null;
};

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
    if (layoutMode === 'force') {
      // In force mode, the simulation owns positions. When new nodes arrive
      // via props (e.g. on expansion), keep existing nodes at their current
      // force position and only adopt prop position for genuinely new nodes.
      setInternalNodes((current) => {
        const currentPosById = new Map(current.map((n) => [n.id, n.position]));
        return nodes.map((n) => ({
          ...n,
          position: currentPosById.get(n.id) ?? n.position,
        }));
      });
    } else {
      setInternalNodes(nodes);
    }
  }, [nodes, setInternalNodes, layoutMode]);

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
        <ForceLayoutController baseNodes={nodes} edges={edges} onHandleReady={handleForceReady} />
      )}
      {layoutMode === 'tree' && <TreeLayoutFit padding={fitViewPadding} maxZoom={fitViewMaxZoom} />}
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
