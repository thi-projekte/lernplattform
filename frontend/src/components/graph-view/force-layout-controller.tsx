import { useEffect, useMemo, useRef } from 'react';
import { useReactFlow, type Edge, type OnNodeDrag } from '@xyflow/react';
import { applyForceLayout, type ForceLayoutHandle } from './topic-graph.utils.ts';

interface ForceLayoutControllerProps {
  baseNodes: { id: string; position: { x: number; y: number } }[];
  edges: Edge[];
  onHandleReady?: (handle: ForceLayoutHandle | null) => void;

  savedPositions?: Record<string, { x: number; y: number }>;
}

const ForceLayoutController = ({
  baseNodes,
  edges,
  onHandleReady,
  savedPositions,
}: ForceLayoutControllerProps) => {
  const { setNodes, getNodes, fitView } = useReactFlow();
  const savedPositionsRef = useRef(savedPositions);
  useEffect(() => {
    savedPositionsRef.current = savedPositions;
  }, [savedPositions]);
  const handleRef = useRef<ForceLayoutHandle | null>(null);
  
  const hasInitializedRef = useRef(false);
 
  const hasFitOnceRef = useRef(false);

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

    const saved = savedPositionsRef.current;
    const isFirstSim = !hasInitializedRef.current;
  
    const hasSaved = isFirstSim && !!saved && baseNodesRef.current.some((n) => saved[n.id]);
    const fullSpread = isFirstSim && !hasSaved;
    // Lower alpha = less energy, so nodes glide instead of being kicked around.
    const initialAlpha = fullSpread ? 1 : 0.03;
    const warmupTicks = fullSpread ? 200 : 0;
    hasInitializedRef.current = true;

  
    const liveNodes = getNodes();
    const livePosById = new Map(liveNodes.map((n) => [n.id, n.position]));
    const seedSaved = hasSaved ? saved : undefined;
    const seedNodes = baseNodesRef.current.map((n) => ({
      id: n.id,
      position: seedSaved?.[n.id] ?? livePosById.get(n.id) ?? n.position,
    }));

    const handle = applyForceLayout(
      seedNodes,
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
      initialAlpha,
      warmupTicks,

      fullSpread ? 1 : 0
    );
    handleRef.current = handle;
    onHandleReady?.(handle);

    return () => {
      handle.stop();
      handleRef.current = null;
      onHandleReady?.(null);
    };
  }, [topologyKey, setNodes, getNodes, onHandleReady]);


  useEffect(() => {
    const timeout = window.setTimeout(() => {
      if (hasFitOnceRef.current) return;
      hasFitOnceRef.current = true;
      fitView({ padding: 0.2, maxZoom: 1.2, duration: 400 });
    }, 400);
    return () => window.clearTimeout(timeout);
  }, [fitView]);

  return null;
};

export type ForceDragHandlers = {
  onNodeDragStart: OnNodeDrag;
  onNodeDrag: OnNodeDrag;
  onNodeDragStop: OnNodeDrag;
};

export default ForceLayoutController;
