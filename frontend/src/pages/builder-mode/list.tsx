import { Layout } from '../../components/layout.tsx';
import { useDeleteTopicMutation, useQueryPersonalTopicsPaginated } from '../../api/topic.ts';
import { useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import { Skeleton } from 'boneyard-js/react';
import EntityTable from '../../components/entity-table.tsx';
import { Button, Flex, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { IconPlusFilled } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTopicColumns } from '../../tableDefinitions/topic.tsx';

const BuilderModeListPage = () => {
  const [pagination, setPagination] = useState<PaginationState>({ pageSize: 20, pageIndex: 0 });
  const { data, isLoading } = useQueryPersonalTopicsPaginated(pagination);

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { mutate } = useDeleteTopicMutation();
  const columns = useTopicColumns({editAction: true, deleteActionHandler: mutate});

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
            isFetching={isLoading}
            setPagination={setPagination}
          />
        )}
      </Skeleton>
    </Layout>
  );
};

export default BuilderModeListPage;
