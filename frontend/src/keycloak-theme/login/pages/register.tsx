import { useState, useLayoutEffect } from 'react';
import type { JSX } from 'keycloakify/tools/JSX';
import type { LazyOrNot } from 'keycloakify/tools/LazyOrNot';
import { kcSanitize } from 'keycloakify/lib/kcSanitize';
import { getKcClsx, type KcClsx } from 'keycloakify/login/lib/kcClsx';
import type { UserProfileFormFieldsProps } from 'keycloakify/login/UserProfileFormFieldsProps';
import type { PageProps } from 'keycloakify/login/pages/PageProps';
import type { KcContext } from '../KcContext';
import type { I18n } from '../i18n';

// Mantine Imports
import {
  Button,
  Checkbox,
  Text,
  Stack,
  Box,
  Anchor,
  Paper,
  InputWrapper,
  Container,
} from '@mantine/core';

type RegisterProps = PageProps<Extract<KcContext, { pageId: 'register.ftl' }>, I18n> & {
  UserProfileFormFields: LazyOrNot<(props: UserProfileFormFieldsProps) => JSX.Element>;
  doMakeUserConfirmPassword: boolean;
};

export default function Register(props: RegisterProps) {
  const {
    kcContext,
    i18n,
    doUseDefaultCss,
    Template,
    classes,
    UserProfileFormFields,
    doMakeUserConfirmPassword,
  } = props;
  const { kcClsx } = getKcClsx({ doUseDefaultCss, classes });
  const {
    messageHeader,
    url,
    messagesPerField,
    recaptchaRequired,
    recaptchaVisible,
    recaptchaSiteKey,
    recaptchaAction,
    termsAcceptanceRequired,
  } = kcContext;
  const { msg, msgStr, advancedMsg } = i18n;

  const [isFormSubmittable, setIsFormSubmittable] = useState(false);
  const [areTermsAccepted, setAreTermsAccepted] = useState(false);

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
    <Template
      kcContext={kcContext}
      i18n={i18n}
      doUseDefaultCss={doUseDefaultCss}
      classes={classes}
      headerNode={messageHeader !== undefined ? advancedMsg(messageHeader) : msg('registerTitle')}
      displayMessage={messagesPerField.exists('global')}
    >
      <Container>
        <Paper withBorder shadow="sm" p="md" radius="md">
          <form id="kc-register-form" action={url.registrationAction} method="post">
            <Stack gap="md">
              {/* Dynamic Fields (FirstName, LastName, Email, etc.) */}
              <UserProfileFormFields
                kcContext={kcContext}
                i18n={i18n}
                kcClsx={kcClsx}
                onIsFormSubmittableValueChange={setIsFormSubmittable}
                doMakeUserConfirmPassword={doMakeUserConfirmPassword}
              />

              <Text size="xs" c="dimmed">
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

              <Stack gap="sm" mt="xl">
                <Button
                  type="submit"
                  fullWidth
                  disabled={!isFormSubmittable || (termsAcceptanceRequired && !areTermsAccepted)}
                  // Keycloak Recaptcha integration if hidden
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

                <Anchor href={url.loginUrl} size="sm" ta="center">
                  {msg('backToLogin')}
                </Anchor>
              </Stack>
            </Stack>
          </form>
        </Paper>
      </Container>
    </Template>
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
      <Text size="sm" fw={500}>
        {msg('termsTitle')}
      </Text>
      <Box
        p="xs"
        bg="var(--mantine-color-gray-0)"
        style={{ borderRadius: '4px', fontSize: '12px', maxHeight: '100px', overflowY: 'auto' }}
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
        />
      </InputWrapper>
    </Stack>
  );
}
