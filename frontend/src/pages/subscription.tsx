import {
  Badge,
  Box,
  Button,
  Container,
  Divider,
  Group,
  Paper,
  Stack,
  Text,
  ThemeIcon,
  UnstyledButton,
} from '@mantine/core';
import { useMediaQuery } from '@mantine/hooks';
import { IconCheck, IconCrown, IconExternalLink, IconStar } from '@tabler/icons-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import {
  useCreateBillingPortalSession,
  useCreateInitialCheckoutSessionForSubscription,
  useCreateInitialCheckoutSessionForTrial,
  useFetchProducts,
} from '../api/subscription.ts';
import { useSubscription } from '../provider/subscription-provider.tsx';
import { notifications } from '@mantine/notifications';
import type { PriceDto, ProductDto, SubscriptionStatus } from '../schemas/payment.ts';
import { useQueryClient } from '@tanstack/react-query';
import LayoutLoader from '../components/layout-loader.tsx';

type Interval = 'month' | 'year';

const FREE_COLOR = '#74c0fc';
const PAID_COLOR = '#f59f00';

const getPlanColor = (status: SubscriptionStatus) => (status === 'FREE' ? FREE_COLOR : PAID_COLOR);

const getPlanIcon = (status: SubscriptionStatus) =>
  status === 'FREE' ? <IconStar size={18} /> : <IconCrown size={18} />;

interface IntervalToggleProps {
  value: Interval;
  onChange: (value: Interval) => void;
  savingsPercent: number;
}

const IntervalToggle = ({ value, onChange, savingsPercent }: IntervalToggleProps) => {
  const { t } = useTranslation();
  const options: { value: Interval; label: string }[] = [
    { value: 'month', label: t('subscription.monthly') },
    { value: 'year', label: t('subscription.yearly') },
  ];

  return (
    <Box
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: 4,
        borderRadius: 999,
        background: 'var(--card-border)',
        gap: 4,
      }}
    >
      {options.map((option) => {
        const isActive = value === option.value;
        const showSavings = option.value === 'year' && savingsPercent > 0;
        return (
          <UnstyledButton
            key={option.value}
            onClick={() => onChange(option.value)}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 8,
              padding: '8px 18px',
              borderRadius: 999,
              background: isActive ? 'var(--card-bg)' : 'transparent',
              boxShadow: isActive ? '0 2px 8px rgba(0,0,0,0.08)' : 'none',
              transition: 'background 120ms ease',
            }}
          >
            <Text fw={600} size="sm" c={isActive ? undefined : 'dimmed'}>
              {option.label}
            </Text>
            {showSavings && (
              <Badge size="xs" color="green" variant="filled" radius="sm">
                {t('subscription.savePercent', { percent: savingsPercent })}
              </Badge>
            )}
          </UnstyledButton>
        );
      })}
    </Box>
  );
};

const formatAmount = (amount: number) => {
  const formatted = amount % 1 === 0 ? amount.toFixed(0) : amount.toFixed(2);
  return `€${formatted}`;
};

interface ProductCardProps {
  product: ProductDto;
  selectedInterval: Interval;
  currentPlan: SubscriptionStatus;
  canAccessBillingPortal: boolean;
  onSubscribe: (priceId: string) => void;
  onCreateTrial: (priceId: string) => void;
  onBillingPortal: () => void;
  isSubscribing: boolean;
  subscribingPriceId?: string;
  isBillingPortalLoading: boolean;
}

const ProductCard = ({
  product,
  selectedInterval,
  currentPlan,
  canAccessBillingPortal,
  onSubscribe,
  onCreateTrial,
  onBillingPortal,
  isSubscribing,
  subscribingPriceId,
  isBillingPortalLoading,
}: ProductCardProps) => {
  const { t } = useTranslation();
  const isCurrent = currentPlan === product.subscriptionStatus;
  const accentColor = getPlanColor(product.subscriptionStatus);
  const icon = getPlanIcon(product.subscriptionStatus);

  const monthlyPrice = product.prices.find((p) => p.interval === 'month');
  const yearlyPrice = product.prices.find((p) => p.interval === 'year');
  const selectedPrice: PriceDto | undefined =
    selectedInterval === 'year' ? (yearlyPrice ?? monthlyPrice) : (monthlyPrice ?? yearlyPrice);

  const displayAmount =
    selectedInterval === 'year' && yearlyPrice
      ? yearlyPrice.amount / 12
      : (selectedPrice?.amount ?? 0);

  const featureNames = (product.features ?? [])
    .map((f) => f.entitlementFeature?.name)
    .filter((name): name is string => !!name);

  return (
    <Paper
      p="lg"
      radius="lg"
      style={{
        border: isCurrent ? `2px solid ${accentColor}` : '1.5px solid var(--card-border)',
        background: isCurrent
          ? `linear-gradient(160deg, color-mix(in srgb, ${accentColor} 10%, var(--card-bg)) 0%, var(--card-bg) 100%)`
          : 'var(--card-bg)',
        boxShadow: isCurrent
          ? `0 8px 32px color-mix(in srgb, ${accentColor} 18%, transparent)`
          : '0 2px 12px rgba(0,0,0,0.06)',
        flex: 1,
        minWidth: 0,
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
      }}
    >
      {isCurrent && (
        <Badge
          size="sm"
          style={{
            position: 'absolute',
            top: 14,
            right: 14,
            background: accentColor,
            color: '#fff',
            fontWeight: 700,
          }}
        >
          {t('subscription.currentPlan')}
        </Badge>
      )}

      <Stack gap="sm" style={{ flex: 1 }}>
        <Group gap="sm">
          <ThemeIcon
            size={36}
            radius="xl"
            style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
          >
            <span style={{ color: accentColor, display: 'flex', alignItems: 'center' }}>
              {icon}
            </span>
          </ThemeIcon>
          <Text fw={800} size="lg">
            {product.title}
          </Text>
        </Group>

        <Box>
          <Group align="baseline" gap={4}>
            <Text fw={800} style={{ fontSize: 30, lineHeight: 1, color: accentColor }}>
              {formatAmount(displayAmount)}
            </Text>
            <Text size="sm" c="dimmed">
              {t('subscription.perMonth')}
            </Text>
          </Group>
          {selectedInterval === 'year' && yearlyPrice && (
            <Text size="xs" c="dimmed" mt={4}>
              {t('subscription.billedAnnually', { total: formatAmount(yearlyPrice.amount) })}
            </Text>
          )}
          {selectedInterval === 'month' && (
            <Text size="xs" c="dimmed" mt={4}>
              {t('subscription.billedMonthly')}
            </Text>
          )}
        </Box>

        <Divider opacity={0.3} mt="xs" />

        {featureNames.length > 0 ? (
          <Stack gap={6}>
            {featureNames.map((name) => (
              <Group key={name} gap={8} wrap="nowrap" align="center">
                <ThemeIcon
                  size={18}
                  radius="xl"
                  style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
                >
                  <IconCheck size={12} style={{ color: accentColor }} />
                </ThemeIcon>
                <Text size="sm">{name}</Text>
              </Group>
            ))}
          </Stack>
        ) : (
          <Text size="sm" c="dimmed">
            {t('subscription.paidTagline')}
          </Text>
        )}

        <Stack gap="xs" mt="xs">
          {product.canHaveTrial && !isCurrent && selectedPrice && (
            <Button
              fullWidth
              radius="md"
              size="sm"
              variant="outline"
              loading={isSubscribing && subscribingPriceId === selectedPrice.id}
              onClick={() => onCreateTrial(selectedPrice.id)}
              style={{ borderColor: accentColor, color: accentColor }}
            >
              {t('subscription.startTrial')}
            </Button>
          )}

          {isCurrent ? (
            <Button
              fullWidth
              radius="md"
              size="sm"
              leftSection={<IconExternalLink size={14} />}
              loading={isBillingPortalLoading || isSubscribing}
              onClick={onBillingPortal}
              style={{
                background: accentColor,
                color: '#fff',
                fontWeight: 700,
                boxShadow: `0 4px 14px color-mix(in srgb, ${accentColor} 38%, transparent)`,
              }}
            >
              {t('subscription.managePlan')}
            </Button>
          ) : canAccessBillingPortal && currentPlan !== 'FREE' ? (
            <Button
              fullWidth
              radius="md"
              size="sm"
              variant="outline"
              leftSection={<IconExternalLink size={14} />}
              loading={isBillingPortalLoading || isSubscribing}
              onClick={onBillingPortal}
              style={{ borderColor: accentColor, color: accentColor }}
            >
              {t('subscription.switchPlan')}
            </Button>
          ) : (
            selectedPrice && (
              <Button
                fullWidth
                radius="md"
                size="sm"
                loading={isSubscribing && subscribingPriceId === selectedPrice.id}
                onClick={() => onSubscribe(selectedPrice.id)}
                style={{
                  background: accentColor,
                  color: '#fff',
                  fontWeight: 700,
                  boxShadow: `0 4px 14px color-mix(in srgb, ${accentColor} 38%, transparent)`,
                }}
              >
                {t('subscription.subscribeNow')}
              </Button>
            )
          )}
        </Stack>
      </Stack>
    </Paper>
  );
};

const FreeCard = ({ currentPlan }: { currentPlan: SubscriptionStatus }) => {
  const { t } = useTranslation();
  const isCurrent = currentPlan === 'FREE';
  const accentColor = FREE_COLOR;

  return (
    <Paper
      p="lg"
      radius="lg"
      style={{
        border: isCurrent ? `2px solid ${accentColor}` : '1.5px solid var(--card-border)',
        background: isCurrent
          ? `linear-gradient(160deg, color-mix(in srgb, ${accentColor} 10%, var(--card-bg)) 0%, var(--card-bg) 100%)`
          : 'var(--card-bg)',
        boxShadow: isCurrent
          ? `0 8px 32px color-mix(in srgb, ${accentColor} 18%, transparent)`
          : '0 2px 12px rgba(0,0,0,0.06)',
        flex: 1,
        minWidth: 0,
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
      }}
    >
      {isCurrent && (
        <Badge
          size="sm"
          style={{
            position: 'absolute',
            top: 14,
            right: 14,
            background: accentColor,
            color: '#fff',
            fontWeight: 700,
          }}
        >
          {t('subscription.currentPlan')}
        </Badge>
      )}
      <Stack gap="sm" style={{ flex: 1 }}>
        <Group gap="sm">
          <ThemeIcon
            size={36}
            radius="xl"
            style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
          >
            <span style={{ color: accentColor, display: 'flex', alignItems: 'center' }}>
              <IconStar size={18} />
            </span>
          </ThemeIcon>
          <Text fw={800} size="lg">
            {t('subscription.free.title')}
          </Text>
        </Group>
        <Box>
          <Group align="baseline" gap={4}>
            <Text fw={800} style={{ fontSize: 30, lineHeight: 1, color: accentColor }}>
              €0
            </Text>
            <Text size="sm" c="dimmed">
              {t('subscription.perMonth')}
            </Text>
          </Group>
          <Text size="sm" c="dimmed" mt={4}>
            {t('subscription.free.description')}
          </Text>
        </Box>
      </Stack>
    </Paper>
  );
};

const SubscriptionPage = () => {
  const { t } = useTranslation();
  const { subscriptionStatus, canAccessBillingPortal } = useSubscription();
  const [selectedInterval, setSelectedInterval] = useState<Interval>('month');
  const isMobile = useMediaQuery('(max-width: 768px)');

  const queryClient = useQueryClient();

  const { data: products = [], isLoading: productsLoading } = useFetchProducts();

  const {
    mutate: subscribe,
    isPending: isSubscribing,
    variables: subscribingPriceId,
  } = useCreateInitialCheckoutSessionForSubscription();

  const { mutate: createTrial, isPending: isCreatingTrial } =
    useCreateInitialCheckoutSessionForTrial();
  const { mutate: openBillingPortal, isPending: isBillingPortalLoading } =
    useCreateBillingPortalSession();

  const handleSubscribe = (priceId: string) => {
    subscribe(priceId, {
      onSuccess: (res) => {
        window.location.href = res.data.url;
      },
      onError: () => notifications.show({ color: 'red', message: t('common.serverError') }),
    });
  };

  const handleCreateTrial = (priceId: string) => {
    createTrial(priceId, {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['subscription'] });
      },
      onError: () => notifications.show({ color: 'red', message: t('common.serverError') }),
    });
  };

  const handleBillingPortal = () => {
    openBillingPortal(undefined, {
      onSuccess: (res) => {
        window.location.href = res.data.url;
      },
      onError: () => notifications.show({ color: 'red', message: t('common.serverError') }),
    });
  };

  const hasYearlyPrices = products.some((p) => p.prices.some((price) => price.interval === 'year'));

  const maxSavingsPercent = products.reduce((max, product) => {
    const monthly = product.prices.find((p) => p.interval === 'month');
    const yearly = product.prices.find((p) => p.interval === 'year');
    if (!monthly || !yearly) return max;
    const saving = Math.round((1 - yearly.amount / 12 / monthly.amount) * 100);
    return Math.max(max, saving);
  }, 0);

  const cardProps = {
    currentPlan: subscriptionStatus,
    canAccessBillingPortal,
    onSubscribe: handleSubscribe,
    onBillingPortal: handleBillingPortal,
    isSubscribing: isSubscribing || isCreatingTrial,
    subscribingPriceId: subscribingPriceId as string | undefined,
    isBillingPortalLoading,
    selectedInterval,
    onCreateTrial: handleCreateTrial,
  };

  if (productsLoading) {
    return <LayoutLoader />;
  }

  return (
    <Layout>
      <Box py="xl" px="md">
        <Container size="lg">
          <Stack gap="xl">
            <Stack gap="xs" align="center">
              <Text fw={800} ta="center" style={{ fontSize: 28 }}>
                {t('subscription.title')}
              </Text>
              <Text ta="center" c="dimmed" size="sm" maw={420}>
                {t('subscription.subtitle')}
              </Text>
            </Stack>

            {hasYearlyPrices && (
              <Group justify="center">
                <IntervalToggle
                  value={selectedInterval}
                  onChange={setSelectedInterval}
                  savingsPercent={maxSavingsPercent}
                />
              </Group>
            )}

            {canAccessBillingPortal && subscriptionStatus === 'FREE' && (
              <Group justify="center">
                <Button
                  variant="subtle"
                  size="sm"
                  leftSection={<IconExternalLink size={14} />}
                  loading={isBillingPortalLoading}
                  onClick={handleBillingPortal}
                >
                  {t('subscription.billingPortal')}
                </Button>
              </Group>
            )}

            <Group
              align="stretch"
              gap="lg"
              style={{ flexWrap: 'wrap', flexDirection: isMobile ? 'column' : 'row' }}
            >
              <FreeCard currentPlan={subscriptionStatus} />
              {products.map((product) => (
                <ProductCard key={product.title} product={product} {...cardProps} />
              ))}
            </Group>
          </Stack>
        </Container>
      </Box>
    </Layout>
  );
};

export default SubscriptionPage;
