import { useParams, useNavigate } from 'react-router';
import { useQueryTopic } from '../../api/topic.ts';
import {
    ReactFlow,
    Controls,
    Background,
    useNodesState,
    useEdgesState,
    type Node,
    type Edge,
    MarkerType,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Layout } from '../../components/layout.tsx';
import {
    Loader,
    Center,
    Paper,
    Title,
    Text,
    Button,
    Badge,
    Group,
    Stack,
} from '@mantine/core';
import {
    IconEdit,
} from '@tabler/icons-react';
import { useEffect, useState } from 'react';
import type { Topic } from '../../schemas/topic.ts';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import TopicNode from '../../components/graph-view/topic-node.tsx';
import ContentNode from '../../components/graph-view/content-node.tsx';


const nodeTypes = {
    topic: TopicNode,
    content: ContentNode,
};



const TopicDetailsPage = () => {
    const { topicId } = useParams<{ topicId: string }>();
    const navigate = useNavigate();
    const { data: topic, isLoading, error } = useQueryTopic(topicId || '', false);

    const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
    const [selectedElement, setSelectedElement] = useState<AnyContentElementDto | Omit<Topic, 'relatedTopics'> | null>(null);

    useEffect(() => {
        if (topic) {
            setSelectedElement(topic);
            const newNodes: Node[] = [];
            const newEdges: Edge[] = [];

            newNodes.push({
                id: 'topic-root',
                type: 'topic',
                position: { x: 400, y: 300 },
                data: topic,
            });

            const radius = 300;

            const sortedContents = [...(topic.contentElements || [])].sort((a, b) => {
                const rankA = a.rank ?? Infinity;
                const rankB = b.rank ?? Infinity;
                return rankA - rankB;
            });

            const getHandleForAngle = (angle: number) => {
                const a = ((angle % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
                if (a >= (7 * Math.PI) / 4 || a < Math.PI / 4) return 'right';
                if (a >= Math.PI / 4 && a < (3 * Math.PI) / 4) return 'bottom';
                if (a >= (3 * Math.PI) / 4 && a < (5 * Math.PI) / 4) return 'left';
                return 'top';
            };

            const getOppositeHandle = (handle: string) => {
                switch (handle) {
                    case 'right': return 'left';
                    case 'left': return 'right';
                    case 'top': return 'bottom';
                    case 'bottom': return 'top';
                    default: return 'left';
                }
            };

            sortedContents.forEach((content, index) => {
                const id = `content-${index}`;
                // Fixed step of 30 degrees (1 hour on the clock)
                const angleStep = Math.PI / 6;
                const angle = (index * angleStep) - Math.PI / 2;
                const x = 400 + radius * Math.cos(angle);
                const y = 300 + radius * Math.sin(angle);

                newNodes.push({
                    id,
                    type: 'content',
                    position: { x, y },
                    data: content,
                });

                const sourceHandle = getHandleForAngle(angle);
                const targetHandle = getOppositeHandle(sourceHandle);

                newEdges.push({
                    id: `edge-root-${id}`,
                    source: 'topic-root',
                    target: id,
                    sourceHandle,
                    targetHandle,
                    animated: true,
                    style: { stroke: '#adb5bd', strokeWidth: 2 },
                    markerEnd: { type: MarkerType.ArrowClosed, color: '#adb5bd' },
                });
            });

            setNodes(newNodes);
            setEdges(newEdges);
        }
    }, [topic, setNodes, setEdges]);

    const onNodeClick = (_event: any, node: Node) => {
        if (node.id === 'topic-root') {
            setSelectedElement(topic || null);
        } else if (node.data.contentData) {
            setSelectedElement(node.data.contentData as AnyContentElementDto);
        }
    };

    if (isLoading) {
        return (
            <Layout>
                <Center h="100vh">
                    <Loader />
                </Center>
            </Layout>
        );
    }

    if (error || !topic) {
        return (
            <Layout>
                <Center h="100vh">
                    <Text c="red">Fehler beim Laden des Themas oder Thema nicht gefunden.</Text>
                </Center>
            </Layout>
        );
    }

    const isTopic = selectedElement && 'teaser' in selectedElement;

    return (
        <Layout>
            <div style={{ display: 'flex', width: '100%', height: 'calc(100vh - 60px)', overflow: 'hidden' }}>
                <div style={{ flex: 1, position: 'relative', backgroundColor: '#f8f9fa' }}>
                    <ReactFlow
                        nodes={nodes}
                        edges={edges}
                        onNodesChange={onNodesChange}
                        onEdgesChange={onEdgesChange}
                        onNodeClick={onNodeClick}
                        nodeTypes={nodeTypes}
                        fitView
                    >
                        <Controls />
                        <Background color="#dee2e6" gap={16} />
                    </ReactFlow>
                </div>

                <Paper shadow="md" p="xl" style={{ width: 400, borderLeft: '1px solid #e9ecef', overflowY: 'auto' }}>
                    {selectedElement ? (
                        <Stack gap="md">
                            <Title order={3}>{selectedElement.title}</Title>

                            {isTopic ? (
                                <>
                                    <Group>
                                        {(selectedElement as Topic).categories?.map((cat, i) => (
                                            <Badge key={i} color={cat.color || 'blue'}>
                                                {cat.title}
                                            </Badge>
                                        ))}
                                    </Group>
                                    <Text size="sm" c="dimmed">
                                        Lernzeit: {(selectedElement as Topic).estimatedLearningDuration} Minuten
                                    </Text>
                                    <Text size="sm">{(selectedElement as Topic).teaser}</Text>

                                    <Button
                                        leftSection={<IconEdit size={16} />}
                                        variant="light"
                                        color="blue"
                                        fullWidth
                                        mt="xl"
                                        onClick={() => navigate(`/builder-mode/topics/${topicId}/edit`)}
                                    >
                                        Detailseite / Bearbeiten
                                    </Button>
                                </>
                            ) : (
                                <>
                                    <Badge color="teal">{(selectedElement as AnyContentElementDto).type}</Badge>
                                    <Text size="sm" c="dimmed">
                                        Dies ist ein Inhaltselement vom Typ {(selectedElement as AnyContentElementDto).type}.
                                    </Text>
                                    {'originalFileName' in selectedElement && (
                                        <Text size="sm">
                                            <b>Dateiname:</b> {(selectedElement as any).originalFileName}
                                        </Text>
                                    )}
                                    {'uri' in selectedElement && (
                                        <Text size="sm" style={{ wordBreak: 'break-all' }}>
                                            <b>Link:</b>{' '}
                                            <a href={(selectedElement as any).uri} target="_blank" rel="noreferrer">
                                                {(selectedElement as any).uri}
                                            </a>
                                        </Text>
                                    )}
                                </>
                            )}
                        </Stack>
                    ) : (
                        <Text c="dimmed">Klicke auf einen Node, um Details zu sehen.</Text>
                    )}
                </Paper>
            </div>
        </Layout>
    );
};

export default TopicDetailsPage;