import {
  Avatar,
  Badge,
  Box,
  Button,
  Card,
  Group,
  Stack,
  Text,
  Title,
  Tooltip,
} from '@mantine/core';
import { Dropzone, IMAGE_MIME_TYPE, type FileWithPath } from '@mantine/dropzone';
import { IconLogout2, IconPhotoUp, IconTrash, IconUser, IconUserCog } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { Layout } from '../components/layout.tsx';
import { useUserService } from '../provider/user-provider.tsx';
import keycloak from '../keycloak.ts';
import { logout } from '../auth.ts';
import {
  useDeleteProfilePictureMutation,
  useQueryProfilePicture,
  useUploadProfilePictureMutation,
} from '../api/profile-picture.ts';
import { notifications } from '@mantine/notifications';

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

const AccountPage = () => {
  const { t } = useTranslation();
  const userService = useUserService();
  const username = userService.account.username;

  const { data: profilePicture } = useQueryProfilePicture(username);
  const { mutateAsync: uploadPicture, isPending: isUploading } =
    useUploadProfilePictureMutation(username);
  const { mutateAsync: deletePicture, isPending: isDeleting } =
    useDeleteProfilePictureMutation(username);

  const displayName =
    [userService.account.firstName, userService.account.lastName].filter(Boolean).join(' ') ||
    username ||
    userService.account.email ||
    '';
  const userRole = userService.roles.includes('builder')
    ? t('auth.role_builder')
    : userService.roles.includes('learner')
      ? t('auth.role_learner')
      : undefined;

  const handleDrop = async (files: FileWithPath[]) => {
    const file = files[0];
    if (!file) return;
    try {
      await uploadPicture(file);
    } catch {
      notifications.show({ color: 'red', message: t('common.serverError') });
    }
  };

  const handleDelete = async () => {
    try {
      await deletePicture();
    } catch {
      notifications.show({ color: 'red', message: t('common.serverError') });
    }
  };

  return (
    <Layout>
      <Stack gap="lg" maw={720} mx="auto">
        <Title order={1}>{t('account.title')}</Title>

        <Card withBorder radius="md" p="lg">
          <Stack gap="md">
            <Group align="center" wrap="nowrap">
              <Avatar src={profilePicture?.url} size={96} radius="50%">
                <IconUser size={48} />
              </Avatar>
              <Stack gap={4} style={{ flex: 1, minWidth: 0 }}>
                <Title order={3}>{displayName}</Title>
                {userService.account.email && (
                  <Text c="dimmed" size="sm">
                    {userService.account.email}
                  </Text>
                )}
                {userRole && (
                  <Badge color="blue" variant="light" w="fit-content">
                    {userRole}
                  </Badge>
                )}
              </Stack>
            </Group>

            <Box>
              <Text fw={600} size="sm" mb="xs">
                {t('account.profilePicture')}
              </Text>
              <Dropzone
                onDrop={handleDrop}
                onReject={() =>
                  notifications.show({
                    color: 'red',
                    message: t('common.fileRejected'),
                  })
                }
                maxSize={MAX_FILE_SIZE}
                accept={IMAGE_MIME_TYPE}
                loading={isUploading}
                multiple={false}
              >
                <Group justify="center" gap="md" mih={120} style={{ pointerEvents: 'none' }}>
                  <IconPhotoUp size={32} stroke={1.5} />
                  <Stack gap={4} align="center">
                    <Text size="sm" fw={500}>
                      {t('common.dropYourFileHere')}
                    </Text>
                    <Text size="xs" c="dimmed">
                      {t('common.maxFileSize', { size: '5 MB' })}
                    </Text>
                  </Stack>
                </Group>
              </Dropzone>

              {profilePicture?.url && (
                <Tooltip label={t('account.deleteProfilePicture')} withArrow>
                  <Button
                    variant="light"
                    color="red"
                    leftSection={<IconTrash size={16} />}
                    mt="md"
                    onClick={handleDelete}
                    loading={isDeleting}
                  >
                    {t('account.deleteProfilePicture')}
                  </Button>
                </Tooltip>
              )}
            </Box>
          </Stack>
        </Card>

        <Group justify="space-between">
          <Button
            variant="light"
            leftSection={<IconUserCog size={16} />}
            onClick={() => keycloak.accountManagement()}
          >
            {t('account.editInKeycloak')}
          </Button>
          <Button
            variant="light"
            color="red"
            leftSection={<IconLogout2 size={16} />}
            onClick={logout}
          >
            {t('account.logout')}
          </Button>
        </Group>
      </Stack>
    </Layout>
  );
};

export default AccountPage;
