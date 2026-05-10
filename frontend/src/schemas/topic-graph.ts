import { z } from 'zod';
import { CategorySchema } from './topic.ts';

export const GraphTopicDtoSchema = z.object({
  id: z.uuid(),
  title: z.string(),
  categories: z.array(CategorySchema),
  creatorId: z.string(),
  creatorFullName: z.string(),
  associatedTopics: z.array(z.uuid()),
});

export type GraphTopicDto = z.infer<typeof GraphTopicDtoSchema>;
