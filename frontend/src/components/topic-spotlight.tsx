import { useState } from 'react';
import { Spotlight } from '@mantine/spotlight';
import { useDebouncedValue } from '@mantine/hooks';
import { IconSearch } from '@tabler/icons-react';
import { useSearchTopicsInGraph } from '../api/topic-graph.ts';
import { useNavigate } from 'react-router';

export function TopicSpotlight() {
  const [query, setQuery] = useState('');
  const [debouncedQuery] = useDebouncedValue(query, 300);
  const { data: topics } = useSearchTopicsInGraph(debouncedQuery);
  const navigate = useNavigate();

  const actions = (topics ?? []).map((topic) => ({
    id: topic.id,
    label: topic.title,
    description: topic.creatorFullName,
    onClick: () => navigate('/', { state: { openTopic: topic } }),
  }));

  return (
    <Spotlight
      actions={actions}
      query={query}
      onQueryChange={setQuery}
      nothingFound="Keine Ergebnisse"
      highlightQuery
      limit={5}
      searchProps={{
        leftSection: <IconSearch size={18} />,
        placeholder: 'Themen suchen...',
      }}
      shortcut={['mod + K']}
    />
  );
}
