import { Handle, Position } from '@xyflow/react';
import HexagonIcon from '../icons/hexagon-icon';
import { Text, type TextProps } from '@mantine/core';
import type { Icon } from '@tabler/icons-react';

interface HexagonNodeProps {
  Icon: Icon;
  color: string;
  size: number;
  label: string;
  labelSize: TextProps['size'];
  labelFontWeight?: number;
  subLabel?: string;
  selected?: boolean;
}

const HexagonNode = ({
  Icon,
  color,
  size,
  label,
  labelFontWeight,
  subLabel,
  labelSize,
  selected,
}: HexagonNodeProps) => (
  <div
    style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      width: Math.max(120, size * 1.5),
    }}
  >
    <div
      style={{
        position: 'relative',
        width: size,
        height: size,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Handle type="target" position={Position.Top} id="top" style={{ opacity: 0, top: 2 }} />
      <Handle
        type="target"
        position={Position.Right}
        id="right"
        style={{ opacity: 0, right: 2 }}
      />
      <Handle
        type="target"
        position={Position.Bottom}
        id="bottom"
        style={{ opacity: 0, bottom: 2 }}
      />
      <Handle type="target" position={Position.Left} id="left" style={{ opacity: 0, left: 2 }} />

      <Handle type="source" position={Position.Top} id="top" style={{ opacity: 0, top: 2 }} />
      <Handle
        type="source"
        position={Position.Right}
        id="right"
        style={{ opacity: 0, right: 2 }}
      />
      <Handle
        type="source"
        position={Position.Bottom}
        id="bottom"
        style={{ opacity: 0, bottom: 2 }}
      />
      <Handle type="source" position={Position.Left} id="left" style={{ opacity: 0, left: 2 }} />

      <HexagonIcon color={color} size={size} selected={selected}>
        <Icon size={size * 0.5} />
      </HexagonIcon>
    </div>

    <Text
      fw={labelFontWeight}
      size={labelSize ?? 'sm'}
      mt={4}
      ta="center"
      style={{ lineHeight: 1.2 }}
    >
      {label}
    </Text>
    {subLabel && (
      <Text size="xs" c="dimmed" ta="center" mt={2} style={{ lineHeight: 1.2 }}>
        {subLabel}
      </Text>
    )}
  </div>
);

export default HexagonNode;
