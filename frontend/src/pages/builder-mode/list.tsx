import { Layout } from '../../components/layout.tsx';
import { useQueryPersonalTopicsPaginated } from '../../api/topic.ts';
import { useState } from 'react';
import type { PaginationState } from '@tanstack/react-table';
import { Skeleton } from 'boneyard-js/react';
import EntityTable from '../../components/entity-table.tsx';


const BuilderModeListPage = () => {
  
  const [pagination, setPagination] = useState<PaginationState>({pageSize: 20, pageIndex: 0});
  const {data, isLoading} = useQueryPersonalTopicsPaginated(pagination);


  return (
    <Layout>
      <Skeleton loading={isLoading}>
        {data && <EntityTable
          data={data.results}
          columns={[]}
          pageCount={data.totalPages}
          pagination={pagination}
          setPagination={setPagination}
        />}
      </Skeleton>
    </Layout>
  );
}


export default BuilderModeListPage;