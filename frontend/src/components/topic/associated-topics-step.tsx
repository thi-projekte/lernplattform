import type { Topic } from '../../schemas/topic.ts';
import { Stack } from '@mantine/core';
import TopicSearchbar from './topic-searchbar.tsx';
import EntityTable from '../entity-table.tsx';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';

interface AssociatedTopicsStepProps {
  topic: Partial<Topic>,
  setTopic: (topic: Partial<Topic>) => void;
}

const AssociatedTopicsStep = ({topic, setTopic}: AssociatedTopicsStepProps) => {

  const columns = useTopicColumns();

  return (
    <Stack>
      <TopicSearchbar
        onAdd={(newTopic) => setTopic({...topic, relatedTopics: [...(topic.relatedTopics ?? []), newTopic]})}
        existingIds={(topic.relatedTopics ?? []).map((t) => t.id)}
      />
      <EntityTable data={topic.relatedTopics ?? []} columns={columns} hidePagination />
    </Stack>
  )
}

export default AssociatedTopicsStep;