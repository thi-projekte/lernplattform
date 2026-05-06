import { apiClient } from './common.ts';
import { useMutation } from '@tanstack/react-query';

const makeUserBuilder = async () => {
  return await apiClient.post('/auth/register-as-builder');
};

export const useMakeUserBuilder = () =>
  useMutation({
    mutationKey: ['makeUserBuilder'],
    mutationFn: makeUserBuilder,
  });

const makeUserLearner = async () => {
  return await apiClient.post('/auth/register-as-learner');
};

export const useMakeUserLearner = () =>
  useMutation({
    mutationKey: ['makeUserLearner'],
    mutationFn: makeUserLearner,
  });
