import { apiClient } from './common.ts';
import { useQuery } from '@tanstack/react-query';
import { type GraphTopicDto, GraphTopicDtoSchema } from '../schemas/topic-graph.ts';
import { z } from 'zod';

const fetchMostPopularWithNeighbors = async (categoryIds?: string[]): Promise<GraphTopicDto[]> => {
  const result = await apiClient.get(
    `/topics/graph/most-popular${categoryIds ? `?categoryFilter=${categoryIds.join(',')}` : ''}`,
    {
      validateStatus: (status) => status <= 204,
    }
  );
  return z.array(GraphTopicDtoSchema).parse(result.data);
};

export const useFetchMostPopularTopicsWithNeighbors = (categoryIds?: string[]) => {
  return useQuery({
    queryKey: ['mostPopularTopics', categoryIds],
    queryFn: () => fetchMostPopularWithNeighbors(categoryIds),
  });
};

const fetchDirectNeighbors = async (topicId: string): Promise<GraphTopicDto[]> => {
  const result = await apiClient.get(`/topics/graph/${topicId}/graph-neighbors`, {
    validateStatus: (status) => status <= 204,
  });
  return z.array(GraphTopicDtoSchema).parse(result.data);
};

export const useFetchDirectNeighbors = (topicId: string) => {
  return useQuery({
    queryKey: ['directNeighborTopics', topicId],
    queryFn: () => fetchDirectNeighbors(topicId),
  });
};
