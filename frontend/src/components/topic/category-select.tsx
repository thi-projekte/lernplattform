import { useMemo, useState } from 'react';
import type { Category } from '../../schemas/topic.ts';
import { useDebouncedValue } from '@mantine/hooks';
import { Loader, MultiSelect } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { useQueryCategories } from '../../api/topic.ts';
import type { GetInputPropsReturnType } from '@mantine/form';

const CategorySelect = ({onChange, value, error, onFocus, onBlur}: GetInputPropsReturnType) => {
  const { t } = useTranslation();

  const [searchValue, setSearchValue] = useState('');
  const [debouncedSearch] = useDebouncedValue(searchValue, 300);
  const { data: searchResults, isLoading, isFetching } = useQueryCategories(debouncedSearch);

  console.log(searchResults);

  const [selectedCategoriesBuffer, setSelectedCategoriesBuffer] = useState<Category[]>([]);

  const selectableData = useMemo(() => {
    const results = searchResults ?? [];
    const combined = [...results];

    selectedCategoriesBuffer.forEach((item) => {
      if (!combined.some((c) => c.id === item.id)) {
        combined.push(item);
      }
    });

    return combined.map((cat) => ({
      value: cat.id,
      label: cat.title,
    }));
  }, [searchResults, selectedCategoriesBuffer]);

  const handleChange = (ids: string[]) => {
    const allKnownCategories = [...(searchResults ?? []), ...selectedCategoriesBuffer];

    const newlySelectedObjects = ids
      .map((id) => allKnownCategories.find((c) => c.id === id))
      .filter((c): c is Category => !!c);

    onChange(newlySelectedObjects);
    setSelectedCategoriesBuffer(newlySelectedObjects);
  };

  return (
    <MultiSelect
      label={t('topic.fields.categories')}
      description={t('topic.other.selectUpToThreeCategories')}
      data={selectableData}
      value={(value as Category[]).map((c) => c.id)}
      onChange={handleChange}
      searchable
      searchValue={searchValue}
      onSearchChange={setSearchValue}
      nothingFoundMessage={(isFetching || isLoading) && !!debouncedSearch ? undefined : t('common.nothingFound')}
      rightSection={(isLoading || isFetching) && !!debouncedSearch ? <Loader size="xs" /> : null}
      maxValues={3}
      hidePickedOptions
      clearable
      error={error}
      onBlur={onBlur}
      onFocus={onFocus}
    />
  );
};

export default CategorySelect;