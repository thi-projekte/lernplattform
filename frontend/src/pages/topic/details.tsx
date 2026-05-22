import { useParams } from 'react-router';
import { useEffect, useMemo, useState } from 'react';
import { useQueryTopic } from '../../api/topic.ts';
import { type Node, type NodeMouseHandler } from '@xyflow/react';
import { Layout } from '../../components/layout.tsx';
import { Drawer, Paper, Text, Stack, Tabs, useMantineTheme } from '@mantine/core';
import { useMediaQuery } from '@mantine/hooks';
import type { Category, Topic } from '../../schemas/topic.ts';
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
import { track } from '@plausible-analytics/tracker';

const nodeTypes = {
  topic: TopicNode,
  content: ContentNode,
};

const TopicDetailsPage = () => {
  const { t } = useTranslation();
  const theme = useMantineTheme();
  const isMobile = useMediaQuery('(max-width: 768px)');

  const { topicId } = useParams<{ topicId: string }>();
  const { data: topic, isLoading } = useQueryTopic(topicId || '', false);

  useEffect(() => {
    if (topicId) {
      track('topicViews', { props: { topicId } });
    }
  }, [topicId]);

  useEffect(() => {
    if (topic) {
      const categoryNames: string[] = topic.categories.map((c: Category) => c.title);
      for (const category of categoryNames) {
        track('topicCategory', { props: { category } });
      }
    }
  }, [topic]);

  const [selectedElement, setSelectedElement] = useState<
    AnyContentElementDto | Omit<Topic, 'relatedTopics'> | null
  >(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const displayedElement = selectedElement ?? topic ?? null;

  const { nodes, edges } = useMemo(() => {
    return buildTopicDetailsGraph(topic);
  }, [topic]);

  const onNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<TopicGraphNodeData>;

    if (graphNode.data.kind === 'topic') {
      setSelectedElement(graphNode.data.payload as Omit<Topic, 'relatedTopics'>);
    } else {
      setSelectedElement(graphNode.data.payload as AnyContentElementDto);
    }

    if (isMobile) {
      setDrawerOpen(true);
    }
  };

  if (isLoading) {
    return <LayoutLoader />;
  }

  const isTopic = displayedElement && 'teaser' in displayedElement;

  const sidebarContent = (
    <Stack gap="md">
      {displayedElement ? (
        isTopic ? (
          <TopicSidebarContent selectedElement={displayedElement as Topic} />
        ) : (
          <ContentSidebarContent
            selectedElement={displayedElement as AnyContentElementDto}
            topicLearnProgress={topic?.learnProgress}
          />
        )
      ) : (
        <Text c="dimmed">{t('common.clickOnANodeToSeeContent')}</Text>
      )}
    </Stack>
  );

  return (
    <Layout>
      <Tabs
        defaultValue="visual"
        style={{ height: 'calc(100vh - 176px)', display: 'flex', flexDirection: 'column' }}
      >
        <Tabs.List mb="md">
          <Tabs.Tab value="visual">{t('topic.tabs.visual')}</Tabs.Tab>
          <Tabs.Tab value="notes">{t('topic.tabs.notes')}</Tabs.Tab>
        </Tabs.List>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: isMobile ? '1fr' : 'minmax(0, 1fr) 360px',
            gap: 16,
            width: '100%',
            flex: 1,
            minHeight: 0,
            overflow: 'hidden',
          }}
        >
          <Tabs.Panel
            value="visual"
            style={{
              position: 'relative',
              borderRadius: 16,
              overflow: 'hidden',
              background: theme.other.graphBg,
            }}
          >
            <TopicGraphView
              nodes={nodes}
              edges={edges}
              onNodeClick={onNodeClick}
              nodeTypes={nodeTypes}
              backgroundColor="#d1d5db"
            />
          </Tabs.Panel>

          <Tabs.Panel value="notes" p="md">
            <Text c="dimmed">{t('topic.tabs.notesPlaceholder')}</Text>
          </Tabs.Panel>

          {!isMobile && (
            <Paper
              withBorder
              shadow="none"
              radius="md"
              p="lg"
              style={{
                width: '100%',
                height: '100%',
                overflowY: 'auto',
              }}
            >
              {sidebarContent}
            </Paper>
          )}
        </div>
      </Tabs>

      <Drawer
        opened={isMobile ? drawerOpen : false}
        onClose={() => setDrawerOpen(false)}
        position="bottom"
        size="auto"
        radius="md"
        title={null}
        withCloseButton={false}
        styles={{
          content: { maxHeight: '75vh', overflowY: 'auto' },
          body: { paddingBottom: 'env(safe-area-inset-bottom, 16px)' },
        }}
      >
        {sidebarContent}
      </Drawer>
    </Layout>
  );
};

export default TopicDetailsPage;
