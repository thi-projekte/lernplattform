import { z } from 'zod';

export const InvitationDtoSchema = z.object({
  id: z.string().uuid(),
  creatorId: z.string(),
  createdAt: z.string(),
});

export type InvitationDto = z.infer<typeof InvitationDtoSchema>;

export const PersonalInvitationStatusSchema = z.object({
  invitationsLeft: z.number(),
  invitationsAlreadySent: z.number(),
});

export type PersonalInvitationStatus = z.infer<typeof PersonalInvitationStatusSchema>;

export const PaginatedInvitationDtoSchema = z.object({
  results: z.array(InvitationDtoSchema),
  totalPages: z.number(),
});

export type PaginatedInvitationDto = z.infer<typeof PaginatedInvitationDtoSchema>;
