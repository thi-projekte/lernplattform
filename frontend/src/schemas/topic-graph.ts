import { z } from 'zod';
import { CategorySchema } from './topic.ts';
import { TopicLearnProgressDtoSchema } from './learn-progress.ts';

export const GraphTopicDtoSchema = z.object({
  id: z.uuid(),
  title: z.string(),
  categories: z.array(CategorySchema),
  creatorId: z.string(),
  creatorFullName: z.string(),
  associatedTopics: z.array(z.uuid()),
  learnProgress: TopicLearnProgressDtoSchema.nullish(),
});

export type GraphTopicDto = z.infer<typeof GraphTopicDtoSchema>;
