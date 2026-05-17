import {
  Avatar,
  Badge,
  Box,
  Button,
  Container,
  Group,
  Paper,
  Stack,
  Text,
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
      <Box
        style={{
          minHeight: 'calc(100vh - 152px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '16px',
        }}
      >
        <Container size={520} w="100%">
          <Paper
            p={32}
            radius="lg"
            style={{
              background: '#FFFFFF',
              border: '1.5px solid #D6DCE2',
              boxShadow: '0 24px 60px rgba(31, 42, 68, 0.12)',
            }}
          >
            <Stack gap="lg">
              <Text
                fw={800}
                ta="center"
                style={{ color: '#1F2A44', fontSize: 26, lineHeight: 1.15 }}
              >
                {t('account.title')}
              </Text>

              <Stack gap="sm" align="center">
                <Avatar
                  src={profilePicture?.url}
                  size={110}
                  radius="50%"
                  style={{ border: '3px solid #7CC6E8' }}
                  styles={{
                    placeholder: {
                      background: 'rgba(124, 198, 232, 0.18)',
                      color: '#1B9ED6',
                    },
                  }}
                >
                  <IconUser size={56} />
                </Avatar>
                <Stack gap={2} align="center">
                  <Text fw={700} size="md" style={{ color: '#1F2A44' }}>
                    {displayName}
                  </Text>
                  {userService.account.email && (
                    <Text size="xs" style={{ color: '#5F6F7E' }}>
                      {userService.account.email}
                    </Text>
                  )}
                  {userRole && (
                    <Badge
                      size="sm"
                      variant="light"
                      style={{ background: 'rgba(124, 198, 232, 0.22)', color: '#1B9ED6' }}
                    >
                      {userRole}
                    </Badge>
                  )}
                </Stack>
              </Stack>

              <Stack gap="xs">
                <Text fw={700} size="sm" style={{ color: '#1F2A44' }}>
                  {t('account.profilePicture')}
                </Text>
                <Dropzone
                  onDrop={handleDrop}
                  onReject={() =>
                    notifications.show({ color: 'red', message: t('common.fileRejected') })
                  }
                  maxSize={MAX_FILE_SIZE}
                  accept={IMAGE_MIME_TYPE}
                  loading={isUploading}
                  multiple={false}
                  radius="md"
                  p="md"
                  styles={{
                    root: { border: '2px dashed #7CC6E8', background: '#F7FCFF' },
                  }}
                >
                  <Group justify="center" gap="md" mih={80} style={{ pointerEvents: 'none' }}>
                    <IconPhotoUp size={28} stroke={1.5} color="#1B9ED6" />
                    <Stack gap={2} align="center">
                      <Text size="sm" fw={600} style={{ color: '#1F2A44' }}>
                        {t('common.dropYourFileHere')}
                      </Text>
                      <Text size="xs" style={{ color: '#5F6F7E' }}>
                        {t('common.maxFileSize', { size: '5 MB' })}
                      </Text>
                    </Stack>
                  </Group>
                </Dropzone>
                {profilePicture?.url && (
                  <Tooltip label={t('account.deleteProfilePicture')} withArrow>
                    <Button
                      variant="outline"
                      color="red"
                      size="xs"
                      leftSection={<IconTrash size={14} />}
                      onClick={handleDelete}
                      loading={isDeleting}
                      radius="md"
                    >
                      {t('account.deleteProfilePicture')}
                    </Button>
                  </Tooltip>
                )}
              </Stack>

              <Stack gap="xs">
                <Button
                  leftSection={<IconUserCog size={16} />}
                  fullWidth
                  radius="md"
                  size="sm"
                  onClick={() => keycloak.accountManagement()}
                  style={{
                    backgroundColor: '#3aa3d8',
                    color: '#FFFFFF',
                    fontWeight: 700,
                    boxShadow: '0 8px 18px rgba(58, 163, 216, 0.4)',
                  }}
                >
                  {t('account.editInKeycloak')}
                </Button>
                <Button
                  leftSection={<IconLogout2 size={16} />}
                  variant="outline"
                  fullWidth
                  radius="md"
                  size="sm"
                  onClick={logout}
                  style={{ borderColor: '#E86A6A', color: '#C24747', fontWeight: 700 }}
                >
                  {t('account.logout')}
                </Button>
              </Stack>
            </Stack>
          </Paper>
        </Container>
      </Box>
    </Layout>
  );
};

export default AccountPage;
