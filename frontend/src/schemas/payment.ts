import {z} from "zod";

export const SubscriptionStatusSchema = z.enum(["FREE", "PLUS", "PRO"]);

export type SubscriptionStatus = z.infer<typeof SubscriptionStatusSchema>;

export const StripeSessionDtoSchema = z.object({
  url: z.string()
});

export type StripeSessionDto = z.infer<typeof StripeSessionDtoSchema>;