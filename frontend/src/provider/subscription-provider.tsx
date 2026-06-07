/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, type ReactNode } from 'react';
import { useFetchSubscription } from '../api/subscription.ts';
import type { SubscriptionStatus } from '../schemas/payment.ts';
import { LoadingOverlay } from '@mantine/core';

interface SubscriptionContextValue {
  subscriptionStatus: SubscriptionStatus;
  canAccessBillingPortal: boolean;
  canLearnTopics: boolean;
  canStartNewTopics: boolean;
  isLoading: boolean;
}

const SubscriptionContext = createContext<SubscriptionContextValue>({
  subscriptionStatus: 'FREE',
  canAccessBillingPortal: false,
  canLearnTopics: true,
  canStartNewTopics: false,
  isLoading: true,
});

export const SubscriptionProvider = ({ children }: { children: ReactNode }) => {
  const { data, isLoading } = useFetchSubscription();

  return (
    <SubscriptionContext
      value={{
        subscriptionStatus: data?.subscriptionStatus ?? 'FREE',
        canAccessBillingPortal: data?.canAccessBillingPortal ?? false,
        canLearnTopics: data?.canLearnTopics ?? false,
        canStartNewTopics: data?.canStartNewTopics ?? false,
        isLoading,
      }}
    >
      <LoadingOverlay zIndex={9999} visible={isLoading} loaderProps={{ type: 'bars' }} />
      {children}
    </SubscriptionContext>
  );
};

export const useSubscription = () => useContext(SubscriptionContext);
