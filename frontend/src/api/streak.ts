import { useQuery } from '@tanstack/react-query';
import { apiClient } from './common.ts';
import type { StreakDto } from '../schemas/streak.ts';

const fetchStreaks = async (): Promise<StreakDto[]> => {
  const response = await apiClient.get<StreakDto[]>('/streaks');
  return response.data;
};

export const useFetchStreaks = () =>
  useQuery({
    queryKey: ['streaks'],
    queryFn: fetchStreaks,
  });
