import {
  Anchor,
  Box,
  Container,
  Divider,
  Group,
  List,
  Paper,
  Stack,
  Text,
  ThemeIcon,
  Title,
} from '@mantine/core';
import {
  IconBuilding,
  IconDatabase,
  IconLock,
  IconMail,
  IconShieldCheck,
  IconUser,
} from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';

const Section = ({ title, children }: { title: string; children: React.ReactNode }) => {
  return (
    <Paper
      p={{ base: 18, sm: 28 }}
      radius="xl"
      style={{
        background: 'var(--card-bg)',
        border: '1.5px solid var(--card-border)',
        boxShadow: '0 2px 12px rgba(0,0,0,0.06)',
      }}
    >
      <Stack gap="md">
        <Title order={2} size="h3">
          {title}
        </Title>
        {children}
      </Stack>
    </Paper>
  );
};

const InfoBlock = ({
  icon,
  title,
  children,
}: {
  icon: React.ReactNode;
  title: string;
  children: React.ReactNode;
}) => {
  return (
    <Group gap="sm" align="flex-start" wrap="nowrap">
      <ThemeIcon radius="xl" variant="light" mt={2}>
        {icon}
      </ThemeIcon>

      <Box>
        <Text fw={700}>{title}</Text>
        <Box mt={6}>{children}</Box>
      </Box>
    </Group>
  );
};

const LegalPage = () => {
  const { t } = useTranslation();

  return (
    <Layout>
      <Box py="xl" px="md">
        <Container size="md">
          <Stack gap="xl">
            <Stack gap="xs" align="center">
              <ThemeIcon
                size={54}
                radius="xl"
                style={{
                  background: 'color-mix(in srgb, #339af0 18%, transparent)',
                }}
              >
                <IconShieldCheck size={28} style={{ color: '#339af0' }} />
              </ThemeIcon>

              <Text fw={800} ta="center" style={{ fontSize: 30 }}>
                {t('legal.pageTitle')}
              </Text>

              <Text ta="center" c="dimmed" size="md" maw={620}>
                {t('legal.pageDescription')}
              </Text>
            </Stack>

            <Section title={t('legal.imprint.title')}>
              <Divider />

              <InfoBlock icon={<IconBuilding size={18} />} title={t('legal.imprint.provider')}>
                <Text size="sm">
                  {t('legal.imprint.providerName')}
                  <br />
                  {t('legal.imprint.street')}
                  <br />
                  {t('legal.imprint.city')}
                  <br />
                  {t('legal.imprint.country')}
                </Text>
              </InfoBlock>

              <Divider />

              <Box>
                <Text fw={700}>{t('legal.imprint.representedBy')}</Text>
                <Text size="sm" mt={6}>
                  {t('legal.imprint.representedByName')}
                </Text>
              </Box>

              <Divider />

              <InfoBlock icon={<IconMail size={18} />} title={t('legal.imprint.contact')}>
                <Text size="sm">
                  {t('legal.imprint.email')}:{' '}
                  <Anchor href="mailto:support@mynd.de">support@mynd.de</Anchor>
                  <br />
                  {t('legal.imprint.phone')}: +49 89 12345678
                </Text>
              </InfoBlock>

              <Divider />

              <Box>
                <Text fw={700}>{t('legal.imprint.registerEntry')}</Text>
                <Text size="sm" mt={6}>
                  {t('legal.imprint.registerText')}
                </Text>
              </Box>

              <Divider />

              <Box>
                <Text fw={700}>{t('legal.imprint.vatId')}</Text>
                <Text size="sm" mt={6}>
                  DE123456789
                </Text>
              </Box>

              <Divider />

              <Box>
                <Text fw={700}>{t('legal.imprint.contentResponsible')}</Text>
                <Text size="sm" mt={6}>
                  {t('legal.imprint.contentResponsibleName')}
                  <br />
                  {t('legal.imprint.contentResponsibleStreet')}
                  <br />
                  {t('legal.imprint.contentResponsibleCity')}
                </Text>
              </Box>
            </Section>

            <Section title={t('legal.privacy.title')}>
              <Text>{t('legal.privacy.intro')}</Text>

              <Divider />

              <InfoBlock icon={<IconDatabase size={18} />} title={t('legal.privacy.collectedData')}>
                <List spacing="xs" size="sm">
                  <List.Item>{t('legal.privacy.collectedDataItems.nameEmail')}</List.Item>
                  <List.Item>{t('legal.privacy.collectedDataItems.accountProfile')}</List.Item>
                  <List.Item>{t('legal.privacy.collectedDataItems.learningUsage')}</List.Item>
                  <List.Item>{t('legal.privacy.collectedDataItems.rolesPermissions')}</List.Item>
                </List>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconLock size={18} />} title={t('legal.privacy.processingPurpose')}>
                <List spacing="xs" size="sm">
                  <List.Item>{t('legal.privacy.processingPurposeItems.platform')}</List.Item>
                  <List.Item>{t('legal.privacy.processingPurposeItems.accountsRoles')}</List.Item>
                  <List.Item>
                    {t('legal.privacy.processingPurposeItems.learningProgress')}
                  </List.Item>
                  <List.Item>{t('legal.privacy.processingPurposeItems.improvement')}</List.Item>
                </List>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconShieldCheck size={18} />} title={t('legal.privacy.disclosure')}>
                <Text size="sm">{t('legal.privacy.disclosureText')}</Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconDatabase size={18} />} title={t('legal.privacy.storage')}>
                <Text size="sm">{t('legal.privacy.storageText')}</Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconUser size={18} />} title={t('legal.privacy.userRights')}>
                <Text size="sm">{t('legal.privacy.userRightsText')}</Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconMail size={18} />} title={t('legal.privacy.privacyContact')}>
                <Text size="sm">
                  {t('legal.imprint.email')}:{' '}
                  <Anchor href="mailto:datenschutz@mynd.de">datenschutz@mynd.de</Anchor>
                </Text>
              </InfoBlock>
            </Section>
          </Stack>
        </Container>
      </Box>
    </Layout>
  );
};

export default LegalPage;
