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
  const isFirstRunRef = useRef(true);
  useEffect(() => {
    // Skip the very first run after mount: focusNodeId may be a selection
    // restored when returning to the page (e.g. switching tabs and back), and
    // we don't want the camera to jump to it. Only react to focus changes the
    // user makes afterwards.
    const wasFirstRun = isFirstRunRef.current;
    isFirstRunRef.current = false;
    if (!focusNodeId) return;
    // When the same node is being explicitly centred (e.g. via search), let
    // NodeCenterer own the camera so the two don't animate against each other.
    if (centerNodeId && focusNodeId === centerNodeId) return;
    if (wasFirstRun) return;
    // Give React Flow a moment to commit any pending position updates (e.g.
    // when a click expands the card from a small dot to a wider popup).
    const timeout = window.setTimeout(() => {
      const node = getNode(focusNodeId);
      if (!node) return;
      const { zoom } = getViewport();
      const container = document.querySelector<HTMLElement>('.react-flow');
      if (!container) return;
      const rect = container.getBoundingClientRect();
      const w = rect.width;
      const h = rect.height;

      // Approximate node screen bounds (use a generous box to account for the
      // expanded popup card rendered above the dot).
      const nodeWidth = node.measured?.width ?? 150;
      const nodeHeight = node.measured?.height ?? 40;
      const nodeCenterX = node.position.x + nodeWidth / 2;

      // The selected node renders a card popup ABOVE the dot (see
      // GenericTopicNode). It is absolutely positioned, so it is NOT part of
      // node.measured — we must account for it explicitly or the card's top
      // gets clipped when the dot sits near the viewport edge. Measure the live
      // card (offsetHeight ignores both the zoom transform and the entrance
      // scale animation, so it is the true flow-unit height); fall back to a
      // generous estimate while it mounts.
      const cardEl = container.querySelector<HTMLElement>(
        `.react-flow__node[data-id="${CSS.escape(focusNodeId)}"] .mantine-Paper-root`
      );
      const CARD_WIDTH = 280;
      const CARD_GAP = 28; // dot gap + connector between card and dot
      const cardHeight = cardEl ? cardEl.offsetHeight : 300;

      // Combined bounding box of the popup card + dot, in flow coordinates.
      const boxLeft = Math.min(node.position.x, nodeCenterX - CARD_WIDTH / 2);
      const boxRight = Math.max(node.position.x + nodeWidth, nodeCenterX + CARD_WIDTH / 2);
      const boxTop = node.position.y - CARD_GAP - cardHeight;
      const boxBottom = node.position.y + nodeHeight;

      // flowToScreenPosition returns window-client coords (it adds the pane's
      // getBoundingClientRect offset), so subtract the container's top/left to
      // get coordinates relative to the graph itself before comparing against
      // its size. Without this the top-clip check never fires, because the
      // graph sits well below the window top (under the app header).
      const screenTopLeft = flowToScreenPosition({ x: boxLeft, y: boxTop });
      const screenBottomRight = flowToScreenPosition({ x: boxRight, y: boxBottom });
      const relTop = screenTopLeft.y - rect.top;
      const relLeft = screenTopLeft.x - rect.left;
      const relRight = screenBottomRight.x - rect.left;
      const relBottom = screenBottomRight.y - rect.top;

      const MARGIN = 24;
      const isClipped =
        relTop < MARGIN || relLeft < MARGIN || relRight > w - MARGIN || relBottom > h - MARGIN;
      if (!isClipped) return;

      // Centre the whole card+dot box so the entire card lands on screen.
      setCenter((boxLeft + boxRight) / 2, (boxTop + boxBottom) / 2, { duration: 350, zoom });
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
  // Force-layout positions saved from a previous mount, to restore the graph
  // instead of re-spreading it when returning to the page.
  savedForcePositions?: Record<string, { x: number; y: number }>;
  // Called on unmount with the current node positions so the parent can persist
  // them (used to restore the force layout on the next mount).
  onPersistPositions?: (positions: { id: string; position: { x: number; y: number } }[]) => void;
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
  savedForcePositions,
  onPersistPositions,
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

  // Persist the current node positions on unmount so the parent can restore the
  // force layout next time the graph mounts, instead of re-spreading it (which
  // makes the layout fan out when navigating away from the page and back).
  const latestNodesRef = useRef(internalNodes);
  useEffect(() => {
    latestNodesRef.current = internalNodes;
  }, [internalNodes]);
  const onPersistRef = useRef(onPersistPositions);
  useEffect(() => {
    onPersistRef.current = onPersistPositions;
  }, [onPersistPositions]);
  useEffect(
    () => () => {
      onPersistRef.current?.(
        latestNodesRef.current.map((n) => ({ id: n.id, position: n.position }))
      );
    },
    []
  );

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
        <ForceLayoutController
          baseNodes={nodes}
          edges={edges}
          onHandleReady={handleForceReady}
          savedPositions={savedForcePositions}
          setNodes={setInternalNodes}
        />
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
