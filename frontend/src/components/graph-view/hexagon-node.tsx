import { Handle, Position, type NodeProps } from "@xyflow/react";
import HexagonIcon from "../icons/hexagon-icon";
import { Text, type TextProps } from "@mantine/core";
import type { Icon } from "@tabler/icons-react";

interface HexagonNodeProps extends NodeProps {
    Icon: Icon;
    color: string;
    iconSize: number;
    label: string;
    labelSize: TextProps['size'];
    labelFontWeight?: number;
    subLabel?: string;

}


const HexagonNode = ({ Icon, color, iconSize, label, labelFontWeight, subLabel, labelSize }: HexagonNodeProps) => (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: 120 }}>
        <Handle type="target" position={Position.Top} id="top" style={{ opacity: 0 }} />
        <Handle type="target" position={Position.Right} id="right" style={{ opacity: 0 }} />
        <Handle type="target" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
        <Handle type="target" position={Position.Left} id="left" style={{ opacity: 0 }} />

        <Handle type="source" position={Position.Top} id="top" style={{ opacity: 0 }} />
        <Handle type="source" position={Position.Right} id="right" style={{ opacity: 0 }} />
        <Handle type="source" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
        <Handle type="source" position={Position.Left} id="left" style={{ opacity: 0 }} />

        <HexagonIcon color={color} size={iconSize * 1.2}>
            <Icon size={iconSize} />
        </HexagonIcon>

        <Text fw={labelFontWeight} size={labelSize ?? "sm"} mt="sm" ta="center">
            {label}
        </Text>
        {subLabel && (
            <Text size="xs" c="dimmed" ta="center">
                {subLabel}
            </Text>
        )}
    </div>
);

export default HexagonNode;