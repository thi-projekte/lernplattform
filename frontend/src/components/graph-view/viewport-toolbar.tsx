import { ActionIcon, Paper, Stack, Tooltip } from '@mantine/core';
import { IconLock, IconLockOpen2, IconMaximize, IconMinus, IconPlus } from '@tabler/icons-react';
import { Panel, useReactFlow } from '@xyflow/react';

interface ViewportToolbarProps {
  fitViewPadding: number;
  fitViewMaxZoom?: number;
  viewportLocked: boolean;
  onToggleViewportLock?: () => void;
}

const ViewportToolbar = ({
  fitViewPadding,
  fitViewMaxZoom,
  viewportLocked,
  onToggleViewportLock,
}: ViewportToolbarProps) => {
  const { zoomIn, zoomOut, fitView } = useReactFlow();

  return (
    <Panel position="bottom-left" style={{ marginBottom: 18, marginLeft: 18 }}>
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
              size={36}
              aria-label="Zoom in"
              onClick={() => zoomIn({ duration: 180 })}
            >
              <IconPlus size={20} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Zoom out" position="right">
            <ActionIcon
              variant="subtle"
              color="gray"
              radius={0}
              size={36}
              aria-label="Zoom out"
              onClick={() => zoomOut({ duration: 180 })}
            >
              <IconMinus size={20} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Fit view" position="right">
            <ActionIcon
              variant="subtle"
              color="gray"
              radius={0}
              size={36}
              aria-label="Fit view"
              onClick={() =>
                fitView({ padding: fitViewPadding, maxZoom: fitViewMaxZoom, duration: 220 })
              }
            >
              <IconMaximize size={18} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label={viewportLocked ? 'Unlock graph' : 'Lock graph'} position="right">
            <ActionIcon
              variant={viewportLocked ? 'filled' : 'subtle'}
              color={viewportLocked ? 'dark' : 'gray'}
              radius={0}
              size={36}
              aria-label={viewportLocked ? 'Unlock graph' : 'Lock graph'}
              onClick={onToggleViewportLock}
            >
              {viewportLocked ? (
                <IconLock size={16} stroke={2} />
              ) : (
                <IconLockOpen2 size={16} stroke={2} />
              )}
            </ActionIcon>
          </Tooltip>
        </Stack>
      </Paper>
    </Panel>
  );
};

export default ViewportToolbar;
