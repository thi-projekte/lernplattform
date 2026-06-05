import { z } from 'zod';

export const ChallengeTypeSchema = z.enum(['WEEKLY']);

export const ChallengeDtoSchema = z.object({
  id: z.string(),
  type: ChallengeTypeSchema,
  startDate: z.string(),
  endDate: z.string(),
  targetCount: z.number(),
  currentCount: z.number(),
  completed: z.boolean(),
  rewardClaimed: z.boolean(),
});

export type ChallengeDto = z.infer<typeof ChallengeDtoSchema>;
