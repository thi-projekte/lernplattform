import { apiClient, safeValidateApiResponseContent } from './common.ts';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  InvitationDtoSchema,
  PaginatedInvitationDtoSchema,
  PersonalInvitationStatusSchema,
  type InvitationDto,
  type PaginatedInvitationDto,
  type PersonalInvitationStatus,
} from '../schemas/invitation.ts';

const fetchInvitationStatus = async (): Promise<PersonalInvitationStatus> => {
  const result = await apiClient.get('/auth/invitations/status', {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(PersonalInvitationStatusSchema, result.data);
};

export const useQueryInvitationStatus = () =>
  useQuery({
    queryKey: ['invitationStatus'],
    queryFn: fetchInvitationStatus,
  });

const fetchSentInvitations = async (
  page: number,
  pageSize: number
): Promise<PaginatedInvitationDto> => {
  const result = await apiClient.get(`/auth/invitations?page=${page}&pageSize=${pageSize}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(PaginatedInvitationDtoSchema, result.data);
};

export const useQuerySentInvitations = (page = 0, pageSize = 10) =>
  useQuery({
    queryKey: ['sentInvitations', page, pageSize],
    queryFn: () => fetchSentInvitations(page, pageSize),
  });

const fetchInvitation = async (id: string): Promise<InvitationDto> => {
  const result = await apiClient.get(`/auth/invitations/${id}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(InvitationDtoSchema, result.data);
};

export const useQueryInvitation = (id: string) =>
  useQuery({
    queryKey: ['invitation', id],
    enabled: !!id,
    queryFn: () => fetchInvitation(id),
  });

const sendInvitation = async (email: string) =>
  apiClient.post(
    '/auth/invitations',
    { email },
    { validateStatus: (status) => status === 202 }
  );

export const useSendInvitationMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: sendInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invitationStatus'] });
      queryClient.invalidateQueries({ queryKey: ['sentInvitations'] });
    },
  });
};

const redeemInvitation = async ({ id, secret }: { id: string; secret: string }) =>
  apiClient.post(
    `/auth/invitations/${id}/redeem`,
    { secret },
    { validateStatus: (status) => status === 204 }
  );

export const useRedeemInvitationMutation = () =>
  useMutation({
    mutationFn: redeemInvitation,
  });
