import { useParams } from 'react-router';
import { useQueryTopic } from '../../api/topic.ts';
import {
    ReactFlow,
    Controls,
    Background,
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
    Text,
    Stack,
} from '@mantine/core';
import { useMemo, useState } from 'react';
import type { Topic } from '../../schemas/topic.ts';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import TopicNode from '../../components/graph-view/topic-node.tsx';
import ContentNode from '../../components/graph-view/content-node.tsx';
import { useTranslation } from 'react-i18next';
import ContentSidebarContent from '../../components/graph-view/sidebar/content-sidebar-content.tsx';
import TopicSidebarContent from '../../components/graph-view/sidebar/topic-sidebar-content.tsx';


const nodeTypes = {
    topic: TopicNode,
    content: ContentNode,
};

// PLEASE DO NOT CHANGE THIS LOGIC !!!!
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

const TopicDetailsPage = () => {

    const { t } = useTranslation();

    const { topicId } = useParams<{ topicId: string }>();
    const { data: topic, isLoading } = useQueryTopic(topicId || '', false);

    const [selectedElement, setSelectedElement] = useState<AnyContentElementDto | Omit<Topic, 'relatedTopics'> | null>(null);


    const { nodes, edges } = useMemo<{ nodes: Node[], edges: Edge[] }>(
        () => {
            const nodes: Node[] = [];
            const edges: Edge[] = [];
            if (topic) {
                nodes.push({
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

                sortedContents.forEach((content, index) => {
                    const id = `content-${index}`;
                    const angleStep = Math.PI / 6;
                    const angle = (index * angleStep) - Math.PI / 2;
                    const x = 400 + radius * Math.cos(angle);
                    const y = 300 + radius * Math.sin(angle);

                    nodes.push({
                        id,
                        type: 'content',
                        position: { x, y },
                        data: content,
                    });

                    const sourceHandle = getHandleForAngle(angle);
                    const targetHandle = getOppositeHandle(sourceHandle);

                    edges.push({
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
            }
            return { nodes, edges };
        },
        [topic]
    )

    const onNodeClick = (_event: any, node: Node) => {
        if (node.id === 'topic-root') {
            setSelectedElement(topic || null);
        } else if (node.id.startsWith('content-')) {
            setSelectedElement(node.data as AnyContentElementDto);
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

    const isTopic = selectedElement && 'teaser' in selectedElement;

    return (
        <Layout>
            <div style={{ display: 'flex', width: '100%', height: 'calc(100vh - 100px)', overflow: 'hidden' }}>
                <div style={{ flex: 1, position: 'relative', backgroundColor: '#f8f9fa' }}>
                    <ReactFlow
                        nodes={nodes}
                        edges={edges}
                        onNodeClick={onNodeClick}
                        nodeTypes={nodeTypes}
                        fitView
                        nodesDraggable={false}
                        nodesConnectable={false}
                        fitViewOptions={{ padding: 0.5 }}
                    >
                        <Controls showInteractive={false} />
                        <Background color="#dee2e6" gap={16} />
                    </ReactFlow>
                </div>

                <Paper shadow="md" p="xl" style={{ width: 400, borderLeft: '1px solid #e9ecef', overflowY: 'auto' }}>
                    {selectedElement ? (
                        <Stack gap="md">
                            {isTopic ? (
                                <TopicSidebarContent selectedElement={selectedElement as Topic} />
                            ) : (
                                <ContentSidebarContent selectedElement={selectedElement as AnyContentElementDto} />
                            )}
                        </Stack>
                    ) : (
                        <Text c="dimmed">{t('common.clickOnANodeToSeeContent')}</Text>
                    )}
                </Paper>
            </div>
        </Layout >
    );
};

export default TopicDetailsPage;