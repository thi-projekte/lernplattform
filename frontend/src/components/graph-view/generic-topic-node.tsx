import { Handle, Position, type NodeProps } from '@xyflow/react';
import { ActionIcon, Badge, Button, Group, Paper, Stack, Text } from '@mantine/core';
import { IconEye } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';
import type { SkillTreeNode } from './skill-tree.types.ts';

const DETAIL_BUTTON_VARIANT: 'icon-text' | 'icon-only' = 'icon-text';

type GenericTopicNodeProps = NodeProps<SkillTreeNode>;

const GenericTopicNode = ({ data, selected }: GenericTopicNodeProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const singleCategoryColor =
    data.categories.length === 1 ? `#${data.categories[0].color}` : undefined;
  const accentColor = singleCategoryColor ?? '#8b5cf6';
  const categoryLabels = data.categories.slice(0, 3);

  return (
    <Paper
      radius="lg"
      shadow={selected ? 'lg' : 'sm'}
      p="md"
      style={{
        minWidth: 220,
        maxWidth: 280,
        background: selected
          ? `color-mix(in srgb, ${accentColor} 8%, white)`
          : 'white',
        transition: 'box-shadow 150ms ease, background 150ms ease',
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
          (DETAIL_BUTTON_VARIANT === 'icon-text' ? (
            <Button
              leftSection={<IconEye size={14} />}
              variant="light"
              color="blue"
              size="xs"
              fullWidth
              onClick={(event) => {
                event.stopPropagation();
                navigate(`/topics/${data.payload.id}/details`);
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
                  navigate(`/topics/${data.payload.id}/details`);
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
