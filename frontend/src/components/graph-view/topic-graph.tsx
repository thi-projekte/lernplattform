import {
  Background,
  Controls,
  Panel,
  ReactFlow,
  PanOnScrollMode,
  useEdgesState,
  useNodesState,
  useReactFlow,
  type Edge,
  type EdgeMouseHandler,
  type Node,
  type NodeMouseHandler,
  type OnConnect,
  type OnMoveEnd,
  type NodeTypes,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { useEffect } from 'react';
import { ActionIcon, Paper, Stack, Tooltip } from '@mantine/core';
import { IconLock, IconLockOpen2, IconMinus, IconPlus, IconZoomReset } from '@tabler/icons-react';

interface TopicGraphViewProps {
  nodes: Node[];
  edges: Edge[];
  nodeTypes: NodeTypes;
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
  backgroundColor?: string;
  backgroundGap?: number;
}

interface ViewportToolbarProps {
  fitViewPadding: number;
  viewportLocked: boolean;
  onToggleViewportLock?: () => void;
}

const ViewportToolbar = ({
  fitViewPadding,
  viewportLocked,
  onToggleViewportLock,
}: ViewportToolbarProps) => {
  const { zoomIn, zoomOut, fitView } = useReactFlow();

  return (
    <Panel position="top-left" style={{ marginTop: 18, marginLeft: 18 }}>
      <Paper
        withBorder
        radius="md"
        shadow="sm"
        style={{ overflow: 'hidden', background: 'rgba(255, 255, 255, 0.94)' }}
      >
        <Stack gap={0}>
          <Tooltip label="Zoom in" position="right">
            <ActionIcon
              variant="subtle"
              color="gray"
              radius={0}
              size={44}
              aria-label="Zoom in"
              onClick={() => zoomIn({ duration: 180 })}
            >
              <IconPlus size={22} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Zoom out" position="right">
            <ActionIcon
              variant="subtle"
              color="gray"
              radius={0}
              size={44}
              aria-label="Zoom out"
              onClick={() => zoomOut({ duration: 180 })}
            >
              <IconMinus size={22} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Fit view" position="right">
            <ActionIcon
              variant="subtle"
              color="gray"
              radius={0}
              size={44}
              aria-label="Fit view"
              onClick={() => fitView({ padding: fitViewPadding, duration: 220 })}
            >
              <IconZoomReset size={20} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label={viewportLocked ? 'Unlock graph' : 'Lock graph'} position="right">
            <ActionIcon
              variant={viewportLocked ? 'filled' : 'subtle'}
              color={viewportLocked ? 'dark' : 'gray'}
              radius={0}
              size={44}
              aria-label={viewportLocked ? 'Unlock graph' : 'Lock graph'}
              onClick={onToggleViewportLock}
            >
              {viewportLocked ? <IconLock size={18} stroke={2} /> : <IconLockOpen2 size={18} stroke={2} />}
            </ActionIcon>
          </Tooltip>
        </Stack>
      </Paper>
    </Panel>
  );
};

const TopicGraphView = ({
  nodes,
  edges,
  nodeTypes,
  onNodeClick,
  onNodeDragStop,
  onEdgeClick,
  onMoveEnd,
  onConnect,
  canEditAssociations = false,
  allowNodeDragging = false,
  allowCanvasPanning = true,
  allowPanOnScroll = false,
  showControls = true,
  showViewportToolbar = false,
  viewportLocked = false,
  onToggleViewportLock,
  fitView = true,
  fitViewPadding = 0.2,
  backgroundColor = '#dee2e6',
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
      onNodeClick={onNodeClick}
      onNodeDragStop={onNodeDragStop}
      onEdgeClick={onEdgeClick}
      onMoveEnd={onMoveEnd}
      onNodesChange={onNodesChange}
      onEdgesChange={onEdgesChange}
      onConnect={canEditAssociations ? onConnect : undefined}
      fitView={fitView}
      fitViewOptions={{ padding: fitViewPadding }}
      nodesDraggable={canEditAssociations || allowNodeDragging}
      nodesConnectable={canEditAssociations}
      connectOnClick={false}
      panOnDrag={allowCanvasPanning ? (allowNodeDragging ? [1, 2] : true) : false}
      panOnScroll={allowPanOnScroll}
      panOnScrollMode={PanOnScrollMode.Free}
      zoomOnScroll={!allowPanOnScroll}
      selectionOnDrag={false}
      elementsSelectable
    >
      {showControls && <Controls showInteractive={canEditAssociations} />}
      {showViewportToolbar && (
        <ViewportToolbar
          fitViewPadding={fitViewPadding}
          viewportLocked={viewportLocked}
          onToggleViewportLock={onToggleViewportLock}
        />
      )}
      <Background color={backgroundColor} gap={backgroundGap} />
    </ReactFlow>
  );
};

export default TopicGraphView;
