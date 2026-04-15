import { Layout } from '../../components/layout.tsx';
import { useQueryPersonalTopicsPaginated } from '../../api/topic.ts';
import { useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import { Skeleton } from 'boneyard-js/react';
import EntityTable, { type EntityTableProps } from '../../components/entity-table.tsx';
import { Badge, Button, Flex, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import type { Category, ListTopicDto } from '../../types/topic.ts';
import { createColumnHelper } from '@tanstack/react-table';
import { IconPlusFilled } from '@tabler/icons-react';
import { useNavigate } from 'react-router';

const BuilderModeListPage = () => {
  const [pagination, setPagination] = useState<PaginationState>({ pageSize: 20, pageIndex: 0 });
  const { data, isLoading } = useQueryPersonalTopicsPaginated(pagination);

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
            <Badge color={category.color} variant="light">
              {category.title}
            </Badge>
          ))}
        </Flex>
      ),
      header: t('topic.fields.categories'),
    }),
    columnHelper.accessor('updatedAt', {
      cell: (info) => info.getValue(),
      header: t('common.updatedAt'),
    }),
    {
      id: 'actions',
      header: t('common.actions'),
      cell: () => <div />,
    },
  ];

  return (
    <Layout>
      <Title>{t('topic.headings.personalTopics')}</Title>
      <Flex justify="flex-end" w="100%" mt={12}>
        <Button variant="filled" onClick={() => navigate('/builder-mode/topics/create')}>
          <IconPlusFilled />
          &nbsp;{t('topic.actions.create')}
        </Button>
      </Flex>
      <Skeleton loading={isLoading}>
        {data && (
          <EntityTable
            data={data.results}
            columns={columns}
            pageCount={data.totalPages}
            pagination={pagination}
            setPagination={setPagination}
          />
        )}
      </Skeleton>
    </Layout>
  );
};

export default BuilderModeListPage;
