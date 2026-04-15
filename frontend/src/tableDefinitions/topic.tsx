import { Badge, Flex } from '@mantine/core';
import { type EntityTableProps } from '../components/entity-table.tsx';
import { createColumnHelper } from '@tanstack/react-table';
import type { Category, ListTopicDto } from '../schemas/topic.ts';
import { useTranslation } from 'react-i18next';
import { formatDate } from '../utils/date.ts';


export const useTopicColumns = (withActions = false) => {
  const {t} = useTranslation();
  const columnHelper = createColumnHelper<ListTopicDto>();
  const columns: EntityTableProps<ListTopicDto>['columns'] = [
    columnHelper.accessor('title', {
      cell: (info) => info.getValue(),
      header: t('topic.fields.title'),
    }),
    columnHelper.accessor('categories', {
      cell: (info) => (
        <Flex gap={3}>
          {info.getValue().map((category: Category) => (
            <Badge color={category.color} variant="light">
              {category.title}
            </Badge>
          ))}
        </Flex>
      ),
      header: t('topic.fields.categories'),
    }),
    columnHelper.accessor('updatedAt', {
      cell: (info) => formatDate(info.getValue()),
      header: t('common.updatedAt'),
    }),
  ];
  
  if (withActions) {
    columns.push({
      id: 'actions',
      header: t('common.actions'),
      cell: () => <div />,
    });
  }

  return columns;
}