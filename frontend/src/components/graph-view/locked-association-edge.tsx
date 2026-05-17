import { BaseEdge, EdgeLabelRenderer, getStraightPath, type EdgeProps } from '@xyflow/react';
import { Tooltip } from '@mantine/core';
import { IconLock } from '@tabler/icons-react';

interface LockedAssociationEdgeData extends Record<string, unknown> {
  tooltipLabel?: string;
}

const LockedAssociationEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  style,
  data,
}: EdgeProps) => {
  const [edgePath, labelX, labelY] = getStraightPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
  });
  const tooltipLabel = (data as LockedAssociationEdgeData | undefined)?.tooltipLabel;

  return (
    <>
      <BaseEdge
        id={id}
        path={edgePath}
        style={{ ...style, strokeDasharray: '6 4', stroke: '#94a3b8' }}
      />
      <EdgeLabelRenderer>
        <Tooltip label={tooltipLabel ?? ''} withArrow position="top" disabled={!tooltipLabel}>
          <div
            style={{
              position: 'absolute',
              transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
              pointerEvents: 'all',
              background: '#ffffff',
              borderRadius: 999,
              border: '1px solid #94a3b8',
              padding: 4,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'not-allowed',
            }}
          >
            <IconLock size={12} color="#64748b" />
          </div>
        </Tooltip>
      </EdgeLabelRenderer>
    </>
  );
};

export default LockedAssociationEdge;
