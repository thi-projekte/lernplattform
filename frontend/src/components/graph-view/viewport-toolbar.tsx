import { ActionIcon, Divider, Paper, Stack, Tooltip } from '@mantine/core';
import {
  IconBinaryTree2,
  IconGrain,
  IconLock,
  IconLockOpen2,
  IconMaximize,
  IconMinus,
  IconPlus,
} from '@tabler/icons-react';
import { Panel, useReactFlow } from '@xyflow/react';
import type { GraphLayoutMode } from './topic-graph.tsx';

interface ViewportToolbarProps {
  fitViewPadding: number;
  fitViewMaxZoom?: number;
  viewportLocked: boolean;
  onToggleViewportLock?: () => void;
  layoutMode?: GraphLayoutMode;
  onChangeLayoutMode?: (mode: GraphLayoutMode) => void;
}

const toolbarActionIconProps = {
  variant: 'subtle',
  radius: 0,
  size: 36,
  style: {
    ['--ai-color' as string]: '#495057',
    ['--ai-hover' as string]: 'rgba(0, 0, 0, 0.06)',
    ['--ai-hover-color' as string]: '#495057',
  },
} as const;

const ViewportToolbar = ({
  fitViewPadding,
  fitViewMaxZoom,
  viewportLocked,
  onToggleViewportLock,
  layoutMode,
  onChangeLayoutMode,
}: ViewportToolbarProps) => {
  const { zoomIn, zoomOut, fitView } = useReactFlow();

  return (
    <Panel position="bottom-left" style={{ marginBottom: 18, marginLeft: 18 }}>
      <Paper
        withBorder
        radius="md"
        shadow="sm"
        style={{
          overflow: 'hidden',
          background: 'rgba(255, 255, 255, 0.94)',
          borderColor: '#dee2e6',
        }}
      >
        <Stack gap={0}>
          {onChangeLayoutMode && (
            <>
              <Tooltip label="Tree layout" position="right">
                <ActionIcon
                  {...toolbarActionIconProps}
                  variant={layoutMode === 'tree' ? 'filled' : 'subtle'}
                  style={{
                    ...toolbarActionIconProps.style,
                    ...(layoutMode === 'tree' && {
                      ['--ai-bg' as string]: '#495057',
                      ['--ai-hover' as string]: '#5c6370',
                      ['--ai-color' as string]: '#ffffff',
                      ['--ai-hover-color' as string]: '#ffffff',
                    }),
                  }}
                  aria-label="Tree layout"
                  onClick={() => onChangeLayoutMode('tree')}
                >
                  <IconBinaryTree2 size={18} stroke={2} />
                </ActionIcon>
              </Tooltip>

              <Tooltip label="Force layout" position="right">
                <ActionIcon
                  {...toolbarActionIconProps}
                  variant={layoutMode === 'force' ? 'filled' : 'subtle'}
                  style={{
                    ...toolbarActionIconProps.style,
                    ...(layoutMode === 'force' && {
                      ['--ai-bg' as string]: '#495057',
                      ['--ai-hover' as string]: '#5c6370',
                      ['--ai-color' as string]: '#ffffff',
                      ['--ai-hover-color' as string]: '#ffffff',
                    }),
                  }}
                  aria-label="Force layout"
                  onClick={() => onChangeLayoutMode('force')}
                >
                  <IconGrain size={18} stroke={2} />
                </ActionIcon>
              </Tooltip>

              <Divider />
            </>
          )}

          <Tooltip label="Zoom in" position="right">
            <ActionIcon
              {...toolbarActionIconProps}
              aria-label="Zoom in"
              onClick={() => zoomIn({ duration: 180 })}
            >
              <IconPlus size={20} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Zoom out" position="right">
            <ActionIcon
              {...toolbarActionIconProps}
              aria-label="Zoom out"
              onClick={() => zoomOut({ duration: 180 })}
            >
              <IconMinus size={20} stroke={2} />
            </ActionIcon>
          </Tooltip>

          <Tooltip label="Fit view" position="right">
            <ActionIcon
              {...toolbarActionIconProps}
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
              {...toolbarActionIconProps}
              variant={viewportLocked ? 'filled' : 'subtle'}
              style={{
                ...toolbarActionIconProps.style,
                ...(viewportLocked && {
                  ['--ai-bg' as string]: '#495057',
                  ['--ai-hover' as string]: '#5c6370',
                  ['--ai-color' as string]: '#ffffff',
                  ['--ai-hover-color' as string]: '#ffffff',
                }),
              }}
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
