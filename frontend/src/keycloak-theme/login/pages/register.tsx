import { useLayoutEffect, useState } from 'react';
import type { JSX } from 'keycloakify/tools/JSX';
import type { LazyOrNot } from 'keycloakify/tools/LazyOrNot';
import { kcSanitize } from 'keycloakify/lib/kcSanitize';
import { getKcClsx, type KcClsx } from 'keycloakify/login/lib/kcClsx';
import type { UserProfileFormFieldsProps } from 'keycloakify/login/UserProfileFormFieldsProps';
import type { PageProps } from 'keycloakify/login/pages/PageProps';
import type { KcContext } from '../KcContext';
import type { I18n } from '../i18n';

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
      <style>
        {`
          html,
          body,
          #root,
          #storybook-root {
            min-height: 100vh !important;
            margin: 0 !important;
            background:
              radial-gradient(
                circle at 50% 0%,
                rgba(124, 198, 232, 0.46) 0%,
                rgba(124, 198, 232, 0.22) 32%,
                rgba(255, 255, 255, 0) 62%
              ),
              linear-gradient(
                180deg,
                #F4FBFF 0%,
                #FFFFFF 56%,
                #F8FAFC 100%
              ) !important;
            background-attachment: fixed !important;
            background-repeat: no-repeat !important;
            background-size: cover !important;
          }

          .mynd-register-page {
            position: relative;
            min-height: 100vh;
            background:
              radial-gradient(
                circle at 50% 0%,
                rgba(124, 198, 232, 0.46) 0%,
                rgba(124, 198, 232, 0.22) 32%,
                rgba(255, 255, 255, 0) 62%
              ),
              linear-gradient(
                180deg,
                #F4FBFF 0%,
                #FFFFFF 56%,
                #F8FAFC 100%
              );
            background-attachment: fixed;
            background-repeat: no-repeat;
            background-size: cover;
            padding-bottom: 56px;
          }

          .mynd-register-header {
            display: flex;
            justify-content: flex-end;
            width: 100%;
            padding: 24px 32px 0;
            box-sizing: border-box;
          }

          .mynd-language-button {
            min-height: 40px;
            border: 1.5px solid #D6DCE2;
            border-radius: 999px;
            padding: 0 14px;
            background: rgba(255, 255, 255, 0.84);
            color: #1F2A44;
            box-shadow: 0 12px 28px rgba(31, 42, 68, 0.08);
          }

          .mynd-language-button:hover {
            background: #FFFFFF;
            border-color: #7CC6E8;
          }

          .mynd-register-card {
            max-width: 100%;
          }

          .mynd-register-card .mantine-InputWrapper-root,
          .mynd-register-card .mantine-TextInput-root,
          .mynd-register-card .mantine-PasswordInput-root,
          .mynd-register-card .mantine-Input-wrapper,
          .mynd-register-card .mantine-PasswordInput-wrapper,
          .mynd-register-card .mantine-Input-input,
          .mynd-register-card .mantine-TextInput-input,
          .mynd-register-card .mantine-PasswordInput-input,
          .mynd-register-card .mantine-PasswordInput-innerInput,
          .mynd-register-card .kcInputGroup,
          .mynd-register-card .pf-c-input-group,
          .mynd-register-card input:not([type="checkbox"]),
          .mynd-register-card textarea,
          .mynd-register-card select {
            width: 100% !important;
            max-width: 100% !important;
            box-sizing: border-box !important;
          }

          .mynd-register-card input:not([type="checkbox"]),
          .mynd-register-card textarea,
          .mynd-register-card select {
            background: #FFFFFF !important;
            border: 2px solid #D6DCE2 !important;
            border-radius: 10px !important;
            color: #1F2A44 !important;
            font-weight: 500 !important;
            min-height: 46px !important;
            padding-left: 15px !important;
            padding-right: 15px !important;
            transition:
              border-color 160ms ease,
              box-shadow 160ms ease,
              background 160ms ease;
          }

          .mynd-register-card .kcInputGroup,
          .mynd-register-card .pf-c-input-group {
            position: relative !important;
            display: flex !important;
            align-items: stretch !important;
          }

          .mynd-register-card .kcInputGroup input:not([type="checkbox"]),
          .mynd-register-card .pf-c-input-group input:not([type="checkbox"]) {
            padding-right: 48px !important;
          }

          .mynd-register-card .kcFormPasswordVisibilityButtonClass,
          .mynd-register-card .pf-c-input-group > .pf-c-button.pf-m-control {
            position: absolute !important;
            top: 50% !important;
            right: 8px !important;
            z-index: 2 !important;
            display: flex !important;
            align-items: center !important;
            justify-content: center !important;
            width: 34px !important;
            min-width: 34px !important;
            height: 34px !important;
            min-height: 34px !important;
            padding: 0 !important;
            margin: 0 !important;
            transform: translateY(-50%) !important;
            border: 0 !important;
            border-radius: 999px !important;
            background: transparent !important;
            color: #1F2A44 !important;
            box-shadow: none !important;
            cursor: pointer !important;
          }

          .mynd-register-card .kcFormPasswordVisibilityButtonClass:hover,
          .mynd-register-card .pf-c-input-group > .pf-c-button.pf-m-control:hover {
            background: #F7FCFF !important;
            color: #1B9ED6 !important;
          }

          .mynd-register-card .kcFormPasswordVisibilityIconShow,
          .mynd-register-card .kcFormPasswordVisibilityIconHide,
          .mynd-register-card .pf-c-input-group > .pf-c-button.pf-m-control i {
            display: block !important;
            width: 18px !important;
            height: 18px !important;
            background-color: currentColor !important;
            font-size: 0 !important;
            line-height: 1 !important;
          }

          .mynd-register-card .kcFormPasswordVisibilityIconShow {
            -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cg fill='none' stroke='black' stroke-linecap='round' stroke-linejoin='round' stroke-width='2'%3E%3Cpath d='M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0'/%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3C/g%3E%3C/svg%3E") center / contain no-repeat !important;
            mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cg fill='none' stroke='black' stroke-linecap='round' stroke-linejoin='round' stroke-width='2'%3E%3Cpath d='M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0'/%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3C/g%3E%3C/svg%3E") center / contain no-repeat !important;
          }

          .mynd-register-card .kcFormPasswordVisibilityIconHide {
            -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cg fill='none' stroke='black' stroke-linecap='round' stroke-linejoin='round' stroke-width='2'%3E%3Cpath d='M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0'/%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3Cpath d='m3 3 18 18'/%3E%3C/g%3E%3C/svg%3E") center / contain no-repeat !important;
            mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cg fill='none' stroke='black' stroke-linecap='round' stroke-linejoin='round' stroke-width='2'%3E%3Cpath d='M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0'/%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3Cpath d='m3 3 18 18'/%3E%3C/g%3E%3C/svg%3E") center / contain no-repeat !important;
          }

          .mynd-register-card input:not([type="checkbox"]):focus,
          .mynd-register-card textarea:focus,
          .mynd-register-card select:focus {
            border-color: #7CC6E8 !important;
            box-shadow: 0 0 0 3px rgba(124, 198, 232, 0.28) !important;
            background: #F7FCFF !important;
            outline: none !important;
          }

          .mynd-register-card label {
            color: #1F2A44 !important;
            font-weight: 700 !important;
            font-size: 14px !important;
            margin-bottom: 7px !important;
          }

          .mynd-register-card .kcLabelWrapperClass,
          .mynd-register-card .col-xs-12.col-sm-12.col-md-12.col-lg-12:has(> label),
          .mynd-register-card .required,
          .mynd-register-card .mantine-InputWrapper-required {
            color: #E86A6A !important;
          }

          .mynd-register-card .kcLabelWrapperClass label,
          .mynd-register-card .kcLabelWrapperClass .kcLabelClass {
            color: #1F2A44 !important;
          }

          .mynd-register-card a {
            color: #1B9ED6 !important;
            font-weight: 700 !important;
            text-decoration: none !important;
          }

          .mynd-register-card a:hover {
            text-decoration: underline !important;
          }

          .mynd-register-card .mantine-Checkbox-input {
            background-color: #FFFFFF !important;
            border-color: #7CC6E8 !important;
          }

          .mynd-register-card .mantine-Checkbox-input:checked {
            background-color: #7DD49B !important;
            border-color: #7DD49B !important;
          }

          .mynd-register-card .mantine-Checkbox-label {
            color: #1F2A44 !important;
            font-weight: 500 !important;
          }

          .mynd-register-card button:disabled {
            background-color: #D6DCE2 !important;
            color: #FFFFFF !important;
            box-shadow: none !important;
          }

          .mynd-register-card ul,
          .mynd-register-card li,
          .mynd-register-card p {
            color: #1F2A44 !important;
          }

          .mynd-register-card .kcInputErrorMessageClass,
          .mynd-register-card span[id^="input-error-"],
          .mynd-register-card [data-kc-msg="error-user-attribute-required"],
          .mynd-register-card .pf-v5-c-helper-text,
          .mynd-register-card .pf-c-helper-text,
          .mynd-register-card .pf-v5-c-form__helper-text,
          .mynd-register-card .pf-c-form__helper-text,
          .mynd-register-card .kcInputHelperTextClass,
          .mynd-register-card [id$="-helper"],
          .mynd-register-card [class*="helper"],
          .mynd-register-card [class*="Helper"] {
            display: none !important;
            visibility: hidden !important;
            height: 0 !important;
            min-height: 0 !important;
            margin: 0 !important;
            padding: 0 !important;
            overflow: hidden !important;
          }

          .mynd-register-card .mantine-InputWrapper-error,
          .mynd-register-card .pf-m-error,
          .mynd-register-card .kc-feedback-text {
            color: #E86A6A !important;
            font-weight: 600 !important;
          }
        `}
      </style>

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