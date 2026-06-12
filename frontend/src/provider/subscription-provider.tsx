/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { useFetchSubscription } from '../api/subscription.ts';
import type { SubscriptionStatus } from '../schemas/payment.ts';
import { LoadingOverlay } from '@mantine/core';
import { isGranted, Role } from '../auth.ts';

interface SubscriptionContextValue {
  subscriptionStatus: SubscriptionStatus;
  canAccessBillingPortal: boolean;
  canLearnTopics: boolean;
  canStartNewTopics: boolean;
  isLoading: boolean;
  isPolling: boolean;
  triggerSubscriptionPoll: () => void;
}

const SubscriptionContext = createContext<SubscriptionContextValue>({
  subscriptionStatus: 'FREE',
  canAccessBillingPortal: false,
  canLearnTopics: true,
  canStartNewTopics: false,
  isLoading: true,
  isPolling: false,
  triggerSubscriptionPoll: () => {},
});

// While polling is active and the subscription is still FREE, we ask the
// backend every POLL_INTERVAL_MS until the status changes (Stripe webhook
// processed / trial activated) or POLL_TIMEOUT_MS elapses as a safety net.
const POLL_INTERVAL_MS = 1500;
const POLL_TIMEOUT_MS = 60_000;

export const SubscriptionProvider = ({ children }: { children: ReactNode }) => {
  const isAuthorizedUser = isGranted([Role.AuthorizedUser]);
  // Polling kicks off in two situations:
  //   1. We come back from a Stripe checkout (URL has ?success=true)
  //   2. The user just clicked "Testphase starten" — that API call returns
  //      200 immediately but the actual subscription flip happens via a
  //      webhook, so we have to wait for it.
  const [pollActive, setPollActive] = useState(
    () => new URLSearchParams(window.location.search).get('success') === 'true'
  );

  const triggerSubscriptionPoll = useCallback(() => {
    setPollActive(true);
  }, []);

  const { data, isLoading } = useFetchSubscription(pollActive ? POLL_INTERVAL_MS : false, isAuthorizedUser);

  const currentStatus = data?.subscriptionStatus ?? 'FREE';

  // Stop polling once the status has flipped from FREE, and clean the URL
  // param so a refresh doesn't re-arm the loop.
  useEffect(() => {
    if (!pollActive) return;
    if (currentStatus === 'FREE') return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setPollActive(false);
    const url = new URL(window.location.href);
    if (url.searchParams.has('success')) {
      url.searchParams.delete('success');
      window.history.replaceState({}, '', url.toString());
    }
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
        isPolling: pollActive,
        triggerSubscriptionPoll,
      }}
    >
      <LoadingOverlay zIndex={9999} visible={isLoading} loaderProps={{ type: 'bars' }} />
      {children}
    </SubscriptionContext>
  );
};

export const useSubscription = () => useContext(SubscriptionContext);
