import { useEffect, useMemo, useRef } from 'react';
import { useReactFlow, type Edge, type OnNodeDrag } from '@xyflow/react';
import { applyForceLayout, type ForceLayoutHandle } from './topic-graph.utils.ts';

interface ForceLayoutControllerProps {
  baseNodes: { id: string; position: { x: number; y: number } }[];
  edges: Edge[];
  fitViewPadding: number;
  fitViewMaxZoom?: number;
  onHandleReady?: (handle: ForceLayoutHandle | null) => void;
}

const ForceLayoutController = ({
  baseNodes,
  edges,
  fitViewPadding,
  fitViewMaxZoom,
  onHandleReady,
}: ForceLayoutControllerProps) => {
  const { setNodes, fitView } = useReactFlow();
  const handleRef = useRef<ForceLayoutHandle | null>(null);

  // Keep the latest baseNodes/edges in refs so the simulation reads up-to-date
  // seeds, but does NOT recreate every time the parent re-renders new array
  // references (e.g. on selection changes).
  const baseNodesRef = useRef(baseNodes);
  const edgesRef = useRef(edges);
  useEffect(() => {
    baseNodesRef.current = baseNodes;
    edgesRef.current = edges;
  }, [baseNodes, edges]);

  // Topology key: only changes when the set of nodes or edges actually
  // changes. Selection / expansion state alone won't trigger sim recreation.
  const topologyKey = useMemo(() => {
    const ids = baseNodes
      .map((n) => n.id)
      .sort()
      .join('|');
    const links = edges
      .map((e) => `${e.source}>${e.target}`)
      .sort()
      .join('|');
    return `${ids}::${links}`;
  }, [baseNodes, edges]);

  useEffect(() => {
    if (baseNodesRef.current.length === 0) {
      handleRef.current = null;
      onHandleReady?.(null);
      return;
    }

    const handle = applyForceLayout(baseNodesRef.current, edgesRef.current, (positions) => {
      setNodes((current) =>
        current.map((node) => {
          if (node.dragging) return node;
          const next = positions.get(node.id);
          return next ? { ...node, position: next } : node;
        })
      );
    });
    handleRef.current = handle;
    onHandleReady?.(handle);

    const settleTimeout = window.setTimeout(() => {
      fitView({
        padding: fitViewPadding,
        maxZoom: fitViewMaxZoom ?? 1.5,
        duration: 600,
      });
    }, 1500);

    return () => {
      window.clearTimeout(settleTimeout);
      handle.stop();
      handleRef.current = null;
      onHandleReady?.(null);
    };
  }, [topologyKey, setNodes, fitView, fitViewPadding, fitViewMaxZoom, onHandleReady]);

  return null;
};

export type ForceDragHandlers = {
  onNodeDragStart: OnNodeDrag;
  onNodeDrag: OnNodeDrag;
  onNodeDragStop: OnNodeDrag;
};

export default ForceLayoutController;
