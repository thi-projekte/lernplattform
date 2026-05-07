import { Handle, Position, type NodeProps } from '@xyflow/react';
import { Badge, Text } from '@mantine/core';
import type { SkillTreeNode } from './skill-tree.types.ts';

type SkillTreeNodeProps = NodeProps<SkillTreeNode>;

const SkillTreeNodeComponent = ({ data, selected }: SkillTreeNodeProps) => {
  const palette = data.isOwned
    ? {
        accent: '#8b5cf6',
        ring: 'rgba(139, 92, 246, 0.20)',
        background: 'rgba(139, 92, 246, 0.12)',
        border: 'rgba(139, 92, 246, 0.28)',
      }
    : data.role === 'root'
      ? {
          accent: '#2563eb',
          ring: 'rgba(37, 99, 235, 0.16)',
          background: 'rgba(37, 99, 235, 0.12)',
          border: 'rgba(37, 99, 235, 0.22)',
        }
      : data.role === 'disconnected'
        ? {
            accent: '#94a3b8',
            ring: 'rgba(148, 163, 184, 0.16)',
            background: 'rgba(148, 163, 184, 0.10)',
            border: 'rgba(148, 163, 184, 0.24)',
          }
        : {
            accent: '#0f766e',
            ring: 'rgba(15, 118, 110, 0.16)',
            background: 'rgba(15, 118, 110, 0.10)',
            border: 'rgba(15, 118, 110, 0.22)',
          };
  const accentColor = palette.accent;
  const categoryLabels = data.categories.slice(0, 3);

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        width: 220,
      }}
    >
      <div
        style={{
          position: 'relative',
          width: 82,
          height: 82,
          borderRadius: '50%',
          background: palette.background,
          border: `1px solid ${palette.border}`,
          boxShadow: selected ? `0 0 0 10px ${palette.ring}` : '0 10px 24px rgba(15, 23, 42, 0.08)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'box-shadow 150ms ease, transform 150ms ease',
        }}
      >
        <Handle type="target" position={Position.Top} id="top" style={{ opacity: 0, top: 0 }} />
        <Handle
          type="target"
          position={Position.Right}
          id="right"
          style={{ opacity: 0, right: 0 }}
        />
        <Handle
          type="target"
          position={Position.Bottom}
          id="bottom"
          style={{ opacity: 0, bottom: 0 }}
        />
        <Handle type="target" position={Position.Left} id="left" style={{ opacity: 0, left: 0 }} />

        <Handle type="source" position={Position.Top} id="top" style={{ opacity: 0, top: 0 }} />
        <Handle
          type="source"
          position={Position.Right}
          id="right"
          style={{ opacity: 0, right: 0 }}
        />
        <Handle
          type="source"
          position={Position.Bottom}
          id="bottom"
          style={{ opacity: 0, bottom: 0 }}
        />
        <Handle type="source" position={Position.Left} id="left" style={{ opacity: 0, left: 0 }} />

        <div
          style={{
            width: 46,
            height: 46,
            borderRadius: '50%',
            background: accentColor,
            border: '6px solid white',
            boxShadow: '0 10px 20px rgba(15, 23, 42, 0.14)',
          }}
        />
      </div>

      <Text
        mt={12}
        fw={700}
        size="md"
        ta="center"
        c={selected ? accentColor : '#2d3748'}
        style={{ lineHeight: 1.25, maxWidth: 204, textWrap: 'balance' }}
      >
        {data.title}
      </Text>

      {categoryLabels.length > 0 && (
        <div
          style={{
            marginTop: 8,
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'center',
            gap: 6,
            maxWidth: 210,
          }}
        >
          {categoryLabels.map((category) => {
            const categoryColor = `#${category.color}`;

            return (
              <Badge
                key={category.id}
                radius="xl"
                variant="light"
                style={{
                  color: categoryColor,
                  background: `color-mix(in srgb, ${categoryColor} 14%, white)`,
                  border: `1px solid color-mix(in srgb, ${categoryColor} 20%, white)`,
                  textTransform: 'none',
                  fontWeight: 600,
                }}
              >
                {category.title}
              </Badge>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SkillTreeNodeComponent;
