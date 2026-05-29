import {z} from "zod";

export const SubscriptionStatusSchema = z.enum(["FREE", "PLUS", "PRO"]);

export type SubscriptionStatus = z.infer<typeof SubscriptionStatusSchema>;

export const PaymentSessionDtoSchema = z.object({
  url: z.string()
});

export type PaymentSessionDto = z.infer<typeof PaymentSessionDtoSchema>;