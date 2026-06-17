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

// When `focusNodeId` changes, check whether that node is comfortably inside
// the viewport. If it is partially clipped (or fully off-screen), pan the
// camera so it becomes visible. We don't move when the node is already in
// view — that would feel jumpy.
const NodeFocusKeeper = ({
  focusNodeId,
  centerNodeId,
}: {
  focusNodeId: string | null | undefined;
  centerNodeId: string | null | undefined;
}) => {
  const { getNode, getViewport, setCenter, flowToScreenPosition } = useReactFlow();
  useEffect(() => {
    if (!focusNodeId) return;
    // When the same node is being explicitly centred (e.g. via search), let
    // NodeCenterer own the camera so the two don't animate against each other.
    if (centerNodeId && focusNodeId === centerNodeId) return;
    // Give React Flow a moment to commit any pending position updates (e.g.
    // when a click expands the card from a small dot to a wider popup).
    const timeout = window.setTimeout(() => {
      const node = getNode(focusNodeId);
      if (!node) return;
      const { zoom } = getViewport();
      const container = document.querySelector<HTMLElement>('.react-flow');
      if (!container) return;
      const { width: w, height: h } = container.getBoundingClientRect();

      // Approximate node screen bounds (use a generous box to account for the
      // expanded popup card rendered above the dot).
      const screenTopLeft = flowToScreenPosition(node.position);
      const measuredWidth = (node.measured?.width ?? 200) * zoom;
      const measuredHeight = (node.measured?.height ?? 200) * zoom;
      // Card pops UP from the dot — extend the box upwards.
      const popupHeight = 260 * zoom;
      const top = screenTopLeft.y - popupHeight;
      const bottom = screenTopLeft.y + measuredHeight;
      const left = screenTopLeft.x;
      const right = screenTopLeft.x + measuredWidth;

      const MARGIN = 32;
      const isClipped = top < MARGIN || left < MARGIN || right > w - MARGIN || bottom > h - MARGIN;
      if (!isClipped) return;

      // Centre the node (accounting for the popup that pops above it) inside
      // the viewport with a smooth transition.
      const centerX = node.position.x + (node.measured?.width ?? 200) / 2;
      const centerY = node.position.y + (node.measured?.height ?? 200) / 2 - 80;
      setCenter(centerX, centerY, { duration: 350, zoom });
    }, 60);
    return () => window.clearTimeout(timeout);
  }, [focusNodeId, centerNodeId, getNode, getViewport, setCenter, flowToScreenPosition]);
  return null;
};

// When `centerNodeId` changes to a non-null id, centre the camera on that node
// — always, regardless of whether it is currently visible. Used for explicit
// navigation such as the spotlight search, where the user expects the target
// node to land in the middle of the viewport. We wait (frame by frame) until
// the node exists and has been measured so the centre is accurate, since a
// freshly injected node is not in the store on the first frame.
const NodeCenterer = ({
  centerNodeId,
  onCenterDone,
}: {
  centerNodeId: string | null | undefined;
  onCenterDone?: () => void;
}) => {
  const { getNode, getViewport, setCenter } = useReactFlow();
  const onCenterDoneRef = useRef(onCenterDone);
  useEffect(() => {
    onCenterDoneRef.current = onCenterDone;
  }, [onCenterDone]);
  useEffect(() => {
    if (!centerNodeId) return;
    let cancelled = false;
    let rafId = 0;
    let frames = 0;
    // ~1.5s at 60fps: long enough for a new node to mount, expand and be
    // measured, short enough to fall back gracefully if it never appears.
    const MAX_FRAMES = 90;
    const attempt = () => {
      if (cancelled) return;
      frames += 1;
      const node = getNode(centerNodeId);
      const measuredWidth = node?.measured?.width;
      const ready = !!node && !!measuredWidth;
      const timedOut = frames >= MAX_FRAMES;
      if (ready || timedOut) {
        if (node) {
          const width = node.measured?.width ?? 150;
          const height = node.measured?.height ?? 120;
          const centerX = node.position.x + width / 2;
          const centerY = node.position.y + height / 2;
          const { zoom } = getViewport();
          setCenter(centerX, centerY, { duration: 400, zoom });
        }
        onCenterDoneRef.current?.();
        return;
      }
      rafId = requestAnimationFrame(attempt);
    };
    rafId = requestAnimationFrame(attempt);
    return () => {
      cancelled = true;
      cancelAnimationFrame(rafId);
    };
  }, [centerNodeId, getNode, getViewport, setCenter]);
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
  focusNodeId?: string | null;
  centerNodeId?: string | null;
  onCenterDone?: () => void;
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
  focusNodeId,
  centerNodeId,
  onCenterDone,
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
      nodesDraggable={!viewportLocked && (canEditAssociations || allowNodeDragging)}
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
      <NodeFocusKeeper focusNodeId={focusNodeId} centerNodeId={centerNodeId} />
      <NodeCenterer centerNodeId={centerNodeId} onCenterDone={onCenterDone} />
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
