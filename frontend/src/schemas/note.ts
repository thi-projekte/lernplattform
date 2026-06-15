import { z } from 'zod';

export const TopicNoteDtoSchema = z.object({
  content: z.string().nullish(),
});
export type TopicNoteDto = z.infer<typeof TopicNoteDtoSchema>;
