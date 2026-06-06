/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, type ReactNode } from 'react';
import { useFetchSubscription } from '../api/subscription.ts';
import type { SubscriptionStatus } from '../schemas/payment.ts';

interface SubscriptionContextValue {
  subscriptionStatus: SubscriptionStatus;
  canAccessBillingPortal: boolean;
  canLearnTopics: boolean;
  isLoading: boolean;
}

const SubscriptionContext = createContext<SubscriptionContextValue>({
  subscriptionStatus: 'FREE',
  canAccessBillingPortal: false,
  canLearnTopics: true,
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
        isLoading,
      }}
    >
      {children}
    </SubscriptionContext>
  );
};

export const useSubscription = () => useContext(SubscriptionContext);
