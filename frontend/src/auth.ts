import keycloak from './keycloak.ts';
import type { KeycloakResourceAccess } from 'keycloak-js';

// @ts-expect-error Idk why the fuck this does not work
export enum Role {
  Builder = 'builder',
}

export const isGranted = (roles: Role[]) => {
  const resourceAccess: KeycloakResourceAccess = keycloak.tokenParsed
    ?.resource_access as KeycloakResourceAccess;
  return roles.some((role) => resourceAccess['mynd']?.roles.indexOf(role) > -1);
};

export const logout = () => {
  keycloak.logout({
    redirectUri: window.location.origin
  })
}
