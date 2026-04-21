import { ActionIcon, Badge, Flex } from '@mantine/core';
import { type EntityTableProps } from '../components/entity-table.tsx';
import { createColumnHelper } from '@tanstack/react-table';
import type { Category, ListTopicDto } from '../schemas/topic.ts';
import { useTranslation } from 'react-i18next';
import { formatDate } from '../utils/date.ts';
import { IconPencil, IconTrash } from '@tabler/icons-react';
import { useDeleteTopicMutation } from '../api/topic.ts';
import { useNavigate } from 'react-router';

export const useTopicColumns = (
  withActions = false,
  withDeleteAction = false,
  withEditAction = false
) => {
  const { t } = useTranslation();

  const { mutate } = useDeleteTopicMutation();
  const navigate = useNavigate();

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
        <Flex direction="row" gap={4}>
          {withEditAction && (
            <ActionIcon
              variant="default"
              onClick={() => navigate(`/builder-mode/topics/${row.original.id}/edit`)}
            >
              <IconPencil />
            </ActionIcon>
          )}
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
