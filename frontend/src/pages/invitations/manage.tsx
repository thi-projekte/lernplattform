import {
  Alert,
  Badge,
  Button,
  Card,
  Group,
  Loader,
  Paper,
  RingProgress,
  Stack,
  Table,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { IconAlertCircle, IconCheck, IconMail, IconSend } from '@tabler/icons-react';
import { Layout } from '../../components/layout.tsx';
import {
  useQueryInvitationStatus,
  useQuerySentInvitations,
  useSendInvitationMutation,
} from '../../api/invitation.ts';
import { notifications } from '@mantine/notifications';

const ManageInvitationsPage = () => {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [page] = useState(0);

  const { data: status, isLoading: statusLoading } = useQueryInvitationStatus();
  const { data: sent, isLoading: sentLoading } = useQuerySentInvitations(page, 10);
  const { mutate: sendInvitation, isPending: isSending } = useSendInvitationMutation();

  const total = (status?.invitationsLeft ?? 0) + (status?.invitationsAlreadySent ?? 0);
  const usedPercent = total > 0 ? ((status?.invitationsAlreadySent ?? 0) / total) * 100 : 0;

  const handleSend = () => {
    if (!email.trim()) return;
    sendInvitation(email.trim(), {
      onSuccess: () => {
        notifications.show({
          color: 'green',
          title: t('common.success'),
          message: t('invitation.manage.sent', { email }),
        });
        setEmail('');
      },
      onError: () => {
        notifications.show({
          color: 'red',
          title: t('common.serverError'),
          message: t('invitation.manage.sendError'),
        });
      },
    });
  };

  return (
    <Layout>
      <Stack gap="lg" maw={680}>
        <Title order={2}>{t('invitation.manage.title')}</Title>

        {statusLoading ? (
          <Loader size="sm" />
        ) : (
          <Group gap="lg" align="flex-start" wrap="nowrap">
            <RingProgress
              size={120}
              thickness={10}
              sections={[{ value: usedPercent, color: 'blue' }]}
              label={
                <Stack gap={0} align="center">
                  <Text ta="center" size="sm" fw={700}>
                    {status?.invitationsLeft ?? 0}
                  </Text>
                  <Text size="xs" c="dimmed" fw={400}>
                    {t('invitation.manage.left')}
                  </Text>
                </Stack>
              }
            />
            <Stack gap={4} pt={8}>
              <Text size="sm" c="dimmed">
                {t('invitation.manage.leftDetail', { count: status?.invitationsLeft ?? 0 })}
              </Text>
              <Text size="sm" c="dimmed">
                {t('invitation.manage.sentDetail', { count: status?.invitationsAlreadySent ?? 0 })}
              </Text>
            </Stack>
          </Group>
        )}

        <Card withBorder radius="md" p="md">
          <Stack gap="sm">
            <Text fw={600}>{t('invitation.manage.sendTitle')}</Text>
            {(status?.invitationsLeft ?? 1) === 0 && (
              <Alert icon={<IconAlertCircle size={16} />} color="orange" variant="light">
                {t('invitation.manage.noInvitationsLeft')}
              </Alert>
            )}
            <Group gap="sm" wrap="nowrap">
              <TextInput
                placeholder={t('invitation.manage.emailPlaceholder')}
                leftSection={<IconMail size={16} />}
                value={email}
                onChange={(e) => setEmail(e.currentTarget.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                disabled={(status?.invitationsLeft ?? 0) === 0}
                style={{ flex: 1 }}
              />
              <Button
                leftSection={<IconSend size={16} />}
                loading={isSending}
                disabled={(status?.invitationsLeft ?? 0) === 0 || !email.trim()}
                onClick={handleSend}
              >
                {t('invitation.manage.send')}
              </Button>
            </Group>
          </Stack>
        </Card>

        <Paper withBorder radius="md" p="md">
          <Title order={4} mb="sm">
            {t('invitation.manage.historyTitle')}
          </Title>
          {sentLoading ? (
            <Loader size="sm" />
          ) : !sent?.results.length ? (
            <Text c="dimmed" size="sm">
              {t('invitation.manage.noHistory')}
            </Text>
          ) : (
            <Table>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>{t('invitation.manage.columns.date')}</Table.Th>
                  <Table.Th>{t('invitation.manage.columns.status')}</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {sent.results.map((inv) => (
                  <Table.Tr key={inv.id}>
                    <Table.Td>
                      <Text size="sm">{new Date(inv.createdAt).toLocaleDateString()}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge
                        size="sm"
                        color="green"
                        variant="light"
                        leftSection={<IconCheck size={12} />}
                      >
                        {t('invitation.manage.statusSent')}
                      </Badge>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          )}
        </Paper>
      </Stack>
    </Layout>
  );
};

export default ManageInvitationsPage;
