import { useParams } from 'react-router';
import {useMemo, useState } from 'react';
import { useQueryTopic } from '../../api/topic.ts';
import { type Node, type NodeMouseHandler } from '@xyflow/react';
import { Layout } from '../../components/layout.tsx';
import { Paper, Text, Stack, Breadcrumbs, Anchor, Tabs } from '@mantine/core';
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

  const displayedElement = selectedElement ?? topic ?? null;

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

  const isTopic = displayedElement && 'teaser' in displayedElement;

  return (
    <Layout>
      <Breadcrumbs mb="md">
        <Anchor href="/">Startseite</Anchor>
        <Anchor>{topic?.title}</Anchor>
        {displayedElement && !('teaser' in displayedElement) && (
          <Anchor>{(displayedElement as AnyContentElementDto).title}</Anchor>
        )}
      </Breadcrumbs>

      <div
        style={{
          display: 'flex',
          width: '100%',
          height: 'calc(100vh - 100px)',
          overflow: 'hidden',
        }}
      >
        <Tabs defaultValue="visual" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <Tabs.List>
            <Tabs.Tab value="visual">{t('topic.tabs.visual')}</Tabs.Tab>
            <Tabs.Tab value="notes">{t('topic.tabs.notes')}</Tabs.Tab>
          </Tabs.List>

          <Tabs.Panel
            value="visual"
            style={{ flex: 1, position: 'relative', borderRadius: 16, overflow: 'hidden' }}
          >
            <TopicGraphView
              nodes={nodes}
              edges={edges}
              onNodeClick={onNodeClick}
              nodeTypes={nodeTypes}
              backgroundColor="#d9e7f3"
            />
          </Tabs.Panel>

          <Tabs.Panel value="notes" p="md">
            <Text c="dimmed">{t('topic.tabs.notesPlaceholder')}</Text>
          </Tabs.Panel>
        </Tabs>

        <Paper
          shadow="md"
          p="xl"
          style={{
            width: 400,
            overflowY: 'auto',
          }}
        >
          {displayedElement ? (
            <Stack gap="md">
              {isTopic ? (
                <TopicSidebarContent selectedElement={displayedElement as Topic} />
              ) : (
                <ContentSidebarContent selectedElement={displayedElement as AnyContentElementDto} />
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
