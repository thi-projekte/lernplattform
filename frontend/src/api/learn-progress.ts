import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './common.ts';

const startTopic = async (topicId: string) =>
  await apiClient.post(`/learn-progress/topics/${topicId}/start`, undefined, {
    validateStatus: (status) => status === 200,
  });

const completeTopic = async (topicId: string) =>
  await apiClient.post(`/learn-progress/topics/${topicId}/complete`, undefined, {
    validateStatus: (status) => status === 200,
  });

const completeContentElement = async (contentElementId: string) =>
  await apiClient.post(
    `/learn-progress/content-elements/${contentElementId}/complete`,
    undefined,
    {
      validateStatus: (status) => status === 200,
    }
  );

const invalidateProgressQueries = (queryClient: ReturnType<typeof useQueryClient>) => {
  queryClient.invalidateQueries({ queryKey: ['topic'] });
  queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
  queryClient.invalidateQueries({ queryKey: ['mostPopularTopics'] });
  queryClient.invalidateQueries({ queryKey: ['directNeighborTopics'] });
};

export const useStartTopicMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['startTopic'],
    mutationFn: startTopic,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};

export const useCompleteTopicMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['completeTopic'],
    mutationFn: completeTopic,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};

export const useCompleteContentElementMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['completeContentElement'],
    mutationFn: completeContentElement,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};
