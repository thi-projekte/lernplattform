import { apiClient } from './common.ts';
import { useMutation } from '@tanstack/react-query';

interface CreateTopicAssociationRequest {
  owningTopicId: string;
  foreignTopicId: string;
}

const createAssociation = async (req: CreateTopicAssociationRequest) => {
  return await apiClient.post(`/topic-associations`, req, {
    validateStatus: (status) => status <= 204,
  });
};

export const useCreateAssociation = () => {
  return useMutation({
    mutationKey: ['createAssociation'],
    mutationFn: createAssociation,
  });
};

const deleteAssociation = async (associationId: string) => {
  return await apiClient.delete(`/topic-associations/${associationId}`, {
    validateStatus: (status) => status <= 204,
  });
};

export const useDeleteAssociation = () => {
  return useMutation({
    mutationKey: ['deleteAssociation'],
    mutationFn: deleteAssociation,
  });
};
