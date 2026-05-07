import { Handle, Position, type NodeProps } from '@xyflow/react';
import { Text } from '@mantine/core';
import type { SkillTreeNode } from './skill-tree.types.ts';

type SkillTreeNodeProps = NodeProps<SkillTreeNode>;

const SkillTreeNodeComponent = ({ data, selected }: SkillTreeNodeProps) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        width: 168,
      }}
    >
      <div
        style={{
          position: 'relative',
          width: 56,
          height: 56,
          borderRadius: '50%',
          background: 'rgba(74, 144, 226, 0.14)',
          boxShadow: selected ? '0 0 0 8px rgba(74, 144, 226, 0.18)' : 'none',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transition: 'box-shadow 150ms ease',
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
            width: 34,
            height: 34,
            borderRadius: '50%',
            background: '#4a90e2',
            border: '4px solid white',
            boxShadow: '0 6px 16px rgba(15, 23, 42, 0.10)',
          }}
        />
      </div>

        <Text
        mt={12}
        fw={700}
        size="sm"
        ta="center"
        c={selected ? '#1c5fd4' : '#2d3748'}
        style={{ lineHeight: 1.25, maxWidth: 156, textWrap: 'balance' }}
      >
        {data.title}
      </Text>
    </div>
  );
};

export default SkillTreeNodeComponent;
