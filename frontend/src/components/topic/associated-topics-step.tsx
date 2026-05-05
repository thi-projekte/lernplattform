import type { Topic } from '../../schemas/topic.ts';
import { Badge, Group, Paper, Stack, Text, Title } from '@mantine/core';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import TopicSearchbar from './topic-searchbar.tsx';
import EntityTable from '../entity-table.tsx';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';
import TopicAssociationsGraph from '../graph-view/topic-associations-graph.tsx';
import type { GraphTopicNodeData } from '../graph-view/topic-graph.types.ts';

interface AssociatedTopicsStepProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const AssociatedTopicsStep = ({ topic, setTopic }: AssociatedTopicsStepProps) => {
  const { t } = useTranslation();
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);

  const removeTopic = (topicId: string) => {
    setTopic({
      ...topic,
      relatedTopics: (topic.relatedTopics ?? []).filter((ass) => ass.id !== topicId),
    });

    setSelectedTopicNode((current) =>
      current && current.payload.id === topicId ? null : current
    );
  };

  const columns = useTopicColumns({ deleteActionHandler: removeTopic });
  const selectedCategories =
    selectedTopicNode && 'categories' in selectedTopicNode.payload
      ? selectedTopicNode.payload.categories
      : undefined;

  return (
    <Stack>
      <TopicSearchbar
        onAdd={(newTopic) =>
          setTopic({ ...topic, relatedTopics: [...(topic.relatedTopics ?? []), newTopic] })
        }
        existingIds={(topic.relatedTopics ?? []).map((t) => t.id)}
      />
      <EntityTable data={topic.relatedTopics ?? []} columns={columns} hidePagination />
      <Paper withBorder radius="md" h={520} style={{ overflow: 'hidden' }}>
        <TopicAssociationsGraph
          topic={{
            id: topic.id,
            title: topic.title,
            categories: topic.categories,
            relatedTopics: topic.relatedTopics,
          }}
          onTopicClick={setSelectedTopicNode}
          onAssociationClick={removeTopic}
          canDeleteAssociations
          allowNodeDragging
          allowCanvasPanning
        />
      </Paper>
      <Paper withBorder radius="md" p="md">
        {selectedTopicNode ? (
          <Stack gap="xs">
            <Title order={4}>{selectedTopicNode.title}</Title>
            {selectedTopicNode.creatorFullName && (
              <Text size="sm" c="dimmed">
                {selectedTopicNode.creatorFullName}
              </Text>
            )}
            {selectedCategories && selectedCategories.length > 0 && (
              <Group gap="xs">
                {selectedCategories.map((category) => (
                  <Badge key={category.title} color={`#${category.color}`}>
                    {category.title}
                  </Badge>
                ))}
              </Group>
            )}
            <Text size="sm" c="dimmed">
              {selectedTopicNode.isRoot
                ? t('topic.graph.currentTopic')
                : t('topic.graph.associatedTopic')}
            </Text>
          </Stack>
        ) : (
          <Stack gap="xs">
            <Text size="sm" c="dimmed">
              {t('topic.graph.clickTopicNodeToSeeDetails')}
            </Text>
            <Text size="sm" c="dimmed">
              {t('topic.graph.clickAssociationToRemove')}
            </Text>
          </Stack>
        )}
      </Paper>
    </Stack>
  );
};

export default AssociatedTopicsStep;
