import { useParams } from 'react-router';
import { createElement, useEffect, useMemo, useState } from 'react';
import { useQueryTopic } from '../../api/topic.ts';
import { type Node, type NodeMouseHandler } from '@xyflow/react';
import { Layout } from '../../components/layout.tsx';
import {
  Box,
  Button,
  Drawer,
  Group,
  Paper,
  Stack,
  Tabs,
  Text,
  ThemeIcon,
  Title,
  UnstyledButton,
  useMantineTheme,
} from '@mantine/core';
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
import { CONTENT_ICONS, DEFAULT_ICON_BY_TYPE } from '../../components/icon-picker/icons.ts';
import { IconCheck, IconMessageCircle, IconRobot } from '@tabler/icons-react';
import CategoryBadge from '../../components/category-badge.tsx';

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

  const [selectedContentElement, setSelectedContentElement] =
    useState<AnyContentElementDto | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const [selectedGraphElement, setSelectedGraphElement] = useState<
    AnyContentElementDto | Omit<Topic, 'relatedTopics'> | null
  >(null);

  const completedIds = topic?.learnProgress?.completedContentElementIds ?? [];

  const { nodes, edges } = useMemo(() => buildTopicDetailsGraph(topic), [topic]);

  const onGraphNodeClick: NodeMouseHandler = (_event, node) => {
    const graphNode = node as Node<TopicGraphNodeData>;
    if (graphNode.data.kind === 'topic') {
      setSelectedGraphElement(graphNode.data.payload as Omit<Topic, 'relatedTopics'>);
    } else {
      setSelectedGraphElement(graphNode.data.payload as AnyContentElementDto);
    }
  };

  const handleContentElementClick = (el: AnyContentElementDto) => {
    setSelectedContentElement(el);
    if (isMobile) setDrawerOpen(true);
  };

  if (isLoading) {
    return <LayoutLoader />;
  }

  const graphDisplayedElement = selectedGraphElement ?? topic ?? null;
  const isGraphTopic = graphDisplayedElement && 'teaser' in graphDisplayedElement;

  const graphSidebarContent = (
    <Stack gap="md">
      {graphDisplayedElement ? (
        isGraphTopic ? (
          <TopicSidebarContent selectedElement={graphDisplayedElement as Topic} />
        ) : (
          <ContentSidebarContent
            selectedElement={graphDisplayedElement as AnyContentElementDto}
            topicLearnProgress={topic?.learnProgress}
          />
        )
      ) : (
        <Text c="dimmed">{t('common.clickOnANodeToSeeContent')}</Text>
      )}
    </Stack>
  );

  const contentElementList = (
    <Stack gap="xs">
      {topic?.contentElements?.length ? (
        topic.contentElements.map((el: AnyContentElementDto) => {
          const isSelected = selectedContentElement?.id === el.id;
          const isCompleted = completedIds.includes((el.id as string) ?? '');
          const iconComp =
            (el.icon ? CONTENT_ICONS[el.icon] : undefined) ??
            CONTENT_ICONS[DEFAULT_ICON_BY_TYPE[el.type]];

          return (
            <UnstyledButton
              key={el.id}
              onClick={() => handleContentElementClick(el)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '10px 14px',
                borderRadius: 12,
                border: `1.5px solid ${isSelected ? theme.colors.brand[5] : theme.other.layoutBorder}`,
                background: isSelected
                  ? 'rgba(58, 163, 216, 0.08)'
                  : isCompleted
                    ? 'rgba(67, 184, 107, 0.06)'
                    : 'white',
                width: '100%',
                transition: 'border-color 150ms ease, background 150ms ease',
                cursor: 'pointer',
              }}
            >
              {iconComp && (
                <ThemeIcon
                  size={32}
                  radius="md"
                  variant="light"
                  color={isCompleted ? 'green' : isSelected ? 'brand' : 'teal'}
                >
                  {createElement(iconComp, { size: 18 })}
                </ThemeIcon>
              )}
              <Box style={{ flex: 1, minWidth: 0 }}>
                <Text
                  fw={600}
                  size="sm"
                  style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                >
                  {el.title}
                </Text>
                <Text size="xs" c="dimmed">
                  {t(`topic.contentElementType.${el.type}`)}
                </Text>
              </Box>
              {isCompleted && (
                <ThemeIcon size={20} radius="xl" color="green" variant="light">
                  <IconCheck size={12} />
                </ThemeIcon>
              )}
            </UnstyledButton>
          );
        })
      ) : (
        <Text c="dimmed" size="sm">
          {t('common.clickOnANodeToSeeContent')}
        </Text>
      )}
    </Stack>
  );

  return (
    <Layout>
      <div
        style={{
          height: 'calc(100vh - 176px)',
          display: 'flex',
          flexDirection: 'column',
          gap: 16,
          overflow: isMobile ? 'auto' : 'hidden',
        }}
      >
        {/* Compact topic header: title + teaser + categories | Myna widget */}
        {topic && (
          <Paper withBorder shadow="none" radius="lg" p="lg" bg="transparent" style={{ flexShrink: 0 }}>
            <Group justify="space-between" align="flex-start" wrap={isMobile ? 'wrap' : 'nowrap'} gap="lg">
              <Stack gap="xs" style={{ flex: 1, minWidth: 0 }}>
                <Title order={2}>{topic.title}</Title>
                <Text size="sm" c="dimmed" lineClamp={2}>{topic.teaser}</Text>
                <Group gap={6}>
                  {topic.categories?.map((cat: Category) => (
                    <CategoryBadge key={cat.id} title={cat.title} color={cat.color ?? '8b5cf6'} />
                  ))}
                </Group>
              </Stack>

              <Paper
                withBorder
                shadow="none"
                radius="md"
                p="md"
                style={{
                  minWidth: 200,
                  flexShrink: 0,
                  border: `1px solid ${theme.other.layoutBorder}`,
                }}
              >
                <Group gap="sm" mb="sm" wrap="nowrap">
                  <ThemeIcon color="blue" radius="xl" variant="light" size={32}>
                    <IconRobot size={16} />
                  </ThemeIcon>
                  <div>
                    <Text fw={600} size="sm">Myna</Text>
                    <Text size="xs" c="dimmed">{t('topic.myna.subtitle')}</Text>
                  </div>
                </Group>
                <Button
                  variant="outline"
                  color="blue"
                  fullWidth
                  size="xs"
                  leftSection={<IconMessageCircle size={14} />}
                >
                  {t('topic.myna.askButton')}
                </Button>
              </Paper>
            </Group>
          </Paper>
        )}

        <Tabs
          defaultValue="content"
          style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}
        >
          <Tabs.List mb="md">
            <Tabs.Tab value="content">{t('topic.tabs.content')}</Tabs.Tab>
            <Tabs.Tab value="visual">{t('topic.tabs.visual')}</Tabs.Tab>
            <Tabs.Tab value="notes">{t('topic.tabs.notes')}</Tabs.Tab>
          </Tabs.List>

          <Tabs.Panel
            value="content"
            style={{ flex: 1, minHeight: 0, overflow: isMobile ? 'auto' : 'hidden' }}
          >
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: isMobile ? '1fr' : '2fr 3fr',
                gap: 16,
                height: '100%',
              }}
            >
              <Paper
                withBorder
                shadow="none"
                radius="lg"
                p="md"
                style={{
                  overflowY: 'auto',
                  border: `1px solid ${theme.other.layoutBorder}`,
                }}
              >
                {contentElementList}
              </Paper>

              {!isMobile && (
                <Paper
                  withBorder
                  shadow="none"
                  radius="lg"
                  p="lg"
                  style={{
                    overflowY: 'auto',
                    border: `1px solid ${theme.other.layoutBorder}`,
                  }}
                >
                  {selectedContentElement ? (
                    <Stack gap="md">
                      <ContentSidebarContent
                        selectedElement={selectedContentElement}
                        topicLearnProgress={topic?.learnProgress}
                      />
                    </Stack>
                  ) : topic ? (
                    <TopicSidebarContent selectedElement={topic} />
                  ) : null}
                </Paper>
              )}
            </div>
          </Tabs.Panel>

          <Tabs.Panel value="visual" style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: isMobile ? '1fr' : 'minmax(0, 1fr) 360px',
                gap: 16,
                height: '100%',
              }}
            >
              <div
                style={{
                  position: 'relative',
                  borderRadius: 16,
                  overflow: 'hidden',
                  background: theme.other.graphBg,
                  border: `1px solid ${theme.other.layoutBorder}`,
                }}
              >
                <TopicGraphView
                  nodes={nodes}
                  edges={edges}
                  onNodeClick={onGraphNodeClick}
                  nodeTypes={nodeTypes}
                  backgroundColor={theme.other.graphDots}
                />
              </div>

              {!isMobile && (
                <Paper withBorder shadow="none" radius="md" p="lg" style={{ overflowY: 'auto' }}>
                  {graphSidebarContent}
                </Paper>
              )}
            </div>
          </Tabs.Panel>

          <Tabs.Panel value="notes" p="md">
            <Text c="dimmed">{t('topic.tabs.notesPlaceholder')}</Text>
          </Tabs.Panel>
        </Tabs>
      </div>

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
        {selectedContentElement && (
          <Stack gap="md">
            <ContentSidebarContent
              selectedElement={selectedContentElement}
              topicLearnProgress={topic?.learnProgress}
            />
          </Stack>
        )}
      </Drawer>
    </Layout>
  );
};

export default TopicDetailsPage;