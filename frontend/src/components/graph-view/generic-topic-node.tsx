import { Handle, Position, type Node, type NodeProps } from '@xyflow/react';
import { ActionIcon, Badge, Button, Group, Paper, Stack, Text } from '@mantine/core';
import { IconEye } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';

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
  payload?: {
    id?: string;
    categories?: NodeCategory[];
  };
}

type GenericTopicNodeProps = NodeProps<Node<GenericTopicNodeData>>;

const GenericTopicNode = ({ data, selected }: GenericTopicNodeProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const categories = data.categories ?? data.payload?.categories ?? [];
  const singleCategoryColor = categories.length === 1 ? `#${categories[0].color}` : undefined;
  const accentColor = singleCategoryColor ?? '#8b5cf6';
  const categoryLabels = categories.slice(0, 3);
  const topicId = data.payload?.id;

  const isBuilderMode = data.kind === 'topic';
  const isForeign = isBuilderMode && data.isOwned === false;

  const baseBackground = isBuilderMode
    ? isForeign
      ? 'linear-gradient(135deg, #f4f9fe 0%, #e8f1f9 100%)'
      : 'linear-gradient(135deg, #fffbf2 0%, #fff5e0 100%)'
    : 'linear-gradient(135deg, #e7f2ff 0%, #fff9e6 100%)';

  const selectedBackground = isBuilderMode
    ? isForeign
      ? 'linear-gradient(135deg, #e6f0fa 0%, #d4e4f3 100%)'
      : 'linear-gradient(135deg, #fff5e0 0%, #ffecc4 100%)'
    : `linear-gradient(135deg, color-mix(in srgb, ${accentColor} 10%, #d4e7fc) 0%, color-mix(in srgb, ${accentColor} 10%, #fff2b8) 100%)`;

  return (
    <Paper
      radius="lg"
      shadow={selected ? 'xl' : 'sm'}
      p="md"
      style={{
        minWidth: 220,
        maxWidth: 280,
        background: selected ? selectedBackground : baseBackground,
        border: selected ? `2px solid ${accentColor}` : '2px solid transparent',
        outline: selected ? `4px solid color-mix(in srgb, ${accentColor} 22%, transparent)` : 'none',
        outlineOffset: selected ? '2px' : '0',
        transition:
          'box-shadow 150ms ease, background 150ms ease, border-color 150ms ease, outline 150ms ease',
      }}
    >
      <Handle type="target" position={Position.Top} id="top" style={{ opacity: 0 }} />
      <Handle type="target" position={Position.Right} id="right" style={{ opacity: 0 }} />
      <Handle type="target" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
      <Handle type="target" position={Position.Left} id="left" style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Top} id="top" style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Right} id="right" style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Left} id="left" style={{ opacity: 0 }} />

      <Stack gap="xs">
        <Group gap="sm" align="flex-start" wrap="nowrap">
          <div
            style={{
              width: 18,
              height: 18,
              borderRadius: '50%',
              background: accentColor,
              flexShrink: 0,
              marginTop: 3,
              boxShadow: '0 2px 6px rgba(15, 23, 42, 0.18)',
            }}
          />
          <Stack gap={6} style={{ flex: 1, minWidth: 0 }}>
            <Text fw={600} size="sm" style={{ lineHeight: 1.3 }}>
              {data.title}
            </Text>
            {categoryLabels.length > 0 && (
              <Group gap={4} wrap="wrap">
                {categoryLabels.map((category) => {
                  const categoryColor = `#${category.color}`;
                  return (
                    <Badge
                      key={category.id}
                      radius="xl"
                      variant="light"
                      size="xs"
                      style={{
                        color: categoryColor,
                        background: `color-mix(in srgb, ${categoryColor} 14%, white)`,
                        textTransform: 'none',
                        fontWeight: 600,
                      }}
                    >
                      {category.title}
                    </Badge>
                  );
                })}
              </Group>
            )}
          </Stack>
        </Group>

        {selected &&
          topicId &&
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
      </Stack>
    </Paper>
  );
};

export default GenericTopicNode;
