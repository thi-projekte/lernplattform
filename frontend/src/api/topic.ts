import { apiClient } from './common.ts';
import {
  type Category,
  CategorySchema,
  type ListTopicDto,
  ListTopicDtoSchema,
  type PaginatedListTopicDto,
  PaginatedListTopicDtoSchema,
  type Topic,
  TopicCoreDataSchema,
  TopicSchema,
} from '../schemas/topic.ts';
import type { PaginationState } from '@tanstack/react-table';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { z } from 'zod';
import {
  type AnyContentElementDto,
  AnyContentElementDtoSchema,
  type AnyContentElementRequest,
} from '../schemas/content-element.ts';

const fetchCategories = async (search: string): Promise<Category[]> => {
  const result = await apiClient.get(`/categories/search?query=${search}`, {
    validateStatus: (status) => status <= 204,
  });
  return z.array(CategorySchema).parse(result.data);
};

export const useQueryCategories = (search: string) => {
  return useQuery({
    queryKey: ['categories', search],
    enabled: !!search,
    queryFn: () => fetchCategories(search),
  });
};

const fetchPersonalTopicsPaginated = async (
  page: number,
  pageSize: number
): Promise<PaginatedListTopicDto> => {
  const result = await apiClient.get(`/topics/personal?page=${page}&pageSize=${pageSize}`, {
    validateStatus: (status) => status <= 204,
  });
  return PaginatedListTopicDtoSchema.parse(result.data);
};

export const useQueryPersonalTopicsPaginated = (pagination: PaginationState) => {
  return useQuery({
    queryKey: ['personalTopics', pagination.pageSize, pagination.pageIndex],
    queryFn: () => fetchPersonalTopicsPaginated(pagination.pageIndex, pagination.pageSize),
  });
};

const fetchTopicsList = async (search: string): Promise<ListTopicDto[]> => {
  const result = await apiClient.get(`/topics?search=${encodeURIComponent(search)}`, {
    validateStatus: (status) => status <= 204,
  });
  return z.array(ListTopicDtoSchema).parse(result.data);
};

export const useQuerySearchTopic = (search: string) => {
  return useQuery({
    queryKey: ['topics', search],
    enabled: search.trim().length > 0,
    queryFn: () => fetchTopicsList(search),
  });
};

const createContentElement = async (
  request: AnyContentElementRequest,
  file: File | null
): Promise<AnyContentElementDto> => {
  const formData = new FormData();
  const jsonBlob = new Blob([JSON.stringify(request)], {
    type: 'application/json',
  });
  formData.set('request', jsonBlob);
  if (file !== null) {
    formData.set('file', file);
  }

  const result = await apiClient.post('/content-elements', formData, {
    validateStatus: (status) => status <= 201,
  });

  return AnyContentElementDtoSchema.parse(result.data);
};

export const useCreateContentElementMutation = () =>
  useMutation({
    mutationKey: ['createContentElement'],
    mutationFn: ({ request, file }: { request: AnyContentElementRequest; file: File }) =>
      createContentElement(request, file),
  });

const deleteContentElement = async (elementId: string) =>
  await apiClient.delete(`/content-elements/${elementId}`, {
    validateStatus: (status) => status === 200 || status === 204,
  });

export const useDeleteContentElementMutation = () =>
  useMutation({
    mutationKey: ['deleteContentElement'],
    mutationFn: deleteContentElement,
  });

const createTopic = async (createTopic: Partial<Topic>) => {
  const result = await apiClient.post('/topics', createTopic, {
    validateStatus: (status) => status <= 204,
  });

  return TopicCoreDataSchema.parse(result.data);
};

export const useCreateTopicMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ['createTopic'],
    mutationFn: createTopic,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
    },
  });
};

const deleteTopic = async (topicId: string) =>
  await apiClient.delete(`/topics/${topicId}`, {
    validateStatus: (status) => status === 200,
  });

export const useDeleteTopicMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ['deleteTopic'],
    mutationFn: deleteTopic,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
    },
  });
};

const fetchTopic = async (topicId: string, withOwnedRelatedTopics: boolean) => {
  const result = await apiClient.get(
    `/topics/${topicId}?withOwnedRelatedTopics=${withOwnedRelatedTopics}`,
    {
      validateStatus: (status) => status <= 204,
    }
  );

  if (false === withOwnedRelatedTopics) {
    return TopicSchema.omit({ relatedTopics: true }).parse(result.data);
  }

  return TopicSchema.parse(result.data);
};

export const useQueryTopic = (topicId: string, withOwnedRelatedTopics: boolean, enabled = true) => {
  return useQuery({
    queryKey: ['topic', topicId, withOwnedRelatedTopics],
    enabled: enabled && topicId.length > 0,
    queryFn: () => fetchTopic(topicId, withOwnedRelatedTopics),
  });
};

const editTopic = async (topicId: string, topic: Partial<Topic>) => {
  return await apiClient.put(`/topics/${topicId}`, topic, {
    validateStatus: (status) => status <= 204,
  });
};

export const useEditTopicMutation = (topicId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ['editTopic', topicId],
    mutationFn: (topic: Partial<Topic>) => editTopic(topicId, topic),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['topic', topicId] });
    },
  });
};
