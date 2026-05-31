import { ActionIcon, Flex } from '@mantine/core';
import { type EntityTableProps } from '../components/entity-table.tsx';
import { createColumnHelper } from '@tanstack/react-table';
import type { Category, ListTopicDto } from '../schemas/topic.ts';
import { useTranslation } from 'react-i18next';
import { formatDate } from '../utils/date.ts';
import { IconEye, IconPencil, IconTrash } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import CategoryBadge from '../components/category-badge.tsx';
import CreatorCell from './creator-cell.tsx';

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
        <Flex gap={3} wrap="wrap">
          {info.getValue().map((category: Category) => (
            <CategoryBadge
              key={category.id}
              title={category.title}
              color={category.color ?? '8b5cf6'}
              size="sm"
            />
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
        <CreatorCell creatorId={info.row.original.creatorId} creatorFullName={info.getValue()} />
      ),
      header: t('common.creatorFullName'),
    }),
  ];

  if (editAction || deleteActionHandler || viewAction) {
    columns.push({
      id: 'actions',
      size: 180,
      minSize: 180,
      maxSize: 180,
      header: () => <Flex justify="center">{t('common.actions')}</Flex>,
      cell: ({ row }) => (
        <Flex direction="row" gap={5} justify="center" w="105%">
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
