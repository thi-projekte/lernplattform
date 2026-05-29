import { useMutation } from '@tanstack/react-query';
import { apiClient } from './common.ts';

type SubscriptionStatus = 'PLUS' | 'PRO';

const createCheckoutSession = async (subscriptionStatus: SubscriptionStatus): Promise<string> => {
  const params = new URLSearchParams({ subscriptionStatus });
  const response = await apiClient.post<{ url: string }>(
    '/payments/create-checkout-session',
    params.toString(),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } },
  );
  return response.data.url;
};

export const useCreateCheckoutSession = () =>
  useMutation({
    mutationFn: (subscriptionStatus: SubscriptionStatus) =>
      createCheckoutSession(subscriptionStatus),
    onSuccess: (url) => {
      window.location.href = url;
    },
  });
