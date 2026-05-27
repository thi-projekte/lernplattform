import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './common.ts';
import type { StreakDto, StreakPreferenceDto } from '../schemas/streak.ts';
import type { StreakType } from '../schemas/streak.ts';

const fetchStreaks = async (): Promise<StreakDto[]> => {
  const response = await apiClient.get<StreakDto[]>('/streaks');
  return response.data;
};

export const useFetchStreaks = () =>
  useQuery({
    queryKey: ['streaks'],
    queryFn: fetchStreaks,
  });

const fetchStreakPreferences = async (): Promise<StreakPreferenceDto> => {
  const response = await apiClient.get<StreakPreferenceDto>('/streaks/preferences');
  return response.data;
};

export const useFetchStreakPreferences = () =>
  useQuery({
    queryKey: ['streakPreferences'],
    queryFn: fetchStreakPreferences,
  });

export const useUpdateStreakPreferences = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (type: StreakType) =>
      apiClient.put('/streaks/preferences', { type, isPublic: false }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['streakPreferences'] }),
  });
};
