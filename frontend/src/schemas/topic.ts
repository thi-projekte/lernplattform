import { z } from 'zod';
import i18n from '../i18n.ts';
import { createPaginatedSchema } from './common.ts';
import { AnyContentElementDtoSchema, ImportableContentElements } from './content-element.ts';
import { TopicLearnProgressDtoSchema } from './learn-progress.ts';

export const CategorySchema = z.object({
  id: z.string().uuid(),
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
  teaser: z
    .string({ error: i18n.t('common.shouldNotBeEmpty') })
    .max(512, { error: i18n.t('topic.errors.teaserTooLong') }),
  categories: z
    .array(CategorySchema)
    .min(1, { error: i18n.t('topic.errors.minOneCategory') })
    .max(3, { error: i18n.t('topic.errors.maxThreeCategory') }),
  estimatedLearningDuration: z.number({ error: i18n.t('common.shouldNotBeEmpty') }),
});

export const TopicAssociatedTopicsSchema = z.object({
  relatedTopics: z.array(ListTopicDtoSchema).min(1).max(3),
});

export const TopicContentElementsSchema = z.object({
  contentElements: z.array(AnyContentElementDtoSchema).min(0),
});

export const IndexCardDtoSchema = z.object({
  id: z.uuid().optional(),
  question: z.string().min(1),
  answer: z.string().min(1),
});
export type IndexCardDto = z.infer<typeof IndexCardDtoSchema>;

export const TopicIndexCardsSchema = z.object({
  indexCards: z.array(IndexCardDtoSchema).max(20),
});

export const TopicSchema = z
  .object({
    id: z.uuid().optional(),
    creatorId: z.string().optional(),
    creatorFullName: z.string().optional(),
    learnProgress: TopicLearnProgressDtoSchema.nullish(),
  })
  .extend(TopicCoreDataSchema.shape)
  .extend(TopicAssociatedTopicsSchema.shape)
  .extend(TopicContentElementsSchema.shape)
  .extend(TopicIndexCardsSchema.shape);

export type Topic = z.infer<typeof TopicSchema>;

const ImportTopicSchema = z.object({
  identifier: z.string().trim().min(1, 'Identifier cannot be blank'),
  title: z.string().trim().min(1, 'Title cannot be blank'),
  teaser: z.string().trim().min(1, 'Teaser cannot be blank'),

  categories: z
    .array(z.string().trim().min(1))
    .min(1, 'Must contain at least 1 category reference')
    .max(2, 'Cannot exceed 2 category entries'),

  duration: z.number().int('Duration must be a whole integer'),

  contentElements: z
    .array(ImportableContentElements)
    .min(1, 'Must have at least 1 content element')
    .max(12, 'Cannot exceed 12 nested content elements'),
});

export const FullImportSchema = z.object({
  topics: z.array(ImportTopicSchema).min(1, 'Topics array cannot be empty'),

  associations: z.record(
    z.string().trim().min(1),
    z
      .array(z.string().trim().min(1))
      .min(1, 'Association targets must contain at least 1 relation link')
      .max(3, 'Associations map value list caps out at a depth of 3 items')
  ),
});
