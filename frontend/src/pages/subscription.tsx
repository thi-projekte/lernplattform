import {
  Badge,
  Box,
  Button,
  Container,
  Divider,
  Group,
  List,
  Paper,
  Stack,
  Text,
  ThemeIcon,
} from '@mantine/core';
import { IconCheck, IconCrown, IconExternalLink, IconRocket, IconStar } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import {
  useCreateBillingPortalSession,
  useCreateInitialCheckoutSessionForSubscription,
} from '../api/subscription.ts';
import { useSubscription } from '../provider/subscription-provider.tsx';
import { notifications } from '@mantine/notifications';
import type { SubscriptionStatus } from '../schemas/payment.ts';

interface PlanCardProps {
  plan: SubscriptionStatus;
  title: string;
  price: string;
  priceSuffix?: string;
  description: string;
  features: string[];
  badge?: string;
  badgeColor?: string;
  accentColor: string;
  icon: React.ReactNode;
  currentPlan: SubscriptionStatus;
  canAccessBillingPortal: boolean;
  onSubscribe: (plan: 'PLUS' | 'PRO') => void;
  onBillingPortal: () => void;
  isSubscribing: boolean;
  isSubscribingPlan?: 'PLUS' | 'PRO';
  isBillingPortalLoading: boolean;
}

const PlanCard = ({
  plan,
  title,
  price,
  priceSuffix,
  description,
  features,
  badge,
  badgeColor,
  accentColor,
  icon,
  currentPlan,
  canAccessBillingPortal,
  onSubscribe,
  onBillingPortal,
  isSubscribing,
  isSubscribingPlan,
  isBillingPortalLoading,
}: PlanCardProps) => {
  const { t } = useTranslation();
  const isCurrent = currentPlan === plan;

  return (
    <Paper
      p={28}
      radius="xl"
      style={{
        border: isCurrent ? `2px solid ${accentColor}` : '1.5px solid var(--card-border)',
        background: isCurrent
          ? `linear-gradient(160deg, color-mix(in srgb, ${accentColor} 8%, var(--card-bg)) 0%, var(--card-bg) 100%)`
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
      {badge && !isCurrent && (
        <Badge
          size="sm"
          style={{
            position: 'absolute',
            top: 16,
            right: 16,
            background: badgeColor,
            color: '#fff',
            fontWeight: 700,
          }}
        >
          {badge}
        </Badge>
      )}
      {isCurrent && (
        <Badge
          size="sm"
          style={{
            position: 'absolute',
            top: 16,
            right: 16,
            background: accentColor,
            color: '#fff',
            fontWeight: 700,
          }}
        >
          {t('subscription.currentPlan')}
        </Badge>
      )}

      <Stack gap="md" style={{ flex: 1 }}>
        <Group gap="sm">
          <ThemeIcon
            size={40}
            radius="xl"
            style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
          >
            <span style={{ color: accentColor, display: 'flex', alignItems: 'center' }}>
              {icon}
            </span>
          </ThemeIcon>
          <Text fw={800} size="lg">
            {title}
          </Text>
        </Group>

        <Box>
          <Group align="baseline" gap={4}>
            <Text fw={800} style={{ fontSize: 32, lineHeight: 1, color: accentColor }}>
              {price}
            </Text>
            {priceSuffix && (
              <Text size="sm" c="dimmed">
                {priceSuffix}
              </Text>
            )}
          </Group>
          <Text size="sm" c="dimmed" mt={4}>
            {description}
          </Text>
        </Box>

        <Divider opacity={0.4} />

        <List
          spacing="xs"
          size="sm"
          style={{ flex: 1 }}
          icon={
            <ThemeIcon
              size={18}
              radius="xl"
              style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
            >
              <IconCheck size={11} style={{ color: accentColor }} />
            </ThemeIcon>
          }
        >
          {features.map((f, i) => (
            <List.Item key={i}>{f}</List.Item>
          ))}
        </List>

        {plan === 'FREE' ? (
          <Button
            fullWidth
            radius="md"
            size="sm"
            variant="outline"
            disabled
            style={{ marginTop: 'auto', borderColor: accentColor, color: accentColor }}
          >
            {t('subscription.free.button')}
          </Button>
        ) : isCurrent ? (
          <Button
            fullWidth
            radius="md"
            size="sm"
            leftSection={<IconExternalLink size={14} />}
            loading={isBillingPortalLoading}
            onClick={onBillingPortal}
            style={{
              marginTop: 'auto',
              background: accentColor,
              color: '#fff',
              fontWeight: 700,
              boxShadow: `0 4px 14px color-mix(in srgb, ${accentColor} 38%, transparent)`,
            }}
          >
            {t('subscription.managePlan')}
          </Button>
        ) : canAccessBillingPortal ? (
          <Button
            fullWidth
            radius="md"
            size="sm"
            variant="outline"
            leftSection={<IconExternalLink size={14} />}
            loading={isBillingPortalLoading}
            onClick={onBillingPortal}
            style={{ marginTop: 'auto', borderColor: accentColor, color: accentColor }}
          >
            {t('subscription.switchPlan')}
          </Button>
        ) : (
          <Button
            fullWidth
            radius="md"
            size="sm"
            loading={isSubscribing && isSubscribingPlan === plan}
            onClick={() => onSubscribe(plan as 'PLUS' | 'PRO')}
            style={{
              marginTop: 'auto',
              background: accentColor,
              color: '#fff',
              fontWeight: 700,
              boxShadow: `0 4px 14px color-mix(in srgb, ${accentColor} 38%, transparent)`,
            }}
          >
            {plan === 'PLUS' ? t('subscription.plus.button') : t('subscription.pro.button')}
          </Button>
        )}
      </Stack>
    </Paper>
  );
};

const SubscriptionPage = () => {
  const { t } = useTranslation();
  const { subscriptionStatus, canAccessBillingPortal } = useSubscription();

  const { mutate: subscribe, isPending: isSubscribing, variables: subscribingPlan } =
    useCreateInitialCheckoutSessionForSubscription();
  const { mutate: openBillingPortal, isPending: isBillingPortalLoading } =
    useCreateBillingPortalSession();

  const handleSubscribe = (plan: 'PLUS' | 'PRO') => {
    subscribe(plan, {
      onSuccess: (res) => {
        window.location.href = res.data.url;
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

  const cardProps = {
    currentPlan: subscriptionStatus,
    canAccessBillingPortal,
    onSubscribe: handleSubscribe,
    onBillingPortal: handleBillingPortal,
    isSubscribing,
    isSubscribingPlan: subscribingPlan as 'PLUS' | 'PRO' | undefined,
    isBillingPortalLoading,
  };

  return (
    <Layout>
      <Box py="xl" px="md">
        <Container size="lg">
          <Stack gap="xl">
            <Stack gap="xs" align="center">
              <Text fw={800} ta="center" style={{ fontSize: 28 }}>
                {t('subscription.title')}
              </Text>
              <Text ta="center" c="dimmed" size="md" maw={480}>
                {t('subscription.subtitle')}
              </Text>
            </Stack>

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

            <Group align="stretch" gap="lg" style={{ flexWrap: 'wrap' }}>
              <PlanCard
                plan="FREE"
                title={t('subscription.free.title')}
                price={t('subscription.free.price')}
                description={t('subscription.free.description')}
                features={[
                  t('subscription.free.feature1'),
                  t('subscription.free.feature2'),
                  t('subscription.free.feature3'),
                ]}
                accentColor="#74c0fc"
                icon={<IconStar size={18} />}
                {...cardProps}
              />
              <PlanCard
                plan="PLUS"
                title={t('subscription.plus.title')}
                price={t('subscription.plus.price')}
                priceSuffix={t('subscription.perMonth')}
                description={t('subscription.plus.description')}
                features={[
                  t('subscription.plus.feature1'),
                  t('subscription.plus.feature2'),
                  t('subscription.plus.feature3'),
                  t('subscription.plus.feature4'),
                ]}
                badge={t('subscription.popular')}
                badgeColor="#339af0"
                accentColor="#339af0"
                icon={<IconRocket size={18} />}
                {...cardProps}
              />
              <PlanCard
                plan="PRO"
                title={t('subscription.pro.title')}
                price={t('subscription.pro.price')}
                priceSuffix={t('subscription.perMonth')}
                description={t('subscription.pro.description')}
                features={[
                  t('subscription.pro.feature1'),
                  t('subscription.pro.feature2'),
                  t('subscription.pro.feature3'),
                  t('subscription.pro.feature4'),
                  t('subscription.pro.feature5'),
                ]}
                badge={t('subscription.bestValue')}
                badgeColor="#f59f00"
                accentColor="#f59f00"
                icon={<IconCrown size={18} />}
                {...cardProps}
              />
            </Group>
          </Stack>
        </Container>
      </Box>
    </Layout>
  );
};

export default SubscriptionPage;
