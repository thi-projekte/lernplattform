import { apiClient, safeValidateApiResponseContent } from './common.ts';
import { useQueries, useQuery } from '@tanstack/react-query';
import { type GraphTopicDto, GraphTopicDtoSchema } from '../schemas/topic-graph.ts';
import { z } from 'zod';

const fetchMostPopularWithNeighbors = async (
  builderMode?: boolean
): Promise<GraphTopicDto[]> => {
  const search = new URLSearchParams();
  if (builderMode) {
    search.set('builderMode', 'true');
  }

  const result = await apiClient.get(
    `/topics/graph${search.size > 0 ? `?${search.toString()}` : ''}`,
    {
      validateStatus: (status) => status <= 204,
    }
  );
  return safeValidateApiResponseContent(z.array(GraphTopicDtoSchema), result.data);
};

export const useFetchMostPopularTopicsWithNeighbors = (
  builderMode?: boolean,
  enabled = true
) => {
  return useQuery({
    queryKey: ['mostPopularTopics', builderMode],
    queryFn: () => fetchMostPopularWithNeighbors(builderMode),
    enabled,
  });
};

const fetchDirectNeighbors = async (topicId: string): Promise<GraphTopicDto[]> => {
  const result = await apiClient.get(`/topics/graph/${topicId}/neighbors`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(z.array(GraphTopicDtoSchema), result.data);
};

export const useFetchDirectNeighbors = (topicId: string | null, enabled = true) => {
  return useQuery({
    queryKey: ['directNeighborTopics', topicId],
    queryFn: () => fetchDirectNeighbors(topicId!),
    enabled: Boolean(topicId) && enabled,
  });
};

export const useFetchDirectNeighborQueries = (topicIds: string[]) => {
  return useQueries({
    queries: topicIds.map((topicId) => ({
      queryKey: ['directNeighborTopics', topicId],
      queryFn: () => fetchDirectNeighbors(topicId),
      enabled: Boolean(topicId),
    })),
  });
};

const searchTopicsInGraph = async (search: string): Promise<GraphTopicDto[]> => {
  const result = await apiClient.get(`/topics/graph?search=${search}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(z.array(GraphTopicDtoSchema), result.data);
};

export const useSearchTopicsInGraph = (search: string) => {
  return useQuery({
    queryKey: ['searchTopicGraph', search],
    queryFn: () => searchTopicsInGraph(search),
  });
};
