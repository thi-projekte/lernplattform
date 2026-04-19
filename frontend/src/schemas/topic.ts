import { z } from 'zod';
import i18n from '../i18n.ts';
import { BaseEntitySchema, createPaginatedSchema } from './common.ts';
import { AnyContentElementDtoSchema } from './content-element.ts';

export const CategorySchema = BaseEntitySchema.extend({
  title: z.string(),
  color: z.string().optional(),
});

export type Category = z.infer<typeof CategorySchema>;

export const ListTopicDtoSchema = z.object({
  id: z.uuid(),
  title: z.string(),
  categories: z.array(CategorySchema),
  updatedAt: z.coerce.date(),
  creatorId: z.string(),
  creatorFullName: z.string(),
});

export type ListTopicDto = z.infer<typeof ListTopicDtoSchema>;

export const PaginatedListTopicDtoSchema = createPaginatedSchema(ListTopicDtoSchema);
export type PaginatedListTopicDto = z.infer<typeof PaginatedListTopicDtoSchema>;

export const TopicCoreDataSchema = z.object({
  title: z.string({ error: i18n.t('topic.errors.titleMissing') }),
  teaser: z.string({ error: i18n.t('common.shouldNotBeEmpty') }),
  categories: z
    .array(CategorySchema)
    .min(1, { error: i18n.t('topic.errors.minOneCategory') })
    .max(3, { error: i18n.t('topic.errors.maxThreeCategory') }),
  estimatedLearningDuration: z.number({ error: i18n.t('common.shouldNotBeEmpty') }),
});

export const TopicAssociatedTopicsSchema = z.object({
  relatedTopics: z.array(ListTopicDtoSchema).min(1).max(4),
});

export const TopicContentElementsSchema = z.object({
  contentElements: z.array(AnyContentElementDtoSchema)
});

export const TopicSchema = z
  .object({
    id: z.uuid().optional(),
  })
  .extend(TopicCoreDataSchema.shape)
  .extend(TopicAssociatedTopicsSchema.shape)
  .extend(TopicContentElementsSchema.shape);

export type Topic = z.infer<typeof TopicSchema>;
