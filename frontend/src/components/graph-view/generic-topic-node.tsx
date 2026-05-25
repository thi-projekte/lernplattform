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
  const singleCategoryColor = categories.length === 1 ? `#${categories[0].color}` : undefined;
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

  if (!selected) {
    return (
      <div
        style={{
          width: 220,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 8,
          cursor: 'pointer',
        }}
      >
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            background: collapsedColor,
            boxShadow: isProgressCompleted
              ? `0 2px 6px rgba(0,0,0,0.12)`
              : `0 0 0 18px color-mix(in srgb, ${accentColor} 20%, transparent), 0 2px 6px rgba(0,0,0,0.1)`,
            position: 'relative',
            transition: 'box-shadow 0.25s ease, background 0.25s ease',
          }}
        >
          {handles}
        </div>
        <Text
          fw={isProgressCompleted ? 400 : 600}
          size="sm"
          ta="center"
          lineClamp={2}
          style={{ maxWidth: 150, lineHeight: 1.3 }}
        >
          {data.title}
        </Text>
      </div>
    );
  }

  return (
    <div
      style={{
        width: 220,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 0,
      }}
    >
      <Paper
        radius="lg"
        shadow="xl"
        p="md"
        style={{
          width: '100%',
          background: selectedBackground,
          border: `2px solid ${accentColor}`,
          outline: `4px solid color-mix(in srgb, ${accentColor} 22%, transparent)`,
          outlineOffset: '2px',
          animation: 'cardEntrance 0.26s cubic-bezier(0.34, 1.4, 0.64, 1) both',
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

      {/* Connector line */}
      <div
        style={{
          width: 2,
          height: 10,
          background: `color-mix(in srgb, ${accentColor} 55%, transparent)`,
          flexShrink: 0,
          transition: 'background 0.25s ease',
        }}
      />

      {/* Pin circle at bottom */}
      <div
        style={{
          width: 32,
          height: 32,
          borderRadius: '50%',
          background: accentColor,
          flexShrink: 0,
          boxShadow: `0 0 0 6px color-mix(in srgb, ${accentColor} 28%, transparent), 0 2px 8px rgba(0,0,0,0.18)`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
          transition: 'box-shadow 0.25s ease, background 0.25s ease',
        }}
      >
        {handles}
      </div>
    </div>
  );
};

export default GenericTopicNode;
