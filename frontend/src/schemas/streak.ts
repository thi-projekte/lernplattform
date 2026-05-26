import { z } from 'zod';

export const StreakTypeSchema = z.enum(['DAILY', 'WEEKLY', 'MONTHLY']);
export type StreakType = z.infer<typeof StreakTypeSchema>;

export const StreakDtoSchema = z.object({
  id: z.string().uuid(),
  creatorId: z.string(),
  startedAt: z.string(),
  endedAt: z.string().nullable(),
  type: StreakTypeSchema,
  lastContinuedAt: z.string().nullable(),
  isActive: z.boolean(),
  isSatisfied: z.boolean(),
});
export type StreakDto = z.infer<typeof StreakDtoSchema>;

export const StreakPreferenceDtoSchema = z.object({
  creatorId: z.string(),
  type: StreakTypeSchema,
  isPublic: z.boolean(),
});
export type StreakPreferenceDto = z.infer<typeof StreakPreferenceDtoSchema>;
