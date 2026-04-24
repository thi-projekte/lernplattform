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
  Handle,
  Position,
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
  IconBook,
  IconFileDescription,
  IconVideo,
  IconHeadphones,
  IconLink,
  IconPhoto,
} from '@tabler/icons-react';
import { useEffect, useState } from 'react';
import type { Topic } from '../../schemas/topic.ts';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';

const HexagonIcon = ({ color, children, size = 100 }: any) => {
  return (
    <div
      style={{
        position: 'relative',
        width: size,
        height: size * 1.15,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <svg
        width={size}
        height={size * 1.15}
        viewBox="0 0 100 115"
        style={{ position: 'absolute', top: 0, left: 0 }}
      >
        <defs>
          <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#000" floodOpacity="0.2" />
          </filter>
        </defs>
        <polygon
          points="50,5 95,31 95,84 50,110 5,84 5,31"
          fill={color}
          filter="url(#shadow)"
        />
      </svg>
      <div style={{ position: 'relative', zIndex: 1, color: 'white' }}>{children}</div>
    </div>
  );
};

const BaseNode = ({ data, color, icon: Icon, isTopic }: any) => (
  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: 120 }}>
    <Handle type="target" position={Position.Top} id="top" style={{ opacity: 0 }} />
    <Handle type="target" position={Position.Right} id="right" style={{ opacity: 0 }} />
    <Handle type="target" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
    <Handle type="target" position={Position.Left} id="left" style={{ opacity: 0 }} />

    <Handle type="source" position={Position.Top} id="top" style={{ opacity: 0 }} />
    <Handle type="source" position={Position.Right} id="right" style={{ opacity: 0 }} />
    <Handle type="source" position={Position.Bottom} id="bottom" style={{ opacity: 0 }} />
    <Handle type="source" position={Position.Left} id="left" style={{ opacity: 0 }} />

    <HexagonIcon color={color} size={isTopic ? 90 : 70}>
      <Icon size={isTopic ? 40 : 30} />
    </HexagonIcon>

    <Text fw={isTopic ? 700 : 500} size={isTopic ? 'md' : 'sm'} mt="sm" ta="center">
      {data.label}
    </Text>
    <Text size="xs" c="dimmed" ta="center">
      {data.type || 'Thema'}
    </Text>
  </div>
);

const TopicNode = ({ data }: any) => <BaseNode data={data} color="#e03131" icon={IconBook} isTopic />;

const ContentNode = ({ data }: any) => {
  const Icon = data.icon || IconFileDescription;
  return <BaseNode data={data} color="#1c7ed6" icon={Icon} isTopic={false} />;
};

const nodeTypes = {
  topic: TopicNode,
  content: ContentNode,
};

const getIconForType = (type: string) => {
  switch (type) {
    case 'PDF':
      return IconFileDescription;
    case 'VIDEO_FILE':
    case 'YOUTUBE_LINK':
      return IconVideo;
    case 'AUDIO_FILE':
    case 'SPOTIFY_LINK':
      return IconHeadphones;
    case 'IMAGE':
      return IconPhoto;
    case 'URI':
      return IconLink;
    default:
      return IconFileDescription;
  }
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
        data: { label: topic.title, topicData: topic },
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
          data: {
            label: content.title,
            type: content.type,
            icon: getIconForType(content.type),
            contentData: content,
          },
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