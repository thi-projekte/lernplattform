import type { Topic } from '../../schemas/topic.ts';
import { Badge, Group, Paper, Stack, Text, Title } from '@mantine/core';
import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { OnMoveEnd, Viewport } from '@xyflow/react';
import TopicSearchbar from './topic-searchbar.tsx';
import EntityTable from '../entity-table.tsx';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';
import TopicAssociationsGraph from '../graph-view/topic-associations-graph.tsx';
import type { GraphTopicNodeData, TopicGraphNodePositions } from '../graph-view/topic-graph.types.ts';
import type { ListTopicDto } from '../../schemas/topic.ts';

interface AssociatedTopicsStepProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const AssociatedTopicsStep = ({ topic, setTopic }: AssociatedTopicsStepProps) => {
  const { t } = useTranslation();
  const [selectedTopicNode, setSelectedTopicNode] = useState<GraphTopicNodeData | null>(null);
  const [lastViewport, setLastViewport] = useState<Viewport | null>(null);
  const [searchSuggestions, setSearchSuggestions] = useState<ListTopicDto[]>([]);
  const [nodePositions, setNodePositions] = useState<TopicGraphNodePositions>({});

  const removeTopic = (topicId: string) => {
    setTopic({
      ...topic,
      relatedTopics: (topic.relatedTopics ?? []).filter((ass) => ass.id !== topicId),
    });

    setSelectedTopicNode((current) =>
      current && current.payload.id === topicId ? null : current
    );
  };

  const createAssociation = (topicId: string) => {
    const topicToAdd = searchSuggestions.find((topic) => topic.id === topicId);

    if (!topicToAdd) {
      return;
    }

    setTopic({
      ...topic,
      relatedTopics: [...(topic.relatedTopics ?? []), topicToAdd],
    });

    setSelectedTopicNode({
      kind: 'topic',
      title: topicToAdd.title,
      creatorFullName: topicToAdd.creatorFullName,
      payload: topicToAdd,
    });
  };

  const columns = useTopicColumns({ deleteActionHandler: removeTopic });
  const existingIds = useMemo(() => (topic.relatedTopics ?? []).map((t) => t.id), [topic.relatedTopics]);
  const selectedCategories =
    selectedTopicNode && 'categories' in selectedTopicNode.payload
      ? selectedTopicNode.payload.categories
      : undefined;

  const handleMoveEnd: OnMoveEnd = (_event, viewport) => {
    setLastViewport(viewport);
  };

  const handleSuggestionsChange = useCallback((topics: ListTopicDto[], searchTerm: string) => {
    const nextSuggestions = searchTerm.trim() ? topics.slice(0, 4) : [];

    setSearchSuggestions((current) => {
      if (
        current.length === nextSuggestions.length &&
        current.every((topic, index) => topic.id === nextSuggestions[index]?.id)
      ) {
        return current;
      }

      return nextSuggestions;
    });
  }, []);

  const handleNodePositionChange = useCallback(
    (nodeId: string, position: { x: number; y: number }) => {
      setNodePositions((current) => {
        const previous = current[nodeId];
        if (previous && previous.x === position.x && previous.y === position.y) {
          return current;
        }

        return {
          ...current,
          [nodeId]: position,
        };
      });
    },
    []
  );

  return (
    <Stack>
      <TopicSearchbar
        onAdd={(newTopic) =>
          setTopic({ ...topic, relatedTopics: [...(topic.relatedTopics ?? []), newTopic] })
        }
        existingIds={existingIds}
        onSuggestionsChange={handleSuggestionsChange}
      />
      <EntityTable data={topic.relatedTopics ?? []} columns={columns} hidePagination />
      <Paper withBorder radius="md" h={520} style={{ overflow: 'hidden' }}>
        <TopicAssociationsGraph
          topic={{
            id: topic.id,
            title: topic.title,
            categories: topic.categories,
            relatedTopics: topic.relatedTopics,
            isolatedTopics: searchSuggestions,
          }}
          onTopicClick={setSelectedTopicNode}
          onAssociationCreate={createAssociation}
          onAssociationClick={removeTopic}
          onMoveEnd={handleMoveEnd}
          onNodePositionChange={handleNodePositionChange}
          canEditAssociations
          canDeleteAssociations
          allowNodeDragging
          allowCanvasPanning
          nodePositions={nodePositions}
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
            <Text size="sm" c="dimmed">
              {t('topic.graph.clickIsolatedTopicToAssociate')}
            </Text>
          </Stack>
        )}
        {lastViewport && (
          <Text size="sm" c="dimmed">
            {t('topic.graph.viewportStatus', {
              x: Math.round(lastViewport.x),
              y: Math.round(lastViewport.y),
              zoom: lastViewport.zoom.toFixed(2),
            })}
          </Text>
        )}
      </Paper>
    </Stack>
  );
};

export default AssociatedTopicsStep;
