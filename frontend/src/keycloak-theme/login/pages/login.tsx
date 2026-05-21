import { useState } from 'react';
import { kcSanitize } from 'keycloakify/lib/kcSanitize';
import type { PageProps } from 'keycloakify/login/pages/PageProps';
import type { KcContext } from '../KcContext';
import type { I18n } from '../i18n';

import {
  Anchor,
  ActionIcon,
  Box,
  Button,
  Checkbox,
  Container,
  Divider,
  Group,
  Image,
  Menu,
  Paper,
  Stack,
  Text,
  TextInput,
  UnstyledButton,
} from '@mantine/core';
import {
  IconAlertCircle,
  IconChevronDown,
  IconEye,
  IconEyeOff,
  IconWorld,
} from '@tabler/icons-react';

import './login.css';

export default function Login(props: PageProps<Extract<KcContext, { pageId: 'login.ftl' }>, I18n>) {
  const { kcContext, i18n } = props;
  const {
    social,
    realm,
    url,
    usernameHidden,
    login,
    auth,
    registrationDisabled,
    messagesPerField,
    message,
    isAppInitiatedAction,
  } = kcContext;
  const { msg, msgStr, currentLanguage, enabledLanguages } = i18n;
  const resourcesPath = (kcContext['x-keycloakify'] as { resourcesPath?: string } | undefined)
    ?.resourcesPath;
  const logoSrc =
    resourcesPath !== undefined
      ? `${resourcesPath}/dist/favicon-logo.png`
      : `${import.meta.env.BASE_URL}mynd-logo.png`;

  const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const canRegister = realm.password && realm.registrationAllowed && !registrationDisabled;
  const hasSocialProviders =
    realm.password && social?.providers !== undefined && social.providers.length !== 0;
  const shouldDisplayMessage =
    message !== undefined && (message.type !== 'warning' || !isAppInitiatedAction);

  return (
    <Box className="mynd-login-page">
      {enabledLanguages.length > 1 && (
        <Box className="mynd-login-header">
          <Menu shadow="md" width={180} position="bottom-end">
            <Menu.Target>
              <UnstyledButton className="mynd-language-button" aria-label={msgStr('languages')}>
                <Group gap={8} wrap="nowrap">
                  <IconWorld size={20} stroke={1.6} />
                  <Text size="sm" fw={700}>
                    {currentLanguage.languageTag.toUpperCase()}
                  </Text>
                  <IconChevronDown size={14} stroke={1.6} />
                </Group>
              </UnstyledButton>
            </Menu.Target>
            <Menu.Dropdown>
              {enabledLanguages.map(({ href, label, languageTag }) => (
                <Menu.Item
                  key={languageTag}
                  component="a"
                  href={href}
                  fw={languageTag === currentLanguage.languageTag ? 700 : 500}
                >
                  {label}
                </Menu.Item>
              ))}
            </Menu.Dropdown>
          </Menu>
        </Box>
      )}

      <Container className="mynd-login-container" size={520} mt="md" mb="xl">
        <Paper
          className="mynd-login-card"
          p={42}
          radius="lg"
          style={{
            background: '#FFFFFF',
            border: '1.5px solid #D6DCE2',
            boxShadow: '0 24px 60px rgba(31, 42, 68, 0.12)',
          }}
        >
          <Stack gap="xl">
            <Stack className="mynd-login-title-block" gap={6} align="center">
              <Image src={logoSrc} alt="MYnd Logo" w={125} fit="contain" />

              <Text
                fw={800}
                ta="center"
                style={{
                  color: '#1F2A44',
                  fontSize: '30px',
                  lineHeight: 1.15,
                }}
              >
                {msg('loginAccountTitle')}
              </Text>
            </Stack>

            {shouldDisplayMessage && (
              <Group
                gap="sm"
                align="flex-start"
                wrap="nowrap"
                p="sm"
                style={{
                  background: '#FDF2F2',
                  border: '1.5px solid #E86A6A',
                  borderRadius: 10,
                  color: '#8C1818',
                }}
              >
                <IconAlertCircle
                  size={20}
                  stroke={1.8}
                  style={{ flex: '0 0 auto', marginTop: 1 }}
                />
                <Text
                  size="sm"
                  fw={700}
                  style={{ color: '#8C1818' }}
                  dangerouslySetInnerHTML={{
                    __html: kcSanitize(message.summary),
                  }}
                />
              </Group>
            )}

            {realm.password && (
              <form
                className="mynd-login-form"
                id="kc-form-login"
                onSubmit={() => {
                  setIsLoginButtonDisabled(true);
                  return true;
                }}
                action={url.loginAction}
                method="post"
              >
                <Stack className="mynd-login-form-fields" gap="md">
                  {!usernameHidden && (
                    <TextInput
                      className="mynd-login-field"
                      label={
                        !realm.loginWithEmailAllowed
                          ? msg('username')
                          : !realm.registrationEmailAsUsername
                            ? msg('usernameOrEmail')
                            : msg('email')
                      }
                      id="username"
                      name="username"
                      defaultValue={login.username ?? ''}
                      autoComplete="username"
                      autoFocus
                      required
                      w="100%"
                      styles={{
                        root: { width: '100%' },
                        wrapper: { width: '100%' },
                        input: { width: '100%' },
                      }}
                      error={
                        messagesPerField.existsError('username', 'password') && (
                          <span
                            dangerouslySetInnerHTML={{
                              __html: kcSanitize(
                                messagesPerField.getFirstError('username', 'password')
                              ),
                            }}
                          />
                        )
                      }
                    />
                  )}

                  <TextInput
                    className="mynd-login-field"
                    type={isPasswordVisible ? 'text' : 'password'}
                    label={msg('password')}
                    id="password"
                    name="password"
                    autoComplete="current-password"
                    required
                    w="100%"
                    styles={{
                      root: { width: '100%' },
                      wrapper: { width: '100%' },
                      input: { width: '100%' },
                    }}
                    rightSection={
                      <ActionIcon
                        type="button"
                        variant="subtle"
                        color="gray"
                        size="sm"
                        aria-label={isPasswordVisible ? 'Hide password' : 'Show password'}
                        onClick={() => setIsPasswordVisible((current) => !current)}
                      >
                        {isPasswordVisible ? <IconEyeOff size={18} /> : <IconEye size={18} />}
                      </ActionIcon>
                    }
                    error={
                      usernameHidden &&
                      messagesPerField.existsError('username', 'password') && (
                        <span
                          dangerouslySetInnerHTML={{
                            __html: kcSanitize(
                              messagesPerField.getFirstError('username', 'password')
                            ),
                          }}
                        />
                      )
                    }
                  />

                  <Group justify="space-between" gap="xs" wrap="wrap">
                    {realm.rememberMe && !usernameHidden && (
                      <Checkbox
                        label={msg('rememberMe')}
                        name="rememberMe"
                        id="rememberMe"
                        defaultChecked={!!login.rememberMe}
                      />
                    )}
                    {realm.resetPasswordAllowed && (
                      <Anchor href={url.loginResetCredentialsUrl} size="sm">
                        {msg('doForgotPassword')}
                      </Anchor>
                    )}
                  </Group>

                  <input type="hidden" name="credentialId" value={auth.selectedCredential} />

                  <Button
                    type="submit"
                    fullWidth
                    radius="md"
                    disabled={isLoginButtonDisabled}
                    name="login"
                    style={{
                      backgroundColor: '#7CC6E8',
                      color: '#FFFFFF',
                      fontWeight: 700,
                      minHeight: '46px',
                      boxShadow: '0 12px 28px rgba(124, 198, 232, 0.32)',
                    }}
                  >
                    {msgStr('doLogIn')}
                  </Button>
                </Stack>
              </form>
            )}

            {hasSocialProviders && (
              <Stack gap="sm">
                <Divider label={msg('identity-provider-login-label')} labelPosition="center" />
                <Stack gap="xs">
                  {(social?.providers ?? []).map((p) => (
                    <Button
                      key={p.alias}
                      component="a"
                      href={p.loginUrl}
                      variant="default"
                      radius="md"
                      fullWidth
                      leftSection={p.iconClasses && <i className={p.iconClasses} />}
                    >
                      {kcSanitize(p.displayName)}
                    </Button>
                  ))}
                </Stack>
              </Stack>
            )}

            {canRegister && (
              <Stack gap="xs">
                <Text ta="center" size="sm" style={{ color: '#5F6F7E' }}>
                  {msg('noAccount')}
                </Text>
                <Button
                  component="a"
                  href={url.registrationUrl}
                  variant="outline"
                  radius="md"
                  fullWidth
                  style={{
                    borderColor: '#7CC6E8',
                    color: '#1B9ED6',
                    fontWeight: 800,
                    minHeight: '40px',
                  }}
                >
                  {msg('doRegister')}
                </Button>
              </Stack>
            )}
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
