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
                Impressum & Datenschutzerklärung
              </Text>

              <Text ta="center" c="dimmed" size="md" maw={620}>
                Rechtliche Informationen zur Plattform MYnd und Hinweise zur Verarbeitung
                personenbezogener Daten.
              </Text>
            </Stack>

            <Section title="Impressum">
              <Divider />

              <InfoBlock icon={<IconBuilding size={18} />} title="Anbieter">
                <Text size="sm">
                  MYnd GmbH
                  <br />
                  Musterstraße 12
                  <br />
                  85049 Ingolstadt
                  <br />
                  Deutschland
                </Text>
              </InfoBlock>

              <Divider />

              <Box>
                <Text fw={700}>Vertreten durch</Text>
                <Text size="sm" mt={6}>
                  Florian Weisenberger
                </Text>
              </Box>

              <Divider />

              <InfoBlock icon={<IconMail size={18} />} title="Kontakt">
                <Text size="sm">
                  E-Mail: <Anchor href="mailto:support@mynd.de">support@mynd.de</Anchor>
                  <br />
                  Telefon: +49 89 12345678
                </Text>
              </InfoBlock>

              <Divider />

              <Box>
                <Text fw={700}>Registereintrag</Text>
                <Text size="sm" mt={6}>
                  Amtsgericht Ingolstadt, HRB 123456
                </Text>
              </Box>

              <Divider />

              <Box>
                <Text fw={700}>Umsatzsteuer-ID</Text>
                <Text size="sm" mt={6}>
                  DE123456789
                </Text>
              </Box>

              <Divider />

              <Box>
                <Text fw={700}>Verantwortlich für den Inhalt</Text>
                <Text size="sm" mt={6}>
                  Florian Weisenberger
                  <br />
                  Musterstraße 12
                  <br />
                  85049 Ingolstadt
                </Text>
              </Box>

            </Section>

            <Section title="Datenschutzerklärung">
              <Text>
                MYnd verarbeitet personenbezogene Daten ausschließlich zur Bereitstellung der
                Plattform auf Grundlage von Art. 6 DSGVO.
              </Text>

              <Divider />

              <InfoBlock icon={<IconDatabase size={18} />} title="Erhobene Daten">
                <List spacing="xs" size="sm">
                  <List.Item>Name und E-Mail-Adresse</List.Item>
                  <List.Item>Benutzerkonto- und Profildaten</List.Item>
                  <List.Item>Lernfortschritt und Nutzungsdaten der Plattform</List.Item>
                  <List.Item>Rollen- und Berechtigungsdaten</List.Item>
                </List>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconLock size={18} />} title="Zweck der Verarbeitung">
                <List spacing="xs" size="sm">
                  <List.Item>Bereitstellung und Verwaltung der Lernplattform</List.Item>
                  <List.Item>Verwaltung von Benutzerkonten und Rollen</List.Item>
                  <List.Item>Speicherung und Anzeige von Lernfortschritten</List.Item>
                  <List.Item>Verbesserung der Lerninhalte und Plattformfunktionen</List.Item>
                </List>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconShieldCheck size={18} />} title="Weitergabe">
                <Text size="sm">
                  Eine Weitergabe erfolgt nur an technische Hosting- und Infrastrukturdienstleister,
                  sofern dies für den Betrieb der Plattform notwendig ist.
                </Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconDatabase size={18} />} title="Speicherung">
                <Text size="sm">
                  Personenbezogene Daten werden nur so lange gespeichert, wie dies für die Nutzung
                  der Plattform und zur Erfüllung gesetzlicher Pflichten erforderlich ist.
                </Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconUser size={18} />} title="Rechte der Nutzer">
                <Text size="sm">
                  Nutzer haben das Recht auf Auskunft, Berichtigung, Löschung, Einschränkung der
                  Verarbeitung und Widerspruch gegen die Verarbeitung.
                </Text>
              </InfoBlock>

              <Divider />

              <InfoBlock icon={<IconMail size={18} />} title="Kontakt Datenschutz">
                <Text size="sm">
                  E-Mail: <Anchor href="mailto:datenschutz@mynd.de">datenschutz@mynd.de</Anchor>
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
