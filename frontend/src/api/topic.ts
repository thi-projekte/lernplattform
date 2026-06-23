import { apiClient, safeValidateApiResponseContent } from './common.ts';
import {
  type Category,
  CategorySchema,
  IndexCardDtoSchema,
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
  return safeValidateApiResponseContent(z.array(CategorySchema), result.data);
};

export const useQueryCategories = (search: string) => {
  return useQuery({
    queryKey: ['categories', search],
    enabled: !!search,
    queryFn: () => fetchCategories(search),
  });
};

const fetchAllCategories = async (): Promise<Category[]> => {
  const result = await apiClient.get('/categories', {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(z.array(CategorySchema), result.data);
};

// All selectable categories. Used by the JSON import to list the allowed
// category names in the schema and validate against them (the importer does not
// create categories, so a topic may only reference ones that already exist).
export const useQueryAllCategories = () =>
  useQuery({
    queryKey: ['categories', 'all'],
    queryFn: fetchAllCategories,
  });

const fetchPersonalTopicsPaginated = async (
  page: number,
  pageSize: number
): Promise<PaginatedListTopicDto> => {
  const result = await apiClient.get(`/topics/personal?page=${page}&pageSize=${pageSize}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(PaginatedListTopicDtoSchema, result.data);
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
  return safeValidateApiResponseContent(z.array(ListTopicDtoSchema), result.data);
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

  return safeValidateApiResponseContent(AnyContentElementDtoSchema, result.data);
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

const createIndexCard = async (request: { question: string; answer: string }) => {
  const result = await apiClient.post('/index-cards', request, {
    validateStatus: (status) => status <= 201,
  });
  return safeValidateApiResponseContent(IndexCardDtoSchema, result.data);
};

export const useCreateIndexCardMutation = () =>
  useMutation({
    mutationKey: ['createIndexCard'],
    mutationFn: createIndexCard,
  });

const deleteIndexCard = async (cardId: string) =>
  await apiClient.delete(`/index-cards/${cardId}`, {
    validateStatus: (status) => status === 200 || status === 204,
  });

export const useDeleteIndexCardMutation = () =>
  useMutation({
    mutationKey: ['deleteIndexCard'],
    mutationFn: deleteIndexCard,
  });

const createTopic = async (createTopic: Partial<Topic>) => {
  const result = await apiClient.post('/topics', createTopic, {
    validateStatus: (status) => status <= 204,
  });

  return safeValidateApiResponseContent(
    TopicCoreDataSchema.extend({ id: z.uuid().optional() }),
    result.data
  );
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

  if (!withOwnedRelatedTopics) {
    return safeValidateApiResponseContent(TopicSchema.omit({ relatedTopics: true }), result.data);
  }

  return safeValidateApiResponseContent(TopicSchema, result.data);
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

const importTopics = async (payload: unknown) =>
  await apiClient.post('/topics/import', payload, {
    validateStatus: (status) => status === 201,
  });

export const useImportTopicsMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ['importTopics'],
    mutationFn: importTopics,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['personalTopics'] });
    },
  });
};
