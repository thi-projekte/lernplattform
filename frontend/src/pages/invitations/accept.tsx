import { Alert, Button, Card, Loader, Stack, Text, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { IconAlertCircle, IconCheck, IconMailCheck } from '@tabler/icons-react';
import { useQueryInvitation, useRedeemInvitationMutation } from '../../api/invitation.ts';
import { notifications } from '@mantine/notifications';

interface AcceptInvitePageProps {
  invitationId: string;
  redemptionSecret: string;
}

const AcceptInvitePage = ({ invitationId, redemptionSecret }: AcceptInvitePageProps) => {
  const { t } = useTranslation();

  const { data: invitation, isLoading, isError } = useQueryInvitation(invitationId);
  const { mutate: redeem, isPending, isSuccess } = useRedeemInvitationMutation();

  const handleAccept = () => {
    redeem(
      { id: invitationId, secret: redemptionSecret },
      {
        onSuccess: () => {
          notifications.show({
            color: 'green',
            title: t('common.success'),
            message: t('invitation.accept.successMessage'),
          });
          setTimeout(() => {
            window.location.replace('/');
            window.location.reload();
          }, 1500);
        },
        onError: () => {
          notifications.show({
            color: 'red',
            title: t('common.serverError'),
            message: t('invitation.accept.errorMessage'),
          });
        },
      }
    );
  };

  return (
    <Stack align="center" justify="center" h="100vh" bg="gray.0">
      <Card shadow="sm" padding="xl" radius="md" withBorder w={420}>
        <Stack gap="md" align="center">
          <IconMailCheck size={48} color="var(--mantine-color-blue-6)" />
          <Title order={3} ta="center">
            {t('invitation.accept.title')}
          </Title>

          {isLoading && <Loader size="sm" />}

          {isError && (
            <Alert icon={<IconAlertCircle size={16} />} color="red" variant="light" w="100%">
              {t('invitation.accept.invalidLink')}
            </Alert>
          )}

          {isSuccess && (
            <Alert icon={<IconCheck size={16} />} color="green" variant="light" w="100%">
              {t('invitation.accept.successMessage')}
            </Alert>
          )}

          {invitation && !isSuccess && (
            <>
              <Text size="sm" c="dimmed" ta="center">
                {t('invitation.accept.description')}
              </Text>
              <Button
                fullWidth
                leftSection={<IconCheck size={16} />}
                loading={isPending}
                onClick={handleAccept}
              >
                {t('invitation.accept.button')}
              </Button>
            </>
          )}
        </Stack>
      </Card>
    </Stack>
  );
};

export default AcceptInvitePage;
