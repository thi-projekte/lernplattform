import { z } from 'zod';

export const SubscriptionStatusSchema = z.enum(['FREE', 'PREMIUM']);
export type SubscriptionStatus = z.infer<typeof SubscriptionStatusSchema>;

export const StripeSessionDtoSchema = z.object({ url: z.string() });
export type StripeSessionDto = z.infer<typeof StripeSessionDtoSchema>;

export const LimitsDtoSchema = z.object({
  dailyLearningLimit: z.number(),
  parallelTopics: z.number(),
});
export type LimitsDto = z.infer<typeof LimitsDtoSchema>;

export const SubscriptionDtoSchema = z.object({
  creatorId: z.string(),
  subscriptionStatus: SubscriptionStatusSchema,
  canAccessBillingPortal: z.boolean(),
  canLearnTopics: z.boolean(),
  canStartNewTopics: z.boolean(),
});
export type SubscriptionDto = z.infer<typeof SubscriptionDtoSchema>;

export const PriceDtoSchema = z.object({
  id: z.string(),
  interval: z.enum(['month', 'year']),
  amount: z.number(),
});
export type PriceDto = z.infer<typeof PriceDtoSchema>;

export const ProductFeatureSchema = z.object({
  entitlementFeature: z
    .object({
      name: z.string(),
      lookupKey: z.string().optional().nullable(),
    })
    .optional()
    .nullable(),
});
export type ProductFeature = z.infer<typeof ProductFeatureSchema>;

export const ProductDtoSchema = z.object({
  title: z.string(),
  subscriptionStatus: SubscriptionStatusSchema,
  canHaveTrial: z.boolean(),
  prices: z.array(PriceDtoSchema),
  features: z.array(ProductFeatureSchema).optional().default([]),
});
export type ProductDto = z.infer<typeof ProductDtoSchema>;
