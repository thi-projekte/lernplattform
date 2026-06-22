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
  // nullish: a topic may have no quiz cards, and some topic endpoints return
  // `indexCards: null` (the builder/edit mapper does not populate the field).
  // Using .optional() alone rejected null and broke the edit save gate.
  indexCards: z.array(IndexCardDtoSchema).max(20).nullish(),
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

// Validates whether a topic is ready to be saved (create/edit). It mirrors the
// backend TopicRequest constraints and intentionally checks array *counts*
// rather than the full response shape: server-managed fields (content element
// presigned URLs, timestamps, related-topic details) must never gate the save
// button. Using the full TopicSchema here silently disabled saving whenever any
// such nested field failed to parse.
export const TopicSaveableSchema = TopicCoreDataSchema.extend({
  relatedTopics: z.array(z.unknown()).min(1).max(4),
  contentElements: z.array(z.unknown()).min(1).max(12),
  // nullish: the builder/edit endpoint returns `indexCards: null`; the quiz is
  // optional and must never block saving.
  indexCards: z.array(z.unknown()).max(20).nullish(),
});

const ImportIndexCardSchema = z.object({
  question: z.string().trim().min(1, 'Question cannot be blank'),
  answer: z.string().trim().min(1, 'Answer cannot be blank'),
});

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

  // Optional — kept in the parsed output so they reach the backend (Zod strips
  // unknown keys, so omitting this field silently dropped index cards and sent
  // null, which made the importer throw — see issue #331).
  indexCards: z.array(ImportIndexCardSchema).max(20, 'Cannot exceed 20 index cards').optional(),
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

export const buildFullImportSchema = (allowedCategories: string[]) => {
  const allowed = new Set(allowedCategories);
  const categoryField =
    allowedCategories.length > 0
      ? z
          .string()
          .trim()
          .min(1)
          .refine((value) => allowed.has(value), {
            message: `Unknown category. Allowed: ${allowedCategories.join(', ')}`,
          })
      : z.string().trim().min(1);

  const importTopic = ImportTopicSchema.extend({
    categories: z
      .array(categoryField)
      .min(1, 'Must contain at least 1 category reference')
      .max(2, 'Cannot exceed 2 category entries'),
  });

  return z.object({
    topics: z.array(importTopic).min(1, 'Topics array cannot be empty'),
    associations: FullImportSchema.shape.associations,
  });
};
