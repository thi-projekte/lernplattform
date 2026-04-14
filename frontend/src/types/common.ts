import { z } from 'zod';

export const BaseEntitySchema = z.object({
  id: z.uuid(),
  createdAt: z.iso.datetime(),
  updatedAt: z.iso.datetime(),
});

export const createPaginatedSchema = <T extends z.ZodTypeAny>(itemSchema: T) => {
  return z.object({
    results: z.array(itemSchema),
    totalPages: z.number(),
    hasNextPage: z.boolean(),
    hasPreviousPage: z.boolean(),
  });
};
