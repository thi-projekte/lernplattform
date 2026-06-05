import { useEffect, useMemo, useRef } from 'react';
import { useReactFlow, type Edge, type OnNodeDrag } from '@xyflow/react';
import { applyForceLayout, type ForceLayoutHandle } from './topic-graph.utils.ts';

interface ForceLayoutControllerProps {
  baseNodes: { id: string; position: { x: number; y: number } }[];
  edges: Edge[];
  onHandleReady?: (handle: ForceLayoutHandle | null) => void;
}

const ForceLayoutController = ({ baseNodes, edges, onHandleReady }: ForceLayoutControllerProps) => {
  const { setNodes, getNodes, fitView } = useReactFlow();
  const handleRef = useRef<ForceLayoutHandle | null>(null);
  // First sim creation seeds from dagre — needs full alpha to spread out.
  // Subsequent recreations (topology change, e.g. expanding a node) use a
  // softer alpha so existing nodes don't get yanked around.
  const hasInitializedRef = useRef(false);
  // The initial fitView must run once after the graph settles. If the
  // simulation is recreated mid-settle (e.g. cached neighbours load right
  // after the page mounts), the previous timeout gets cancelled — so we keep
  // re-scheduling fitView until it has actually fired once.
  const hasFitOnceRef = useRef(false);

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
    // Warm up the simulation synchronously on the first creation so nodes
    // appear at their settled positions immediately, without the user having
    // to wait for the live tick loop. Later recreations skip the warmup so
    // existing nodes don't visibly jump.
    const warmupTicks = isFirstSim ? 200 : 0;
    hasInitializedRef.current = true;

    // Seed from the live React Flow store so existing nodes start at their
    // current force-positions rather than the (stale) tree positions from
    // the parent's nodes prop. New nodes (just added by expansion) fall back
    // to the prop position which home.tsx places near the parent.
    const liveNodes = getNodes();
    const livePosById = new Map(liveNodes.map((n) => [n.id, n.position]));
    const seedNodes = baseNodesRef.current.map((n) => ({
      id: n.id,
      position: livePosById.get(n.id) ?? n.position,
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
      warmupTicks
    );
    handleRef.current = handle;
    onHandleReady?.(handle);

    return () => {
      handle.stop();
      handleRef.current = null;
      onHandleReady?.(null);
    };
  }, [topologyKey, setNodes, getNodes, onHandleReady]);

  // Initial camera fit — runs once per mount, independent of sim recreations
  // so it survives cached-neighbour reloads that would otherwise cancel a
  // timeout tied to the sim useEffect.
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
