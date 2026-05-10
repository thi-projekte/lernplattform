import { useState } from 'react';
import { kcSanitize } from 'keycloakify/lib/kcSanitize';
import type { PageProps } from 'keycloakify/login/pages/PageProps';
import type { KcContext } from '../KcContext';
import type { I18n } from '../i18n';

// Mantine Imports
import {
  TextInput,
  PasswordInput,
  Button,
  Checkbox,
  Stack,
  Group,
  Divider,
  Text,
  Paper,
  Anchor,
} from '@mantine/core';

export default function Login(props: PageProps<Extract<KcContext, { pageId: 'login.ftl' }>, I18n>) {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;
  const {
    social,
    realm,
    url,
    usernameHidden,
    login,
    auth,
    registrationDisabled,
    messagesPerField,
  } = kcContext;
  const { msg, msgStr } = i18n;

  const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);

  return (
    <Template
      kcContext={kcContext}
      i18n={i18n}
      doUseDefaultCss={doUseDefaultCss}
      classes={classes}
      displayMessage={!messagesPerField.existsError('username', 'password')}
      headerNode={msg('loginAccountTitle')}
      displayInfo={realm.password && realm.registrationAllowed && !registrationDisabled}
      infoNode={
        <Stack gap="xs" mt="md">
          <Text size="sm" ta="center">
            {msg('noAccount')}
          </Text>
          <Button component="a" href={url.registrationUrl} variant="outline" fullWidth>
            {msg('doRegister')}
          </Button>
        </Stack>
      }
      socialProvidersNode={
        <>
          {realm.password && social?.providers !== undefined && social.providers.length !== 0 && (
            <Stack mt="xl">
              <Divider label={msg('identity-provider-login-label')} labelPosition="center" />
              <Group grow>
                {social.providers.map((p) => (
                  <Button
                    key={p.alias}
                    component="a"
                    href={p.loginUrl}
                    variant="default"
                    leftSection={p.iconClasses && <i className={p.iconClasses} />}
                  >
                    {kcSanitize(p.displayName)}
                  </Button>
                ))}
              </Group>
            </Stack>
          )}
        </>
      }
    >
      <Paper>
        {realm.password && (
          <form
            id="kc-form-login"
            onSubmit={() => {
              setIsLoginButtonDisabled(true);
              return true;
            }}
            action={url.loginAction}
            method="post"
          >
            <Stack gap="md">
              {!usernameHidden && (
                <TextInput
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

              <PasswordInput
                label={msg('password')}
                id="password"
                name="password"
                autoComplete="current-password"
                required
                error={
                  usernameHidden &&
                  messagesPerField.existsError('username', 'password') && (
                    <span
                      dangerouslySetInnerHTML={{
                        __html: kcSanitize(messagesPerField.getFirstError('username', 'password')),
                      }}
                    />
                  )
                }
              />

              <Group justify="space-between">
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

              <Button type="submit" fullWidth mt="md" disabled={isLoginButtonDisabled} name="login">
                {msgStr('doLogIn')}
              </Button>
            </Stack>
          </form>
        )}
      </Paper>
    </Template>
  );
}
