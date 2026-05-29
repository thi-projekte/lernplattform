import { apiClient } from './common.ts';
import { useMutation } from '@tanstack/react-query';
import type { PaymentSessionDto, SubscriptionStatus } from '../schemas/payment.ts';
import type { AxiosResponse } from 'axios';

const createCheckoutSessionForSubscription = async (subscriptionStatus: SubscriptionStatus): Promise<AxiosResponse<PaymentSessionDto>> => {
  const formData = new FormData();
  formData.set("subscriptionStatus", subscriptionStatus);
  return await apiClient.post(`/payments/create-checkout-session`, formData, {
    validateStatus: (status) => status <= 204,
  });
};

export const useCreateCheckoutSessionForSubscription = () => {
  return useMutation({
    mutationKey: ['createCheckoutSession'],
    mutationFn: createCheckoutSessionForSubscription,
  });
};