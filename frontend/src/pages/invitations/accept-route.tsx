import { useSearchParams } from 'react-router';
import { Alert, Stack } from '@mantine/core';
import { IconAlertCircle } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import AcceptInvitePage from './accept.tsx';

const AcceptInviteRoute = () => {
  const { t } = useTranslation();
  const [params] = useSearchParams();
  const id = params.get('id') ?? '';
  const secret = params.get('redemptionSecret') ?? '';

  if (!id || !secret) {
    return (
      <Stack align="center" justify="center" h="100vh" bg="gray.0">
        <Alert icon={<IconAlertCircle size={16} />} color="red" variant="light" w={420}>
          {t('invitation.accept.invalidLink')}
        </Alert>
      </Stack>
    );
  }

  return <AcceptInvitePage invitationId={id} redemptionSecret={secret} />;
};

export default AcceptInviteRoute;
