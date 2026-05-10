import { useLayoutEffect, useState } from 'react';
import type { JSX } from 'keycloakify/tools/JSX';
import type { LazyOrNot } from 'keycloakify/tools/LazyOrNot';
import { kcSanitize } from 'keycloakify/lib/kcSanitize';
import { getKcClsx, type KcClsx } from 'keycloakify/login/lib/kcClsx';
import type { UserProfileFormFieldsProps } from 'keycloakify/login/UserProfileFormFieldsProps';
import type { PageProps } from 'keycloakify/login/pages/PageProps';
import type { KcContext } from '../KcContext';
import type { I18n } from '../i18n';

import './register.css';

import {
  Anchor,
  Box,
  Button,
  Checkbox,
  Container,
  Group,
  Image,
  InputWrapper,
  Menu,
  Paper,
  Stack,
  Text,
  UnstyledButton,
} from '@mantine/core';

import { IconAlertCircle, IconChevronDown, IconWorld } from '@tabler/icons-react';

type RegisterProps = PageProps<Extract<KcContext, { pageId: 'register.ftl' }>, I18n> & {
  UserProfileFormFields: LazyOrNot<(props: UserProfileFormFieldsProps) => JSX.Element>;
  doMakeUserConfirmPassword: boolean;
};

export default function Register(props: RegisterProps) {
  const {
    kcContext,
    i18n,
    doUseDefaultCss,
    classes,
    UserProfileFormFields,
    doMakeUserConfirmPassword,
  } = props;

  const { kcClsx } = getKcClsx({ doUseDefaultCss, classes });

  const {
    url,
    messagesPerField,
    recaptchaRequired,
    recaptchaVisible,
    recaptchaSiteKey,
    recaptchaAction,
    termsAcceptanceRequired,
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

  const languageTag = currentLanguage.languageTag.toLowerCase();

  const registerTexts = (() => {
    if (languageTag.startsWith('fr')) {
      return {
        title: "S'inscrire à MYnd",
        loginPrefix: 'Ou ',
        loginLink: 'se connecter avec un compte existant',
      };
    }

    if (languageTag.startsWith('es')) {
      return {
        title: 'Registrarse en MYnd',
        loginPrefix: 'O ',
        loginLink: 'iniciar sesión con una cuenta existente',
      };
    }

    return {
      title: 'Register with MYnd',
      loginPrefix: 'Or ',
      loginLink: 'sign in with an existing account',
    };
  })();

  const languageOrder = ['en', 'fr', 'es'];

  const sortedEnabledLanguages = [...enabledLanguages].sort((a, b) => {
    const aTag = a.languageTag.toLowerCase();
    const bTag = b.languageTag.toLowerCase();

    const aIndex = languageOrder.findIndex((tag) => aTag.startsWith(tag));
    const bIndex = languageOrder.findIndex((tag) => bTag.startsWith(tag));

    if (aIndex !== -1 && bIndex !== -1) {
      return aIndex - bIndex;
    }

    if (aIndex !== -1) {
      return -1;
    }

    if (bIndex !== -1) {
      return 1;
    }

    return a.label.localeCompare(b.label);
  });

  const [areTermsAccepted, setAreTermsAccepted] = useState(false);

  const shouldDisplayMessage =
    message !== undefined && (message.type !== 'warning' || !isAppInitiatedAction);

  useLayoutEffect(() => {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    (window as any)['onSubmitRecaptcha'] = () => {
      (document.getElementById('kc-register-form') as HTMLFormElement).requestSubmit();
    };

    return () => {
      delete (window as any)['onSubmitRecaptcha'];
    };
  }, []);

  return (
    <Box className="mynd-register-page">
      {enabledLanguages.length > 1 && (
        <Box className="mynd-register-header">
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
              {sortedEnabledLanguages.map(({ href, label, languageTag }) => (
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

      <Container size={520} mt="md" mb="xl">
        <Paper
          className="mynd-register-card"
          p={42}
          radius="lg"
          style={{
            background: '#FFFFFF',
            border: '1.5px solid #D6DCE2',
            boxShadow: '0 24px 60px rgba(31, 42, 68, 0.12)',
          }}
        >
          <Stack gap="xl">
            <Stack gap={6} align="center">
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
                {registerTexts.title}
              </Text>

              <Text ta="center" size="sm" style={{ color: '#1F2A44' }}>
                {registerTexts.loginPrefix}
                <Anchor href={url.loginUrl} size="sm">
                  {registerTexts.loginLink}
                </Anchor>
              </Text>
            </Stack>

            {shouldDisplayMessage && message !== undefined && (
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

            <form id="kc-register-form" action={url.registrationAction} method="post" noValidate>
              <Stack gap="md">
                <UserProfileFormFields
                  kcContext={kcContext}
                  i18n={i18n}
                  kcClsx={kcClsx}
                  onIsFormSubmittableValueChange={() => undefined}
                  doMakeUserConfirmPassword={doMakeUserConfirmPassword}
                />

                <Text size="xs" style={{ color: '#8C1818', fontWeight: 500 }}>
                  {'* '}
                  {msg('requiredFields')}
                </Text>

                {termsAcceptanceRequired && (
                  <TermsAcceptance
                    i18n={i18n}
                    kcClsx={kcClsx}
                    messagesPerField={messagesPerField}
                    areTermsAccepted={areTermsAccepted}
                    onAreTermsAcceptedValueChange={setAreTermsAccepted}
                  />
                )}

                {recaptchaRequired && (recaptchaVisible || recaptchaAction === undefined) && (
                  <Box
                    className="g-recaptcha"
                    data-size="compact"
                    data-sitekey={recaptchaSiteKey}
                    data-action={recaptchaAction}
                  />
                )}

                <Button
                  type="submit"
                  fullWidth
                  radius="md"
                  disabled={termsAcceptanceRequired && !areTermsAccepted}
                  style={{
                    backgroundColor: '#7CC6E8',
                    color: '#FFFFFF',
                    fontWeight: 700,
                    minHeight: '46px',
                    boxShadow: '0 12px 28px rgba(124, 198, 232, 0.32)',
                  }}
                  {...(recaptchaRequired && !recaptchaVisible && recaptchaAction !== undefined
                    ? {
                        className: 'g-recaptcha',
                        'data-sitekey': recaptchaSiteKey,
                        'data-callback': 'onSubmitRecaptcha',
                        'data-action': recaptchaAction,
                      }
                    : {})}
                >
                  {msgStr('doRegister')}
                </Button>
              </Stack>
            </form>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}

function TermsAcceptance(props: {
  i18n: I18n;
  kcClsx: KcClsx;
  messagesPerField: Pick<KcContext['messagesPerField'], 'existsError' | 'get'>;
  areTermsAccepted: boolean;
  onAreTermsAcceptedValueChange: (areTermsAccepted: boolean) => void;
}) {
  const { i18n, messagesPerField, areTermsAccepted, onAreTermsAcceptedValueChange } = props;
  const { msg } = i18n;

  return (
    <Stack gap="xs">
      <Text size="sm" fw={700} style={{ color: '#1F2A44' }}>
        {msg('termsTitle')}
      </Text>

      <Box
        p="sm"
        style={{
          backgroundColor: '#F7FCFF',
          border: '1.5px solid #D6DCE2',
          borderRadius: '10px',
          fontSize: '12px',
          color: '#1F2A44',
          maxHeight: '110px',
          overflowY: 'auto',
        }}
      >
        {msg('termsText')}
      </Box>

      <InputWrapper
        error={
          messagesPerField.existsError('termsAccepted') && (
            <span
              dangerouslySetInnerHTML={{
                __html: kcSanitize(messagesPerField.get('termsAccepted')),
              }}
            />
          )
        }
      >
        <Checkbox
          label={msg('acceptTerms')}
          name="termsAccepted"
          id="termsAccepted"
          checked={areTermsAccepted}
          onChange={(event) => onAreTermsAcceptedValueChange(event.currentTarget.checked)}
          styles={{
            input: {
              backgroundColor: '#FFFFFF',
              borderColor: '#7CC6E8',
            },
            label: {
              color: '#1F2A44',
              fontSize: '14px',
              fontWeight: 500,
            },
          }}
        />
      </InputWrapper>
    </Stack>
  );
}
