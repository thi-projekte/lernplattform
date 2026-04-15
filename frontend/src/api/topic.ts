import { apiClient } from './common.ts';
import {
  type Category,
  CategorySchema,
  type ListTopicDto,
  ListTopicDtoSchema,
  type PaginatedListTopicDto,
  PaginatedListTopicDtoSchema,
} from '../schemas/topic.ts';
import type { PaginationState } from '@tanstack/react-table';
import { useQuery } from '@tanstack/react-query';
import { z } from 'zod';

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

const fetchTopicsList = async (
  search: string
): Promise<ListTopicDto[]> => {
  const result = await apiClient.get(`/topics/personal?search=${search}`, {
    validateStatus: (status) => status <= 204,
  });
  return z.array(ListTopicDtoSchema).parse(result.data);
};

export const useQuerySearchTopic = (search: string) => {
  return useQuery({
    queryKey: ['topics', search],
    queryFn: () => fetchTopicsList(search)
  });
};
