import { useParams } from 'react-router';
import { useEffect, useState, createElement } from 'react';
import { useQueryTopic } from '../../api/topic.ts';
import { Layout } from '../../components/layout.tsx';
import {
  Badge,
  Group,
  Paper,
  Progress,
  ScrollArea,
  Stack,
  Tabs,
  Text,
  ThemeIcon,
  Title,
  UnstyledButton,
} from '@mantine/core';
import { IconLayoutList } from '@tabler/icons-react';
import CategoryBadge from '../../components/category-badge.tsx';
import { useMediaQuery } from '@mantine/hooks';
import type { Category } from '../../schemas/topic.ts';
import type { AnyContentElementDto } from '../../schemas/content-element.ts';
import { useTranslation } from 'react-i18next';
import ContentSidebarContent from '../../components/graph-view/sidebar/content-sidebar-content.tsx';
import TopicSidebarContent from '../../components/graph-view/sidebar/topic-sidebar-content.tsx';
import QuizView from '../../components/topic/quiz-view.tsx';
import TopicChat from '../../components/topic/topic-chat.tsx';
import TopicNotes from '../../components/topic/topic-notes.tsx';
import LayoutLoader from '../../components/layout-loader.tsx';
import { CONTENT_ICONS, DEFAULT_ICON_BY_TYPE } from '../../components/icon-picker/icons.ts';
import { IconCheck } from '@tabler/icons-react';
import { track } from '@plausible-analytics/tracker';

const TopicDetailsPage = () => {
  const { t } = useTranslation();
  const { topicId } = useParams<{ topicId: string }>();
  const { data: topic, isLoading } = useQueryTopic(topicId || '', false);
  const isMobile = useMediaQuery('(max-width: 768px)');

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

  const [selectedElement, setSelectedElement] = useState<AnyContentElementDto | null>(null);
  const [activeTab, setActiveTab] = useState<string | null>('visual');

  if (isLoading) {
    return <LayoutLoader />;
  }

  const unsortedContentElements: AnyContentElementDto[] = topic?.contentElements ?? [];

  const contentElements = unsortedContentElements.toSorted((a, b) => {
    const rankA = a.rank ?? Infinity;
    const rankB = b.rank ?? Infinity;

    return rankA - rankB;
  });

  const learnProgress = topic?.learnProgress;

  const contentElementIds = new Set(
    topic?.contentElements?.map((el: AnyContentElementDto) => el.id) ?? []
  );
  const completedIds = (learnProgress?.completedContentElementIds ?? []).filter((id: string) =>
    contentElementIds.has(id)
  );
  const totalElements = contentElementIds.size;
  const isTopicCompleted =
    learnProgress?.status === 'COMPLETED_MANUALLY' ||
    (totalElements > 0 && completedIds.length >= totalElements);
  const topicProgressPercent = isTopicCompleted
    ? 100
    : totalElements > 0
      ? Math.round((completedIds.length / totalElements) * 100)
      : 0;

  return (
    <Layout>
      <Tabs
        value={activeTab}
        onChange={setActiveTab}
        style={{
          height: isMobile ? 'auto' : 'calc(100vh - 176px)',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {topic && (
          <Stack gap={6} mb="md">
            <Title order={3}>{topic.title}</Title>
            {topic.categories && topic.categories.length > 0 && (
              <Group gap={6}>
                {topic.categories.map((cat: Category) => (
                  <CategoryBadge key={cat.id} title={cat.title} color={cat.color ?? '8b5cf6'} />
                ))}
              </Group>
            )}
            {learnProgress && (
              <Group
                gap={8}
                mt={4}
                align="center"
                wrap="nowrap"
                style={{ maxWidth: isMobile ? '100%' : '25vw' }}
              >
                <Progress
                  value={topicProgressPercent}
                  color={isTopicCompleted ? 'green' : 'blue'}
                  size="sm"
                  style={{ flex: 1 }}
                />
                <Text size="xs" fw={600} style={{ flexShrink: 0 }}>
                  {topicProgressPercent}%
                </Text>
              </Group>
            )}
          </Stack>
        )}

        <Tabs.List mb="md">
          <Tabs.Tab value="visual">{t('topic.tabs.visual')}</Tabs.Tab>
          <Tabs.Tab value="notes">{t('topic.tabs.notes')}</Tabs.Tab>
          <Tabs.Tab value="quiz">{t('topic.tabs.quiz')}</Tabs.Tab>
          <Tabs.Tab value="chat">{t('topic.tabs.chat')}</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel
          value="visual"
          style={{
            flex: 1,
            minHeight: 0,
            overflow: isMobile ? 'visible' : 'hidden',
          }}
        >
          <div
            style={{
              display: 'flex',
              gap: 16,
              height: isMobile ? 'auto' : '100%',
              flexDirection: isMobile ? 'column' : 'row',
            }}
          >
            <Paper
              withBorder
              shadow="none"
              radius="md"
              style={{
                width: isMobile ? '100%' : '30%',
                flexShrink: 0,
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                minHeight: isMobile ? 200 : undefined,
              }}
            >
              <ScrollArea style={{ flex: 1 }} p="sm">
                <Stack gap="xs">
                  {topic && (
                    <UnstyledButton
                      onClick={() => setSelectedElement(null)}
                      p="sm"
                      style={(theme) => ({
                        borderRadius: theme.radius.md,
                        background: !selectedElement ? 'var(--card-selected-bg)' : 'var(--card-bg)',
                        border: `1px solid ${!selectedElement ? 'var(--card-selected-border)' : 'var(--card-border)'}`,
                        transition: 'background 0.15s, border-color 0.15s',
                      })}
                    >
                      <Group gap="sm" wrap="nowrap">
                        <ThemeIcon
                          size={36}
                          radius="md"
                          variant="light"
                          color="violet"
                          style={{ flexShrink: 0 }}
                        >
                          {createElement(IconLayoutList, { size: 18 })}
                        </ThemeIcon>
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <Text size="sm" fw={600} truncate>
                            {topic.title}
                          </Text>
                          <Text size="xs" c="dimmed">
                            {t('topic.actions.overview')}
                          </Text>
                        </div>
                      </Group>
                    </UnstyledButton>
                  )}
                  {contentElements.length === 0 ? (
                    <Text c="dimmed" size="sm" p="xs">
                      {t('common.noEntriesFound')}
                    </Text>
                  ) : (
                    contentElements.map((el) => {
                      const iconComponent =
                        (el.icon ? CONTENT_ICONS[el.icon] : undefined) ??
                        CONTENT_ICONS[DEFAULT_ICON_BY_TYPE[el.type]];
                      const isCompleted = !!learnProgress?.completedContentElementIds.includes(
                        el.id
                      );
                      const isSelected = selectedElement?.id === el.id;

                      return (
                        <UnstyledButton
                          key={el.id}
                          onClick={() => setSelectedElement(isSelected ? null : el)}
                          p="sm"
                          style={(theme) => ({
                            borderRadius: theme.radius.md,
                            background: isSelected ? 'var(--card-selected-bg)' : 'var(--card-bg)',
                            border: `1px solid ${isSelected ? 'var(--card-selected-border)' : 'var(--card-border)'}`,
                            transition: 'background 0.15s, border-color 0.15s',
                          })}
                        >
                          <Group gap="sm" wrap="nowrap">
                            <ThemeIcon
                              size={36}
                              radius="md"
                              variant="light"
                              color={isCompleted ? 'green' : 'blue'}
                              style={{ flexShrink: 0 }}
                            >
                              {isCompleted
                                ? createElement(IconCheck, { size: 18 })
                                : iconComponent && createElement(iconComponent, { size: 18 })}
                            </ThemeIcon>
                            <div style={{ flex: 1, minWidth: 0 }}>
                              <Text size="sm" fw={500} truncate>
                                {el.title}
                              </Text>
                              <Text size="xs" c="dimmed">
                                {t(`topic.contentElementType.${el.type}`)}
                              </Text>
                            </div>
                            {isCompleted && (
                              <Badge
                                size="xs"
                                color="green"
                                variant="light"
                                style={{ flexShrink: 0 }}
                              >
                                {t('topic.actions.contentElementCompleted')}
                              </Badge>
                            )}
                          </Group>
                        </UnstyledButton>
                      );
                    })
                  )}
                </Stack>
              </ScrollArea>
            </Paper>

            <Paper
              withBorder
              shadow="none"
              radius="md"
              p="lg"
              style={{
                flex: 1,
                minWidth: 0,
                overflowY: 'auto',
                background: 'var(--panel-right-bg)',
              }}
            >
              <Stack gap="md">
                {selectedElement ? (
                  <ContentSidebarContent
                    selectedElement={selectedElement}
                    topicLearnProgress={learnProgress}
                  />
                ) : topic ? (
                  <TopicSidebarContent selectedElement={topic} />
                ) : (
                  <Text c="dimmed">{t('common.clickOnANodeToSeeContent')}</Text>
                )}
              </Stack>
            </Paper>
          </div>
        </Tabs.Panel>

        <Tabs.Panel value="notes" style={{ flex: 1, minHeight: 0 }} p="md">
          {topicId && <TopicNotes topicId={topicId} />}
        </Tabs.Panel>

        <Tabs.Panel value="quiz" style={{ flex: 1, minHeight: 0, overflowY: 'auto' }} p="md">
          <QuizView indexCards={topic?.indexCards ?? []} />
        </Tabs.Panel>

        {/* Connect the chat socket only while the tab is open. */}
        <Tabs.Panel value="chat" style={{ flex: 1, minHeight: 0 }} p="md">
          {activeTab === 'chat' && topicId && <TopicChat topicId={topicId} />}
        </Tabs.Panel>
      </Tabs>
    </Layout>
  );
};

export default TopicDetailsPage;
