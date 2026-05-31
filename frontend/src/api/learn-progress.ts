import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './common.ts';

const startTopic = async (topicId: string) =>
  await apiClient.post(`/learn-progress/topics/${topicId}/start`, undefined, {
    validateStatus: (status) => status >= 200 && status < 300,
  });

const completeTopicManually = async (topicId: string) =>
  await apiClient.post(`/learn-progress/topics/${topicId}/complete`, undefined, {
    validateStatus: (status) => status >= 200 && status < 300,
  });

const completeContentElement = async (contentElementId: string) =>
  await apiClient.post(`/learn-progress/content-elements/${contentElementId}/complete`, undefined, {
    validateStatus: (status) => status >= 200 && status < 300,
  });

const resetTopic = async (topicId: string) =>
  await apiClient.post(`/learn-progress/topics/${topicId}/reset`, undefined, {
    validateStatus: (status) => status >= 200 && status < 300,
  });

const resetContentElement = async (contentElementId: string) =>
  await apiClient.post(`/learn-progress/content-elements/${contentElementId}/reset`, undefined, {
    validateStatus: (status) => status >= 200 && status < 300,
  });

const invalidateProgressQueries = (queryClient: ReturnType<typeof useQueryClient>) => {
  queryClient.invalidateQueries({ queryKey: ['topic'] });
  queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
  queryClient.invalidateQueries({ queryKey: ['mostPopularTopics'] });
  queryClient.invalidateQueries({ queryKey: ['directNeighborTopics'] });
  queryClient.invalidateQueries({ queryKey: ['streaks'] });
};

export const useStartTopicMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['startTopic'],
    mutationFn: startTopic,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};

export const useCompleteTopicManuallyMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['completeTopicManually'],
    mutationFn: completeTopicManually,
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

export const useResetTopicMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['resetTopic'],
    mutationFn: resetTopic,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};

export const useResetContentElementMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['resetContentElement'],
    mutationFn: resetContentElement,
    onSuccess: () => invalidateProgressQueries(queryClient),
  });
};
