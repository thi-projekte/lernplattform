import { apiClient } from './common.ts';
import { useMutation } from '@tanstack/react-query';
import type { StripeSessionDto, SubscriptionStatus } from '../schemas/payment.ts';
import type { AxiosResponse } from 'axios';

const createCheckoutSessionForSubscription = async (
  subscriptionStatus: SubscriptionStatus
): Promise<AxiosResponse<StripeSessionDto>> => {
  return await apiClient.post(
    `/payments/subscribe`,
    { subscriptionStatus },
    {
      validateStatus: (status) => status <= 204,
    }
  );
};

export const useCreateInitialCheckoutSessionForSubscription = () => {
  return useMutation({
    mutationKey: ['createCheckoutSession'],
    mutationFn: createCheckoutSessionForSubscription,
  });
};

const createBillingPortalSession = async (): Promise<AxiosResponse<StripeSessionDto>> => {
  return await apiClient.post(
    `/subscriptions/billing-portal-session`,
    { },
    {
      validateStatus: (status) => status <= 204,
    }
  );
};

export const useCreateBillingPortalSession = () => {
  return useMutation({
    mutationKey: ['createBillingPortalSession'],
    mutationFn: createBillingPortalSession,
  });
};