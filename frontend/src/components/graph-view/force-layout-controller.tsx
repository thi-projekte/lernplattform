import { useEffect, useMemo, useRef } from 'react';
import { useReactFlow, type Edge, type OnNodeDrag } from '@xyflow/react';
import { applyForceLayout, type ForceLayoutHandle } from './topic-graph.utils.ts';

interface ForceLayoutControllerProps {
  baseNodes: { id: string; position: { x: number; y: number } }[];
  edges: Edge[];
  onHandleReady?: (handle: ForceLayoutHandle | null) => void;
}

const ForceLayoutController = ({
  baseNodes,
  edges,
  onHandleReady,
}: ForceLayoutControllerProps) => {
  const { setNodes, fitView } = useReactFlow();
  const handleRef = useRef<ForceLayoutHandle | null>(null);
  // First sim creation seeds from dagre — needs full alpha to spread out, and
  // a fitView so the user sees the freshly arranged graph. Subsequent
  // recreations (topology change, e.g. expanding a node) use a softer alpha
  // and do NOT touch the camera so the user's current focus is preserved.
  const hasInitializedRef = useRef(false);

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

    const isFirstSim = !hasInitializedRef.current;
    const initialAlpha = isFirstSim ? 1 : 0.3;
    hasInitializedRef.current = true;
    const handle = applyForceLayout(
      baseNodesRef.current,
      edgesRef.current,
      (positions) => {
        setNodes((current) =>
          current.map((node) => {
            if (node.dragging) return node;
            const next = positions.get(node.id);
            return next ? { ...node, position: next } : node;
          })
        );
      },
      initialAlpha
    );
    handleRef.current = handle;
    onHandleReady?.(handle);

    // Only fitView on the very first simulation so the user sees the freshly
    // arranged graph; later recreations keep the user's current camera.
    let settleTimeout: number | undefined;
    if (isFirstSim) {
      settleTimeout = window.setTimeout(() => {
        fitView({ padding: 0.2, maxZoom: 1.2, duration: 600 });
      }, 1400);
    }

    return () => {
      if (settleTimeout !== undefined) window.clearTimeout(settleTimeout);
      handle.stop();
      handleRef.current = null;
      onHandleReady?.(null);
    };
  }, [topologyKey, setNodes, fitView, onHandleReady]);

  return null;
};

export type ForceDragHandlers = {
  onNodeDragStart: OnNodeDrag;
  onNodeDrag: OnNodeDrag;
  onNodeDragStop: OnNodeDrag;
};

export default ForceLayoutController;
