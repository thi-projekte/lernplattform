import { ActionIcon, Badge, Flex } from '@mantine/core';
import { type EntityTableProps } from '../components/entity-table.tsx';
import { createColumnHelper } from '@tanstack/react-table';
import type { Category, ListTopicDto } from '../schemas/topic.ts';
import { useTranslation } from 'react-i18next';
import { formatDate } from '../utils/date.ts';
import { IconEye, IconPencil, IconTrash } from '@tabler/icons-react';
import { useNavigate } from 'react-router';

interface TopicColumnProps {
  editAction?: boolean;
  deleteActionHandler?: (topicId: string) => void;
  viewAction?: boolean;
}

export const useTopicColumns = ({
  editAction,
  deleteActionHandler,
  viewAction,
}: TopicColumnProps) => {
  const { t } = useTranslation();

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
            <Badge color={category.color} variant="light" key={category.id}>
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

  if (editAction || deleteActionHandler || viewAction) {
    columns.push({
      id: 'actions',
      header: () => <Flex justify="center">{t('common.actions')}</Flex>,
      cell: ({ row }) => (
        <Flex direction="row" gap={4} justify="center" w="100%">
          {viewAction && (
            <ActionIcon
              variant="default"
              onClick={() => navigate(`/topics/${row.original.id}/details`)}
            >
              <IconEye />
            </ActionIcon>
          )}
          {editAction && (
            <ActionIcon
              variant="default"
              onClick={() => navigate(`/builder-mode/topics/${row.original.id}/edit`)}
            >
              <IconPencil />
            </ActionIcon>
          )}
          {deleteActionHandler && (
            <ActionIcon variant="default" onClick={() => deleteActionHandler(row.original.id)}>
              <IconTrash />
            </ActionIcon>
          )}
        </Flex>
      ),
    });
  }

  return columns;
};
