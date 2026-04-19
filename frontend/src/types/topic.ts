import { z } from 'zod';
import { BaseEntitySchema, createPaginatedSchema } from './common.ts';

const CategorySchema = BaseEntitySchema.extend({
  title: z.string(),
  color: z.string().optional(),
});

export type Category = z.infer<typeof CategorySchema>;

const ListTopicDtoSchema = z.object({
  id: z.uuid(),
  title: z.string(),
  categories: z.array(CategorySchema),
  updatedAt: z.iso.datetime().pipe(z.coerce.date()),
});

export type ListTopicDto = z.infer<typeof ListTopicDtoSchema>;

export const PaginatedListTopicDtoSchema = createPaginatedSchema(ListTopicDtoSchema);
export type PaginatedListTopicDto = z.infer<typeof PaginatedListTopicDtoSchema>;
