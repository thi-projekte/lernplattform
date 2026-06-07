import { apiClient } from './common.ts';
import { useMutation, useQuery } from '@tanstack/react-query';
import type { ProductDto, StripeSessionDto, SubscriptionDto } from '../schemas/payment.ts';
import type { AxiosResponse } from 'axios';

export const useFetchSubscription = () =>
  useQuery({
    queryKey: ['subscription'],
    queryFn: async (): Promise<SubscriptionDto> => {
      const res = await apiClient.get<SubscriptionDto>('/subscriptions');
      return res.data;
    },
  });

export const useFetchProducts = () =>
  useQuery({
    queryKey: ['products'],
    queryFn: async (): Promise<ProductDto[]> => {
      const res = await apiClient.get<ProductDto[]>('/payments/products');
      return res.data;
    },
  });

const createCheckoutSessionForSubscription = async (
  priceId: string
): Promise<AxiosResponse<StripeSessionDto>> => {
  return await apiClient.post(
    `/payments/subscribe`,
    { priceId },
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

const createCheckoutSessionForTrial = async (
  priceId: string
): Promise<AxiosResponse<StripeSessionDto>> => {
  return await apiClient.post(
    `/payments/trial`,
    { priceId },
    {
      validateStatus: (status) => status <= 204,
    }
  );
};

export const useCreateInitialCheckoutSessionForTrial = () => {
  return useMutation({
    mutationKey: ['createCheckoutSessionTrial'],
    mutationFn: createCheckoutSessionForTrial,
  });
};

const createBillingPortalSession = async (): Promise<AxiosResponse<StripeSessionDto>> => {
  return await apiClient.post(
    `/subscriptions/billing-portal-session`,
    {},
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
