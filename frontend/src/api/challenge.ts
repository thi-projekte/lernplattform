import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { z } from 'zod';
import { apiClient } from './common.ts';
import { ChallengeDtoSchema, type ChallengeDto } from '../schemas/challenge.ts';

const fetchCurrentChallenge = async (): Promise<ChallengeDto> => {
  const res = await apiClient.get('/challenges/current');
  return ChallengeDtoSchema.parse(res.data);
};

const fetchChallengeHistory = async (): Promise<ChallengeDto[]> => {
  const res = await apiClient.get('/challenges/history');
  return z.array(ChallengeDtoSchema).parse(res.data);
};

const claimReward = async (id: string): Promise<ChallengeDto> => {
  const res = await apiClient.post('/challenges/claim/' + id);
  return ChallengeDtoSchema.parse(res.data);
};

export const useFetchCurrentChallenge = () =>
  useQuery({ queryKey: ['challenge', 'current'], queryFn: fetchCurrentChallenge });

export const useFetchChallengeHistory = () =>
  useQuery({ queryKey: ['challenge', 'history'], queryFn: fetchChallengeHistory });

export const useClaimRewardMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: claimReward,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['challenge'] }),
  });
};
