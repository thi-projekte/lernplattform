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
import { IconCheck, IconCrown, IconRocket, IconStar } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import { useCreateBillingPortalSession, useCreateInitialCheckoutSessionForSubscription } from '../api/subscription.ts';
import { type SubscriptionStatus, SubscriptionStatusSchema } from '../schemas/payment.ts';

interface PlanCardProps {
  title: string;
  price: string;
  priceSuffix?: string;
  description: string;
  features: string[];
  badge?: string;
  badgeColor?: string;
  buttonLabel: string;
  subscriptionStatus?: 'PLUS' | 'PRO';
  isCurrentPlan?: boolean;
  accentColor: string;
  icon: React.ReactNode;
  onSubscribe?: () => void;
}

const PlanCard = ({
  title,
  price,
  priceSuffix,
  description,
  features,
  badge,
  badgeColor,
  buttonLabel,
  subscriptionStatus,
  isCurrentPlan,
  accentColor,
  icon,
  onSubscribe
}: PlanCardProps) => (
  <Paper
    p={28}
    radius="xl"
    style={{
      border: isCurrentPlan ? `2px solid ${accentColor}` : '1.5px solid var(--card-border)',
      background: isCurrentPlan
        ? `linear-gradient(160deg, color-mix(in srgb, ${accentColor} 8%, var(--card-bg)) 0%, var(--card-bg) 100%)`
        : 'var(--card-bg)',
      boxShadow: isCurrentPlan
        ? `0 8px 32px color-mix(in srgb, ${accentColor} 18%, transparent)`
        : '0 2px 12px rgba(0,0,0,0.06)',
      flex: 1,
      minWidth: 0,
      display: 'flex',
      flexDirection: 'column',
      position: 'relative',
    }}
  >
    {badge && (
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

    <Stack gap="md" style={{ flex: 1 }}>
      <Group gap="sm">
        <ThemeIcon
          size={40}
          radius="xl"
          style={{ background: `color-mix(in srgb, ${accentColor} 18%, transparent)` }}
        >
          <span style={{ color: accentColor, display: 'flex', alignItems: 'center' }}>{icon}</span>
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

      {subscriptionStatus ? (
          <Button
            onClick={onSubscribe}
            fullWidth
            radius="md"
            size="sm"
            style={{
              background: accentColor,
              color: '#fff',
              fontWeight: 700,
              boxShadow: `0 4px 14px color-mix(in srgb, ${accentColor} 38%, transparent)`,
            }}
          >
            {buttonLabel}
          </Button>
      ) : (
        <Button
          fullWidth
          radius="md"
          size="sm"
          variant="outline"
          disabled
          style={{ marginTop: 'auto', borderColor: accentColor, color: accentColor }}
        >
          {buttonLabel}
        </Button>
      )}
    </Stack>
  </Paper>
);

const SubscriptionPage = () => {
  const { t } = useTranslation();

  const {mutateAsync: subscribe} = useCreateInitialCheckoutSessionForSubscription();
  const {mutateAsync: createBillingPortalSession} = useCreateBillingPortalSession();

  const buySubscription = async (status: SubscriptionStatus) => {
    const dto = await subscribe(status);
    window.location.replace(dto.data.url);
  }

  const openBillingPortal = async () => {
    const dto = await createBillingPortalSession();
    window.location.replace(dto.data.url);
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
            <Button onClick={openBillingPortal}>
              BILLING PORTAL HIER LABEL
            </Button>

            <Group align="stretch" gap="lg" style={{ flexWrap: 'wrap' }}>
              <PlanCard
                title={t('subscription.free.title')}
                price={t('subscription.free.price')}
                description={t('subscription.free.description')}
                features={[
                  t('subscription.free.feature1'),
                  t('subscription.free.feature2'),
                  t('subscription.free.feature3'),
                ]}
                buttonLabel={t('subscription.free.button')}
                isCurrentPlan
                accentColor="#74c0fc"
                icon={<IconStar size={18} />}
              />

              <PlanCard
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
                buttonLabel={t('subscription.plus.button')}
                subscriptionStatus="PLUS"
                accentColor="#339af0"
                icon={<IconRocket size={18} />}
                onSubscribe={() => buySubscription(SubscriptionStatusSchema.enum.PLUS)}
              />

              <PlanCard
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
                buttonLabel={t('subscription.pro.button')}
                subscriptionStatus="PRO"
                accentColor="#f59f00"
                icon={<IconCrown size={18} />}
                onSubscribe={() => buySubscription(SubscriptionStatusSchema.enum.PRO)}
              />
            </Group>
          </Stack>
        </Container>
      </Box>
    </Layout>
  );
};

export default SubscriptionPage;
