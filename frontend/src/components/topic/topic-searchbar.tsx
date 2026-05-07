import { useEffect, useMemo, useState } from 'react';
import { Autocomplete, Loader } from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { useQuerySearchTopic } from '../../api/topic.ts';
import type { ListTopicDto } from '../../schemas/topic.ts';
import { useTranslation } from 'react-i18next';

interface TopicSearchbarProps {
  onAdd?: (topic: ListTopicDto) => void;
  existingIds?: string[];
  onSuggestionsChange?: (topics: ListTopicDto[], searchTerm: string) => void;
}

const TopicSearchbar = ({ onAdd, existingIds = [], onSuggestionsChange }: TopicSearchbarProps) => {
  const [value, setValue] = useState('');
  const [debouncedSearch] = useDebouncedValue(value, 300);

  const { t } = useTranslation();

  const { data: suggestions, isLoading, isFetching } = useQuerySearchTopic(debouncedSearch);

  const filteredSuggestions = useMemo(
    () => (suggestions ?? []).filter((t) => !existingIds.includes(t.id)),
    [existingIds, suggestions]
  );
  const autocompleteData = useMemo(
    () => filteredSuggestions.map((t) => t.title),
    [filteredSuggestions]
  );

  useEffect(() => {
    onSuggestionsChange?.(filteredSuggestions, debouncedSearch);
  }, [debouncedSearch, filteredSuggestions, onSuggestionsChange]);

  const handleOptionSubmit = (val: string) => {
    const selectedTopic = suggestions?.find((t) => t.title === val);

    if (selectedTopic && onAdd) {
      onAdd(selectedTopic);
      setValue('');
    }
  };

  return (
    <Autocomplete
      placeholder={t('topic.other.searchByTitleOrTeaser')}
      value={value}
      onChange={setValue}
      onOptionSubmit={handleOptionSubmit}
      data={autocompleteData}
      rightSection={isLoading || isFetching ? <Loader size="xs" /> : null}
      rightSectionPointerEvents="all"
    />
  );
};

export default TopicSearchbar;
