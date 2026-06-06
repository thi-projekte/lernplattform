import { z } from 'zod';

export const CategoryTreeDtoSchema: z.ZodType<CategoryTreeDto> = z.lazy(() =>
  z.object({
    id: z.string().uuid(),
    title: z.string(),
    color: z.string().optional(),
    path: z.string().optional(),
    children: z.array(CategoryTreeDtoSchema),
  })
);

export interface CategoryTreeDto {
  id: string;
  title: string;
  color?: string;
  path?: string;
  children: CategoryTreeDto[];
}

export const CategoryRequestSchema = z.object({
  title: z.string().min(1),
  color: z.string().min(1),
  parentId: z.string().uuid().optional(),
});

export type CategoryRequest = z.infer<typeof CategoryRequestSchema>;
