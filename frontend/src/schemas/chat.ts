import { z } from 'zod';
import { StreakDtoSchema } from './streak.ts';

export const MessageSenderDtoSchema = z.object({
  creatorId: z.string(),
  creatorFullName: z.string().nullish(),
  streakToDisplay: StreakDtoSchema.nullish(),
});
export type MessageSenderDto = z.infer<typeof MessageSenderDtoSchema>;

export const ChatMessageDtoSchema = z.object({
  id: z.uuid(),
  message: z.string(),
  sender: MessageSenderDtoSchema,
});
export type ChatMessageDto = z.infer<typeof ChatMessageDtoSchema>;
