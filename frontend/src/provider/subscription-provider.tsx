/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
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

// While the user is returning from a Stripe checkout (URL has ?success=true)
// and the subscription is still FREE, poll the backend every POLL_INTERVAL_MS
// until the status changes (Stripe webhook has been processed) or we hit
// POLL_TIMEOUT_MS as a safety net.
const POLL_INTERVAL_MS = 1500;
const POLL_TIMEOUT_MS = 60_000;

export const SubscriptionProvider = ({ children }: { children: ReactNode }) => {
  // Active while we are returning from a Stripe checkout and the subscription
  // status hasn't flipped from FREE yet. Initialised from the URL once on mount
  // so changes to the URL afterwards don't restart polling.
  const [pollActive, setPollActive] = useState(
    () => new URLSearchParams(window.location.search).get('success') === 'true'
  );

  const { data, isLoading } = useFetchSubscription(pollActive ? POLL_INTERVAL_MS : false);

  const currentStatus = data?.subscriptionStatus ?? 'FREE';

  // Stop polling once the status has flipped from FREE, and clean the URL
  // param so a refresh doesn't re-arm the loop. setState here is intentional —
  // it's the standard pattern for reacting to async query data.
  useEffect(() => {
    if (!pollActive) return;
    if (currentStatus === 'FREE') return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setPollActive(false);
    const url = new URL(window.location.href);
    url.searchParams.delete('success');
    window.history.replaceState({}, '', url.toString());
  }, [pollActive, currentStatus]);

  // Hard timeout via setTimeout so we never poll forever (e.g. webhook never
  // arrives). The callback fires asynchronously, which is what the lint rule
  // about "no setState in effect body" allows.
  useEffect(() => {
    if (!pollActive) return;
    const timeout = window.setTimeout(() => setPollActive(false), POLL_TIMEOUT_MS);
    return () => window.clearTimeout(timeout);
  }, [pollActive]);

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
