import { Handle, Position, type Node, type NodeProps } from '@xyflow/react';
import { Text, Group, Avatar } from '@mantine/core';
import type { GraphTopicNodeData } from './topic-graph.types';
import { useQueryProfilePicture } from '../../api/profile-picture.ts';

type TopicNodeProps = NodeProps<Node<GraphTopicNodeData>>;

const TopicNode = ({ data, selected }: TopicNodeProps) => {
    const accentColor = data.isRoot
        ? '#e03131'
        : data.isIsolated || data.isOwned === false
            ? '#228be6'
            : '#f08c00';
    const backgroundColor = data.isRoot
      ? 'rgba(224, 49, 49, 0.10)'
      : data.isIsolated || data.isOwned === false
        ? 'rgba(34, 139, 230, 0.10)'
        : 'rgba(240, 140, 0, 0.10)';

    // Ersteller-ID entweder direkt aus data oder aus dem zugrundeliegenden Payload holen
    const creatorId = data.creatorId || (data.payload as { creatorId?: string })?.creatorId;
    const { data: profilePicture } = useQueryProfilePicture(creatorId);

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
                    width: '100%',
                    minHeight: 58,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    padding: '0 18px',
                    borderRadius: 999,
                    background: backgroundColor,
                    border: `2px solid ${accentColor}`,
                    boxShadow: selected
                        ? `0 0 0 6px color-mix(in srgb, ${accentColor} 14%, transparent)`
                        : '0 8px 18px rgba(15, 23, 42, 0.08)',
                    color: '#1f2937',
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

                <span
                    style={{
                        width: 12,
                        height: 12,
                        minWidth: 12,
                        borderRadius: '50%',
                        background: accentColor,
                        boxShadow: `0 0 0 4px color-mix(in srgb, ${accentColor} 16%, transparent)`,
                    }}
                />

                <Text fw={700} size="md" style={{ lineHeight: 1.2, flex: 1, textWrap: 'balance' }}>
                    {data.title}
                </Text>
            </div>

            {data.creatorFullName && (
                <Group gap={6} mt={8} align="center" justify="center" wrap="nowrap">
                    <Avatar
                        src={profilePicture?.url}
                        size={16}
                        radius="xl"
                        name={data.creatorFullName}
                        color="initials"
                    />
                    <Text
                        size="xs"
                        c="dimmed"
                        style={{ lineHeight: 1.2, maxWidth: 160, textWrap: 'balance' }}
                    >
                        {data.creatorFullName}
                    </Text>
                </Group>
            )}
        </div>
    );
};

export default TopicNode;