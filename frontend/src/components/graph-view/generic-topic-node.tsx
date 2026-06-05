import { type CSSProperties, useEffect } from 'react';
import { Handle, Position, useUpdateNodeInternals, type Node, type NodeProps } from '@xyflow/react';
import {
  ActionIcon,
  Button,
  Group,
  Paper,
  Progress,
  Stack,
  Text,
  useMantineTheme,
} from '@mantine/core';
import { IconEye, IconEyeOff, IconLink } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';
import CategoryBadge from '../category-badge.tsx';
import type { TopicLearnProgressDto } from '../../schemas/learn-progress.ts';

const DETAIL_BUTTON_VARIANT: 'icon-text' | 'icon-only' = 'icon-text';

interface NodeCategory {
  id: string;
  color: string;
  title: string;
}

interface GenericTopicNodeData extends Record<string, unknown> {
  kind?: 'skill-topic' | 'topic';
  title: string;
  categories?: NodeCategory[];
  isOwned?: boolean;
  onExpand?: () => void;
  isExpanded?: boolean;
  onHide?: () => void;
  payload?: {
    id?: string;
    categories?: NodeCategory[];
    learnProgress?: TopicLearnProgressDto | null;
  };
}

type GenericTopicNodeProps = NodeProps<Node<GenericTopicNodeData>>;

const GenericTopicNode = ({ id, data, selected }: GenericTopicNodeProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const theme = useMantineTheme();
  const updateNodeInternals = useUpdateNodeInternals();

  const categories = data.categories ?? data.payload?.categories ?? [];
  const singleCategoryColor =
    categories.length === 1 && categories[0].color ? `#${categories[0].color}` : undefined;
  const accentColor = singleCategoryColor ?? '#00aaff';
  const categoryLabels = categories.slice(0, 3);
  const topicId = data.payload?.id;

  const isBuilderMode = data.kind === 'topic';
  const isForeign = isBuilderMode && data.isOwned === false;
  const learnProgress = data.payload?.learnProgress;
  const isProgressCompleted = !!learnProgress?.completed;
  const progressPercent = isProgressCompleted
    ? 100
    : Math.round((learnProgress?.percentageCompleted ?? 0) * 100);
  const handleStyle: CSSProperties = isBuilderMode
    ? { opacity: 0 }
    : { opacity: 0, pointerEvents: 'none' };

  const selectedBackground = isBuilderMode
    ? isForeign
      ? theme.other.nodeForeignSelectedBg
      : theme.other.nodeOwnSelectedBg
    : `linear-gradient(135deg, color-mix(in srgb, ${accentColor} 10%, #d4e7fc) 0%, color-mix(in srgb, ${accentColor} 10%, #fff2b8) 100%)`;

  useEffect(() => {
    updateNodeInternals(id);
  }, [selected, id, updateNodeInternals]);

  const handles = (
    <>
      <Handle type="target" position={Position.Top} id="top" style={handleStyle} />
      <Handle type="target" position={Position.Right} id="right" style={handleStyle} />
      <Handle type="target" position={Position.Bottom} id="bottom" style={handleStyle} />
      <Handle type="target" position={Position.Left} id="left" style={handleStyle} />
      <Handle type="source" position={Position.Top} id="top" style={handleStyle} />
      <Handle type="source" position={Position.Right} id="right" style={handleStyle} />
      <Handle type="source" position={Position.Bottom} id="bottom" style={handleStyle} />
      <Handle type="source" position={Position.Left} id="left" style={handleStyle} />
    </>
  );

  const collapsedColor = isProgressCompleted ? '#40c057' : accentColor;

  const dotColor = selected ? accentColor : collapsedColor;
  const dotShadow =
    selected || !isProgressCompleted
      ? `0 0 0 ${selected ? 6 : 18}px color-mix(in srgb, ${accentColor} ${selected ? 28 : 20}%, transparent), 0 2px ${selected ? 8 : 6}px rgba(0,0,0,${selected ? 0.18 : 0.1})`
      : '0 2px 6px rgba(0,0,0,0.12)';

  return (
    <div
      style={{
        width: 150,
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 8,
        cursor: 'pointer',
      }}
    >
      {/* Card popup ABOVE the dot — rendered absolutely so the dot's position
          on screen never changes when the card opens/closes. */}
      {selected && (
        <div
          style={{
            position: 'absolute',
            bottom: 'calc(100% + 12px)',
            left: '50%',
            transform: 'translateX(-50%)',
            width: 240,
          }}
        >
          <Paper
            radius="lg"
            shadow="xl"
            p="md"
            onMouseDown={(event) => event.stopPropagation()}
            onClick={(event) => event.stopPropagation()}
            style={{
              width: '100%',
              background: selectedBackground,
              border: `2px solid ${accentColor}`,
              outline: `4px solid color-mix(in srgb, ${accentColor} 22%, transparent)`,
              outlineOffset: '2px',
              transformOrigin: 'bottom center',
              animation: 'cardEntrance 0.18s ease-out both',
            }}
          >
            <Stack gap="xs">
              <Stack gap={6}>
                <Text fw={600} size="sm" style={{ lineHeight: 1.3 }}>
                  {data.title}
                </Text>
                {categoryLabels.length > 0 && (
                  <Group gap={4} wrap="wrap">
                    {categoryLabels.map((category) => (
                      <CategoryBadge
                        key={category.id}
                        title={category.title}
                        color={category.color}
                        size="xs"
                      />
                    ))}
                  </Group>
                )}
              </Stack>

              {topicId &&
                (DETAIL_BUTTON_VARIANT === 'icon-text' ? (
                  <Button
                    leftSection={<IconEye size={14} />}
                    variant="light"
                    color="blue"
                    size="xs"
                    fullWidth
                    onClick={(event) => {
                      event.stopPropagation();
                      navigate(`/topics/${topicId}/details`);
                    }}
                  >
                    {t('journey.openDetails')}
                  </Button>
                ) : (
                  <Group justify="flex-end">
                    <ActionIcon
                      variant="light"
                      color="blue"
                      size="md"
                      onClick={(event) => {
                        event.stopPropagation();
                        navigate(`/topics/${topicId}/details`);
                      }}
                      aria-label={t('journey.openDetails')}
                    >
                      <IconEye size={16} />
                    </ActionIcon>
                  </Group>
                ))}

              {data.onExpand && !data.isExpanded && (
                <Button
                  leftSection={<IconLink size={14} />}
                  variant="light"
                  color="gray"
                  size="xs"
                  fullWidth
                  onClick={(event) => {
                    event.stopPropagation();
                    data.onExpand?.();
                  }}
                >
                  {t('journey.expandNeighbors')}
                </Button>
              )}

              {data.onHide && (
                <Button
                  leftSection={<IconEyeOff size={14} />}
                  variant="subtle"
                  color="gray"
                  size="xs"
                  fullWidth
                  className="hide-node-btn"
                  onClick={(event) => {
                    event.stopPropagation();
                    data.onHide?.();
                  }}
                >
                  {t('journey.hideNode')}
                </Button>
              )}

              {learnProgress && (
                <Group gap="xs" align="center" wrap="nowrap">
                  <Text
                    size="xs"
                    fw={700}
                    c={isProgressCompleted ? 'green.7' : 'blue.7'}
                    style={{ minWidth: 36, textAlign: 'left' }}
                  >
                    {progressPercent}%
                  </Text>
                  <Progress
                    value={progressPercent}
                    color={isProgressCompleted ? 'green' : 'blue'}
                    size="xs"
                    radius="xl"
                    style={{ flex: 1 }}
                  />
                </Group>
              )}
            </Stack>
          </Paper>
          {/* Connector line from card down to the dot */}
          <div
            style={{
              width: 2,
              height: 12,
              background: `color-mix(in srgb, ${accentColor} 55%, transparent)`,
              margin: '0 auto',
            }}
          />
        </div>
      )}

      {/* Dot — always at the same position whether collapsed or selected */}
      <div
        className="mynd-drag-handle"
        style={{
          width: 32,
          height: 32,
          borderRadius: '50%',
          background: dotColor,
          flexShrink: 0,
          boxShadow: dotShadow,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
          cursor: 'grab',
          transition: 'box-shadow 0.25s ease, background 0.25s ease',
        }}
      >
        {handles}
      </div>

      {/* Title below dot only when collapsed (when selected, title is in the card) */}
      {!selected && (
        <Text
          fw={isProgressCompleted ? 400 : 600}
          size="sm"
          ta="center"
          lineClamp={2}
          style={{ maxWidth: 150, lineHeight: 1.3 }}
        >
          {data.title}
        </Text>
      )}
    </div>
  );
};

export default GenericTopicNode;
