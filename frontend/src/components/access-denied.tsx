import { Button, rem, Stack, Text, Title } from '@mantine/core';
import { IconArrowLeft, IconLockOff } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';

const AccessDenied = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  return (
    <Stack align="center" gap="md">
      <div style={{ position: 'relative', marginBottom: rem(20) }}>
        <IconLockOff size={80} stroke={1.5} color="var(--mantine-color-red-filled)" />
      </div>

      <Title order={1} ta="center" fw={900} fz={{ base: 32, sm: 48 }}>
        {t('auth.accessDenied')}
      </Title>

      <Text c="dimmed" size="lg" ta="center" maw={500} mx="auto">
        {t('auth.noAccessPageText')}
      </Text>
      <Button
        variant="light"
        color="gray"
        leftSection={<IconArrowLeft size={18} />}
        onClick={() => navigate(-1)}
      >
        {t('common.back')}
      </Button>
    </Stack>
  );
};

export default AccessDenied;
