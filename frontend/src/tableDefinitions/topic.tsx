import { ActionIcon, Badge, Flex } from '@mantine/core';
import { type EntityTableProps } from '../components/entity-table.tsx';
import { createColumnHelper } from '@tanstack/react-table';
import type { Category, ListTopicDto } from '../schemas/topic.ts';
import { useTranslation } from 'react-i18next';
import { formatDate } from '../utils/date.ts';
import { IconTrash } from '@tabler/icons-react';
import { useDeleteTopicMutation } from '../api/topic.ts';

export const useTopicColumns = (withActions = false, withDeleteAction = false) => {
  const { t } = useTranslation();

  const { mutate } = useDeleteTopicMutation();

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
    columnHelper.accessor('creatorFullName', {
      cell: (info) => (
        <Badge color="indigo" variant="light">
          {info.getValue()}
        </Badge>
      ),
      header: t('common.creatorFullName'),
    }),
  ];

  if (withActions) {
    columns.push({
      id: 'actions',
      header: t('common.actions'),
      cell: ({ row }) => (
        <Flex direction="row" gap={1}>
          {withDeleteAction && (
            <ActionIcon variant="default" onClick={() => mutate(row.original.id)}>
              <IconTrash />
            </ActionIcon>
          )}
        </Flex>
      ),
    });
  }

  return columns;
};
