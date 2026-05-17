import { z } from 'zod';

export const LearnProgressStatusSchema = z.enum([
  'STARTED',
  'ALL_CONTENT_ELEMENTS_COMPLETED',
  'COMPLETED_MANUALLY',
]);

export type LearnProgressStatus = z.infer<typeof LearnProgressStatusSchema>;

export const TopicLearnProgressDtoSchema = z.object({
  topicId: z.uuid(),
  status: LearnProgressStatusSchema,
  completed: z.boolean(),
  percentageCompleted: z.number(),
  completedContentElementIds: z.array(z.uuid()),
});

export type TopicLearnProgressDto = z.infer<typeof TopicLearnProgressDtoSchema>;
