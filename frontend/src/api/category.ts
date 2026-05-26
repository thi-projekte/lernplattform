import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './common.ts';
import { type CategoryRequest, type CategoryTreeDto } from '../schemas/category.ts';

const fetchCategoryTree = async (): Promise<CategoryTreeDto[]> => {
  const result = await apiClient.get('/categories/tree', {
    validateStatus: (status) => status <= 204,
  });
  return result.data as CategoryTreeDto[];
};

export const useQueryCategoryTree = () =>
  useQuery({
    queryKey: ['categoryTree'],
    queryFn: fetchCategoryTree,
  });

export const useCreateCategoryMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CategoryRequest) =>
      apiClient.post('/categories', request, {
        validateStatus: (status) => status <= 204,
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categoryTree'] }),
  });
};

export const useUpdateCategoryMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: CategoryRequest }) =>
      apiClient.put(`/categories/${id}`, request, {
        validateStatus: (status) => status <= 204,
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categoryTree'] }),
  });
};
