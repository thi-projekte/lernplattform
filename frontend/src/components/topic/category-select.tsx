import { useMemo, useState } from 'react';
import type { Category } from '../../schemas/topic.ts';
import { useDebouncedValue } from '@mantine/hooks';
import { Loader, MultiSelect, type MultiSelectProps, Pill } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { useQueryCategories } from '../../api/topic.ts';
import type { GetInputPropsReturnType } from '@mantine/form';
import { getContrastColor } from '../../utils/color.ts';

const CategorySelect = ({ onChange, value, error, onFocus, onBlur }: GetInputPropsReturnType) => {
  const { t } = useTranslation();

  const selectedCategories = (value ?? []) as Category[];

  const [searchValue, setSearchValue] = useState('');
  const [debouncedSearch] = useDebouncedValue(searchValue, 300);
  const { data: searchResults, isLoading, isFetching } = useQueryCategories(debouncedSearch);

  const selectableData = useMemo(() => {
    const results = searchResults ?? [];
    const combined = [...results];

    selectedCategories.forEach((item) => {
      if (!combined.some((c) => c.id === item.id)) {
        combined.push(item);
      }
    });

    return combined.map((cat) => ({
      value: cat.id,
      label: cat.title,
    }));
  }, [searchResults, selectedCategories]);

  const handleChange = (ids: string[]) => {
    const allKnownCategories = [...(searchResults ?? []), ...selectedCategories];

    const newlySelectedObjects = ids
      .map((id) => allKnownCategories.find((c) => c.id === id))
      .filter((c): c is Category => !!c);

    onChange(newlySelectedObjects);
  };

  const renderMultiSelectPill: MultiSelectProps['renderPill'] = (props) => {
    const category = selectedCategories.find((c) => c.id === props.value);
    return (
      <Pill
        withRemoveButton
        onRemove={props.onRemove}
        styles={{
          root: {
            backgroundColor: category?.color ? `#${category.color}` : undefined,
            color: category?.color ? getContrastColor(category.color) : undefined,
          },
        }}
        variant="filled"
      >
        {props.option.label}
      </Pill>
    );
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
      nothingFoundMessage={
        (isFetching || isLoading) && !!debouncedSearch ? undefined : t('common.nothingFound')
      }
      rightSection={(isLoading || isFetching) && !!debouncedSearch ? <Loader size="xs" /> : null}
      maxValues={3}
      hidePickedOptions
      clearable
      error={error}
      onBlur={onBlur}
      onFocus={onFocus}
      withAsterisk
      renderPill={renderMultiSelectPill}
    />
  );
};

export default CategorySelect;
