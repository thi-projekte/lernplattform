import { Handle, Position, type NodeProps } from '@xyflow/react';
import { Badge, Text } from '@mantine/core';
import type { SkillTreeNode } from './skill-tree.types.ts';

type SkillTreeNodeProps = NodeProps<SkillTreeNode>;

const SkillTreeNodeComponent = ({ data, selected }: SkillTreeNodeProps) => {
  const accentColor = data.categories[0] ? `#${data.categories[0].color}` : '#4a90e2';
  const categoryTitle = data.categories[0]?.title;

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
          background: `color-mix(in srgb, ${accentColor} 14%, white)`,
          border: `1px solid color-mix(in srgb, ${accentColor} 28%, white)`,
          boxShadow: selected
            ? `0 0 0 10px color-mix(in srgb, ${accentColor} 18%, transparent)`
            : '0 10px 24px rgba(15, 23, 42, 0.08)',
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

      {categoryTitle && (
        <Badge
          mt={8}
          radius="xl"
          variant="light"
          style={{
            color: accentColor,
            background: `color-mix(in srgb, ${accentColor} 14%, white)`,
            border: `1px solid color-mix(in srgb, ${accentColor} 20%, white)`,
            textTransform: 'none',
            fontWeight: 600,
          }}
        >
          {categoryTitle}
        </Badge>
      )}
    </div>
  );
};

export default SkillTreeNodeComponent;
