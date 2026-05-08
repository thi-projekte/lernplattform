import { useParams } from 'react-router';
import { useQueryTopic } from '../../api/topic.ts';
import { type Node, type NodeMouseHandler } from '@xyflow/react';
import { Layout } from '../../components/layout.tsx';
import { Paper, Text, Stack } from '@mantine/core';
import { useMemo, useState } from 'react';
import type { Topic } from '../../schemas/topic.ts';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import TopicNode from '../../components/graph-view/topic-node.tsx';
import ContentNode from '../../components/graph-view/content-node.tsx';
import { useTranslation } from 'react-i18next';
import ContentSidebarContent from '../../components/graph-view/sidebar/content-sidebar-content.tsx';
import TopicSidebarContent from '../../components/graph-view/sidebar/topic-sidebar-content.tsx';
import LayoutLoader from '../../components/layout-loader.tsx';
import TopicGraphView from '../../components/graph-view/topic-graph.tsx';
import { buildTopicDetailsGraph } from '../../components/graph-view/topic-graph.utils.ts';
import type { TopicGraphNodeData } from '../../components/graph-view/topic-graph.types.ts';

const nodeTypes = {
  topic: TopicNode,
  content: ContentNode,
};

const TopicDetailsPage = () => {
  const { t } = useTranslation();

  const { topicId } = useParams<{ topicId: string }>();
  const { data: topic, isLoading } = useQueryTopic(topicId || '', false);

  const [selectedElement, setSelectedElement] = useState<
    AnyContentElementDto | Omit<Topic, 'relatedTopics'> | null
  >(null);

  const { nodes, edges } = useMemo(() => {
    return buildTopicDetailsGraph(topic);
  }, [topic]);

  const onNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<TopicGraphNodeData>;

    if (graphNode.data.kind === 'topic') {
      setSelectedElement(graphNode.data.payload as Omit<Topic, 'relatedTopics'>);
      return;
    }

    setSelectedElement(graphNode.data.payload as AnyContentElementDto);
  };

  if (isLoading) {
    return <LayoutLoader />;
  }

  const isTopic = selectedElement && 'teaser' in selectedElement;

  return (
    <Layout>
      <div
        style={{
          display: 'flex',
          width: '100%',
          height: 'calc(100vh - 100px)',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            flex: 1,
            position: 'relative',
            background: 'rgba(244, 250, 255, 0.95)',
            borderRadius: 16,
            border: '1px solid rgba(176, 210, 232, 0.5)',
            overflow: 'hidden',
          }}
        >
          <TopicGraphView
            nodes={nodes}
            edges={edges}
            onNodeClick={onNodeClick}
            nodeTypes={nodeTypes}
            backgroundColor="#d9e7f3"
          />
        </div>

        <Paper
          shadow="md"
          p="xl"
          style={{
            width: 400,
            borderLeft: '1px solid rgba(176, 210, 232, 0.65)',
            overflowY: 'auto',
            background: 'rgba(248, 252, 255, 0.94)',
          }}
        >
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
    </Layout>
  );
};

export default TopicDetailsPage;
