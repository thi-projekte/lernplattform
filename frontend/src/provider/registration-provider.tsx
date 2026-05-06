import type { ReactNode } from 'react';
import type { KeycloakResourceAccess } from 'keycloak-js';
import keycloak from '../keycloak.ts';
import Onboarding from '../pages/onboarding/onboarding.tsx';

const RegistrationProvider = ({ children }: { children: ReactNode }) => {
  const resourceAccess: KeycloakResourceAccess = keycloak.tokenParsed
    ?.resource_access as KeycloakResourceAccess;
  const isRegistered = resourceAccess['mynd']?.roles.length > 0;

  if (isRegistered) {
    return <>{children}</>;
  }

  return <Onboarding />;
};

export default RegistrationProvider;
