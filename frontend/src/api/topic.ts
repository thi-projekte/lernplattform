import { apiClient } from './common.ts';
import { type PaginatedListTopicDto, PaginatedListTopicDtoSchema } from '../types/topic.ts';
import type { PaginationState } from '@tanstack/react-table';
import { useQuery } from '@tanstack/react-query';


const fetchPersonalTopicsPaginated = async (page: number, pageSize: number): Promise<PaginatedListTopicDto> => {
    const result = await apiClient.get(`/topics/personal?page=${page}&pageSize=${pageSize}`, {
      validateStatus: (status) => status <= 204
    });
    return PaginatedListTopicDtoSchema.parse(result.data);
}

export const useQueryPersonalTopicsPaginated = (pagination: PaginationState) => {
  return useQuery({
    queryKey: ["personalTopics", pagination.pageSize, pagination.pageIndex],
    queryFn: () => fetchPersonalTopicsPaginated(pagination.pageIndex, pagination.pageSize)
  });
}