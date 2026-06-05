import { Anchor, Box, Container, Group, Text, useMantineTheme } from '@mantine/core';
import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';

export const Footer = () => {
  const theme = useMantineTheme();
  const { t } = useTranslation();

  return (
    <Box
      component="footer"
      py="md"
      px="md"
      style={{
        borderTop: `1px solid ${theme.other.layoutBorder}`,
        background: theme.other.layoutNavbarBg,
        marginTop: 'auto',
      }}
    >
      <Container size="lg">
        <Group justify="center" gap="xs" wrap="wrap">
          <Text size="sm" c="dimmed">
            © {new Date().getFullYear()} MYnd
          </Text>

          <Text size="sm" c="dimmed">
            ·
          </Text>

          <Anchor component={Link} to="/legal" size="sm" c="dimmed" underline="hover">
            {t('footer.legal')}
          </Anchor>
        </Group>
      </Container>
    </Box>
  );
};